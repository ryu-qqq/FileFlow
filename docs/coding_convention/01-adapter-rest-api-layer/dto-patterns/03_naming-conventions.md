# REST API DTO 네이밍 규칙

## 개요

REST API Layer의 DTO는 명확한 네이밍 규칙을 따라야 하며, ArchUnit으로 자동 검증됩니다.

---

## 1. Request DTO 네이밍 규칙

### 규칙

1. **record 타입 필수**
2. **ApiRequest 접미사 필수**
3. **dto.request 패키지에 위치**

### 예시

```java
// ✅ 올바른 네이밍
package com.ryuqq.adapter.in.rest.example.dto.request;

public record ExampleApiRequest(
    String message
) {
}

public record CreateOrderApiRequest(
    Long userId,
    List<OrderItemApiRequest> items
) {
    // 중첩 record도 ApiRequest 접미사 사용
    public record OrderItemApiRequest(
        Long productId,
        int quantity
    ) {
    }
}

public record ExampleSearchApiRequest(
    String message,
    String status,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Integer page,
    Integer size
) {
}
```

```java
// ❌ 잘못된 네이밍
package com.ryuqq.adapter.in.rest.example.dto.request;

// ❌ class 타입 (record 아님)
public class ExampleRequest {
    private String message;
}

// ❌ ApiRequest 접미사 없음
public record ExampleDto(String message) {
}

// ❌ Request 접미사만 있음
public record ExampleRequest(String message) {
}

// ❌ Dto 접미사 사용
public record ExampleRequestDto(String message) {
}
```

---

## 2. Response DTO 네이밍 규칙

### 규칙

1. **record 타입 필수**
2. **ApiResponse 접미사 필수**
3. **dto.response 패키지에 위치**

### 예시

```java
// ✅ 올바른 네이밍
package com.ryuqq.adapter.in.rest.example.dto.response;

public record ExampleApiResponse(
    String message
) {
    public static ExampleApiResponse fromResponse(String message) {
        return new ExampleApiResponse(message);
    }
}

public record ExampleDetailApiResponse(
    Long id,
    String message,
    String status,
    LocalDateTime createdAt
) {
    public static ExampleDetailApiResponse from(ExampleDetailResponse response) {
        return new ExampleDetailApiResponse(
            response.id(),
            response.message(),
            response.status(),
            response.createdAt()
        );
    }
}

// 페이징 응답
public record ExamplePageApiResponse(
    List<ExampleDetailApiResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
    public static ExamplePageApiResponse from(PageResponse<ExampleDetailResponse> pageResponse) {
        return new ExamplePageApiResponse(
            pageResponse.content().stream()
                .map(ExampleDetailApiResponse::from)
                .toList(),
            pageResponse.page(),
            pageResponse.size(),
            pageResponse.totalElements(),
            pageResponse.totalPages()
        );
    }
}
```

```java
// ❌ 잘못된 네이밍
package com.ryuqq.adapter.in.rest.example.dto.response;

// ❌ class 타입 (record 아님)
public class ExampleResponse {
    private String message;
}

// ❌ ApiResponse 접미사 없음
public record ExampleDto(String message) {
}

// ❌ Response 접미사만 있음
public record ExampleResponse(String message) {
}

// ❌ Dto 접미사 사용
public record ExampleResponseDto(String message) {
}
```

---

## 3. Mapper 네이밍 규칙

### 규칙

1. **ApiMapper 접미사 필수**
2. **@Component 어노테이션 필수**
3. **mapper 패키지에 위치**

### 예시

