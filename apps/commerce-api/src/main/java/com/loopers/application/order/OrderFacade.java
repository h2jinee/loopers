package com.loopers.application.order;

import com.loopers.domain.order.*;
import com.loopers.infrastructure.order.StockReservationJpaRepository;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.point.PointCommand;
import com.loopers.domain.point.PointService;
import com.loopers.domain.point.PointEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderFacade {
    
    private final OrderService orderService;
    private final ProductService productService;
    private final PaymentService paymentService;
    private final PointService pointService;
    private final StockReservationJpaRepository stockReservationJpaRepository;
    
    @Transactional
    public OrderResult.CreateResult createOrder(OrderCriteria.Create criteria) {
        OrderCommand.Create command = criteria.toCommand();
        try {
            ProductCommand.GetOne getProductCommand = new ProductCommand.GetOne(command.productId());
            ProductEntity product = productService.getProduct(getProductCommand);
            
            // 동시성 제어: 재고 감소와 주문 생성을 함께 처리
            productService.decreaseStock(new ProductCommand.DecreaseStock(
                command.productId(), command.quantity()
            ));
            
            OrderCommand.CreateWithProduct createWithProductCommand = 
                OrderCommand.CreateWithProduct.from(command, product);
            OrderService.OrderCreationResult creationResult = 
                orderService.createOrderWithoutStockCheck(createWithProductCommand);
            
            OrderEntity order = creationResult.order();
            
            order = orderService.saveOrder(order);
            Long orderId = order.getId();
            
            StockReservationEntity reservation = orderService.createStockReservation(
                orderId, creationResult.productId(), creationResult.quantity()
            );
            stockReservationJpaRepository.save(reservation);
            
            try {
                PointCommand.GetOne getPointCommand = new PointCommand.GetOne(command.userId());
                PointEntity point = pointService.getPointEntity(getPointCommand);
                
                PaymentCommand.ProcessPayment paymentCommand = 
                    new PaymentCommand.ProcessPayment(order, command.userId());
                PaymentCommand.ProcessPaymentWithPoint paymentWithPointCommand = 
                    PaymentCommand.ProcessPaymentWithPoint.from(paymentCommand, point);
                paymentService.processPayment(paymentWithPointCommand);
                
                PointCommand.Use useCommand = new PointCommand.Use(
                    command.userId(), order.getTotalAmount(), order.getId()
                );
                pointService.usePoint(useCommand);
                
                order.confirmPayment();
                orderService.updateOrder(order);
                
                orderService.confirmStockReservations(orderId);
                
            } catch (CoreException e) {
                order.failPayment();
                orderService.updateOrder(order);
                
                orderService.cancelStockReservations(orderId);
                
                for (StockReservationEntity stockReservation : stockReservationJpaRepository.findByOrderId(orderId)) {
                    if (stockReservation.getStatus() == StockReservationEntity.ReservationStatus.RESERVED) {
                        ProductCommand.IncreaseStock increaseCommand = new ProductCommand.IncreaseStock(
                            stockReservation.getProductId(), stockReservation.getQuantity()
                        );
                        productService.increaseStock(increaseCommand);
                    }
                }
                
                throw e;
            }
            
            OrderInfo.CreateResult domainInfo = OrderInfo.CreateResult.from(order);
            return OrderResult.CreateResult.from(domainInfo);
            
        } catch (CoreException e) {
            throw e;
        } catch (Exception e) {
            throw new CoreException(ErrorType.INTERNAL_ERROR, "주문 처리 중 오류가 발생했습니다.");
        }
    }
    
    public OrderResult.Detail getOrderDetail(OrderCriteria.GetDetail criteria) {
        OrderCommand.GetDetail command = criteria.toCommand();
        OrderEntity order = orderService.getUserOrder(command);
        OrderInfo.Detail domainInfo = OrderInfo.Detail.from(order);
        return OrderResult.Detail.from(domainInfo);
    }
    
    public Page<OrderResult.Summary> getUserOrders(OrderCriteria.GetList criteria) {
        OrderCommand.GetList command = criteria.toCommand();
        Page<OrderEntity> orders = orderService.getUserOrders(command);
        
        return orders.map(order -> {
            OrderInfo.Summary domainInfo = OrderInfo.Summary.from(order);
            return OrderResult.Summary.from(domainInfo);
        });
    }
}
