# REST API Unit Test 가이드 (MockMvc)

> **Unit Test는 Controller 로직만 격리하여 빠르게 테스트합니다.**
> MockMvc를 사용하여 실제 HTTP 서버 없이 Spring MVC를 테스트합니다.

## 목차
1. [Unit Test 소개](#1-unit-test-소개)
2. [MockMvc 설정](#2-mockmvc-설정)
3. [@WebMvcTest vs @SpringBootTest](#3-webmvctest-vs-springboottest)
4. [Controller Unit Test 패턴](#4-controller-unit-test-패턴)
5. [Mocking 전략](#5-mocking-전략)
6. [Validation 테스트](#6-validation-테스트)
7. [Exception 처리 테스트](#7-exception-처리-테스트)
8. [Best Practices](#8-best-practices)

---

## 1. Unit Test 소개

### 1.1 Unit Test란?

**Unit Test**는 **단일 컴포넌트를 격리**하여 테스트합니다.

**검증 범위** (Controller만):
```
REST API Controller (테스트 대상)
    ↓
Application Layer (Mock)
    ↓
Domain Layer (Mock)
    ↓
Persistence Layer (Mock)
```

### 1.2 Integration Test vs Unit Test

| 항목 | Unit Test | Integration Test |
|------|-----------|------------------|
| **범위** | Controller만 | Controller → DB까지 |
| **속도** | 매우 빠름 (ms) | 느림 (초) |
| **의존성** | Mock/Stub | 실제 Infrastructure |
| **목적** | 로직 검증 | 통합 동작 검증 |
| **실행 시점** | 매 커밋, 개발 중 | PR/빌드 시 |
| **DB 필요** | ❌ 불필요 | ✅ 필요 (Testcontainers) |

**예시 비교**:
```java
// ✅ Unit Test (빠름, Controller만)
@WebMvcTest(ExampleController.class)
class ExampleControllerUnitTest {
    @MockBean
    private CreateExampleUseCase createExampleUseCase;

    @Test
    void createExample_ShouldReturn201() {
        // Given: UseCase Mock
        when(createExampleUseCase.execute(any()))
            .thenReturn(new ExampleResponse(1L, "Test"));

        // When & Then: Controller 로직만 테스트
        mockMvc.perform(post("/api/v1/examples")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isCreated());
    }
}

// ✅ Integration Test (느림, Controller → DB)
class ExampleControllerIntegrationTest extends AbstractIntegrationTest {
    @Test
    void createExample_ShouldPersistToDatabase() {
        // Given
        ExampleApiRequest request = new ExampleApiRequest("Test");

        // When: 실제 DB에 저장
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/examples")
        .then()
            .statusCode(201);

        // Then: DB 확인
        ExampleJpaEntity saved = exampleRepository.findById(1L).orElseThrow();
        assertEquals("Test", saved.getMessage());
    }
}
```

### 1.3 언제 Unit Test를 작성하는가?

✅ **Unit Test가 필요한 경우**:
- **빠른 피드백**: 개발 중 즉시 검증
- **Controller 로직**: 요청/응답 변환, Validation
- **Edge Case**: 다양한 예외 상황 (빠른 테스트)
- **Mapper 테스트**: DTO 변환 로직
- **TDD**: 테스트 주도 개발 (Red-Green-Refactor)

❌ **Unit Test가 불필요한 경우**:
- **DB 통합 검증**: JPA Query, Transaction 동작 (Integration Test로)
- **실제 동작 검증**: 전체 흐름 (Integration Test로)
- **성능 테스트**: 실제 부하 테스트 (별도 도구)

---

## 2. MockMvc 설정

### 2.1 MockMvc란?

**MockMvc**는 Spring MVC를 **실제 HTTP 서버 없이** 테스트하는 도구입니다.

**특징**:
- ✅ **빠름**: 실제 서버 시작 없음 (ms 단위)
- ✅ **격리**: Controller만 테스트 (의존성 Mock)
- ✅ **Spring 통합**: Spring MVC 전체 스택 테스트
- ✅ **DSL**: Fluent API로 가독성 높음

### 2.2 의존성 추가

**`build.gradle.kts`** (이미 포함됨):
```kotlin
dependencies {
    // Spring Boot Test (MockMvc 포함)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
```

### 2.3 MockMvc 초기화

**방법 1: @WebMvcTest (권장, 빠름)**
```java
@WebMvcTest(ExampleController.class)  // ExampleController만 로딩
class ExampleControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;  // 자동 주입

    @MockBean
    private CreateExampleUseCase createExampleUseCase;  // Mock

    @Test
    void createExample_ShouldReturn201() {
        // MockMvc 사용
        mockMvc.perform(post("/api/v1/examples")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isCreated());
    }
}
```

**방법 2: @SpringBootTest + @AutoConfigureMockMvc (느림)**
```java
@SpringBootTest
@AutoConfigureMockMvc  // MockMvc 자동 설정
class ExampleControllerFullContextTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createExample_ShouldReturn201() {
        // 전체 Spring Context 로딩 (느림)
        mockMvc.perform(post("/api/v1/examples")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isCreated());
    }
}
```

---

## 3. @WebMvcTest vs @SpringBootTest

### 3.1 비교표

| 항목 | @WebMvcTest | @SpringBootTest |
|------|-------------|-----------------|
| **로딩 범위** | Controller Layer만 | 전체 Context |
| **속도** | 빠름 (0.5초) | 느림 (5초) |
| **의존성** | 수동 Mock 필요 | 자동 주입 |
| **용도** | Controller 단위 테스트 | Integration Test |
| **DB 필요** | ❌ 불필요 | ✅ 필요 |

### 3.2 @WebMvcTest 예시 (권장)

```java
@WebMvcTest(ExampleController.class)  // ExampleController만 로딩
class ExampleControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    // 필요한 의존성만 Mock
    @MockBean
    private CreateExampleUseCase createExampleUseCase;

    @MockBean
    private GetExampleQueryService getExampleQueryService;

    @MockBean
    private ExampleApiMapper exampleApiMapper;

    @Test
    void createExample_ShouldReturn201_WhenValidRequest() {
        // Given: Mock 설정
        ExampleApiRequest request = new ExampleApiRequest("Test");
        CreateExampleCommand command = CreateExampleCommand.builder()
            .message("Test")
            .build();
        ExampleResponse useCaseResponse = new ExampleResponse(1L, "Test");
        ExampleApiResponse apiResponse = new ExampleApiResponse(1L, "Test");

        when(exampleApiMapper.toCreateCommand(request)).thenReturn(command);
        when(createExampleUseCase.execute(command)).thenReturn(useCaseResponse);
        when(exampleApiMapper.toApiResponse(useCaseResponse)).thenReturn(apiResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/examples")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "message": "Test"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.message").value("Test"));

        // Verify: UseCase 호출 확인
        verify(createExampleUseCase).execute(command);
    }
}
```

### 3.3 @SpringBootTest 예시 (느림, Integration Test 수준)

```java
@SpringBootTest
@AutoConfigureMockMvc
class ExampleControllerFullContextTest {

    @Autowired
    private MockMvc mockMvc;

    // 실제 Bean 사용 (Mock 불필요)

    @Test
    void createExample_ShouldReturn201() {
        // 전체 Context 로딩 (느림, Integration Test에 가까움)
        mockMvc.perform(post("/api/v1/examples")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isCreated());
    }
}
```

**권장**: **Unit Test는 @WebMvcTest 사용** (빠름, 격리됨)

---

## 4. Controller Unit Test 패턴

### 4.1 POST (Command 생성)

**테스트 목적**: Example 생성 API가 정상 동작하는지 검증

```java
package com.ryuqq.adapter.in.rest.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.adapter.in.rest.example.dto.request.ExampleApiRequest;
import com.ryuqq.adapter.in.rest.example.dto.response.ExampleApiResponse;
import com.ryuqq.adapter.in.rest.example.mapper.ExampleApiMapper;
import com.ryuqq.application.example.dto.command.CreateExampleCommand;
import com.ryuqq.application.example.dto.response.ExampleResponse;
import com.ryuqq.application.example.port.in.CreateExampleUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ExampleController Unit Test (POST 생성)
 *
 * <p>MockMvc를 사용하여 Controller 로직만 격리 테스트합니다.</p>
 *
 * @author Claude Code
 * @since 2025-10-31
 */
@WebMvcTest(ExampleController.class)
@DisplayName("ExampleController Unit Test - POST (생성)")
class ExampleControllerCreateUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateExampleUseCase createExampleUseCase;

    @MockBean
    private ExampleApiMapper exampleApiMapper;

    @Test
    @DisplayName("Example 생성 - 정상 케이스")
    void createExample_ShouldReturn201_WhenValidRequest() throws Exception {
        // Given: Request DTO
        ExampleApiRequest request = new ExampleApiRequest("Test Message");

        // Mock: Mapper toCreateCommand()
        CreateExampleCommand command = CreateExampleCommand.builder()
            .message("Test Message")
            .build();
        when(exampleApiMapper.toCreateCommand(any(ExampleApiRequest.class)))
            .thenReturn(command);

        // Mock: UseCase execute()
        ExampleResponse useCaseResponse = new ExampleResponse(1L, "Test Message");
        when(createExampleUseCase.execute(any(CreateExampleCommand.class)))
            .thenReturn(useCaseResponse);

        // Mock: Mapper toApiResponse()
        ExampleApiResponse apiResponse = new ExampleApiResponse(1L, "Test Message");
        when(exampleApiMapper.toApiResponse(any(ExampleResponse.class)))
            .thenReturn(apiResponse);

        // When & Then: POST /api/v1/examples
        mockMvc.perform(post("/api/v1/examples")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())  // 요청/응답 출력 (디버깅용)
            .andExpect(status().isCreated())  // HTTP 201
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.message").value("Test Message"));

        // Verify: UseCase 호출 확인
        verify(exampleApiMapper).toCreateCommand(any(ExampleApiRequest.class));
        verify(createExampleUseCase).execute(any(CreateExampleCommand.class));
        verify(exampleApiMapper).toApiResponse(any(ExampleResponse.class));
    }

    @Test
    @DisplayName("Example 생성 - 응답 구조 검증 (ApiResponse 래핑)")
    void createExample_ShouldReturnApiResponseStructure() throws Exception {
        // Given
        ExampleApiRequest request = new ExampleApiRequest("Test");

        when(exampleApiMapper.toCreateCommand(any())).thenReturn(mock(CreateExampleCommand.class));
        when(createExampleUseCase.execute(any())).thenReturn(new ExampleResponse(1L, "Test"));
        when(exampleApiMapper.toApiResponse(any())).thenReturn(new ExampleApiResponse(1L, "Test"));

        // When & Then: ApiResponse 구조 검증
        mockMvc.perform(post("/api/v1/examples")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").exists())  // success 필드 존재
            .andExpect(jsonPath("$.data").exists())  // data 필드 존재
            .andExpect(jsonPath("$.error").doesNotExist())  // error 필드 없음
            .andExpect(jsonPath("$.timestamp").exists())  // timestamp 필드 존재
            .andExpect(jsonPath("$.requestId").exists());  // requestId 필드 존재
    }
}
```

### 4.2 GET (Query 조회)

**테스트 목적**: Example 단건 조회 API 검증

```java
package com.ryuqq.adapter.in.rest.example.controller;

import com.ryuqq.adapter.in.rest.example.dto.response.ExampleDetailApiResponse;
import com.ryuqq.adapter.in.rest.example.mapper.ExampleApiMapper;
import com.ryuqq.application.example.dto.query.ExampleQuery;
import com.ryuqq.application.example.dto.response.ExampleDetailResponse;
import com.ryuqq.application.example.port.in.GetExampleQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ExampleController Unit Test (GET 조회)
 *
 * @author Claude Code
 * @since 2025-10-31
 */
@WebMvcTest(ExampleController.class)
@DisplayName("ExampleController Unit Test - GET (조회)")
class ExampleControllerGetUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetExampleQueryService getExampleQueryService;

    @MockBean
    private ExampleApiMapper exampleApiMapper;

    @Test
    @DisplayName("Example 단건 조회 - 정상 케이스")
    void getExample_ShouldReturn200_WhenExampleExists() throws Exception {
        // Given: Example ID
        Long exampleId = 1L;

        // Mock: Mapper toGetQuery()
        ExampleQuery query = ExampleQuery.builder().id(exampleId).build();
        when(exampleApiMapper.toGetQuery(exampleId)).thenReturn(query);

        // Mock: QueryService getById()
        ExampleDetailResponse detailResponse = new ExampleDetailResponse(
            1L,
            "Test Message",
            "ACTIVE",
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        when(getExampleQueryService.getById(any(ExampleQuery.class)))
            .thenReturn(detailResponse);

        // Mock: Mapper toDetailApiResponse()
        ExampleDetailApiResponse apiResponse = new ExampleDetailApiResponse(
            1L,
            "Test Message",
            "ACTIVE",
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        when(exampleApiMapper.toDetailApiResponse(any(ExampleDetailResponse.class)))
            .thenReturn(apiResponse);

        // When & Then: GET /api/v1/examples/1
        mockMvc.perform(get("/api/v1/examples/{id}", exampleId))
            .andExpect(status().isOk())  // HTTP 200
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.message").value("Test Message"))
            .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        // Verify
        verify(exampleApiMapper).toGetQuery(exampleId);
        verify(getExampleQueryService).getById(any(ExampleQuery.class));
        verify(exampleApiMapper).toDetailApiResponse(any(ExampleDetailResponse.class));
    }

    @Test
    @DisplayName("Example 단건 조회 - PathVariable 검증")
    void getExample_ShouldAcceptPathVariable() throws Exception {
        // Given
        Long exampleId = 123L;

        when(exampleApiMapper.toGetQuery(anyLong())).thenReturn(mock(ExampleQuery.class));
        when(getExampleQueryService.getById(any())).thenReturn(mock(ExampleDetailResponse.class));
        when(exampleApiMapper.toDetailApiResponse(any())).thenReturn(mock(ExampleDetailApiResponse.class));

        // When & Then: PathVariable이 올바르게 전달되는지 검증
        mockMvc.perform(get("/api/v1/examples/{id}", exampleId))
            .andExpect(status().isOk());

        // Verify: 123L이 전달됨
        verify(exampleApiMapper).toGetQuery(123L);
    }
}
```

### 4.3 Pagination (Query Parameter)

**테스트 목적**: Query Parameter 바인딩 검증

```java
@Test
@DisplayName("Example 검색 - Query Parameter 바인딩")
void searchExamples_ShouldBindQueryParameters() throws Exception {
    // Given: Query Parameters
    // ?cursor=abc&size=20&sortBy=createdAt&sortDirection=DESC

    // Mock
    when(exampleApiMapper.toSearchQuery(any())).thenReturn(mock(ExampleSearchQuery.class));
    when(searchExampleQueryService.searchByCursor(any())).thenReturn(mock(SliceResponse.class));
    when(exampleApiMapper.toSliceApiResponse(any())).thenReturn(mock(ExampleSliceApiResponse.class));

    // When & Then: GET /api/v1/examples?cursor=abc&size=20
    mockMvc.perform(get("/api/v1/examples")
            .param("cursor", "abc")
            .param("size", "20")
            .param("sortBy", "createdAt")
            .param("sortDirection", "DESC"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    // Verify: toSearchQuery()가 호출되었는지 확인
    verify(exampleApiMapper).toSearchQuery(any(ExampleSearchApiRequest.class));
}
```

---

## 5. Mocking 전략

### 5.1 Mock 대상 결정

**원칙**: **Controller의 의존성은 모두 Mock**

```
Controller (테스트 대상)
    ↓
UseCase (Mock)
Mapper (Mock)
```

**예시**:
```java
@WebMvcTest(ExampleController.class)
class ExampleControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    // Controller의 모든 의존성 Mock
    @MockBean
    private CreateExampleUseCase createExampleUseCase;  // ✅ Mock

    @MockBean
    private GetExampleQueryService getExampleQueryService;  // ✅ Mock

    @MockBean
    private SearchExampleQueryService searchExampleQueryService;  // ✅ Mock

    @MockBean
    private ExampleApiMapper exampleApiMapper;  // ✅ Mock
}
```

### 5.2 Mock 설정 패턴

#### 패턴 1: given().willReturn() (BDD 스타일)

```java
@Test
void createExample_ShouldReturn201() {
    // Given: BDD 스타일 Mock
    given(exampleApiMapper.toCreateCommand(any()))
        .willReturn(command);

    given(createExampleUseCase.execute(any()))
        .willReturn(useCaseResponse);

    given(exampleApiMapper.toApiResponse(any()))
        .willReturn(apiResponse);

    // When & Then
    mockMvc.perform(post("/api/v1/examples")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson))
        .andExpect(status().isCreated());
}
```

#### 패턴 2: when().thenReturn() (전통적 스타일)

```java
@Test
void createExample_ShouldReturn201() {
    // Given: 전통적 스타일 Mock
    when(exampleApiMapper.toCreateCommand(any()))
        .thenReturn(command);

    when(createExampleUseCase.execute(any()))
        .thenReturn(useCaseResponse);

    when(exampleApiMapper.toApiResponse(any()))
        .thenReturn(apiResponse);

    // When & Then
    mockMvc.perform(post("/api/v1/examples")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson))
        .andExpect(status().isCreated());
}
```

**권장**: **BDD 스타일 (given-when-then 일관성)**

### 5.3 ArgumentMatcher 사용

#### any() - 모든 인자 허용

```java
// Given: 어떤 ExampleApiRequest도 허용
when(exampleApiMapper.toCreateCommand(any(ExampleApiRequest.class)))
    .thenReturn(command);
```

#### eq() - 특정 값 매칭

```java
// Given: ID가 정확히 1L인 경우만
when(exampleApiMapper.toGetQuery(eq(1L)))
    .thenReturn(query);
```

#### argThat() - 커스텀 매칭

```java
// Given: message가 "Test"로 시작하는 경우만
when(exampleApiMapper.toCreateCommand(
    argThat(request -> request.message().startsWith("Test"))))
    .thenReturn(command);
```

### 5.4 Verify 패턴

**목적**: Mock 호출 여부 및 횟수 검증

```java
@Test
void createExample_ShouldCallUseCaseOnce() throws Exception {
    // Given
    when(exampleApiMapper.toCreateCommand(any())).thenReturn(command);
    when(createExampleUseCase.execute(any())).thenReturn(response);
    when(exampleApiMapper.toApiResponse(any())).thenReturn(apiResponse);

    // When
    mockMvc.perform(post("/api/v1/examples")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson))
        .andExpect(status().isCreated());

    // Then: UseCase가 정확히 1번 호출되었는지 검증
    verify(createExampleUseCase, times(1)).execute(any());

    // 또는
    verify(createExampleUseCase).execute(any());  // times(1)이 기본값

    // 호출되지 않았는지 검증
    verify(createExampleUseCase, never()).execute(any());

    // 최소 1번 호출되었는지 검증
    verify(createExampleUseCase, atLeastOnce()).execute(any());
}
```

---

## 6. Validation 테스트

### 6.1 @Valid 어노테이션 테스트

**목적**: Request DTO Validation이 정상 동작하는지 검증

```java
@Test
@DisplayName("Example 생성 - Validation 실패 (message가 blank)")
void createExample_ShouldReturn400_WhenMessageIsBlank() throws Exception {
    // Given: message가 빈 문자열 (Validation 실패)
    String invalidRequestJson = """
        {
            "message": ""
        }
        """;

    // When & Then: Validation 실패 → HTTP 400
    mockMvc.perform(post("/api/v1/examples")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidRequestJson))
        .andExpect(status().isBadRequest())  // HTTP 400
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.title").value("Validation Failed"))
        .andExpect(jsonPath("$.error.detail").exists());

    // Verify: UseCase는 호출되지 않음 (Validation 단계에서 실패)
    verify(createExampleUseCase, never()).execute(any());
}

@Test
@DisplayName("Example 생성 - Validation 실패 (message가 null)")
void createExample_ShouldReturn400_WhenMessageIsNull() throws Exception {
    // Given: message가 null
    String invalidRequestJson = """
        {
            "message": null
        }
        """;

    // When & Then
    mockMvc.perform(post("/api/v1/examples")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidRequestJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));

    verify(createExampleUseCase, never()).execute(any());
}

@Test
@DisplayName("Example 생성 - Validation 실패 (message 필드 누락)")
void createExample_ShouldReturn400_WhenMessageIsMissing() throws Exception {
    // Given: message 필드 자체가 없음
    String invalidRequestJson = """
        {
        }
        """;

    // When & Then
    mockMvc.perform(post("/api/v1/examples")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidRequestJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));

    verify(createExampleUseCase, never()).execute(any());
}
```

### 6.2 PathVariable Validation 테스트

```java
@Test
@DisplayName("Example 조회 - Validation 실패 (ID가 음수)")
void getExample_ShouldReturn400_WhenIdIsNegative() throws Exception {
    // Given: 음수 ID (@Positive Validation 실패)
    Long invalidId = -1L;

    // When & Then
    mockMvc.perform(get("/api/v1/examples/{id}", invalidId))
        .andExpect(status().isBadRequest())  // HTTP 400
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.detail").value("ID는 양수여야 합니다"));

    // Verify: QueryService는 호출되지 않음
    verify(getExampleQueryService, never()).getById(any());
}

@Test
@DisplayName("Example 조회 - Validation 실패 (ID가 0)")
void getExample_ShouldReturn400_WhenIdIsZero() throws Exception {
    // Given: 0 (@Positive는 1 이상만 허용)
    Long invalidId = 0L;

    // When & Then
    mockMvc.perform(get("/api/v1/examples/{id}", invalidId))
        .andExpect(status().isBadRequest());

    verify(getExampleQueryService, never()).getById(any());
}
```

---

## 7. Exception 처리 테스트

### 7.1 DomainException 처리

**목적**: GlobalExceptionHandler가 DomainException을 HTTP 응답으로 변환하는지 검증

```java
@Test
@DisplayName("Example 조회 - DomainException (NOT_FOUND)")
void getExample_ShouldReturn404_WhenDomainExceptionThrown() throws Exception {
    // Given: QueryService가 DomainException 던짐
    when(exampleApiMapper.toGetQuery(anyLong())).thenReturn(mock(ExampleQuery.class));
    when(getExampleQueryService.getById(any()))
        .thenThrow(new ExampleNotFoundException(1L));

    // When & Then: DomainException → HTTP 404
    mockMvc.perform(get("/api/v1/examples/{id}", 1L))
        .andExpect(status().isNotFound())  // HTTP 404
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("EXAMPLE.NOT_FOUND"))
        .andExpect(jsonPath("$.error.title").value("Example Not Found"))
        .andExpect(jsonPath("$.error.detail").exists());
}

@Test
@DisplayName("Example 생성 - DomainException (CONFLICT)")
void createExample_ShouldReturn409_WhenDuplicateException() throws Exception {
    // Given: UseCase가 DomainException 던짐 (중복)
    when(exampleApiMapper.toCreateCommand(any())).thenReturn(mock(CreateExampleCommand.class));
    when(createExampleUseCase.execute(any()))
        .thenThrow(new ExampleDuplicateException("Test"));

    // When & Then: DomainException → HTTP 409
    mockMvc.perform(post("/api/v1/examples")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "message": "Test"
                }
                """))
        .andExpect(status().isConflict())  // HTTP 409
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("EXAMPLE.DUPLICATE"));
}
```

### 7.2 RuntimeException 처리

**목적**: 예상치 못한 예외가 HTTP 500으로 변환되는지 검증

```java
@Test
@DisplayName("Example 생성 - RuntimeException (INTERNAL_SERVER_ERROR)")
void createExample_ShouldReturn500_WhenRuntimeExceptionThrown() throws Exception {
    // Given: UseCase가 RuntimeException 던짐
    when(exampleApiMapper.toCreateCommand(any())).thenReturn(mock(CreateExampleCommand.class));
    when(createExampleUseCase.execute(any()))
        .thenThrow(new RuntimeException("Unexpected error"));

    // When & Then: RuntimeException → HTTP 500
    mockMvc.perform(post("/api/v1/examples")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "message": "Test"
                }
                """))
        .andExpect(status().isInternalServerError())  // HTTP 500
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.title").value("Internal Server Error"));
}
```

---

## 8. Best Practices

### 8.1 테스트 네이밍 규칙

**패턴**: `{메서드명}_{예상결과}_{조건}`

```java
✅ createExample_ShouldReturn201_WhenValidRequest()
✅ getExample_ShouldReturn404_WhenNotFound()
✅ createExample_ShouldReturn400_WhenMessageIsBlank()

❌ test1()
❌ testCreateExample()
❌ shouldWork()
```

### 8.2 Given-When-Then 패턴

```java
@Test
void createExample_ShouldReturn201() throws Exception {
    // Given: Mock 설정
    when(exampleApiMapper.toCreateCommand(any())).thenReturn(command);
    when(createExampleUseCase.execute(any())).thenReturn(response);
    when(exampleApiMapper.toApiResponse(any())).thenReturn(apiResponse);

    // When: API 호출
    var result = mockMvc.perform(post("/api/v1/examples")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestJson));

    // Then: 결과 검증
    result.andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true));
}
```

### 8.3 ObjectMapper 재사용

**권장**: `@Autowired ObjectMapper` 사용

```java
@WebMvcTest(ExampleController.class)
class ExampleControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;  // ✅ Spring이 제공하는 ObjectMapper

    @Test
    void createExample_ShouldReturn201() throws Exception {
        // Given
        ExampleApiRequest request = new ExampleApiRequest("Test");

        // When: ObjectMapper로 JSON 변환
        mockMvc.perform(post("/api/v1/examples")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))  // ✅
            .andExpect(status().isCreated());
    }
}
```

### 8.4 @ParameterizedTest로 Edge Case 테스트

**목적**: 다양한 Validation 케이스를 한 번에 테스트

```java
@ParameterizedTest
@ValueSource(strings = {"", "   ", "\t", "\n"})
@DisplayName("Example 생성 - Validation 실패 (빈 문자열 변형)")
void createExample_ShouldReturn400_WhenMessageIsBlankVariations(String invalidMessage) throws Exception {
    // Given: 빈 문자열 변형 ("", "   ", "\t", "\n")
    String requestJson = String.format("""
        {
            "message": "%s"
        }
        """, invalidMessage);

    // When & Then: 모두 HTTP 400
    mockMvc.perform(post("/api/v1/examples")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson))
        .andExpect(status().isBadRequest());

    verify(createExampleUseCase, never()).execute(any());
}

@ParameterizedTest
@CsvSource({
    "-1, ID는 양수여야 합니다",
    "0, ID는 양수여야 합니다"
})
@DisplayName("Example 조회 - Validation 실패 (음수 및 0)")
void getExample_ShouldReturn400_WhenIdIsInvalid(Long invalidId, String expectedMessage) throws Exception {
    // When & Then
    mockMvc.perform(get("/api/v1/examples/{id}", invalidId))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.detail").value(expectedMessage));

    verify(getExampleQueryService, never()).getById(any());
}
```

### 8.5 @Nested로 테스트 그룹화

**목적**: 관련 테스트를 논리적으로 그룹화

```java
@WebMvcTest(ExampleController.class)
@DisplayName("ExampleController Unit Test")
class ExampleControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateExampleUseCase createExampleUseCase;

    @Nested
    @DisplayName("POST /api/v1/examples (생성)")
    class CreateExampleTests {

        @Test
        @DisplayName("정상 케이스 - HTTP 201")
        void shouldReturn201_WhenValidRequest() throws Exception {
            // ...
        }

        @Test
        @DisplayName("Validation 실패 - HTTP 400")
        void shouldReturn400_WhenInvalidRequest() throws Exception {
            // ...
        }
    }

    @Nested
    @DisplayName("GET /api/v1/examples/{id} (조회)")
    class GetExampleTests {

        @Test
        @DisplayName("정상 케이스 - HTTP 200")
        void shouldReturn200_WhenExampleExists() throws Exception {
            // ...
        }

        @Test
        @DisplayName("존재하지 않음 - HTTP 404")
        void shouldReturn404_WhenNotFound() throws Exception {
            // ...
        }
    }
}
```

### 8.6 MockMvc ResultActions 재사용

**최적화**: ResultActions를 변수에 저장하여 재사용

```java
@Test
void createExample_ShouldReturnValidResponse() throws Exception {
    // Given
    when(exampleApiMapper.toCreateCommand(any())).thenReturn(command);
    when(createExampleUseCase.execute(any())).thenReturn(response);
    when(exampleApiMapper.toApiResponse(any())).thenReturn(apiResponse);

    // When: ResultActions 저장
    ResultActions result = mockMvc.perform(post("/api/v1/examples")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestJson));

    // Then: 여러 검증을 체이닝
    result
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").exists())
        .andExpect(jsonPath("$.timestamp").exists())
        .andDo(print());  // 마지막에 출력
}
```

### 8.7 테스트 속도 최적화

**팁**:
1. **@WebMvcTest 사용**: @SpringBootTest보다 10배 빠름
2. **필요한 Controller만 로딩**: `@WebMvcTest(ExampleController.class)`
3. **Mock 최소화**: 필요한 의존성만 Mock
4. **병렬 실행**: JUnit 5 parallel execution

```properties
# junit-platform.properties
junit.jupiter.execution.parallel.enabled = true
junit.jupiter.execution.parallel.mode.default = concurrent
```

---

## 요약

### Unit Test 체크리스트

#### 프로젝트 설정
- [ ] `spring-boot-starter-test` 의존성 추가 (MockMvc 포함)
- [ ] @WebMvcTest 어노테이션 사용 (빠른 테스트)
- [ ] @MockBean으로 의존성 Mock

#### 테스트 작성
- [ ] Given-When-Then 패턴 사용
- [ ] 한 테스트는 하나의 시나리오만 검증
- [ ] @DisplayName으로 명확한 테스트 이름
- [ ] ObjectMapper로 JSON 변환

#### Mocking
- [ ] Controller의 모든 의존성 Mock (UseCase, Mapper)
- [ ] given().willReturn() (BDD 스타일 권장)
- [ ] verify()로 Mock 호출 검증
- [ ] ArgumentMatcher 적절히 사용 (any, eq, argThat)

#### 검증 범위
- [ ] HTTP 상태 코드 검증 (200, 201, 400, 404 등)
- [ ] Response Body 검증 (JsonPath)
- [ ] ApiResponse<T> 구조 검증
- [ ] Validation 실패 케이스 테스트
- [ ] DomainException 처리 테스트
- [ ] PathVariable/QueryParameter 바인딩 테스트

#### Best Practices
- [ ] @Nested로 테스트 그룹화
- [ ] @ParameterizedTest로 Edge Case 커버
- [ ] 테스트 독립성 보장 (실행 순서 무관)
- [ ] 병렬 실행 설정 (속도 최적화)

---

## 참고 문서

### REST API Layer 컨벤션
- [ArchUnit 테스트 가이드](./01_archunit-test-guide.md) - 아키텍처 자동 검증
- [Integration Test 가이드](./02_integration-test-guide.md) - 통합 테스트
- [Controller 디자인](../controller-design/) - Controller 설계 가이드
- [DTO 패턴](../dto-patterns/) - Request/Response DTO 가이드

### 외부 링크
- [Spring MockMvc 공식 문서](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#spring-mvc-test-framework)
- [Mockito 공식 문서](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [JUnit 5 공식 문서](https://junit.org/junit5/docs/current/user-guide/)

---

**✅ 이 가이드를 따르면 빠르고 견고한 REST API Unit Test를 작성할 수 있습니다!**
