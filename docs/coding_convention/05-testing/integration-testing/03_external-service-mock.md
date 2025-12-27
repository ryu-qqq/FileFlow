# 외부 서비스 모킹 가이드 (Mockito + WireMock)

> **목적**: 통합 테스트에서 외부 서비스(결제, 알림, 외부 API 등) 모킹 전략

---

## 1️⃣ 모킹 전략 선택 가이드

### 언제 어떤 도구를 사용할까?

```
외부 서비스 호출 필요
        ↓
┌───────────────────────────────────────┐
│ 호출 방식이 무엇인가?                   │
└───────────────────────────────────────┘
        ↓                     ↓
   Port 인터페이스          직접 HTTP 호출
   (헥사고날 패턴)           (RestTemplate, WebClient)
        ↓                     ↓
   ┌─────────┐           ┌──────────┐
   │ Mockito │           │ WireMock │
   └─────────┘           └──────────┘
   - 가볍고 빠름            - 실제 HTTP 시뮬레이션
   - Port Mock으로 충분     - 네트워크 레벨 검증
   - 기본 권장              - 고급 시나리오
```

### 도구별 특성 비교

| 항목 | Mockito | WireMock |
|-----|---------|----------|
| **용도** | Port 인터페이스 Mock | HTTP API Mock |
| **설정 복잡도** | 낮음 | 중간 |
| **실행 속도** | 빠름 | 약간 느림 |
| **HTTP 검증** | ❌ 불가 | ✅ 가능 |
| **요청/응답 로깅** | ❌ | ✅ 자동 |
| **시나리오 테스트** | 제한적 | ✅ 강력 |
| **추천 시점** | 기본 | 외부 API 직접 호출 시 |

---

## 2️⃣ Mockito 방식 (기본 권장)

### IntegrationTestConfig

```java
package com.ryuqq.setof.integration.config;

import com.ryuqq.setof.application.common.port.out.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

/**
 * 통합 테스트 외부 서비스 Mock 설정
 *
 * <p>헥사고날 아키텍처의 Port 인터페이스를 Mock으로 대체</p>
 */
@TestConfiguration
public class IntegrationTestConfig {

    // ========================================
    // 이메일 서비스
    // ========================================
    @Bean
    @Primary
    public EmailSendPort emailSendPort() {
        return mock(EmailSendPort.class);
    }

    // ========================================
    // 파일 저장소 (S3)
    // ========================================
    @Bean
    @Primary
    public FileStoragePort fileStoragePort() {
        return mock(FileStoragePort.class);
    }

    // ========================================
    // 결제 게이트웨이
    // ========================================
    @Bean
    @Primary
    public PaymentGatewayPort paymentGatewayPort() {
        return mock(PaymentGatewayPort.class);
    }

    // ========================================
    // 푸시 알림 (FCM)
    // ========================================
    @Bean
    @Primary
    public PushNotificationPort pushNotificationPort() {
        return mock(PushNotificationPort.class);
    }

    // ========================================
    // SMS 발송
    // ========================================
    @Bean
    @Primary
    public SmsSendPort smsSendPort() {
        return mock(SmsSendPort.class);
    }

    // ========================================
    // 외부 배송 API
    // ========================================
    @Bean
    @Primary
    public DeliveryTrackingPort deliveryTrackingPort() {
        return mock(DeliveryTrackingPort.class);
    }
}
```

