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

        boolean alreadyLiked = likeRepository.existsByUserIdAndProductId(
            command.userId(),
            command.productId()
        );

        if (alreadyLiked) {
            log.debug("이미 좋아요한 상품 (멱등): userId={}, productId={}", command.userId(), command.productId());
            return false;
        }

        Like like = new Like(command.userId(), command.productId());
        likeRepository.save(like);
        return true;
    }

    @Transactional
    public boolean removeLike(LikeCommand.Toggle command) {
        likeRepository.deleteByUserIdAndProductId(command.userId(), command.productId());
        return true;
    }

    public boolean isLiked(LikeCommand.IsLiked command) {
        return likeRepository.existsByUserIdAndProductId(command.userId(), command.productId());
    }

    public Page<Like> getUserLikes(String userId, Pageable pageable) {
        log.debug("좋아요 목록 조회 - userId: {}, page: {}, size: {}", userId, pageable.getPageNumber(), pageable.getPageSize());
        return likeRepository.findByUserId(userId, pageable);
    }
}
