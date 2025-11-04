# Command Pattern - 실행 요청 캡슐화

**목적**: 외부 API 호출 요청을 명확하고 타입 안전하게 표현

**관련 문서**:
- [Orchestration Pattern Overview](./00_orchestration-pattern-overview.md)
- [Idempotency Handling](./02_idempotency-handling.md)
- [Record Patterns](../06-java21-patterns/record-patterns/01_dto-with-records.md)

**필수 버전**: Java 21+

---

## 📌 핵심 원칙

### Command Pattern이란?

1. **실행 요청 캡슐화**: 실행에 필요한 모든 정보를 하나의 객체로 표현
2. **불변성**: 생성 후 변경 불가능
3. **타입 안전성**: 컴파일 타임에 유효성 검증
4. **직렬화 가능**: 큐, DB, 로그에 저장 가능

### 왜 필요한가?

```java
// ❌ Before - 메서드 파라미터로 전달 (문제점)
public void processPayment(
    String domain,           // 도메인이 뭐지?
    String eventType,        // 이벤트 타입은?
    String businessKey,      // 필수인가?
    String idempotencyKey,   // null 가능한가?
    String jsonPayload       // JSON 형식이 맞나?
) {
    // ❌ 유효성 검증 누락 위험
    // ❌ 파라미터 순서 혼동 위험
    // ❌ 도메인 로직 분산
}

// ✅ After - Command 객체로 캡슐화
public void processPayment(Command command) {
    // ✅ 이미 유효성 검증됨
    // ✅ 파라미터 순서 혼동 없음
    // ✅ 도메인 로직 집중
}
```

---

## 🏗️ Command 구조

### 핵심 컴포넌트

```java
/**
 * Command - 외부 API 호출 요청 캡슐화
 *
 * @param domain 업무 도메인 (예: ORDER, PAYMENT, FILE)
 * @param eventType 이벤트 유형 (예: CREATE, UPDATE, DELETE)
 * @param bizKey 비즈니스 키 (업무 엔티티 식별자)
 * @param idemKey 멱등성 키 (클라이언트 제공, 중복 요청 방지)
 * @param payload 업무 데이터 (직렬화된 JSON)
 *
 * @author development-team
 * @since 1.0.0
 */
public record Command(
    Domain domain,
    EventType eventType,
    BizKey bizKey,
    IdemKey idemKey,
    Payload payload
) {

    /**
     * Compact Constructor - 유효성 검증
     */
    public Command {
        if (domain == null) {
            throw new IllegalArgumentException("domain cannot be null");
        }
        if (eventType == null) {
            throw new IllegalArgumentException("eventType cannot be null");
        }
        if (bizKey == null) {
            throw new IllegalArgumentException("bizKey cannot be null");
        }
        if (idemKey == null) {
            throw new IllegalArgumentException("idemKey cannot be null");
        }
        // payload는 null 허용 (빈 Command도 가능)
    }

    /**
     * Static Factory Method - 명시적 생성
     */
    public static Command of(
        Domain domain,
        EventType eventType,
        BizKey bizKey,
        IdemKey idemKey,
        Payload payload
    ) {
        return new Command(domain, eventType, bizKey, idemKey, payload);
    }
}
```

---

## 🎯 Value Objects 정의

### 1. Domain (도메인)

```java
/**
 * 업무 도메인
 *
 * <p><strong>사용 예시:</strong></p>
 * <ul>
 *   <li>ORDER - 주문 도메인</li>
 *   <li>PAYMENT - 결제 도메인</li>
 *   <li>FILE - 파일 도메인</li>
 *   <li>NOTIFICATION - 알림 도메인</li>
 * </ul>
 *
 * @param value 도메인 식별자 (대문자 권장)
 */
public record Domain(String value) {

    public Domain {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Domain cannot be null or blank");
        }
        if (value.length() > 50) {
            throw new IllegalArgumentException("Domain length cannot exceed 50 characters");
        }
    }

    public static Domain of(String value) {
        return new Domain(value);
    }
}
```

### 2. EventType (이벤트 타입)

