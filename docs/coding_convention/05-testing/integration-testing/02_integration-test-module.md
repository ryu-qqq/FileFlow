# Integration Test 모듈 구성 가이드

> **목적**: 별도 `integration-test` 모듈을 통한 E2E 통합 테스트 환경 구축

---

## 1️⃣ 왜 별도 모듈인가?

### 기존 방식의 한계

```
adapter-in/rest-api/src/test/
├── unit/               # 단위 테스트
└── integration/        # 통합 테스트 ← 같은 모듈에 혼재
```

**문제점**:
- 단위 테스트와 통합 테스트 실행 시간 혼재
- 테스트 설정 충돌 (H2 vs TestContainers)
- 공통 설정/Fixture 중복
- CI/CD 파이프라인에서 분리 실행 어려움

### 별도 모듈 방식

```
project/
├── domain/
├── application/
├── adapter-in/rest-api/
├── adapter-out/persistence-mysql/
├── bootstrap/bootstrap-web-api/
└── integration-test/              ⭐ 독립 모듈
    ├── build.gradle
    └── src/test/
        ├── java/
        │   └── config/            # 테스트 설정
        │   └── {domain}/          # 도메인별 테스트
        └── resources/
            ├── application-test.yml
            └── sql/               # cleanup 스크립트
```

**장점**:
- 전체 스택 통합 테스트 격리
- 명확한 책임 분리
- CI/CD에서 선택적 실행 (`./gradlew :integration-test:test`)
- 공통 설정 중앙화

---

## 2️⃣ 모듈 구조

### 디렉토리 레이아웃

```
integration-test/
├── build.gradle
└── src/test/
    ├── java/
    │   └── com/ryuqq/setof/integration/
    │       ├── config/
    │       │   └── IntegrationTestConfig.java      # 외부 서비스 Mock
    │       ├── product/
    │       │   ├── ProductCrudIntegrationTest.java
    │       │   ├── ProductSearchIntegrationTest.java
    │       │   └── fixture/
    │       │       └── ProductIntegrationTestFixture.java
    │       ├── order/
    │       │   ├── OrderFlowIntegrationTest.java
    │       │   └── fixture/
    │       │       └── OrderIntegrationTestFixture.java
    │       └── category/
    │           ├── CategoryCrudIntegrationTest.java
    │           └── fixture/
    │               └── CategoryIntegrationTestFixture.java
    └── resources/
        ├── application-test.yml
        └── sql/
            ├── product/
            │   └── cleanup.sql
            ├── order/
            │   └── cleanup.sql
            └── category/
                └── cleanup.sql
```

---

## 3️⃣ build.gradle 설정

### integration-test/build.gradle

```gradle
plugins {
    id 'java'
}

group = 'com.ryuqq.setof'
version = '1.0.0-SNAPSHOT'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    // ========================================
    // Spring Boot 테스트 스택
    // ========================================
    testImplementation libs.spring.boot.starter.web
    testImplementation libs.spring.boot.starter.test
    testImplementation libs.spring.boot.starter.data.jpa

    // ========================================
    // 테스트 프레임워크
    // ========================================
    testImplementation libs.junit.jupiter
    testImplementation libs.assertj.core
    testImplementation libs.mockito.core

    // ========================================
    // Database
    // ========================================
    // Option A: H2 인메모리 (빠른 실행, MySQL 호환 모드)
    testRuntimeOnly libs.h2database

    // Option B: Testcontainers MySQL (실제 MySQL, 높은 신뢰도)
    // testImplementation libs.testcontainers.mysql
    // testImplementation libs.testcontainers.junit

    // MySQL Driver (Classpath 로딩 시 필요)
    testRuntimeOnly libs.mysql.connector.java

    // ========================================
    // WireMock (외부 API 모킹)
    // ========================================
    testImplementation libs.wiremock

    // ========================================
    // 프로젝트 모듈 의존성
    // ========================================
    testImplementation project(':adapter-in:rest-api')
    testImplementation project(':adapter-in:rest-api-admin')
    testImplementation project(':application')
    testImplementation project(':domain')
    testImplementation project(':adapter-out:persistence-mysql')
    testImplementation project(':adapter-out:persistence-redis')
    testImplementation project(':bootstrap:bootstrap-web-api')

    // ========================================
    // Test Fixtures (Domain/Application Fixture 재사용)
    // ========================================
    testImplementation testFixtures(project(':domain'))
    testImplementation testFixtures(project(':application'))
}

tasks.named('test') {
    useJUnitPlatform()

    // 병렬 실행 (선택적)
    maxParallelForks = Runtime.runtime.availableProcessors().intdiv(2) ?: 1

    // 테스트 로깅
    testLogging {
        events "passed", "skipped", "failed"
        showStandardStreams = false
    }
}
```

### settings.gradle 추가

```gradle
// settings.gradle (프로젝트 루트)
include 'integration-test'
```

### libs.versions.toml 추가 (필요 시)

