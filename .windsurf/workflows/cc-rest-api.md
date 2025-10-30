---
description: rest-api layer controller, mapper(dto, error), properties 보일러 템플릿 를 CC에 준수하여 만든다
---

# REST API Adapter Layer 코드 생성 Workflow

## Overview
REST API Adapter Layer의 Controller, DTO, Mapper, Error Mapper, Properties를 일관된 컨벤션으로 생성하는 Cascade Workflow입니다.
## Prerequisites
- ✅ Application Layer UseCase/Facade 구현 완료
- ✅ Application Layer Command/Response DTO 구현 완료
- ✅ Domain Layer 구현 완료
- ✅ `application.yml`에 API 엔드포인트 경로 설정

## Inputs
- **domain**: 도메인 이름 (예: `Tenant`, `Organization`, `Permission`)
- **feature**: 기능 이름 (예: `create`, `update`, `delete`, `search`)
- **useCaseClass**: 사용할 UseCase 클래스명 (예: `CreateTenantUseCase`)
- **commandClass**: Application Layer Command 클래스명 (예: `CreateTenantCommand`)
- **responseClass**: Application Layer Response 클래스명 (예: `TenantResponse`)

## Workflow Steps

### Step 1: API 엔드포인트 설정 (application.yml)
```yaml
# adapter-in/rest-api/src/main/resources/application.yml

api:
  endpoints:
    base-v1: /api/v1
    iam:
      {domain-lower}:
        base: /{domain-plurals}
        by-id: /{domainId}
        # 추가 엔드포인트...
```

**예시 (Tenant)**:
```yaml
api:
  endpoints:
    base-v1: /api/v1
    iam:
      tenant:
        base: /tenants
        by-id: /{tenantId}
        status: /{tenantId}/status
```

---

### Step 2: Properties 클래스 생성
**위치**: `adapter-in/rest-api/src/main/java/com/ryuqq/fileflow/adapter/rest/config/properties/`

**파일명**: `{Feature}EndpointProperties.java`

**컨벤션 체크리스트**:
- ✅ `@Component` + `@ConfigurationProperties(prefix = "api.endpoints.{feature}")` 어노테이션
- ✅ 중첩 static 클래스로 도메인별 엔드포인트 그룹화
- ✅ 필드 기본값 설정
- ✅ ❌ Lombok 사용 금지 - Pure Java getter/setter
- ✅ Javadoc 작성 (`@author`, `@since` 필수)

**템플릿**:
```java
package com.ryuqq.fileflow.adapter.rest.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * {Feature} API 엔드포인트 설정
 *
 * <p>application.yml의 {@code api.endpoints.{feature}} 설정을 매핑합니다.</p>
 *
 * @author {author}
 * @since {date}
 */
@Component
@ConfigurationProperties(prefix = "api.endpoints.{feature}")
public class {Feature}EndpointProperties {

    private {Domain}Endpoints {domain} = new {Domain}Endpoints();

    public {Domain}Endpoints get{Domain}() {
        return {domain};
    }

    public void set{Domain}({Domain}Endpoints {domain}) {
        this.{domain} = {domain};
    }

    /**
     * {Domain} API 엔드포인트
     */
    public static class {Domain}Endpoints {
        private String base = "/{domain-plurals}";
        private String byId = "/{domainId}";

        // getters and setters
        public String getBase() {
            return base;
        }

        public void setBase(String base) {
            this.base = base;
        }

        public String getById() {
            return byId;
        }

        public void setById(String byId) {
            this.byId = byId;
        }
    }
}
```

---

### Step 3: Request DTO 생성
**위치**: `adapter-in/rest-api/src/main/java/com/ryuqq/fileflow/adapter/rest/{feature}/{domain}/dto/request/`

**파일명**: `{Feature}{Domain}ApiRequest.java` 또는 `{Domain}SearchApiRequest.java` (Query Parameter)

**컨벤션 체크리스트**:
- ✅ **Java 21 Record** 사용
- ✅ **Bean Validation** 어노테이션 (@NotBlank, @NotNull, @Valid, @Min, @Max 등)
- ✅ **Query Parameter용**: `isOffsetBased()` + `toQuery()` 메서드 포함
- ✅ **Compact Constructor**: 기본값 설정 (Query Parameter)
- ✅ ❌ Lombok 사용 금지
- ✅ Javadoc + JSON 예시

**템플릿 (Request Body)**:
```java
package com.ryuqq.fileflow.adapter.rest.{feature}.{domain}.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * {Domain} {Feature} API 요청
 *
 * <p><strong>Request Body 예시</strong>:</p>
 * <pre>
 * {
 *   "field1": "value1",
 *   "field2": "value2"
 * }
 * </pre>
 *
 * @param field1 필드1 설명
 * @param field2 필드2 설명
 * @author {author}
 * @since {date}
 */
public record {Feature}{Domain}ApiRequest(
    @NotBlank(message = "필드1은 필수입니다")
    String field1,

    @NotBlank(message = "필드2는 필수입니다")
    String field2
) {
}
```

