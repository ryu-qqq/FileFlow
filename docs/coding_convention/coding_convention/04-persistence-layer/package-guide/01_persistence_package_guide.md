# PERSISTENCE 패키지 가이드

> 멀티모듈 기반 Persistence Layer. **MySQL/Redis 기술 스택 분리**, **Bounded Context 단위 구성**.

## 🎯 핵심 원칙

> **Persistence는 저장/조회만! 절대 데이터 변경하지 않음.**
>
> - ✅ Domain ↔ JPA Entity 변환
> - ✅ 쿼리 최적화 (N+1 방지, Fetch Join)
> - ✅ JPA Dirty Checking으로 UPDATE 자동 처리
> - ❌ 비즈니스 로직 작성 금지
> - ❌ 데이터 직접 변경 금지 (setter 호출 금지)
> - ❌ 상태 전이 금지 (Domain 메서드 없이 필드 수정 금지)
>
> 상세한 Layer별 책임은 [비즈니스 로직 배치 원칙](../_shared/business-logic-placement.md) 참조.

## 모듈 구조

```
root/
├─ domain/                          # 순수 도메인 모델
├─ application/                     # UseCase, Port 인터페이스
├─ adapter-in/rest-api/                  # REST API
├─ adapter-out/persistence-mysql/   # ✅ MySQL 영속성
├─ adapter-out/persistence-redis/   # ✅ Redis 영속성
└─ adapter-out/external/            # 외부 API
```

---

## 디렉터리 구조

### adapter-out-persistence-mysql/

```
adapter-out-persistence-mysql/
└─ src/main/java/com/company/adapter/out/persistence/mysql/
   ├─ order/                        # Order Bounded Context
   │  ├─ entity/                    # JPA Entity
   │  │  ├─ OrderJpaEntity.java
   │  │  └─ OrderItemJpaEntity.java
   │  ├─ repository/                # Spring Data JPA
   │  │  ├─ OrderJpaRepository.java
   │  │  └─ OrderItemJpaRepository.java
   │  ├─ querydsl/                  # QueryDSL Custom Repository
   │  │  └─ OrderQueryDslRepository.java
   │  ├─ adapter/                   # Port 구현체
   │  │  └─ OrderPersistenceAdapter.java
   │  ├─ mapper/                    # Entity ↔ Domain 변환
   │  │  └─ OrderEntityMapper.java
   │  └─ dto/                       # Persistence 내부 DTO
   │     └─ OrderSummaryDto.java
   │
   ├─ product/                      # Product Bounded Context
   │  ├─ entity/
   │  ├─ repository/
   │  ├─ querydsl/
   │  ├─ adapter/
   │  ├─ mapper/
   │  └─ dto/
   │
   └─ config/                       # MySQL 전용 설정
      ├─ JpaConfig.java
      └─ QueryDslConfig.java
```

### adapter-out-persistence-redis/

```
adapter-out-persistence-redis/
└─ src/main/java/com/company/adapter/out/persistence/redis/
   ├─ session/                      # Session Bounded Context
   │  ├─ entity/                    # Redis Hash Entity
   │  │  └─ SessionRedisEntity.java
   │  ├─ repository/                # Spring Data Redis
   │  │  └─ SessionRedisRepository.java
   │  ├─ adapter/                   # Port 구현체
   │  │  └─ SessionCacheAdapter.java
   │  ├─ mapper/                    # Entity ↔ Domain 변환
   │  │  └─ SessionEntityMapper.java
   │  └─ dto/                       # Persistence 내부 DTO
   │
   └─ config/                       # Redis 전용 설정
      └─ RedisConfig.java
```

---

## 패키지별 역할

### 1. entity/ - 영속성 객체

**역할**:
- DB 테이블/Redis Hash 매핑
- JPA Entity (`@Entity`, `@Table`)
- Redis Entity (`@RedisHash`)

**규칙**:
- Long FK 전략 (JPA 관계 어노테이션 금지)
- `private final` 필드 (불변성)
- **❌ Setter 금지**
- **❌ 비즈니스 메서드 금지** - Domain Model에서만
- Static Factory Method (`create()`, `reconstitute()`)

