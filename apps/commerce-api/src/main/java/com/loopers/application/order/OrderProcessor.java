package com.loopers.application.order;

import com.loopers.application.stock.StockFacade;
import com.loopers.domain.order.*;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.ProductDomainInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProcessor {
    
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final StockFacade stockFacade;
    
    /**
     * 주문 처리
     * - 상품 조회, 재고 차감, 주문 생성, 재고 예약
     */
    public Order processOrder(OrderCommand.Create command) {
        // 1. 상품 정보 조회
        ProductCommand.GetOne getProductCommand = new ProductCommand.GetOne(command.productId());
        ProductDomainInfo product = productService.getProduct(getProductCommand);
        
        // 2. 재고 차감
        stockFacade.decreaseStock(command.productId(), command.quantity());
        
        // 3. 주문 생성
        OrderCommand.CreateWithProduct createWithProductCommand = 
            OrderCommand.CreateWithProduct.from(command, product);
        Order order = orderService.createOrder(createWithProductCommand);
        Order savedOrder = orderRepository.save(order);
        
        // 4. 재고 예약 생성
        stockFacade.createReservation(savedOrder.getId(), command.productId(), command.quantity());
        
        log.info("주문 처리 완료 - orderId: {}, productId: {}, quantity: {}", 
            savedOrder.getId(), command.productId(), command.quantity());
        
        return savedOrder;
    }
    
    /**
     * 주문 확정
     * - 주문 상태 변경 및 재고 예약 확정
     */
    public void confirmOrder(Order order) {
        // 1. 주문 상태 변경(결제 완료)
        order.confirmPayment();
        orderRepository.save(order);
        
        // 2. 재고 예약 확정
        stockFacade.confirmReservations(order.getId());
        
        log.info("주문 확정 완료 - orderId: {}", order.getId());
    }
    
    /**
     * 주문 취소
     * - 주문 상태 변경, 재고 예약 취소 및 복원
     */
    public void cancelOrder(Order order) {
        // 1. 주문 상태 변경(실패)
        order.failPayment();
        orderRepository.save(order);
        
        // 2. 재고 예약 취소 및 재고 복원
        stockFacade.cancelReservationsAndRestoreStock(order.getId());
        
        log.info("주문 취소 완료 - orderId: {}", order.getId());
    }
}