```toml
[versions]
wiremock = "3.9.1"
h2database = "2.2.224"

[libraries]
wiremock = { module = "org.wiremock:wiremock-standalone", version.ref = "wiremock" }
h2database = { module = "com.h2database:h2", version.ref = "h2database" }
```

---

## 4️⃣ application-test.yml 설정

### H2 MySQL 호환 모드 (권장: 빠른 실행)

```yaml
# integration-test/src/test/resources/application-test.yml

spring:
  # ========================================
  # DataSource (H2 MySQL 호환 모드)
  # ========================================
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;DATABASE_TO_LOWER=TRUE
    username: sa
    password:

    hikari:
      maximum-pool-size: 5
      minimum-idle: 2
      connection-timeout: 30000
      pool-name: IntegrationTestPool

  # ========================================
  # JPA/Hibernate
  # ========================================
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: create-drop            # 매 테스트마다 스키마 재생성
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
        dialect: org.hibernate.dialect.H2Dialect
    show-sql: true

  # ========================================
  # Flyway 비활성화 (H2는 ddl-auto 사용)
  # ========================================
  flyway:
    enabled: false

  # ========================================
  # 외부 설정 Import
  # ========================================
  config:
    import:
      - classpath:rest-api.yml

# ========================================
# 보안 비활성화 (테스트 환경)
# ========================================
security:
  service-token:
    enabled: false
    secret: test-secret

  gateway:
    enabled: false
    header-name: X-Gateway-Auth
    user-id-header: X-User-Id
    user-roles-header: X-User-Roles

# ========================================
# 로깅
# ========================================
logging:
  level:
    root: INFO
    com.ryuqq: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### Testcontainers MySQL 사용 시 (높은 신뢰도)

```yaml
# Testcontainers 사용 시 application-test.yml
spring:
  datasource:
    # Testcontainers가 동적으로 설정
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: validate              # Flyway가 스키마 관리
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect

  flyway:
    enabled: true                     # Flyway 활성화
    locations: classpath:db/migration
```

---

## 5️⃣ IntegrationTestConfig (외부 서비스 Mock)

### 기본 설정

```java
package com.ryuqq.setof.integration.config;

import com.ryuqq.setof.application.common.port.out.EmailSendPort;
import com.ryuqq.setof.application.common.port.out.FileStoragePort;
import com.ryuqq.setof.application.common.port.out.PaymentGatewayPort;
import com.ryuqq.setof.application.common.port.out.NotificationPort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

/**
 * 통합 테스트 환경 설정
 *
 * <p>외부 서비스(Email, S3, 결제 등)를 Mock으로 대체하여
 * 통합 테스트 시 외부 의존성 없이 실행 가능하게 함</p>
 *
 * @author Development Team
 * @since 1.0.0
 */
@TestConfiguration
public class IntegrationTestConfig {

    // ========================================
    // 이메일 서비스 Mock
    // ========================================
    @Bean
    @Primary
    public EmailSendPort emailSendPort() {
        return mock(EmailSendPort.class);
    }

    // ========================================
    // 파일 저장소 Mock (S3)
    // ========================================
    @Bean
    @Primary
    public FileStoragePort fileStoragePort() {
        return mock(FileStoragePort.class);
    }

    // ========================================
    // 결제 게이트웨이 Mock
    // ========================================
    @Bean
    @Primary
    public PaymentGatewayPort paymentGatewayPort() {
        return mock(PaymentGatewayPort.class);
    }

    // ========================================
    // 알림 서비스 Mock (FCM, SMS 등)
    // ========================================
    @Bean
    @Primary
    public NotificationPort notificationPort() {
        return mock(NotificationPort.class);
    }
}
```

---

## 6️⃣ 테스트 클래스 템플릿

### 기본 구조

```java
package com.ryuqq.setof.integration.product;