```java
/**
 * 이벤트 유형
 *
 * <p><strong>사용 예시:</strong></p>
 * <ul>
 *   <li>CREATE - 생성</li>
 *   <li>UPDATE - 수정</li>
 *   <li>DELETE - 삭제</li>
 *   <li>CHARGE - 결제</li>
 *   <li>UPLOAD - 업로드</li>
 * </ul>
 *
 * @param value 이벤트 타입 (대문자 권장)
 */
public record EventType(String value) {

    public EventType {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("EventType cannot be null or blank");
        }
        if (value.length() > 50) {
            throw new IllegalArgumentException("EventType length cannot exceed 50 characters");
        }
    }

    public static EventType of(String value) {
        return new EventType(value);
    }
}
```

### 3. BizKey (비즈니스 키)

```java
/**
 * 비즈니스 키 - 업무 엔티티 식별자
 *
 * <p><strong>사용 예시:</strong></p>
 * <ul>
 *   <li>ORDER-12345 - 주문 번호</li>
 *   <li>USER-67890 - 사용자 ID</li>
 *   <li>FILE-uuid - 파일 식별자</li>
 * </ul>
 *
 * <p><strong>주의사항:</strong></p>
 * <ul>
 *   <li>파티션 키로 사용 가능 (Queue 분산)</li>
 *   <li>업무적으로 의미 있는 키 사용 권장</li>
 * </ul>
 *
 * @param value 비즈니스 키
 */
public record BizKey(String value) {

    public BizKey {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("BizKey cannot be null or blank");
        }
        if (value.length() > 255) {
            throw new IllegalArgumentException("BizKey length cannot exceed 255 characters");
        }
    }

    public static BizKey of(String value) {
        return new BizKey(value);
    }
}
```

### 4. IdemKey (멱등성 키)

```java
/**
 * 멱등성 키 - 중복 요청 방지
 *
 * <p><strong>생성 방법:</strong></p>
 * <ul>
 *   <li>클라이언트에서 생성 (UUID 권장)</li>
 *   <li>동일 요청은 동일한 IdemKey 사용</li>
 * </ul>
 *
 * <p><strong>예시:</strong></p>
 * <pre>
 * String idemKey = UUID.randomUUID().toString();
 * IdemKey key = IdemKey.of(idemKey);
 * </pre>
 *
 * @param value 멱등성 키 (UUID 또는 고유 문자열)
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

### 5. Payload (페이로드)

```java
/**
 * 업무 데이터 페이로드
 *
 * <p><strong>형식:</strong></p>
 * <ul>
 *   <li>JSON 문자열 (직렬화된 형태)</li>
 *   <li>null 허용 (빈 Command 가능)</li>
 * </ul>
 *
 * <p><strong>예시:</strong></p>
 * <pre>
 * Payload payload = Payload.of("{\"amount\":50000,\"currency\":\"KRW\"}");
 * </pre>
 *
 * @param json JSON 문자열
 */
public record Payload(String json) {

    /**
     * null 허용 (빈 페이로드 가능)
     */
    public Payload {
        // json은 null 허용
    }

    public static Payload of(String json) {
        return new Payload(json);
    }

    public static Payload empty() {
        return new Payload(null);
    }
}
```

---

## 🎯 실전 예제

### 예제 1: 결제 Command

```java
/**
 * 결제 요청 Command 생성
 */
public class PaymentCommandFactory {

    public static Command createChargeCommand(PaymentRequest request) {
        // 1. 멱등성 키 생성 (클라이언트에서 제공 받는 것이 권장)
        String idemKey = UUID.randomUUID().toString();

        // 2. Payload JSON 생성
        String payloadJson = """
            {
                "orderId": "%s",
                "amount": %d,
                "currency": "KRW",
                "cardNumber": "%s"
            }
            """.formatted(
                request.orderId(),
                request.amount(),
                request.cardNumber()
            );

        // 3. Command 생성
        return Command.of(
            Domain.of("PAYMENT"),
            EventType.of("CHARGE"),
            BizKey.of("ORDER-" + request.orderId()),
            IdemKey.of(idemKey),
            Payload.of(payloadJson)
        );
    }
}
```

### 예제 2: 파일 업로드 Command

```java
/**
 * 파일 업로드 Command 생성
 */
public class FileCommandFactory {

