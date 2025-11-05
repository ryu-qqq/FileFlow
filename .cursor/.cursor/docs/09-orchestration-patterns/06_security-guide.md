# 06. Security Guide: Orchestration Pattern 보안 강화

## 1. 개요

Orchestration Pattern은 외부 API 호출을 안전하게 처리하지만, **보안 공격**에 대한 방어 메커니즘이 필요합니다.

이 문서는 다음 보안 위협을 다룹니다:
1. **IdemKey 남용 공격**: 동일 IdemKey로 대량 요청
2. **Payload 크기 공격**: 대용량 Payload로 메모리 고갈 (DoS)
3. **타이밍 공격**: 응답 시간 차이로 정보 유출
4. **Authorization 누락**: Operation 소유권 검증 부재

---

## 2. IdemKey 남용 방지 (Rate Limiting)

### 2.1 문제 상황

```java
// 공격자가 동일 IdemKey로 초당 1000회 요청
for (int i = 0; i < 1000; i++) {
    POST /api/payments
    {
        "idempotencyKey": "IDEM-12345",  // 동일한 키
        "orderId": "ORDER-001",
        "amount": 10000
    }
}
```

**문제점**:
- DB에 `SELECT * FROM operations WHERE idem_key = 'IDEM-12345'` 쿼리가 초당 1000회 실행
- CPU, DB Connection Pool 고갈

### 2.2 해결책: Rate Limiter

#### A. Dependency 추가

```gradle
dependencies {
    implementation 'com.github.ben-manes.caffeine:caffeine:3.1.8'
    // 또는 Redis 기반: spring-boot-starter-data-redis
}
```

#### B. Rate Limiter 구현

```java
package com.example.common.orchestration.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * IdemKey 남용 방지 Rate Limiter
 *
 * <p>동일 IdemKey로 짧은 시간에 여러 요청 시도를 차단합니다.</p>
 *
 * <p><b>전략</b>:
 * - Sliding Window: 1분 동안 최대 5회 허용
 * - Key: {idemKey}:{clientIp}
 * - TTL: 1분
 * </p>
 *
 * @author coding-convention-09
 * @since 1.0
 */
@Component
public class IdemKeyRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW_SIZE = Duration.ofMinutes(1);

    private final Cache<String, AtomicInteger> attemptCache;

    public IdemKeyRateLimiter() {
        this.attemptCache = Caffeine.newBuilder()
            .expireAfterWrite(WINDOW_SIZE)
            .maximumSize(10_000)  // 최대 10,000개 키 저장
            .build();
    }

    /**
     * IdemKey 사용 가능 여부 확인
     *
     * @param idemKey 멱등성 키
     * @param clientIp 클라이언트 IP
     * @throws TooManyRequestsException 제한 초과 시
     */
    public void validate(String idemKey, String clientIp) {
        String rateLimitKey = idemKey + ":" + clientIp;

        AtomicInteger attempts = attemptCache.get(rateLimitKey, k -> new AtomicInteger(0));

        if (attempts.incrementAndGet() > MAX_ATTEMPTS) {
            throw new TooManyRequestsException(
                String.format(
                    "Too many attempts with idempotency key '%s' from IP '%s'. Max %d attempts per %d seconds.",
                    idemKey, clientIp, MAX_ATTEMPTS, WINDOW_SIZE.toSeconds()
                )
            );
        }
    }
}
```

#### C. Orchestrator에 통합

```java
@Service
public class PaymentOrchestrator extends BaseOrchestrator<PaymentCommand> {

    private final IdemKeyRateLimiter rateLimiter;

    @Override
    @Transactional
    public Outcome accept(PaymentCommand command) {
        // 1️⃣ Rate Limiting 검증 (DB 조회 전)
        String clientIp = RequestContextHolder.currentRequestAttributes()
            .getAttribute("clientIp", RequestAttributes.SCOPE_REQUEST);

        rateLimiter.validate(command.idempotencyKey(), clientIp);

        // 2️⃣ 기존 Accept 로직
        return super.accept(command);
    }
}
```

