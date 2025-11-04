# Outcome Modeling - 실행 결과 타입 안전성

**목적**: Operation 실행 결과를 타입 안전하게 모델링하여 컴파일 타임에 모든 케이스 처리 강제

**관련 문서**:
- [Command Pattern](./01_command-pattern.md)
- [State Machine Pattern](./05_state-machine-pattern.md)
- [Sealed Classes](../06-java21-patterns/sealed-classes/03_result-types.md)

**필수 버전**: Java 21+

---

## 📌 핵심 원칙

### Outcome이란?

**정의**: Operation 실행 결과를 나타내는 Sealed Interface

```java
public sealed interface Outcome permits Ok, Retry, Fail {
    // 세 가지 경우만 존재: Ok, Retry, Fail
}
```

**왜 Sealed Interface인가?**
- 컴파일 타임에 모든 케이스 처리 강제
- Pattern Matching으로 타입 안전한 분기
- 새로운 케이스 추가 시 컴파일 오류로 누락 방지

### 전통적인 방식의 문제점

```java
// ❌ Before - enum으로 결과 표현 (문제점)
public enum PaymentResult {
    SUCCESS, RETRY, FAILED
}

public class PaymentResponse {
    private PaymentResult result;
    private String message;  // ❌ 어떤 상황에서 필요한지 불명확
    private Integer retryAfter;  // ❌ RETRY일 때만 필요하지만 항상 존재
    private String errorCode;  // ❌ FAILED일 때만 필요
}

// ❌ 사용 코드 - 런타임 오류 가능
public void handle(PaymentResponse response) {
    if (response.getResult() == PaymentResult.RETRY) {
        int retryAfter = response.getRetryAfter();  // ❌ null일 수 있음
        // NullPointerException 위험
    }
}

// ✅ After - Sealed Interface + Records
public sealed interface Outcome permits Ok, Retry, Fail {}
public record Ok(OpId opId, String message) implements Outcome {}
public record Retry(String reason, int attemptCount, long nextRetryAfterMillis) implements Outcome {}
public record Fail(String errorCode, String message, String cause) implements Outcome {}

// ✅ 사용 코드 - 타입 안전
public void handle(Outcome outcome) {
    String result = switch (outcome) {
        case Ok ok -> "Success: " + ok.message();
        case Retry retry -> "Retry after " + retry.nextRetryAfterMillis() + "ms";
        case Fail fail -> "Failed: " + fail.errorCode();
        // 컴파일러가 모든 케이스 처리 강제 (default 불필요)
    };
}
```

---

## 🏗️ Outcome 타입 정의

### 1. Outcome Interface

```java
/**
 * Operation 실행 결과
 *
 * <p>Sealed interface로 정의되어 모든 케이스를 컴파일 타임에 검증합니다.</p>
 *
 * <p><strong>허용된 구현체:</strong></p>
 * <ul>
 *   <li>{@link Ok}: 성공적으로 완료됨</li>
 *   <li>{@link Retry}: 일시적 실패, 재시도 가능</li>
 *   <li>{@link Fail}: 영구적 실패, 재시도 불가</li>
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public sealed interface Outcome permits Ok, Retry, Fail {

    /**
     * 결과가 성공인지 확인
     */
    default boolean isOk() {
        return this instanceof Ok;
    }

    /**
     * 결과가 재시도 가능한지 확인
     */
    default boolean isRetry() {
        return this instanceof Retry;
    }

    /**
     * 결과가 영구 실패인지 확인
     */
    default boolean isFail() {
        return this instanceof Fail;
    }
}
```

### 2. Ok (성공)

```java
/**
 * 성공 결과
 *
 * <p>Operation이 성공적으로 완료되었음을 나타냅니다.</p>
 *
 * @param opId Operation ID
 * @param message 성공 메시지 (선택, null 가능)
 *
 * @author development-team
 * @since 1.0.0
 */
public record Ok(
    OpId opId,
    String message
) implements Outcome {

    public Ok {
        if (opId == null) {
            throw new IllegalArgumentException("opId cannot be null");
        }
        // message는 null 허용
    }

    public static Ok of(OpId opId, String message) {
        return new Ok(opId, message);
    }

    public static Ok of(OpId opId) {
        return new Ok(opId, null);
    }
}
```

### 3. Retry (재시도 가능)

