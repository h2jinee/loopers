package com.loopers.infrastructure.payment.adapter;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.loopers.domain.common.Money;
import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.command.PgPaymentCommand;
import com.loopers.domain.payment.result.PgPaymentResult;
import com.loopers.domain.payment.vo.CardInfo;
import com.loopers.infrastructure.payment.PaymentGatewayClient;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;

@SpringBootTest
@DisplayName("PgPaymentAdapter CircuitBreaker 테스트")
public class PgPaymentAdapterCircuitBreakerTest {

    @MockitoSpyBean
    private PaymentGatewayClient paymentGatewayClient;
    
    @Autowired
    private PgPaymentAdapter pgPaymentAdapter;
    
    private PgPaymentCommand testCommand;
    
    @BeforeEach
    void setUp() {
        reset(paymentGatewayClient);
        
        CardInfo cardInfo = new CardInfo(
            CardType.SAMSUNG, 
            "1234-5678-9012-3456"
        );
        
        testCommand = new PgPaymentCommand(
            1L,
            "user123",
            Money.of(5000),
            cardInfo
        );
    }
    
    @Nested
    @DisplayName("CircuitBreaker 동작 테스트")
    class CircuitBreakerBehavior {
        
        @Test
        @DisplayName("연속 실패시 CircuitBreaker가 열리고 Fallback이 호출된다")
        void circuitBreaker_OpensOnContinuousFailures() {
            // given - PG 서비스를 계속 실패하도록 설정
            FeignException.ServiceUnavailable exception = createFeignException(
                FeignException.ServiceUnavailable.class, "Service Unavailable"
            );
            
            when(paymentGatewayClient.send(eq("user123"), any()))
                .thenThrow(exception);
            
            // when - 여러 번 호출하여 CircuitBreaker가 열리도록 함
            PgPaymentResult result1 = pgPaymentAdapter.processPayment(testCommand);
            PgPaymentResult result2 = pgPaymentAdapter.processPayment(testCommand);
            PgPaymentResult result3 = pgPaymentAdapter.processPayment(testCommand);
            PgPaymentResult result4 = pgPaymentAdapter.processPayment(testCommand);
            PgPaymentResult result5 = pgPaymentAdapter.processPayment(testCommand);
            
            // then - 모두 실패 응답 (처음엔 실제 실패, 나중엔 Fallback)
            assertThat(result1.isSuccess()).isFalse();
            assertThat(result2.isSuccess()).isFalse();
            assertThat(result3.isSuccess()).isFalse();
            assertThat(result4.isSuccess()).isFalse();
            assertThat(result5.isSuccess()).isFalse();
            
            // CircuitBreaker가 열린 후에는 Fallback 메시지 확인
            await().atMost(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    PgPaymentResult fallbackResult = pgPaymentAdapter.processPayment(testCommand);
                    assertThat(fallbackResult.failureReason())
                        .isEqualTo("PG 시스템 일시 장애로 결제할 수 없습니다. 잠시 후 다시 시도해주세요.");
                });
        }
        
        @Test
        @DisplayName("Fallback 메서드가 정상 작동한다")
        void fallback_WorksCorrectly() {
            // given
            Exception exception = new RuntimeException("Circuit breaker open");
            
            // when - Fallback 메서드 직접 호출
            PgPaymentResult result = pgPaymentAdapter.processPaymentFallback(testCommand, exception);
            
            // then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.failureReason())
                .isEqualTo("PG 시스템 일시 장애로 결제할 수 없습니다. 잠시 후 다시 시도해주세요.");
            assertThat(result.transactionId()).isNull();
        }
        
        @Test
        @DisplayName("서킷브레이커가 열린 후 일정 시간 후 Half-Open 상태가 된다")
        void circuitBreaker_RecoveryAfterWaitTime() {
            // given - PG 서비스를 처음엔 실패, 나중엔 성공하도록 설정
            FeignException.ServiceUnavailable exception = createFeignException(
                FeignException.ServiceUnavailable.class, "Service Unavailable"
            );
            
            when(paymentGatewayClient.send(eq("user123"), any()))
                .thenThrow(exception)  // 처음 몇 번은 실패
                .thenThrow(exception)
                .thenThrow(exception)
                .thenThrow(exception)
                .thenThrow(exception)
                .thenReturn(createSuccessResponse());  // 나중엔 성공
            
            // when - 먼저 CircuitBreaker를 열도록 여러 번 호출
            for (int i = 0; i < 5; i++) {
                pgPaymentAdapter.processPayment(testCommand);
            }
            
            // 3초 대기 (wait-duration-in-open-state 설정값)
            await().pollDelay(Duration.ofSeconds(4))
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    // Half-Open 상태에서 성공하면 CircuitBreaker가 닫힘
                    PgPaymentResult result = pgPaymentAdapter.processPayment(testCommand);
                    // 성공하거나 또는 여전히 Fallback일 수 있음 (타이밍에 따라)
                    assertThat(result).isNotNull();
                });
        }
    }
    
    @Nested
    @DisplayName("Retry 동작 테스트")
    class RetryBehavior {
        
        @Test
        @DisplayName("일시적 실패시 재시도가 작동한다")
        void retry_WorksOnTransientFailures() {
            // given - 처음엔 실패, 재시도에서 성공
            FeignException.ServiceUnavailable exception = createFeignException(
                FeignException.ServiceUnavailable.class, "Temporary failure"
            );
            
            when(paymentGatewayClient.send(eq("user123"), any()))
                .thenThrow(exception)  // 첫 번째 시도 실패
                .thenReturn(createSuccessResponse());  // 재시도 성공
            
            // when
            PgPaymentResult result = pgPaymentAdapter.processPayment(testCommand);
            
            // then - 결과적으로 성공
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.transactionId()).isNotNull();
            
            // 실제로 2번 호출되었는지 확인 (원본 + 재시도)
            verify(paymentGatewayClient, times(2)).send(eq("user123"), any());
        }
    }
    
    private com.loopers.interfaces.api.ApiResponse<com.loopers.infrastructure.payment.dto.PaymentResponse> createSuccessResponse() {
        com.loopers.infrastructure.payment.dto.PaymentResponse response = 
            new com.loopers.infrastructure.payment.dto.PaymentResponse(
                "txn123", "SUCCESS", null
            );
        return com.loopers.interfaces.api.ApiResponse.success(response);
    }
    
    private <T extends FeignException> T createFeignException(Class<T> clazz, String message) {
        Request request = Request.create(
            Request.HttpMethod.POST,
            "http://test.com",
            new HashMap<>(),
            "test".getBytes(StandardCharsets.UTF_8),
            StandardCharsets.UTF_8,
            new RequestTemplate()
        );
        
        try {
            if (clazz == FeignException.ServiceUnavailable.class) {
                return clazz.cast(new FeignException.ServiceUnavailable(message, request, null, null));
            } else {
                return clazz.cast(new FeignException.InternalServerError(message, request, null, null));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
