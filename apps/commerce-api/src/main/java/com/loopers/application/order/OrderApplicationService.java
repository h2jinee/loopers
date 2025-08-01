package com.loopers.application.order;

import com.loopers.domain.order.*;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentDomainService;
import com.loopers.domain.product.ProductDomainService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderApplicationService {
    
    private final OrderDomainService orderDomainService;
    private final ProductDomainService productDomainService;
    private final PaymentDomainService paymentDomainService;
    private final StockReservationRepository stockReservationRepository;
    
    @Transactional
    public OrderInfo.CreateResult createOrder(OrderCommand.Create command) {
        try {
            // 1. 주문 생성 및 재고 예약
            OrderDomainService.OrderCreationResult creationResult = 
                orderDomainService.createOrder(command);
            
            OrderEntity order = creationResult.order();
            
            // 2. 주문 저장
            order = orderDomainService.saveOrder(order);
            Long orderId = order.getId();
            
            // 3. 재고 예약 생성 및 저장
            StockReservationEntity reservation = orderDomainService.createStockReservation(
                orderId, creationResult.productId(), creationResult.quantity()
            );
            stockReservationRepository.save(reservation);
            
            // 4. 재고 임시 차감
            productDomainService.decreaseStock(new com.loopers.domain.product.ProductCommand.DecreaseStock(
                command.productId(), command.quantity()
            ));
            
            // 5. 결제 처리
            try {
                PaymentCommand.ProcessPayment paymentCommand = 
                    new PaymentCommand.ProcessPayment(order, command.userId());
                paymentDomainService.processPayment(paymentCommand);
                
                // 결제 성공
                order.confirmPayment();
                orderDomainService.updateOrder(order);
                
                // 재고 예약 확정
                orderDomainService.confirmStockReservations(orderId);
                
            } catch (CoreException e) {
                // 결제 실패
                order.failPayment();
                orderDomainService.updateOrder(order);
                
                // 재고 롤백
                orderDomainService.cancelStockReservations(orderId);
                
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
