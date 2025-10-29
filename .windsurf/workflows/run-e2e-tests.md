---
description: E2E 테스트 실행 (전체 시스템 통합)
---

# Run E2E Tests

**🎯 역할**: E2E (End-to-End) 테스트 실행

**📋 도구**: Gradle + RestAssured

## What It Does

전체 시스템을 통합한 E2E 테스트:

1. ✅ **Full Stack** - 실제 애플리케이션 실행
2. ✅ **REST API** - HTTP 요청/응답 테스트
3. ✅ **Business Flows** - 사용자 시나리오 테스트
4. ✅ **Data Validation** - 데이터 무결성 검증

## Usage

### 전체 E2E 테스트

```bash
# 애플리케이션 시작
./gradlew bootRun &
APP_PID=$!

# E2E 테스트 실행
./gradlew e2eTest

# 애플리케이션 종료
kill $APP_PID
```

### Docker Compose 사용

```bash
# 전체 환경 구성
docker-compose -f docker-compose.test.yml up -d

# E2E 테스트
./gradlew e2eTest

# 정리
docker-compose -f docker-compose.test.yml down
```

## Cascade에서 실행

```
/run-e2e-tests
```

## Test Example

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class OrderE2ETest {

    @LocalServerPort
    private int port;

    @Test
    void shouldCreateAndConfirmOrder() {
        // given: Create order
        String orderId = given()
            .port(port)
            .contentType("application/json")
            .body("""
                {
                  "customerId": 1,
                  "items": [{"productId": 100, "quantity": 2}]
                }
                """)
        .when()
            .post("/api/orders")
        .then()
            .statusCode(201)
            .extract().path("orderId");

        // when: Confirm order
        given()
            .port(port)
        .when()
            .post("/api/orders/" + orderId + "/confirm")
        .then()
            .statusCode(200);

        // then: Verify status
        given()
            .port(port)
        .when()
            .get("/api/orders/" + orderId)
        .then()
            .statusCode(200)
            .body("status", equalTo("CONFIRMED"));
    }
}
```

## Output

**성공 시**:
```
✅ E2E tests passed
Tests: 15 passed
Duration: 2m 30s

Scenarios tested:
  - Order creation and confirmation
  - Payment processing
  - Inventory update
```

**실패 시**:
```
❌ E2E test failed
Failed: shouldProcessPayment

Error: Expected status 200 but was 500

Response: {"error": "Insufficient inventory"}

Check logs: build/test-results/e2eTest/
```

## Dependencies

`build.gradle.kts`:

```kotlin
dependencies {
    testImplementation("io.rest-assured:rest-assured:5.3.0")
    testImplementation("io.rest-assured:json-path:5.3.0")
    testImplementation("org.awaitility:awaitility:4.2.0")
}
```

## Best Practices

1. **Isolation** - 각 테스트는 독립적
2. **Cleanup** - 테스트 후 데이터 정리
3. **Timeouts** - 적절한 타임아웃 설정
4. **Error Messages** - 명확한 실패 메시지

## Related

- **Gradle**: `./gradlew e2eTest`
- **Tests**: `*/src/test/java/**/*E2ETest.java`
- **RestAssured**: https://rest-assured.io/
- **Integration Tests**: `run-integration-tests.md`
