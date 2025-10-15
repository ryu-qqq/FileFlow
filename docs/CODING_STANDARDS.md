# 🎯 Hexagonal Architecture Coding Standards

이 문서는 **표준화된 Spring Boot 프로젝트**의 코딩 표준을 정의합니다.
모든 레이어는 **Hexagonal Architecture (Ports & Adapters)** 원칙을 엄격히 준수해야 합니다.

---

## 📐 아키텍처 원칙

### 의존성 방향 (Dependency Rule)

```
Bootstrap → Adapter → Application → Domain
               ↓           ↓
           (구현)      (인터페이스)
```

#### ✅ 허용되는 의존성
- **Domain**: 아무것도 의존하지 않음 (완전 독립)
- **Application**: Domain만 의존
- **Adapter-In**: Application(Port) + Domain 의존
- **Adapter-Out**: Application(Port) + Domain 의존
- **Bootstrap**: 모든 레이어 의존 (조립 목적)

#### ❌ 금지되는 의존성
- Adapter → Adapter (Adapter 간 직접 의존 절대 금지)
- Application → Adapter (구체 구현 의존 금지)
- Domain → 모든 외부 의존성 (완전 순수성)
- 모든 레이어의 순환 의존성

---

## 🏛️ Domain Layer 규칙

### 1. 완전한 순수성 (Purity)

#### ❌ 금지 사항
```java
// ❌ Spring Framework 의존
import org.springframework.*;

// ❌ JPA/Hibernate 의존
import jakarta.persistence.*;
import org.hibernate.*;

// ❌ Lombok
import lombok.*;

// ❌ 인프라 라이브러리
import com.amazonaws.*;
import org.apache.http.*;
```

#### ✅ 허용 사항
```java
// ✅ Java 표준 라이브러리
import java.util.*;
import java.time.*;

// ✅ Jakarta Validation (표준)
import jakarta.validation.*;

// ✅ 순수 유틸리티
import org.apache.commons.lang3.StringUtils;
```

### 2. 불변성 (Immutability)

#### ❌ Bad
```java
public class Order {
    private Long id;
    private String status;

    // ❌ Setter 금지
    public void setStatus(String status) {
        this.status = status;
    }
}
```

#### ✅ Good
```java
public class Order {
    private final OrderId id;
    private final OrderStatus status;

    private Order(OrderId id, OrderStatus status) {
        this.id = id;
        this.status = status;
    }

    // ✅ 수정은 새 객체 반환
    public Order confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Cannot confirm non-pending order");
        }
        return new Order(this.id, OrderStatus.CONFIRMED);
    }

    // ✅ Getter만
    public OrderId getId() { return id; }
    public OrderStatus getStatus() { return status; }
}
```

### 3. 생성 규칙

#### ❌ Bad
```java
// ❌ Public 생성자 금지
public class Order {
    public Order(Long id, String status) { }
}
```

#### ✅ Good
```java
public class Order {
    // ✅ Private 생성자
    private Order(OrderId id, OrderStatus status) {
        this.id = Objects.requireNonNull(id, "Order ID cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
    }

    // ✅ 정적 팩토리 메서드
    public static Order create(OrderId id, List<OrderItem> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        return new Order(id, OrderStatus.PENDING);
    }

    // ✅ 재구성용 (영속성 계층에서 복원 시)
    public static Order reconstitute(OrderId id, OrderStatus status, LocalDateTime createdAt) {
        Order order = new Order(id, status);
        // 추가 복원 로직
        return order;
    }
}
```

### 4. 비즈니스 로직 위치

#### ✅ 규칙
- 모든 비즈니스 규칙은 **Domain 객체 내부**에 위치
- 도메인 서비스는 **여러 Aggregate 간 로직**만 담당
- 계산, 검증, 상태 전이는 **Domain 객체 메서드**로

#### ✅ Good
```java
public class Order {
    public Money calculateTotal() {
        return items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(Money.ZERO, Money::add);
    }

    public Order cancel() {
        if (this.status == OrderStatus.SHIPPED) {
            throw new OrderAlreadyShippedException("Cannot cancel shipped order");
        }
        return new Order(this.id, OrderStatus.CANCELLED);
    }
}
```

### 5. Value Object

#### ✅ Record 사용 권장
```java
// ✅ 식별자가 없는 값 객체는 record
public record Money(BigDecimal amount, Currency currency) {
    public Money {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add different currencies");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
}

public record OrderId(Long value) {
    public OrderId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Order ID must be positive");
        }
    }
}
```

### 6. 예외 처리

#### ✅ Domain 전용 예외
```java
// ✅ 도메인 예외 계층
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) {
        super(message);
    }
}

public class InvalidOrderStateException extends DomainException {
    public InvalidOrderStateException(String message) {
        super(message);
    }
}

public class OrderNotFoundException extends DomainException {
    public OrderNotFoundException(OrderId orderId) {
        super("Order not found: " + orderId);
    }
}
```

### 7. Single Responsibility Principle (SRP) 메트릭

Domain 클래스는 **단일 책임**을 가져야 하며, 다음 메트릭으로 검증됩니다:

#### SRP 기준 (Domain Layer - 가장 엄격)

| 메트릭 | 제한 | 근거 |
|--------|------|------|
| Public 메서드 수 | ≤ 7개 | 많은 메서드 = 여러 책임 의심 |
| Instance 필드 수 | ≤ 5개 | 많은 필드 = 여러 관심사 의심 |
| 클래스 라인 수 | ≤ 200 라인 | 길수록 복잡도 증가 |
| LCOM (Lack of Cohesion) | 낮을수록 좋음 | 높을수록 응집도 낮음 |

**검증 방법:**
- ArchUnit 테스트: `SingleResponsibilityTest.java`
- PMD 정적 분석: `GodClass` 룰 (LCOM 측정)

#### ❌ Bad - SRP 위반
```java
// ❌ 너무 많은 책임
public class Order {
    // ❌ 10개 이상의 필드 (여러 관심사)
    private OrderId id;
    private UserId userId;
    private OrderStatus status;
    private Money total;
    private ShippingAddress address;
    private PaymentMethod payment;
    private CouponCode coupon;
    private DeliveryTracker tracker;
    private InventoryChecker inventory;
    private TaxCalculator taxCalc;

    // ❌ 10개 이상의 public 메서드 (여러 책임)
    public void validate() { }
    public Money calculateTotal() { }
    public Money calculateTax() { }
    public Money calculateShipping() { }
    public Money applyDiscount() { }
    public void checkInventory() { }
    public void reserveStock() { }
    public void processPayment() { }
    public void sendNotification() { }
    public void trackDelivery() { }
    public void generateInvoice() { }
    // ... 더 많은 메서드
}
```

#### ✅ Good - 단일 책임
```java
// ✅ Order는 주문 상태와 금액 계산에만 집중
public class Order {
    private final OrderId id;
    private final UserId userId;
    private final OrderStatus status;
    private final List<OrderItem> items;
    private final LocalDateTime createdAt;

    // ✅ 7개 이하의 public 메서드
    public OrderId getId() { return id; }
    public OrderStatus getStatus() { return status; }

    public Money calculateTotal() {
        return items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(Money.ZERO, Money::add);
    }

    public Order confirm() {
        if (status != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Only pending orders can be confirmed");
        }
        return new Order(id, userId, OrderStatus.CONFIRMED, items, createdAt);
    }

    public Order cancel() {
        if (status == OrderStatus.SHIPPED) {
            throw new InvalidOrderStateException("Shipped orders cannot be cancelled");
        }
        return new Order(id, userId, OrderStatus.CANCELLED, items, createdAt);
    }
}

// ✅ 다른 책임은 별도 클래스로 분리
public class OrderShipping {
    private final OrderId orderId;
    private final ShippingAddress address;

    public Money calculateShippingCost() { }
    public void trackDelivery() { }
}

public class OrderPayment {
    private final OrderId orderId;
    private final PaymentMethod method;

    public void processPayment(Money amount) { }
    public void refund(Money amount) { }
}
```

**LCOM (Lack of Cohesion in Methods) 측정:**
- PMD의 `GodClass` 룰로 자동 측정
- LCOM > 0.8 이면 클래스 분리 검토 필요
- 메서드들이 서로 다른 필드를 사용하면 LCOM 높아짐 → SRP 위반 신호

### 8. Law of Demeter (데미터의 법칙)

**핵심 원칙**: 객체는 자기 자신, 메서드 파라미터, 생성한 객체, 인스턴스 변수만 접근

#### 금지 패턴: Getter 체이닝 (Train Wreck)

**절대 금지:**
```java
// ❌ Getter 체이닝 절대 금지
obj.getX().getY().getZ()
```

**PMD 검증:**
- `DomainLayerDemeterStrict` 룰
- XPath AST 분석으로 2단계 이상 체이닝 감지
- `//PrimaryExpression[count(PrimarySuffix) > 1]`

#### ❌ Bad - Law of Demeter 위반
```java
// ❌ Getter 체이닝
public class OrderService {
    public Money calculateShipping(Order order) {
        // ❌ 3단계 체이닝
        String city = order.getShippingAddress().getCity().getName();

        // ❌ 2단계 체이닝
        String zipCode = order.getShippingAddress().getZipCode();

        // ❌ 중간 객체 조작
        order.getCustomer().getAddress().updateZipCode("12345");

        return calculateByCityAndZip(city, zipCode);
    }
}

// ❌ Getter만 제공 (Tell, Don't Ask 위반)
public class Order {
    private ShippingAddress shippingAddress;

    public ShippingAddress getShippingAddress() {
        return shippingAddress;
    }
}
```

#### ✅ Good - Tell, Don't Ask 패턴