#### D. Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ApiResponse> handleTooManyRequests(TooManyRequestsException e) {
        return ResponseEntity.status(429)  // 429 Too Many Requests
            .header("Retry-After", "60")  // 60초 후 재시도
            .body(new ApiResponse("RATE_LIMIT_EXCEEDED", e.getMessage()));
    }
}
```

---

## 3. Payload 크기 제한 (DoS 방지)

### 3.1 문제 상황

```java
// 공격자가 1GB Payload 전송
POST /api/payments
{
    "idempotencyKey": "IDEM-12345",
    "orderId": "ORDER-001",
    "amount": 10000,
    "metadata": "A".repeat(1_000_000_000)  // 1GB 문자열
}
```

**문제점**:
- DB에 1GB 데이터 저장 시도 → 메모리 고갈
- Serialization/Deserialization 비용 과다

### 3.2 해결책: Payload Validator

#### A. Command에 검증 로직 추가

```java
public record PaymentCommand(
    String orderId,
    String idempotencyKey,
    BigDecimal amount,
    String metadata  // Optional
) {
    private static final int MAX_METADATA_SIZE = 10_000;  // 10KB

    public PaymentCommand {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey must not be null");
        Objects.requireNonNull(amount, "amount must not be null");

        // Payload 크기 검증
        if (metadata != null && metadata.length() > MAX_METADATA_SIZE) {
            throw new IllegalArgumentException(
                String.format(
                    "Metadata exceeds maximum size: %d bytes (max: %d bytes)",
                    metadata.length(), MAX_METADATA_SIZE
                )
            );
        }
    }
}
```

#### B. 공통 Payload Validator

```java
package com.example.common.orchestration.security;

import org.springframework.stereotype.Component;

/**
 * Payload 크기 검증
 *
 * @author coding-convention-09
 * @since 1.0
 */
@Component
public class PayloadValidator {

    private static final int MAX_PAYLOAD_SIZE = 1024 * 1024;  // 1MB

    /**
     * Payload 크기 검증
     *
     * @param payload 검증할 Payload
     * @throws PayloadTooLargeException 크기 초과 시
     */
    public void validate(Payload payload) {
        long sizeInBytes = payload.sizeInBytes();

        if (sizeInBytes > MAX_PAYLOAD_SIZE) {
            throw new PayloadTooLargeException(
                String.format(
                    "Payload size %d bytes exceeds maximum %d bytes",
                    sizeInBytes, MAX_PAYLOAD_SIZE
                )
            );
        }
    }
}
```

#### C. Spring Boot 설정

```yaml
# application.yml
spring:
  servlet:
    multipart:
      max-file-size: 10MB     # 파일 업로드 최대 크기
      max-request-size: 10MB  # 전체 요청 최대 크기

server:
  max-http-header-size: 16KB  # HTTP 헤더 최대 크기
```

---

## 4. 타이밍 공격 방지 (Constant-Time Comparison)

### 4.1 문제 상황

```java
// ❌ 취약한 코드: 타이밍 공격 가능
@Override
public Outcome accept(PaymentCommand command) {
    Operation existing = operationRepository.findByIdemKey(command.idempotencyKey())
        .orElse(null);

    if (existing != null) {
        // IdemKey 존재 시 응답 시간: 10ms
        return Outcome.ok(existing.opId(), "Already accepted");
    } else {
        // IdemKey 미존재 시 응답 시간: 50ms (DB INSERT 포함)
        Operation newOp = createOperation(command);
        operationRepository.save(newOp);
        return Outcome.ok(newOp.opId(), "Accepted");
    }
}
```

**문제점**:
- 공격자가 응답 시간 차이로 IdemKey 존재 여부 유추 가능
- IdemKey 생성 규칙 역공학 가능

### 4.2 해결책: Constant-Time Response

```java
@Override
@Transactional
public Outcome accept(PaymentCommand command) {
    try {
        // 1️⃣ 항상 INSERT 시도 (Unique 제약으로 중복 차단)
        Operation operation = Operation.create(
            OpId.generate(),
            command.idempotencyKey(),
            command.bizKey(),
            domain(),
            eventType(),
            Payload.of(command)
        );

        operationRepository.save(operation);

        // 2️⃣ 성공 시: 새 Operation 생성
        return Outcome.ok(operation.opId(), "Operation accepted");

    } catch (DataIntegrityViolationException e) {
        // 3️⃣ 중복 시: 기존 Operation 조회
        Operation existing = operationRepository.findByIdemKey(command.idempotencyKey())
            .orElseThrow();

        // 4️⃣ 응답 시간 동일하게 유지
        return Outcome.ok(existing.opId(), "Operation already accepted");
    }
}
```

**장점**:
- 성공/중복 모두 비슷한 응답 시간 (DB 작업 1회)
- 타이밍 공격 불가능

---

## 5. Authorization (Operation 소유권 검증)

### 5.1 문제 상황

```java
// 공격자가 다른 사용자의 Operation 조회
GET /api/operations/OP-a1b2c3d4

// 인증은 되어 있지만, 소유권 검증 없음
// → 다른 사용자의 결제 정보 유출
```

### 5.2 해결책: Ownership Validation

#### A. Operation에 소유자 추가

```java
@Entity
@Table(name = "operations")
public class Operation {

    @Id
    @Column(name = "op_id")
    private String opId;

    @Column(name = "user_id", nullable = false)
    private String userId;  // 소유자 ID

    // ...
}
```

#### B. Orchestrator에 userId 추가

```java
@Service
public class PaymentOrchestrator extends BaseOrchestrator<PaymentCommand> {

