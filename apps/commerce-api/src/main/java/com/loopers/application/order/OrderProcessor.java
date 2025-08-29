package com.loopers.application.order;

import com.loopers.domain.order.*;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.stock.StockService;
import com.loopers.domain.point.PointService;
import com.loopers.domain.common.Money;
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
    private final PointService pointService;
    
    /**
     * 주문 생성 - 메인 로직만 동기 처리 (실패시 주문 실패)
     */
    @Transactional
    public Order processOrder(OrderCommand.Create command, Money pointToUse) {
        // 1. 상품 정보 조회 (메인 로직)
        ProductCommand.GetOne getProductCommand = new ProductCommand.GetOne(command.productId());
        ProductInfo product = productService.getProduct(getProductCommand);

        // 2. 재고 차감 (메인 로직 - 실패시 주문 불가)
        stockService.decreaseStock(command.productId(), command.quantity());

        // 3. 주문 생성 (주문의 본질적 책임)
        OrderCommand.CreateWithProduct createCommand = 
            OrderCommand.CreateWithProduct.from(command, product);
        Order order = orderService.createOrder(createCommand);

        // 4. 포인트 차감 (메인 로직 - 부족시 주문 불가)
        if (pointToUse != null && !pointToUse.isZero()) {
            pointService.usePoint(command.userId(), pointToUse, order.getId());
        }

        // 5. 재고 예약 생성 (메인 로직)
        stockService.createReservation(order.getId(), command.productId(), command.quantity());
        
        log.info("메인 로직 완료 - orderId: {}, 재고차감: {}, 포인트사용: {}", 
            order.getId(), command.quantity(), pointToUse);
        return order;
    }
    
    /**
     * 주문 확정(주문 상태 변경)
     */
    @Transactional
    public void confirmOrder(Long orderId) {
        Order order = orderService.findById(orderId);
        order.confirmPayment();
        orderService.save(order);
        
        log.info("주문 확정 완료 - orderId: {}", orderId);
    }

    /**
     * 주문 취소(주문 상태 변경)
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderService.findById(orderId);
        order.failPayment();
        orderService.save(order);
        
        log.info("주문 취소 완료 - orderId: {}", orderId);
    }
}
