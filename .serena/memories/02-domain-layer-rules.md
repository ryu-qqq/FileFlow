# Domain Layer 규칙 요약본 (2025-11-05)

> **용도**: `/cc:load` 초기 로딩용 Domain Layer 핵심 요약본
> **상세 규칙**: Hook이 자동으로 17개 Cache Rules를 실시간 주입 (O(1) 검색)

---

## 🚨 Zero-Tolerance (절대 금지)

- ❌ **Lombok 절대 금지**: Domain에서는 특히 엄격 (`@Data`, `@Builder`, `@Getter` 등 모두 금지)
- ❌ **Getter 체이닝**: `order.getCustomer().getAddress().getZipCode()` (Law of Demeter 위반)
- ❌ **Anemic Domain Model**: Getter/Setter만 있고 비즈니스 로직 없음
- ❌ **Public Setter**: Domain 불변식 보호 불가능
- ❌ **Spring/Jakarta 의존성**: Domain은 순수 Java (Framework 독립적)

---

## ✅ 필수 규칙

### 1️⃣ Aggregate 설계 (DDD 핵심)
- ✅ **Aggregate Root**: 불변식 보호, 외부 접근 통제
- ✅ **Aggregate Boundaries**: 트랜잭션 경계 = Aggregate 경계
- ✅ **Consistency Boundaries**: 강한 일관성 vs 최종 일관성
- ✅ **Identity**: EntityId (Value Object)로 식별성 관리

### 2️⃣ Law of Demeter (Tell, Don't Ask)
- ✅ **Getter 체이닝 금지**: `order.getCustomer().getAddress()` ❌
- ✅ **Tell, Don't Ask**: `order.getCustomerZipCode()` ✅
- ✅ **Domain Encapsulation**: 비즈니스 메서드로 캡슐화
- ✅ **예시**:
  ```java
  // ❌ Bad: Getter 체이닝
  if (order.getCustomer().getAddress().getZipCode().startsWith("06")) { ... }

  // ✅ Good: Tell, Don't Ask
  if (order.isSeoulAreaOrder()) { ... }
  ```

### 3️⃣ Domain 객체 생성
- ✅ **Factory Pattern**: 복잡한 생성 로직 분리
- ✅ **Builder Pattern (Pure Java)**: Lombok 없이 직접 구현
- ✅ **Named Constructor**: `Order.createNew()`, `Order.fromExisting()`
- ✅ **Validation**: 생성 시점에 불변식 검증

### 4️⃣ Value Object
- ✅ **Immutable**: 불변 객체
- ✅ **Equality by Value**: `equals()` + `hashCode()` 오버라이드
- ✅ **Self-Validation**: 생성 시 유효성 검증
- ✅ **예시**: `OrderId`, `Money`, `Email`, `Address`

### 5️⃣ Domain Event
- ✅ **Event Naming**: `OrderPlaced`, `PaymentCompleted` (과거형)
- ✅ **Event Publishing**: `AbstractAggregateRoot.registerEvent()`
- ✅ **Event Handling**: `@TransactionalEventListener`

### 6️⃣ Testing (Domain은 빠른 테스트)
- ✅ **Unit Test**: Spring Context 없이 Pure Java 테스트
- ✅ **Aggregate Testing**: 비즈니스 로직 검증
- ✅ **Value Object Testing**: 불변식 검증
- ✅ **Test Fixture Pattern**: 테스트 데이터 재사용
- ✅ **Object Mother Pattern**: 비즈니스 시나리오 표현

---

## 📊 레이어 통계

- **총 규칙 수**: 17개
- **Zero-Tolerance**: 5개
- **필수 규칙**: 12개
- **Cache Rules**: 17개 (Hook 자동 주입)

---

## 🔗 상세 문서

**Hook이 자동으로 주입하는 Cache Rules (17개)**:

### Aggregate Design
- `00_domain-object-creation-guide.md` - Domain 객체 생성 가이드
- `01_aggregate-boundaries.md` - Aggregate 경계 설정
- `02_aggregate-root-design.md` - Aggregate Root 설계
- `03_consistency-boundaries.md` - 일관성 경계

### Law of Demeter
- `01_getter-chaining-prohibition.md` - Getter 체이닝 금지 ⭐
- `02_tell-dont-ask-pattern.md` - Tell, Don't Ask 패턴 ⭐
- `03_domain-encapsulation.md` - Domain 캡슐화

### Package Guide
- `01_domain_package_guide.md` - Domain 패키지 구조

### Testing
- `00_testing-support-toolkit.md` - 테스트 지원 도구
- `01_aggregate-testing.md` - Aggregate 테스트
- `02_value-object-testing.md` - Value Object 테스트
- `03_test-fixture-pattern.md` - Test Fixture 패턴
- `04_factory-testing.md` - Factory 테스트
- `04_object-mother-pattern.md` - Object Mother 패턴
- `05_domain-event-testing.md` - Domain Event 테스트
- `06_policy-testing.md` - Policy 테스트
- `08_archunit-rules.md` - ArchUnit 규칙

**완전한 규칙은 Hook 시스템이 실시간으로 제공합니다!**

---

## 🎯 핵심 패턴

### Aggregate Root 구조
```java
public class Order {
    private final OrderId id;
    private OrderStatus status;
    private List<OrderLine> orderLines;

    // ✅ Factory Method (Named Constructor)
    public static Order createNew(CustomerId customerId, List<OrderLine> lines) {
        validateOrderLines(lines);
        return new Order(OrderId.newId(), customerId, lines, OrderStatus.PENDING);
    }

    // ✅ Tell, Don't Ask (비즈니스 메서드)
    public void place() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be placed");
        }
        this.status = OrderStatus.PLACED;
        registerEvent(new OrderPlaced(this.id));
    }

    // ✅ Domain Encapsulation (Getter 체이닝 방지)
    public boolean isSeoulAreaOrder() {
        return this.deliveryAddress.isSeoulArea();
    }

    // ❌ Lombok 금지 - Pure Java Getter
    public OrderId getId() { return id; }
    public OrderStatus getStatus() { return status; }
}
```

### Value Object 구조
```java
public class Money {
    private final BigDecimal amount;
    private final Currency currency;

    // ✅ Immutable + Validation
    public Money(BigDecimal amount, Currency currency) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.amount = amount;
        this.currency = currency;
    }

    // ✅ Equality by Value
    @Override
    public boolean equals(Object o) { ... }

    @Override
    public int hashCode() { ... }
}
```

---

**✅ 이 요약본은 Domain Layer 17개 규칙의 핵심만 포함합니다.**

**🔥 Law of Demeter, Tell Don't Ask는 A/B 테스트에서 40회 위반 → 0회로 감소!**
