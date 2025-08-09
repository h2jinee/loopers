package com.loopers.application.like;

import com.loopers.domain.like.LikeCommand;

public class LikeCriteria {
    
    public record AddLike(
        String userId,
        Long productId
    ) {
        public LikeCommand.Toggle toCommand() {
            return new LikeCommand.Toggle(userId, productId);
        }
        
        public LikeCommand.IsLiked toIsLikedCommand() {
            return new LikeCommand.IsLiked(userId, productId);
        }
    }
    
    public record RemoveLike(
        String userId,
        Long productId
    ) {
        public LikeCommand.Toggle toCommand() {
            return new LikeCommand.Toggle(userId, productId);
        }
        
        public LikeCommand.IsLiked toIsLikedCommand() {
            return new LikeCommand.IsLiked(userId, productId);
        }
    }
    
    public record GetLikedProducts(
        String userId,
        Integer page,
        Integer size
    ) {
        public LikeCommand.GetList toCommand() {
            return LikeCommand.GetList.of(userId, page, size);
        }
    }
}