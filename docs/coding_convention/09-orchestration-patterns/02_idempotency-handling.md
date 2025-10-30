# Idempotency Handling - 멱등성 처리

**목적**: 동일한 요청을 여러 번 실행해도 결과가 동일하도록 보장

**관련 문서**:
- [Command Pattern](./01_command-pattern.md)
- [Write-Ahead Log Pattern](./03_write-ahead-log-pattern.md)
- [Implementation Guide](./07_implementation-guide.md)

**필수 버전**: Java 21+, Spring Framework 6.0+

---

## 📌 핵심 원칙

### Idempotency란?

**정의**: 동일한 요청을 여러 번 실행해도 결과가 동일한 성질

```
f(x) = y
f(f(x)) = y
f(f(f(x))) = y
```

**예시**:
- ✅ 멱등: `DELETE /users/123` (여러 번 호출해도 결과 동일)
- ❌ 비멱등: `POST /payments` (매번 새로운 결제 생성)

### 왜 필요한가?

```java
// ❌ 문제 상황
@PostMapping("/api/payments")
public PaymentResponse processPayment(@RequestBody PaymentRequest request) {
    Payment payment = paymentGateway.charge(request.amount());  // ⚠️ 중복 실행 위험
    return new PaymentResponse(payment.id());
}

/**
 * 클라이언트 재시도 시나리오:
 *
 * T1: 첫 번째 요청 → 타임아웃 ⏱️
 * T2: 재시도 → 중복 결제 발생 💸💸
 *
 * 결과: 고객에게 2번 청구됨 ❌
 */
```

---

## 🏗️ Idempotency Key 구조

### 1. IdemKey Value Object

```java
/**
 * 멱등성 키
 *
 * <p><strong>생성 방법:</strong></p>
 * <ul>
 *   <li>클라이언트에서 생성 (UUID 권장)</li>
 *   <li>동일 요청은 동일한 IdemKey 사용</li>
 * </ul>
 *
 * @param value 멱등성 키 (UUID 또는 고유 문자열)
 *
 * @author development-team
 * @since 1.0.0
 */
public record IdemKey(String value) {

    public IdemKey {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("IdemKey cannot be null or blank");
        }
        if (value.length() > 255) {
            throw new IllegalArgumentException("IdemKey length cannot exceed 255 characters");
        }
    }

    public static IdemKey of(String value) {
        return new IdemKey(value);
    }
}
```

### 2. 데이터베이스 스키마

```sql
/**
 * Operation 테이블 - 멱등성 검사용
 */
CREATE TABLE operations (
    op_id VARCHAR(255) PRIMARY KEY,
    idem_key VARCHAR(255) NOT NULL,
    domain VARCHAR(50) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    biz_key VARCHAR(255) NOT NULL,
    state VARCHAR(20) NOT NULL,
    payload TEXT,
    accepted_at BIGINT NOT NULL,
    completed_at BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- ✅ 멱등성 보장을 위한 UNIQUE 제약
    CONSTRAINT uk_operations_idem_key UNIQUE (idem_key)
);

/**
 * 인덱스 생성
 */
CREATE INDEX idx_operations_state ON operations(state);
CREATE INDEX idx_operations_domain ON operations(domain);
CREATE INDEX idx_operations_biz_key ON operations(biz_key);
```

---

## ✅ Idempotency 구현 패턴

### 패턴 1: Database Unique Constraint