```java
/**
 * 재시도 가능한 일시적 실패
 *
 * <p><strong>예시:</strong></p>
 * <ul>
 *   <li>네트워크 타임아웃</li>
 *   <li>외부 서비스 일시 장애 (503 Service Unavailable)</li>
 *   <li>Rate Limit 초과 (429 Too Many Requests)</li>
 *   <li>DB Connection Pool 고갈</li>
 * </ul>
 *
 * @param reason 재시도 사유
 * @param attemptCount 현재까지 시도 횟수 (1 이상)
 * @param nextRetryAfterMillis 다음 재시도까지 대기 시간 (밀리초, 0 이상)
 *
 * @author development-team
 * @since 1.0.0
 */
public record Retry(
    String reason,
    int attemptCount,
    long nextRetryAfterMillis
) implements Outcome {

    public Retry {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason cannot be null or blank");
        }
        if (attemptCount < 1) {
            throw new IllegalArgumentException("attemptCount must be positive (current: " + attemptCount + ")");
        }
        if (nextRetryAfterMillis < 0) {
            throw new IllegalArgumentException("nextRetryAfterMillis must be non-negative (current: " + nextRetryAfterMillis + ")");
        }
    }

    public static Retry of(String reason, int attemptCount, long nextRetryAfterMillis) {
        return new Retry(reason, attemptCount, nextRetryAfterMillis);
    }

    /**
     * Exponential Backoff 계산
     */
    public static Retry withExponentialBackoff(String reason, int attemptCount) {
        long backoffMillis = (long) Math.pow(2, attemptCount - 1) * 1000;  // 1s, 2s, 4s, 8s...
        long maxBackoff = 60_000;  // 최대 60초
        long retryAfter = Math.min(backoffMillis, maxBackoff);
        return new Retry(reason, attemptCount, retryAfter);
    }
}
```

### 4. Fail (영구 실패)

```java
/**
 * 영구적 실패 (재시도 불가)
 *
 * <p><strong>예시:</strong></p>
 * <ul>
 *   <li>유효성 검증 실패 (잘못된 파라미터)</li>
 *   <li>권한 없음 (403 Forbidden)</li>
 *   <li>리소스 없음 (404 Not Found)</li>
 *   <li>비즈니스 규칙 위반 (잔액 부족, 재고 없음)</li>
 * </ul>
 *
 * @param errorCode 오류 코드 (예: PAY-001, FILE-404)
 * @param message 오류 메시지
 * @param cause 원인 (선택, null 가능)
 *
 * @author development-team
 * @since 1.0.0
 */
public record Fail(
    String errorCode,
    String message,
    String cause
) implements Outcome {

    public Fail {
        if (errorCode == null || errorCode.isBlank()) {
            throw new IllegalArgumentException("errorCode cannot be null or blank");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message cannot be null or blank");
        }
        // cause는 null 허용
    }

    public static Fail of(String errorCode, String message, String cause) {
        return new Fail(errorCode, message, cause);
    }

    public static Fail of(String errorCode, String message) {
        return new Fail(errorCode, message, null);
    }
}
```

---

## ✅ Pattern Matching 활용

### 패턴 1: Switch Expression

```java
/**
 * Switch Expression을 사용한 Outcome 처리
 *
 * @author development-team
 * @since 1.0.0
 */
public class OutcomeHandler {

    public String handle(Outcome outcome) {
        // ✅ Pattern Matching - 모든 케이스 처리 강제
        return switch (outcome) {
            case Ok ok -> handleSuccess(ok);
            case Retry retry -> handleRetry(retry);
            case Fail fail -> handleFailure(fail);
            // default 불필요 (컴파일러가 모든 케이스 보장)
        };
    }

    private String handleSuccess(Ok ok) {
        return "Success: " + ok.message();
    }

    private String handleRetry(Retry retry) {
        return "Retry after " + retry.nextRetryAfterMillis() + "ms (attempt " + retry.attemptCount() + ")";
    }

    private String handleFailure(Fail fail) {
        return "Failed [" + fail.errorCode() + "]: " + fail.message();
    }
}
```

### 패턴 2: Type Pattern

```java
/**
 * Type Pattern을 사용한 분기 처리
 *
 * @author development-team
 * @since 1.0.0
 */
public class QueueWorker {

    public void process(Envelope envelope) {
        Outcome outcome = executor.execute(envelope, Map.of());

        // ✅ Type Pattern으로 타입별 처리
        if (outcome instanceof Ok ok) {
            handleSuccess(envelope.opId(), ok);
        } else if (outcome instanceof Retry retry) {
            scheduleRetry(envelope, retry);
        } else if (outcome instanceof Fail fail) {
            handleFailure(envelope.opId(), fail);
        }
    }

    private void handleSuccess(OpId opId, Ok ok) {
        store.finalize(opId, ok, Instant.now());
        log.info("Operation completed: opId={}, message={}", opId, ok.message());
    }

    private void scheduleRetry(Envelope envelope, Retry retry) {
        // 재시도 큐에 발행 (지연 시간 포함)
        retryQueue.publish(envelope, retry.nextRetryAfterMillis());
        log.warn("Operation scheduled for retry: opId={}, attemptCount={}", 
            envelope.opId(), retry.attemptCount());
    }

    private void handleFailure(OpId opId, Fail fail) {
        store.finalize(opId, fail, Instant.now());
        log.error("Operation failed permanently: opId={}, errorCode={}, message={}", 
            opId, fail.errorCode(), fail.message());
    }
}
```

