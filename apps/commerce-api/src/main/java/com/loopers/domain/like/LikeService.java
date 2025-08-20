package com.loopers.domain.like;

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
    
    private final LikeRepository likeRepository;

    @Transactional
    public boolean addLike(LikeCommand.Toggle command) {
        try {
            LikeEntity like = new LikeEntity(command.userId(), command.productId());
            likeRepository.save(like);
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
        likeRepository.deleteByUserIdAndProductId(command.userId(), command.productId());
        return true; // 삭제 성공
    }
    
    @Transactional(readOnly = true)
    public boolean isLiked(LikeCommand.IsLiked command) {
        return likeRepository.existsByUserIdAndProductId(command.userId(), command.productId());
    }
    
    /**
     * 좋아요한 상품 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<LikedProductDto> getLikedProducts(LikeCommand.GetList command, Pageable pageable) {
        log.debug("좋아요한 상품 목록 조회 시작 - userId: {}, page: {}, size: {}", 
                  command.userId(), pageable.getPageNumber(), pageable.getPageSize());
        
        Page<LikedProductDto> result = likeRepository.findLikedProductsByUserId(
            command.userId(), 
            pageable
        );
        
        log.debug("좋아요한 상품 목록 조회 완료 - 조회된 상품 수: {}, 전체 페이지: {}", 
                  result.getNumberOfElements(), result.getTotalPages());
        
        return result;
    }
}