```java
// ✅ 위임 메서드 제공 (Delegation Methods)
public class Order {
    private final ShippingAddress shippingAddress;

    // ✅ Getter 노출 대신 위임 메서드 제공
    public String getShippingCity() {
        return shippingAddress.getCityName();
    }

    public String getShippingZipCode() {
        return shippingAddress.getZipCode();
    }

    // ✅ 비즈니스 로직 캡슐화
    public Money calculateShippingCost() {
        return shippingAddress.calculateShippingCost();
    }
}

// ✅ ShippingAddress도 위임 패턴 사용
public class ShippingAddress {
    private final City city;
    private final ZipCode zipCode;

    public String getCityName() {
        return city.getName();
    }

    public Money calculateShippingCost() {
        return city.getShippingRate();
    }
}
```

#### 허용 패턴

**✅ 허용되는 체이닝:**
```java
// ✅ Builder 패턴 (Fluent API)
Order order = Order.builder()
    .id(orderId)
    .userId(userId)
    .build();

// ✅ Stream API
Money total = orders.stream()
    .map(Order::getTotal)
    .reduce(Money.ZERO, Money::add);

// ✅ StringBuilder
String message = new StringBuilder()
    .append("Order ")
    .append(orderId)
    .toString();
```

**검증 도구:**
- ArchUnit: `LawOfDemeterTest.java`
- PMD: `DomainLayerDemeterStrict` 룰
- 위반 시 컴파일 단계에서 차단

---

## 🔧 Application Layer 규칙

### 1. Port 책임 원칙 (Port Responsibility Principle)

Port는 **레이어 간 인터페이스**로서 명확한 책임 범위를 가져야 합니다.

#### Port 종류별 책임

**Inbound Port (UseCase)**
- **책임**: 비즈니스 유스케이스 정의만
- **포함**: 메서드 시그니처, 입출력 DTO 정의
- **제외**: 비즈니스 규칙, 검증 로직, 트랜잭션 관리

**Outbound Port**
- **책임**: 데이터 영속성 및 외부 시스템 연동 추상화만
- **포함**: 저장, 조회, 삭제 같은 데이터 작업
- **제외**: 비즈니스 규칙, 검증 로직, 상태 전이

#### ❌ Bad - Port에 비즈니스 규칙 포함

```java
/**
 * UploadPolicy 삭제를 위한 Outbound Port
 */
public interface DeleteUploadPolicyPort {
    /**
     * PolicyKey에 해당하는 UploadPolicy를 삭제합니다.
     *
     * 비즈니스 규칙:
     * - 활성화된 정책은 삭제할 수 없습니다  ❌ Port의 책임 아님!
     * - PolicyKey에 해당하는 정책이 존재하지 않으면 예외 발생  ❌
     */
    void delete(PolicyKey policyKey);
}
```

**문제점:**
- 비즈니스 규칙은 Application Service의 책임
- Port는 순수 데이터 작업만 담당해야 함
- 책임 경계가 모호해짐

#### ✅ Good - Port는 순수 데이터 작업만

```java
/**
 * UploadPolicy 삭제를 위한 Outbound Port
 *
 * <p>Persistence Adapter에서 구현하며, 데이터 영속성 작업만 수행합니다.
 * 비즈니스 규칙 검증은 Application Service에서 처리합니다.</p>
 *
 * @see DeleteUploadPolicyService 비즈니스 규칙은 여기서 처리
 */
public interface DeleteUploadPolicyPort {
    /**
     * PolicyKey에 해당하는 UploadPolicy를 삭제합니다.
     *
     * @param policyKey 삭제할 정책의 키
     * @throws IllegalArgumentException policyKey가 null인 경우
     */
    void delete(PolicyKey policyKey);
}
```

```java
// ✅ 비즈니스 규칙은 Application Service에서
@UseCase
@Transactional
public class DeleteUploadPolicyService implements DeleteUploadPolicyUseCase {
    private final LoadUploadPolicyPort loadPort;
    private final DeleteUploadPolicyPort deletePort;

    @Override
    public void execute(DeletePolicyCommand command) {
        // ✅ 비즈니스 규칙 검증
        UploadPolicy policy = loadPort.loadByKey(command.policyKey())
            .orElseThrow(() -> new PolicyNotFoundException(...));

        if (policy.isActive()) {  // ✅ 비즈니스 규칙
            throw new IllegalStateException("활성 정책은 삭제 불가");
        }

        // ✅ 단순 데이터 작업만 Port로 위임
        deletePort.delete(command.policyKey());
    }
}
```

#### Port Javadoc 정책

**모든 public Port 인터페이스는 클래스 레벨 Javadoc 필수:**

```java
/**
 * UploadPolicy 생성을 위한 Inbound Port (Use Case)
 *
 * <p>외부(Web Adapter 등)에서 새로운 업로드 정책을 생성할 때 사용하는 인터페이스입니다.</p>
 *
 * @author your-name
 * @since 1.0.0
 */
public interface CreateUploadPolicyUseCase {
    UploadPolicyResponse execute(CreateUploadPolicyCommand command);
}

/**
 * UploadPolicy 저장을 위한 Outbound Port
 *
 * <p>Persistence Adapter에서 구현하며, UploadPolicy 엔티티의 저장을 담당합니다.
 * 비즈니스 규칙 검증은 Application Service에서 수행됩니다.</p>
 *
 * @see UploadPolicy
 * @see CreateUploadPolicyService
 */
public interface SaveUploadPolicyPort {
    /**
     * UploadPolicy를 저장합니다.
     *
     * @param policy 저장할 정책 (null 불가)
     * @return 저장된 정책 (ID 포함)
     * @throws IllegalArgumentException policy가 null인 경우
     */
    UploadPolicy save(UploadPolicy policy);
}
```

**Javadoc 필수 항목:**
- Port의 목적과 책임 범위
- Adapter Layer 구현 위치 언급
- 비즈니스 규칙 처리 위치 명시 (Service)
- 관련 Domain 객체 참조 (`@see`)

---

### 2. Single Responsibility Principle (SRP) for UseCases

Application Layer의 UseCase는 **작고 집중된** 단일 비즈니스 유스케이스만 처리해야 합니다.

#### SRP 기준 (Application Layer)

| 메트릭 | 제한 | 근거 |
|--------|------|------|
| Public 메서드 수 | ≤ 5개 | UseCase는 하나의 작업만 수행 |
| @Transactional 메서드 수 | 1개 권장 | 여러 트랜잭션 = 여러 책임 의심 |
| 클래스 라인 수 | ≤ 150 라인 | UseCase는 작아야 함 |

**검증 방법:**
- ArchUnit 테스트: `SingleResponsibilityTest.java`

#### ❌ Bad - UseCase SRP 위반
```java
// ❌ 여러 책임을 가진 Service
@UseCase
public class OrderService {

    // ❌ 여러 @Transactional 메서드 (여러 책임)
    @Transactional
    public OrderResult createOrder(CreateOrderCommand command) {
        // 주문 생성
    }

    @Transactional
    public OrderResult updateOrder(UpdateOrderCommand command) {
        // 주문 수정
    }

    @Transactional
    public void deleteOrder(OrderId orderId) {
        // 주문 삭제
    }

    @Transactional
    public void cancelOrder(OrderId orderId) {
        // 주문 취소
    }

    // ❌ 8개 이상의 public 메서드
    public OrderResult getOrder(OrderId orderId) { }
    public List<OrderResult> listOrders() { }
    public void validateOrder(Order order) { }
    public Money calculateTotal(Order order) { }
}
```

#### ✅ Good - UseCase별 분리
```java
// ✅ 하나의 UseCase = 하나의 Service
@UseCase
@Transactional
public class CreateOrderService implements CreateOrderUseCase {
    private final LoadUserPort loadUserPort;
    private final SaveOrderPort saveOrderPort;

    // ✅ 단일 execute 메서드 (단일 책임)
    @Override
    public CreateOrderResult execute(CreateOrderCommand command) {
        User user = loadUserPort.loadById(command.userId())
            .orElseThrow(() -> new UserNotFoundException(command.userId()));

        Order order = Order.create(user.getId(), command.items());
        Order savedOrder = saveOrderPort.save(order);

        return CreateOrderResult.from(savedOrder);
    }
}

// ✅ 별도 UseCase로 분리
@UseCase
@Transactional
public class UpdateOrderService implements UpdateOrderUseCase {

    @Override
    public OrderResult execute(UpdateOrderCommand command) {
        // 주문 수정 로직만
    }
}

// ✅ 조회 전용 UseCase (readOnly = true)
@UseCase
@Transactional(readOnly = true)
public class GetOrderService implements GetOrderUseCase {

    @Override
    public GetOrderResult execute(GetOrderQuery query) {
        // 조회 로직만
    }
}
```

**여러 @Transactional 메서드의 문제점:**
- 각 트랜잭션은 독립적인 비즈니스 유스케이스
- 하나의 Service에 여러 트랜잭션 = 여러 책임
- UseCase는 단일 트랜잭션 경계를 가져야 함
- 예외: Query 메서드들 (`findAll`, `count` 등)은 허용

**권장 패턴:**
- 1 UseCase = 1 Service Class = 1 execute 메서드
- CQRS 패턴: Command/Query 분리
- 복잡한 로직은 Domain Service로 위임

---

### 3. Port 정의

#### ✅ Input Port (UseCase)
```java
package application.order.port.in;

// ✅ 인터페이스로 정의, 단일 메서드 권장
public interface CreateOrderUseCase {
    CreateOrderResult execute(CreateOrderCommand command);
}
```

