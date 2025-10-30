---
description: API 문서 자동 생성 (Spring REST Docs + OpenAPI 3.0)
---

# API 문서 자동 생성

**목적**: Spring REST Docs + OpenAPI 3.0 기반 API 문서 자동 생성

**타겟**: REST API Layer - API Documentation Generation

**생성 내용**: Test-driven Documentation, OpenAPI 3.0 Spec, AsciiDoc

---

## 🎯 사용법

```bash
# Order API 문서 생성
/test-gen-api-docs OrderApi

# Payment API 문서 생성
/test-gen-api-docs PaymentApi
```

---

## ✅ 자동 생성되는 문서 구성

### 1. Spring REST Docs 테스트 설정

```java
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(
    outputDir = "build/generated-snippets",
    uriScheme = "https",
    uriHost = "api.example.com",
    uriPort = 443
)
@DisplayName("Order API 문서 생성 테스트")
class OrderApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    @DisplayName("POST /api/v1/orders - 주문 생성")
    void createOrder() throws Exception {
        // Given
        CreateOrderRequest request = new CreateOrderRequest(
            100L,  // customerId
            List.of(
                new OrderItemRequest(1L, 2),
                new OrderItemRequest(2L, 1)
            )
        );

        OrderResponse response = new OrderResponse(
            "ORD-12345",
            100L,
            "PLACED",
            LocalDateTime.now(),
            List.of(
                new OrderItemResponse(1L, "상품A", 2, 10000),
                new OrderItemResponse(2L, "상품B", 1, 20000)
            ),
            40000
        );

        given(orderService.createOrder(any())).willReturn(response);

        // When & Then
        mockMvc.perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.orderId").value("ORD-12345"))
        .andExpect(jsonPath("$.status").value("PLACED"))
        .andDo(document("orders/create",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestFields(
                fieldWithPath("customerId")
                    .type(JsonFieldType.NUMBER)
                    .description("고객 ID"),
                fieldWithPath("items")
                    .type(JsonFieldType.ARRAY)
                    .description("주문 상품 목록"),
                fieldWithPath("items[].productId")
                    .type(JsonFieldType.NUMBER)
                    .description("상품 ID"),
                fieldWithPath("items[].quantity")
                    .type(JsonFieldType.NUMBER)
                    .description("주문 수량")
            ),
            responseFields(
                fieldWithPath("orderId")
                    .type(JsonFieldType.STRING)
                    .description("주문 ID"),
                fieldWithPath("customerId")
                    .type(JsonFieldType.NUMBER)
                    .description("고객 ID"),
                fieldWithPath("status")
                    .type(JsonFieldType.STRING)
                    .description("주문 상태 (PLACED, CONFIRMED, CANCELLED)"),
                fieldWithPath("createdAt")
                    .type(JsonFieldType.STRING)
                    .description("주문 생성 시간 (ISO 8601)"),
                fieldWithPath("items")
                    .type(JsonFieldType.ARRAY)
                    .description("주문 상품 목록"),
                fieldWithPath("items[].productId")
                    .type(JsonFieldType.NUMBER)
                    .description("상품 ID"),
                fieldWithPath("items[].productName")
                    .type(JsonFieldType.STRING)
                    .description("상품명"),
                fieldWithPath("items[].quantity")
                    .type(JsonFieldType.NUMBER)
                    .description("주문 수량"),
                fieldWithPath("items[].price")
                    .type(JsonFieldType.NUMBER)
                    .description("상품 단가"),
                fieldWithPath("totalAmount")
                    .type(JsonFieldType.NUMBER)
                    .description("총 주문 금액")
            )
        ));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderId} - 주문 조회")
    void getOrder() throws Exception {
        // Given
        String orderId = "ORD-12345";
        OrderResponse response = new OrderResponse(
            orderId,
            100L,
            "PLACED",
            LocalDateTime.now(),
            List.of(new OrderItemResponse(1L, "상품A", 2, 10000)),
            20000
        );

        given(orderService.getOrder(orderId)).willReturn(response);

        // When & Then
        mockMvc.perform(
            get("/api/v1/orders/{orderId}", orderId)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orderId").value(orderId))
        .andDo(document("orders/get",
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("orderId")
                    .description("조회할 주문 ID")
            ),
            responseFields(
                fieldWithPath("orderId")
                    .type(JsonFieldType.STRING)
                    .description("주문 ID"),
                fieldWithPath("customerId")
                    .type(JsonFieldType.NUMBER)
                    .description("고객 ID"),
                fieldWithPath("status")
                    .type(JsonFieldType.STRING)
                    .description("주문 상태"),
                fieldWithPath("createdAt")
                    .type(JsonFieldType.STRING)
                    .description("주문 생성 시간"),
                fieldWithPath("items")
                    .type(JsonFieldType.ARRAY)
                    .description("주문 상품 목록"),
                fieldWithPath("items[].productId")
                    .type(JsonFieldType.NUMBER)
                    .description("상품 ID"),
                fieldWithPath("items[].productName")
                    .type(JsonFieldType.STRING)
                    .description("상품명"),
                fieldWithPath("items[].quantity")
                    .type(JsonFieldType.NUMBER)
                    .description("수량"),
                fieldWithPath("items[].price")
                    .type(JsonFieldType.NUMBER)
                    .description("단가"),
                fieldWithPath("totalAmount")
                    .type(JsonFieldType.NUMBER)
                    .description("총 금액")
            )
        ));
    }

    @Test
    @DisplayName("GET /api/v1/orders - 주문 목록 조회 (페이징)")
    void getOrders() throws Exception {
        // Given
        List<OrderResponse> orders = List.of(
            new OrderResponse("ORD-1", 100L, "PLACED", LocalDateTime.now(), List.of(), 10000),
            new OrderResponse("ORD-2", 101L, "CONFIRMED", LocalDateTime.now(), List.of(), 20000)
        );

        Page<OrderResponse> page = new PageImpl<>(orders, PageRequest.of(0, 10), 2);
        given(orderService.getOrders(any(), any())).willReturn(page);

        // When & Then
        mockMvc.perform(
            get("/api/v1/orders")
                .param("page", "0")
                .param("size", "10")
                .param("status", "PLACED")
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(document("orders/list",
            preprocessResponse(prettyPrint()),
            queryParameters(
                parameterWithName("page")
                    .description("페이지 번호 (0부터 시작)")
                    .optional(),
                parameterWithName("size")
                    .description("페이지 크기 (기본값: 20)")
                    .optional(),
                parameterWithName("status")
                    .description("주문 상태 필터 (PLACED, CONFIRMED, CANCELLED)")
                    .optional()
            ),
            responseFields(
                fieldWithPath("content")
                    .type(JsonFieldType.ARRAY)
                    .description("주문 목록"),
                fieldWithPath("content[].orderId")
                    .type(JsonFieldType.STRING)
                    .description("주문 ID"),
                fieldWithPath("content[].customerId")
                    .type(JsonFieldType.NUMBER)
                    .description("고객 ID"),
                fieldWithPath("content[].status")
                    .type(JsonFieldType.STRING)
                    .description("주문 상태"),
                fieldWithPath("content[].createdAt")
                    .type(JsonFieldType.STRING)
                    .description("생성 시간"),
                fieldWithPath("content[].items")
                    .type(JsonFieldType.ARRAY)
                    .description("상품 목록"),
                fieldWithPath("content[].totalAmount")
                    .type(JsonFieldType.NUMBER)
                    .description("총 금액"),
                fieldWithPath("pageable")
                    .type(JsonFieldType.OBJECT)
                    .description("페이징 정보"),
                fieldWithPath("totalElements")
                    .type(JsonFieldType.NUMBER)
                    .description("전체 요소 수"),
                fieldWithPath("totalPages")
                    .type(JsonFieldType.NUMBER)
                    .description("전체 페이지 수"),
                fieldWithPath("size")
                    .type(JsonFieldType.NUMBER)
                    .description("페이지 크기"),
                fieldWithPath("number")
                    .type(JsonFieldType.NUMBER)
                    .description("현재 페이지 번호")
            )
        ));
    }
}
```