**템플릿 (Query Parameter)**:
```java
package com.ryuqq.fileflow.adapter.rest.{feature}.{domain}.dto.request;

import com.ryuqq.fileflow.application.{feature}.{domain}.dto.query.Get{Domain}sQuery;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

/**
 * {Domain} 검색 Query Parameter
 *
 * <p><strong>Pagination 전략</strong>:</p>
 * <ul>
 *   <li>Offset-based: page, size 파라미터 사용 ({@link #isOffsetBased()})</li>
 *   <li>Cursor-based: cursor 파라미터 사용</li>
 * </ul>
 *
 * <p><strong>Query Parameter 예시</strong>:</p>
 * <pre>
 * GET /api/v1/{domain-plurals}?page=0&size=20&field1=value1
 * GET /api/v1/{domain-plurals}?cursor=abc123&size=20
 * </pre>
 *
 * @param page 페이지 번호 (0-based, Offset Pagination)
 * @param size 페이지 크기 (기본값: 20, 최대: 100)
 * @param cursor 커서 (Cursor Pagination)
 * @param field1 검색 필드1
 * @author {author}
 * @since {date}
 */
public record {Domain}SearchApiRequest(
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
    Integer page,

    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
    Integer size,

    String cursor,

    @Positive(message = "Tenant ID는 양수여야 합니다")
    Long tenantId,

    String field1
) {
    /**
     * Compact Constructor - 기본값 설정
     */
    public {Domain}SearchApiRequest {
        size = (size == null) ? 20 : size;
    }

    /**
     * Offset-based Pagination 여부 확인
     *
     * @return page 파라미터가 있으면 true
     */
    public boolean isOffsetBased() {
        return page != null;
    }

    /**
     * Application Layer Query로 변환
     *
     * @return Get{Domain}sQuery
     */
    public Get{Domain}sQuery toQuery() {
        return new Get{Domain}sQuery(
            page, size, cursor, tenantId, field1
        );
    }
}
```

---

### Step 4: Response DTO 생성
**위치**: `adapter-in/rest-api/src/main/java/com/ryuqq/fileflow/adapter/rest/{feature}/{domain}/dto/response/`

**파일명**: `{Domain}ApiResponse.java`

**컨벤션 체크리스트**:
- ✅ **Java 21 Record** 사용
- ✅ ❌ Lombok 사용 금지
- ✅ Javadoc + JSON 예시

**템플릿**:
```java
package com.ryuqq.fileflow.adapter.rest.{feature}.{domain}.dto.response;

import java.time.LocalDateTime;

/**
 * {Domain} API 응답
 *
 * <p><strong>Response Body 예시</strong>:</p>
 * <pre>
 * {
 *   "{domainId}": 1,
 *   "field1": "value1",
 *   "field2": "value2",
 *   "createdAt": "2025-10-27T10:00:00",
 *   "updatedAt": "2025-10-27T15:30:00"
 * }
 * </pre>
 *
 * @param {domainId} {Domain} ID
 * @param field1 필드1
 * @param field2 필드2
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 * @author {author}
 * @since {date}
 */
public record {Domain}ApiResponse(
    Long {domainId},
    String field1,
    String field2,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
```

---

### Step 5: Mapper 클래스 생성
**위치**: `adapter-in/rest-api/src/main/java/com/ryuqq/fileflow/adapter/rest/{feature}/{domain}/mapper/`

**파일명**: `{Domain}ApiMapper.java`

**컨벤션 체크리스트**:
- ✅ **final 클래스**
- ✅ **private 생성자** (`throw new UnsupportedOperationException("Utility 클래스는 인스턴스화할 수 없습니다")`)
- ✅ **모든 public 메서드는 static**
- ✅ **모든 private 헬퍼 메서드도 static**
- ✅ ❌ Lombok 사용 금지
- ✅ ❌ @Component 어노테이션 사용 금지
- ✅ Null-safe 검증 (모든 입력 파라미터)
- ✅ 메서드명: `toCommand()`, `toApiResponse()`
- ✅ Javadoc

