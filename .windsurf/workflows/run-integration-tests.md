---
description: 통합 테스트 실행 (Testcontainers + DB)
---

# Run Integration Tests

**🎯 역할**: 통합 테스트 실행 (DB, 외부 의존성)

**📋 도구**: Gradle + Testcontainers

## What It Does

Testcontainers를 사용한 실제 DB 통합 테스트:

1. ✅ **Testcontainers** - Docker 기반 테스트 환경
2. ✅ **Real DB** - 실제 MySQL 컨테이너 사용
3. ✅ **JPA Tests** - Repository, Query 테스트
4. ✅ **Transaction** - 트랜잭션 롤백 테스트

## Usage

### 전체 통합 테스트

```bash
./gradlew integrationTest
```

### 특정 모듈만

```bash
./gradlew :adapter-out-persistence:integrationTest
```

### Profile 지정

```bash
./gradlew integrationTest -Pspring.profiles.active=test
```

## Cascade에서 실행

```
/run-integration-tests
```

## Test Configuration

```java
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class OrderRepositoryIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @Test
    void shouldSaveOrder() {
        // given
        Order order = Order.create(...);

        // when
        Order saved = orderRepository.save(order);

        // then
        assertThat(saved.getId()).isNotNull();
    }
}
```

## Output

**성공 시**:
```
✅ Integration tests passed
Tests: 42 passed
Duration: 30s

Containers started:
  - MySQL 8.0
  - Redis 7.0
```

**실패 시**:
```
❌ Integration tests failed
Failed: OrderRepositoryTest.shouldFindByStatus

Error: Connection refused to MySQL container

Check logs: build/reports/tests/integrationTest/index.html
```

## Testcontainers Setup

`build.gradle.kts`:

```kotlin
dependencies {
    testImplementation("org.testcontainers:testcontainers:1.19.0")
    testImplementation("org.testcontainers:mysql:1.19.0")
    testImplementation("org.testcontainers:junit-jupiter:1.19.0")
}
```

## Best Practices

1. **@Transactional** - 테스트 후 자동 롤백
2. **@Sql** - 테스트 데이터 초기화
3. **@DirtiesContext** - 컨텍스트 재생성 (필요시)
4. **Cleanup** - 테스트 후 데이터 정리

## Related

- **Gradle**: `./gradlew integrationTest`
- **Tests**: `*/src/test/java/**/*IntegrationTest.java`
- **Testcontainers**: https://www.testcontainers.org/
- **Unit Tests**: `run-unit-tests.md`