    public static Command createUploadCommand(FileUploadRequest request) {
        String idemKey = UUID.randomUUID().toString();

        String payloadJson = """
            {
                "fileName": "%s",
                "fileSize": %d,
                "contentType": "%s",
                "bucket": "%s",
                "key": "%s"
            }
            """.formatted(
                request.fileName(),
                request.fileSize(),
                request.contentType(),
                request.bucket(),
                request.key()
            );

        return Command.of(
            Domain.of("FILE"),
            EventType.of("UPLOAD"),
            BizKey.of("FILE-" + request.fileName()),
            IdemKey.of(idemKey),
            Payload.of(payloadJson)
        );
    }
}
```

### 예제 3: 알림 발송 Command

```java
/**
 * 알림 발송 Command 생성
 */
public class NotificationCommandFactory {

    public static Command createSendCommand(NotificationRequest request) {
        String idemKey = UUID.randomUUID().toString();

        String payloadJson = """
            {
                "userId": "%s",
                "title": "%s",
                "message": "%s",
                "channel": "%s"
            }
            """.formatted(
                request.userId(),
                request.title(),
                request.message(),
                request.channel()
            );

        return Command.of(
            Domain.of("NOTIFICATION"),
            EventType.of("SEND"),
            BizKey.of("USER-" + request.userId()),
            IdemKey.of(idemKey),
            Payload.of(payloadJson)
        );
    }
}
```

---

## 🔄 Command 역직렬화

### Payload에서 객체 추출

```java
/**
 * Payload JSON → 객체 변환
 */
public class PayloadParser {

    private final ObjectMapper objectMapper;

    public PayloadParser() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Payload에서 특정 타입의 객체 추출
     */
    public <T> T parse(Payload payload, Class<T> type) {
        if (payload == null || payload.json() == null) {
            throw new IllegalArgumentException("Payload is empty");
        }

        try {
            return objectMapper.readValue(payload.json(), type);
        } catch (JsonProcessingException e) {
            throw new PayloadParseException("Failed to parse payload: " + payload.json(), e);
        }
    }

    /**
     * Payload에서 Map 추출
     */
    public Map<String, Object> parseAsMap(Payload payload) {
        return parse(payload, new TypeReference<Map<String, Object>>() {});
    }
}

/**
 * 사용 예시
 */
public class PaymentExecutor {

    private final PayloadParser parser;

    public Outcome execute(Envelope envelope) {
        // 1. Payload 파싱
        PaymentData data = parser.parse(
            envelope.command().payload(),
            PaymentData.class
        );

        // 2. 외부 API 호출
        PaymentApiResponse response = paymentGateway.charge(
            data.amount(),
            data.cardNumber()
        );

        // 3. 결과 반환
        return Ok.of(envelope.opId(), "Payment successful: " + response.transactionId());
    }
}

/**
 * Payload 데이터 모델
 */
record PaymentData(
    String orderId,
    long amount,
    String currency,
    String cardNumber
) {}
```

---

## 📋 Command 검증 전략

### 1. 생성 시점 검증 (Compact Constructor)

```java
public record Command(
    Domain domain,
    EventType eventType,
    BizKey bizKey,
    IdemKey idemKey,
    Payload payload
) {
    /**
     * ✅ 생성 시점에 필수 필드 검증
     */
    public Command {
        if (domain == null) {
            throw new IllegalArgumentException("domain cannot be null");
        }
        if (eventType == null) {
            throw new IllegalArgumentException("eventType cannot be null");
        }
        if (bizKey == null) {
            throw new IllegalArgumentException("bizKey cannot be null");
        }
        if (idemKey == null) {
            throw new IllegalArgumentException("idemKey cannot be null");
        }
    }
}
```

### 2. 비즈니스 규칙 검증 (별도 Validator)

```java
/**
 * Command 비즈니스 규칙 검증
 */
@Component
public class CommandValidator {

    /**
     * 도메인별 비즈니스 규칙 검증
     */
    public void validate(Command command) {
        switch (command.domain().value()) {
            case "PAYMENT" -> validatePaymentCommand(command);
            case "FILE" -> validateFileCommand(command);
            case "NOTIFICATION" -> validateNotificationCommand(command);
            default -> throw new UnsupportedDomainException(
                "Unsupported domain: " + command.domain().value()
            );
        }
    }

