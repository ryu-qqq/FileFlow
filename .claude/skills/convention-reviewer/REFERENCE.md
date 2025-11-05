# 코딩 컨벤션 참조 가이드

이 문서는 `convention-reviewer` Skill이 참조하는 상세 규칙 목록입니다.

## Zero-Tolerance 규칙 (🔴 Critical)

### 1. Lombok 금지

**위치**: `docs/coding_convention/02-domain-layer/law-of-demeter/02_lombok-prohibition.md`

**규칙**:
- Domain layer에서 Lombok 절대 금지
- `@Data`, `@Builder`, `@Getter`, `@Setter` 등 모두 금지
- Pure Java getter/setter 직접 작성 필수

**감지 패턴**:
```bash
grep -r "@Data\|@Builder\|@Getter\|@Setter\|@AllArgsConstructor\|@NoArgsConstructor" domain/
```

**수정 예시**:
```java
// ❌ Before
@Data
public class Order {
    private Long id;
    private String orderNumber;
}

// ✅ After
public class Order {
    private Long id;
    private String orderNumber;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
}
```

### 2. Law of Demeter (Getter 체이닝 금지)

**위치**: `docs/coding_convention/02-domain-layer/law-of-demeter/01_getter-chaining-prohibition.md`

**규칙**:
- Getter 체이닝 절대 금지: `order.getCustomer().getAddress().getZip()`
- "Tell, Don't Ask" 원칙 준수
- Domain 객체에 행동 메서드 추가

**감지 패턴**:
```bash
grep -r "\.get.*()\.get.*()\.get" --include="*.java"
```

**수정 예시**:
```java
// ❌ Before
String zip = order.getCustomer().getAddress().getZip();

// ✅ After
String zip = order.getCustomerZipCode();

// Order.java에 추가
public String getCustomerZipCode() {
    return customer.getAddressZipCode();
}
```

### 3. Long FK 전략 (JPA 관계 금지)

**위치**: `docs/coding_convention/04-persistence-layer/jpa-entity-design/01_long-fk-strategy.md`

**규칙**:
- JPA 관계 어노테이션 절대 금지
- `@ManyToOne`, `@OneToMany`, `@OneToOne`, `@ManyToMany` 사용 불가
- Long FK 사용: `private Long userId;`

**감지 패턴**:
```bash
grep -r "@ManyToOne\|@OneToMany\|@OneToOne\|@ManyToMany" persistence/
```

**수정 예시**:
```java
// ❌ Before
@Entity
public class Order {
    @ManyToOne
    private Customer customer;
}

// ✅ After
@Entity
public class Order {
    private Long customerId;  // Long FK
}
```

### 4. Transaction 경계

**위치**: `docs/coding_convention/03-application-layer/transaction-management/01_transaction-boundary.md`

**규칙**:
- `@Transactional` 내 외부 API 호출 절대 금지
- RestTemplate, WebClient, Feign 호출은 트랜잭션 밖에서
- 트랜잭션은 짧게 유지

**감지 패턴**:
```bash
# @Transactional 메서드 내 외부 API 호출 감지 (수동 검토 필요)
grep -A 30 "@Transactional" application/ | grep -E "restTemplate|webClient|feignClient"
```

**수정 예시**:
```java
// ❌ Before
@Transactional
public void placeOrder(OrderCommand cmd) {
    Order order = orderRepository.save(new Order(cmd));
    paymentClient.processPayment(order);  // ❌ 외부 API
}

// ✅ After
public void placeOrder(OrderCommand cmd) {
    Order order = placeOrderInTransaction(cmd);
    paymentClient.processPayment(order);  // ✅ 트랜잭션 밖
}

@Transactional
private Order placeOrderInTransaction(OrderCommand cmd) {
    return orderRepository.save(new Order(cmd));
}
```

### 5. Spring Proxy 제약사항

**위치**: `docs/coding_convention/03-application-layer/transaction-management/02_spring-proxy-constraints.md`

**규칙**:
- Private 메서드에 `@Transactional` 금지
- Final 클래스/메서드에 `@Transactional` 금지
- 같은 클래스 내부 호출 (`this.method()`)에서 `@Transactional` 작동 안 함

**감지 패턴**:
```bash
grep -r "private.*@Transactional\|@Transactional.*private" application/
grep -r "final.*@Transactional\|@Transactional.*final" application/
```

## 레이어별 규칙 (🟡 Important)

### Domain Layer

**규칙 디렉토리**: `docs/coding_convention/02-domain-layer/`

