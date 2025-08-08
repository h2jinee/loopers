package com.loopers.domain.like;

import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.infrastructure.like.LikeJpaRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class LikeService {
    
    private final LikeJpaRepository likeJpaRepository;
    private final ProductJpaRepository productJpaRepository;

    @Transactional
    public boolean addLike(LikeCommand.Toggle command) {
        if (!productJpaRepository.existsById(command.productId())) {
            throw new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다.");
        }
        
        // 이미 존재하는지 확인
        if (likeJpaRepository.existsByUserIdAndProductId(command.userId(), command.productId())) {
            return false;
        }

        try {
            LikeEntity like = new LikeEntity(command.userId(), command.productId());
            likeJpaRepository.save(like);
            return true;
        } catch (Exception e) {
            // 다른 스레드가 먼저 insert한 경우
            log.warn("좋아요 추가 중 동시성 이슈 발생: userId={}, productId={}", 
                     command.userId(), command.productId());
            return false;
        }
    }
    
    @Transactional
    public boolean removeLike(LikeCommand.Toggle command) {
        // 삭제 전에 존재 여부 확인
        if (!likeJpaRepository.existsByUserIdAndProductId(command.userId(), command.productId())) {
            return false; // 삭제할 좋아요가 없음
        }
        
        likeJpaRepository.deleteByUserIdAndProductId(command.userId(), command.productId());
        return true; // 삭제 성공
    }
    
    @Transactional(readOnly = true)
    public boolean isLiked(LikeCommand.IsLiked command) {
        return likeJpaRepository.existsByUserIdAndProductId(command.userId(), command.productId());
    }
    
    /**
     * 좋아요한 상품 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<LikedProductDto> getLikedProducts(LikeCommand.GetList command, Pageable pageable) {
        log.debug("좋아요한 상품 목록 조회 시작 - userId: {}, page: {}, size: {}", 
                  command.userId(), pageable.getPageNumber(), pageable.getPageSize());
        
        Page<LikedProductDto> result = likeJpaRepository.findLikedProductsByUserId(
            command.userId(), 
            pageable
        );
        
        log.debug("좋아요한 상품 목록 조회 완료 - 조회된 상품 수: {}, 전체 페이지: {}", 
                  result.getNumberOfElements(), result.getTotalPages());
        
        return result;
    }
}