**예시**:
```java
package com.company.adapter.out.persistence.mysql.order.entity;

/**
 * Order JPA Entity
 *
 * ✅ DB 매핑만 담당
 * ❌ 비즈니스 로직 없음!
 */
@Entity
@Table(name = "orders")
public class OrderJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private final Long userId;  // ✅ Long FK

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    protected OrderJpaEntity() {
        this.userId = null;
    }

    public static OrderJpaEntity create(Long userId) {
        return new OrderJpaEntity(userId, OrderStatus.PENDING);
    }

    // ✅ Getter만 제공 (비즈니스 메서드 없음!)
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
}
```

---

### 2. repository/ - 데이터 접근

**역할**:
- Spring Data JPA/Redis Repository
- 단순 CRUD, 메서드 쿼리
- 복잡한 쿼리는 querydsl/ 패키지로 분리

**규칙**:
- `JpaRepository<Entity, ID>` 상속
- 메서드 이름 규칙 준수 (`findByXxx`, `existsByXxx`)
- 복잡한 쿼리는 querydsl/로 분리

**예시**:
```java
package com.company.adapter.out.persistence.mysql.order.repository;

/**
 * Order JPA Repository
 */
@Repository
public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long> {

    // ✅ 단순 쿼리만
    List<OrderJpaEntity> findByUserId(Long userId);

    Optional<OrderJpaEntity> findByIdAndStatus(Long id, OrderStatus status);
}
```

---

### 3. querydsl/ - 복잡한 쿼리

**역할**:
- QueryDSL 기반 복잡한 쿼리
- DTO Projection
- Dynamic Query (BooleanBuilder)
- Batch Processing

**규칙**:
- `@Repository` 어노테이션
- `JPAQueryFactory` 주입
- DTO Projection 우선 사용

**예시**:
```java
package com.company.adapter.out.persistence.mysql.order.querydsl;

/**
 * Order QueryDSL Repository
 */
@Repository
public class OrderQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public List<OrderSummaryDto> findOrderSummaries(SearchOrdersQuery query) {
        QOrderJpaEntity order = QOrderJpaEntity.orderJpaEntity;
        QUserJpaEntity user = QUserJpaEntity.userJpaEntity;

        BooleanBuilder builder = new BooleanBuilder();
        if (query.status() != null) {
            builder.and(order.status.eq(query.status()));
        }

        return queryFactory
            .select(Projections.constructor(OrderSummaryDto.class,
                order.id,
                user.name,
                order.totalAmount
            ))
            .from(order)
            .join(user).on(order.userId.eq(user.id))
            .where(builder)
            .fetch();
    }
}
```

---

### 4. adapter/ - Port 구현체

**역할**:
- Outbound Port 인터페이스 구현
- Entity ↔ Domain 변환 (Mapper 사용)
- **❌ 트랜잭션 경계 없음** (Application Layer에서 관리)

**규칙**:
- `@Component` 어노테이션
- Port 인터페이스 구현
- **❌ `@Transactional` 절대 금지**
- Mapper를 통한 변환만 (비즈니스 로직 없음)

**예시**:
```java
package com.company.adapter.out.persistence.mysql.order.adapter;

/**
 * Order Persistence Adapter
 *
 * ✅ LoadOrderPort 구현
 * ✅ SaveOrderPort 구현
 * ❌ @Transactional 금지!
 */
@Component
public class OrderPersistenceAdapter implements LoadOrderPort, SaveOrderPort {

    private final OrderJpaRepository orderRepository;
    private final OrderEntityMapper mapper;

    @Override
    public Optional<Order> loadById(OrderId orderId) {
        return orderRepository.findById(orderId.getValue())
            .map(mapper::toDomain);
    }

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = mapper.toEntity(order);
        OrderJpaEntity saved = orderRepository.save(entity);
        return mapper.toDomain(saved);
    }
}
```

---

### 5. mapper/ - Entity ↔ Domain 변환

**역할**:
- Entity → Domain 변환 (`toDomain()`)
- Domain → Entity 변환 (`toEntity()`)
- 순수 변환 로직만 (비즈니스 로직 없음)

**규칙**:
- `@Component` 어노테이션
- Stateless (상태 없음)
- Null 체크 포함

