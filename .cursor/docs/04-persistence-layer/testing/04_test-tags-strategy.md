# Test Tags Strategy (테스트 태그 전략)

**목적**: JUnit 5 @Tag를 사용한 테스트 분류 및 실행 전략 정의

**위치**: 전체 프로젝트 테스트 코드

**필수 버전**: Java 21+, JUnit 5.10+

---

## 🎯 핵심 원칙

### 테스트 태그 전략

테스트 태그는 **테스트 유형별 선택적 실행**을 위한 분류 체계입니다:

```
태그 체계:
1. 실행 범위: @Tag("unit"), @Tag("integration"), @Tag("e2e")
2. CQRS 분리: @Tag("command"), @Tag("query")
3. 특수 목적: @Tag("performance"), @Tag("security")
```

**규칙**:
- ✅ 모든 테스트 클래스에 최소 2개 태그 필수
- ✅ 실행 범위 태그 1개 + CQRS 태그 1개
- ✅ 명확한 네이밍 규칙 (소문자, 하이픈 없음)
- ❌ 임의의 태그 생성 금지 (표준 태그만 사용)

---

## 📋 표준 태그 정의

### 1. 실행 범위 태그 (Scope Tags)

| 태그 | 목적 | 실행 환경 |
|------|------|----------|
| `@Tag("unit")` | 단위 테스트 (격리된 테스트) | @DataJpaTest, Mockito |
| `@Tag("integration")` | 통합 테스트 (실제 인프라) | @SpringBootTest, Testcontainers |
| `@Tag("e2e")` | E2E 테스트 (전체 시스템) | @SpringBootTest, MockMvc/RestAssured |
| `@Tag("performance")` | 성능 테스트 (응답 시간, TPS) | 대용량 데이터, 부하 테스트 |

### 2. CQRS 태그 (CQRS Tags)

| 태그 | 목적 | 테스트 대상 |
|------|------|------------|
| `@Tag("command")` | Command 테스트 (쓰기) | SavePort, DeletePort, CommandAdapter |
| `@Tag("query")` | Query 테스트 (읽기) | LoadPort, QueryAdapter, DTO Projection |

### 3. 특수 목적 태그 (Special Tags)

| 태그 | 목적 | 사용 시나리오 |
|------|------|--------------|
| `@Tag("security")` | 보안 테스트 | 인증/인가, 암호화 검증 |
| `@Tag("archunit")` | 아키텍처 테스트 | 레이어 의존성, 네이밍 규칙 |
| `@Tag("slow")` | 느린 테스트 (>5초) | CI 제외, 야간 실행 |

---

## 🧪 태그 적용 예시

### Command Adapter 단위 테스트

```java
@DataJpaTest
@Import({OrderCommandAdapter.class, OrderEntityMapperImpl.class})
@Tag("unit")         // 실행 범위: 단위 테스트
@Tag("command")      // CQRS: Command 테스트
@DisplayName("Order Command Adapter 단위 테스트")
class OrderCommandAdapterTest {
    // Command 테스트 (save, softDelete, restore)
}
```

### Query Adapter 단위 테스트

```java
@DataJpaTest
@Import({OrderQueryAdapter.class, QueryDslConfig.class})
@Tag("unit")         // 실행 범위: 단위 테스트
@Tag("query")        // CQRS: Query 테스트
@DisplayName("Order Query Adapter 단위 테스트")
class OrderQueryAdapterTest {
    // Query 테스트 (loadById, loadAll)
}
```

### Command Adapter 통합 테스트

```java
@SpringBootTest
@Import(TestcontainersConfig.class)
@Tag("integration")  // 실행 범위: 통합 테스트
@Tag("command")      // CQRS: Command 테스트
@DisplayName("Order Command Adapter 통합 테스트")
class OrderCommandAdapterIntegrationTest {
    // Testcontainers 기반 Command 통합 테스트
}
```

### Query Adapter 통합 테스트

```java
@SpringBootTest
@Import(TestcontainersConfig.class)
@Tag("integration")  // 실행 범위: 통합 테스트
@Tag("query")        // CQRS: Query 테스트
@DisplayName("Order Query Adapter 통합 테스트")
class OrderQueryAdapterIntegrationTest {
    // Testcontainers 기반 Query 통합 테스트
}
```