```java
/**
 * Store 구현 - 멱등성 검사
 *
 * @author development-team
 * @since 1.0.0
 */
@Repository
public class JpaOperationStore implements Store {

    private final OperationEntityRepository repository;

    /**
     * ✅ 멱등성 보장: 동일 IdemKey로 재요청 시 기존 OpId 반환
     */
    @Override
    @Transactional
    public OpId accept(Command command) {
        // 1. 기존 Operation 조회 (IdemKey 기준)
        Optional<OperationEntity> existing = repository.findByIdemKey(
            command.idemKey().value()
        );

        // 2. 이미 존재하면 기존 OpId 반환 (멱등성 보장)
        if (existing.isPresent()) {
            return OpId.of(existing.get().getOpId());
        }

        // 3. 새로운 Operation 생성
        String opId = UUID.randomUUID().toString();
        OperationEntity entity = OperationEntity.builder()
            .opId(opId)
            .idemKey(command.idemKey().value())
            .domain(command.domain().value())
            .eventType(command.eventType().value())
            .bizKey(command.bizKey().value())
            .state(OperationState.PENDING)
            .payload(command.payload() != null ? command.payload().json() : null)
            .acceptedAt(System.currentTimeMillis())
            .build();

        try {
            repository.save(entity);
            return OpId.of(opId);
        } catch (DataIntegrityViolationException e) {
            // ⚠️ 동시 요청으로 UNIQUE 제약 위반 발생
            // → 기존 Operation 조회하여 반환
            OperationEntity race = repository.findByIdemKey(command.idemKey().value())
                .orElseThrow(() -> new IllegalStateException("Race condition handling failed"));
            return OpId.of(race.getOpId());
        }
    }
}
```

### 패턴 2: 멱등성 검사 Service

```java
/**
 * 멱등성 검사 전용 Service
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class IdempotencyService {

    private final OperationRepository repository;

    /**
     * ✅ 멱등성 검사
     *
     * @param idemKey 멱등성 키
     * @return 기존 OpId (존재하는 경우), 없으면 Optional.empty()
     */
    public Optional<OpId> checkIdempotency(IdemKey idemKey) {
        return repository.findByIdemKey(idemKey.value())
            .map(entity -> OpId.of(entity.getOpId()));
    }

    /**
     * ✅ 멱등성 보장 생성
     *
     * @param command 실행 명령
     * @return OpId (기존 또는 새로 생성)
     */
    @Transactional
    public OpId getOrCreate(Command command) {
        // 1. 기존 Operation 확인
        Optional<OpId> existing = checkIdempotency(command.idemKey());
        if (existing.isPresent()) {
            return existing.get();
        }

        // 2. 새로 생성
        String opId = UUID.randomUUID().toString();
        OperationEntity entity = createEntity(opId, command);

        try {
            repository.save(entity);
            return OpId.of(opId);
        } catch (DataIntegrityViolationException e) {
            // Race condition 처리
            return checkIdempotency(command.idemKey())
                .orElseThrow(() -> new IllegalStateException("Race condition handling failed"));
        }
    }
}
```

---

## 🎯 클라이언트 측 IdemKey 생성

### HTTP Header 패턴

```java
/**
 * REST Controller - IdemKey를 HTTP Header로 받기
 *
 * @author development-team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final Orchestrator orchestrator;

    /**
     * ✅ Idempotency-Key 헤더로 받기
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
        @RequestBody PaymentRequest request,
        @RequestHeader("Idempotency-Key") String idemKey  // ✅ 클라이언트 제공
    ) {
        // 1. Command 생성
        Command command = Command.of(
            Domain.of("PAYMENT"),
            EventType.of("CHARGE"),
            BizKey.of("ORDER-" + request.orderId()),
            IdemKey.of(idemKey),  // ✅ 클라이언트가 제공한 키
            Payload.of(toJson(request))
        );

        // 2. 멱등성 보장된 실행
        OpId opId = orchestrator.start(command, Duration.ofMinutes(5));

        // 3. 응답
        return ResponseEntity.ok(new PaymentResponse(opId.getValue()));
    }
}
```

### 클라이언트 구현 예시

```typescript
/**
 * TypeScript 클라이언트 - Idempotency Key 생성
 */
import { v4 as uuidv4 } from 'uuid';

class PaymentClient {

  async createPayment(request: PaymentRequest): Promise<PaymentResponse> {
    // ✅ 멱등성 키 생성
    const idempotencyKey = uuidv4();

    try {
      const response = await fetch('/api/payments', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Idempotency-Key': idempotencyKey,  // ✅ 헤더로 전송
        },
        body: JSON.stringify(request),
      });

      return await response.json();

    } catch (error) {
      // ⚠️ 네트워크 오류 시 재시도 (동일한 Idempotency Key 사용)
      if (error instanceof NetworkError) {
        return this.retryWithSameKey(request, idempotencyKey);
      }
      throw error;
    }
  }

  private async retryWithSameKey(
    request: PaymentRequest,
    idempotencyKey: string
  ): Promise<PaymentResponse> {
    // ✅ 동일한 Idempotency Key로 재시도
    const response = await fetch('/api/payments', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Idempotency-Key': idempotencyKey,  // ✅ 동일한 키 사용
      },
      body: JSON.stringify(request),
    });

    return await response.json();
  }
}
```

