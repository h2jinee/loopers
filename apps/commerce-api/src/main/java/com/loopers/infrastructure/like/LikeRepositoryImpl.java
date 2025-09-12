package com.loopers.infrastructure.like;

import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeRepositoryImpl implements LikeRepository {

    private final LikeJpaRepository likeJpaRepository;
    private final LikeRedisRepository likeRedisRepository;

    @Override
    public Like save(Like like) {
        // 1. Redis에 추가
        boolean addedToRedis = likeRedisRepository.addLike(
            like.getUserId(),
            like.getProductId()
        );

        if (!addedToRedis) {
            // Redis에 이미 존재
            log.debug("좋아요 이미 존재 (Redis): userId={}, productId={}",
                like.getUserId(), like.getProductId());

            // DB에서 기존 Like 조회해서 반환
            Optional<Like> existing = likeJpaRepository.findByUserIdAndProductId(like.getUserId(), like.getProductId());
            return existing.orElse(like);  // 있으면 기존 것, 없으면 전달받은 것 반환
        }

        // 2. DB에 저장
        try {
            return likeJpaRepository.save(like);
        } catch (DataIntegrityViolationException e) {
            log.warn("DB 저장 실패 (Redis는 성공): userId={}, productId={}", like.getUserId(), like.getProductId());
            likeRedisRepository.removeLike(like.getUserId(), like.getProductId());
            throw e;
        }
    }

    @Override
    public void deleteByUserIdAndProductId(String userId, Long productId) {
        // 1. Redis에서 제거
        boolean removedFromRedis = likeRedisRepository.removeLike(userId, productId);

        if (removedFromRedis) {
            // 2. DB 제거
            likeJpaRepository.deleteByUserIdAndProductId(userId, productId);
        } else {
            log.debug("제거할 좋아요 없음 (Redis): userId={}, productId={}", userId, productId);
        }
    }

    @Override
    public boolean existsByUserIdAndProductId(String userId, Long productId) {
        return likeRedisRepository.exists(userId, productId);
    }

    @Override
    public Page<Like> findByUserId(String userId, Pageable pageable) {
        return likeJpaRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
}
