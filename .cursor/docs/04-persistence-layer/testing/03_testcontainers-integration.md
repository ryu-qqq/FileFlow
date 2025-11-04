# Testcontainers Integration Testing (통합 테스트)

**목적**: Testcontainers를 사용한 실제 데이터베이스 통합 테스트 전략

**위치**: `adapter-persistence/src/test/java/[module]/integration/`

**필수 버전**: Java 21+, Spring Boot 3.0+, Testcontainers 1.19+

---

## 🎯 핵심 원칙

### Testcontainers 통합 테스트 전략

Testcontainers는 **실제 데이터베이스 환경**에서 Adapter를 테스트합니다:

```
테스트 대상:
1. 실제 DB 연동 (MySQL, PostgreSQL 등)
2. DB 제약 조건 검증 (Unique, FK 등)
3. 트랜잭션 격리 수준 검증
4. 복잡한 Query 실행 및 성능 검증
5. Soft Delete 인덱스 동작 검증
6. Infrastructure 통합 (Redis, Kafka 등)
```

**규칙**:
- ✅ `@SpringBootTest` (전체 컨텍스트 로드)
- ✅ `@Testcontainers` + `@Container` 사용
- ✅ `@Tag("integration")` 필수
- ✅ Command/Query 분리: `@Tag("command")` 또는 `@Tag("query")`
- ✅ 실제 DB 제약 조건 검증
- ❌ H2 In-Memory DB 사용 금지 (실제 DB 사용)

---

## 📦 Testcontainers 설정

### 의존성 추가 (build.gradle)

```gradle
dependencies {
    // Testcontainers
    testImplementation 'org.testcontainers:testcontainers:1.19.3'
    testImplementation 'org.testcontainers:mysql:1.19.3'
    testImplementation 'org.testcontainers:postgresql:1.19.3'
    testImplementation 'org.testcontainers:kafka:1.19.3'

    // JUnit 5 통합
    testImplementation 'org.testcontainers:junit-jupiter:1.19.3'
}
```

### 기본 설정 클래스

```java
package com.company.adapter.out.persistence.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Testcontainers 공통 설정
 *
 * @author development-team
 * @since 1.0.0
 */
@Testcontainers
@TestConfiguration
public class TestcontainersConfig {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test")
        .withReuse(true);  // 컨테이너 재사용 (성능 향상)

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }
}
```

---

## 🧪 Command Adapter 통합 테스트

### Command Adapter Integration Test

