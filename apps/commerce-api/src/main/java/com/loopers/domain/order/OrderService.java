package com.loopers.domain.order;

import com.loopers.domain.common.Money;
import com.loopers.domain.product.ProductInfo;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    
    public Order createOrder(OrderCommand.CreateWithProduct command) {
        // 1. 주문 엔티티 생성
        Order order = new Order(command.userId(), command.receiverInfo());
        
        // 2. 상품 정보 추출
        ProductInfo product = command.product();
        Money totalPrice = product.totalPrice();
        
        // 3. 주문 라인 추가
        order.addOrderLine(product.productId(), product.nameKo(), command.quantity(), totalPrice);
        
        // 4. 주문 저장
        return orderRepository.save(order);
    }
    
    /**
     * 주문 조회
     */
    public Order findById(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, 
                "주문을 찾을 수 없습니다. orderId: " + orderId));
    }
    
    /**
     * 주문 저장
     */
    @Transactional
    public Order save(Order order) {
        return orderRepository.save(order);
    }
}