---

## 🎯 실전 예제

### 예제 1: 결제 멱등성 보장

```java
/**
 * 결제 처리 - 멱등성 보장
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class PaymentService {

    private final Orchestrator orchestrator;

    /**
     * ✅ 멱등성 보장된 결제 처리
     */
    @Transactional
    public PaymentResult processPayment(PaymentRequest request, String idemKey) {
        // 1. Command 생성
        Command command = Command.of(
            Domain.of("PAYMENT"),
            EventType.of("CHARGE"),
            BizKey.of("ORDER-" + request.orderId()),
            IdemKey.of(idemKey),
            Payload.of(toJson(request))
        );

        // 2. 멱등성 보장된 실행
        OpId opId = orchestrator.start(command, Duration.ofMinutes(5));

        // 3. 결과 반환
        return new PaymentResult(opId.getValue(), "ACCEPTED");
    }
}

/**
 * 클라이언트 재시도 시나리오:
 *
 * T1: 첫 번째 요청 (idemKey=abc-123)
 *     → OpId=op-001 생성
 *     → 네트워크 타임아웃 ⏱️
 *
 * T2: 재시도 (동일한 idemKey=abc-123)
 *     → 기존 OpId=op-001 반환 ✅
 *     → 중복 결제 방지 ✅
 */
```

### 예제 2: 파일 업로드 멱등성 보장

```java
/**
 * 파일 업로드 - 멱등성 보장
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class FileUploadService {

    private final Orchestrator orchestrator;

    /**
     * ✅ 멱등성 보장된 파일 업로드
     */
    @Transactional
    public FileUploadResult uploadFile(
        MultipartFile file,
        String fileName,
        String idemKey
    ) {
        // 1. Payload 생성
        String payloadJson = """
            {
                "fileName": "%s",
                "fileSize": %d,
                "contentType": "%s"
            }
            """.formatted(fileName, file.getSize(), file.getContentType());

        // 2. Command 생성
        Command command = Command.of(
            Domain.of("FILE"),
            EventType.of("UPLOAD"),
            BizKey.of("FILE-" + fileName),
            IdemKey.of(idemKey),
            Payload.of(payloadJson)
        );

        // 3. 멱등성 보장된 실행
        OpId opId = orchestrator.start(command, Duration.ofMinutes(10));

        return new FileUploadResult(opId.getValue(), fileName);
    }
}

/**
 * 멱등성 보장 시나리오:
 *
 * T1: 첫 번째 업로드 시도 (idemKey=file-abc)
 *     → OpId=op-001 생성
 *     → S3 업로드 중 네트워크 끊김 ⚠️
 *
 * T2: 재시도 (동일한 idemKey=file-abc)
 *     → 기존 OpId=op-001 반환 ✅
 *     → 중복 파일 생성 방지 ✅
 */
```

---

## 🔄 Race Condition 처리

### 동시 요청 시나리오

```
Thread 1                    Thread 2
────────                    ────────
SELECT (idem_key=abc)       SELECT (idem_key=abc)
  → 없음                      → 없음
INSERT (idem_key=abc)       INSERT (idem_key=abc)
  → 성공 ✅                    → UNIQUE 제약 위반 ❌
```

### Race Condition 해결 패턴

