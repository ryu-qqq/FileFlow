# Persistence Layer 규칙 요약본 (2025-11-05)

> **용도**: `/cc:load` 초기 로딩용 Persistence Layer 핵심 요약본
> **상세 규칙**: Hook이 자동으로 27개 Cache Rules를 실시간 주입 (O(1) 검색)

---

## 🚨 Zero-Tolerance (절대 금지)

- ❌ **JPA 관계 어노테이션**: `@ManyToOne`, `@OneToMany`, `@OneToOne`, `@ManyToMany` ⭐
- ❌ **Lombok 사용**: `@Data`, `@Builder`, `@NoArgsConstructor` 등 금지
- ❌ **N+1 쿼리**: `fetch join` 또는 `@EntityGraph` 사용
- ❌ **Entity를 Domain으로 직접 사용**: Entity ↔ Domain 분리 필수
- ❌ **QueryDSL 없이 복잡한 조회**: Native Query 대신 QueryDSL

---

## ✅ 필수 규칙

### 1️⃣ JPA Entity 설계 (Long FK 전략)
- ✅ **Long FK Strategy**: `private Long userId;` (관계 어노테이션 금지) ⭐
- ✅ **Constructor Pattern**: Protected 생성자 + Factory Method
- ✅ **Audit Entity Pattern**: `BaseEntity` (createdAt, updatedAt)
- ✅ **Unique Constraints**: `@UniqueConstraint` 명시

### 2️⃣ Command Adapter (Write 작업)
- ✅ **Save Port Pattern**: `SaveOrderPort`
- ✅ **Delete Port Pattern**: `DeleteOrderPort`
- ✅ **Command Adapter Implementation**: Entity ↔ Domain 변환
- ✅ **Command Mapper**: `OrderJpaMapper.toEntity()`, `toDomain()`

### 3️⃣ Query Adapter (Read 작업)
- ✅ **Load Port Pattern**: `LoadOrderPort`
- ✅ **QueryDSL DTO Projection**: `Projections.constructor()`
- ✅ **Query Adapter Implementation**: 복잡한 조회 최적화
- ✅ **Query Performance Optimization**: `fetch join`, `@EntityGraph`

### 4️⃣ QueryDSL 최적화
- ✅ **DTO Projection**: Select 절에 필요한 필드만
- ✅ **Dynamic Query**: BooleanBuilder로 동적 조건
- ✅ **Batch Processing**: `batchSize` 설정

### 5️⃣ Repository 패턴
- ✅ **Aggregate Repository**: Aggregate Root만 Repository
- ✅ **Custom Repository**: `@Repository` + Custom 인터페이스
- ✅ **Specification Pattern**: 복잡한 조회 조건 캡슐화

### 6️⃣ Configuration (MySQL, Redis)
- ✅ **MySQL HikariCP**: Connection Pool 설정
- ✅ **Redis Lettuce**: Redis 연결 설정

### 7️⃣ Testing (Testcontainers)
- ✅ **Repository Unit Testing**: `@DataJpaTest`
- ✅ **Command Adapter Unit Testing**: Mock 사용
- ✅ **Query Adapter Unit Testing**: Testcontainers
- ✅ **Testcontainers Integration**: Real DB 테스트
- ✅ **Test Tags Strategy**: `@Tag("integration")`

---

## 📊 레이어 통계

- **총 규칙 수**: 27개
- **Zero-Tolerance**: 5개
- **필수 규칙**: 22개
- **Cache Rules**: 27개 (Hook 자동 주입)

---

## 🔗 상세 문서 (27개 Cache Rules)

### Command Adapter Patterns
- `01_save-port-pattern.md`, `02_delete-port-pattern.md`, `03_command-adapter-implementation.md`, `04_command-mapper-patterns.md`

### Config
- `01_mysql-hikaricp-configuration.md`, `02_redis-lettuce-configuration.md`

### JPA Entity Design
- `00_jpa-entity-core-rules.md` ⭐, `01_long-fk-strategy.md` ⭐, `02_constructor-pattern.md`, `03_audit-entity-pattern.md`

### Package Guide
- `01_persistence_package_guide.md`

### Query Adapter Patterns
- `01_load-port-pattern.md`, `02_querydsl-dto-projection.md`, `03_query-adapter-implementation.md`, `04_query-performance-optimization.md`

### QueryDSL Optimization
- `01_dto-projection.md`, `02_dynamic-query.md`, `03_batch-processing.md`

### Repository Patterns
- `01_aggregate-repository.md`, `02_custom-repository.md`, `03_specification-pattern.md`

### Testing
- `01_command-adapter-unit-testing.md`, `01_repository-unit-testing.md`, `02_query-adapter-unit-testing.md`, `02_testcontainers-integration.md`, `03_test-tags-strategy.md`, `03_testcontainers-integration.md`, `04_test-tags-strategy.md`

**완전한 규칙은 Hook 시스템이 실시간으로 제공합니다!**

---

## 🎯 핵심 패턴

### Long FK Strategy (핵심!)
```java
@Entity
@Table(name = "orders")
public class OrderJpaEntity {
    @Id
    private Long id;

    // ✅ Long FK (관계 어노테이션 금지)
    private Long customerId;
    private Long productId;

    // ❌ 금지!
    // @ManyToOne
    // private CustomerJpaEntity customer;
}
```

### Entity ↔ Domain 분리
```java
// Entity (Persistence)
@Entity
public class OrderJpaEntity {
    @Id private Long id;
    private Long customerId;
    private String status;
}

// Domain (Business Logic)
public class Order {
    private final OrderId id;
    private OrderStatus status; // Enum

    public void place() { ... } // 비즈니스 로직
}

// Mapper
public class OrderJpaMapper {
    public static OrderJpaEntity toEntity(Order domain) { ... }
    public static Order toDomain(OrderJpaEntity entity) { ... }
}
```

---

**✅ Long FK 전략은 A/B 테스트에서 40회 위반 → 0회로 감소!**