**템플릿**:
```java
package com.ryuqq.fileflow.adapter.rest.{feature}.{domain}.mapper;

import com.ryuqq.fileflow.adapter.rest.{feature}.{domain}.dto.request.{Feature}{Domain}ApiRequest;
import com.ryuqq.fileflow.adapter.rest.{feature}.{domain}.dto.response.{Domain}ApiResponse;
import com.ryuqq.fileflow.application.{feature}.{domain}.dto.command.{Feature}{Domain}Command;
import com.ryuqq.fileflow.application.{feature}.{domain}.dto.response.{Domain}Response;

/**
 * {Domain} DTO Mapper
 *
 * <p>{Domain} REST API DTO ↔ Application DTO 변환을 담당합니다.</p>
 *
 * <p><strong>매핑 패턴</strong>:</p>
 * <ul>
 *   <li>Request → Command (Inbound Adapter → Application)</li>
 *   <li>Response → ApiResponse (Application → Outbound Adapter)</li>
 * </ul>
 *
 * <p><strong>규칙 준수</strong>:</p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Static Utility Class (Stateless, 인스턴스 생성 금지)</li>
 *   <li>✅ Null-safe 변환</li>
 * </ul>
 *
 * @author {author}
 * @since {date}
 */
public final class {Domain}ApiMapper {

    /**
     * Private Constructor - 인스턴스 생성 방지
     *
     * @throws UnsupportedOperationException 항상 발생
     * @author {author}
     * @since {date}
     */
    private {Domain}ApiMapper() {
        throw new UnsupportedOperationException("Utility 클래스는 인스턴스화할 수 없습니다");
    }

    /**
     * {Feature}{Domain}ApiRequest → {Feature}{Domain}Command 변환
     *
     * <p>REST API Request DTO를 Application Layer Command로 변환합니다.</p>
     *
     * @param request {Domain} {Feature} 요청 DTO
     * @return {Feature}{Domain}Command
     * @throws IllegalArgumentException request가 null인 경우
     * @author {author}
     * @since {date}
     */
    public static {Feature}{Domain}Command toCommand({Feature}{Domain}ApiRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("{Feature}{Domain}Request는 null일 수 없습니다");
        }

        return new {Feature}{Domain}Command(
            request.field1(),
            request.field2()
        );
    }

    /**
     * {Domain}Response → {Domain}ApiResponse 변환
     *
     * <p>Application Layer Response를 REST API Response DTO로 변환합니다.</p>
     *
     * @param response {Domain} Response
     * @return {Domain}ApiResponse
     * @throws IllegalArgumentException response가 null인 경우
     * @author {author}
     * @since {date}
     */
    public static {Domain}ApiResponse toApiResponse({Domain}Response response) {
        if (response == null) {
            throw new IllegalArgumentException("{Domain}Response는 null일 수 없습니다");
        }

        return new {Domain}ApiResponse(
            response.{domainId}(),
            response.field1(),
            response.field2(),
            response.createdAt(),
            response.updatedAt()
        );
    }
}
```

---

### Step 6: Controller 생성
**위치**: `adapter-in/rest-api/src/main/java/com/ryuqq/fileflow/adapter/rest/{feature}/{domain}/controller/`

**파일명**: `{Domain}Controller.java`

**컨벤션 체크리스트**:
- ✅ `@RestController` + `@RequestMapping("${api.endpoints.base-v1}${api.endpoints.{feature}.{domain}.base}")`
- ✅ **Constructor Injection** (Field Injection 금지)
- ✅ **Facade/UseCase 의존성** (상황에 따라 선택, [Decision Tree](../docs/coding_convention/03-application-layer/facade/01_facade-usage-guide.md) 참고)
- ✅ **Thin Controller** (비즈니스 로직 금지, Facade/UseCase 호출만)
- ✅ **@Valid** 어노테이션 (@RequestBody, @ModelAttribute 모두)
- ✅ **ResponseEntity + HTTP Status** 명시
- ✅ ❌ Lombok 사용 금지
- ✅ ❌ Mapper DI 금지 (Static 메서드 직접 호출)
- ✅ 포괄적인 Javadoc (API 문서, HTTP Method, Path, Status Codes, 예시)

**Facade vs UseCase 직접 호출 선택 기준**:
```
UseCase가 2개 이상인가?
├─ Yes → ✅ Facade 사용 (Controller 의존성 감소)
└─ No → UseCase 1개만 있음
         ↓
    추가 로직 필요? (트랜잭션 조율, 데이터 변환)
    ├─ Yes → ✅ Facade 권장
    └─ No → ❌ UseCase 직접 호출 (Facade 불필요, YAGNI 원칙)
```

**자세한 가이드**: [Facade 사용 가이드](../docs/coding_convention/03-application-layer/facade/01_facade-usage-guide.md)

**템플릿 1: Facade 사용 (UseCase 2개 이상)**:
```java
package com.ryuqq.fileflow.adapter.rest.{feature}.{domain}.controller;

import com.ryuqq.fileflow.adapter.rest.common.dto.ApiResponse;
import com.ryuqq.fileflow.adapter.rest.{feature}.{domain}.dto.request.{Feature}{Domain}ApiRequest;
import com.ryuqq.fileflow.adapter.rest.{feature}.{domain}.dto.response.{Domain}ApiResponse;
import com.ryuqq.fileflow.adapter.rest.{feature}.{domain}.mapper.{Domain}ApiMapper;
import com.ryuqq.fileflow.application.{feature}.{domain}.dto.command.{Feature}{Domain}Command;
import com.ryuqq.fileflow.application.{feature}.{domain}.dto.response.{Domain}Response;
import com.ryuqq.fileflow.application.{feature}.{domain}.facade.{Domain}CommandFacade;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * {Domain} REST API Contro