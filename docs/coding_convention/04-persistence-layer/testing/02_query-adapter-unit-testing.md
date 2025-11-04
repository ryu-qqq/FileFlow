# Query Adapter Unit Testing (쿼리 어댑터 단위 테스트)

**목적**: CQRS Query Adapter의 단위 테스트 전략 정의

**위치**: `adapter-persistence/src/test/java/[module]/adapter/`

**필수 버전**: Java 21+, Spring Boot 3.0+, JUnit 5, QueryDSL 5.0+

---

## 🎯 핵심 원칙

### Query Adapter 테스트 전략

Query Adapter는 **QueryDSL DTO Projection**을 사용하여 DTO를 직접 조회하는 책임만 검증합니다:

```
테스트 대상:
1. QueryDSL DTO Projection 동작
2. DTO 직접 반환 (Domain 변환 없음)
3. Soft Delete 필터링 (deletedAt IS NULL)
4. Join 쿼리 N+1 방지
5. Pagination 동작
6. 동적 쿼리 (BooleanBuilder)
```

**규칙**:
- ✅ `@DataJpaTest` (JPA 슬라이스 테스트)
- ✅ H2 In-Memory DB 사용
- ✅ `@Tag("unit")`, `@Tag("query")` 필수
- ✅ DTO 직접 검증 (Domain Model 아님)
- ❌ Command 테스트 금지 (Command Adapter 테스트로 분리)
- ❌ JpaRepository 사용 금지 (JPAQueryFactory만 사용)

---

## 📦 기본 테스트 구조

### Query Adapter 단위 테스트 템플릿

```java
package com.company.adapter.out.persistence.order.adapter;

import com.company.adapter.out.persistence.config.QueryDslConfig;
import com.company.adapter.out.persistence.order.entity.OrderJpaEntity;
import com.company.application.order.dto.response.OrderDetailResponse;
import com.company.application.order.dto.response.OrderSummaryResponse;
import com.company.domain.order.OrderId;
import com.company.domain.order.CustomerId;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Order Query Adapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DataJpaTest
@Import({OrderQueryAdapter.class, QueryDslConfig.class})
@Tag("unit")
@Tag("query")
@DisplayName("Order Query Adapter 단위 테스트")
class OrderQueryAdapterTest {

    @Autowired
    private OrderQueryAdapter queryAdapter;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("ID로 Order 조회 시 DTO를 반환해야 한다")
    void loadById_WithExistingOrder_ShouldReturnDTO() {
        // Given - Entity 직접 저장
        OrderJpaEntity entity = OrderJpaEntity.create(100L, "ORDER-001");
        entity.setTotalAmount(BigDecimal.valueOf(10000));
        entityManager.persist(entity);
        entityManager.flush();
        entityManager.clear();

        // When - DTO 직접 조회
        Optional<OrderDetailResponse> result =
            queryAdapter.loadById(OrderId.of(entity.getId()));

        // Then - DTO 검증
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(entity.getId());
        assertThat(result.get().orderNumber()).isEqualTo("ORDER-001");
        assertThat(result.get().totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(10000));
    }

    @Test
    @DisplayName("Soft Delete된 Order는 조회되지 않아야 한다")
    void loadById_WithDeletedOrder_ShouldReturnEmpty() {
        // Given - Soft Delete된 Entity
        OrderJpaEntity entity = OrderJpaEntity.create(100L, "ORDER-001");
        entity.markAsDeleted();
        entityManager.persist(entity);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<OrderDetailResponse> result =
            queryAdapter.loadById(OrderId.of(entity.getId()));

        // Then - 조회 안 됨
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Customer ID로 Order 목록 조회 시 정렬된 DTO 목록을 반환해야 한다")
    void loadByCustomerId_WithMultipleOrders_ShouldReturnSortedList() {
        // Given
        OrderJpaEntity order1 = OrderJpaEntity.create(100L, "ORDER-001");
        OrderJpaEntity order2 = OrderJpaEntity.create(100L, "ORDER-002");
        OrderJpaEntity order3 = OrderJpaEntity.create(200L, "ORDER-003");

        entityManager.persist(order1);
        entityManager.persist(order2);
        entityManager.persist(order3);
        entityManager.flush();
        entityManager.clear();

        // When
        List<OrderSummaryResponse> results =
            queryAdapter.loadByCustomerId(CustomerId.of(100L));

        // Then
        assertThat(results).hasSize(2);
        assertThat(results)
            .extracting(OrderSummaryResponse::orderNumber)
            .containsExactly("ORDER-002", "ORDER-001");  // createdAt desc
    }

    @Test
    @DisplayName("페이징 조회 시 올바른 Page 객체를 반환해야 한다")
    void loadAll_WithPageable_ShouldReturnPage() {
        // Given - 15개 Order 저장
        for (int i = 1; i <= 15; i++) {
            OrderJpaEntity order = OrderJpaEntity.create(
                100L,
                "ORDER-" + String.format("%03d", i)
            );
            entityManager.persist(order);
        }
        entityManager.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<OrderSummaryResponse> page = queryAdapter.loadAll(pageable);

        // Then
        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isEqualTo(15);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    @DisplayName("두 번째 페이지 조회 시 올바른 오프셋을 적용해야 한다")
    void loadAll_WithSecondPage_ShouldReturnCorrectOffset() {
        // Given
        for (int i = 1; i <= 15; i++) {
            OrderJpaEntity order = OrderJpaEntity.create(100L, "ORDER-" + String.format("%03d", i));
            entityManager.persist(order);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(1, 10);  // 두 번째 페이지

        // When
        Page<OrderSummaryResponse> page = queryAdapter.loadAll(pageable);

        // Then
        assertThat(page.getContent()).hasSize(5);  // 나머지 5개
        assertThat(page.getNumber()).isEqualTo(1);
        assertThat(page.isFirst()).isFalse();
        assertThat(page.isLast()).isTrue();
        assertThat(page.hasNext()).isFalse();
    }

    @Test
    @DisplayName("Soft Delete된 Order는 목록에서 제외되어야 한다")
    void loadAll_WithDeletedOrders_ShouldExcludeThem() {
        // Given
        OrderJpaEntity order1 = OrderJpaEntity.create(100L, "ORDER-001");
        OrderJpaEntity order2 = OrderJpaEntity.create(100L, "ORDER-002");
        order2.markAsDeleted();  // Soft Delete
        OrderJpaEntity order3 = OrderJpaEntity.create(100L, "ORDER-003");

        entityManager.persist(order1);
        entityManager.persist(order2);
        entityManager.persist(order3);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<OrderSummaryResponse> page = queryAdapter.loadAll(pageable);

        // Then
        assertThat(page.getContent()).hasSize(2);  // order2 제외
        assertThat(page.getContent())
            .extracting(OrderSummaryResponse::orderNumber)
            .containsExactlyInAnyOrder("ORDER-001", "ORDER-003");
    }
}
```