### 2. Error Response 문서화

```java
@Test
@DisplayName("POST /api/v1/orders - 잘못된 요청 (400 Bad Request)")
void createOrderWithInvalidRequest() throws Exception {
    // Given
    CreateOrderRequest request = new CreateOrderRequest(
        null,  // customerId 누락
        List.of()
    );

    // When & Then
    mockMvc.perform(
        post("/api/v1/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
    )
    .andExpect(status().isBadRequest())
    .andDo(document("orders/error-400",
        preprocessResponse(prettyPrint()),
        responseFields(
            fieldWithPath("error.code")
                .type(JsonFieldType.STRING)
                .description("에러 코드"),
            fieldWithPath("error.message")
                .type(JsonFieldType.STRING)
                .description("에러 메시지"),
            fieldWithPath("error.field")
                .type(JsonFieldType.STRING)
                .description("에러 발생 필드")
                .optional(),
            fieldWithPath("error.timestamp")
                .type(JsonFieldType.STRING)
                .description("에러 발생 시간 (ISO 8601)")
        )
    ));
}

@Test
@DisplayName("GET /api/v1/orders/{orderId} - 주문 없음 (404 Not Found)")
void getOrderNotFound() throws Exception {
    // Given
    String orderId = "NON-EXISTENT";
    given(orderService.getOrder(orderId))
        .willThrow(new OrderNotFoundException(orderId));

    // When & Then
    mockMvc.perform(
        get("/api/v1/orders/{orderId}", orderId)
            .accept(MediaType.APPLICATION_JSON)
    )
    .andExpect(status().isNotFound())
    .andDo(document("orders/error-404",
        preprocessResponse(prettyPrint()),
        responseFields(
            fieldWithPath("error.code")
                .type(JsonFieldType.STRING)
                .description("에러 코드 (ORDER_NOT_FOUND)"),
            fieldWithPath("error.message")
                .type(JsonFieldType.STRING)
                .description("에러 메시지"),
            fieldWithPath("error.timestamp")
                .type(JsonFieldType.STRING)
                .description("에러 발생 시간")
        )
    ));
}
```

