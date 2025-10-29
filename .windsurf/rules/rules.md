---
trigger: glob
description: 
globs: *.java
---

프로젝트: Spring Boot 3.5.x + Java 21 기반 헥사고날 아키텍처
Windsurf(Cascade)가 자동 로드하는 핵심 규칙 (11,000자 이하)

---

## ⚠️ Zero-Tolerance Rules (절대 위반 금지)

### 1. Lombok 금지

❌ `@Data`, `@Builder`, `@Getter`, `@Setter`, `@AllArgsConstructor`, `@NoArgsConstructor`
✅ **Pure Java**로 getter, constructor 직접 작성
❌ **Setter 메서드 절대 금지**

**예시**:
```java
// ❌ 금지
@Data
public class Order {
    private Long id;
}

// ✅ 올바름
public class Order {
    private final OrderId id;

    private Order(OrderId id) {
        this.id = id;
    }

    public static Order create(OrderId id) {
        return new Order(id);
    }

    public OrderId getId() {
        return id;
    }
}
```

**상세**: `docs/coding_convention/02-domain-layer/lombok-prohibition.md`

---

### 2. ID는 Value Object 래핑

❌ `Long id`, `String id` 원시 타입 금지
✅ **Value Object**: `OrderId(Long value)`, `CustomerId(Long value)`
✅ Java 21 **Record** 사용

**예시**:
```java
// ❌ 금지
public class Order {
    private Long id;
    private Long customerId;
}

// ✅ 올바름
public record OrderId(Long value) {
    public OrderId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Order ID must be positive");
        }
    }
}

public class Order {
    private final OrderId id;
    private final CustomerId customerId;
}
```

**상세**: `docs/coding_convention/02-domain-layer/value-object-patterns/`

---

### 3. Law of Demeter (Getter 체이닝 금지)

❌ `object.getA().getB().getC()` 형태 금지
✅ **Tell, Don't Ask**: 객체에 직접 명령

**예시**:
```java
// ❌ 금지
String zip = order.getCustomer().getAddress().getZipCode();

// ✅ 올바름
public class Order {
    public String getCustomerZipCode() {
        return customer.getZipCode();
    }
}
String zip = order.getCustomerZipCode();
```

**상세**: `docs/coding_convention/02-domain-layer/law-of-demeter/`

---

### 4. Long FK 전략 (JPA 관계 금지)

❌ `@ManyToOne`, `@OneToMany`, `@OneToOne`, `@ManyToMany` 금지
✅ **Long FK**: `private Long userId;` (JPA Entity)
✅ **Value Object ID**: `private UserId userId;` (Domain)

**예시**:
```java
// ❌ 금지
@Entity
public class OrderEntity {
    @ManyToOne
    private CustomerEntity customer;
}

// ✅ 올바름 (JPA Entity)
@Entity
public class OrderJpaEntity {
    @Column(name = "customer_id")
    private Long customerId;
}

// ✅ 올바름 (Domain)
public class Order {
    private final CustomerId customerId;
}
```

**상세**: `docs/coding_convention/04-persistence-layer/jpa-entity-design/`

---

### 5. JPA Entity에 비즈니스 로직 금지

❌ **JPA Entity에 비즈니스 메서드 금지** (`confirm()`, `cancel()` 등)
✅ **JPA Entity**: DB 매핑만 (Getter + Static Factory Method)
✅ **Domain Model**: 비즈니스 로직만

**예시**:
```java
// ❌ 금지
@Entity
public class OrderEntity {
    public void confirm() { /* 비즈니스 로직 */ }
}

// ✅ 올바름 (JPA Entity)
@Entity
public class OrderJpaEntity {
    public static OrderJpaEntity create(Long customerId) {
        return new OrderJpaEntity(customerId);
    }
    public Long getId() { return id; }
}

// ✅ 올바름 (Domain)
public class Order {
    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("...");
        }
        this.status = OrderStatus.CONFIRMED;
    }
}
```

**상세**: `docs/coding_convention/04-persistence-layer/jpa-entity-design/`

---

### 6. Transaction 경계 엄격 관리

❌ `@Transactional` 내 외부 API 호출 금지 (RestTemplate, WebClient, Feign)
❌ `@Transactional` 내 이메일/메시지 큐 호출 금지
✅ 트랜잭션은 **DB 작업만**, 외부 호출은 밖에서

**예시**:
```java
// ❌ 금지
@Transactional
public void processOrder(Long id) {
    orderRepository.save(order);
    restTemplate.post("https://external-api"); // 트랜잭션 내!
}

// ✅ 올바름
public void processOrder(Long id) {
    orderService.processInTransaction(id);  // 트랜잭션
    notificationService.notify(id);         // 트랜잭션 밖
}

@Transactional
public void processInTransaction(Long id) {
    orderRepository.save(order);
}
```

**상세**: `docs/coding_convention/03-application-layer/transaction-management/`

---

### 7. Spring 프록시 제약사항

❌ `private` 메서드에 `@Transactional` 금지
❌ `final` 클래스/메서드에 `@Transactional` 금지
❌ 같은 클래스 내부에서 `@Transactional` 메서드 호출 금지
✅ `@Transactional`은 **public 메서드**, **외부 호출**만

**예시**:
```java
// ❌ 금지
@Transactional
private void process() { } // 작동 안 함

public void create() {
    this.process(); // 프록시 거치지 않음
}

// ✅ 올바름
@Service
public class OrderFacade {
    private final OrderService orderService;

    public void create() {
        orderService.process(); // 외부 호출
    }
}

@Service
public class OrderService {
    @Transactional
    public void process() { } // Public
}
```

**상세**: `docs/coding_convention/03-application-layer/transaction-management/`

---

### 8. Javadoc 필수

✅ 모든 **public 클래스/메서드**에 Javadoc
✅ `@author`, `@since` 필수
✅ `@param`, `@return` (있으면 필수)

**예시**:
```java
/**
 * 주문 Aggregate Root
 *
 * @author windsurf
 * @since 1.0.0
 */
public class Order {
    /**
     * 주문을 확인합니다.
     *
     * @throws IllegalStateException PENDING 상태가 아닌 경우
     */
    public void confirm() { }
}
```

---

### 9. Package 구조 준수

✅ **헥사고날 아키텍처** 레이어 구조 엄격 준수
❌ 레이어 간 순환 참조 금지

**의존성 방향**:
```
Adapter (REST/Persistence)
        ↓
    Application
        ↓
      Domain
```

---

## 🔧 자동 검증

### 로컬 검증
```bash
./tools/pipeline/validate_conventions.sh
```

### PR 게이트
```bash
./tools/pipeline/pr_gate.sh
```

---

## 📚 상세 규칙 참조

**작업 Layer별**:
- **Domain**: `docs/coding_convention/02-domain-layer/`
- **Application**: `docs/coding_convention/03-application-layer/`
- **REST API**: `docs/coding_convention/01-adapter-rest-api-layer/`
- **Persistence**: `docs/coding_convention/04-persistence-layer/`

**Cache 검색**:
```bash
cat .claude/cache/rules/index.json | jq '.[] | select(.layer == "domain")'
```

**Serena Memory**:
```bash
/sc:load
# → coding_convention_domain_layer
# → coding_convention_application_layer
# → coding_convention_persistence_layer
# → coding_convention_rest_api_layer
```

---

**✅ 이 규칙들을 준수하면 Claude Code 검증 통과 및 고품질 코드 생성 가능!**