### 성능 테스트

```java
@SpringBootTest
@Import(TestcontainersConfig.class)
@Tag("performance")  // 특수 목적: 성능 테스트
@Tag("query")        // CQRS: Query 테스트
@DisplayName("Order Query 성능 테스트")
class OrderQueryPerformanceTest {
    // 대용량 데이터 성능 검증
}
```

---

## 🔧 Gradle 설정

### build.gradle 테스트 필터링

```gradle
test {
    useJUnitPlatform {
        // 기본 실행: 단위 테스트만
        includeTags 'unit'
    }
}

// 통합 테스트 실행 Task
tasks.register('integrationTest', Test) {
    useJUnitPlatform {
        includeTags 'integration'
    }
    shouldRunAfter test
}

// Command 테스트만 실행
tasks.register('commandTest', Test) {
    useJUnitPlatform {
        includeTags 'command'
    }
}

// Query 테스트만 실행
tasks.register('queryTest', Test) {
    useJUnitPlatform {
        includeTags 'query'
    }
}

// 성능 테스트 실행
tasks.register('performanceTest', Test) {
    useJUnitPlatform {
        includeTags 'performance'
    }
}

// 전체 테스트 실행 (unit + integration)
tasks.register('fullTest', Test) {
    useJUnitPlatform {
        includeTags 'unit', 'integration'
    }
}

// CI 파이프라인용 (통합 제외, 느린 테스트 제외)
tasks.register('ciTest', Test) {
    useJUnitPlatform {
        includeTags 'unit'
        excludeTags 'slow'
    }
}
```

---

## 🚀 실행 전략

### 1. 로컬 개발 (빠른 피드백)

```bash
# 단위 테스트만 실행 (빠름, 2-5초)
./gradlew test

# Command 단위 테스트만 실행
./gradlew commandTest --tests "*CommandAdapterTest"

# Query 단위 테스트만 실행
./gradlew queryTest --tests "*QueryAdapterTest"
```

### 2. PR 검증 (CI 파이프라인)

```bash
# CI 테스트 (단위 + 빠른 통합)
./gradlew ciTest

# 또는 단계별 실행
./gradlew test              # 단위 테스트
./gradlew integrationTest   # 통합 테스트
```

### 3. 배포 전 검증 (전체 테스트)

```bash
# 전체 테스트 실행 (unit + integration + performance)
./gradlew fullTest

# 성능 테스트 별도 실행
./gradlew performanceTest
```

### 4. 특정 Layer 테스트

```bash
# Persistence Layer Command 테스트만
./gradlew test --tests "com.company.adapter.out.persistence.*CommandAdapterTest"

# Persistence Layer Query 테스트만
./gradlew test --tests "com.company.adapter.out.persistence.*QueryAdapterTest"
```

---

## 📊 CI/CD 파이프라인 통합

### GitHub Actions 예시

```yaml
name: Test Pipeline

on:
  pull_request:
    branches: [ main, develop ]

jobs:
  unit-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
      - name: Run Unit Tests
        run: ./gradlew test

  integration-test:
    runs-on: ubuntu-latest
    needs: unit-test
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
      - name: Run Integration Tests
        run: ./gradlew integrationTest

  performance-test:
    runs-on: ubuntu-latest
    needs: integration-test
    if: github.event_name == 'push' && github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
      - name: Run Performance Tests
        run: ./gradlew performanceTest
```

---

## 🎯 태그 조합 패턴

### 권장 조합

| 테스트 유형 | 태그 조합 | 예시 클래스 |
|------------|---------|-----------|
| Command 단위 테스트 | `@Tag("unit")` + `@Tag("command")` | OrderCommandAdapterTest |
| Query 단위 테스트 | `@Tag("unit")` + `@Tag("query")` | OrderQueryAdapterTest |
| Command 통합 테스트 | `@Tag("integration")` + `@Tag("command")` | OrderCommandAdapterIntegrationTest |
| Query 통합 테스트 | `@Tag("integration")` + `@Tag("query")` | OrderQueryAdapterIntegrationTest |
| Query 성능 테스트 | `@Tag("performance")` + `@Tag("query")` | OrderQueryPerformanceTest |

