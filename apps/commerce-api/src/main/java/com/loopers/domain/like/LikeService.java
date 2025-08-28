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
            Like like = new Like(command.userId(), command.productId());
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
    
    public boolean isLiked(LikeCommand.IsLiked command) {
        return likeRepository.existsByUserIdAndProductId(command.userId(), command.productId());
    }
    
    /**
     * 사용자가 좋아요한 목록 조회
     */
    public Page<Like> getUserLikes(String userId, Pageable pageable) {
        log.debug("좋아요 목록 조회 - userId: {}, page: {}, size: {}", 
                  userId, pageable.getPageNumber(), pageable.getPageSize());
        return likeRepository.findByUserId(userId, pageable);
    }
}