```java
/**
 * Race Condition 안전한 멱등성 처리
 *
 * @author development-team
 * @since 1.0.0
 */
@Repository
public class SafeIdempotencyStore implements Store {

    private final OperationRepository repository;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OpId accept(Command command) {
        try {
            // 1. 낙관적 INSERT 시도
            return createNewOperation(command);

        } catch (DataIntegrityViolationException e) {
            // 2. UNIQUE 제약 위반 → 기존 Operation 조회
            return findExistingOperation(command.idemKey());
        }
    }

    private OpId createNewOperation(Command command) {
        String opId = UUID.randomUUID().toString();
        OperationEntity entity = OperationEntity.builder()
            .opId(opId)
            .idemKey(command.idemKey().value())
            .domain(command.domain().value())
            .eventType(command.eventType().value())
            .bizKey(command.bizKey().value())
            .state(OperationState.PENDING)
            .payload(command.payload() != null ? command.payload().json() : null)
            .acceptedAt(System.currentTimeMillis())
            .build();

        repository.save(entity);
        return OpId.of(opId);
    }

    private OpId findExistingOperation(IdemKey idemKey) {
        // ✅ Race condition 발생 시 기존 Operation 조회
        OperationEntity existing = repository.findByIdemKey(idemKey.value())
            .orElseThrow(() -> new IllegalStateException(
                "Race condition: Operation not found after UNIQUE constraint violation"
            ));

        return OpId.of(existing.getOpId());
    }
}
```

---

## 📋 IdemKey 생성 전략

### 전략 1: UUID 기반

```java
/**
 * UUID 기반 IdemKey 생성
 */
public class UuidIdemKeyGenerator {

    public static IdemKey generate() {
        return IdemKey.of(UUID.randomUUID().toString());
    }
}

/**
 * 장점:
 * - 충돌 가능성 매우 낮음
 * - 구현 단순
 *
 * 단점:
 * - 업무적 의미 없음
 * - 사람이 읽기 어려움
 */
```

### 전략 2: 업무 키 조합

```java
/**
 * 업무 키 조합 IdemKey 생성
 */
public class BusinessIdemKeyGenerator {

    /**
     * 주문 ID + 결제 요청 시각 조합
     */
    public static IdemKey forPayment(String orderId, Instant requestTime) {
        String key = "PAY-%s-%d".formatted(orderId, requestTime.toEpochMilli());
        return IdemKey.of(key);
    }

    /**
     * 파일 이름 + 업로드 시각 조합
     */
    public static IdemKey forFileUpload(String fileName, Instant uploadTime) {
        String key = "FILE-%s-%d".formatted(fileName, uploadTime.toEpochMilli());
        return IdemKey.of(key);
    }
}

/**
 * 장점:
 * - 업무적 의미 명확
 * - 디버깅 용이
 *
 * 단점:
 * - 충돌 가능성 고려 필요
 * - 클라이언트와 서버 시각 동기화 필요
 */
```

### 전략 3: Composite Key

```java
/**
 * Composite Key 기반 IdemKey 생성
 */
public class CompositeIdemKeyGenerator {

    /**
     * userId + orderId + timestamp 조합
     */
    public static IdemKey forOrder(String userId, String orderId, Instant timestamp) {
        String composite = "%s:%s:%d".formatted(userId, orderId, timestamp.toEpochMilli());
        String hash = hashSha256(composite);
        return IdemKey.of(hash);
    }

    private static String hashSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}

/**
 * 장점:
 * - 충돌 가능성 매우 낮음
 * - 업무 키 기반 추적 가능
 *
 * 단점:
 * - 해시 계산 오버헤드
 * - 원본 키 복원 불가
 */
```

---

## 🧪 테스트 전략

### 1. 멱등성 검증 테스트

```java
@SpringBootTest
class IdempotencyTest {

    @Autowired
    private Orchestrator orchestrator;

    @Test
    void 동일한_IdemKey로_재요청_시_동일한_OpId_반환() {
        // given
        String idemKey = UUID.randomUUID().toString();
        Command command = Command.of(
            Domain.of("PAYMENT"),
            EventType.of("CHARGE"),
            BizKey.of("ORDER-123"),
            IdemKey.of(idemKey),
            Payload.of("{\"amount\":50000}")
        );

        // when
        OpId firstOpId = orchestrator.start(command, Duration.ofMinutes(5));
        OpId secondOpId = orchestrator.start(command, Duration.ofMinutes(5));

        // then
        assertThat(firstOpId).isEqualTo(secondOpId);
    }

    @Test
    void 다른_IdemKey로_요청_시_다른_OpId_반환() {
        // given
        Command command1 = Command.of(
            Domain.of("PAYMENT"),
            EventType.of("CHARGE"),
            BizKey.of("ORDER-123"),
            IdemKey.of(UUID.randomUUID().toString()),
            Payload.of("{\"amount\":50000}")
        );

        Command command2 = Command.of(
            Domain.of("PAYMENT"),
            EventType.of("CHARGE"),
            BizKey.of("ORDER-123"),
            IdemKey.of(UUID.randomUUID().toString()),
            Payload.of("{\"amount\":50000}")
        );

        // when
        OpId opId1 = orchestrator.start(command1, Duration.ofMinutes(5));
        OpId opId2 = orchestrator.start(command2, Duration.ofMinutes(5));

        // then
        assertThat(opId1).isNotEqualTo(opId2);
    }
}
```

