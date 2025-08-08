package com.loopers.domain.order;

import com.loopers.domain.common.Money;
import com.loopers.domain.product.ProductEntity;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final StockReservationJpaRepository stockReservationJpaRepository;
    private final OrderJpaRepository orderJpaRepository;

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
    
    @Transactional
    public void confirmStockReservations(Long orderId) {
        List<StockReservationEntity> reservations = stockReservationJpaRepository.findByOrderId(orderId);
        
        reservations.forEach(StockReservationEntity::confirm);
        stockReservationJpaRepository.saveAll(reservations);
    }
    
    @Transactional
    public void cancelStockReservations(Long orderId) {
        List<StockReservationEntity> reservations = stockReservationJpaRepository.findByOrderId(orderId);
        
        reservations.forEach(StockReservationEntity::cancel);
        stockReservationJpaRepository.saveAll(reservations);
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