---

## 🧪 테스트 케이스 패턴

### 1. 단일 조회 테스트 (loadById)

```java
@Test
@DisplayName("ID로 Order 조회 시 DTO를 반환해야 한다")
void loadById_WithExistingOrder_ShouldReturnDTO() {
    // Given
    OrderJpaEntity entity = OrderJpaEntity.create(100L, "ORDER-001");
    entityManager.persist(entity);
    entityManager.flush();
    entityManager.clear();

    // When
    Optional<OrderDetailResponse> result =
        queryAdapter.loadById(OrderId.of(entity.getId()));

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().id()).isEqualTo(entity.getId());
}
```

### 2. Soft Delete 필터링 테스트

```java
@Test
@DisplayName("Soft Delete된 Order는 조회되지 않아야 한다")
void loadById_WithDeletedOrder_ShouldReturnEmpty() {
    // Given
    OrderJpaEntity entity = OrderJpaEntity.create(100L, "ORDER-001");
    entity.markAsDeleted();
    entityManager.persist(entity);
    entityManager.flush();

    // When
    Optional<OrderDetailResponse> result =
        queryAdapter.loadById(OrderId.of(entity.getId()));

    // Then
    assertThat(result).isEmpty();
}
```

### 3. 목록 조회 테스트 (loadByCustomerId)

```java
@Test
@DisplayName("Customer ID로 Order 목록 조회")
void loadByCustomerId_ShouldReturnList() {
    // Given
    OrderJpaEntity order1 = OrderJpaEntity.create(100L, "ORDER-001");
    OrderJpaEntity order2 = OrderJpaEntity.create(100L, "ORDER-002");

    entityManager.persist(order1);
    entityManager.persist(order2);
    entityManager.flush();

    // When
    List<OrderSummaryResponse> results =
        queryAdapter.loadByCustomerId(CustomerId.of(100L));

    // Then
    assertThat(results).hasSize(2);
}
```

### 4. 페이징 테스트 (loadAll)

```java
@Test
@DisplayName("페이징 조회")
void loadAll_WithPageable_ShouldReturnPage() {
    // Given
    for (int i = 1; i <= 15; i++) {
        OrderJpaEntity order = OrderJpaEntity.create(100L, "ORDER-" + i);
        entityManager.persist(order);
    }
    entityManager.flush();

    Pageable pageable = PageRequest.of(0, 10);

    // When
    Page<OrderSummaryResponse> page = queryAdapter.loadAll(pageable);

    // Then
    assertThat(page.getContent()).hasSize(10);
    assertThat(page.getTotalElements()).isEqualTo(15);
}
```

---

## 🔧 테스트 설정

### @DataJpaTest 설정

```java
@DataJpaTest
@Import({OrderQueryAdapter.class, QueryDslConfig.class})
@Tag("unit")
@Tag("query")
class OrderQueryAdapterTest {
    // ...
}
```