### 비권장 조합

```java
// ❌ 잘못된 예: 실행 범위 태그 누락
@Tag("command")
class OrderCommandAdapterTest { }

// ❌ 잘못된 예: CQRS 태그 누락
@Tag("unit")
class OrderCommandAdapterTest { }

// ❌ 잘못된 예: 임의의 태그 사용
@Tag("my-custom-tag")
class OrderCommandAdapterTest { }

// ❌ 잘못된 예: Command/Query 동시 태깅
@Tag("command")
@Tag("query")  // Command와 Query는 별도 클래스로 분리!
class OrderAdapterTest { }
```

---

## 🔍 IntelliJ IDEA 실행 설정

### Run Configuration 생성

1. **Edit Configurations...**
2. **Add New Configuration** → **JUnit**
3. **Test kind**: Tags
4. **Tag expression** 입력:

```
# Command 단위 테스트만
unit & command

# Query 통합 테스트만
integration & query

# 전체 단위 테스트
unit

# 통합 테스트 제외
unit & !integration
```

---

## 📋 체크리스트

테스트 태그 작성 시:
- [ ] 실행 범위 태그 1개 (`unit`, `integration`, `e2e`, `performance`)
- [ ] CQRS 태그 1개 (`command`, `query`)
- [ ] 표준 태그만 사용 (임의 태그 금지)
- [ ] 클래스 레벨 적용 (메서드 레벨 아님)
- [ ] 명확한 `@DisplayName` 작성
- [ ] Gradle 필터링 설정 확인

---

## 🚨 Do / Don't

### ❌ Bad Examples

```java
// ❌ 태그 누락
@DataJpaTest
class OrderCommandAdapterTest {
    // @Tag("unit"), @Tag("command") 필수!
}

// ❌ 임의의 태그 사용
@DataJpaTest
@Tag("my-test")
@Tag("order")
class OrderCommandAdapterTest {
    // 표준 태그만 사용!
}

// ❌ 메서드 레벨 태그 (클래스 레벨로 통일)
@DataJpaTest
@Tag("unit")
class OrderCommandAdapterTest {
    @Test
    @Tag("command")  // 클래스 레벨로!
    void save_ShouldPersist() { }
}

// ❌ Command/Query 혼재
@DataJpaTest
@Tag("unit")
@Tag("command")
@Tag("query")  // 하나만 선택!
class OrderAdapterTest { }
```

### ✅ Good Examples

```java
// ✅ Command 단위 테스트
@DataJpaTest
@Import({OrderCommandAdapter.class, OrderEntityMapperImpl.class})
@Tag("unit")
@Tag("command")
class OrderCommandAdapterTest {
    // ...
}

// ✅ Query 단위 테스트
@DataJpaTest
@Import({OrderQueryAdapter.class, QueryDslConfig.class})
@Tag("unit")
@Tag("query")
class OrderQueryAdapterTest {
    // ...
}

// ✅ Query 통합 테스트
@SpringBootTest
@Import(TestcontainersConfig.class)
@Tag("integration")
@Tag("query")
class OrderQueryAdapterIntegrationTest {
    // ...
}

// ✅ 성능 테스트
@SpringBootTest
@Import(TestcontainersConfig.class)
@Tag("performance")
@Tag("query")
class OrderQueryPerformanceTest {
    // ...
}
```

---

## 📖 관련 문서

- **[Command Adapter Unit Testing](./01_command-adapter-unit-testing.md)** - Command 단위 테스트
- **[Query Adapter Unit Testing](./02_query-adapter-unit-testing.md)** - Query 단위 테스트
- **[Testcontainers Integration](./03_testcontainers-integration.md)** - 통합 테스트
- **[Command Adapter Implementation](../command-adapter-patterns/03_command-adapter-implementation.md)** - Command Adapter 구현
- **[Query Adapter Implementation](../query-adapter-patterns/03_query-adapter-implementation.md)** - Query Adapter 구현

---

**작성자**: Development Team
**최종 수정일**: 2025-11-04
**버전**: 1.0.0