---

## 🎯 실전 예제

### 예제 1: 결제 Executor

```java
/**
 * 결제 Executor - Outcome 반환
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class PaymentExecutor implements Executor {

    private final PaymentGateway paymentGateway;

    @Override
    public Outcome execute(Envelope envelope, Map<String, String> headers) {
        try {
            // 1. Payload 파싱
            PaymentData data = parsePayload(envelope.command().payload());

            // 2. 외부 결제 API 호출
            PaymentApiResponse response = paymentGateway.charge(
                data.amount(),
                data.cardNumber()
            );

            // 3. ✅ 성공 결과 반환
            return Ok.of(
                envelope.opId(),
                "Payment successful: txId=" + response.transactionId()
            );

        } catch (NetworkTimeoutException e) {
            // ⚠️ 일시적 실패 - 재시도 가능
            int attemptCount = getAttemptCount(headers);
            return Retry.withExponentialBackoff("Network timeout", attemptCount + 1);

        } catch (InsufficientFundsException e) {
            // ❌ 영구 실패 - 재시도 불가
            return Fail.of("PAY-001", "Insufficient funds", e.getMessage());

        } catch (InvalidCardException e) {
            // ❌ 영구 실패 - 재시도 불가
            return Fail.of("PAY-002", "Invalid card number", e.getMessage());

        } catch (PaymentGatewayException e) {
            // ⚠️ 일시적 실패 가능성
            if (e.isRetryable()) {
                int attemptCount = getAttemptCount(headers);
                return Retry.withExponentialBackoff("Payment gateway error", attemptCount + 1);
            } else {
                return Fail.of("PAY-999", "Payment gateway error", e.getMessage());
            }
        }
    }
}
```

### 예제 2: 파일 업로드 Executor

```java
/**
 * 파일 업로드 Executor - Outcome 반환
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class FileUploadExecutor implements Executor {

    private final S3Client s3Client;

    @Override
    public Outcome execute(Envelope envelope, Map<String, String> headers) {
        try {
            // 1. Payload 파싱
            FileData data = parsePayload(envelope.command().payload());

            // 2. S3 업로드
            PutObjectResponse response = s3Client.putObject(
                data.bucket(),
                data.key(),
                data.content()
            );

            // 3. ✅ 성공
            return Ok.of(
                envelope.opId(),
                "File uploaded: s3://" + data.bucket() + "/" + data.key()
            );

        } catch (S3Exception e) {
            // HTTP 상태 코드 기반 분기
            return switch (e.statusCode()) {
                case 429 -> {  // Too Many Requests
                    int attemptCount = getAttemptCount(headers);
                    yield Retry.withExponentialBackoff("Rate limit exceeded", attemptCount + 1);
                }
                case 500, 503 -> {  // Server Error, Service Unavailable
                    int attemptCount = getAttemptCount(headers);
                    yield Retry.withExponentialBackoff("S3 service error", attemptCount + 1);
                }
                case 403 -> Fail.of("FILE-403", "Access denied", e.getMessage());
                case 404 -> Fail.of("FILE-404", "Bucket not found", e.getMessage());
                default -> Fail.of("FILE-999", "S3 error: " + e.statusCode(), e.getMessage());
            };

        } catch (IOException e) {
            // ⚠️ 네트워크 오류 - 재시도 가능
            int attemptCount = getAttemptCount(headers);
            return Retry.withExponentialBackoff("Network I/O error", attemptCount + 1);
        }
    }
}
```

---

## 🔄 Outcome → State 전이

### Outcome에 따른 상태 전이 규칙

```java
/**
 * Outcome → OperationState 전이
 *
 * @author development-team
 * @since 1.0.0
 */
public class OutcomeStateMapper {

    /**
     * Outcome에 따른 최종 상태 결정
     */
    public static OperationState mapToFinalState(Outcome outcome) {
        return switch (outcome) {
            case Ok ok -> OperationState.COMPLETED;
            case Retry retry -> OperationState.IN_PROGRESS;  // 재시도 대기
            case Fail fail -> OperationState.FAILED;
        };
    }

    /**
     * Retry 최대 횟수 초과 시 Fail로 전환
     */
    public static Outcome failIfMaxRetryExceeded(Outcome outcome, int maxRetries) {
        if (outcome instanceof Retry retry && retry.attemptCount() >= maxRetries) {
            return Fail.of(
                "MAX-RETRY",
                "Maximum retry attempts exceeded: " + maxRetries,
                retry.reason()
            );
        }
        return outcome;
    }
}
```