**설명**:
- `@DataJpaTest`: JPA 관련 Bean만 로드 (빠른 실행)
- `@Import`: Query Adapter와 QueryDslConfig 로드
- `@Tag("unit")`: 단위 테스트 태그
- `@Tag("query")`: Query 테스트 태그 (Command와 분리)

### H2 In-Memory DB 설정

```yaml
# src/test/resources/application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        format_sql: true
        show_sql: true
```

---

## 📊 EntityManager 활용

### Flush & Clear 패턴

```java
@Test
void loadById_ShouldQueryFromDatabase() {
    // Given
    OrderJpaEntity entity = OrderJpaEntity.create(100L, "ORDER-001");
    entityManager.persist(entity);
    entityManager.flush();  // DB에 강제 저장
    entityManager.clear();  // 1차 캐시 초기화

    // When - DB에서 다시 조회 (캐시 미사용)
    Optional<OrderDetailResponse> result =
        queryAdapter.loadById(OrderId.of(entity.getId()));

    // Then
    assertThat(result).isPresent();
}
```

**설명**:
- `flush()`: 영속성 컨텍스트 변경 내용을 DB에 반영
- `clear()`: 1차 캐시 초기화 (실제 Query 실행 검증)

---

## 🚨 Do / Don't

### ❌ Bad Examples

```java
// ❌ Domain Model 검증 (Query는 DTO만!)
@Test
void loadById_ShouldReturnDomain() {
    Order order = queryAdapter.loadById(OrderId.of(1L));  // Domain 반환 금지!
}

// ❌ Command 테스트 포함 (Query Adapter는 조회만!)
@Test
void save_WithNewOrder_ShouldPersist() {
    // Command 테스트는 Command Adapter Test로!
}

// ❌ @SpringBootTest 사용 (무겁고 느림)
@SpringBootTest
class OrderQueryAdapterTest {
    // @DataJpaTest 사용!
}

// ❌ @Tag 누락
@DataJpaTest
class OrderQueryAdapterTest {
    // @Tag("unit"), @Tag("query") 필수!
}

// ❌ JpaRepository 검증 (Query Adapter는 QueryDSL만!)
@Test
void findById_ShouldReturnEntity() {
    OrderJpaEntity entity = jpaRepository.findById(1L).get();  // Query Adapter는 JPAQueryFactory만!
}
```

### ✅ Good Examples

```java
// ✅ @DataJpaTest + @Tag
@DataJpaTest
@Import({OrderQueryAdapter.class, QueryDslConfig.class})
@Tag("unit")
@Tag("query")
class OrderQueryAdapterTest {
    // ...
}

// ✅ DTO 직접 검증
@Test
void loadById_ShouldReturnDTO() {
    Optional<OrderDetailResponse> result = queryAdapter.loadById(OrderId.of(1L));
    assertThat(result).isPresent();
    assertThat(result.get().id()).isEqualTo(1L);
}

// ✅ Soft Delete 필터링 검증
@Test
void loadById_WithDeletedOrder_ShouldReturnEmpty() {
    OrderJpaEntity entity = OrderJpaEntity.create(100L, "ORDER-001");
    entity.markAsDeleted();
    entityManager.persist(entity);

    Optional<OrderDetailResponse> result = queryAdapter.loadById(OrderId.of(entity.getId()));
    assertThat(result).isEmpty();
}

// ✅ EntityManager flush & clear
@Test
void loadAll_ShouldQueryFromDatabase() {
    OrderJpaEntity entity = OrderJpaEntity.create(100L, "ORDER-001");
    entityManager.persist(entity);
    entityManager.flush();
    entityManager.clear();

    List<OrderSummaryResponse> results = queryAdapter.loadByCustomerId(CustomerId.of(100L));
    assertThat(results).hasSize(1);
}
```

---

## 📋 체크리스트

Query Adapter 테스트 작성 시:
- [ ] `@DataJpaTest` 사용
- [ ] `@Import(QueryAdapter, QueryDslConfig)` 설정
- [ ] `@Tag("unit")`, `@Tag("query")` 필수
- [ ] H2 In-Memory DB 사용
- [ ] DTO 직접 검증 (Domain Model 아님)
- [ ] Soft Delete 필터링 검증 (`deletedAt IS NULL`)
- [ ] 페이징 검증 (totalElements, totalPages)
- [ ] Command 테스트 없음
- [ ] EntityManager flush & clear 활용

---

## 📖 관련 문서

- **[Query Adapter Implementation](../query-adapter-patterns/03_query-adapter-implementation.md)** - Query Adapter 구현
- **[QueryDSL DTO Projection](../query-adapter-patterns/02_querydsl-dto-projection.md)** - DTO Projection 패턴
- **[Command Adapter Unit Testing](./01_command-adapter-unit-testing.md)** - Command 테스트 비교
- **[Testcontainers Integration](./03_testcontainers-integration.md)** - 통합 테스트
- **[Test Tags Strategy](./04_test-tags-strategy.md)** - 테스트 태그 전략

---

**작성자**: Development Team
**최종 수정일**: 2025-11-04
**버전**: 1.0.0
