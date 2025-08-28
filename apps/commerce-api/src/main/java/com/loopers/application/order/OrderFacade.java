package com.loopers.application.order;

import com.loopers.application.payment.PaymentProcessor;
import com.loopers.domain.common.Money;
import com.loopers.domain.order.*;
import com.loopers.domain.payment.PaymentResult;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderFacade {
    
    private final OrderRepository orderRepository;
    private final OrderProcessor orderProcessor;
    private final PaymentProcessor paymentProcessor;
    
    /**
     * 주문 생성
     */
    public OrderResult.CreateResult createOrder(OrderCriteria.Create criteria) {
        OrderCommand.Create orderCommand = criteria.toOrderCommand();
        
        // 1. 주문 생성
        Order order = orderProcessor.processOrder(orderCommand);
        
        try {
            // 2. 결제 처리
            PaymentResult paymentResult = processPayment(criteria, order);
            
            // 3. 주문 확정
            orderProcessor.confirmOrder(order.getId());
            
            return buildResult(order);
            
        } catch (Exception e) {
            // 결제 실패 시 주문 취소
            compensateOrder(order.getId(), e);
            throw new CoreException(ErrorType.INTERNAL_ERROR, 
                "주문 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 결제 처리 조율 - 3가지 케이스 처리
     */
    private PaymentResult processPayment(OrderCriteria.Create criteria, Order order) {
        Money totalAmount = order.getTotalAmount();
        Money pointToUse = criteria.pointToUse();
        
        try {
            // 케이스 1: 포인트 미사용 - PG 전액 결제
            if (pointToUse == null || pointToUse.isZero()) {
                return paymentProcessor.processPgPayment(
                    criteria.toPgPaymentCommand(order, totalAmount)
                );
            }
            
            // 케이스 3: 포인트 전액 결제
            if (pointToUse.equals(totalAmount)) {
                return paymentProcessor.processPointPayment(
                    criteria.toPointPaymentCommand(order)
                );
            }
            
            // 케이스 2: 복합 결제 (포인트 + PG)
            return processCombinedPayment(criteria, order, pointToUse);
            
        } catch (Exception e) {
            // 결제 처리 실패 - 에러 로깅만 (보상은 상위에서 처리)
            log.error("결제 처리 실패 - orderId: {}", order.getId(), e);
            throw e;
        }
    }
    
    /**
     * 복합 결제 처리 (포인트 + PG)
     * 포인트 결제 후 PG 결제, 실패 시 포인트 롤백
     */
    private PaymentResult processCombinedPayment(
        OrderCriteria.Create criteria, 
        Order order, 
        Money pointToUse
    ) {
        Money remainingAmount = order.getTotalAmount().minus(pointToUse);
        
        // 1. 포인트 결제 (독립 트랜잭션)
        PaymentResult pointResult = paymentProcessor.processPointPayment(
            criteria.toPointPaymentCommand(order)
        );
        
        try {
            // 2. PG 결제 (독립 트랜잭션)
            PaymentResult pgResult = paymentProcessor.processPgPayment(
                criteria.toPgPaymentCommand(order, remainingAmount)
            );
            
            // 3. 복합 결제 결과 반환
            return PaymentResult.combined(pointResult, pgResult);
            
        } catch (Exception pgException) {
            // PG 실패 시 포인트 롤백 (별도 트랜잭션)
            log.error("PG 결제 실패, 포인트 롤백 시작 - orderId: {}", order.getId(), pgException);
            rollbackPointPayment(order.getId(), criteria.userId(), pointToUse);
            throw pgException;
        }
    }
    
    /**
     * 포인트 결제 롤백
     */
    private void rollbackPointPayment(Long orderId, String userId, Money amount) {
        try {
            paymentProcessor.cancelPointPayment(orderId, userId, amount);
            log.info("포인트 롤백 성공 - orderId: {}", orderId);
        } catch (Exception rollbackEx) {
            log.error("포인트 롤백 실패 - orderId: {}, 수동 보상 필요", orderId, rollbackEx);
            // TODO: 보상 배치 또는 알림
        }
    }
    
    /**
     * 주문 보상 처리
     */
    private void compensateOrder(Long orderId, Exception e) {
        log.error("주문 보상 처리 시작 - orderId: {}", orderId, e);
        
        try {
            // 주문 취소 (독립 트랜잭션)
            orderProcessor.cancelOrder(orderId);
            log.info("주문 취소 성공 - orderId: {}", orderId);
        } catch (Exception cancelEx) {
            log.error("주문 취소 실패 - orderId: {}, 수동 보상 필요", orderId, cancelEx);
            // TODO: 보상 배치 또는 알림
        }
    }
    
    /**
     * 결과 빌드
     */
    private OrderResult.CreateResult buildResult(Order order) {
        // Order에서 첫 번째 주문 라인의 정보 추출
        OrderLine firstOrderLine = order.getOrderLines().getFirst();
        OrderInfo.CreateResult domainInfo = OrderInfo.CreateResult.from(
            order, 
            firstOrderLine.getProductId(), 
            firstOrderLine.getQuantity()
        );
        OrderResult.CreateResult result = OrderResult.CreateResult.from(domainInfo);
        
        log.info("주문 생성 완료 - userId: {}, orderId: {}, totalAmount: {}",
            order.getUserId(), order.getId(), order.getTotalAmount());
        
        return result;
    }
    
    public OrderResult.Detail getOrderDetail(OrderCriteria.GetDetail criteria) {
        OrderCommand.GetDetail command = criteria.toCommand();
        
        Order order = orderRepository.findByIdAndUserId(command.orderId(), command.userId())
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, 
                "주문을 찾을 수 없습니다. orderId: " + command.orderId()));
        
        OrderInfo.Detail domainInfo = OrderInfo.Detail.from(order);
        return OrderResult.Detail.from(domainInfo);
    }
    
    public Page<OrderResult.Summary> getUserOrders(OrderCriteria.GetList criteria) {
        OrderCommand.GetList command = criteria.toCommand();
        
        Pageable pageable = PageRequest.of(
            command.page(),
            command.size(),
            Sort.by(Sort.Direction.DESC, "createdAt")
        );
        
        Page<Order> orders = orderRepository.findByUserId(command.userId(), pageable);
        
        return orders.map(order -> {
            OrderInfo.Summary domainInfo = OrderInfo.Summary.from(order);
            return OrderResult.Summary.from(domainInfo);
        });
    }
}
