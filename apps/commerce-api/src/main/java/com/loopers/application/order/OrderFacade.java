package com.loopers.application.order;

import com.loopers.domain.order.*;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductStockService;
import com.loopers.domain.point.PointCommand;
import com.loopers.domain.point.PointService;
import com.loopers.domain.point.PointEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderFacade {
    
    private final OrderService orderService;
    private final ProductService productService;
    private final ProductStockService productStockService;
    private final PaymentService paymentService;
    private final PointService pointService;
    
    @Transactional
    public OrderResult.CreateResult createOrder(OrderCriteria.Create criteria) {
        OrderCommand.Create command = criteria.toCommand();
        try {
            ProductCommand.GetOne getProductCommand = new ProductCommand.GetOne(command.productId());
            ProductEntity product = productService.getProduct(getProductCommand);
            
            // 동시성 제어 (재고 감소 + 주문 생성)
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
            orderService.saveStockReservation(reservation);
            
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
                
                for (StockReservationEntity stockReservation : orderService.findStockReservationsByOrderId(orderId)) {
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
    
    /**
     * 비관적 락
     */
	@Transactional(isolation = Isolation.READ_COMMITTED)
    public OrderResult.CreateResult createOrderPessimistic(OrderCriteria.Create criteria) {
        OrderCommand.Create command = criteria.toCommand();
        log.info("주문 처리 시작 (비관적 락) - userId: {}, productId: {}, quantity: {}", 
                command.userId(), command.productId(), command.quantity());
        
        try {
            ProductCommand.GetOne getProductCommand = new ProductCommand.GetOne(command.productId());
            ProductEntity product = productService.getProduct(getProductCommand);
            
            // 비관적 락으로 재고 차감
            productStockService.decreaseStockPessimistic(command.productId(), command.quantity());
            
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
            orderService.saveStockReservation(reservation);
            
            try {
                // 포인트 조회
                PointCommand.GetOne getPointCommand = new PointCommand.GetOne(command.userId());
                PointEntity point = pointService.getPointEntity(getPointCommand);
                
                // 결제 처리
                PaymentCommand.ProcessPayment paymentCommand = 
                    new PaymentCommand.ProcessPayment(order, command.userId());
                PaymentCommand.ProcessPaymentWithPoint paymentWithPointCommand = 
                    PaymentCommand.ProcessPaymentWithPoint.from(paymentCommand, point);
                paymentService.processPayment(paymentWithPointCommand);
                
                // 포인트 차감 (비관적 락)
                PointCommand.Use useCommand = new PointCommand.Use(
                    command.userId(), order.getTotalAmount(), order.getId()
                );
                pointService.usePointPessimistic(useCommand);
                
                order.confirmPayment();
                orderService.updateOrder(order);
                orderService.confirmStockReservations(orderId);
                
            } catch (CoreException e) {
                order.failPayment();
                orderService.updateOrder(order);
                orderService.cancelStockReservations(orderId);
                
                List<StockReservationEntity> reservedStocks = orderService.findStockReservationsByOrderId(orderId)
                    .stream()
                    .filter(sr -> sr.getStatus() == StockReservationEntity.ReservationStatus.RESERVED)
                    .toList();
                
                if (!reservedStocks.isEmpty()) {
                    Map<Long, Integer> stockUpdates = reservedStocks.stream()
                        .collect(Collectors.toMap(
                            StockReservationEntity::getProductId,
                            StockReservationEntity::getQuantity,
                            Integer::sum
                        ));
                    productStockService.restoreStocks(stockUpdates);
                }
                
                throw e;
            }
            
            log.info("주문 처리 완료 - orderId: {}", orderId);
            OrderInfo.CreateResult domainInfo = OrderInfo.CreateResult.from(order);
            return OrderResult.CreateResult.from(domainInfo);
            
        } catch (CoreException e) {
            log.error("주문 처리 실패 - userId: {}, productId: {}, error: {}", 
                    command.userId(), command.productId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("주문 처리 중 예상치 못한 오류 발생", e);
            throw new CoreException(ErrorType.INTERNAL_ERROR, "주문 처리 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 주문 생성 (낙관적 락)
     */
    @Retryable(
        retryFor = ObjectOptimisticLockingFailureException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 100)
    )
    @Transactional
    public OrderResult.CreateResult createOrderOptimistic(OrderCriteria.Create criteria) {
        OrderCommand.Create command = criteria.toCommand();
        log.info("주문 처리 시작 (낙관적 락) - userId: {}, productId: {}, quantity: {}", 
                command.userId(), command.productId(), command.quantity());
        
        try {
            ProductCommand.GetOne getProductCommand = new ProductCommand.GetOne(command.productId());
            ProductEntity product = productService.getProduct(getProductCommand);
            
            // 재고 차감 (낙관적 락)
            productStockService.decreaseStockOptimistic(command.productId(), command.quantity());
            
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
            orderService.saveStockReservation(reservation);
            
            try {
                // 포인트 조회 먼저 하고
                PointCommand.GetOne getPointCommand = new PointCommand.GetOne(command.userId());
                PointEntity point = pointService.getPointEntity(getPointCommand);
                
                // 결제 처리
                PaymentCommand.ProcessPayment paymentCommand = 
                    new PaymentCommand.ProcessPayment(order, command.userId());
                PaymentCommand.ProcessPaymentWithPoint paymentWithPointCommand = 
                    PaymentCommand.ProcessPaymentWithPoint.from(paymentCommand, point);
                paymentService.processPayment(paymentWithPointCommand);
                
                // 낙관적 락으로 포인트 차감
                PointCommand.Use useCommand = new PointCommand.Use(
                    command.userId(), order.getTotalAmount(), order.getId()
                );
                pointService.usePointOptimistic(useCommand);
                
                order.confirmPayment();
                orderService.updateOrder(order);
                orderService.confirmStockReservations(orderId);
                
            } catch (CoreException e) {
                order.failPayment();
                orderService.updateOrder(order);
                orderService.cancelStockReservations(orderId);
                
                List<StockReservationEntity> reservedStocks = orderService.findStockReservationsByOrderId(orderId)
                    .stream()
                    .filter(sr -> sr.getStatus() == StockReservationEntity.ReservationStatus.RESERVED)
                    .toList();
                
                if (!reservedStocks.isEmpty()) {
                    Map<Long, Integer> stockUpdates = reservedStocks.stream()
                        .collect(Collectors.toMap(
                            StockReservationEntity::getProductId,
                            StockReservationEntity::getQuantity,
                            Integer::sum
                        ));
                    productStockService.restoreStocks(stockUpdates);
                }
                
                throw e;
            }
            
            log.info("주문 처리 완료 - orderId: {}", orderId);
            OrderInfo.CreateResult domainInfo = OrderInfo.CreateResult.from(order);
            return OrderResult.CreateResult.from(domainInfo);
            
        } catch (CoreException e) {
            log.error("주문 처리 실패 - userId: {}, productId: {}, error: {}", 
                    command.userId(), command.productId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("주문 처리 중 예상치 못한 오류 발생", e);
            throw new CoreException(ErrorType.INTERNAL_ERROR, "주문 처리 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 락 없이 주문 생성 (테스트 용도)
     * 동시성 문제를 확인하기 위한 메서드
     */
    @Transactional
    public OrderResult.CreateResult createOrderNoLock(OrderCriteria.Create criteria) {
        OrderCommand.Create command = criteria.toCommand();
        log.info("주문 처리 시작 (락 없음) - userId: {}, productId: {}, quantity: {}", 
                command.userId(), command.productId(), command.quantity());
        
        try {
            ProductCommand.GetOne getProductCommand = new ProductCommand.GetOne(command.productId());
            ProductEntity product = productService.getProduct(getProductCommand);
            
            // 락 없이 재고 차감
            productStockService.decreaseStockNoLock(command.productId(), command.quantity());
            
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
            orderService.saveStockReservation(reservation);
            
            try {
                // 포인트 조회 먼저 하고
                PointCommand.GetOne getPointCommand = new PointCommand.GetOne(command.userId());
                PointEntity point = pointService.getPointEntity(getPointCommand);
                
                // 결제 처리
                PaymentCommand.ProcessPayment paymentCommand = 
                    new PaymentCommand.ProcessPayment(order, command.userId());
                PaymentCommand.ProcessPaymentWithPoint paymentWithPointCommand = 
                    PaymentCommand.ProcessPaymentWithPoint.from(paymentCommand, point);
                paymentService.processPayment(paymentWithPointCommand);
                
                // 락 없이 포인트 차감
                PointCommand.Use useCommand = new PointCommand.Use(
                    command.userId(), order.getTotalAmount(), order.getId()
                );
                pointService.usePointNoLock(useCommand);
                
                order.confirmPayment();
                orderService.updateOrder(order);
                orderService.confirmStockReservations(orderId);
                
            } catch (CoreException e) {
                order.failPayment();
                orderService.updateOrder(order);
                orderService.cancelStockReservations(orderId);
                
                List<StockReservationEntity> reservedStocks = orderService.findStockReservationsByOrderId(orderId)
                    .stream()
                    .filter(sr -> sr.getStatus() == StockReservationEntity.ReservationStatus.RESERVED)
                    .toList();
                
                if (!reservedStocks.isEmpty()) {
                    Map<Long, Integer> stockUpdates = reservedStocks.stream()
                        .collect(Collectors.toMap(
                            StockReservationEntity::getProductId,
                            StockReservationEntity::getQuantity,
                            Integer::sum
                        ));
                    productStockService.restoreStocks(stockUpdates);
                }
                
                throw e;
            }
            
            log.info("주문 처리 완료 - orderId: {}", orderId);
            OrderInfo.CreateResult domainInfo = OrderInfo.CreateResult.from(order);
            return OrderResult.CreateResult.from(domainInfo);
            
        } catch (CoreException e) {
            log.error("주문 처리 실패 - userId: {}, productId: {}, error: {}", 
                    command.userId(), command.productId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("주문 처리 중 예상치 못한 오류 발생", e);
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
