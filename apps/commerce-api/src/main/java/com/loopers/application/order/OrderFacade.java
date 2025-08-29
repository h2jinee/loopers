package com.loopers.application.order;

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
    private final OrderApplicationEventPublisher orderEventPublisher;
    
    /**
     * 주문 생성
     */
    @Transactional
    public OrderResult.CreateResult createOrder(OrderCriteria.Create criteria) {
        log.info("순수 주문 생성 시작 - userId: {}, productId: {}", 
            criteria.userId(), criteria.productId());
        
        // 1. 주문 생성 (메인 로직: 재고/포인트 검증 포함)
        OrderCommand.Create orderCommand = criteria.toOrderCommand();
        Order order = orderProcessor.processOrder(orderCommand, criteria.pointToUse());
        
        // 2. 주문 완료 이벤트 발행 (스냅샷 포함)
        OrderCompleted event = OrderCompleted.from(order, criteria);
        orderEventPublisher.publish(event);
        
        log.info("주문 생성 완료 및 이벤트 발행 - orderId: {}, totalAmount: {}",
            order.getId(), order.getTotalAmount());
        
        // 3. 결과 반환
        return buildResult(order);
    }
    
    /**
     * 주문 확정
     */
    @Transactional
    public void confirmOrder(Long orderId) {
        log.info("주문 확정 처리 - orderId: {}", orderId);
        
        orderProcessor.confirmOrder(orderId);
        
        // 주문 확정 이벤트 발행
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, 
                "주문을 찾을 수 없습니다. orderId: " + orderId));
        
        OrderConfirmed event = OrderConfirmed.from(orderId, order.getUserId());
        orderEventPublisher.publish(event);
    }
    
    /**
     * 주문 실패 처리
     */
    @Transactional
    public void failOrder(Long orderId, String reason) {
        log.info("주문 실패 처리 - orderId: {}, reason: {}", orderId, reason);
        
        orderProcessor.cancelOrder(orderId);
        
        // 주문 취소 이벤트 발행
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, 
                "주문을 찾을 수 없습니다. orderId: " + orderId));
        
        OrderCancelled event = OrderCancelled.from(orderId, order.getUserId(), reason);
        orderEventPublisher.publish(event);
    }
    
    /**
     * 결과 빌드
     */
    private OrderResult.CreateResult buildResult(Order order) {
        OrderLine firstOrderLine = order.getOrderLines().getFirst();
        OrderInfo.CreateResult domainInfo = OrderInfo.CreateResult.from(
            order, 
            firstOrderLine.getProductId(), 
            firstOrderLine.getQuantity()
        );
        return OrderResult.CreateResult.from(domainInfo);
    }
    
    /**
     * 주문 상세 조회
     */
    public OrderResult.Detail getOrderDetail(OrderCriteria.GetDetail criteria) {
        OrderCommand.GetDetail command = criteria.toCommand();
        
        Order order = orderRepository.findByIdAndUserId(command.orderId(), command.userId())
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, 
                "주문을 찾을 수 없습니다. orderId: " + command.orderId()));
        
        OrderInfo.Detail domainInfo = OrderInfo.Detail.from(order);
        return OrderResult.Detail.from(domainInfo);
    }
    
    /**
     * 사용자 주문 목록 조회
     */
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