    private void validatePaymentCommand(Command command) {
        if (command.payload() == null || command.payload().json() == null) {
            throw new InvalidCommandException("Payment command requires payload");
        }

        // Payload 내부 검증
        Map<String, Object> data = parsePayload(command.payload());
        if (!data.containsKey("amount")) {
            throw new InvalidCommandException("Payment command requires 'amount' field");
        }

        long amount = ((Number) data.get("amount")).longValue();
        if (amount <= 0) {
            throw new InvalidCommandException("Payment amount must be positive");
        }
    }

    private void validateFileCommand(Command command) {
        // 파일 업로드 규칙 검증
    }

    private void validateNotificationCommand(Command command) {
        // 알림 발송 규칙 검증
    }
}
```

---

## 🎯 Best Practices

### 1. IdemKey는 클라이언트에서 생성

```java
// ✅ Good - 클라이언트가 IdemKey 생성
@RestController
public class PaymentController {

    @PostMapping("/api/payments")
    public ResponseEntity<PaymentResponse> createPayment(
        @RequestBody PaymentRequest request,
        @RequestHeader("X-Idempotency-Key") String idemKey  // ✅ 클라이언트 제공
    ) {
        Command command = Command.of(
            Domain.of("PAYMENT"),
            EventType.of("CHARGE"),
            BizKey.of("ORDER-" + request.orderId()),
            IdemKey.of(idemKey),  // ✅ 헤더에서 받은 값 사용
            Payload.of(toJson(request))
        );

        OpId opId = orchestrator.start(command, Duration.ofMinutes(5));
        return ResponseEntity.ok(new PaymentResponse(opId.getValue()));
    }
}

// ❌ Bad - 서버에서 IdemKey 생성
@PostMapping("/api/payments")
public ResponseEntity<PaymentResponse> createPayment(
    @RequestBody PaymentRequest request
) {
    String idemKey = UUID.randomUUID().toString();  // ❌ 클라이언트가 재시도 시 다른 키
    // ...
}
```

### 2. BizKey는 파티션 키로 활용

```java
/**
 * BizKey를 파티션 키로 사용하여 Queue 분산
 */
public class QueuePublisher {

    public void publish(Envelope envelope) {
        // ✅ BizKey를 파티션 키로 사용
        String partitionKey = envelope.command().bizKey().value();

        kafkaTemplate.send(
            "orchestrator-topic",
            partitionKey,  // ✅ 동일 BizKey는 동일 파티션으로
            envelope
        );
    }
}
```

### 3. Payload는 직렬화 가능한 형태로

```java
// ✅ Good - 직렬화 가능한 JSON
Payload payload = Payload.of("""
    {
        "amount": 50000,
        "currency": "KRW"
    }
    """);

// ❌ Bad - 직렬화 불가능한 객체 참조
class PaymentPayload {
    private Connection dbConnection;  // ❌ 직렬화 불가
    private HttpClient httpClient;    // ❌ 직렬화 불가
}
```

### 4. Domain/EventType은 대문자 상수로

```java
/**
 * 도메인 상수
 */
public final class Domains {
    public static final Domain PAYMENT = Domain.of("PAYMENT");
    public static final Domain FILE = Domain.of("FILE");
    public static final Domain NOTIFICATION = Domain.of("NOTIFICATION");

    private Domains() {}
}

/**
 * 이벤트 타입 상수
 */
public final class EventTypes {
    public static final EventType CREATE = EventType.of("CREATE");
    public static final EventType UPDATE = EventType.of("UPDATE");
    public static final EventType DELETE = EventType.of("DELETE");
    public static final EventType CHARGE = EventType.of("CHARGE");
    public static final EventType UPLOAD = EventType.of("UPLOAD");

    private EventTypes() {}
}

/**
 * 사용 예시
 */
Command command = Command.of(
    Domains.PAYMENT,
    EventTypes.CHARGE,
    BizKey.of("ORDER-123"),
    IdemKey.of(idemKey),
    payload
);
```

---

## 🧪 테스트 전략

### 1. Value Object 테스트

```java
class DomainTest {