**주요 규칙**:
1. **Aggregate 설계** (`aggregate-design/00_domain-object-creation-guide.md`)
   - Aggregate Root 식별
   - Invariant 보호
   - 최소 단위 유지

2. **Tell Don't Ask** (`law-of-demeter/03_domain-encapsulation.md`)
   - 상태 묻지 말고 행동 시키기
   - 캡슐화 유지

3. **패키지 구조** (`package-guide/01_domain_package_guide.md`)
   - Aggregate 중심 구조
   - Entity, ValueObject, DomainService 분리

### Application Layer

**규칙 디렉토리**: `docs/coding_convention/03-application-layer/`

**주요 규칙**:
1. **UseCase 설계** (`usecase-design/01_usecase-interface.md`)
   - Port/In 인터페이스 정의
   - 단일 책임 원칙

2. **DTO 패턴** (`dto-patterns/01_command-pattern.md`, `02_response-pattern.md`)
   - Command/Query 분리
   - Immutable DTO

3. **Facade 패턴** (`facade/01_facade-usage-guide.md`)
   - 복잡한 UseCase 조합
   - Transaction 경계 관리

### Persistence Layer

**규칙 디렉토리**: `docs/coding_convention/04-persistence-layer/`

**주요 규칙**:
1. **JPA Entity 설계** (`jpa-entity-design/00_jpa-entity-core-rules.md`)
   - Long FK 전략 (관계 어노테이션 금지)
   - Immutable Entity
   - Constructor 패턴

2. **QueryDSL 최적화** (`query-adapter-patterns/04_query-performance-optimization.md`)
   - DTO Projection
   - N+1 방지

3. **Repository 패턴** (`command-adapter-patterns/01_save-port-pattern.md`)
   - Command/Query 분리
   - Port/Out 인터페이스

### REST API Layer

**규칙 디렉토리**: `docs/coding_convention/01-adapter-rest-api-layer/`

**주요 규칙**:
1. **Controller 설계** (`controller-design/01_rest-api-conventions.md`)
   - REST 표준 준수
   - HTTP 상태 코드 정확한 사용

2. **Exception 처리** (`exception-handling/01_global-exception-handler.md`)
   - Global Exception Handler
   - 일관된 Error Response

3. **Mapper 패턴** (`mapper-patterns/01_request-mapper.md`)
   - DTO ↔ Domain 변환
   - Validation

## Best Practices (🟢 Recommended)

### Java 21 Patterns

**규칙 디렉토리**: `docs/coding_convention/06-java21-patterns/`

1. **Record 패턴** (`record-patterns/02_value-objects-with-records.md`)
   - ValueObject는 Record 사용
   - Compact Constructor

2. **Sealed Class** (`sealed-classes/01_sealed-hierarchy.md`)
   - 제한된 상속 구조
   - Pattern Matching

3. **Virtual Threads** (`virtual-threads/01_virtual-threads-integration.md`)
   - @Async + Virtual Threads
   - 동시성 향상

### Orchestration Patterns

**규칙 디렉토리**: `docs/coding_convention/09-orchestration-patterns/`

1. **3-Phase Lifecycle** (`overview/01_3-phase-lifecycle.md`)
   - Log → Execute → Finalize
   - Crash Recovery

2. **Idempotency** (`idempotency-handling/01_idem-key-strategy.md`)
   - IdemKey 기반 멱등성
   - Race Condition 방지

3. **WAL (Write-Ahead Log)** (`write-ahead-log/01_wal-entity-design.md`)
   - 작업 로깅
   - Reaper/Finalizer

## 스캔 우선순위

1. **🔴 Critical (Zero-Tolerance)**: 즉시 수정 필요
   - Lombok 사용
   - Getter 체이닝
   - JPA 관계 어노테이션
   - Transaction 경계 위반
   - Spring Proxy 제약사항 위반

2. **🟡 Important (레이어 규칙)**: 리팩토링 권장
   - Domain 캡슐화 부족
   - UseCase 설계 미흡
   - Repository 패턴 위반
   - Controller 설계 미흡

3. **🟢 Recommended (Best Practices)**: 점진적 개선
   - Java 21 패턴 미적용
   - Orchestration 패턴 미적용
   - Enterprise 패턴 미적용

## 참고 사항

- 모든 규칙 문서는 `docs/coding_convention/` 하위에 있습니다
- 각 규칙은 "Before/After" 예시를 포함합니다
- ArchUnit 테스트로 일부 규칙은 자동 검증됩니다 (`bootstrap/src/test/java/.../architecture/`)
