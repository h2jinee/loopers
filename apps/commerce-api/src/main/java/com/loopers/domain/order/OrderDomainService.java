package com.loopers.domain.order;

import com.loopers.domain.common.Money;
import com.loopers.domain.order.vo.ReceiverInfo;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductDomainService;
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
public class OrderDomainService {
    
    private final ProductDomainService productDomainService;
    private final StockReservationRepository stockReservationRepository;
    private final OrderRepository orderRepository;
    
    public OrderCreationResult createOrder(OrderCommand.Create command) {
        // 상품 정보 조회
        ProductCommand.GetOne getProductCommand = new ProductCommand.GetOne(command.productId());
        ProductEntity product = productDomainService.getProduct(getProductCommand);
        
        // 재고 확인
        if (!product.isAvailable()) {
            throw new CoreException(ErrorType.CONFLICT, 
                "구매할 수 없는 상품입니다. 상품명: " + product.getNameKo());
        }
        
        if (!product.hasStock(command.quantity())) {
            throw new CoreException(ErrorType.CONFLICT, 
                "재고가 부족합니다. 상품명: " + product.getNameKo());
        }
        
        // 주문 생성
        OrderEntity order = new OrderEntity(command.userId(), command.receiverInfo());
        
        // 주문 항목 추가
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
        List<StockReservationEntity> reservations = stockReservationRepository.findByOrderId(orderId);
        
        for (StockReservationEntity reservation : reservations) {
            reservation.confirm();
            stockReservationRepository.save(reservation);
        }
    }
    
    public void cancelStockReservations(Long orderId) {
        List<StockReservationEntity> reservations = stockReservationRepository.findByOrderId(orderId);
        
        for (StockReservationEntity reservation : reservations) {
            // 재고 복원
            if (reservation.getStatus() == StockReservationEntity.ReservationStatus.RESERVED) {
                ProductCommand.IncreaseStock increaseCommand = new ProductCommand.IncreaseStock(
                    reservation.getProductId(), reservation.getQuantity()
                );
                productDomainService.increaseStock(increaseCommand);
            }
            
            reservation.cancel();
            stockReservationRepository.save(reservation);
        }
    }

    public OrderEntity saveOrder(OrderEntity order) {
        return orderRepository.save(order);
    }

    public OrderEntity getUserOrder(OrderCommand.GetDetail command) {
        return orderRepository.findByIdAndUserId(command.orderId(), command.userId())
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "주문을 찾을 수 없습니다."));
    }
    
    public Page<OrderEntity> getUserOrders(OrderCommand.GetList command) {
        Pageable pageable = PageRequest.of(
            command.page(),
            command.size(),
            Sort.by(Sort.Direction.DESC, "createdAt")
        );
        
        return orderRepository.findByUserId(command.userId(), pageable);
    }
    
    public void updateOrder(OrderEntity order) {
        orderRepository.save(order);
    }
    
    public record OrderCreationResult(
        OrderEntity order,
        Long productId,
        Integer quantity
    ) {}
}