```java
// ✅ 올바른 네이밍
package com.ryuqq.adapter.in.rest.example.mapper;

import org.springframework.stereotype.Component;

@Component
public class ExampleApiMapper {

    // Request → Command/Query
    public CreateExampleCommand toCreateCommand(ExampleApiRequest request) {
        return CreateExampleCommand.of(request.message());
    }

    public SearchExampleQuery toSearchQuery(ExampleSearchApiRequest request) {
        return SearchExampleQuery.of(
            request.message(),
            request.status(),
            request.page(),
            request.size()
        );
    }

    // Application Response → REST API Response
    public ExampleApiResponse toApiResponse(ExampleResponse response) {
        return ExampleApiResponse.fromResponse(response.message());
    }

    public ExampleDetailApiResponse toDetailApiResponse(ExampleDetailResponse response) {
        return ExampleDetailApiResponse.from(response);
    }
}
```

```java
// ❌ 잘못된 네이밍
package com.ryuqq.adapter.in.rest.example.mapper;

// ❌ ApiMapper 접미사 없음
@Component
public class ExampleMapper {
}

// ❌ Mapper 접미사만 있음
@Component
public class ExampleRestMapper {
}

// ❌ @Component 어노테이션 없음
public class ExampleApiMapper {
}
```

---

## 4. ArchUnit 검증 규칙

### 위치

```
bootstrap/bootstrap-web-api/src/test/java/com/ryuqq/bootstrap/architecture/RestApiLayerRulesTest.java
```

### 자동 검증 항목

#### DTO 검증

```java
@Test
void requestDtosShouldBeRecords() {
    // Request DTO는 record 타입이어야 함
}

@Test
void responseDtosShouldBeRecords() {
    // Response DTO는 record 타입이어야 함
}

@Test
void requestDtosShouldHaveApiRequestSuffix() {
    // Request DTO는 ApiRequest 접미사 필수
}

@Test
void responseDtosShouldHaveApiResponseSuffix() {
    // Response DTO는 ApiResponse 접미사 필수
}

@Test
void dtoClassesShouldNotHaveDtoSuffix() {
    // DTO 또는 Dto 접미사 사용 금지
}
```

#### Mapper 검증

```java
@Test
void mappersShouldHaveApiMapperSuffix() {
    // Mapper는 ApiMapper 접미사 필수
}

@Test
void mappersShouldBeAnnotatedWithComponent() {
    // Mapper는 @Component 어노테이션 필수
}
```

---

## 5. 패키지 구조

```
adapter-in/rest-api/src/main/java/com/ryuqq/adapter/in/rest/
├── common/
│   ├── dto/
│   │   ├── ApiResponse.java       (공통 응답 래퍼)
│   │   └── ErrorInfo.java         (에러 정보)
│   └── mapper/
│       └── ErrorMapper.java       (에러 매퍼)
│
└── example/
    ├── controller/
    │   └── ExampleController.java
    ├── dto/
    │   ├── request/
    │   │   ├── ExampleApiRequest.java           ✅ ApiRequest 접미사
    │   │   └── ExampleSearchApiRequest.java     ✅ ApiRequest 접미사
    │   └── response/
    │       ├── ExampleApiResponse.java          ✅ ApiResponse 접미사
    │       ├── ExampleDetailApiResponse.java    ✅ ApiResponse 접미사
    │       ├── ExamplePageApiResponse.java      ✅ ApiResponse 접미사
    │       └── ExampleSliceApiResponse.java     ✅ ApiResponse 접미사
    └── mapper/
        └── ExampleApiMapper.java                ✅ ApiMapper 접미사
```

---

## 6. Controller에서의 사용 예시

