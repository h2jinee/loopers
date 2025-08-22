package com.loopers.application.order;

import com.loopers.domain.order.*;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.stock.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProcessor {
    
    private final OrderService orderService;
    private final ProductService productService;
    private final StockService stockService;
    
    /**
     * 주문 생성 (재고 차감 포함, 결제 대기 상태)
     * 독립 트랜잭션 - 주문 생성과 재고 차감이 원자적으로 처리
     */
    @Transactional
    public Order processOrder(OrderCommand.Create command) {
        // 1. 상품 정보 조회
        ProductCommand.GetOne getProductCommand = new ProductCommand.GetOne(command.productId());
        ProductInfo product = productService.getProduct(getProductCommand);

        // 2. 재고 차감
        stockService.decreaseStock(command.productId(), command.quantity());

        // 3. 주문 생성
        OrderCommand.CreateWithProduct createCommand = 
            OrderCommand.CreateWithProduct.from(command, product);
        Order order = orderService.createOrder(createCommand);

        // 4. 재고 예약 생성
        stockService.createReservation(order.getId(), command.productId(), command.quantity());
        
        log.info("주문 생성 완료 (결제 대기) - orderId: {}", order.getId());
        return order;
    }
    
    /**
     * 주문 확정 (결제 완료 후 호출)
     * 독립 트랜잭션 - 주문 상태 변경과 예약 확정이 원자적으로 처리
     */
    @Transactional
    public void confirmOrder(Long orderId) {
        // 1. 주문 상태 변경
        Order order = orderService.findById(orderId);
        order.confirmPayment();
        orderService.save(order);

        // 2. 재고 예약 확정
        stockService.confirmReservations(orderId);
        
        log.info("주문 확정 완료 - orderId: {}", orderId);
    }

    /**
     * 주문 취소
     * 독립 트랜잭션 - 주문 취소와 재고 복원이 원자적으로 처리
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        // 1. 주문 상태 변경
        Order order = orderService.findById(orderId);
        order.failPayment();
        orderService.save(order);

        // 2. 재고 예약 취소 및 재고 복원
        stockService.cancelReservationsAndRestoreStock(orderId);
        
        log.info("주문 취소 완료 - orderId: {}", orderId);
    }
}