#### ✅ Output Port
```java
package application.order.port.out;

// ✅ 영속성 추상화
public interface SaveOrderPort {
    Order save(Order order);
}

public interface LoadOrderPort {
    Optional<Order> loadById(OrderId orderId);
}

// ✅ 외부 시스템 추상화
public interface SendOrderEventPort {
    void send(OrderCreatedEvent event);
}
```

### 2. 트랜잭션 관리

#### ❌ Bad - Adapter에 @Transactional
```java
// ❌ 절대 금지
@Component
public class OrderPersistenceAdapter implements SaveOrderPort {
    @Transactional  // ❌ Adapter에 트랜잭션 금지!
    public Order save(Order order) { }
}
```

#### ✅ Good - Application에 @Transactional
```java
// ✅ Application UseCase에만 트랜잭션
@UseCase
@Transactional
public class CreateOrderService implements CreateOrderUseCase {
    private final LoadUserPort loadUserPort;
    private final SaveOrderPort saveOrderPort;
    private final SendOrderEventPort sendEventPort;

    public CreateOrderService(
        LoadUserPort loadUserPort,
        SaveOrderPort saveOrderPort,
        SendOrderEventPort sendEventPort
    ) {
        this.loadUserPort = loadUserPort;
        this.saveOrderPort = saveOrderPort;
        this.sendEventPort = sendEventPort;
    }

    @Override
    public CreateOrderResult execute(CreateOrderCommand command) {
        // 1. Domain 객체 로드
        User user = loadUserPort.loadById(command.userId())
            .orElseThrow(() -> new UserNotFoundException(command.userId()));

        // 2. Domain 로직 실행
        Order order = Order.create(user.getId(), command.items());

        // 3. 저장
        Order savedOrder = saveOrderPort.save(order);

        // 4. 이벤트 발행
        sendEventPort.send(new OrderCreatedEvent(savedOrder.getId()));

        return CreateOrderResult.from(savedOrder);
    }
}

// ✅ Read 전용은 readOnly = true
@UseCase
@Transactional(readOnly = true)
public class GetOrderService implements GetOrderUseCase {
    // ...
}
```

### 2-1. Transaction Boundaries with External Calls (Issue #28)

**원칙: 외부 호출과 트랜잭션 분리**

외부 API 호출은 `@Transactional` 메서드 밖에 배치해야 합니다:
- S3, SQS, SNS 등 AWS 서비스
- HTTP/REST API 호출
- Message Queue 발행
- 이메일/SMS 발송
- 외부 결제 게이트웨이

**문제점**: 외부 API 호출(평균 100-500ms)이 트랜잭션 내부에 있으면:
- DB 커넥션을 장기간 점유 (외부 API 응답 대기 중)
- 커넥션 풀 고갈 위험 (동시 요청 증가 시)
- 트랜잭션 타임아웃 가능성
- 외부 API 장애 시 DB 트랜잭션까지 실패

#### ❌ Bad - 외부 호출이 트랜잭션 내부
```java
@Service
public class UploadSessionService {

    @Transactional
    public UploadSessionWithUrlResponse createSession(Command command) {
        // 1. 정책 검증 (메모리 작업)
        validateUploadPolicy(command);

        // 2. 도메인 객체 생성 (메모리 작업)
        UploadSession session = UploadSession.create(...);

        // ❌ 3. S3 Presigned URL 발급 (외부 API - 트랜잭션 내부!)
        //    - 네트워크 I/O로 인한 지연 발생 (100-500ms)
        //    - DB 커넥션을 불필요하게 점유
        //    - S3 장애 시 DB 트랜잭션까지 실패
        PresignedUrlInfo presignedUrlInfo = generatePresignedUrlPort.generate(command);

        // 4. DB 저장 (트랜잭션 필요)
        UploadSession savedSession = uploadSessionPort.save(session);

        return new UploadSessionWithUrlResponse(savedSession, presignedUrlInfo);
    }
}
```

#### ✅ Good - 외부 호출과 DB 작업 분리
```java
@Service
public class UploadSessionService {
    private final UploadSessionPersistenceService persistenceService;

    // ✅ @Transactional 제거 - 외부 API 호출 포함
    public UploadSessionWithUrlResponse createSession(Command command) {
        // 1. 정책 검증 (메모리 작업)
        validateUploadPolicy(command);

        // 2. 도메인 객체 생성 (메모리 작업)
        UploadSession session = UploadSession.create(...);

        // ✅ 3. S3 Presigned URL 발급 (외부 API - 트랜잭션 밖!)
        //    - DB 커넥션 점유 없음
        //    - S3 장애와 DB 작업 분리
        PresignedUrlInfo presignedUrlInfo;
        try {
            presignedUrlInfo = generatePresignedUrlPort.generate(command);
        } catch (Exception e) {
            throw new PresignedUrlGenerationException(
                "Failed to generate presigned URL for session: " + session.getSessionId(),
                e
            );
        }

        // ✅ 4. DB 저장 (별도 트랜잭션 - 빠른 커밋)
        //    - persistenceService 내부에서 @Transactional 적용
        //    - 외부 API 호출 없이 빠르게 커밋 (10-50ms)
        UploadSession savedSession = persistenceService.saveSession(session);

        return new UploadSessionWithUrlResponse(savedSession, presignedUrlInfo);
    }
}

@Service
public class UploadSessionPersistenceService {

    // ✅ 외부 API 호출 없는 순수 DB 작업만 포함
    @Transactional
    public UploadSession saveSession(UploadSession session) {
        if (session == null) {
            throw new IllegalArgumentException("UploadSession must not be null");
        }
        return uploadSessionPort.save(session);
    }
}
```

**성능 개선 효과:**
- Before: 트랜잭션 시간 ~500ms (DB 50ms + S3 400ms + 여유 50ms)
- After: 트랜잭션 시간 ~50ms (DB 작업만)
- **DB 커넥션 점유 시간 90% 감소**
- 동시 100 요청 시 커넥션 풀(10개) 고갈 위험 해소

#### 외부 API 호출 식별 기준

다음 패턴들은 외부 API 호출로 간주하여 `@Transactional` 밖에 배치:

**AWS SDK 호출**
```java
s3Client.putObject(...)
s3Client.generatePresignedUrl(...)
sqsClient.sendMessage(...)
snsClient.publish(...)
```

**HTTP Client 호출**
```java
restTemplate.getForObject(...)
restTemplate.postForEntity(...)
webClient.get().retrieve()...
feign Client 호출
```

**Message Queue 발행**
```java
rabbitTemplate.convertAndSend(...)
kafkaTemplate.send(...)
```

#### 트랜잭션 분리 전략

**전략 1: 외부 호출 → DB 작업**
```java
// 외부 호출이 실패하면 DB 작업 자체를 시작하지 않음
public Result process(Command cmd) {
    ExternalResult result = externalApi.call();  // 트랜잭션 밖
    return persistenceService.save(result);      // 별도 트랜잭션
}
```

**전략 2: DB 작업 → 외부 호출 (이벤트 기반)**
```java
@Transactional
public Result process(Command cmd) {
    Entity entity = repository.save(new Entity());
    eventPublisher.publishEvent(new EntityCreated(entity.getId()));
    return Result.success();
}

// 비동기 이벤트 핸들러 (트랜잭션 밖)
@EventListener
public void handleEntityCreated(EntityCreated event) {
    externalApi.notify(event);  // 실패해도 DB 작업은 완료됨
}
```

**전략 3: 보상 트랜잭션 (Saga 패턴)**
```java
public Result process(Command cmd) {
    // 1. 외부 호출 먼저
    ExternalResult extResult = externalApi.call();

    // 2. DB 저장 (별도 트랜잭션)
    try {
        return persistenceService.save(extResult);
    } catch (Exception e) {
        // 3. 외부 작업 롤백 (보상)
        externalApi.rollback(extResult);
        throw e;
    }
}
```

#### 체크리스트
- [ ] `@Transactional` 메서드에 S3/SQS/SNS 호출 없음
- [ ] `@Transactional` 메서드에 HTTP/REST 호출 없음
- [ ] `@Transactional` 메서드에 Message Queue 발행 없음
- [ ] 외부 API 실패와 DB 트랜잭션이 독립적으로 처리됨
- [ ] DB 작업만 포함한 메서드는 별도 Service로 분리
- [ ] 트랜잭션 시간이 100ms 이내 (외부 호출 제외)