    @Override
    @Transactional
    public Outcome accept(PaymentCommand command, String userId) {  // userId 파라미터 추가
        Command orchestrationCommand = Command.create(
            domain(),
            eventType(),
            BizKey.of(command.orderId()),
            IdemKey.of(command.idempotencyKey()),
            Payload.of(command),
            userId  // 소유자 기록
        );

        return super.accept(orchestrationCommand);
    }
}
```

#### C. Controller에서 인증 정보 전달

```java
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(
        @RequestBody PaymentRequest request,
        @AuthenticationPrincipal UserDetails userDetails  // Spring Security
    ) {
        String userId = userDetails.getUsername();

        Outcome outcome = paymentOrchestrator.accept(
            new PaymentCommand(
                request.orderId(),
                request.idempotencyKey(),
                request.amount()
            ),
            userId  // 인증된 사용자 ID
        );

        return ResponseEntity.accepted().body(toResponse(outcome));
    }
}
```

#### D. 조회 시 소유권 검증

```java
@RestController
@RequestMapping("/api/operations")
public class OperationController {

    @GetMapping("/{opId}")
    public ResponseEntity<OperationResponse> getOperation(
        @PathVariable String opId,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userId = userDetails.getUsername();

        Operation operation = operationRepository.findById(opId)
            .orElseThrow(() -> new OperationNotFoundException(opId));

        // 소유권 검증
        if (!operation.userId().equals(userId)) {
            throw new AccessDeniedException(
                "You are not authorized to access this operation"
            );
        }

        return ResponseEntity.ok(toResponse(operation));
    }
}
```

---

## 6. 보안 체크리스트

| 항목 | 구현 | 설명 |
|------|------|------|
| ✅ Rate Limiting | `IdemKeyRateLimiter` | 동일 IdemKey 남용 방지 |
| ✅ Payload 크기 제한 | `PayloadValidator` | DoS 공격 방지 (1MB 제한) |
| ✅ Constant-Time Response | `accept()` 메서드 | 타이밍 공격 방지 |
| ✅ Authorization | `userId` 검증 | Operation 소유권 검증 |
| ✅ HTTPS Only | Spring Security 설정 | 전송 구간 암호화 |
| ✅ Input Validation | `@Valid` 어노테이션 | SQL Injection 방지 |
| ✅ Audit Logging | Spring AOP | 모든 Operation 변경 기록 |

---

## 7. 추가 권장 사항

### 7.1 HTTPS Only

```java
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .requiresChannel(channel -> channel
                .anyRequest().requiresSecure()  // HTTPS 강제
            );
        return http.build();
    }
}
```

### 7.2 Audit Logging

```java
@Aspect
@Component
public class OrchestrationAuditAspect {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    @AfterReturning(
        pointcut = "execution(* com.example..orchestrator.*Orchestrator.accept(..))",
        returning = "outcome"
    )
    public void auditAccept(JoinPoint joinPoint, Outcome outcome) {
        Object[] args = joinPoint.getArgs();
        String userId = extractUserId(args);
        String idemKey = extractIdemKey(args);

        auditLog.info(
            "action=ACCEPT, userId={}, idemKey={}, outcome={}",
            userId, idemKey, outcome.getClass().getSimpleName()
        );
    }
}
```

---

## 8. 테스트

### 8.1 Rate Limiting 테스트

```java
@Test
void accept_ShouldThrowException_WhenRateLimitExceeded() {
    PaymentCommand command = new PaymentCommand(
        "ORDER-001", "IDEM-12345", BigDecimal.valueOf(10000)
    );

    // Given: 5회 성공
    for (int i = 0; i < 5; i++) {
        orchestrator.accept(command, "user-123");
    }

    // When: 6번째 시도
    // Then: TooManyRequestsException
    assertThrows(TooManyRequestsException.class, () ->
        orchestrator.accept(command, "user-123")
    );
}
```

### 8.2 Authorization 테스트

```java
@Test
void getOperation_ShouldThrowException_WhenUnauthorized() {
    // Given: user-123이 생성한 Operation
    Operation operation = createOperation("user-123");

    // When: user-456이 조회 시도
    // Then: AccessDeniedException
    assertThrows(AccessDeniedException.class, () ->
        operationController.getOperation(operation.opId(), "user-456")
    );
}
```

---

## 9. 요약

```
✅ Rate Limiting: IdemKey 남용 방지 (1분에 5회)
✅ Payload 제한: DoS 방지 (최대 1MB)
✅ Constant-Time: 타이밍 공격 방지
✅ Authorization: Operation 소유권 검증
✅ HTTPS Only: 전송 구간 암호화
✅ Audit Logging: 모든 변경 기록
```

**다음 단계**: [07_testing-guide.md](./07_testing-guide.md)에서 테스트 전략을 확인하세요.