### 3. AsciiDoc 문서 템플릿

```asciidoc
= Order API Documentation
:doctype: book
:icons: font
:source-highlighter: highlightjs
:toc: left
:toclevels: 3
:sectlinks:

[[overview]]
== Overview

Order API는 주문 생성, 조회, 확인, 취소 등 주문 관리 기능을 제공합니다.

[[overview-http-verbs]]
=== HTTP Verbs

본 API는 RESTful 원칙을 따르며 다음 HTTP 메서드를 사용합니다:

|===
| Verb | Usage

| `GET`
| 리소스 조회

| `POST`
| 새 리소스 생성

| `PATCH`
| 기존 리소스 부분 수정

| `DELETE`
| 기존 리소스 삭제
|===

[[overview-http-status-codes]]
=== HTTP Status Codes

본 API는 다음 HTTP 상태 코드를 사용합니다:

|===
| Status Code | Usage

| `200 OK`
| 요청이 성공적으로 처리됨

| `201 Created`
| 새 리소스가 성공적으로 생성됨

| `400 Bad Request`
| 잘못된 요청 (필수 파라미터 누락, 유효성 검증 실패 등)

| `404 Not Found`
| 요청한 리소스가 존재하지 않음

| `500 Internal Server Error`
| 서버 내부 오류
|===

[[resources]]
== Resources

[[resources-orders]]
=== Orders

주문 리소스는 고객의 주문 정보를 관리합니다.

[[resources-orders-create]]
==== 주문 생성

`POST` 요청으로 새로운 주문을 생성합니다.

operation::orders/create[snippets='request-fields,response-fields,curl-request,http-response']

[[resources-orders-get]]
==== 주문 조회

`GET` 요청으로 특정 주문을 조회합니다.

operation::orders/get[snippets='path-parameters,response-fields,curl-request,http-response']

[[resources-orders-list]]
==== 주문 목록 조회

`GET` 요청으로 주문 목록을 페이징하여 조회합니다.

operation::orders/list[snippets='query-parameters,response-fields,curl-request,http-response']

[[resources-orders-errors]]
==== 에러 응답

[[resources-orders-error-400]]
===== 400 Bad Request

잘못된 요청 시 반환됩니다.

operation::orders/error-400[snippets='response-fields,http-response']

[[resources-orders-error-404]]
===== 404 Not Found

주문을 찾을 수 없을 때 반환됩니다.

operation::orders/error-404[snippets='response-fields,http-response']
```

### 4. OpenAPI 3.0 Spec 생성