**검증 방법:**
- ArchUnit 테스트: `TransactionArchitectureTest.java`
- Pre-commit Hook: `hooks/validators/transaction-boundary-validator.sh`
- 참고: [Issue #28](https://github.com/ryu-qqq/claude-spring-standards/issues/28)

### 2-2. Spring Proxy Limitations (Issue #27)

**원칙: Spring AOP 프록시 한계 이해**

Spring은 AOP 프록시를 통해 `@Transactional`을 구현합니다:
- **JDK Dynamic Proxy**: 인터페이스 기반 (인터페이스 구현체)
- **CGLIB Proxy**: 서브클래스 기반 (구체 클래스)

**프록시가 작동하지 않는 경우:**
1. Private 메서드 (서브클래스에서 접근 불가)
2. Final 메서드 (오버라이드 불가)
3. Final 클래스 (상속 불가)
4. 같은 클래스 내부 메서드 호출 (`this.method()`)

#### 프록시 우회 시나리오

**시나리오 1: Private 메서드에 @Transactional**
```java
// ❌ 작동하지 않음
@Service
public class OrderService {

    public void processOrder(OrderCommand cmd) {
        // 이 호출은 프록시를 거치지 않음
        this.saveOrder(cmd);  // @Transactional 무시됨!
    }

    @Transactional  // ❌ Private 메서드는 프록시 불가
    private void saveOrder(OrderCommand cmd) {
        // 트랜잭션이 적용되지 않음!
        orderRepository.save(...);
    }
}
```

**시나리오 2: 내부 메서드 호출**
```java
// ❌ 작동하지 않음
@Service
public class OrderService {

    @Transactional
    public void processOrder(OrderCommand cmd) {
        try {
            orderRepository.save(...);
        } catch (Exception e) {
            // ❌ 내부 호출 - 프록시 우회!
            this.handleFailure(cmd.getId(), e.getMessage());
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void handleFailure(Long orderId, String reason) {
        // ❌ REQUIRES_NEW가 작동하지 않음
        // 새 트랜잭션이 생성되지 않고 상위 트랜잭션 사용
        failureLogRepository.save(...);
    }
}
```

#### ✅ Good - 별도 빈으로 분리

```java
// ✅ 올바른 패턴: 별도 서비스 빈
@Service
public class OrderService {
    private final OrderPersistenceService persistenceService;
    private final OrderFailureService failureService;

    public OrderService(
        OrderPersistenceService persistenceService,
        OrderFailureService failureService
    ) {
        this.persistenceService = persistenceService;
        this.failureService = failureService;
    }

    // @Transactional 없음 - 외부 API 호출 가능
    public void processOrder(OrderCommand cmd) {
        try {
            // ✅ 별도 빈 호출 - 프록시 작동
            persistenceService.saveOrder(cmd);
        } catch (Exception e) {
            // ✅ 별도 빈 호출 - REQUIRES_NEW 정상 작동
            failureService.logFailure(cmd.getId(), e.getMessage());
            throw e;
        }
    }
}

@Service
public class OrderPersistenceService {

    // ✅ Public 메서드 + 별도 빈 = 프록시 작동
    @Transactional
    public void saveOrder(OrderCommand cmd) {
        orderRepository.save(...);
    }
}

@Service
public class OrderFailureService {

    // ✅ REQUIRES_NEW가 정상 작동
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailure(Long orderId, String reason) {
        failureLogRepository.save(...);
    }
}
```

#### 프록시 작동 원리

```
// 외부에서 호출 시 (프록시를 통과)
orderService.processOrder(cmd)
    ↓
[Spring Proxy Intercepts]
    ↓
@Transactional Begin
    ↓
OrderService.processOrder()  // 실제 메서드 실행
    ↓
@Transactional Commit/Rollback

// 내부에서 호출 시 (프록시 우회)
OrderService.processOrder()
    ↓
this.saveOrder()  // 직접 호출 (프록시 없음)
    ↓
OrderService.saveOrder()  // @Transactional 무시됨!
```

#### Final 제한사항

```java
// ❌ Final 클래스 - CGLIB 프록시 불가
@Service
public final class OrderService {  // ❌ final 제거 필요

    @Transactional
    public void processOrder(OrderCommand cmd) {
        // 트랜잭션이 작동하지 않음!
    }
}

// ❌ Final 메서드 - 오버라이드 불가
@Service
public class OrderService {

    @Transactional
    public final void processOrder(OrderCommand cmd) {  // ❌ final 제거 필요
        // 트랜잭션이 작동하지 않음!
    }
}
```

#### 체크리스트
- [ ] `@Transactional`은 public 메서드에만 사용
- [ ] 같은 클래스 내부에서 `@Transactional` 메서드를 호출하지 않음
- [ ] 다른 트랜잭션 전파 속성이 필요하면 별도 빈으로 분리
- [ ] 보상 트랜잭션은 별도 서비스 클래스로 구현
- [ ] `@Transactional` 메서드와 클래스에 final 사용하지 않음

**검증 방법:**
- ArchUnit 테스트: `TransactionArchitectureTest.java`
- Pre-commit Hook: `hooks/validators/transaction-proxy-validator.sh`
- 참고: [Issue #27](https://github.com/ryu-qqq/claude-spring-standards/issues/27)

### 3. UseCase DTO

#### ✅ Command/Query/Result 패턴
```java
// ✅ Command (쓰기 작업)
public record CreateOrderCommand(
    UserId userId,
    List<OrderItem> items
) {
    public CreateOrderCommand {
        Objects.requireNonNull(userId, "User ID required");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Items required");
        }
    }
}

// ✅ Query (읽기 작업)
public record GetOrderQuery(
    OrderId orderId
) {
    public GetOrderQuery {
        Objects.requireNonNull(orderId, "Order ID required");
    }
}

// ✅ Result
public record CreateOrderResult(
    OrderId orderId,
    OrderStatus status,
    Money total,
    LocalDateTime createdAt
) {
    public static CreateOrderResult from(Order order) {
        return new CreateOrderResult(
            order.getId(),
            order.getStatus(),
            order.calculateTotal(),
            order.getCreatedAt()
        );
    }
}
```

### 4. 의존성 규칙

#### ❌ Bad
```java
// ❌ JPA Repository 직접 의존
@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;  // ❌ 구체 구현 의존
}

// ❌ JPA Entity 사용
@Service
public class OrderService {
    public OrderEntity getOrder(Long id) {  // ❌ Entity 노출
        return orderRepository.findById(id);
    }
}
```

#### ✅ Good
```java
// ✅ Port 인터페이스만 의존
@UseCase
@Transactional
public class CreateOrderService implements CreateOrderUseCase {
    private final LoadUserPort loadUserPort;      // ✅ 추상화된 Port
    private final SaveOrderPort saveOrderPort;    // ✅ 추상화된 Port

    // ✅ Domain 객체만 사용
    public CreateOrderResult execute(CreateOrderCommand command) {
        User user = loadUserPort.loadById(command.userId());
        Order order = Order.create(user.getId(), command.items());
        Order savedOrder = saveOrderPort.save(order);
        return CreateOrderResult.from(savedOrder);
    }
}
```

### 5. Test Double 작성 가이드

Port를 테스트하기 위한 Test Double(테스트 대역) 작성 패턴입니다.

#### 패턴 1: Inner Static Class (권장 - 단순한 경우)

```java
@DisplayName("CreateUploadPolicyService 단위 테스트")
class CreateUploadPolicyServiceTest {

    private CreateUploadPolicyService service;
    private TestLoadUploadPolicyPort loadPort;
    private TestSaveUploadPolicyPort savePort;

    @BeforeEach
    void setUp() {
        loadPort = new TestLoadUploadPolicyPort();
        savePort = new TestSaveUploadPolicyPort();
        service = new CreateUploadPolicyService(loadPort, savePort);
    }

    @Test
    @DisplayName("정책 생성 시 기존 정책이 없으면 성공")
    void createPolicy_WhenNoPreviousPolicy_ShouldSucceed() {
        // given
        loadPort.setPolicy(null);  // 기존 정책 없음
        CreatePolicyCommand command = new CreatePolicyCommand(...);

        // when
        PolicyResponse response = service.execute(command);

        // then
        assertThat(response).isNotNull();
        assertThat(savePort.getSavedPolicy()).isNotNull();
    }

    // ✅ Inner Static Class로 Test Double 구현
    static class TestLoadUploadPolicyPort implements LoadUploadPolicyPort {
        private UploadPolicy policy;

        void setPolicy(UploadPolicy policy) {
            this.policy = policy;
        }

        @Override
        public Optional<UploadPolicy> loadByKey(PolicyKey policyKey) {
            return Optional.ofNullable(policy);
        }
    }

    static class TestSaveUploadPolicyPort implements SaveUploadPolicyPort {
        private UploadPolicy savedPolicy;

        @Override
        public UploadPolicy save(UploadPolicy uploadPolicy) {
            this.savedPolicy = uploadPolicy;
            return uploadPolicy;
        }

        UploadPolicy getSavedPolicy() {
            return savedPolicy;
        }
    }
}
```

#### 패턴 2: 별도 Fixture Class (복잡한 경우)

```java
// test/.../fixture/UploadPolicyFixtures.java
public class UploadPolicyFixtures {

    /**
     * 여러 Port 구현을 통합한 In-Memory Test Double
     * 복잡한 상태 관리가 필요한 경우 사용
     */
    public static class InMemoryUploadPolicyPort implements
            LoadUploadPolicyPort,
            SaveUploadPolicyPort,
            UpdateUploadPolicyPort,
            DeleteUploadPolicyPort {

        private final Map<PolicyKey, UploadPolicy> storage = new HashMap<>();

        @Override
        public Optional<UploadPolicy> loadByKey(PolicyKey key) {
            return Optional.ofNullable(storage.get(key));
        }

        @Override
        public UploadPolicy save(UploadPolicy policy) {
            storage.put(policy.getPolicyKey(), policy);
            return policy;
        }

        @Override
        public UploadPolicy update(UploadPolicy policy) {
            // Application Service에서 존재 여부를 검증했다고 가정
            // Test Double은 데이터 저장/수정 작업에만 집중
            storage.put(policy.getPolicyKey(), policy);
            return policy;
        }

        @Override
        public void delete(PolicyKey key) {
            storage.remove(key);
        }

        // 테스트 편의 메서드
        public void clear() {
            storage.clear();
        }

        public int size() {
            return storage.size();
        }
    }
}

// 테스트 클래스에서 사용
@DisplayName("UploadPolicy 통합 테스트")
class UploadPolicyIntegrationTest {
    private InMemoryUploadPolicyPort policyPort;

    @BeforeEach
    void setUp() {
        policyPort = new InMemoryUploadPolicyPort();
    }

    @Test
    void multipleOperations() {
        // given
        UploadPolicy policy = createTestPolicy();

        // when
        policyPort.save(policy);
        UploadPolicy loaded = policyPort.loadByKey(policy.getPolicyKey()).orElseThrow();
        policyPort.delete(policy.getPolicyKey());

        // then
        assertThat(loaded).isEqualTo(policy);
        assertThat(policyPort.size()).isZero();
    }
}
```

#### 패턴 선택 기준

| 상황 | 권장 패턴 | 이유 |
|------|-----------|------|
| 단일 테스트 클래스에서만 사용 | Inner Static Class | 응집도 높음, 간단한 로직 |
| 여러 테스트 클래스에서 공유 | 별도 Fixture Class | 재사용성, 일관성 |
| 간단한 상태 관리 | Inner Static Class | 불필요한 복잡도 방지 |
| 복잡한 상태 관리 (CRUD) | 별도 Fixture Class | 상태 관리 로직 집중화 |
| Port 1-2개 | Inner Static Class | 코드 간결성 |
| Port 3개 이상 | 별도 Fixture Class | 통합 관리 용이 |

#### ❌ Mockito 사용 지양

```java
// ❌ 가능하면 피할 것
@Test
void shouldCreateOrder() {
    LoadOrderPort loadPort = Mockito.mock(LoadOrderPort.class);
    Mockito.when(loadPort.loadById(any())).thenReturn(Optional.of(order));
    // ...
}
```

**이유:**
- Mockito는 구현 세부사항에 의존하게 만듦
- 진짜 객체(Test Double)가 더 신뢰성 높음
- 리팩토링 시 테스트가 깨지기 쉬움

**예외적으로 허용:**
- 외부 시스템 연동 Port (AWS, 결제 게이트웨이 등)
- 복잡한 설정이 필요한 Port
- 테스트 대역 작성이 과도하게 복잡한 경우

---

## 💾 Persistence Adapter 규칙

### 1. JPA Entity 설계

#### ❌ Bad - 연관관계 사용
```java
// ❌ JPA 연관관계 절대 금지
@Entity
public class OrderEntity {
    @Id
    private Long id;

    @ManyToOne  // ❌ 금지!
    private UserEntity user;

    @OneToMany  // ❌ 금지!
    private List<OrderItemEntity> items;

    public void setStatus(String status) {  // ❌ Setter 금지!
        this.status = status;
    }
}
```

#### ✅ Good - 외래키만 사용
```java
@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ 외래키는 Long 타입 필드로만
    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ✅ JPA 전용 기본 생성자 (protected)
    protected OrderEntity() {}

    // ✅ Private 생성자
    private OrderEntity(Long userId, OrderStatus status, BigDecimal totalAmount) {
        this.userId = userId;
        this.status = status;
        this.totalAmount = totalAmount;
    }

    // ✅ 정적 팩토리 - 새 엔티티 생성
    public static OrderEntity create(Long userId, OrderStatus status, BigDecimal totalAmount) {
        return new OrderEntity(userId, status, totalAmount);
    }

    // ✅ 정적 팩토리 - DB에서 복원
    public static OrderEntity reconstitute(Long id, Long userId, OrderStatus status, BigDecimal totalAmount, LocalDateTime createdAt) {
        OrderEntity entity = new OrderEntity(userId, status, totalAmount);
        entity.id = id;
        entity.createdAt = createdAt;
        return entity;
    }

    // ✅ Getter만 (Setter 금지)
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

#### Long FK 전략 (Foreign Key Strategy)

**왜 JPA 관계 어노테이션을 금지하는가?**

JPA 관계 어노테이션은 여러 문제를 야기하므로 **절대 사용 금지**합니다:

| 문제점 | 설명 | 영향 |
|--------|------|------|
| **Law of Demeter 위반** | 연관 Entity를 직접 탐색 (`order.getUser().getName()`) | Getter 체이닝 발생, 캡슐화 위반 |
| **N+1 쿼리 문제** | 연관 Entity 로딩 시 추가 쿼리 발생 | 성능 저하, 예측 불가능한 쿼리 |
| **양방향 참조 복잡도** | `mappedBy`, cascade, orphanRemoval 관리 | 순환 참조, 예상치 못한 삭제 |
| **영속성 컨텍스트 의존** | JPA 세션 외부에서 LazyInitializationException | 레이어 경계 침범 |
| **테스트 어려움** | Entity 그래프 전체를 준비해야 함 | Mock 복잡도 증가 |

**Long FK 전략의 장점:**

1. **Law of Demeter 준수**: Entity 간 직접 참조 없음 → Repository SRP 자연스럽게 달성
2. **명시적 데이터 로딩**: 필요한 데이터만 Application Layer에서 명시적으로 로드
3. **성능 예측 가능**: 쿼리가 명확하고 최적화 용이
4. **테스트 단순화**: Entity를 독립적으로 테스트 가능
5. **레이어 분리 강화**: Persistence가 Domain 구조를 오염시키지 않음

#### Long FK 사용 패턴

**패턴 1: 1:N 관계 (One-to-Many)**

```java
// ❌ JPA 관계 어노테이션 사용 (금지!)
@Entity
public class OrderEntity {
    @Id
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> items;  // ❌ 절대 금지!
}

// ✅ Long FK 전략
@Entity
public class OrderEntity {
    @Id
    private Long id;
    // OrderItem은 별도 조회 (Application Layer에서 처리)
}

@Entity
public class OrderItemEntity {
    @Id
    private Long id;

    // ✅ Long FK로만 관계 표현
    @Column(nullable = false)
    private Long orderId;  // Order와의 관계
}

// ✅ Application Layer에서 명시적 조합
@UseCase
@Transactional(readOnly = true)
public class GetOrderWithItemsService {
    private final LoadOrderPort loadOrderPort;
    private final LoadOrderItemsPort loadOrderItemsPort;

    public OrderWithItemsResult execute(OrderId orderId) {
        // 1. Order 조회
        Order order = loadOrderPort.loadById(orderId).orElseThrow();

        // 2. OrderItem 조회
        List<OrderItem> items = loadOrderItemsPort.loadByOrderId(orderId);

        // 3. 조합
        return OrderWithItemsResult.of(order, items);
    }
}
```

**패턴 2: N:1 관계 (Many-to-One)**

```java
// ❌ JPA 관계 어노테이션 사용 (금지!)
@Entity
public class OrderEntity {
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;  // ❌ 절대 금지!
}

// ✅ Long FK 전략
@Entity
public class OrderEntity {
    @Id
    private Long id;

    // ✅ Long FK로만 관계 표현
    @Column(nullable = false)
    private Long userId;
}

// ✅ Application Layer에서 명시적 로드
@UseCase
@Transactional(readOnly = true)
public class GetOrderWithUserService {
    private final LoadOrderPort loadOrderPort;
    private final LoadUserPort loadUserPort;

    public OrderWithUserResult execute(OrderId orderId) {
        // 1. Order 조회
        Order order = loadOrderPort.loadById(orderId).orElseThrow();

        // 2. User 조회 (필요한 경우만)
        User user = loadUserPort.loadById(order.getUserId()).orElseThrow();

        // 3. 조합
        return OrderWithUserResult.of(order, user);
    }
}
```

**패턴 3: N:M 관계 (Many-to-Many)**

```java
// ❌ JPA 관계 어노테이션 사용 (금지!)
@Entity
public class OrderEntity {
    @Id
    private Long id;

    @ManyToMany
    @JoinTable(name = "order_products")
    private List<ProductEntity> products;  // ❌ 절대 금지!
}

// ✅ Long FK 전략 + 명시적 중간 테이블
@Entity
@Table(name = "order_products")
public class OrderProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Long FK로만 관계 표현
    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;
}

// ✅ Application Layer에서 명시적 조합
@UseCase
@Transactional(readOnly = true)
public class GetOrderWithProductsService {
    private final LoadOrderPort loadOrderPort;
    private final LoadOrderProductsPort loadOrderProductsPort;
    private final LoadProductPort loadProductPort;

    public OrderWithProductsResult execute(OrderId orderId) {
        // 1. Order 조회
        Order order = loadOrderPort.loadById(orderId).orElseThrow();

        // 2. OrderProduct (중간 테이블) 조회
        List<OrderProduct> orderProducts = loadOrderProductsPort.loadByOrderId(orderId);

        // 3. Product 조회
        List<ProductId> productIds = orderProducts.stream()
            .map(OrderProduct::getProductId)
            .toList();
        List<Product> products = loadProductPort.loadByIds(productIds);

        // 4. 조합
        return OrderWithProductsResult.of(order, orderProducts, products);
    }
}
```

**핵심 원칙:**
- JPA 연관관계 어노테이션 **절대 사용 금지**
- Entity 간 참조는 **Long FK 필드**로만 표현
- 데이터 조합은 **Application Layer**에서 명시적으로 처리
- Repository는 **단일 Entity**만 담당 (SRP 준수)

### 2. Entity ↔ Domain 매핑

#### ✅ Mapper 클래스 사용
```java
@Component
class OrderEntityMapper {

    // ✅ Entity → Domain
    public Order toDomain(OrderEntity entity) {
        return Order.reconstitute(
            OrderId.of(entity.getId()),
            UserId.of(entity.getUserId()),
            entity.getStatus(),
            Money.of(entity.getTotalAmount()),
            entity.getCreatedAt()
        );
    }

    // ✅ Domain → Entity
    public OrderEntity toEntity(Order domain) {
        if (domain.getId() == null) {
            // 신규 생성
            return OrderEntity.create(
                domain.getUserId().value(),
                domain.getStatus(),
                domain.getTotal().amount()
            );
        } else {
            // 기존 엔티티 복원
            return OrderEntity.reconstitute(
                domain.getId().value(),
                domain.getUserId().value(),
                domain.getStatus(),
                domain.getTotal().amount(),
                domain.getCreatedAt()
            );
        }
    }
}
```

### 3. Repository 구현

#### ✅ Package-Private JpaRepository
```java
// ✅ package-private (외부 노출 금지)
interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {
    // QueryDSL 사용 권장
}

// ✅ Port 구현 클래스만 public
@Component
public class OrderPersistenceAdapter implements SaveOrderPort, LoadOrderPort {

    private final OrderJpaRepository jpaRepository;
    private final OrderEntityMapper mapper;

    public OrderPersistenceAdapter(
        OrderJpaRepository jpaRepository,
        OrderEntityMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Order save(Order order) {
        OrderEntity entity = mapper.toEntity(order);
        OrderEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Order> loadById(OrderId orderId) {
        return jpaRepository.findById(orderId.value())
            .map(mapper::toDomain);
    }
}
```

### 4. QueryDSL 사용

#### ❌ Bad - JPQL 문자열
```java
// ❌ 문자열 쿼리 금지 (타입 안전성 없음)
@Query("SELECT o FROM OrderEntity o WHERE o.userId = :userId")
List<OrderEntity> findByUserId(@Param("userId") Long userId);
```

#### ✅ Good - QueryDSL
```java
@Repository
public class OrderQueryRepository {
    private final JPAQueryFactory queryFactory;

    public List<Order> findByUserId(UserId userId) {
        QOrderEntity order = QOrderEntity.orderEntity;

        return queryFactory
            .selectFrom(order)
            .where(order.userId.eq(userId.value()))
            .fetch()
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
}
```

### 5. 예외 처리

#### ✅ JPA 예외 → Domain 예외 변환
```java
@Component
public class OrderPersistenceAdapter implements LoadOrderPort {

    @Override
    public Order loadById(OrderId orderId) {
        try {
            return jpaRepository.findById(orderId.value())
                .map(mapper::toDomain)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        } catch (DataAccessException e) {
            throw new PersistenceException("Failed to load order", e);
        }
    }
}
```

### 6. Single Responsibility Principle (SRP) for Repositories

Repository는 **단일 Entity**에만 집중해야 하며, 여러 Entity에 의존하는 것은 여러 책임을 가진 것으로 간주됩니다.

#### SRP 원칙 (Repository Layer)

| 원칙 | 설명 | 검증 방법 |
|------|------|----------|
| 단일 Entity 의존 | Repository는 하나의 Entity만 다뤄야 함 | ArchUnit 테스트 |
| Entity 필드 개수 | Entity 타입 필드 ≤ 1개 | 정적 분석 |

**검증 방법:**
- ArchUnit 테스트: `SingleResponsibilityTest.java`
- `haveSingleEntityDependency()` 조건 검사

#### ❌ Bad - 여러 Entity 의존 (여러 책임)

```java
// ❌ 여러 Entity를 필드로 가진 Repository
@Component
public class OrderPersistenceAdapter implements SaveOrderPort, LoadOrderPort {

    private final OrderJpaRepository orderRepository;

    // ❌ 여러 Entity Repository 의존
    private final UserJpaRepository userRepository;
    private final ProductJpaRepository productRepository;
    private final PaymentJpaRepository paymentRepository;

    private final OrderEntityMapper orderMapper;
    private final UserEntityMapper userMapper;
    private final ProductEntityMapper productMapper;

    @Override
    public Order save(Order order) {
        // ❌ 여러 Entity를 함께 다룸
        UserEntity user = userRepository.findById(order.getUserId().value()).orElseThrow();
        ProductEntity product = productRepository.findById(order.getProductId().value()).orElseThrow();
        PaymentEntity payment = paymentRepository.findById(order.getPaymentId().value()).orElseThrow();

        // Order 저장 로직
        OrderEntity orderEntity = orderMapper.toEntity(order);
        return orderMapper.toDomain(orderRepository.save(orderEntity));
    }
}
```

**문제점:**
- 하나의 Repository가 Order, User, Product, Payment Entity를 모두 다룸
- 여러 Entity 의존 = 여러 데이터 책임
- SRP 위반: Repository가 단일 Entity 관리 책임을 초과

#### ✅ Good - 단일 Entity만 관리

```java
// ✅ Order Entity만 담당
@Component
public class OrderPersistenceAdapter implements SaveOrderPort, LoadOrderPort {

    private final OrderJpaRepository orderRepository;
    private final OrderEntityMapper orderMapper;

    public OrderPersistenceAdapter(
        OrderJpaRepository orderRepository,
        OrderEntityMapper orderMapper
    ) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    @Override
    public Order save(Order order) {
        // ✅ Order Entity만 다룸
        OrderEntity orderEntity = orderMapper.toEntity(order);
        OrderEntity savedEntity = orderRepository.save(orderEntity);
        return orderMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Order> loadById(OrderId orderId) {
        // ✅ Order Entity만 조회
        return orderRepository.findById(orderId.value())
            .map(orderMapper::toDomain);
    }
}

// ✅ User Entity는 별도 Adapter
@Component
public class UserPersistenceAdapter implements LoadUserPort {

    private final UserJpaRepository userRepository;
    private final UserEntityMapper userMapper;

    @Override
    public Optional<User> loadById(UserId userId) {
        return userRepository.findById(userId.value())
            .map(userMapper::toDomain);
    }
}

// ✅ Product Entity는 별도 Adapter
@Component
public class ProductPersistenceAdapter implements LoadProductPort {

    private final ProductJpaRepository productRepository;
    private final ProductEntityMapper productMapper;

    @Override
    public Optional<Product> loadById(ProductId productId) {
        return productRepository.findById(productId.value())
            .map(productMapper::toDomain);
    }
}
```

#### Long FK 전략과 SRP의 관계

**Long FK 전략**을 사용하면 Repository SRP가 자연스럽게 지켜집니다:

```java
// ✅ Long FK 사용 → 단일 Entity만 의존
@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    private Long id;

    // ✅ Long FK만 사용 (JPA 관계 어노테이션 없음)
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Long paymentId;
}

// ✅ Repository는 OrderEntity만 다룸
@Component
public class OrderPersistenceAdapter implements SaveOrderPort {
    private final OrderJpaRepository orderRepository;

    @Override
    public Order save(Order order) {
        // ✅ OrderEntity만 저장 (관련 Entity는 Application Layer에서 처리)
        OrderEntity entity = OrderEntity.create(
            order.getUserId().value(),  // Long FK
            order.getProductId().value(),  // Long FK
            order.getPaymentId().value()   // Long FK
        );
        return orderMapper.toDomain(orderRepository.save(entity));
    }
}
```

**분리 기준:**
- 1 Repository = 1 Entity
- 다른 Entity 정보가 필요하면 Application Layer에서 여러 Port 조합
- Repository는 순수 데이터 작업만 (비즈니스 로직 없음)

---

## 🌐 Controller Adapter (Adapter-In-Web) 규칙

### 1. Controller 구조

#### ❌ Bad
```java
// ❌ 내부 클래스 금지
@RestController
public class OrderController {

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        // ...
    }

    // ❌ 내부 클래스 금지!
    public static class OrderRequest {
        public Long userId;
        public List<String> items;
    }

    // ❌ 내부 클래스 금지!
    public static class OrderResponse {
        public Long orderId;
        public String status;
    }
}
```

#### ✅ Good
```java
// ✅ Controller는 얇게 유지
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;

    // ✅ Constructor Injection
    public OrderController(
        CreateOrderUseCase createOrderUseCase,
        GetOrderUseCase getOrderUseCase
    ) {
        this.createOrderUseCase = createOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOrderResponse createOrder(
        @Valid @RequestBody CreateOrderRequest request
    ) {
        CreateOrderCommand command = request.toCommand();
        CreateOrderResult result = createOrderUseCase.execute(command);
        return CreateOrderResponse.from(result);
    }

    @GetMapping("/{orderId}")
    public GetOrderResponse getOrder(@PathVariable Long orderId) {
        GetOrderQuery query = new GetOrderQuery(OrderId.of(orderId));
        GetOrderResult result = getOrderUseCase.execute(query);
        return GetOrderResponse.from(result);
    }
}
```

### 2. Request/Response DTO

#### ✅ Record 필수
```java
// ✅ 별도 파일: CreateOrderRequest.java
public record CreateOrderRequest(
    @NotNull(message = "User ID is required")
    Long userId,

    @NotEmpty(message = "Items cannot be empty")
    @Valid
    List<OrderItemRequest> items
) {
    // ✅ Compact Constructor에서 추가 검증
    public CreateOrderRequest {
        if (userId != null && userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
    }

    // ✅ Domain Command로 변환
    public CreateOrderCommand toCommand() {
        return new CreateOrderCommand(
            UserId.of(userId),
            items.stream()
                .map(OrderItemRequest::toDomain)
                .toList()
        );
    }
}

// ✅ 별도 파일: CreateOrderResponse.java
public record CreateOrderResponse(
    Long orderId,
    String status,
    BigDecimal totalAmount,
    LocalDateTime createdAt
) {
    public static CreateOrderResponse from(CreateOrderResult result) {
        return new CreateOrderResponse(
            result.orderId().value(),
            result.status().name(),
            result.total().amount(),
            result.createdAt()
        );
    }
}
```

### 3. 전역 예외 처리

#### ✅ @RestControllerAdvice
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleOrderNotFound(OrderNotFoundException e) {
        return new ErrorResponse("ORDER_NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(InvalidOrderStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidOrderState(InvalidOrderStateException e) {
        return new ErrorResponse("INVALID_ORDER_STATE", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationErrors(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        return new ErrorResponse("VALIDATION_ERROR", message);
    }
}

public record ErrorResponse(
    String code,
    String message,
    LocalDateTime timestamp
) {
    public ErrorResponse(String code, String message) {
        this(code, message, LocalDateTime.now());
    }
}
```

### 4. Single Responsibility Principle (SRP) for Controllers

Controller는 **단일 리소스**에 집중하며, 엔드포인트 개수를 제한해야 합니다.

#### SRP 기준 (Controller Layer)

| 메트릭 | 제한 | 근거 |
|--------|------|------|
| Public 엔드포인트 수 | ≤ 10개 | 많은 엔드포인트 = 여러 리소스 혼재 |
| 의존 UseCase 수 | ≤ 10개 | 여러 UseCase = 여러 책임 |

**검증 방법:**
- ArchUnit 테스트: `SingleResponsibilityTest.java`

#### ❌ Bad - 여러 리소스를 하나의 Controller에서 처리

```java
// ❌ 너무 많은 엔드포인트 (여러 리소스 혼재)
@RestController
@RequestMapping("/api/v1")
public class ApiController {

    // ❌ Order 관련
    @PostMapping("/orders")
    public OrderResponse createOrder(@RequestBody OrderRequest request) { }

    @GetMapping("/orders/{id}")
    public OrderResponse getOrder(@PathVariable Long id) { }

    @PutMapping("/orders/{id}")
    public OrderResponse updateOrder(@PathVariable Long id) { }

    @DeleteMapping("/orders/{id}")
    public void deleteOrder(@PathVariable Long id) { }

    // ❌ User 관련
    @PostMapping("/users")
    public UserResponse createUser(@RequestBody UserRequest request) { }

    @GetMapping("/users/{id}")
    public UserResponse getUser(@PathVariable Long id) { }

    // ❌ Product 관련
    @PostMapping("/products")
    public ProductResponse createProduct(@RequestBody ProductRequest request) { }

    @GetMapping("/products/{id}")
    public ProductResponse getProduct(@PathVariable Long id) { }

    // ❌ Payment 관련
    @PostMapping("/payments")
    public PaymentResponse processPayment(@RequestBody PaymentRequest request) { }

    @GetMapping("/payments/{id}")
    public PaymentResponse getPayment(@PathVariable Long id) { }

    // ❌ Shipping 관련
    @PostMapping("/shipping")
    public ShippingResponse createShipping(@RequestBody ShippingRequest request) { }

    @GetMapping("/shipping/{id}")
    public ShippingResponse getShipping(@PathVariable Long id) { }

    // 총 12개 엔드포인트 (SRP 위반!)
}
```

#### ✅ Good - 리소스별 Controller 분리

```java
// ✅ Order 리소스만 담당
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final UpdateOrderUseCase updateOrderUseCase;
    private final DeleteOrderUseCase deleteOrderUseCase;
    private final ListOrdersUseCase listOrdersUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;

    // ✅ Order 관련 엔드포인트만 (6개)
    @PostMapping
    public OrderResponse createOrder(@RequestBody CreateOrderRequest request) { }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id) { }

    @GetMapping
    public List<OrderResponse> listOrders(@RequestParam Map<String, String> params) { }

    @PutMapping("/{id}")
    public OrderResponse updateOrder(@PathVariable Long id, @RequestBody UpdateOrderRequest request) { }

    @PostMapping("/{id}/cancel")
    public void cancelOrder(@PathVariable Long id) { }

    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable Long id) { }
}

// ✅ User 리소스는 별도 Controller
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    // User 관련 엔드포인트만
}

// ✅ Product 리소스는 별도 Controller
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    // Product 관련 엔드포인트만
}
```

**리소스별 분리 원칙:**
- 1 Controller = 1 REST Resource
- 엔드포인트 수가 10개를 초과하면 리소스를 세분화
- 예: `OrderController` → `OrderController` + `OrderItemController`
- CRUD + 커스텀 액션을 포함해도 10개 이하 유지

---

## ☁️ External System Adapter 규칙

### 1. AWS S3 Adapter 예제

#### ✅ Good
```java
@Component
public class S3FileStorageAdapter implements FileStoragePort {

    private final S3Client s3Client;
    private final S3Properties properties;

    public S3FileStorageAdapter(S3Client s3Client, S3Properties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
    }

    @Override
    public FileUrl upload(FileData fileData) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(fileData.path())
                .contentType(fileData.contentType())
                .build();

            s3Client.putObject(request, fileData.content());

            String url = buildUrl(fileData.path());
            return FileUrl.of(url);

        } catch (S3Exception e) {
            throw new FileStorageException("Failed to upload file", e);
        }
    }

    private String buildUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
            properties.bucket(),
            properties.region(),
            key
        );
    }
}

// ✅ Configuration Properties
@ConfigurationProperties(prefix = "aws.s3")
public record S3Properties(
    @NotBlank String bucket,
    @NotBlank String region
) {
    public S3Properties {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalArgumentException("S3 bucket is required");
        }
        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("S3 region is required");
        }
    }
}
```

---

## 🔧 공통 규칙

### 1. 패키지 구조 (Aggregate별 수직 슬라이싱)

**모든 레이어에 Aggregate별 서브패키지를 일관되게 적용합니다.**
자세한 내용은 [DDD_AGGREGATE_MIGRATION_GUIDE.md](DDD_AGGREGATE_MIGRATION_GUIDE.md)를 참조하세요.

```
com.example.project
├── domain/
│   ├── order/                      # Order Aggregate
│   │   ├── Order.java              # Aggregate Root
│   │   ├── OrderId.java            # Value Object
│   │   ├── OrderItem.java          # Entity
│   │   ├── OrderStatus.java        # Enum
│   │   ├── vo/                     # Value Objects
│   │   │   ├── Money.java
│   │   │   └── Quantity.java
│   │   ├── event/                  # Domain Events
│   │   │   ├── OrderPlacedEvent.java
│   │   │   └── OrderCancelledEvent.java
│   │   ├── exception/              # Domain Exceptions
│   │   │   └── InvalidOrderException.java
│   │   └── service/                # Domain Services
│   │       └── OrderDomainService.java
│   └── user/                       # User Aggregate
│       ├── User.java
│       ├── UserId.java
│       └── vo/
│
├── application/
│   ├── order/                      # Order Aggregate
│   │   ├── dto/
│   │   │   ├── CreateOrderCommand.java
│   │   │   └── CreateOrderResult.java
│   │   ├── port/
│   │   │   ├── in/
│   │   │   │   ├── CreateOrderUseCase.java
│   │   │   │   └── GetOrderUseCase.java
│   │   │   └── out/
│   │   │       ├── LoadOrderPort.java
│   │   │       ├── SaveOrderPort.java
│   │   │       └── SendOrderEventPort.java
│   │   └── service/
│   │       ├── CreateOrderService.java
│   │       └── GetOrderService.java
│   └── user/                       # User Aggregate
│       ├── dto/
│       ├── port/
│       └── service/
│
├── adapter/
│   ├── in/web/
│   │   ├── order/                  # Order Aggregate
│   │   │   ├── controller/
│   │   │   │   └── OrderController.java
│   │   │   ├── request/
│   │   │   │   └── CreateOrderRequest.java
│   │   │   └── response/
│   │   │       └── CreateOrderResponse.java
│   │   ├── user/                   # User Aggregate
│   │   └── common/                 # 공통 컴포넌트
│   │       └── GlobalExceptionHandler.java
│   └── out/
│       ├── persistence/
│       │   ├── order/              # Order Aggregate
│       │   │   ├── entity/
│       │   │   │   └── OrderEntity.java
│       │   │   ├── repository/
│       │   │   │   └── OrderJpaRepository.java
│       │   │   ├── mapper/
│       │   │   │   └── OrderEntityMapper.java
│       │   │   ├── OrderPersistenceAdapter.java
│       │   │   └── OrderQueryRepository.java
│       │   └── user/               # User Aggregate
│       ├── aws/
│       │   ├── s3/
│       │   │   ├── S3FileStorageAdapter.java
│       │   │   └── S3Properties.java
│       │   └── sqs/
│       │       └── SqsEventPublisher.java
│       └── external/
│           └── payment/
│               └── PaymentGatewayAdapter.java
│
└── bootstrap/
    └── config/
        ├── Application.java
        ├── JpaConfig.java
        ├── SecurityConfig.java
        └── AwsConfig.java
```

**패키지 구조 원칙:**
1. **모든 레이어 일관성**: Domain, Application, Adapter 모두 동일한 Aggregate 기준 적용
2. **비즈니스 경계 명확화**: Aggregate 단위로 코드가 그룹화되어 도메인 경계 가시화
3. **확장성**: 새 Aggregate 추가 시 명확한 위치 파악 가능
4. **응집도**: 관련 코드가 한 Aggregate 디렉토리 내에 모임
5. **공통 컴포넌트**: `common/` 디렉토리에 별도 배치

### 2. 명명 규칙

| 유형 | 패턴 | 예제 |
|------|------|------|
| Domain Entity | `{명사}` | `Order`, `User`, `Product` |
| Value Object | `{명사}` | `OrderId`, `Money`, `Email` |
| UseCase Interface | `{동사}{명사}UseCase` | `CreateOrderUseCase`, `GetUserUseCase` |
| UseCase Impl | `{동사}{명사}Service` | `CreateOrderService`, `GetUserService` |
| Input Port | `{동사}{명사}UseCase` | `CreateOrderUseCase` |
| Output Port | `{동사}{명사}Port` | `LoadOrderPort`, `SaveOrderPort` |
| Adapter | `{시스템}{기능}Adapter` | `OrderPersistenceAdapter`, `S3FileStorageAdapter` |
| Controller | `{리소스}Controller` | `OrderController`, `UserController` |
| JPA Entity | `{명사}Entity` | `OrderEntity`, `UserEntity` |
| DTO | `{동작}{리소스}Request/Response` | `CreateOrderRequest`, `GetOrderResponse` |

### 3. Annotation 규칙

| 레이어 | 허용 Annotations | 금지 Annotations |
|--------|------------------|------------------|
| **Domain** | 없음 (순수 Java) | `@Component`, `@Service`, `@Entity`, `@Data` 등 모든 프레임워크 애노테이션 |
| **Application** | `@UseCase`, `@Transactional` | `@Component`, `@Service`, `@Repository` |
| **Adapter** | `@Component`, `@RestController`, `@Repository` | `@Transactional` (Application에서만) |
| **DTO** | `@NotNull`, `@Valid`, `@Email` (Bean Validation만) | `@Data`, `@Builder` (Lombok 금지) |

### 4. 의존성 주입

#### ❌ Bad
```java
// ❌ Field Injection 금지
@Service
public class OrderService {
    @Autowired
    private OrderRepository repository;
}

// ❌ Setter Injection 금지
@Service
public class OrderService {
    private OrderRepository repository;

    @Autowired
    public void setRepository(OrderRepository repository) {
        this.repository = repository;
    }
}
```

#### ✅ Good
```java
// ✅ Constructor Injection만 사용
@UseCase
@Transactional
public class CreateOrderService implements CreateOrderUseCase {
    private final LoadUserPort loadUserPort;
    private final SaveOrderPort saveOrderPort;

    // ✅ 단일 생성자는 @Autowired 생략
    public CreateOrderService(
        LoadUserPort loadUserPort,
        SaveOrderPort saveOrderPort
    ) {
        this.loadUserPort = loadUserPort;
        this.saveOrderPort = saveOrderPort;
    }
}
```

### 5. Lombok 정책

#### ❌ 전 프로젝트 완전 금지
```java
// ❌ 모든 Lombok 애노테이션 금지
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
```

#### ✅ 수동 작성
```java
// ✅ Getter 수동 작성
public class Order {
    private final OrderId id;

    public OrderId getId() {
        return id;
    }
}

// ✅ Builder 대신 정적 팩토리
public static Order create(OrderId id, List<OrderItem> items) {
    return new Order(id, OrderStatus.PENDING, items);
}
```

### 6. String Case Conversion

**문제**: `String.toLowerCase()` 및 `toUpperCase()`는 기본 Locale에 의존하여 예상치 못한 결과를 초래할 수 있습니다.

**대표적 예시 - Turkish Locale 문제**:
```java
// Turkish Locale 환경에서 실행 시
String input = "IMAGE";
String normalized = input.toLowerCase(); // "ımage" (소문자 i가 다름!)

// 검증 실패 가능성
Set<String> allowed = Set.of("image", "jpeg", "png");
allowed.contains(normalized); // false! 보안 우회 위험
```

#### ❌ Bad
```java
// ❌ Locale 미지정 (기본 Locale 사용)
String format = "IMAGE";
String normalized = format.toLowerCase();

// ❌ HTTP Header 처리
String contentType = header.toUpperCase();
```

#### ✅ Good
```java
import java.util.Locale;

// ✅ 내부 처리용 (비교, 검증) - Locale.ROOT 사용
String normalized = format.toLowerCase(Locale.ROOT).trim();

// ✅ 사용자 표시용 - Locale.getDefault() 사용
String displayText = text.toLowerCase(Locale.getDefault());

// ✅ 프로토콜/표준 (HTTP, SQL) - Locale.ENGLISH 사용
String httpMethod = method.toUpperCase(Locale.ENGLISH);
```

#### 사용 케이스별 가이드

| 사용 목적 | 권장 Locale | 이유 |
|-----------|-------------|------|
| 내부 처리 (비교, 검증) | `Locale.ROOT` | Locale 독립적 동작 보장 |
| 사용자 표시용 | `Locale.getDefault()` | 사용자 언어 환경 반영 |
| 프로토콜/표준 (HTTP, SQL) | `Locale.ENGLISH` | 국제 표준 준수 |

#### 실제 예시

```java
// ✅ Domain: Value Object 검증
public class ImageFormat {
    private static final Set<String> ALLOWED_FORMATS =
        Set.of("image", "jpeg", "png", "gif");

    private final String value;

    private ImageFormat(String value) {
        java.util.Objects.requireNonNull(value, "Image format value cannot be null");
        // ✅ Locale.ROOT로 일관된 검증
        String normalized = value.toLowerCase(java.util.Locale.ROOT).trim();
        if (!ALLOWED_FORMATS.contains(normalized)) {
            throw new IllegalArgumentException("Invalid format: " + value);
        }
        this.value = normalized;
    }

    public static ImageFormat of(String value) {
        return new ImageFormat(value);
    }
}

// ✅ Adapter: HTTP Header 처리
@RestController
public class FileController {
    @PostMapping("/files")
    public ResponseEntity<?> uploadFile(@RequestHeader("Content-Type") String contentType) {
        // ✅ Locale.ENGLISH로 표준 프로토콜 처리
        String normalized = contentType.toLowerCase(Locale.ENGLISH);

        if (normalized.startsWith("image/")) {
            // 이미지 처리
        }
        return ResponseEntity.ok().build();
    }
}
```

**SpotBugs 경고**: `reportLevel = LOW` 설정 시 `DM_CONVERT_CASE` 경고 발생
- **해결**: 항상 명시적으로 Locale 지정
- **참고**: [Turkish i18n Issue](https://haacked.com/archive/2012/07/05/turkish-i-problem-and-why-you-should-care.aspx/)

---

## 🔒 금지 사항 종합

### Domain Layer
- ❌ Spring Framework 의존성
- ❌ JPA/Hibernate 의존성
- ❌ Lombok
- ❌ 인프라 라이브러리
- ❌ Setter 메서드
- ❌ Public 생성자

### Application Layer
- ❌ Adapter 구체 클래스 의존
- ❌ JPA Entity 사용
- ❌ Repository 인터페이스 직접 의존
- ❌ HTTP, AWS SDK 등 인프라 라이브러리

### Adapter Layer
- ❌ `@Transactional` (Application에서만)
- ❌ 다른 Adapter 의존
- ❌ Domain 객체 외부 노출
- ❌ 비즈니스 로직

### Persistence Adapter
- ❌ JPA 연관관계 (`@OneToMany`, `@ManyToOne`, `@OneToOne`, `@ManyToMany`)
- ❌ Entity에 Setter
- ❌ Entity에 Public 생성자
- ❌ Entity에 비즈니스 로직

### Controller Adapter
- ❌ 내부 클래스
- ❌ Domain 객체 직접 반환
- ❌ Repository/Entity 직접 의존

### 전체 프로젝트
- ❌ Lombok 사용
- ❌ Field Injection
- ❌ Setter Injection
- ❌ 순환 의존성

---

## ✅ 체크리스트

### Domain Layer
- [ ] 모든 필드는 `private final`인가?
- [ ] Setter 메서드가 없는가?
- [ ] Public 생성자가 없는가?
- [ ] 정적 팩토리 메서드를 사용하는가?
- [ ] 비즈니스 로직이 Domain 객체 내부에 있는가?
- [ ] Spring, JPA, Lombok 의존성이 없는가?

### Application Layer
- [ ] `@Transactional`이 UseCase 구현체에 있는가?
- [ ] Port 인터페이스만 의존하는가?
- [ ] Domain 객체만 사용하는가?
- [ ] UseCase별 Command/Query/Result DTO를 정의했는가?

### Persistence Adapter
- [ ] JPA 연관관계 애노테이션이 없는가?
- [ ] 외래키가 Long 타입 필드인가?
- [ ] Entity에 Setter가 없는가?
- [ ] Entity에 Public 생성자가 없는가?
- [ ] Mapper 클래스를 사용하는가?
- [ ] `@Transactional`이 없는가?

### Controller Adapter
- [ ] 내부 클래스가 없는가?
- [ ] Request/Response가 record 타입인가?
- [ ] Record 생성자에 validation이 있는가?
- [ ] Domain 객체를 직접 반환하지 않는가?
- [ ] UseCase(Port)만 의존하는가?

### 공통
- [ ] Lombok을 사용하지 않는가?
- [ ] Constructor Injection을 사용하는가?
- [ ] 순환 의존성이 없는가?
- [ ] 레이어 의존성 방향을 준수하는가?

---

## 📚 참고 문서

- **[DDD_AGGREGATE_MIGRATION_GUIDE.md](DDD_AGGREGATE_MIGRATION_GUIDE.md)** - DDD Aggregate 패턴 전환 가이드
  - Domain 레이어 Aggregate 구조
  - Application/Adapter 레이어 Aggregate별 구조
  - Technical Concern vs DDD Aggregate 패턴 비교
- **[VERSION_MANAGEMENT_GUIDE.md](VERSION_MANAGEMENT_GUIDE.md)** - Gradle Version Catalog 사용법
- **[DYNAMIC_HOOKS_GUIDE.md](DYNAMIC_HOOKS_GUIDE.md)** - Claude Code 동적 훅 시스템
- **[JAVA_RECORD_GUIDE.md](JAVA_RECORD_GUIDE.md)** - Java Record 활용 가이드
- **[README.md](README.md)** - 프로젝트 전체 가이드
- **GitHub Issues**:
  - [#13: Port & Interface 설계 가이드](https://github.com/ryu-qqq/claude-spring-standards/issues/13)
  - [#12: 패키지 구조 개선](https://github.com/ryu-qqq/claude-spring-standards/issues/12)

---

**🎯 이 문서의 모든 규칙은 ArchUnit 테스트, Git 훅, Checkstyle로 자동 검증됩니다.**
