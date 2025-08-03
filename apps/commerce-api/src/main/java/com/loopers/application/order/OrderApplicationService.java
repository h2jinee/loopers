package com.loopers.application.order;

import com.loopers.domain.order.*;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentDomainService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductDomainService;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.point.PointCommand;
import com.loopers.domain.point.PointDomainService;
import com.loopers.domain.point.PointEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderApplicationService {
    
    private final OrderDomainService orderDomainService;
    private final ProductDomainService productDomainService;
    private final PaymentDomainService paymentDomainService;
    private final PointDomainService pointDomainService;
    private final StockReservationRepository stockReservationRepository;
    
    @Transactional
    public OrderInfo.CreateResult createOrder(OrderCommand.Create command) {
        try {
            ProductCommand.GetOne getProductCommand = new ProductCommand.GetOne(command.productId());
            ProductEntity product = productDomainService.getProduct(getProductCommand);
            
            OrderCommand.CreateWithProduct createWithProductCommand = 
                OrderCommand.CreateWithProduct.from(command, product);
            OrderDomainService.OrderCreationResult creationResult = 
                orderDomainService.createOrder(createWithProductCommand);
            
            OrderEntity order = creationResult.order();
            
            order = orderDomainService.saveOrder(order);
            Long orderId = order.getId();
            
            StockReservationEntity reservation = orderDomainService.createStockReservation(
                orderId, creationResult.productId(), creationResult.quantity()
            );
            stockReservationRepository.save(reservation);
            
            productDomainService.decreaseStock(new ProductCommand.DecreaseStock(
                command.productId(), command.quantity()
            ));
            
            try {
                PointCommand.GetOne getPointCommand = new PointCommand.GetOne(command.userId());
                PointEntity point = pointDomainService.getPointEntity(getPointCommand);
                
                PaymentCommand.ProcessPayment paymentCommand = 
                    new PaymentCommand.ProcessPayment(order, command.userId());
                PaymentCommand.ProcessPaymentWithPoint paymentWithPointCommand = 
                    PaymentCommand.ProcessPaymentWithPoint.from(paymentCommand, point);
                paymentDomainService.processPayment(paymentWithPointCommand);
                
                PointCommand.Use useCommand = new PointCommand.Use(
                    command.userId(), order.getTotalAmount(), order.getId()
                );
                pointDomainService.usePoint(useCommand);
                
                order.confirmPayment();
                orderDomainService.updateOrder(order);
                
                orderDomainService.confirmStockReservations(orderId);
                
            } catch (CoreException e) {
                order.failPayment();
                orderDomainService.updateOrder(order);
                
                orderDomainService.cancelStockReservations(orderId);
                
                for (StockReservationEntity stockReservation : stockReservationRepository.findByOrderId(orderId)) {
                    if (stockReservation.getStatus() == StockReservationEntity.ReservationStatus.RESERVED) {
                        ProductCommand.IncreaseStock increaseCommand = new ProductCommand.IncreaseStock(
                            stockReservation.getProductId(), stockReservation.getQuantity()
                        );
                        productDomainService.increaseStock(increaseCommand);
                    }
                }
                
                throw e;
            }
            
            return OrderInfo.CreateResult.from(order);
            
        } catch (CoreException e) {
            throw e;
        } catch (Exception e) {
            throw new CoreException(ErrorType.INTERNAL_ERROR, "주문 처리 중 오류가 발생했습니다.");
        }
    }
    
    public OrderInfo.Detail getOrderDetail(Long orderId, String userId) {
        OrderCommand.GetDetail command = new OrderCommand.GetDetail(userId, orderId);
        OrderEntity order = orderDomainService.getUserOrder(command);
        return OrderInfo.Detail.from(order);
    }
    
    public Page<OrderInfo.Summary> getUserOrders(String userId, Integer page, Integer size) {
        OrderCommand.GetList command = OrderCommand.GetList.of(userId, page, size);
        Page<OrderEntity> orders = orderDomainService.getUserOrders(command);
        
        return orders.map(OrderInfo.Summary::from);
    }
}
