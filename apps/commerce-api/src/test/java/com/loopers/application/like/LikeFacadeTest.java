package com.loopers.application.like;

import com.loopers.domain.common.Money;
import com.loopers.domain.event.Outbox;
import com.loopers.domain.event.OutboxRepository;
import com.loopers.domain.point.Point;
import com.loopers.domain.user.User;
import com.loopers.infrastructure.point.PointJpaRepository;
import com.loopers.infrastructure.user.UserJpaRepository;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * 좋아요-집계 분리 테스트
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LikeFacadeTest {

    @Autowired
    private LikeFacade likeFacade;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private PointJpaRepository pointRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String TEST_USER_ID = "likeuser123";
    private int initialOutboxCount;

    @BeforeEach
    void setUp() {
        // Redis 초기화 - 좋아요 관련 키만 삭제
        Set<String> likeKeys = redisTemplate.keys("likes:*");
        if (likeKeys != null && !likeKeys.isEmpty()) {
            redisTemplate.delete(likeKeys);
        }

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

        // 현재 Outbox 이벤트 개수 저장
        initialOutboxCount = outboxRepository.findByStatusOrderByCreatedAt(
            Outbox.OutboxStatus.PENDING,
            PageRequest.of(0, 100)
        ).size();
    }

    @Test
    void savesCountChangedEventToOutbox_whenLikeIsAddedSuccessfully() {
        // Given
        String testUserId = TEST_USER_ID + "_test1";
        LikeCriteria.AddLike criteria = new LikeCriteria.AddLike(testUserId, 1L);

        // When
        LikeResult.LikeToggleResult result = likeFacade.addLike(criteria);

        // Then
        assertThat(result.isLiked()).isTrue();

        // Outbox에 CountChanged 이벤트가 저장되었는지 확인
        List<Outbox> outboxEvents = outboxRepository.findByStatusOrderByCreatedAt(
            Outbox.OutboxStatus.PENDING,
            PageRequest.of(0, 100)
        );

        // 이 테스트에서 추가된 이벤트만 확인 (초기값 + 1)
        assertThat(outboxEvents).hasSize(initialOutboxCount + 1);

        // 마지막 이벤트가 우리가 추가한 것인지 확인
        Outbox lastEvent = outboxEvents.get(outboxEvents.size() - 1);
        assertThat(lastEvent.getAggregateId()).isEqualTo("1");
        assertThat(lastEvent.getEventType()).isEqualTo("CountChanged");
        assertThat(lastEvent.getTopic()).isEqualTo("catalog-events");
        assertThat(lastEvent.getStatus()).isEqualTo(Outbox.OutboxStatus.PENDING);
    }

    @Test
    void savesCountChangedEventToOutbox_whenLikeIsRemovedSuccessfully() {
        // Given
        String testUserId = TEST_USER_ID + "_test2";
        LikeCriteria.AddLike addCriteria = new LikeCriteria.AddLike(testUserId, 2L);
        likeFacade.addLike(addCriteria);

        // 추가 후 현재 이벤트 개수 확인
        int afterAddCount = outboxRepository.findByStatusOrderByCreatedAt(
            Outbox.OutboxStatus.PENDING,
            PageRequest.of(0, 100)
        ).size();

        // When
        LikeCriteria.RemoveLike removeCriteria = new LikeCriteria.RemoveLike(testUserId, 2L);
        LikeResult.LikeToggleResult result = likeFacade.removeLike(removeCriteria);

        // Then
        assertThat(result.isLiked()).isFalse();

        // Outbox에 CountChanged 이벤트가 추가로 저장되었는지 확인
        List<Outbox> outboxEvents = outboxRepository.findByStatusOrderByCreatedAt(
            Outbox.OutboxStatus.PENDING,
            PageRequest.of(0, 100)
        );

        // 제거로 인해 1개 더 추가됨
        assertThat(outboxEvents).hasSize(afterAddCount + 1);

        // productId=2인 이벤트가 2개 있는지 확인 (추가 1, 제거 1)
        long productId2Events = outboxEvents.stream()
            .filter(o -> "2".equals(o.getAggregateId()) && "CountChanged".equals(o.getEventType()))
            .count();
        assertThat(productId2Events).isEqualTo(2);
    }

    @Test
    void maintainsLikeSuccess_whenAggregationFails() {
        // Given
        String testUserId = TEST_USER_ID + "_test3";
        LikeCriteria.AddLike criteria = new LikeCriteria.AddLike(testUserId, 999L);

        // When
        LikeResult.LikeToggleResult result = likeFacade.addLike(criteria);

        // Then
        assertThat(result.isLiked()).isTrue();

        // Outbox에 CountChanged 이벤트가 저장됨
        List<Outbox> outboxEvents = outboxRepository.findByStatusOrderByCreatedAt(
            Outbox.OutboxStatus.PENDING,
            PageRequest.of(0, 100)
        );

        // 999 productId 이벤트 확인
        boolean hasEvent999 = outboxEvents.stream()
            .anyMatch(o -> "999".equals(o.getAggregateId()) && "CountChanged".equals(o.getEventType()));
        assertThat(hasEvent999).isTrue();
    }

    @Test
    void doesNotPublishEvent_whenDuplicateLikeIsAdded() {
        // Given
        String testUserId = TEST_USER_ID + "_test4";
        LikeCriteria.AddLike criteria = new LikeCriteria.AddLike(testUserId, 4L);

        // 첫 번째 좋아요 성공
        LikeResult.LikeToggleResult firstResult = likeFacade.addLike(criteria);
        assertThat(firstResult.isLiked()).isTrue();

        // 첫 번째 이벤트 개수 확인
        int afterFirstAdd = outboxRepository.findByStatusOrderByCreatedAt(
            Outbox.OutboxStatus.PENDING,
            PageRequest.of(0, 100)
        ).size();

        // When
        // 중복 좋아요 시도
        LikeResult.LikeToggleResult duplicateResult = likeFacade.addLike(criteria);

        // Then
        // 좋아요 상태는 유지되지만 새로운 이벤트는 발행되지 않음
        assertThat(duplicateResult.isLiked()).isTrue();

        // 이벤트 개수가 증가하지 않았는지 확인
        int afterDuplicate = outboxRepository.findByStatusOrderByCreatedAt(
            Outbox.OutboxStatus.PENDING,
            PageRequest.of(0, 100)
        ).size();

        assertThat(afterDuplicate).isEqualTo(afterFirstAdd);
    }
}
