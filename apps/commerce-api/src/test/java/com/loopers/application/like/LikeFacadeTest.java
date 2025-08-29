package com.loopers.application.like;

import com.loopers.domain.common.Money;
import com.loopers.domain.point.Point;
import com.loopers.domain.user.User;
import com.loopers.infrastructure.point.PointJpaRepository;
import com.loopers.infrastructure.user.UserJpaRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * 좋아요-집계 분리 테스트
 */
@SpringBootTest
@RecordApplicationEvents
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LikeFacadeTest {

    @Autowired
    private LikeFacade likeFacade;
    
    @Autowired
    private ApplicationEvents events;
    
    @Autowired
    private UserJpaRepository userRepository;
    
    @Autowired
    private PointJpaRepository pointRepository;
    
    private static final String TEST_USER_ID = "likeuser123";
    
    @BeforeEach
    void setUp() {
        // 테스트용 사용자 준비
        if (!userRepository.existsByUserId(TEST_USER_ID)) {
            User testUser = new User(
                TEST_USER_ID,
                "좋아요테스트유저", 
                User.Gender.M,
                "19900101",
                "like@example.com"
            );
            userRepository.save(testUser);
            
            Point point = new Point(TEST_USER_ID, Money.of(BigDecimal.valueOf(5000)));
            pointRepository.save(point);
        }
    }

    @Test
    void publishesLikeAddedEvent_whenLikeIsAddedSuccessfully() {
        // Given
        String testUserId = TEST_USER_ID + "_test1";
        LikeCriteria.AddLike criteria = new LikeCriteria.AddLike(testUserId, 1L);
        
        // When
        LikeResult.LikeToggleResult result = likeFacade.addLike(criteria);
        
        // Then
        assertThat(result.isLiked()).isTrue();
        
        // LikeAdded 이벤트 발행됨
        assertThat(events.stream(LikeAdded.class))
            .hasSize(1)
            .first()
            .satisfies(event -> {
                assertThat(event.userId()).isEqualTo(testUserId);
                assertThat(event.productId()).isEqualTo(1L);
                assertThat(event.addedAt()).isNotNull();
            });
    }

    @Test
    void publishesLikeRemovedEvent_whenLikeIsRemovedSuccessfully() {
        // Given
        String testUserId = TEST_USER_ID + "_test2";
        LikeCriteria.AddLike addCriteria = new LikeCriteria.AddLike(testUserId, 2L);
        likeFacade.addLike(addCriteria);
        
        // When
        LikeCriteria.RemoveLike removeCriteria = new LikeCriteria.RemoveLike(testUserId, 2L);
        LikeResult.LikeToggleResult result = likeFacade.removeLike(removeCriteria);
        
        // Then
        assertThat(result.isLiked()).isFalse();
        
        // LikeRemoved 이벤트 발행됨
        assertThat(events.stream(LikeRemoved.class))
            .hasSize(1)
            .first()
            .satisfies(event -> {
                assertThat(event.userId()).isEqualTo(testUserId);
                assertThat(event.productId()).isEqualTo(2L);
                assertThat(event.removedAt()).isNotNull();
            });
    }

    @Test
    void maintainsLikeSuccess_whenAggregationFails() {
        // Given
        String testUserId = TEST_USER_ID + "_test3";
        LikeCriteria.AddLike criteria = new LikeCriteria.AddLike(testUserId, 999L); // 존재하지 않는 상품
        
        // When
        LikeResult.LikeToggleResult result = likeFacade.addLike(criteria);
        
        // Then
        assertThat(result.isLiked()).isTrue();
        
        // LikeAdded 이벤트는 발행됨
        assertThat(events.stream(LikeAdded.class))
            .hasSize(1)
            .first()
            .satisfies(event -> {
                assertThat(event.productId()).isEqualTo(999L);
            });
        
        // Awaitility로 비동기 집계 처리 검증
        Awaitility.await()
            .atMost(3, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                assertThat(events.stream(LikeAdded.class)).hasSize(1);
            });
    }

    @Test
    void doesNotPublishEvent_whenDuplicateLikeIsAdded() {
        // Given
        String testUserId = TEST_USER_ID + "_test4";
        LikeCriteria.AddLike criteria = new LikeCriteria.AddLike(testUserId, 4L);
        
        // 첫 번째 좋아요 성공
        LikeResult.LikeToggleResult firstResult = likeFacade.addLike(criteria);
        assertThat(firstResult.isLiked()).isTrue();
        
        // When
        // 중복 좋아요 시도 (LikeService에서 false 반환)
        LikeResult.LikeToggleResult duplicateResult = likeFacade.addLike(criteria);
        
        // Then
        // 좋아요 상태는 유지되지만 새로운 이벤트는 발행되지 않음
        assertThat(duplicateResult.isLiked()).isTrue();
        
        // LikeAdded 이벤트는 1번만 발행됨
        assertThat(events.stream(LikeAdded.class)).hasSize(1);
    }
}