```java
@RestController
@RequestMapping("/api/v1/examples")
public class ExampleController {

    private final ExampleApiMapper mapper;
    private final CreateExampleUseCase createExampleUseCase;
    private final GetExampleUseCase getExampleUseCase;

    /**
     * Example 생성
     *
     * @param request ExampleApiRequest (REST API Layer DTO)
     * @return ApiResponse<ExampleApiResponse> (공통 응답 래퍼)
     */
    @PostMapping
    public ApiResponse<ExampleApiResponse> createExample(
        @RequestBody @Valid ExampleApiRequest request
    ) {
        // 1. REST API Request → Application Command
        CreateExampleCommand command = mapper.toCreateCommand(request);

        // 2. UseCase 실행
        ExampleResponse response = createExampleUseCase.execute(command);

        // 3. Application Response → REST API Response
        ExampleApiResponse apiResponse = mapper.toApiResponse(response);

        return ApiResponse.success(apiResponse);
    }

    /**
     * Example 조회
     *
     * @param id Example ID
     * @return ApiResponse<ExampleDetailApiResponse>
     */
    @GetMapping("/{id}")
    public ApiResponse<ExampleDetailApiResponse> getExample(
        @PathVariable Long id
    ) {
        // 1. ID → Application Query
        GetExampleQuery query = mapper.toGetQuery(id);

        // 2. UseCase 실행
        ExampleDetailResponse response = getExampleUseCase.execute(query);

        // 3. Application Response → REST API Response
        ExampleDetailApiResponse apiResponse = mapper.toDetailApiResponse(response);

        return ApiResponse.success(apiResponse);
    }

    /**
     * Example 검색 (페이징)
     *
     * @param request ExampleSearchApiRequest
     * @return ApiResponse<ExamplePageApiResponse>
     */
    @GetMapping
    public ApiResponse<ExamplePageApiResponse> searchExamples(
        @ModelAttribute ExampleSearchApiRequest request
    ) {
        // 1. Search Request → Application Query
        SearchExampleQuery query = mapper.toSearchQuery(request);

        // 2. UseCase 실행
        PageResponse<ExampleDetailResponse> pageResponse = searchExampleUseCase.execute(query);

        // 3. Application PageResponse → REST API PageResponse
        ExamplePageApiResponse apiResponse = mapper.toPageApiResponse(pageResponse);

        return ApiResponse.success(apiResponse);
    }
}
```

---

## 7. 네이밍 규칙 체크리스트

### Request DTO
- [ ] record 타입 사용
- [ ] ApiRequest 접미사 사용
- [ ] dto.request 패키지에 위치
- [ ] DTO/Dto 접미사 사용 안 함

### Response DTO
- [ ] record 타입 사용
- [ ] ApiResponse 접미사 사용
- [ ] dto.response 패키지에 위치
- [ ] DTO/Dto 접미사 사용 안 함
- [ ] 정적 팩토리 메서드 제공 (from, of 등)

### Mapper
- [ ] ApiMapper 접미사 사용
- [ ] @Component 어노테이션 사용
- [ ] mapper 패키지에 위치
- [ ] Request → Command/Query 변환 메서드
- [ ] Application Response → REST API Response 변환 메서드

---

## 8. 이점

### 명확한 식별

```java
// 레이어 구분이 명확함
ExampleApiRequest          // REST API Layer
ExampleCommand             // Application Layer
ExampleDomain              // Domain Layer
ExampleJpaEntity           // Persistence Layer
```

### 자동 검증

```bash
# Gradle 빌드 시 ArchUnit이 자동으로 검증
./gradlew test

# 위반 시 빌드 실패
❌ Architecture Violation:
   Class ExampleRequest does not have simple name ending with 'ApiRequest'
```

### IDE 자동완성

```java
// "Api" 입력 시 REST API Layer DTO만 필터링
ExampleApiRequest
ExampleApiResponse
ExampleApiMapper
```

---

## 요약

| 타입 | 네이밍 규칙 | 타입 | 필수 어노테이션 | 패키지 |
|------|------------|------|----------------|--------|
| **Request DTO** | `*ApiRequest` | record | - | `dto.request` |
| **Response DTO** | `*ApiResponse` | record | - | `dto.response` |
| **Mapper** | `*ApiMapper` | class | `@Component` | `mapper` |

✅ **이 규칙은 ArchUnit으로 자동 검증되며, 위반 시 빌드가 실패합니다.**

✅ **모든 REST API Layer DTO와 Mapper는 이 규칙을 반드시 따라야 합니다. (Zero-Tolerance)**
