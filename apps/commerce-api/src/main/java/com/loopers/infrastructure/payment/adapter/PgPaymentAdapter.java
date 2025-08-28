package com.loopers.infrastructure.payment.adapter;

import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.command.PgPaymentCommand;
import com.loopers.domain.payment.port.PgPaymentPort;
import com.loopers.domain.payment.result.PgPaymentResult;
import com.loopers.infrastructure.payment.PaymentGatewayClient;
import com.loopers.infrastructure.payment.dto.PaymentRequest;
import com.loopers.infrastructure.payment.dto.PaymentResponse;
import com.loopers.interfaces.api.ApiResponse;
import feign.FeignException;
import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.SocketTimeoutException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
public class PgPaymentAdapter implements PgPaymentPort {

    private final PaymentGatewayClient paymentGatewayClient;
    
    @Value("${payment.callback.url}")
    private String callbackUrl;
    
    @Override
    @CircuitBreaker(name = "pg-payment", fallbackMethod = "processPaymentFallback")
    @Retry(name = "pg-payment")
    public PgPaymentResult processPayment(PgPaymentCommand command) {
        String tempTransactionId = generateTempTransactionId(command.orderId());
        
        try {
            PaymentRequest request = toPaymentRequest(command);
            
            ApiResponse<PaymentResponse> response = paymentGatewayClient.send(
                command.userId(),
                request
            );
            
            if (response == null || response.data() == null) {
                log.error("PG 응답이 null입니다. orderId={}", command.orderId());
                return PgPaymentResult.pending(tempTransactionId, "PG 시스템 응답 없음");
            }
            
            PaymentResponse pgResponse = response.data();
            
            if (pgResponse.isSuccess()) {
                log.info("PG 결제 요청 성공: transactionKey={}", pgResponse.transactionKey());
                return PgPaymentResult.pending(pgResponse.transactionKey(), "결제 처리중");
            } else {
                log.warn("PG 결제 실패: reason={}", pgResponse.reason());
                return PgPaymentResult.failure(pgResponse.reason());
            }
        } catch (FeignException.BadRequest e) {
            log.error("잘못된 결제 요청: orderId={}, status={}", command.orderId(), e.status());
            return PgPaymentResult.failure("잘못된 결제 요청");
            
        } catch (FeignException.Unauthorized | FeignException.Forbidden e) {
            log.error("PG 인증 실패: orderId={}, status={}", command.orderId(), e.status());
            return PgPaymentResult.failure("PG 인증 실패");
            
        } catch (FeignException.GatewayTimeout e) {
            log.warn("PG 타임아웃 발생: orderId={}, status={}", command.orderId(), e.status());
            return PgPaymentResult.pending(tempTransactionId, "PG 응답 지연 - 동기화 필요");
            
        } catch (FeignException.ServiceUnavailable | FeignException.BadGateway e) {
            log.warn("PG 서버 일시 장애: orderId={}, status={}", command.orderId(), e.status());
            throw e;
            
        } catch (RetryableException e) {
            log.warn("재시도 가능한 오류: orderId={}", command.orderId(), e);
            throw e;
            
        } catch (FeignException e) {
            log.error("PG 통신 오류: orderId={}, status={}", command.orderId(), e.status(), e);
            return PgPaymentResult.failure("PG 시스템 오류");
            
        } catch (Exception e) {
            if (isTimeoutException(e)) {
                log.warn("결제 처리 타임아웃: orderId={}", command.orderId());
                return PgPaymentResult.pending(tempTransactionId, "결제 처리 타임아웃 - 동기화 필요");
            }
            log.error("PG 결제 처리 중 오류 발생: orderId={}", command.orderId(), e);
            return PgPaymentResult.failure("결제 처리 중 오류 발생");
        }
    }

    private PaymentRequest toPaymentRequest(PgPaymentCommand command) {
        return new PaymentRequest(
            command.orderId().toString(),
            mapCardTypeToExternalCode(command.cardInfo().cardType()),
            command.cardInfo().cardNo(),
            command.amount().amount().longValue(),
            callbackUrl
        );
    }

    private String mapCardTypeToExternalCode(CardType cardType) {
        return switch (cardType) {
            case SAMSUNG -> "SAMSUNG";
            case KB -> "KB";
            case HYUNDAI -> "HYUNDAI";
        };
    }
    
    private String generateTempTransactionId(Long orderId) {
        return String.format("TEMP_%s_%s", orderId, UUID.randomUUID().toString().substring(0, 8));
    }
    
    private boolean isTimeoutException(Exception e) {
        Throwable cause = e.getCause();
        return e instanceof TimeoutException ||
               e instanceof SocketTimeoutException ||
               cause instanceof TimeoutException ||
               cause instanceof SocketTimeoutException;
    }
    
    public PgPaymentResult processPaymentFallback(PgPaymentCommand command, Exception ex) {
        log.error("PG 결제 실패, Fallback 처리: orderId={}, error={}", 
            command.orderId(), ex.getMessage());
        
        return PgPaymentResult.failure("PG 시스템 일시 장애로 결제할 수 없습니다. 잠시 후 다시 시도해주세요.");
    }
}