### 2. Race Condition 테스트

```java
@SpringBootTest
class RaceConditionTest {

    @Autowired
    private Orchestrator orchestrator;

    @Test
    void 동시_요청_시_하나의_OpId만_생성() throws InterruptedException {
        // given
        String idemKey = UUID.randomUUID().toString();
        Command command = Command.of(
            Domain.of("PAYMENT"),
            EventType.of("CHARGE"),
            BizKey.of("ORDER-123"),
            IdemKey.of(idemKey),
            Payload.of("{\"amount\":50000}")
        );

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        ConcurrentHashMap<String, OpId> results = new ConcurrentHashMap<>();

        // when
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    OpId opId = orchestrator.start(command, Duration.ofMinutes(5));
                    results.put("thread-" + index, opId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        // then
        Set<OpId> uniqueOpIds = new HashSet<>(results.values());
        assertThat(uniqueOpIds).hasSize(1);  // ✅ 모든 스레드가 동일한 OpId 반환
    }
}
```

---

## 📚 Common Pitfalls

### ❌ Pitfall 1: 서버에서 IdemKey 생성

```java
// ❌ Bad - 클라이언트 재시도 시 멱등성 보장 안 됨
@PostMapping("/api/payments")
public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest request) {
    String idemKey = UUID.randomUUID().toString();  // ❌ 매번 새로운 키
    Command command = createCommand(request, idemKey);
    OpId opId = orchestrator.start(command, Duration.ofMinutes(5));
    return ResponseEntity.ok(new PaymentResponse(opId.getValue()));
}

// ✅ Good - 클라이언트가 IdemKey 제공
@PostMapping("/api/payments")
public ResponseEntity<PaymentResponse> createPayment(
    @RequestBody PaymentRequest request,
    @RequestHeader("Idempotency-Key") String idemKey  // ✅ 클라이언트 제공
) {
    Command command = createCommand(request, idemKey);
    OpId opId = orchestrator.start(command, Duration.ofMinutes(5));
    return ResponseEntity.ok(new PaymentResponse(opId.getValue()));
}
```

### ❌ Pitfall 2: IdemKey 재사용

```java
// ❌ Bad - 다른 요청에 동일한 IdemKey 재사용
String idemKey = "payment-key";  // ❌ 고정된 키
for (PaymentRequest request : requests) {
    Command command = createCommand(request, idemKey);  // ❌ 모든 요청이 동일한 키
    orchestrator.start(command, Duration.ofMinutes(5));
}

// ✅ Good - 요청마다 고유한 IdemKey
for (PaymentRequest request : requests) {
    String idemKey = UUID.randomUUID().toString();  // ✅ 고유한 키
    Command command = createCommand(request, idemKey);
    orchestrator.start(command, Duration.ofMinutes(5));
}
```

### ❌ Pitfall 3: UNIQUE 제약 없음

```sql
-- ❌ Bad - UNIQUE 제약 없음 (중복 가능)
CREATE TABLE operations (
    op_id VARCHAR(255) PRIMARY KEY,
    idem_key VARCHAR(255) NOT NULL  -- ❌ UNIQUE 제약 없음
);

-- ✅ Good - UNIQUE 제약 추가
CREATE TABLE operations (
    op_id VARCHAR(255) PRIMARY KEY,
    idem_key VARCHAR(255) NOT NULL,
    CONSTRAINT uk_operations_idem_key UNIQUE (idem_key)  -- ✅ UNIQUE 제약
);
```

---

**작성자**: Development Team
**최종 수정일**: 2025-10-30
**버전**: 1.0.0