### 테스트에서 Mock 행동 정의

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfig.class)
@ActiveProfiles("test")
class OrderPaymentIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PaymentGatewayPort paymentGatewayPort;  // Mock 주입

    @BeforeEach
    void setup() {
        // Mock 행동 정의
        given(paymentGatewayPort.processPayment(any()))
            .willReturn(new PaymentResult("PAY-123", PaymentStatus.SUCCESS));
    }

    @Test
    @DisplayName("결제 성공 시 주문 완료")
    void payment_success_completes_order() {
        // given
        var request = OrderFixture.createOrderRequest();

        // when
        ResponseEntity<ApiResponse<OrderResponse>> response = restTemplate.exchange(
            "/api/v1/orders",
            HttpMethod.POST,
            new HttpEntity<>(request),
            new ParameterizedTypeReference<>() {}
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().data().status()).isEqualTo("PAID");

        // Mock 호출 검증
        then(paymentGatewayPort).should(times(1)).processPayment(any());
    }

    @Test
    @DisplayName("결제 실패 시 주문 실패 처리")
    void payment_failure_fails_order() {
        // given - 결제 실패 시나리오
        given(paymentGatewayPort.processPayment(any()))
            .willThrow(new PaymentFailedException("잔액 부족"));

        var request = OrderFixture.createOrderRequest();

        // when
        ResponseEntity<ProblemDetail> response = restTemplate.exchange(
            "/api/v1/orders",
            HttpMethod.POST,
            new HttpEntity<>(request),
            ProblemDetail.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
```

---

## 3️⃣ WireMock 방식 (HTTP API 직접 호출 시)

### Gradle 의존성

```gradle
// build.gradle
dependencies {
    testImplementation 'org.wiremock:wiremock-standalone:3.9.1'
}
```

### 기본 사용법

```java
package com.ryuqq.setof.integration.payment;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 외부 결제 API 통합 테스트 (WireMock)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@WireMockTest(httpPort = 8089)  // WireMock 서버 8089 포트
class ExternalPaymentApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    // ========================================
    // 외부 API URL을 WireMock으로 대체
    // ========================================
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("external.payment.api.url", () -> "http://localhost:8089");
    }

    @BeforeEach
    void setupWireMock() {
        // 결제 승인 API 모킹
        stubFor(post(urlEqualTo("/api/payments/approve"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withRequestBody(matchingJsonPath("$.orderId"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                        "paymentId": "PAY-123456",
                        "status": "APPROVED",
                        "approvedAt": "2025-01-01T10:00:00Z"
                    }
                    """)));
    }

    @Test
    @DisplayName("외부 결제 API 호출 성공")
    void externalPaymentApi_success() {
        // given
        var request = new PaymentApprovalRequest("ORDER-001", 50000);

        // when - 내부 API 호출 → 외부 API(WireMock) 호출
        ResponseEntity<ApiResponse<PaymentResponse>> response = restTemplate.exchange(
            "/api/v1/payments/approve",
            HttpMethod.POST,
            new HttpEntity<>(request),
            new ParameterizedTypeReference<>() {}
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data().paymentId()).isEqualTo("PAY-123456");

        // WireMock 호출 검증
        verify(postRequestedFor(urlEqualTo("/api/payments/approve"))
            .withRequestBody(matchingJsonPath("$.orderId", equalTo("ORDER-001"))));
    }
}
```

---

## 4️⃣ WireMock 고급 시나리오

### 시나리오 기반 테스트 (상태 전이)

```java
@BeforeEach
void setupScenarioWireMock() {
    // 시나리오: 첫 번째 호출 → 두 번째 호출 다른 응답

    // 초기 상태: 결제 대기
    stubFor(get(urlEqualTo("/api/payments/PAY-123/status"))
        .inScenario("Payment Flow")
        .whenScenarioStateIs(Scenario.STARTED)
        .willReturn(aResponse()
            .withStatus(200)
            .withBody("""{"status": "PENDING"}"""))
        .willSetStateTo("APPROVED"));

    // 두 번째 호출: 결제 승인됨
    stubFor(get(urlEqualTo("/api/payments/PAY-123/status"))
        .inScenario("Payment Flow")
        .whenScenarioStateIs("APPROVED")
        .willReturn(aResponse()
            .withStatus(200)
            .withBody("""{"status": "APPROVED"}""")));
}

@Test
@DisplayName("결제 상태 변경 시나리오")
void paymentStatusTransition() {
    // 첫 번째 조회: PENDING
    var firstResponse = restTemplate.getForEntity(
        "/api/v1/payments/PAY-123/status", PaymentStatusResponse.class);
    assertThat(firstResponse.getBody().status()).isEqualTo("PENDING");

    // 두 번째 조회: APPROVED
    var secondResponse = restTemplate.getForEntity(
        "/api/v1/payments/PAY-123/status", PaymentStatusResponse.class);
    assertThat(secondResponse.getBody().status()).isEqualTo("APPROVED");
}
```

### 지연 응답 테스트 (Timeout 검증)

```java
@Test
@DisplayName("외부 API 타임아웃 시 재시도 동작 검증")
void externalApi_timeout_retries() {
    // 첫 번째: 2초 지연 (타임아웃)
    stubFor(post(urlEqualTo("/api/payments/approve"))
        .inScenario("Retry")
        .whenScenarioStateIs(Scenario.STARTED)
        .willReturn(aResponse()
            .withStatus(200)
            .withFixedDelay(2000))  // 2초 지연
        .willSetStateTo("RETRY_1"));

    // 두 번째: 정상 응답
    stubFor(post(urlEqualTo("/api/payments/approve"))
        .inScenario("Retry")
        .whenScenarioStateIs("RETRY_1")
        .willReturn(aResponse()
            .withStatus(200)
            .withBody("""{"paymentId": "PAY-123", "status": "APPROVED"}""")));

    // when - 재시도 로직 동작 확인
    var response = restTemplate.postForEntity(
        "/api/v1/payments/approve",
        new PaymentApprovalRequest("ORDER-001", 50000),
        ApiResponse.class
    );

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    // 2번 호출 검증 (첫 번째 타임아웃 + 재시도)
    verify(2, postRequestedFor(urlEqualTo("/api/payments/approve")));
}
```

### 에러 응답 테스트

```java
@Test
@DisplayName("외부 API 500 에러 시 적절한 에러 처리")
void externalApi_serverError_handledProperly() {
    // 500 Internal Server Error 응답
    stubFor(post(urlEqualTo("/api/payments/approve"))
        .willReturn(aResponse()
            .withStatus(500)
            .withBody("""{"error": "Internal Server Error"}""")));

    // when
    var response = restTemplate.postForEntity(
        "/api/v1/payments/approve",
        new PaymentApprovalRequest("ORDER-001", 50000),
        ProblemDetail.class
    );

    // then - 500이 아닌 적절한 에러 응답
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    assertThat(response.getBody().getDetail()).contains("결제 서비스");
}
```

---

## 5️⃣ WireMock + TestContainers 조합

### Docker 기반 WireMock

```java
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AdvancedExternalApiTest {

    @Container
    static WireMockContainer wiremock = new WireMockContainer("wiremock/wiremock:3.3.1")
        .withMappingFromResource("mappings/payment-api.json");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("external.payment.api.url", wiremock::getBaseUrl);
    }

    @Test
    void externalApiCall() {
        // WireMock 컨테이너가 자동으로 시작됨
    }
}
```

### mappings/payment-api.json

```json
{
  "mappings": [
    {
      "request": {
        "method": "POST",
        "urlPath": "/api/payments/approve"
      },
      "response": {
        "status": 200,
        "headers": {
          "Content-Type": "application/json"
        },
        "jsonBody": {
          "paymentId": "PAY-123456",
          "status": "APPROVED"
        }
      }
    }
  ]
}
```

---

## 6️⃣ 모킹 전략 결정 플로우차트

```
통합 테스트에서 외부 서비스 호출 필요?
                ↓
┌────────────────────────────────────┐
│ 호출 방식 확인                      │
└────────────────────────────────────┘
         ↓                    ↓
   헥사고날 Port            직접 HTTP 호출
   (내부 추상화)            (RestTemplate 등)
         ↓                    ↓
   ┌──────────┐         ┌──────────────┐
   │ Mockito  │         │ WireMock     │
   │ (권장)   │         │              │
   └──────────┘         └──────────────┘
         │                     │
   IntegrationTestConfig    @WireMockTest
         │                     │
   @Bean @Primary          stubFor(...)
   mock(Port.class)        verify(...)
```

---

## 7️⃣ 체크리스트

### Mockito 사용 시
- [ ] `IntegrationTestConfig`에 `@Bean @Primary` 추가
- [ ] 테스트에서 `@Import(IntegrationTestConfig.class)`
- [ ] 필요 시 `given(...).willReturn(...)` 행동 정의
- [ ] `then(mock).should(...)` 호출 검증

### WireMock 사용 시
- [ ] `wiremock-standalone` 의존성 추가
- [ ] `@WireMockTest(httpPort = ...)` 어노테이션
- [ ] `@DynamicPropertySource`로 URL 대체
- [ ] `stubFor(...)` 응답 정의
- [ ] `verify(...)` 요청 검증

### 공통
- [ ] 성공/실패/타임아웃 시나리오 커버
- [ ] 외부 API 변경 시 테스트 업데이트 계획

---

## 8️⃣ Best Practices

### DO (권장)
```java
// ✅ Port 추상화 사용 → Mockito
@Bean @Primary
public PaymentGatewayPort paymentGatewayPort() {
    return mock(PaymentGatewayPort.class);
}

// ✅ 명확한 시나리오별 테스트
@Test void payment_success() { }
@Test void payment_failure_insufficientBalance() { }
@Test void payment_timeout_retried() { }

// ✅ WireMock은 외부 HTTP 직접 호출 시만
@WireMockTest(httpPort = 8089)
class ThirdPartyApiTest { }
```

### DON'T (금지)
```java
// ❌ 실제 외부 API 호출
restTemplate.postForEntity("https://real-payment-api.com/...", ...);

// ❌ Mock 없이 외부 의존성
@Test void test() {
    // PaymentGatewayPort가 실제 구현체... 외부 API 호출됨!
}

// ❌ 너무 복잡한 WireMock 시나리오
// 10단계 상태 전이보다는 단순한 테스트 여러 개로 분리
```

---

## 📖 관련 문서

- **[Integration Test Module](./02_integration-test-module.md)** - 통합 테스트 모듈 구성
- **[Integration Test Fixture](./04_integration-test-fixture.md)** - Fixture 패턴
- **[Integration Testing Overview](./01_integration-testing-overview.md)** - 개념 개요

---

**작성자**: Development Team
**최종 수정일**: 2025-12-23
**버전**: 1.0.0
