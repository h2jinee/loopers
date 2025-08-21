package com.loopers.application.order;

import com.loopers.application.payment.PaymentProcessor;
import com.loopers.domain.order.*;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderFacade {
    
    private final OrderRepository orderRepository;
    private final OrderProcessor orderProcessor;
    private final PaymentProcessor paymentProcessor;
    
    @Transactional
    public OrderResult.CreateResult createOrder(OrderCriteria.Create criteria) {
        OrderCommand.Create command = criteria.toCommand();
        
        // 1. 주문 처리 (재고 차감, 주문 생성, 재고 예약)
        Order order = orderProcessor.processOrder(command);
        
        // 2. 결제 처리 (포인트 차감)
        paymentProcessor.processPayment(order, order.getUserId());
        
        // 3. 주문 확정 (상태 변경, 재고 예약 확정)
        orderProcessor.confirmOrder(order);
        
        // 4. 결과 반환
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
