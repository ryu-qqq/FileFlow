package com.ryuqq.application.{domain_lower};

import com.ryuqq.application.common.orchestration.BaseOrchestrator;
import com.ryuqq.application.common.orchestration.Outcome;
import com.ryuqq.application.common.orchestration.OpId;
import com.ryuqq.application.{domain_lower}.command.{Domain}Command;
import com.ryuqq.domain.common.Domain;
import com.ryuqq.domain.common.EventType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * {Domain} Orchestrator
 *
 * <p>외부 API 호출을 안전하게 관리하는 Orchestration Pattern을 구현합니다.
 * 3-Phase Lifecycle (Accept → Execute → Finalize)을 통해
 * 멱등성, 크래시 복구, 타입 안전성을 보장합니다.</p>
 *
 * <h3>핵심 원칙</h3>
 * <ul>
 *   <li>IdemKey를 통한 중복 실행 방지</li>
 *   <li>WAL 기록을 통한 크래시 복구</li>
 *   <li>Exponential Backoff 재시도 전략</li>
 *   <li>Outcome Modeling (Ok/Retry/Fail)</li>
 *   <li>executeInternal()은 @Async (트랜잭션 밖)</li>
 * </ul>
 *
 * @author {author_name}
 * @since {version}
 */
@Service
public class {Domain}Orchestrator extends BaseOrchestrator<{Domain}Command> {

    // TODO: 외부 API Client 주입 (예: PaymentGateway, S3Client, FCMClient)
    // private final ExternalApiClient externalApiClient;

    // TODO: 필요시 다른 의존성 주입 (Repository, Port 등)

    public {Domain}Orchestrator(
        // TODO: 생성자 파라미터 추가
    ) {
        // TODO: 의존성 초기화
    }

    /**
     * Domain 정의
     *
     * @return {Domain} Domain
     */
    @Override
    protected Domain domain() {
        return Domain.{DOMAIN_UPPER};
    }

    /**
     * EventType 정의
     *
     * @return {Domain} EventType
     */
    @Override
    protected EventType eventType() {
        return EventType.{EVENT_TYPE_UPPER};
    }

    /**
     * 외부 API 호출 실행 (비동기, 트랜잭션 밖)
     *
     * <p><strong>중요:</strong> 이 메서드는 @Async로 선언되어야 하며,
     * @Transactional을 절대 사용하지 마세요. 외부 API 호출은
     * 트랜잭션 밖에서 실행되어야 합니다.</p>
     *
     * <h3>구현 가이드</h3>
     * <ol>
     *   <li>외부 API 호출 수행</li>
     *   <li>일시적 오류 감지 → Outcome.retry() 반환</li>
     *   <li>영구적 오류 감지 → Outcome.fail() 반환</li>
     *   <li>성공 시 → Outcome.ok() 반환</li>
     * </ol>
     *
     * <h3>Error 분류</h3>
     * <ul>
     *   <li><strong>일시적 오류 (Retry)</strong>: 네트워크 타임아웃, 서버 일시 장애, Rate Limit</li>
     *   <li><strong>영구적 오류 (Fail)</strong>: 잘못된 파라미터, 권한 없음, 비즈니스 규칙 위반</li>
     * </ul>
     *
     * @param opId Operation ID
     * @param cmd Command
     * @return Outcome (Ok/Retry/Fail)
     */
    @Override
    @Async
    protected Outcome executeInternal(OpId opId, {Domain}Command cmd) {
        try {
            // TODO: 외부 API 호출 구현
            // 예시:
            // String txId = externalApiClient.process(cmd.businessKey(), ...);

            // 성공 시 Ok 반환
            return Outcome.ok(opId, "Operation completed successfully");

        } catch (TransientException e) {
            // 일시적 오류 → Retry
            int nextAttempt = 1; // TODO: 실제 attempt count 가져오기
            long backoffMs = calculateBackoff(nextAttempt);
            return Outcome.retry(
                e.getMessage(),
                nextAttempt,
                backoffMs
            );

        } catch (PermanentException e) {
            // 영구적 오류 → Fail
            return Outcome.fail(
                e.getErrorCode(),
                e.getMessage(),
                "N/A" // 또는 실제 transactionId
            );

        } catch (Exception e) {
            // 예상치 못한 오류 → Fail (재시도 불가)
            return Outcome.fail(
                "UNKNOWN_ERROR",
                "Unexpected error: " + e.getMessage(),
                "N/A"
            );
        }
    }

    /**
     * Exponential Backoff 계산
     *
     * <p>재시도 대기 시간을 지수적으로 증가시킵니다:
     * 5초, 10초, 20초, 40초, ...</p>
     *
     * @param attemptCount 시도 횟수
     * @return 대기 시간 (밀리초)
     */
    private long calculateBackoff(int attemptCount) {
        return 5000L * (long) Math.pow(2, attemptCount - 1);
    }

    // TODO: 비즈니스별 예외 클래스 정의
    /**
     * 일시적 오류 (재시도 가능)
     */
    private static class TransientException extends Exception {
        public TransientException(String message) {
            super(message);
        }
    }

    /**
     * 영구적 오류 (재시도 불가)
     */
    private static class PermanentException extends Exception {
        private final String errorCode;

        public PermanentException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }
}