**예시**:
```java
package com.company.adapter.out.persistence.mysql.order.mapper;

/**
 * Order Entity Mapper
 */
@Component
public class OrderEntityMapper {

    public Order toDomain(OrderJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Order.reconstitute(
            OrderId.of(entity.getId()),
            UserId.of(entity.getUserId()),
            entity.getStatus(),
            Money.of(entity.getTotalAmount())
        );
    }

    public OrderJpaEntity toEntity(Order order) {
        if (order == null) {
            return null;
        }

        return OrderJpaEntity.reconstitute(
            order.getId().getValue(),
            order.getUserId().getValue(),
            order.getStatus(),
            order.getTotalAmount().getValue()
        );
    }
}
```

---

### 6. dto/ - Persistence 내부 DTO

**역할**:
- QueryDSL DTO Projection 결과 담기
- Join 결과 임시 저장
- **Persistence Layer 내부에서만 사용**

**규칙**:
- Java Record 사용 권장
- **Application Layer로 반환하지 않음** (Domain으로 변환 후 반환)

**예시**:
```java
package com.company.adapter.out.persistence.mysql.order.dto;

/**
 * Order Summary DTO (Persistence 내부 전용)
 *
 * ✅ QueryDSL Projection 결과
 * ❌ Application Layer로 반환 금지
 */
public record OrderSummaryDto(
    Long orderId,
    String userName,
    BigDecimal totalAmount,
    OrderStatus status
) {}
```

---

## Entity vs Domain Model 구분

| 구분 | JPA Entity | Domain Model |
|------|-----------|--------------|
| **역할** | DB 매핑만 담당 | 비즈니스 로직 포함 |
| **위치** | `adapter/out/persistence-mysql/{context}/entity/` | `domain/{context}/` |
| **특징** | JPA 어노테이션, Long FK, Getter만 | Framework 독립, 비즈니스 메서드 |
| **변경** | Mapper를 통해서만 | 비즈니스 메서드로 |

**변환 흐름**:
```
Domain (Order) ↔ Mapper ↔ Entity (OrderJpaEntity)
```

---

## CQRS와 Persistence Layer

**Application Layer (CQRS 분리)**:
```
application/
├─ command/
│  └─ CreateOrderService implements CreateOrderUseCase
│     → SaveOrderPort 사용
└─ query/
   └─ GetOrderService implements GetOrderQuery
      → LoadOrderPort 사용
```

**Persistence Layer (구현체는 동일)**:
```
OrderPersistenceAdapter implements SaveOrderPort, LoadOrderPort
```

**핵심**:
- Adapter는 **Command Port와 Query Port 모두 구현**
- CQRS는 Application Layer의 UseCase 분리일 뿐
- Persistence Layer는 Port만 구현하면 됨

---

## 트랜잭션 관리

### ❌ Persistence Layer에서 트랜잭션 금지!

```java
// ❌ 절대 금지!
@Component
public class OrderPersistenceAdapter implements SaveOrderPort {

    @Transactional  // ❌❌❌ 절대 금지!
    @Override
    public Order save(Order order) {
        // ...
    }
}
```

### ✅ Application Layer에서만 트랜잭션 관리

```java
// ✅ 올바른 방법
@UseCase
@Transactional  // ✅ Application Layer에서만!
public class CreateOrderService implements CreateOrderUseCase {

    private final SaveOrderPort saveOrderPort;

    @Override
    public OrderId execute(CreateOrderCommand command) {
        // 1. Domain 로직
        Order order = Order.create(command.userId(), command.totalAmount());

        // 2. Persistence 저장 (트랜잭션 내에서)
        Order saved = saveOrderPort.save(order);

        return saved.getId();
    }
}
```

---

## 데이터 흐름

```
1. Application Layer (UseCase)
   @Transactional  ← 트랜잭션 경계
   CreateOrderService.execute() {
       Order order = Order.create(...);  // Domain 로직
       order.confirm();  // 비즈니스 로직
       saveOrderPort.save(order);  // Port 호출
   }

↓

2. Persistence Adapter (Port 구현)
   ❌ @Transactional 없음!
   OrderPersistenceAdapter.save() {
       OrderJpaEntity entity = mapper.toEntity(order);  // Domain → Entity
       OrderJpaEntity saved = orderRepository.save(entity);  // DB 저장
       return mapper.toDomain(saved);  // Entity → Domain
   }

↓

3. JPA Entity
   ❌ 비즈니스 로직 없음!
   OrderJpaEntity {
       private Long id;
       private Long userId;  // Long FK
       public Long getId() { return id; }  // Getter만
   }
```

