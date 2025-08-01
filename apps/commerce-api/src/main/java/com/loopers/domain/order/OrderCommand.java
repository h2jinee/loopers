package com.loopers.domain.order;

import com.loopers.domain.order.vo.ReceiverInfo;
import com.loopers.domain.product.ProductEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class OrderCommand {
    
    public record Create(
        String userId,
        Long productId,
        Integer quantity,
        ReceiverInfo receiverInfo
    ) {
        public Create {
            if (userId == null || userId.isBlank()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
            }
            if (productId == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "상품 ID는 필수입니다.");
            }
            if (quantity == null || quantity <= 0) {
                throw new CoreException(ErrorType.BAD_REQUEST, "수량은 1개 이상이어야 합니다.");
            }
            if (receiverInfo == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "수령인 정보는 필수입니다.");
            }
        }
    }
    
    public record CreateWithProduct(
        String userId,
        Long productId,
        Integer quantity,
        ReceiverInfo receiverInfo,
        ProductEntity product
    ) {
        public CreateWithProduct {
            if (userId == null || userId.isBlank()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
            }
            if (productId == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "상품 ID는 필수입니다.");
            }
            if (quantity == null || quantity <= 0) {
                throw new CoreException(ErrorType.BAD_REQUEST, "수량은 1개 이상이어야 합니다.");
            }
            if (receiverInfo == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "수령인 정보는 필수입니다.");
            }
            if (product == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "상품 정보는 필수입니다.");
            }
        }
        
        public static CreateWithProduct from(Create create, ProductEntity product) {
            return new CreateWithProduct(
                create.userId(),
                create.productId(),
                create.quantity(),
                create.receiverInfo(),
                product
            );
        }
    }
    
    public record GetList(
        String userId,
        Integer page,
        Integer size
    ) {
        public GetList {
            if (userId == null || userId.isBlank()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
            }
            if (page == null || page < 0) {
                page = 0;
            }
            if (size == null || size <= 0) {
                size = 20;
            }
            if (size > 100) {
                throw new CoreException(ErrorType.BAD_REQUEST, "페이지 크기는 최대 100개까지 가능합니다.");
            }
        }
        
        public static GetList of(String userId, Integer page, Integer size) {
            return new GetList(userId, page, size);
        }
    }
    
    public record GetDetail(
        String userId,
        Long orderId
    ) {
        public GetDetail {
            if (userId == null || userId.isBlank()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
            }
            if (orderId == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "주문 ID는 필수입니다.");
            }
        }
    }
}