---

## 📋 Best Practices

### 1. Outcome 생성 시 충분한 정보 제공

```java
// ❌ Bad - 정보 부족
return Fail.of("ERROR", "Failed");

// ✅ Good - 상세한 정보
return Fail.of(
    "PAY-001",
    "Payment failed: insufficient funds",
    "Available: $50, Required: $100"
);
```

### 2. Retry는 Exponential Backoff 사용

```java
// ❌ Bad - 고정된 재시도 간격
return Retry.of("Network timeout", attemptCount, 1000);  // 항상 1초 대기

// ✅ Good - Exponential Backoff
return Retry.withExponentialBackoff("Network timeout", attemptCount);
// 1s, 2s, 4s, 8s, 16s, 32s, 60s (최대)
```

### 3. 재시도 불가능한 오류는 Fail 반환

```java
// ✅ 재시도 가능 vs 불가능 구분
try {
    PaymentApiResponse response = paymentGateway.charge(amount, cardNumber);
    return Ok.of(opId, "Success");

} catch (NetworkTimeoutException e) {
    // ✅ 일시적 오류 - 재시도 가능
    return Retry.withExponentialBackoff("Network timeout", attemptCount + 1);

} catch (InvalidCardException e) {
    // ✅ 영구 오류 - 재시도 불가
    return Fail.of("PAY-002", "Invalid card", e.getMessage());
}
```

### 4. 최대 재시도 횟수 체크

```java
/**
 * 최대 재시도 횟수 제한
 */
public Outcome executeWithMaxRetry(Envelope envelope, int maxRetries) {
    int attemptCount = getAttemptCount(envelope);

    // ✅ 최대 재시도 횟수 초과 시 Fail
    if (attemptCount >= maxRetries) {
        return Fail.of(
            "MAX-RETRY",
            "Maximum retry attempts exceeded: " + maxRetries,
            "Last attempt count: " + attemptCount
        );
    }

    // 정상 실행
    return executor.execute(envelope, Map.of());
}
```

---

## 🧪 테스트 전략

### 1. Outcome 생성 테스트

```java
class OutcomeTest {

    @Test
    void Ok_생성() {
        // given
        OpId opId = OpId.of("op-123");

        // when
        Ok ok = Ok.of(opId, "Success");

        // then
        assertThat(ok.opId()).isEqualTo(opId);
        assertThat(ok.message()).isEqualTo("Success");
    }

    @Test
    void Retry_Exponential_Backoff() {
        // when
        Retry retry1 = Retry.withExponentialBackoff("Timeout", 1);
        Retry retry2 = Retry.withExponentialBackoff("Timeout", 2);
        Retry retry3 = Retry.withExponentialBackoff("Timeout", 3);

        // then
        assertThat(retry1.nextRetryAfterMillis()).isEqualTo(1000);   // 1s
        assertThat(retry2.nextRetryAfterMillis()).isEqualTo(2000);   // 2s
        assertThat(retry3.nextRetryAfterMillis()).isEqualTo(4000);   // 4s
    }

    @Test
    void Fail_생성() {
        // when
        Fail fail = Fail.of("PAY-001", "Payment failed", "Insufficient funds");

        // then
        assertThat(fail.errorCode()).isEqualTo("PAY-001");
        assertThat(fail.message()).isEqualTo("Payment failed");
        assertThat(fail.cause()).isEqualTo("Insufficient funds");
    }
}
```

### 2. Pattern Matching 테스트

```java
class PatternMatchingTest {

    @Test
    void Switch_Expression_모든_케이스_처리() {
        // given
        List<Outcome> outcomes = List.of(
            Ok.of(OpId.of("op-1"), "Success"),
            Retry.withExponentialBackoff("Timeout", 1),
            Fail.of("ERR-001", "Failed")
        );

        // when & then
        for (Outcome outcome : outcomes) {
            String result = switch (outcome) {
                case Ok ok -> "OK";
                case Retry retry -> "RETRY";
                case Fail fail -> "FAIL";
            };

            assertThat(result).isIn("OK", "RETRY", "FAIL");
        }
    }
}
```

---

**작성자**: Development Team
**최종 수정일**: 2025-10-30
**버전**: 1.0.0