```java
@TestConfiguration
public class OpenApiConfiguration {

    @Bean
    public OpenAPIDefinition openAPIDefinition() {
        return OpenAPIDefinition.builder()
            .info(Info.builder()
                .title("Order API")
                .version("1.0.0")
                .description("주문 관리 REST API")
                .contact(Contact.builder()
                    .name("API Support")
                    .email("api@example.com")
                    .build())
                .license(License.builder()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")
                    .build())
                .build())
            .servers(List.of(
                Server.builder()
                    .url("https://api.example.com")
                    .description("Production")
                    .build(),
                Server.builder()
                    .url("https://staging-api.example.com")
                    .description("Staging")
                    .build()
            ))
            .build();
    }

    @Bean
    public OperationCustomizer operationCustomizer() {
        return (operation, handlerMethod) -> {
            operation.addTagsItem("Orders");
            return operation;
        };
    }
}
```

### 5. Gradle 빌드 설정

```groovy
plugins {
    id 'org.asciidoctor.jvm.convert' version '3.3.2'
    id 'org.springdoc.openapi-gradle-plugin' version '1.6.0'
}

dependencies {
    testImplementation 'org.springframework.restdocs:spring-restdocs-mockmvc'
    testImplementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0'
}

ext {
    snippetsDir = file('build/generated-snippets')
}

test {
    outputs.dir snippetsDir
}

asciidoctor {
    inputs.dir snippetsDir
    dependsOn test

    baseDirFollowsSourceDir()

    attributes 'snippets': snippetsDir,
               'source-highlighter': 'highlightjs',
               'toc': 'left',
               'toclevels': '3'
}

bootJar {
    dependsOn asciidoctor
    from("${asciidoctor.outputDir}") {
        into 'static/docs'
    }
}

// OpenAPI 3.0 JSON 생성
task generateOpenApiDocs {
    dependsOn test
    doLast {
        // OpenAPI JSON 파일 생성
    }
}
```

---

## 🔧 생성 규칙

### 1. 파일 위치
```
adapter-in/rest-api/src/test/java/
└── com/ryuqq/adapter/in/rest/{api}/
    └── {Api}DocumentationTest.java

src/docs/asciidoc/
└── {api}.adoc

build/generated-snippets/
└── {api}/
    ├── create/
    ├── get/
    └── list/
```

### 2. REST Docs 설정 템플릿
```java
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@DisplayName("{Api} 문서 생성 테스트")
class {Api}DocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void documentApiEndpoint() throws Exception {
        mockMvc.perform(...)
            .andDo(document("{endpoint-name}",
                requestFields(...),
                responseFields(...)
            ));
    }
}
```

### 3. Zero-Tolerance 규칙 준수

- ✅ **Spring REST Docs**: Test-driven documentation
- ✅ **Field Documentation**: 모든 필드 설명 필수
- ✅ **Error Documentation**: 모든 에러 케이스 문서화
- ✅ **OpenAPI 3.0**: 표준 스펙 준수
- ✅ **AsciiDoc**: 구조화된 문서 작성

---

## 📊 문서 커버리지 목표

| 항목 | 목표 | 설명 |
|------|------|------|
| Endpoint Coverage | 100% | 모든 API 엔드포인트 |
| Request Fields | 100% | 모든 요청 필드 설명 |
| Response Fields | 100% | 모든 응답 필드 설명 |
| Error Cases | 100% | 모든 에러 응답 |
| Examples | 100% | curl/HTTP 예제 |

---

## 💡 Claude Code 활용 팁

### 1. 전체 API 문서 생성
```
"Generate complete REST Docs tests for OrderController with all endpoints"
```

### 2. Error 문서 집중
```
"Add error response documentation for all error codes in OrderApi"
```

### 3. OpenAPI 3.0 생성
```
"Generate OpenAPI 3.0 specification from REST Docs tests"
```

### 4. AsciiDoc 템플릿
```
"Create AsciiDoc template for Order API with table of contents"
```

---

## 🎯 기대 효과

1. **자동 문서화**: 테스트가 문서 생성
2. **항상 최신 상태**: 코드 변경 시 문서 자동 업데이트
3. **표준 준수**: OpenAPI 3.0 표준
4. **개발자 친화적**: curl, HTTP 예제 포함

---

**✅ 이 명령어는 Claude Code가 REST API의 고품질 문서를 자동 생성하는 데 사용됩니다.**

**💡 핵심**: Windsurf가 REST API를 생성하면, Claude Code가 Spring REST Docs + OpenAPI 문서를 자동 생성!