```java
package com.company.adapter.out.persistence.order.integration;

import com.company.adapter.out.persistence.config.TestcontainersConfig;
import com.company.adapter.out.persistence.order.adapter.OrderCommandAdapter;
import com.company.adapter.out.persistence.order.entity.OrderJpaEntity;
import com.company.adapter.out.persistence.order.repository.OrderJpaRepository;
import com.company.application.order.port.out.SaveOrderPort;
import com.company.domain.order.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

/**
 * Order Command Adapter 통합 테스트 (Testcontainers)
 *
 * @author development-team
 * @since 1.0.0
 */
@SpringBootTest
@Import(TestcontainersConfig.class)
@Tag("integration")
@Tag("command")
@DisplayName("Order Command Adapter 통합 테스트")
class OrderCommandAdapterIntegrationTest {

    @Autowired
    private SaveOrderPort saveOrderPort;

    @Autowired
    private OrderJpaRepository jpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    @DisplayName("신규 Order 저장 시 실제 DB에 저장되어야 한다")
    void save_WithNewOrder_ShouldPersistToRealDatabase() {
        // Given
        Order order = Order.create(
            UserId.of(100L),
            OrderItems.of(
                OrderItem.of(ProductId.of(1L), Quantity.of(2))
            )
        );

        // When
        Order savedOrder = saveOrderPort.save(order);

        // Then
        assertThat(savedOrder.getId()).isNotNull();

        // 실제 DB 검증 (트랜잭션 커밋 후 확인)
        entityManager.flush();
        entityManager.clear();

        OrderJpaEntity entity = jpaRepository.findById(savedOrder.getId().getValue())
            .orElseThrow();
        assertThat(entity.getUserId()).isEqualTo(100L);
    }

    @Test
    @Transactional
    @DisplayName("Unique 제약 조건 위반 시 예외가 발생해야 한다")
    void save_WithDuplicateOrderNumber_ShouldThrowException() {
        // Given - 첫 번째 Order 저장
        OrderJpaEntity entity1 = OrderJpaEntity.create(100L, "ORDER-001");
        jpaRepository.save(entity1);
        entityManager.flush();

        // When & Then - 동일한 orderNumber로 저장 시도
        OrderJpaEntity entity2 = OrderJpaEntity.create(200L, "ORDER-001");
        assertThatThrownBy(() -> {
            jpaRepository.save(entity2);
            entityManager.flush();  // Unique 제약 조건 위반
        })
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Duplicate entry");
    }

    @Test
    @Transactional
    @DisplayName("Soft Delete 후 복원 시 deletedAt이 null이 되어야 한다")
    void restore_WithDeletedOrder_ShouldClearDeletedAt() {
        // Given - Order 저장 및 Soft Delete
        OrderJpaEntity entity = OrderJpaEntity.create(100L, "ORDER-001");
        entity = jpaRepository.save(entity);
        entity.markAsDeleted();
        jpaRepository.save(entity);
        entityManager.flush();

        Long orderId = entity.getId();
        assertThat(entity.isDeleted()).isTrue();

        // When - 복원
        OrderJpaEntity deletedEntity = jpaRepository.findById(orderId).get();
        deletedEntity.restore();
        jpaRepository.save(deletedEntity);
        entityManager.flush();
        entityManager.clear();

        // Then
        OrderJpaEntity restoredEntity = jpaRepository.findById(orderId).get();
        assertThat(restoredEntity.isDeleted()).isFalse();
        assertThat(restoredEntity.getDeletedAt()).isNull();
    }
}
```

---

## 🔍 Query Adapter 통합 테스트

### Query Adapter Integration Test

