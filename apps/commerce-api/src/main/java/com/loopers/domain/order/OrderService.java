package com.loopers.domain.order;

import com.loopers.domain.common.Money;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductStockService;
import com.loopers.infrastructure.order.StockReservationJpaRepository;
import com.loopers.infrastructure.order.OrderJpaRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final StockReservationJpaRepository stockReservationJpaRepository;
    private final OrderJpaRepository orderJpaRepository;
    private final ProductStockService productStockService;
    
    public OrderCreationResult createOrder(OrderCommand.CreateWithProduct command) {
        ProductEntity product = command.product();
        
        if (!productStockService.isAvailable(product.getId())) {
            throw new CoreException(ErrorType.CONFLICT, 
                "구매할 수 없는 상품입니다. 상품명: " + product.getNameKo());
        }
        
        if (!productStockService.hasStock(product.getId(), command.quantity())) {
            throw new CoreException(ErrorType.CONFLICT, 
                "재고가 부족합니다. 상품명: " + product.getNameKo());
        }
        
        OrderEntity order = new OrderEntity(command.userId(), command.receiverInfo());
        
        Money totalPrice = product.getTotalPrice();
        OrderLineEntity orderLine = new OrderLineEntity(
            product.getId(),
            product.getNameKo(),
            command.quantity(),
            totalPrice
        );
        order.addOrderLine(orderLine);
        
        return new OrderCreationResult(order, command.productId(), command.quantity());
    }
    
    public OrderCreationResult createOrderWithoutStockCheck(OrderCommand.CreateWithProduct command) {
        ProductEntity product = command.product();
        
        OrderEntity order = new OrderEntity(command.userId(), command.receiverInfo());
        
        Money totalPrice = product.getTotalPrice();
        OrderLineEntity orderLine = new OrderLineEntity(
            product.getId(),
            product.getNameKo(),
            command.quantity(),
            totalPrice
        );
        order.addOrderLine(orderLine);
        
        return new OrderCreationResult(order, command.productId(), command.quantity());
    }
    
    public StockReservationEntity createStockReservation(Long orderId, Long productId, Integer quantity) {
        return new StockReservationEntity(orderId, productId, quantity);
    }
    
    public void confirmStockReservations(Long orderId) {
        List<StockReservationEntity> reservations = stockReservationJpaRepository.findByOrderId(orderId);
        
        for (StockReservationEntity reservation : reservations) {
            reservation.confirm();
            stockReservationJpaRepository.save(reservation);
        }
    }
    
    public void cancelStockReservations(Long orderId) {
        List<StockReservationEntity> reservations = stockReservationJpaRepository.findByOrderId(orderId);
        
        for (StockReservationEntity reservation : reservations) {
            reservation.cancel();
            stockReservationJpaRepository.save(reservation);
        }
    }

    public OrderEntity saveOrder(OrderEntity order) {
        return orderJpaRepository.save(order);
    }

    public OrderEntity getUserOrder(OrderCommand.GetDetail command) {
        return orderJpaRepository.findByIdAndUserId(command.orderId(), command.userId())
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "주문을 찾을 수 없습니다."));
    }
    
    public Page<OrderEntity> getUserOrders(OrderCommand.GetList command) {
        Pageable pageable = PageRequest.of(
            command.page(),
            command.size(),
            Sort.by(Sort.Direction.DESC, "createdAt")
        );
        
        return orderJpaRepository.findByUserId(command.userId(), pageable);
    }
    
    public void updateOrder(OrderEntity order) {
        orderJpaRepository.save(order);
    }
    
    public record OrderCreationResult(
        OrderEntity order,
        Long productId,
        Integer quantity
    ) {}
}