---

## Do / Don't

### ✅ Do

- Long FK 전략 사용 (JPA 관계 어노테이션 금지)
- Entity는 DB 매핑만, Domain Model은 비즈니스 로직
- 트랜잭션은 Application Layer에서만
- Mapper로 Entity ↔ Domain 변환
- DTO Projection으로 조회 최적화
- Bounded Context별 패키지 분리
- MySQL/Redis 모듈 분리

### ❌ Don't

- Entity에 `@OneToMany`, `@ManyToOne` 사용 금지
- Entity에 비즈니스 메서드 추가 금지
- Entity에 Setter 추가 금지
- Persistence Adapter에 `@Transactional` 사용 금지
- DTO를 Application Layer로 반환 금지 (Domain으로 변환)
- 기술 스택 혼재 금지 (MySQL/Redis 분리)

---

## ArchUnit 검증

```java
/**
 * Persistence Layer 규칙 검증
 */
@AnalyzeClasses(packages = "com.company.adapter.out.persistence")
public class PersistenceLayerTest {

    /**
     * Entity는 JPA 관계 어노테이션 금지
     */
    @ArchTest
    static final ArchRule jpa_entities_should_not_use_relationship_annotations =
        fields()
            .that().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
            .should().notBeAnnotatedWith(OneToMany.class)
            .andShould().notBeAnnotatedWith(ManyToOne.class)
            .andShould().notBeAnnotatedWith(ManyToMany.class)
            .andShould().notBeAnnotatedWith(OneToOne.class);

    /**
     * Entity는 Setter 금지
     */
    @ArchTest
    static final ArchRule entities_should_not_have_setters =
        methods()
            .that().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
            .and().arePublic()
            .should().notHaveNameMatching("set[A-Z].*");

    /**
     * Adapter는 @Transactional 금지
     */
    @ArchTest
    static final ArchRule adapters_should_not_have_transactional =
        classes()
            .that().resideInAPackage("..adapter.out.persistence..")
            .and().haveSimpleNameEndingWith("Adapter")
            .should().notBeAnnotatedWith(Transactional.class);
}
```

---

## 체크리스트

### entity/
- [ ] Long FK 전략 (JPA 관계 어노테이션 금지)
- [ ] `private final` 필드
- [ ] Setter 금지
- [ ] 비즈니스 메서드 금지
- [ ] Static Factory Method 제공

### repository/
- [ ] 단순 쿼리만 (메서드 쿼리)
- [ ] 복잡한 쿼리는 querydsl/로 분리

### querydsl/
- [ ] DTO Projection 사용
- [ ] BooleanBuilder로 동적 쿼리
- [ ] Batch Processing 최적화

### adapter/
- [ ] Port 인터페이스 구현
- [ ] **❌ `@Transactional` 절대 금지**
- [ ] Mapper로 변환만
- [ ] Stateless

### mapper/
- [ ] `toDomain()`, `toEntity()` 제공
- [ ] Null 체크 포함
- [ ] Stateless

### dto/
- [ ] Record 사용
- [ ] Persistence 내부에서만 사용
- [ ] Application Layer로 반환 금지

---

## 관련 가이드

**전제 조건**:
- [Domain Package Guide](../../02-domain-layer/package-guide/01_domain_package_guide.md)

**JPA Entity Design**:
- [Long FK Strategy](../jpa-entity-design/01_long-fk-strategy.md)
- [Entity Immutability](../jpa-entity-design/02_entity-immutability.md)
- [N+1 Prevention](../jpa-entity-design/03_n-plus-one-prevention.md)

**QueryDSL Optimization**:
- [DTO Projection](../querydsl-optimization/01_dto-projection.md)
- [Dynamic Query](../querydsl-optimization/02_dynamic-query.md)
- [Batch Processing](../querydsl-optimization/03_batch-processing.md)

**Repository Patterns**:
- [Aggregate Repository](../repository-patterns/01_aggregate-repository.md)
- [Custom Repository](../repository-patterns/02_custom-repository.md)
- [Specification Pattern](../repository-patterns/03_specification-pattern.md)

---

**작성일**: 2025-10-17
**검증 도구**: ArchUnit `PersistenceLayerTest.java`
