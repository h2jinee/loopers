package com.loopers.domain.order;

import com.loopers.domain.common.Money;
import com.loopers.domain.product.ProductDomainInfo;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    
    public Order createOrder(OrderCommand.CreateWithProduct command) {
        // 1. 주문 엔티티 생성
        Order order = new Order(command.userId(), command.receiverInfo());
        
        // 2. 상품 정보 추출
        ProductDomainInfo product = command.product();
        Money totalPrice = product.getTotalPrice();
        
        // 3. 주문 라인 추가
        order.addOrderLine(product.id(), product.nameKo(), command.quantity(), totalPrice);
        
        return order;
    }
}
