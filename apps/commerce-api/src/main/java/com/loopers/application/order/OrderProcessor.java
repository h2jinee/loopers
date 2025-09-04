package com.loopers.application.order;

import com.loopers.domain.order.*;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.stock.StockInfo;
import com.loopers.domain.stock.StockService;
import com.loopers.domain.point.PointService;
import com.loopers.domain.common.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
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
    private final PointService pointService;
    private final StockService stockService;

    /**
     * 주문 생성
     */
    @Transactional
    public Order processOrder(OrderCommand.Create command, Money pointToUse) {
        // 1. 상품 정보 조회
        ProductCommand.GetOne getProductCommand = new ProductCommand.GetOne(command.productId());
        ProductInfo product = productService.getProduct(getProductCommand);

        // 2. 재고 검증
        StockInfo stockInfo = stockService.getStockInfo(command.productId());
        if (stockInfo.quantity() < command.quantity()) {
            throw new CoreException(ErrorType.CONFLICT, "재고가 부족합니다.");
        }

        // 3. 포인트 검증
        if (pointToUse != null && !pointToUse.isZero()) {
            pointService.validateAvailablePoints(command.userId(), pointToUse);
        }

        // 4. 주문 생성
        OrderCommand.CreateWithProduct createCommand =
            OrderCommand.CreateWithProduct.from(command, product);
        Order order = orderService.createOrder(createCommand);

        log.info("주문서(재고 / 포인트 검증 포함) 생성 완료 - orderId: {}", order.getId());
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