```java
package com.company.adapter.out.persistence.order.integration;

import com.company.adapter.out.persistence.config.TestcontainersConfig;
import com.company.adapter.out.persistence.order.adapter.OrderQueryAdapter;
import com.company.adapter.out.persistence.order.entity.OrderJpaEntity;
import com.company.application.order.dto.response.OrderDetailResponse;
import com.company.application.order.dto.response.OrderSummaryResponse;
import com.company.application.order.port.out.LoadOrderPort;
import com.company.domain.order.CustomerId;
import com.company.domain.order.OrderId;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Order Query Adapter 통합 테스트 (Testcontainers)
 *
 * @author development-team
 * @since 1.0.0
 */
@SpringBootTest
@Import(TestcontainersConfig.class)
@Tag("integration")
@Tag("query")
@DisplayName("Order Query Adapter 통합 테스트")
class OrderQueryAdapterIntegrationTest {

    @Autowired
    private LoadOrderPort loadOrderPort;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    @DisplayName("실제 DB에서 DTO를 조회해야 한다")
    void loadById_WithRealDatabase_ShouldReturnDTO() {
        // Given
        OrderJpaEntity entity = OrderJpaEntity.create(100L, "ORDER-001");
        entityManager.persist(entity);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<OrderDetailResponse> result =
            loadOrderPort.loadById(OrderId.of(entity.getId()));

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().orderNumber()).isEqualTo("ORDER-001");
    }

    @Test
    @Transactional
    @DisplayName("Soft Delete 인덱스가 정상 동작해야 한다")
    void loadAll_WithDeletedAtIndex_ShouldFilterDeleted() {
        // Given - 3개 Order (1개 Soft Delete)
        OrderJpaEntity order1 = OrderJpaEntity.create(100L, "ORDER-001");
        OrderJpaEntity order2 = OrderJpaEntity.create(100L, "ORDER-002");
        order2.markAsDeleted();
        OrderJpaEntity order3 = OrderJpaEntity.create(100L, "ORDER-003");

        entityManager.persist(order1);
        entityManager.persist(order2);
        entityManager.persist(order3);
        entityManager.flush();
        entityManager.clear();

        // When
        List<OrderSummaryResponse> results =
            loadOrderPort.loadByCustomerId(CustomerId.of(100L));

        // Then - order2는 제외되어야 함
        assertThat(results).hasSize(2);
        assertThat(results)
            .extracting(OrderSummaryResponse::orderNumber)
            .containsExactlyInAnyOrder("ORDER-001", "ORDER-003");
    }

    @Test
    @Transactional
    @DisplayName("복잡한 페이징 쿼리가 실제 DB에서 정상 동작해야 한다")
    void loadAll_WithComplexPagination_ShouldWorkCorrectly() {
        // Given - 100개 Order 저장
        for (int i = 1; i <= 100; i++) {
            OrderJpaEntity order = OrderJpaEntity.create(
                100L,
                "ORDER-" + String.format("%03d", i)
            );
            entityManager.persist(order);
        }
        entityManager.flush();
        entityManager.clear();

        // When - 10개씩 페이징
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderSummaryResponse> page = loadOrderPort.loadAll(pageable);

        // Then
        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isEqualTo(100);
        assertThat(page.getTotalPages()).isEqualTo(10);
    }

    @Test
    @Transactional
    @DisplayName("Index를 사용한 쿼리가 성능 요구사항을 만족해야 한다")
    void loadByCustomerId_WithIndex_ShouldMeetPerformanceRequirements() {
        // Given - 10,000개 Order 저장
        for (int i = 1; i <= 10000; i++) {
            OrderJpaEntity order = OrderJpaEntity.create(
                i % 100,  // 100명의 Customer
                "ORDER-" + String.format("%05d", i)
            );
            entityManager.persist(order);

            if (i % 500 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
        entityManager.clear();

        // When - 특정 Customer의 Order 조회 (Index 사용)
        long startTime = System.currentTimeMillis();
        List<OrderSummaryResponse> results =
            loadOrderPort.loadByCustomerId(CustomerId.of(50L));
        long elapsedTime = System.currentTimeMillis() - startTime;

        // Then - 100ms 이내 응답 (Index 효과)
        assertThat(results).isNotEmpty();
        assertThat(elapsedTime).isLessThan(100);
    }
}
```

---

## 🗄️ 다양한 데이터베이스 지원

### PostgreSQL 설정

```java
@Testcontainers
@TestConfiguration
public class PostgresTestcontainersConfig {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test")
        .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

---

## 🔧 Infrastructure 통합 테스트

### Redis + Kafka 통합 설정

```java
@Testcontainers
@TestConfiguration
public class InfrastructureTestcontainersConfig {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withReuse(true);

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7")
        .withExposedPorts(6379)
        .withReuse(true);

    @Container
    static KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.4.0")
    ).withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // MySQL
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);

        // Redis
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);

        // Kafka
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }
}
```

---

## 📊 성능 벤치마크 테스트

### 대용량 데이터 성능 테스트

```java
@Test
@Transactional
@DisplayName("10만 건 데이터에서 페이징 성능 검증")
void loadAll_With100kRecords_ShouldMeetPerformanceRequirements() {
    // Given - 100,000개 Order 저장
    for (int i = 1; i <= 100000; i++) {
        OrderJpaEntity order = OrderJpaEntity.create(
            i % 1000,  // 1000명의 Customer
            "ORDER-" + String.format("%06d", i)
        );
        entityManager.persist(order);

        if (i % 1000 == 0) {
            entityManager.flush();
            entityManager.clear();
        }
    }
    entityManager.flush();
    entityManager.clear();

    // When - 10페이지 조회 (offset 100)
    Pageable pageable = PageRequest.of(10, 10);
    long startTime = System.currentTimeMillis();
    Page<OrderSummaryResponse> page = loadOrderPort.loadAll(pageable);
    long elapsedTime = System.currentTimeMillis() - startTime;

    // Then - 200ms 이내 응답
    assertThat(page.getContent()).hasSize(10);
    assertThat(elapsedTime).isLessThan(200);
}
```

---

## 🚨 Do / Don't

### ❌ Bad Examples

```java
// ❌ H2 In-Memory DB 사용 (Testcontainers 목적 위배)
@SpringBootTest
@Tag("integration")
class OrderCommandAdapterIntegrationTest {
    // Testcontainers 사용!
}