import com.ryuqq.setof.SetofCommerceApplication;
import com.ryuqq.setof.adapter.in.rest.common.dto.ApiResponse;
import com.ryuqq.setof.integration.config.IntegrationTestConfig;
import com.ryuqq.setof.integration.product.fixture.ProductIntegrationTestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 상품 CRUD 통합 테스트
 *
 * @author Development Team
 * @since 1.0.0
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = SetofCommerceApplication.class
)
@Import(IntegrationTestConfig.class)
@ActiveProfiles("test")
@Sql(
    scripts = "/sql/product/cleanup.sql",
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
@DisplayName("상품 CRUD 통합 테스트")
class ProductCrudIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1/products";
    }

    // ========================================
    // TC-001: 상품 생성 - 성공
    // ========================================
    @Test
    @DisplayName("TC-001: 상품 생성 - 성공")
    void createProduct_success() {
        // given
        var request = ProductIntegrationTestFixture.createProductRequest();

        // when
        ResponseEntity<ApiResponse<ProductApiResponse>> response = restTemplate.exchange(
            baseUrl(),
            HttpMethod.POST,
            new HttpEntity<>(request),
            new ParameterizedTypeReference<>() {}
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().id()).isNotNull();
    }

    // ========================================
    // TC-002: 상품 조회 - 성공
    // ========================================
    @Test
    @DisplayName("TC-002: 상품 조회 - 성공")
    void getProduct_success() {
        // given - 먼저 상품 생성
        var createRequest = ProductIntegrationTestFixture.createProductRequest();
        ResponseEntity<ApiResponse<ProductApiResponse>> createResponse = restTemplate.exchange(
            baseUrl(),
            HttpMethod.POST,
            new HttpEntity<>(createRequest),
            new ParameterizedTypeReference<>() {}
        );
        Long productId = createResponse.getBody().data().id();

        // when - 상품 조회
        ResponseEntity<ApiResponse<ProductApiResponse>> response = restTemplate.exchange(
            baseUrl() + "/" + productId,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {}
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data().id()).isEqualTo(productId);
    }

    // ========================================
    // TC-003: 존재하지 않는 상품 조회 - 404
    // ========================================
    @Test
    @DisplayName("TC-003: 존재하지 않는 상품 조회 - 404 NOT_FOUND")
    void getProduct_notFound_returns404() {
        // when
        ResponseEntity<ProblemDetail> response = restTemplate.exchange(
            baseUrl() + "/999999",
            HttpMethod.GET,
            null,
            ProblemDetail.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
```

---

## 7️⃣ Cleanup SQL 패턴

### 기본 cleanup.sql

```sql
-- integration-test/src/test/resources/sql/product/cleanup.sql

-- 테스트 후 데이터 정리
-- 외래키 순서 고려: 자식 → 부모

DELETE FROM product_stock WHERE 1=1;
DELETE FROM product_option WHERE 1=1;
DELETE FROM product_image WHERE 1=1;
DELETE FROM product WHERE 1=1;
DELETE FROM product_group WHERE 1=1;
```

### @Sql 사용 패턴

```java
// AFTER_TEST_METHOD: 각 테스트 완료 후 정리
@Sql(
    scripts = "/sql/product/cleanup.sql",
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)

// BEFORE_TEST_METHOD + AFTER_TEST_METHOD: 시작 전/후 모두 정리
@Sql(scripts = "/sql/product/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/product/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)

// 특정 테스트에만 다른 SQL 적용
@Test
@Sql(scripts = "/sql/product/setup-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/product/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
void testWithPresetData() { }
```

---

## 8️⃣ H2 vs Testcontainers 선택 가이드

### 비교표

| 항목 | H2 (MySQL 호환) | Testcontainers MySQL |
|-----|----------------|---------------------|
| **실행 속도** | ⚡ 매우 빠름 (초) | 🐢 느림 (10-30초) |
| **신뢰도** | 중간 (호환성 이슈 가능) | 높음 (실제 MySQL) |
| **설정 복잡도** | 낮음 | 중간 (Docker 필요) |
| **CI/CD** | 어디서나 실행 | Docker 필요 |
| **SQL 호환성** | 대부분 호환 | 100% 호환 |
| **권장 시점** | 개발 중 빠른 피드백 | CI/CD, 릴리스 검증 |

### 권장 전략

```yaml
Local Development:
  database: H2 (MySQL 모드)
  reason: 빠른 피드백 루프

CI Pipeline:
  database: Testcontainers MySQL
  reason: 높은 신뢰도, 운영 환경과 동일

Pre-Release:
  database: Testcontainers MySQL
  reason: 최종 검증
```

---

## 9️⃣ 체크리스트

### 모듈 설정
- [ ] `settings.gradle`에 `include 'integration-test'` 추가
- [ ] `build.gradle` 의존성 설정 완료
- [ ] `application-test.yml` 생성

### 디렉토리 구조
- [ ] `src/test/java/.../config/IntegrationTestConfig.java` 생성
- [ ] `src/test/resources/sql/` cleanup 스크립트 준비
- [ ] 도메인별 테스트 패키지 구성

### 테스트 클래스
- [ ] `@SpringBootTest(webEnvironment = RANDOM_PORT)` 사용
- [ ] `@Import(IntegrationTestConfig.class)` 추가
- [ ] `@ActiveProfiles("test")` 추가
- [ ] `@Sql(executionPhase = AFTER_TEST_METHOD)` 추가
- [ ] `TestRestTemplate` 사용 (MockMvc 금지)

### CI/CD
- [ ] `./gradlew :integration-test:test` 실행 확인
- [ ] 필요 시 Docker 환경 준비 (Testcontainers)

---

## 📖 관련 문서

- **[External Service Mock Guide](./03_external-service-mock.md)** - Mockito + WireMock 상세
- **[Integration Test Fixture](./04_integration-test-fixture.md)** - Fixture 패턴 상세
- **[Integration Testing Overview](./01_integration-testing-overview.md)** - 개념 및 TestContainers 방식

---

**작성자**: Development Team
**최종 수정일**: 2025-12-23
**버전**: 1.0.0
