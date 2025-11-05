# REST API Documentation 가이드 (Spring REST Docs)

> **Spring REST Docs는 테스트 기반 API 문서화를 제공합니다.**
> 테스트가 통과해야만 문서가 생성되므로 항상 최신 상태를 유지합니다.

## 목차
1. [Spring REST Docs 소개](#1-spring-rest-docs-소개)
2. [프로젝트 설정](#2-프로젝트-설정)
3. [AbstractRestDocsTest 베이스 클래스](#3-abstractrestdocstest-베이스-클래스)
4. [API 문서화 패턴](#4-api-문서화-패턴)
5. [Snippet 생성](#5-snippet-생성)
6. [AsciiDoc 템플릿](#6-asciidoc-템플릿)
7. [OpenAPI 3.0 변환](#7-openapi-30-변환)
8. [Best Practices](#8-best-practices)

---

## 1. Spring REST Docs 소개

### 1.1 Spring REST Docs란?

**Spring REST Docs**는 **테스트 코드를 기반**으로 API 문서를 자동 생성하는 도구입니다.

**핵심 원칙**:
- ✅ **Test-Driven Documentation**: 테스트 통과 = 문서 정확성 보장
- ✅ **Always Up-to-Date**: API 변경 시 테스트 실패 → 문서 자동 갱신
- ✅ **Production-Ready**: AsciiDoc 또는 Markdown 기반 고품질 문서
- ✅ **OpenAPI 변환**: Swagger UI 통합 가능

### 1.2 Swagger vs Spring REST Docs

| 항목 | Swagger (@Operation) | Spring REST Docs |
|------|---------------------|------------------|
| **문서 생성 방식** | 코드 어노테이션 | 테스트 코드 |
| **정확성** | 낮음 (검증 없음) | 높음 (테스트 통과 필수) |
| **코드 침투성** | 높음 (어노테이션 많음) | 낮음 (비침투적) |
| **유지보수** | 어려움 (수동 동기화) | 쉬움 (자동 동기화) |
| **생성 문서** | OpenAPI JSON | AsciiDoc/Markdown |
| **실행 시점** | 런타임 | 빌드 타임 |

**권장**: **Spring REST Docs (문서 정확성) + Swagger UI (인터랙티브)**

### 1.3 작동 원리

```
1. Controller 테스트 작성
   ↓
2. 테스트 실행 시 Snippet 생성
   (request/response 자동 기록)
   ↓
3. AsciiDoc 템플릿에서 Snippet 참조
   (operation::get-example[snippets='curl-request,http-response'])
   ↓
4. HTML 문서 생성
   (build/docs/asciidoc/api-guide.html)
   ↓
5. OpenAPI 3.0 변환 (선택)
   (springdoc-openapi-docs-plugin)
```

---

## 2. 프로젝트 설정

### 2.1 의존성 추가

**`build.gradle.kts` (bootstrap-web-api 모듈)**:
```kotlin
plugins {
    id("org.asciidoctor.jvm.convert") version "4.0.2"  // AsciiDoc 변환
}

configurations {
    create("asciidoctorExt")  // REST Docs 확장 설정
}

dependencies {
    // Spring REST Docs
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")

    // AsciiDoc 확장 (operation snippet 지원)
    "asciidoctorExt"("org.springframework.restdocs:spring-restdocs-asciidoctor")
}

// ========================================
// REST Docs Snippet 생성 경로
// ========================================
val snippetsDir = file("build/generated-snippets")

tasks.test {
    outputs.dir(snippetsDir)  // 테스트 실행 시 Snippet 생성
}

// ========================================
// AsciiDoc → HTML 변환
// ========================================
tasks.asciidoctor {
    inputs.dir(snippetsDir)  // Snippet 디렉토리 참조
    dependsOn(tasks.test)  // 테스트 후 문서 생성

    configurations("asciidoctorExt")  // REST Docs 확장 사용

    baseDirFollowsSourceFile()  // include 경로를 소스 파일 기준으로

    doFirst {
        delete("build/docs/asciidoc")  // 기존 문서 삭제
    }
}

// ========================================
// 생성된 HTML을 JAR에 포함
// ========================================
tasks.bootJar {
    dependsOn(tasks.asciidoctor)  // AsciiDoc 변환 후 JAR 생성

    from("${tasks.asciidoctor.get().outputDir}") {
        into("static/docs")  // /static/docs/api-guide.html
    }
}
```

### 2.2 디렉토리 구조

```
bootstrap-web-api/
├── src/
│   ├── docs/
│   │   └── asciidoc/
│   │       ├── api-guide.adoc         # 메인 API 문서 (템플릿)
│   │       ├── overview.adoc          # 개요
│   │       └── example/
│   │           └── example-api.adoc   # Example API 상세
│   └── test/
│       └── java/
│           └── com/ryuqq/bootstrap/
│               ├── docs/
│               │   ├── AbstractRestDocsTest.java  # 베이스 클래스
│               │   └── example/
│               │       └── ExampleControllerDocsTest.java
│               └── integration/
└── build/
    ├── generated-snippets/            # 테스트 실행 시 생성 (자동)
    │   └── example/
    │       ├── create-example/
    │       │   ├── curl-request.adoc
    │       │   ├── http-request.adoc
    │       │   ├── http-response.adoc
    │       │   ├── request-fields.adoc
    │       │   └── response-fields.adoc
    │       └── get-example/
    └── docs/
        └── asciidoc/
            └── api-guide.html         # 최종 HTML 문서
```

---

## 3. AbstractRestDocsTest 베이스 클래스

### 3.1 베이스 클래스 목적

**AbstractRestDocsTest**는 모든 REST Docs 테스트의 부모 클래스입니다.

**제공 기능**:
- 📄 **REST Docs 설정**: MockMvc + RestDocumentationResultHandler
- 🎨 **Snippet 포맷팅**: 예쁜 JSON 출력, 한글 인코딩
- 📁 **디렉토리 관리**: Snippet 경로 자동 설정
- 🧪 **공통 문서화**: Request/Response 공통 필드

### 3.2 AbstractRestDocsTest 구현

**파일**: `bootstrap/bootstrap-web-api/src/test/java/com/ryuqq/bootstrap/docs/AbstractRestDocsTest.java`

```java
package com.ryuqq.bootstrap.docs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;

/**
 * REST Docs 테스트 베이스 클래스
 *
 * <p>모든 REST API 문서화 테스트의 부모 클래스입니다.</p>
 *
 * <p><strong>제공 기능:</strong></p>
 * <ul>
 *   <li>MockMvc + REST Docs 설정</li>
 *   <li>Pretty Print (예쁜 JSON 포맷)</li>
 *   <li>공통 Snippet 디렉토리 관리</li>
 *   <li>Request/Response 전처리 (URI 정리, 인코딩)</li>
 * </ul>
 *
 * <p><strong>사용 방법:</strong></p>
 * <pre>{@code
 * @WebMvcTest(ExampleController.class)
 * class ExampleControllerDocsTest extends AbstractRestDocsTest {
 *
 *     @Test
 *     void createExample() throws Exception {
 *         mockMvc.perform(post("/api/v1/examples")
 *                 .contentType(MediaType.APPLICATION_JSON)
 *                 .content(requestJson))
 *             .andExpect(status().isCreated())
 *             .andDo(restDocs.document(
 *                 requestFields(
 *                     fieldWithPath("message").description("메시지")
 *                 ),
 *                 responseFields(
 *                     fieldWithPath("success").description("성공 여부"),
 *                     fieldWithPath("data.id").description("Example ID"),
 *                     fieldWithPath("data.message").description("메시지")
 *                 )
 *             ));
 *     }
 * }
 * }</pre>
 *
 * @author Claude Code
 * @since 2025-10-31
 */
@ExtendWith(RestDocumentationExtension.class)
public abstract class AbstractRestDocsTest {

    protected MockMvc mockMvc;
    protected RestDocumentationResultHandler restDocs;

    /**
     * MockMvc + REST Docs 설정
     *
     * <p>각 테스트 전에 MockMvc를 초기화하고 REST Docs를 설정합니다.</p>
     *
     * @param webApplicationContext Spring Web Context
     * @param restDocumentation REST Docs Context Provider
     */
    @BeforeEach
    void setUpRestDocs(
        WebApplicationContext webApplicationContext,
        RestDocumentationContextProvider restDocumentation
    ) {
        this.restDocs = MockMvcRestDocumentation.document(
            "{class-name}/{method-name}",  // Snippet 경로 패턴
            preprocessRequest(
                modifyUris()
                    .scheme("https")
                    .host("api.example.com")  // 실제 API 도메인으로 변경
                    .removePort(),
                prettyPrint()  // 예쁜 JSON 포맷
            ),
            preprocessResponse(prettyPrint())  // 예쁜 JSON 포맷
        );

        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .apply(documentationConfiguration(restDocumentation)
                .uris()
                    .withScheme("https")
                    .withHost("api.example.com")
                    .withPort(443)
                .and()
                .operationPreprocessors()
                    .withRequestDefaults(prettyPrint())
                    .withResponseDefaults(prettyPrint())
            )
            .alwaysDo(restDocs)  // 모든 요청에 REST Docs 적용
            .build();
    }

    /**
     * Request 전처리기
     *
     * <p>Request를 문서화하기 전에 URI, 포맷 등을 정리합니다.</p>
     *
     * @return OperationRequestPreprocessor
     */
    protected OperationRequestPreprocessor preprocessRequest() {
        return preprocessRequest(
            modifyUris()
                .scheme("https")
                .host("api.example.com")
                .removePort(),
            prettyPrint()
        );
    }

    /**
     * Response 전처리기
     *
     * <p>Response를 문서화하기 전에 포맷을 정리합니다.</p>
     *
     * @return OperationResponsePreprocessor
     */
    protected OperationResponsePreprocessor preprocessResponse() {
        return preprocessResponse(prettyPrint());
    }
}
```

---

## 4. API 문서화 패턴

### 4.1 POST (Command 생성) 문서화

```java
package com.ryuqq.bootstrap.docs.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.adapter.in.rest.example.controller.ExampleController;
import com.ryuqq.adapter.in.rest.example.dto.request.ExampleApiRequest;
import com.ryuqq.adapter.in.rest.example.dto.response.ExampleApiResponse;
import com.ryuqq.adapter.in.rest.example.mapper.ExampleApiMapper;
import com.ryuqq.application.example.dto.command.CreateExampleCommand;
import com.ryuqq.application.example.dto.response.ExampleResponse;
import com.ryuqq.application.example.port.in.CreateExampleUseCase;
import com.ryuqq.bootstrap.docs.AbstractRestDocsTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ExampleController REST Docs 테스트
 *
 * @author Claude Code
 * @since 2025-10-31
 */
@WebMvcTest(ExampleController.class)
@DisplayName("ExampleController REST Docs")
class ExampleControllerDocsTest extends AbstractRestDocsTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateExampleUseCase createExampleUseCase;

    @MockBean
    private ExampleApiMapper exampleApiMapper;

    @Test
    @DisplayName("Example 생성 API 문서화")
    void createExample() throws Exception {
        // Given
        ExampleApiRequest request = new ExampleApiRequest("Hello World");

        when(exampleApiMapper.toCreateCommand(any())).thenReturn(mock(CreateExampleCommand.class));
        when(createExampleUseCase.execute(any())).thenReturn(new ExampleResponse(1L, "Hello World"));
        when(exampleApiMapper.toApiResponse(any())).thenReturn(new ExampleApiResponse(1L, "Hello World"));

        // When & Then
        mockMvc.perform(post("/api/v1/examples")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andDo(document("example/create-example",  // Snippet 경로
                requestFields(
                    fieldWithPath("message")
                        .description("메시지 (1-500자)")
                        .attributes(key("constraints").value("Not Blank, 1-500자"))
                ),
                responseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("data").description("응답 데이터"),
                    fieldWithPath("data.id").description("Example ID"),
                    fieldWithPath("data.message").description("메시지"),
                    fieldWithPath("error").description("에러 정보 (성공 시 null)").optional(),
                    fieldWithPath("timestamp").description("응답 시간 (ISO 8601)"),
                    fieldWithPath("requestId").description("요청 추적 ID (UUID)")
                )
            ));
    }
}
```

### 4.2 GET (Query 조회) 문서화

```java
@Test
@DisplayName("Example 단건 조회 API 문서화")
void getExample() throws Exception {
    // Given
    Long exampleId = 1L;

    when(exampleApiMapper.toGetQuery(anyLong())).thenReturn(mock(ExampleQuery.class));
    when(getExampleQueryService.getById(any())).thenReturn(
        new ExampleDetailResponse(
            1L,
            "Hello World",
            "ACTIVE",
            LocalDateTime.now(),
            LocalDateTime.now()
        )
    );
    when(exampleApiMapper.toDetailApiResponse(any())).thenReturn(
        new ExampleDetailApiResponse(
            1L,
            "Hello World",
            "ACTIVE",
            LocalDateTime.now(),
            LocalDateTime.now()
        )
    );

    // When & Then
    mockMvc.perform(get("/api/v1/examples/{id}", exampleId))
        .andExpect(status().isOk())
        .andDo(document("example/get-example",
            pathParameters(
                parameterWithName("id").description("Example ID (양수)")
            ),
            responseFields(
                fieldWithPath("success").description("성공 여부"),
                fieldWithPath("data").description("응답 데이터"),
                fieldWithPath("data.id").description("Example ID"),
                fieldWithPath("data.message").description("메시지"),
                fieldWithPath("data.status").description("상태 (ACTIVE, INACTIVE, DELETED)"),
                fieldWithPath("data.createdAt").description("생성 시간 (ISO 8601)"),
                fieldWithPath("data.updatedAt").description("수정 시간 (ISO 8601)"),
                fieldWithPath("error").description("에러 정보 (성공 시 null)").optional(),
                fieldWithPath("timestamp").description("응답 시간"),
                fieldWithPath("requestId").description("요청 추적 ID")
            )
        ));
}
```

### 4.3 Pagination (Query Parameter) 문서화

```java
@Test
@DisplayName("Example 검색 API 문서화 (Cursor 기반)")
void searchExamplesByCursor() throws Exception {
    // Given
    when(exampleApiMapper.toSearchQuery(any())).thenReturn(mock(ExampleSearchQuery.class));
    when(searchExampleQueryService.searchByCursor(any())).thenReturn(
        new SliceResponse<>(
            List.of(
                new ExampleDetailResponse(1L, "Example 1", "ACTIVE", LocalDateTime.now(), LocalDateTime.now()),
                new ExampleDetailResponse(2L, "Example 2", "ACTIVE", LocalDateTime.now(), LocalDateTime.now())
            ),
            20,
            true,
            "next-cursor-abc"
        )
    );
    when(exampleApiMapper.toSliceApiResponse(any())).thenReturn(mock(ExampleSliceApiResponse.class));

    // When & Then
    mockMvc.perform(get("/api/v1/examples")
            .param("cursor", "abc")
            .param("size", "20")
            .param("sortBy", "createdAt")
            .param("sortDirection", "DESC"))
        .andExpect(status().isOk())
        .andDo(document("example/search-examples-cursor",
            queryParameters(
                parameterWithName("cursor").description("커서 (첫 페이지는 생략)").optional(),
                parameterWithName("size").description("페이지 크기 (1-100, 기본 20)").optional(),
                parameterWithName("sortBy").description("정렬 기준 (createdAt, updatedAt)").optional(),
                parameterWithName("sortDirection").description("정렬 방향 (ASC, DESC)").optional()
            ),
            responseFields(
                fieldWithPath("success").description("성공 여부"),
                fieldWithPath("data").description("응답 데이터"),
                fieldWithPath("data.content[]").description("Example 목록"),
                fieldWithPath("data.content[].id").description("Example ID"),
                fieldWithPath("data.content[].message").description("메시지"),
                fieldWithPath("data.content[].status").description("상태"),
                fieldWithPath("data.content[].createdAt").description("생성 시간"),
                fieldWithPath("data.content[].updatedAt").description("수정 시간"),
                fieldWithPath("data.size").description("페이지 크기"),
                fieldWithPath("data.hasNext").description("다음 페이지 존재 여부"),
                fieldWithPath("data.nextCursor").description("다음 페이지 커서 (없으면 null)").optional(),
                fieldWithPath("error").description("에러 정보").optional(),
                fieldWithPath("timestamp").description("응답 시간"),
                fieldWithPath("requestId").description("요청 추적 ID")
            )
        ));
}
```

---

## 5. Snippet 생성

### 5.1 생성되는 Snippet 종류

테스트 실행 시 `build/generated-snippets/{identifier}/` 디렉토리에 생성:

- **curl-request.adoc**: cURL 명령어
- **http-request.adoc**: HTTP 요청 전체
- **http-response.adoc**: HTTP 응답 전체
- **httpie-request.adoc**: HTTPie 명령어
- **request-body.adoc**: 요청 Body
- **response-body.adoc**: 응답 Body
- **request-fields.adoc**: 요청 필드 상세 (문서화한 경우)
- **response-fields.adoc**: 응답 필드 상세 (문서화한 경우)
- **path-parameters.adoc**: PathVariable (문서화한 경우)
- **query-parameters.adoc**: Query Parameter (문서화한 경우)

### 5.2 Snippet 예시

**`curl-request.adoc`**:
```bash
$ curl 'https://api.example.com/api/v1/examples' -i -X POST \
    -H 'Content-Type: application/json' \
    -d '{
  "message" : "Hello World"
}'
```

**`http-request.adoc`**:
```http
POST /api/v1/examples HTTP/1.1
Content-Type: application/json
Host: api.example.com

{
  "message" : "Hello World"
}
```

**`http-response.adoc`**:
```http
HTTP/1.1 201 Created
Content-Type: application/json

{
  "success" : true,
  "data" : {
    "id" : 1,
    "message" : "Hello World"
  },
  "error" : null,
  "timestamp" : "2025-10-31T10:30:00",
  "requestId" : "abc-123-def"
}
```

**`request-fields.adoc`**:
| Path | Type | Description | Constraints |
|------|------|-------------|-------------|
| message | String | 메시지 (1-500자) | Not Blank, 1-500자 |

---

## 6. AsciiDoc 템플릿

### 6.1 메인 API 문서

**파일**: `src/docs/asciidoc/api-guide.adoc`

```asciidoc
= REST API 가이드
Claude Code <noreply@example.com>
v1.0.0, 2025-10-31
:doctype: book
:icons: font
:source-highlighter: highlightjs
:toc: left
:toclevels: 3
:sectlinks:
:operation-curl-request-title: cURL Example
:operation-http-request-title: HTTP Request
:operation-http-response-title: HTTP Response

[[overview]]
= 개요

이 문서는 Spring Standards 프로젝트의 REST API를 설명합니다.

[[overview-http-verbs]]
== HTTP 메서드

|===
| HTTP 메서드 | 용도

| `GET`
| 리소스 조회

| `POST`
| 리소스 생성

| `PUT`
| 리소스 전체 수정

| `PATCH`
| 리소스 부분 수정

| `DELETE`
| 리소스 삭제
|===

[[overview-http-status-codes]]
== HTTP 상태 코드

|===
| 상태 코드 | 의미

| `200 OK`
| 요청 성공

| `201 Created`
| 리소스 생성 성공

| `400 Bad Request`
| 잘못된 요청 (Validation 실패)

| `404 Not Found`
| 리소스를 찾을 수 없음

| `409 Conflict`
| 리소스 충돌 (중복)

| `500 Internal Server Error`
| 서버 오류
|===

[[overview-response-structure]]
== 응답 구조

모든 API 응답은 `ApiResponse<T>` 구조를 따릅니다.

**성공 응답**:
[source,json]
----
{
  "success": true,
  "data": { ... },
  "error": null,
  "timestamp": "2025-10-31T10:30:00",
  "requestId": "abc-123-def"
}
----

**실패 응답**:
[source,json]
----
{
  "success": false,
  "data": null,
  "error": {
    "code": "EXAMPLE.NOT_FOUND",
    "title": "Example Not Found",
    "detail": "Example with ID 1 not found"
  },
  "timestamp": "2025-10-31T10:30:00",
  "requestId": "abc-123-def"
}
----

[[example-api]]
= Example API

[[example-create]]
== Example 생성

`POST /api/v1/examples`

**Request**
include::{snippets}/example/create-example/http-request.adoc[]

**Request Fields**
include::{snippets}/example/create-example/request-fields.adoc[]

**Response**
include::{snippets}/example/create-example/http-response.adoc[]

**Response Fields**
include::{snippets}/example/create-example/response-fields.adoc[]

**cURL Example**
include::{snippets}/example/create-example/curl-request.adoc[]

[[example-get]]
== Example 조회

`GET /api/v1/examples/{id}`

**Request**
include::{snippets}/example/get-example/http-request.adoc[]

**Path Parameters**
include::{snippets}/example/get-example/path-parameters.adoc[]

**Response**
include::{snippets}/example/get-example/http-response.adoc[]

**Response Fields**
include::{snippets}/example/get-example/response-fields.adoc[]

[[example-search]]
== Example 검색 (Cursor)

`GET /api/v1/examples`

**Request**
include::{snippets}/example/search-examples-cursor/http-request.adoc[]

**Query Parameters**
include::{snippets}/example/search-examples-cursor/query-parameters.adoc[]

**Response**
include::{snippets}/example/search-examples-cursor/http-response.adoc[]

**Response Fields**
include::{snippets}/example/search-examples-cursor/response-fields.adoc[]
```

### 6.2 문서 생성 및 확인

**Gradle 명령어**:
```bash
# 1. 테스트 실행 → Snippet 생성
./gradlew test

# 2. AsciiDoc → HTML 변환
./gradlew asciidoctor

# 3. 생성된 문서 확인
open build/docs/asciidoc/api-guide.html
```

**빌드 시 자동 실행**:
```bash
# JAR 빌드 시 자동으로 문서 생성 및 포함
./gradlew bootJar

# JAR 내부에 /static/docs/api-guide.html 포함됨
```

**실행 중인 애플리케이션에서 문서 확인**:
```
http://localhost:8080/docs/api-guide.html
```

---

## 7. OpenAPI 3.0 변환

### 7.1 RestDocs OpenAPI 플러그인

**목적**: Spring REST Docs Snippet → OpenAPI 3.0 JSON

**의존성 추가**:
```kotlin
plugins {
    id("com.epages.restdocs-api-spec") version "0.18.4"  // REST Docs → OpenAPI
}

dependencies {
    testImplementation("com.epages.restdocs-api-spec:restdocs-api-spec-mockmvc:0.18.4")
}

// OpenAPI 생성 설정
openapi3 {
    setServer("https://api.example.com")  // API 서버 URL
    title = "Spring Standards API"
    description = "REST API Documentation"
    version = "1.0.0"
    format = "json"  // JSON 또는 YAML
}

tasks.named("openapi3") {
    dependsOn(tasks.test)  // 테스트 후 OpenAPI 생성
}
```

### 7.2 OpenAPI 생성

**명령어**:
```bash
# 1. 테스트 실행
./gradlew test

# 2. OpenAPI JSON 생성
./gradlew openapi3

# 3. 확인
cat build/api-spec/openapi3.json
```

**결과** (`build/api-spec/openapi3.json`):
```json
{
  "openapi": "3.0.1",
  "info": {
    "title": "Spring Standards API",
    "description": "REST API Documentation",
    "version": "1.0.0"
  },
  "servers": [
    {
      "url": "https://api.example.com"
    }
  ],
  "paths": {
    "/api/v1/examples": {
      "post": {
        "tags": ["Example API"],
        "operationId": "createExample",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/ExampleApiRequest"
              }
            }
          }
        },
        "responses": {
          "201": {
            "description": "Created",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ApiResponse-ExampleApiResponse"
                }
              }
            }
          }
        }
      }
    }
  },
  "components": {
    "schemas": {
      "ExampleApiRequest": {
        "type": "object",
        "properties": {
          "message": {
            "type": "string",
            "description": "메시지 (1-500자)"
          }
        },
        "required": ["message"]
      }
    }
  }
}
```

### 7.3 Swagger UI 통합

**의존성 추가**:
```kotlin
dependencies {
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
}
```

**설정** (`application.yml`):
```yaml
springdoc:
  api-docs:
    enabled: true
    path: /v3/api-docs  # OpenAPI JSON 경로
  swagger-ui:
    enabled: true
    path: /swagger-ui.html  # Swagger UI 경로
    operations-sorter: alpha
```

**접근**:
```
http://localhost:8080/swagger-ui.html
```

---

## 8. Best Practices

### 8.1 문서화 필수 필드

**원칙**: **Request/Response의 모든 필드를 문서화**

```java
@Test
void createExample() throws Exception {
    mockMvc.perform(post("/api/v1/examples")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson))
        .andExpect(status().isCreated())
        .andDo(document("example/create-example",
            requestFields(
                // ✅ 모든 필드 문서화
                fieldWithPath("message").description("메시지")
            ),
            responseFields(
                // ✅ 모든 필드 문서화 (ApiResponse 공통 필드 포함)
                fieldWithPath("success").description("성공 여부"),
                fieldWithPath("data").description("응답 데이터"),
                fieldWithPath("data.id").description("Example ID"),
                fieldWithPath("data.message").description("메시지"),
                fieldWithPath("error").description("에러 정보").optional(),
                fieldWithPath("timestamp").description("응답 시간"),
                fieldWithPath("requestId").description("요청 추적 ID")
            )
        ));
}
```

**테스트 실패 예시**:
```
org.springframework.restdocs.snippet.SnippetException:
The following parts of the payload were not documented:
{
  "timestamp" : "2025-10-31T10:30:00"
}
```
→ **문서화하지 않은 필드가 있으면 테스트 실패** (정확성 보장)

### 8.2 공통 필드 재사용

**Helper 메서드**:
```java
public abstract class AbstractRestDocsTest {

    /**
     * ApiResponse<T> 공통 필드
     */
    protected List<FieldDescriptor> apiResponseFields(FieldDescriptor... dataFields) {
        List<FieldDescriptor> fields = new ArrayList<>();
        fields.add(fieldWithPath("success").description("성공 여부"));
        fields.add(fieldWithPath("data").description("응답 데이터"));
        fields.addAll(Arrays.asList(dataFields));
        fields.add(fieldWithPath("error").description("에러 정보 (성공 시 null)").optional());
        fields.add(fieldWithPath("timestamp").description("응답 시간 (ISO 8601)"));
        fields.add(fieldWithPath("requestId").description("요청 추적 ID (UUID)"));
        return fields;
    }
}
```

**사용**:
```java
@Test
void createExample() throws Exception {
    mockMvc.perform(post("/api/v1/examples")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson))
        .andExpect(status().isCreated())
        .andDo(document("example/create-example",
            requestFields(
                fieldWithPath("message").description("메시지")
            ),
            responseFields(
                apiResponseFields(  // ✅ 공통 필드 재사용
                    fieldWithPath("data.id").description("Example ID"),
                    fieldWithPath("data.message").description("메시지")
                )
            )
        ));
}
```

### 8.3 Constraints 문서화

**Validation 제약 조건 명시**:
```java
@Test
void createExample() throws Exception {
    mockMvc.perform(post("/api/v1/examples")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson))
        .andExpect(status().isCreated())
        .andDo(document("example/create-example",
            requestFields(
                fieldWithPath("message")
                    .description("메시지")
                    .attributes(
                        key("constraints").value("Not Blank, 1-500자"),  // ✅ 제약 조건
                        key("example").value("Hello World")  // 예시
                    )
            ),
            responseFields(
                apiResponseFields(
                    fieldWithPath("data.id")
                        .description("Example ID")
                        .type(JsonFieldType.NUMBER),  // ✅ 타입 명시
                    fieldWithPath("data.message")
                        .description("메시지")
                        .type(JsonFieldType.STRING)
                )
            )
        ));
}
```

### 8.4 문서 버전 관리

**디렉토리 구조**:
```
src/docs/asciidoc/
├── v1/
│   ├── api-guide.adoc
│   ├── example-api.adoc
│   └── order-api.adoc
└── v2/
    ├── api-guide.adoc
    └── example-api.adoc
```

**Gradle 설정**:
```kotlin
tasks.asciidoctor {
    sources {
        include("v1/api-guide.adoc")  // v1 문서만 빌드
    }
}
```

### 8.5 CI/CD 통합

**GitHub Actions**:
```yaml
name: Generate API Docs

on: [push, pull_request]

jobs:
  docs:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
      - name: Generate REST Docs
        run: ./gradlew asciidoctor
      - name: Deploy to GitHub Pages
        uses: peaceiris/actions-gh-pages@v3
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
          publish_dir: ./build/docs/asciidoc
```

---

## 요약

### REST Docs 체크리스트

#### 프로젝트 설정
- [ ] `spring-restdocs-mockmvc` 의존성 추가
- [ ] AsciiDoctor Gradle 플러그인 설정
- [ ] Snippet 생성 경로 설정 (`build/generated-snippets`)
- [ ] `AbstractRestDocsTest` 베이스 클래스 작성

#### 문서화 테스트
- [ ] `@WebMvcTest` + `AbstractRestDocsTest` 상속
- [ ] MockMvc로 API 호출
- [ ] `.andDo(document())` 으로 Snippet 생성
- [ ] Request/Response 모든 필드 문서화
- [ ] Validation 제약 조건 문서화

#### AsciiDoc 작성
- [ ] `api-guide.adoc` 템플릿 작성
- [ ] `include::{snippets}/...` 으로 Snippet 참조
- [ ] HTTP 메서드, 상태 코드 표 작성
- [ ] 응답 구조 예시 작성

#### 문서 생성
- [ ] `./gradlew test` (Snippet 생성)
- [ ] `./gradlew asciidoctor` (HTML 생성)
- [ ] `./gradlew bootJar` (JAR에 문서 포함)
- [ ] `http://localhost:8080/docs/api-guide.html` 확인

#### OpenAPI 변환 (선택)
- [ ] `restdocs-api-spec` 플러그인 추가
- [ ] `./gradlew openapi3` (OpenAPI JSON 생성)
- [ ] Swagger UI 통합

---

## 참고 문서

### REST API Layer 컨벤션
- [ArchUnit 테스트 가이드](./01_archunit-test-guide.md)
- [Integration Test 가이드](./02_integration-test-guide.md)
- [Unit Test 가이드](./03_unit-test-guide.md)

### 외부 링크
- [Spring REST Docs 공식 문서](https://docs.spring.io/spring-restdocs/docs/current/reference/html5/)
- [AsciiDoc 문법](https://asciidoctor.org/docs/asciidoc-syntax-quick-reference/)
- [RestDocs OpenAPI 플러그인](https://github.com/ePages-de/restdocs-api-spec)

---

**✅ 이 가이드를 따르면 항상 최신 상태의 정확한 API 문서를 자동으로 생성할 수 있습니다!**