    @Test
    void 정상적인_도메인_생성() {
        // given
        String value = "PAYMENT";

        // when
        Domain domain = Domain.of(value);

        // then
        assertThat(domain.value()).isEqualTo("PAYMENT");
    }

    @Test
    void null_도메인_생성_실패() {
        // when & then
        assertThatThrownBy(() -> Domain.of(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null");
    }

    @Test
    void 빈_문자열_도메인_생성_실패() {
        // when & then
        assertThatThrownBy(() -> Domain.of(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null or blank");
    }

    @Test
    void 길이_초과_도메인_생성_실패() {
        // given
        String tooLong = "A".repeat(51);

        // when & then
        assertThatThrownBy(() -> Domain.of(tooLong))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot exceed 50 characters");
    }
}
```

### 2. Command 테스트

```java
class CommandTest {

    @Test
    void 정상적인_Command_생성() {
        // given
        Domain domain = Domain.of("PAYMENT");
        EventType eventType = EventType.of("CHARGE");
        BizKey bizKey = BizKey.of("ORDER-123");
        IdemKey idemKey = IdemKey.of(UUID.randomUUID().toString());
        Payload payload = Payload.of("{\"amount\":50000}");

        // when
        Command command = Command.of(domain, eventType, bizKey, idemKey, payload);

        // then
        assertThat(command.domain()).isEqualTo(domain);
        assertThat(command.eventType()).isEqualTo(eventType);
        assertThat(command.bizKey()).isEqualTo(bizKey);
        assertThat(command.idemKey()).isEqualTo(idemKey);
        assertThat(command.payload()).isEqualTo(payload);
    }

    @Test
    void null_domain_Command_생성_실패() {
        // when & then
        assertThatThrownBy(() -> Command.of(
            null,
            EventType.of("CHARGE"),
            BizKey.of("ORDER-123"),
            IdemKey.of("idem-123"),
            Payload.of("{}")
        ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("domain cannot be null");
    }

    @Test
    void payload_null_허용() {
        // given
        Command command = Command.of(
            Domain.of("PAYMENT"),
            EventType.of("CHARGE"),
            BizKey.of("ORDER-123"),
            IdemKey.of("idem-123"),
            null  // ✅ payload는 null 허용
        );

        // then
        assertThat(command.payload()).isNull();
    }
}
```

---

## 📚 Common Pitfalls

### ❌ Pitfall 1: 서버에서 IdemKey 생성

```java
// ❌ Bad
@PostMapping("/api/payments")
public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest request) {
    String idemKey = UUID.randomUUID().toString();  // ❌ 매번 다른 키
    Command command = Command.of(domain, eventType, bizKey, IdemKey.of(idemKey), payload);
    // → 클라이언트가 재시도해도 멱등성 보장 안 됨
}

// ✅ Good
@PostMapping("/api/payments")
public ResponseEntity<PaymentResponse> createPayment(
    @RequestBody PaymentRequest request,
    @RequestHeader("X-Idempotency-Key") String idemKey  // ✅ 클라이언트 제공
) {
    Command command = Command.of(domain, eventType, bizKey, IdemKey.of(idemKey), payload);
}
```

### ❌ Pitfall 2: Payload에 직렬화 불가능한 객체 포함

```java
// ❌ Bad
class PaymentPayload {
    private LocalDateTime timestamp;  // ❌ 직렬화 시 문제 발생 가능
    private BigDecimal amount;        // ❌ JSON 직렬화 복잡
}

// ✅ Good
String payloadJson = """
    {
        "timestamp": "2025-10-30T10:00:00Z",
        "amount": "50000"
    }
    """;
Payload payload = Payload.of(payloadJson);
```

### ❌ Pitfall 3: BizKey에 고유성 없는 값 사용

```java
// ❌ Bad
BizKey bizKey = BizKey.of("ORDER");  // ❌ 모든 주문이 동일한 BizKey

// ✅ Good
BizKey bizKey = BizKey.of("ORDER-" + orderId);  // ✅ 고유한 식별자
```

---

**작성자**: Development Team
**최종 수정일**: 2025-10-30
**버전**: 1.0.0