// ❌ @DataJpaTest 사용 (통합 테스트는 @SpringBootTest)
@DataJpaTest
@Testcontainers
class OrderCommandAdapterIntegrationTest {
    // @SpringBootTest 사용!
}

// ❌ @Tag 누락 또는 잘못된 태그
@SpringBootTest
@Import(TestcontainersConfig.class)
class OrderCommandAdapterIntegrationTest {
    // @Tag("integration"), @Tag("command") 필수!
}

// ❌ Command/Query 혼재 테스트
@SpringBootTest
@Tag("integration")
class OrderAdapterIntegrationTest {
    @Test
    void save_ShouldPersist() { }  // Command

    @Test
    void loadById_ShouldReturnDTO() { }  // Query

    // Command와 Query는 별도 테스트 클래스로 분리!
}
```

### ✅ Good Examples

```java
// ✅ Testcontainers + @Tag
@SpringBootTest
@Import(TestcontainersConfig.class)
@Tag("integration")
@Tag("command")
class OrderCommandAdapterIntegrationTest {
    // ...
}

// ✅ 실제 DB 제약 조건 검증
@Test
@Transactional
void save_WithDuplicateOrderNumber_ShouldThrowException() {
    OrderJpaEntity entity1 = OrderJpaEntity.create(100L, "ORDER-001");
    jpaRepository.save(entity1);
    entityManager.flush();

    OrderJpaEntity entity2 = OrderJpaEntity.create(200L, "ORDER-001");
    assertThatThrownBy(() -> {
        jpaRepository.save(entity2);
        entityManager.flush();
    }).hasMessageContaining("Duplicate entry");
}

// ✅ 성능 검증
@Test
@Transactional
void loadByCustomerId_WithIndex_ShouldMeetPerformanceRequirements() {
    // Given - 대량 데이터
    // When - Index 사용 쿼리
    long elapsedTime = System.currentTimeMillis() - startTime;
    // Then - 성능 요구사항 검증
    assertThat(elapsedTime).isLessThan(100);
}
```

---

## 📋 체크리스트

Testcontainers 통합 테스트 작성 시:
- [ ] `@SpringBootTest` 사용
- [ ] `@Testcontainers` + `@Import(TestcontainersConfig.class)` 설정
- [ ] `@Tag("integration")` 필수
- [ ] Command/Query 분리: `@Tag("command")` 또는 `@Tag("query")`
- [ ] 실제 DB 컨테이너 사용 (MySQL, PostgreSQL 등)
- [ ] DB 제약 조건 검증 (Unique, FK)
- [ ] 트랜잭션 격리 수준 검증
- [ ] 성능 요구사항 검증
- [ ] `withReuse(true)` 설정 (성능 향상)

---

## 📖 관련 문서

- **[Command Adapter Unit Testing](./01_command-adapter-unit-testing.md)** - Command 단위 테스트
- **[Query Adapter Unit Testing](./02_query-adapter-unit-testing.md)** - Query 단위 테스트
- **[Test Tags Strategy](./04_test-tags-strategy.md)** - 테스트 태그 전략
- **[Command Adapter Implementation](../command-adapter-patterns/03_command-adapter-implementation.md)** - Command Adapter 구현
- **[Query Adapter Implementation](../query-adapter-patterns/03_query-adapter-implementation.md)** - Query Adapter 구현

---

**작성자**: Development Team
**최종 수정일**: 2025-11-04
**버전**: 1.0.0
