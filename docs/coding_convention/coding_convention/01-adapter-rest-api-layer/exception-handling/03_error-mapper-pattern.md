# ErrorMapper 패턴 가이드

## 목차
1. [개요](#1-개요)
2. [아키텍처](#2-아키텍처)
3. [ErrorMapper 인터페이스](#3-errormapper-인터페이스)
4. [ErrorMapperRegistry](#4-errormapperregistry)
5. [ErrorMapper 구현](#5-errormapper-구현)
6. [GlobalExceptionHandler 통합](#6-globalexceptionhandler-통합)
7. [RFC 7807 Problem Details](#7-rfc-7807-problem-details)
8. [로깅 전략](#8-로깅-전략)
9. [MessageSource 국제화](#9-messagesource-국제화)
10. [베스트 프랙티스](#10-베스트-프랙티스)

---

## 1. 개요

**ErrorMapper 패턴**은 Domain Layer의 예외를 HTTP 응답으로 변환하는 추상화 계층입니다.

### 1.1 문제점

**Without ErrorMapper (Anti-Pattern)**:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException ex) {
        // ❌ Domain 예외 코드를 REST Layer에서 하드코딩
        return switch (ex.code()) {
            case "EXAMPLE_NOT_FOUND" ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.ofError("Example을 찾을 수 없습니다"));
            case "ORDER_NOT_FOUND" ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.ofError("Order를 찾을 수 없습니다"));
            case "PRODUCT_NOT_FOUND" ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.ofError("Product를 찾을 수 없습니다"));
            // ❌ 도메인이 추가될 때마다 GlobalExceptionHandler 수정 필요
            default ->
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.ofError(ex.getMessage()));
        };
    }
}
```

**문제점**:
- ❌ **OCP 위반**: 도메인이 추가될 때마다 GlobalExceptionHandler 수정 필요
- ❌ **단일 책임 위반**: GlobalExceptionHandler가 모든 도메인의 에러 매핑 책임
- ❌ **테스트 불가능**: 도메인별 매핑 로직을 독립적으로 테스트할 수 없음
- ❌ **확장 불가능**: 복잡한 에러 매핑 로직(MessageSource, 국제화 등) 추가 어려움

### 1.2 해결책: ErrorMapper 패턴

**With ErrorMapper (Best Practice)**:

```java
// 1. ErrorMapper 인터페이스 (추상화)
public interface ErrorMapper {
    boolean supports(String code);
    MappedError map(DomainException ex, Locale locale);
}

// 2. 도메인별 ErrorMapper 구현
@Component
public class ExampleErrorMapper implements ErrorMapper {
    @Override
    public boolean supports(String code) {
        return code.startsWith("EXAMPLE_");
    }

    @Override
    public MappedError map(DomainException ex, Locale locale) {
        // Example 도메인 전용 매핑 로직
    }
}

// 3. GlobalExceptionHandler는 ErrorMapperRegistry만 사용
@RestControllerAdvice
public class GlobalExceptionHandler {
    private final ErrorMapperRegistry errorMapperRegistry;

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> handleDomainException(
            DomainException ex, Locale locale) {
        // ✅ ErrorMapperRegistry가 적절한 Mapper 선택
        var mapped = errorMapperRegistry.map(ex, locale)
            .orElseGet(() -> errorMapperRegistry.defaultMapping(ex));
        // ...
    }
}
```

**장점**:
- ✅ **OCP 준수**: 도메인 추가 시 ErrorMapper 구현체만 추가하면 됨
- ✅ **단일 책임**: 각 ErrorMapper는 자신의 도메인 에러 매핑만 책임
- ✅ **테스트 가능**: 도메인별 ErrorMapper를 독립적으로 단위 테스트 가능
- ✅ **확장 가능**: MessageSource, 국제화, 복잡한 매핑 로직 쉽게 추가

---

## 2. 아키텍처

### 2.1 전체 흐름

```
┌─────────────────────────────────────────────────────────────────┐
│ Client Request                                                   │
└────────────────────────────────┬────────────────────────────────┘
                                 ↓
┌─────────────────────────────────────────────────────────────────┐
│ REST Layer (Adapter-In)                                          │
│                                                                   │
│  @RestController                                                 │
│  ├─ ExampleController.createExample()                            │
│  │   └─ createExampleUseCase.execute()  ───────────┐            │
│  │                                                   │            │
└──┼───────────────────────────────────────────────────┼───────────┘
   │ ⚡ DomainException 발생                           │
   ↓                                                   ↓
┌─────────────────────────────────────────────────────────────────┐
│ Application Layer                                                │
│                                                                   │
│  @UseCase                                                        │
│  └─ CreateExampleUseCase.execute()                              │
│      └─ exampleRepository.save()  ─────────────┐                │
│                                                 │                │
└─────────────────────────────────────────────────┼────────────────┘
                                                  │ ⚡ DomainException
                                                  ↓
┌─────────────────────────────────────────────────────────────────┐
│ Domain Layer                                                     │
│                                                                   │
│  Example (Aggregate Root)                                        │
│  └─ validateMessage()                                            │
│      └─ throw ExampleNotFoundException("EXAMPLE_NOT_FOUND")     │
│                                                                   │
└──────────────────────────────────┬──────────────────────────────┘
                                   ↓ ⚡ DomainException
┌─────────────────────────────────────────────────────────────────┐
│ REST Layer (GlobalExceptionHandler)                              │
│                                                                   │
│  @ExceptionHandler(DomainException.class)                        │
│  └─ errorMapperRegistry.map(ex, locale) ─────┐                  │
│                                               │                  │
│      ErrorMapperRegistry                     │                  │
│      ├─ ExampleErrorMapper   ←──────────────┘                  │
│      ├─ OrderErrorMapper                                        │
│      ├─ ProductErrorMapper                                      │
│      └─ ... (도메인별 Mapper)                                    │
│                                                                   │
│  ErrorMapper.map(ex, locale)                                     │
│  ├─ 1. HttpStatus 매핑 (404, 409, 400 등)                       │
│  ├─ 2. MessageSource 국제화 (title, detail)                     │
│  └─ 3. RFC 7807 Problem Details 생성                            │
│                                                                   │
│  ResponseEntity<ProblemDetail>                                   │
│  └─ RFC 7807 표준 JSON 응답                                      │
│                                                                   │
└────────────────────────────────┬────────────────────────────────┘
                                 ↓
┌─────────────────────────────────────────────────────────────────┐
│ Client Response (RFC 7807 Problem Details)                       │
│                                                                   │
│  HTTP/1.1 404 Not Found                                          │
│  Content-Type: application/problem+json                          │
│                                                                   │
│  {                                                                │
│    "type": "https://api.example.com/problems/example-not-found", │
│    "title": "리소스를 찾을 수 없습니다",                         │
│    "status": 404,                                                 │
│    "detail": "요청하신 예시 리소스를 찾을 수 없습니다. (id=123)",│
│    "instance": "/api/v1/examples/123",                           │
│    "timestamp": "2025-10-28T10:30:00Z",                          │
│    "code": "EXAMPLE_NOT_FOUND",                                  │
│    "traceId": "550e8400-e29b-41d4-a716-446655440000"             │
│  }                                                                │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 컴포넌트 책임

| 컴포넌트 | 책임 | 위치 |
|---------|------|------|
| **ErrorMapper** | 인터페이스 정의 (추상화) | `common/mapper/` |
| **ErrorMapperRegistry** | ErrorMapper 관리 및 선택 | `common/error/` |
| **ExampleErrorMapper** | Example 도메인 에러 매핑 | `example/error/` |
| **GlobalExceptionHandler** | 예외 처리 및 RFC 7807 응답 생성 | `common/controller/` |
| **ErrorHandlingConfig** | ErrorMapperRegistry Bean 등록 | `config/` |

---

## 3. ErrorMapper 인터페이스

### 3.1 인터페이스 설계

```java
package com.ryuqq.adapter.in.rest.common.mapper;

import com.ryuqq.domain.common.DomainException;

import java.net.URI;
import java.util.Locale;

import org.springframework.http.HttpStatus;

/**
 * DomainException을 HTTP 응답으로 변환하는 Mapper 인터페이스
 *
 * <p>도메인별로 ErrorMapper를 구현하여 DomainException을 HTTP 응답으로 매핑합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>특정 도메인 예외 코드 지원 여부 확인 (supports)</li>
 *   <li>DomainException을 HTTP 응답(MappedError)으로 변환 (map)</li>
 *   <li>국제화(i18n) 지원 (Locale 기반)</li>
 *   <li>RFC 7807 Problem Details 준수</li>
 * </ul>
 *
 * <p><strong>구현 예시:</strong></p>
 * <pre>{@code
 * @Component
 * public class ExampleErrorMapper implements ErrorMapper {
 *     @Override
 *     public boolean supports(String code) {
 *         return code.startsWith("EXAMPLE_");
 *     }
 *
 *     @Override
 *     public MappedError map(DomainException ex, Locale locale) {
 *         HttpStatus status = switch (ex.code()) {
 *             case "EXAMPLE_NOT_FOUND" -> HttpStatus.NOT_FOUND;
 *             case "EXAMPLE_DUPLICATE_KEY" -> HttpStatus.CONFLICT;
 *             default -> HttpStatus.BAD_REQUEST;
 *         };
 *
 *         String title = messageSource.getMessage("problem.title." + ex.code().toLowerCase(), null, locale);
 *         String detail = messageSource.getMessage("problem.detail." + ex.code().toLowerCase(), ex.args(), locale);
 *         URI type = URI.create("https://api.example.com/problems/" + ex.code().toLowerCase().replace('_', '-'));
 *
 *         return new MappedError(status, title, detail, type);
 *     }
 * }
 * }</pre>
 *
 * @author windsurf
 * @since 1.0.0
 */
public interface ErrorMapper {

    /**
     * 특정 도메인 예외 코드 지원 여부 확인
     *
     * <p>ErrorMapperRegistry가 적절한 Mapper를 선택할 때 사용합니다.</p>
     *
     * <p><strong>구현 예시:</strong></p>
     * <pre>{@code
     * @Override
     * public boolean supports(String code) {
     *     return code != null && code.startsWith("EXAMPLE_");
     * }
     * }</pre>
     *
     * @param code DomainException의 에러 코드 (예: "EXAMPLE_NOT_FOUND")
     * @return 지원 여부 (true: 지원함, false: 지원 안 함)
     */
    boolean supports(String code);

    /**
     * DomainException을 HTTP 응답용으로 매핑
     *
     * <p>도메인 예외를 RFC 7807 Problem Details 형식으로 변환합니다.</p>
     *
     * <p><strong>매핑 책임:</strong></p>
     * <ul>
     *   <li>HttpStatus 결정 (404, 409, 400 등)</li>
     *   <li>title: 에러 요약 (국제화 지원)</li>
     *   <li>detail: 에러 상세 설명 (국제화 지원, 파라미터 치환)</li>
     *   <li>type: 에러 문서 URI (RFC 7807 표준)</li>
     * </ul>
     *
     * @param ex DomainException
     * @param locale 클라이언트 로케일 (국제화 지원)
     * @return 매핑 결과 (HttpStatus, title, detail, type)
     */
    MappedError map(DomainException ex, Locale locale);

    /**
     * 매핑 결과 DTO (RFC 7807 Problem Details 일부)
     *
     * <p>GlobalExceptionHandler가 이 정보를 사용하여 ProblemDetail을 생성합니다.</p>
     *
     * @param status HTTP 상태 코드 (404, 409, 400 등)
     * @param title 에러 요약 (국제화된 메시지)
     * @param detail 에러 상세 설명 (국제화된 메시지 + 파라미터 치환)
     * @param type 에러 문서 URI (RFC 7807 표준, 예: https://api.example.com/problems/example-not-found)
     */
    record MappedError(HttpStatus status, String title, String detail, URI type) {}

}
```

### 3.2 주요 메서드

#### supports(String code)

**목적**: ErrorMapperRegistry가 적절한 Mapper를 선택하기 위한 메서드

**구현 패턴**:

```java
// 1. Prefix 기반 (권장)
@Override
public boolean supports(String code) {
    return code != null && code.startsWith("EXAMPLE_");
}

// 2. Exact Match (여러 코드)
@Override
public boolean supports(String code) {
    return Set.of("EXAMPLE_NOT_FOUND", "EXAMPLE_DUPLICATE_KEY", "EXAMPLE_INVALID_STATE")
        .contains(code);
}

// 3. Regex 기반 (복잡한 패턴)
@Override
public boolean supports(String code) {
    return code != null && code.matches("^EXAMPLE_(NOT_FOUND|DUPLICATE|INVALID).*");
}
```

#### map(DomainException ex, Locale locale)

**목적**: DomainException을 HTTP 응답으로 변환

**구현 책임**:
1. **HttpStatus 매핑**: 예외 코드에 따라 적절한 HTTP 상태 코드 선택
2. **title 생성**: 에러 요약 메시지 (국제화)
3. **detail 생성**: 에러 상세 설명 (국제화 + 파라미터 치환)
4. **type URI 생성**: RFC 7807 표준 에러 문서 URI

---

## 4. ErrorMapperRegistry

### 4.1 Registry 구현

```java
package com.ryuqq.adapter.in.rest.common.error;

import com.ryuqq.adapter.in.rest.common.mapper.ErrorMapper;
import com.ryuqq.domain.common.DomainException;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.http.HttpStatus;

/**
 * ErrorMapper들을 관리하고 적절한 Mapper를 선택하는 Registry
 *
 * <p>Spring에 등록된 모든 ErrorMapper 구현체를 관리하고,
 * DomainException의 code에 따라 적절한 Mapper를 선택합니다.</p>
 *
 * <p><strong>작동 방식:</strong></p>
 * <ul>
 *   <li>ErrorHandlingConfig에서 모든 ErrorMapper 빈을 주입받아 관리</li>
 *   <li>DomainException 발생 시 code로 적절한 Mapper 선택 (supports 메서드)</li>
 *   <li>매칭되는 Mapper가 없으면 defaultMapping 반환</li>
 * </ul>
 *
 * <p><strong>확장성:</strong></p>
 * <ul>
 *   <li>새로운 도메인 추가 시 ErrorMapper 구현체만 추가하면 자동으로 등록됨</li>
 *   <li>GlobalExceptionHandler는 변경 불필요 (OCP 준수)</li>
 * </ul>
 *
 * @author windsurf
 * @since 1.0.0
 */
public class ErrorMapperRegistry {
    private final List<ErrorMapper> mappers;

    /**
     * ErrorMapperRegistry 생성자
     *
     * @param mappers Spring에 등록된 모든 ErrorMapper 빈들
     */
    public ErrorMapperRegistry(List<ErrorMapper> mappers) {
        this.mappers = mappers;
    }

    /**
     * DomainException을 HTTP 응답으로 매핑
     *
     * <p>등록된 ErrorMapper 중에서 supports(code)가 true인 첫 번째 Mapper를 사용합니다.</p>
     *
     * <p><strong>선택 로직:</strong></p>
     * <pre>{@code
     * 1. mappers.stream()
     * 2. filter(m -> m.supports(ex.code()))  // 지원하는 Mapper 필터링
     * 3. findFirst()  // 첫 번째 Mapper 선택
     * 4. map(m -> m.map(ex, locale))  // 매핑 수행
     * }</pre>
     *
     * @param ex DomainException
     * @param locale 클라이언트 로케일
     * @return 매핑 결과 (Optional)
     */
    public Optional<ErrorMapper.MappedError> map(DomainException ex, Locale locale) {
        return mappers.stream()
            .filter(m -> m.supports(ex.code()))
            .findFirst()
            .map(m -> m.map(ex, locale));
    }

    /**
     * 기본 매핑 (Fallback)
     *
     * <p>매칭되는 ErrorMapper가 없을 때 사용하는 기본 매핑입니다.</p>
     *
     * <p><strong>기본 동작:</strong></p>
     * <ul>
     *   <li>HttpStatus: 400 BAD_REQUEST</li>
     *   <li>title: "Bad Request"</li>
     *   <li>detail: ex.getMessage() 또는 "Invalid request"</li>
     *   <li>type: about:blank</li>
     * </ul>
     *
     * @param ex DomainException
     * @return 기본 매핑 결과
     */
    public ErrorMapper.MappedError defaultMapping(DomainException ex) {
        return new ErrorMapper.MappedError(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            ex.getMessage() != null ? ex.getMessage() : "Invalid request",
            URI.create("about:blank")
        );
    }

}
```

### 4.2 Spring Bean 등록

```java
package com.ryuqq.adapter.in.rest.config;

import java.util.List;

import com.ryuqq.adapter.in.rest.common.error.ErrorMapperRegistry;
import com.ryuqq.adapter.in.rest.common.mapper.ErrorMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 에러 핸들링 설정
 *
 * <p>도메인 예외를 HTTP 응답으로 변환하는 ErrorMapper들을 관리합니다.</p>
 *
 * <p><strong>자동 등록:</strong></p>
 * <ul>
 *   <li>Spring에 등록된 모든 ErrorMapper 빈들을 자동으로 수집</li>
 *   <li>ErrorMapperRegistry에 등록하여 GlobalExceptionHandler에서 사용</li>
 *   <li>도메인별로 ErrorMapper를 추가하면 자동으로 감지되어 등록됨</li>
 * </ul>
 *
 * @author windsurf
 * @since 1.0.0
 */
@Configuration
public class ErrorHandlingConfig {

    /**
     * ErrorMapperRegistry를 Spring Bean으로 등록
     *
     * <p>Spring이 모든 ErrorMapper 구현체를 찾아서 List로 주입합니다.</p>
     *
     * @param mappers Spring에 등록된 모든 ErrorMapper 빈들
     * @return ErrorMapperRegistry 빈
     */
    @Bean
    public ErrorMapperRegistry errorMapperRegistry(List<ErrorMapper> mappers) {
        return new ErrorMapperRegistry(mappers);
    }
}
```

---

## 5. ErrorMapper 구현

### 5.1 Example 도메인 ErrorMapper

```java
package com.ryuqq.adapter.in.rest.example.error;

import com.ryuqq.adapter.in.rest.common.mapper.ErrorMapper;
import com.ryuqq.domain.common.DomainException;

import java.net.URI;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Example 도메인 ErrorMapper
 *
 * <p>Example 도메인에서 발생한 DomainException을 HTTP 응답으로 변환합니다.</p>
 *
 * <p><strong>지원하는 예외 코드:</strong></p>
 * <ul>
 *   <li>EXAMPLE_NOT_FOUND → 404 Not Found</li>
 *   <li>EXAMPLE_DUPLICATE_KEY → 409 Conflict</li>
 *   <li>EXAMPLE_INVALID_STATE → 409 Conflict</li>
 *   <li>기타 EXAMPLE_* → 400 Bad Request</li>
 * </ul>
 *
 * @author windsurf
 * @since 1.0.0
 */
@Component
public class ExampleErrorMapping implements ErrorMapper {

    private static final String PREFIX = "EXAMPLE_";
    private static final String TYPE_BASE = "https://api.example.com/problems/"; // 사내 문서 URL 권장

    private final MessageSource messageSource;

    public ExampleErrorMapping(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public boolean supports(String code) {
        return code != null && code.startsWith(PREFIX);
    }

    @Override
    public MappedError map(DomainException ex, Locale locale) {
        String code = ex.code();

        // 1. 상태코드 매핑 (컨텍스트별 정책)
        HttpStatus status = switch (code) {
            case "EXAMPLE_NOT_FOUND"     -> HttpStatus.NOT_FOUND;
            case "EXAMPLE_DUPLICATE_KEY" -> HttpStatus.CONFLICT;
            case "EXAMPLE_INVALID_STATE" -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };

        // 2. 사용자 친화 타이틀/디테일 (i18n → 없으면 도메인 메시지)
        String titleKey  = "problem.title."  + code.toLowerCase(); // 예: problem.title.example_not_found
        String detailKey = "problem.detail." + code.toLowerCase(); // 예: problem.detail.example_not_found

        Object[] args = ex.args().toArray(); // DomainException의 args를 배열로 변환
        String defaultTitle = status.getReasonPhrase();

        String title  = messageSource.getMessage(titleKey,  args, defaultTitle, locale);
        String detail = messageSource.getMessage(detailKey, args,
            ex.getMessage() != null ? ex.getMessage() : defaultTitle, locale);

        // 3. type URI (문서화 가능)
        URI type = URI.create(TYPE_BASE + code.toLowerCase().replace('_', '-'));
        // 예: https://api.example.com/problems/example-not-found

        return new MappedError(status, title, detail, type);
    }
}
```

### 5.2 messages.properties 설정

**messages_ko.properties**:

```properties
# Example 도메인 에러 메시지 (한국어)

# EXAMPLE_NOT_FOUND
problem.title.example_not_found=리소스를 찾을 수 없습니다
problem.detail.example_not_found=요청하신 예시 리소스를 찾을 수 없습니다. (id={0})

# EXAMPLE_DUPLICATE_KEY
problem.title.example_duplicate_key=중복된 데이터
problem.detail.example_duplicate_key=이미 존재하는 값입니다. (key={0})

# EXAMPLE_INVALID_STATE
problem.title.example_invalid_state=요청을 처리할 수 없는 상태
problem.detail.example_invalid_state=현재 상태에서는 수행할 수 없습니다. (state={0})
```

**messages_en.properties**:

```properties
# Example Domain Error Messages (English)

# EXAMPLE_NOT_FOUND
problem.title.example_not_found=Resource Not Found
problem.detail.example_not_found=The requested example resource could not be found. (id={0})

# EXAMPLE_DUPLICATE_KEY
problem.title.example_duplicate_key=Duplicate Data
problem.detail.example_duplicate_key=A resource with this key already exists. (key={0})

# EXAMPLE_INVALID_STATE
problem.title.example_invalid_state=Invalid State
problem.detail.example_invalid_state=This operation cannot be performed in the current state. (state={0})
```

### 5.3 다른 도메인 ErrorMapper 예시

**OrderErrorMapper**:

```java
package com.ryuqq.adapter.in.rest.order.error;

import com.ryuqq.adapter.in.rest.common.mapper.ErrorMapper;
import com.ryuqq.domain.common.DomainException;

import java.net.URI;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Order 도메인 ErrorMapper
 *
 * <p><strong>지원하는 예외 코드:</strong></p>
 * <ul>
 *   <li>ORDER_NOT_FOUND → 404 Not Found</li>
 *   <li>ORDER_ALREADY_CANCELLED → 409 Conflict</li>
 *   <li>ORDER_INVALID_STATUS_TRANSITION → 409 Conflict</li>
 *   <li>ORDER_INSUFFICIENT_STOCK → 409 Conflict</li>
 * </ul>
 *
 * @author windsurf
 * @since 1.0.0
 */
@Component
public class OrderErrorMapping implements ErrorMapper {

    private static final String PREFIX = "ORDER_";
    private static final String TYPE_BASE = "https://api.example.com/problems/";

    private final MessageSource messageSource;

    public OrderErrorMapping(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public boolean supports(String code) {
        return code != null && code.startsWith(PREFIX);
    }

    @Override
    public MappedError map(DomainException ex, Locale locale) {
        String code = ex.code();

        HttpStatus status = switch (code) {
            case "ORDER_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "ORDER_ALREADY_CANCELLED",
                 "ORDER_INVALID_STATUS_TRANSITION",
                 "ORDER_INSUFFICIENT_STOCK" -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };

        String titleKey  = "problem.title."  + code.toLowerCase();
        String detailKey = "problem.detail." + code.toLowerCase();

        Object[] args = ex.args().toArray();
        String defaultTitle = status.getReasonPhrase();

        String title  = messageSource.getMessage(titleKey,  args, defaultTitle, locale);
        String detail = messageSource.getMessage(detailKey, args,
            ex.getMessage() != null ? ex.getMessage() : defaultTitle, locale);

        URI type = URI.create(TYPE_BASE + code.toLowerCase().replace('_', '-'));

        return new MappedError(status, title, detail, type);
    }
}
```

---

## 6. GlobalExceptionHandler 통합

### 6.1 DomainException 처리

```java
package com.ryuqq.adapter.in.rest.common.controller;

import java.net.URI;
import java.time.Instant;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;

import com.ryuqq.adapter.in.rest.common.error.ErrorMapperRegistry;
import com.ryuqq.domain.common.DomainException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리
 *
 * <p>REST API에서 발생하는 모든 예외를 처리하고 RFC 7807 Problem Details 형식으로 응답합니다.</p>
 *
 * @author windsurf
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ErrorMapperRegistry errorMapperRegistry;

    public GlobalExceptionHandler(ErrorMapperRegistry errorMapperRegistry) {
        this.errorMapperRegistry = errorMapperRegistry;
    }

    /**
     * 도메인 예외 처리
     *
     * <p>Domain Layer에서 발생한 예외를 HTTP 응답으로 변환합니다.</p>
     * <p>ErrorMapperRegistry를 통해 도메인별 커스텀 매핑을 적용합니다.</p>
     *
     * <p><strong>로깅 레벨 전략:</strong></p>
     * <ul>
     *   <li>5xx 에러 → ERROR 레벨 (서버 문제, 즉시 대응 필요, 스택트레이스 포함)</li>
     *   <li>404 에러 → DEBUG 레벨 (정상적인 흐름, 로그 노이즈 방지)</li>
     *   <li>4xx 에러 → WARN 레벨 (클라이언트 오류, 모니터링 필요)</li>
     * </ul>
     *
     * @param ex 도메인 예외
     * @param req HTTP 요청
     * @param locale 로케일 (국제화 지원)
     * @return RFC 7807 Problem Details 응답
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> handleDomain(DomainException ex,
                                                      HttpServletRequest req,
                                                      Locale locale) {
        // 1. ErrorMapperRegistry를 통해 적절한 Mapper 선택 및 매핑
        var mapped = errorMapperRegistry.map(ex, locale)
            .orElseGet(() -> errorMapperRegistry.defaultMapping(ex));

        // 2. RFC 7807 ProblemDetail 생성
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(mapped.status(), mapped.detail());
        pd.setTitle(mapped.title());
        pd.setType(mapped.type());

        // 3. RFC 7807 optional fields / extension members
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("code", ex.code());
        if (!ex.args().isEmpty()) {
            pd.setProperty("args", ex.args());
        }

        // 4. 요청 경로를 instance로
        if (req != null) {
            String uri = req.getRequestURI();
            if (req.getQueryString() != null && !req.getQueryString().isBlank()) {
                uri = uri + "?" + req.getQueryString();
            }
            pd.setInstance(URI.create(uri));
        }

        // 5. tracing(id 존재 시) — Micrometer/Logback MDC 키 관례
        String traceId = MDC.get("traceId");
        String spanId  = MDC.get("spanId");
        if (traceId != null) pd.setProperty("traceId", traceId);
        if (spanId  != null) pd.setProperty("spanId",  spanId);

        // 6. HTTP 상태 코드에 따라 로깅 레벨 구분
        if (mapped.status().is5xxServerError()) {
            // 5xx: 서버 에러 - ERROR 레벨 (스택트레이스 포함)
            log.error("DomainException (Server Error): code={}, status={}, detail={}, args={}",
                ex.code(), mapped.status().value(), mapped.detail(), ex.args(), ex);
        } else if (mapped.status() == HttpStatus.NOT_FOUND) {
            // 404: 찾을 수 없음 - DEBUG 레벨 (정상 흐름, 로그 노이즈 방지)
            log.debug("DomainException (Not Found): code={}, status={}, detail={}, args={}",
                ex.code(), mapped.status().value(), mapped.detail(), ex.args());
        } else {
            // 기타 4xx: 클라이언트 에러 - WARN 레벨
            log.warn("DomainException (Client Error): code={}, status={}, detail={}, args={}",
                ex.code(), mapped.status().value(), mapped.detail(), ex.args());
        }

        return ResponseEntity.status(mapped.status()).body(pd);
    }

    // ... (다른 예외 핸들러들: Validation, IllegalArgument, 500 등)
}
```

---

## 7. RFC 7807 Problem Details

### 7.1 RFC 7807 표준

**RFC 7807**은 HTTP API 에러 응답 표준 형식입니다.

**표준 필드**:

| 필드 | 타입 | 필수 | 설명 | 예시 |
|------|------|------|------|------|
| `type` | URI | ✅ | 에러 타입 문서 URI | `https://api.example.com/problems/example-not-found` |
| `title` | String | ✅ | 에러 요약 (인간 친화적) | `"리소스를 찾을 수 없습니다"` |
| `status` | Integer | ✅ | HTTP 상태 코드 | `404` |
| `detail` | String | - | 에러 상세 설명 (인스턴스별) | `"요청하신 예시 리소스를 찾을 수 없습니다. (id=123)"` |
| `instance` | URI | - | 에러 발생 요청 URI | `"/api/v1/examples/123"` |

**확장 필드** (프로젝트별 커스터마이징):

| 필드 | 타입 | 설명 | 예시 |
|------|------|------|------|
| `code` | String | 도메인 예외 코드 | `"EXAMPLE_NOT_FOUND"` |
| `args` | Array | 예외 파라미터 | `["123"]` |
| `timestamp` | String | 에러 발생 시간 (ISO 8601) | `"2025-10-28T10:30:00Z"` |
| `traceId` | String | 분산 추적 ID | `"550e8400-e29b-41d4-a716-446655440000"` |
| `spanId` | String | Span ID | `"7a085853722dc6d2"` |

### 7.2 응답 예시

**Example Not Found (404)**:

```json
{
  "type": "https://api.example.com/problems/example-not-found",
  "title": "리소스를 찾을 수 없습니다",
  "status": 404,
  "detail": "요청하신 예시 리소스를 찾을 수 없습니다. (id=123)",
  "instance": "/api/v1/examples/123",
  "code": "EXAMPLE_NOT_FOUND",
  "args": ["123"],
  "timestamp": "2025-10-28T10:30:00Z",
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "spanId": "7a085853722dc6d2"
}
```

**Example Duplicate Key (409)**:

```json
{
  "type": "https://api.example.com/problems/example-duplicate-key",
  "title": "중복된 데이터",
  "status": 409,
  "detail": "이미 존재하는 값입니다. (key=example-123)",
  "instance": "/api/v1/examples",
  "code": "EXAMPLE_DUPLICATE_KEY",
  "args": ["example-123"],
  "timestamp": "2025-10-28T10:30:00Z",
  "traceId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Validation Error (400)**:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Validation failed for request",
  "instance": "/api/v1/examples",
  "errors": {
    "message": "message는 1자 이상이어야 합니다"
  },
  "timestamp": "2025-10-28T10:30:00Z",
  "traceId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 7.3 type URI 설계

**type URI**는 에러 타입을 설명하는 문서의 URI입니다.

**권장 패턴**:

```
https://api.{company}.com/problems/{error-code-kebab-case}
```

**예시**:

| 에러 코드 | type URI |
|----------|----------|
| `EXAMPLE_NOT_FOUND` | `https://api.example.com/problems/example-not-found` |
| `ORDER_ALREADY_CANCELLED` | `https://api.example.com/problems/order-already-cancelled` |
| `PRODUCT_INSUFFICIENT_STOCK` | `https://api.example.com/problems/product-insufficient-stock` |

**type URI 문서 예시** (`https://api.example.com/problems/example-not-found`):

```html
<!DOCTYPE html>
<html>
<head>
  <title>Example Not Found</title>
</head>
<body>
  <h1>EXAMPLE_NOT_FOUND</h1>
  <p><strong>Status:</strong> 404 Not Found</p>

  <h2>설명</h2>
  <p>요청한 Example 리소스를 찾을 수 없습니다.</p>

  <h2>발생 원인</h2>
  <ul>
    <li>존재하지 않는 Example ID로 조회 시도</li>
    <li>Example이 삭제된 경우</li>
  </ul>

  <h2>해결 방법</h2>
  <ul>
    <li>올바른 Example ID를 확인하세요</li>
    <li>Example 목록 API(/api/v1/examples)로 존재하는 ID를 확인하세요</li>
  </ul>

  <h2>관련 API</h2>
  <ul>
    <li>GET /api/v1/examples - Example 목록 조회</li>
    <li>GET /api/v1/examples/{id} - Example 단건 조회</li>
  </ul>
</body>
</html>
```

---

## 8. 로깅 전략

### 8.1 로깅 레벨 분리

**목적**: 로그 노이즈 방지 및 중요도에 따른 모니터링

| 상태 코드 | 로깅 레벨 | 이유 | 스택트레이스 |
|----------|----------|------|-------------|
| **5xx** (Server Error) | `ERROR` | 서버 문제, 즉시 대응 필요 | ✅ 포함 |
| **404** (Not Found) | `DEBUG` | 정상적인 흐름, 로그 노이즈 방지 | ❌ 제외 |
| **기타 4xx** (Client Error) | `WARN` | 클라이언트 오류, 모니터링 필요 | ❌ 제외 |

### 8.2 로깅 구현

```java
@ExceptionHandler(DomainException.class)
public ResponseEntity<ProblemDetail> handleDomain(DomainException ex,
                                                  HttpServletRequest req,
                                                  Locale locale) {
    var mapped = errorMapperRegistry.map(ex, locale)
        .orElseGet(() -> errorMapperRegistry.defaultMapping(ex));

    // ... ProblemDetail 생성 ...

    // HTTP 상태 코드에 따라 로깅 레벨 구분
    if (mapped.status().is5xxServerError()) {
        // 5xx: 서버 에러 - ERROR 레벨 (스택트레이스 포함)
        log.error("DomainException (Server Error): code={}, status={}, detail={}, args={}",
            ex.code(), mapped.status().value(), mapped.detail(), ex.args(), ex);
    } else if (mapped.status() == HttpStatus.NOT_FOUND) {
        // 404: 찾을 수 없음 - DEBUG 레벨 (정상 흐름, 로그 노이즈 방지)
        log.debug("DomainException (Not Found): code={}, status={}, detail={}, args={}",
            ex.code(), mapped.status().value(), mapped.detail(), ex.args());
    } else {
        // 기타 4xx: 클라이언트 에러 - WARN 레벨
        log.warn("DomainException (Client Error): code={}, status={}, detail={}, args={}",
            ex.code(), mapped.status().value(), mapped.detail(), ex.args());
    }

    return ResponseEntity.status(mapped.status()).body(pd);
}
```

### 8.3 로깅 예시

**5xx 서버 에러**:

```log
2025-10-28 10:30:00.123 ERROR [http-nio-8080-exec-1] c.r.a.i.r.c.c.GlobalExceptionHandler - DomainException (Server Error): code=INTERNAL_ERROR, status=500, detail=내부 서버 오류가 발생했습니다, args=[database]
com.ryuqq.domain.common.DomainException: Internal error
    at com.ryuqq.domain.example.Example.validateMessage(Example.java:45)
    at com.ryuqq.application.example.CreateExampleUseCase.execute(CreateExampleUseCase.java:30)
    ... (full stack trace)
```

**404 Not Found (DEBUG 레벨)**:

```log
2025-10-28 10:30:00.123 DEBUG [http-nio-8080-exec-1] c.r.a.i.r.c.c.GlobalExceptionHandler - DomainException (Not Found): code=EXAMPLE_NOT_FOUND, status=404, detail=요청하신 예시 리소스를 찾을 수 없습니다. (id=123), args=[123]
```

**400 Client Error (WARN 레벨)**:

```log
2025-10-28 10:30:00.123 WARN  [http-nio-8080-exec-1] c.r.a.i.r.c.c.GlobalExceptionHandler - DomainException (Client Error): code=EXAMPLE_INVALID_STATE, status=409, detail=현재 상태에서는 수행할 수 없습니다. (state=DELETED), args=[DELETED]
```

---

## 9. MessageSource 국제화

### 9.1 국제화 흐름

```
1. Client Request with Accept-Language Header
   ↓
2. GlobalExceptionHandler.handleDomain(ex, locale)
   ↓
3. ErrorMapperRegistry.map(ex, locale)
   ↓
4. ExampleErrorMapper.map(ex, locale)
   ├─ messageSource.getMessage("problem.title.example_not_found", args, locale)
   └─ messageSource.getMessage("problem.detail.example_not_found", args, locale)
   ↓
5. Response with localized messages
```

### 9.2 메시지 키 네이밍 컨벤션

**패턴**:

```
problem.{field}.{error_code_lowercase}
```

**예시**:

| 에러 코드 | title 키 | detail 키 |
|----------|---------|----------|
| `EXAMPLE_NOT_FOUND` | `problem.title.example_not_found` | `problem.detail.example_not_found` |
| `ORDER_ALREADY_CANCELLED` | `problem.title.order_already_cancelled` | `problem.detail.order_already_cancelled` |
| `PRODUCT_INSUFFICIENT_STOCK` | `problem.title.product_insufficient_stock` | `problem.detail.product_insufficient_stock` |

### 9.3 파라미터 치환

**messages_ko.properties**:

```properties
# {0}, {1}, {2} 형식으로 파라미터 치환
problem.detail.example_not_found=요청하신 예시 리소스를 찾을 수 없습니다. (id={0})
problem.detail.order_already_cancelled=이미 취소된 주문입니다. (orderId={0}, status={1})
problem.detail.product_insufficient_stock=재고가 부족합니다. (productId={0}, requested={1}, available={2})
```

**DomainException 생성 시 args 전달**:

```java
// Example Domain
throw new ExampleNotFoundException("EXAMPLE_NOT_FOUND", List.of("123"));
// args: ["123"]

// Order Domain
throw new OrderAlreadyCancelledException("ORDER_ALREADY_CANCELLED", List.of("ORD-123", "CANCELLED"));
// args: ["ORD-123", "CANCELLED"]

// Product Domain
throw new ProductInsufficientStockException("PRODUCT_INSUFFICIENT_STOCK", List.of("PROD-456", "10", "3"));
// args: ["PROD-456", "10", "3"]
```

**ErrorMapper에서 args 사용**:

```java
@Override
public MappedError map(DomainException ex, Locale locale) {
    String detailKey = "problem.detail." + ex.code().toLowerCase();

    // ex.args()를 배열로 변환하여 MessageSource에 전달
    Object[] args = ex.args().toArray();

    String detail = messageSource.getMessage(detailKey, args,
        ex.getMessage() != null ? ex.getMessage() : "Error", locale);

    // detail: "요청하신 예시 리소스를 찾을 수 없습니다. (id=123)"

    return new MappedError(status, title, detail, type);
}
```

### 9.4 Fallback 전략

**MessageSource가 키를 찾지 못한 경우**:

```java
String title  = messageSource.getMessage(titleKey,  args,
    defaultTitle,  // Fallback: HttpStatus.getReasonPhrase()
    locale);

String detail = messageSource.getMessage(detailKey, args,
    ex.getMessage() != null ? ex.getMessage() : defaultTitle,  // Fallback: DomainException 메시지
    locale);
```

**우선순위**:
1. `messages_{locale}.properties`에서 키 검색
2. Fallback 1: `messages.properties` (기본 로케일)
3. Fallback 2: 코드에서 제공한 기본값 (`defaultTitle` 또는 `ex.getMessage()`)

---

## 10. 베스트 프랙티스

### 10.1 ErrorMapper 설계 원칙

#### 1. 도메인당 하나의 ErrorMapper

**✅ 권장**:

```java
// Example 도메인
@Component
public class ExampleErrorMapper implements ErrorMapper {
    @Override
    public boolean supports(String code) {
        return code.startsWith("EXAMPLE_");
    }
}

// Order 도메인
@Component
public class OrderErrorMapper implements ErrorMapper {
    @Override
    public boolean supports(String code) {
        return code.startsWith("ORDER_");
    }
}
```

**❌ Anti-Pattern**:

```java
// 여러 도메인을 하나의 Mapper에서 처리 (단일 책임 위반)
@Component
public class CommonErrorMapper implements ErrorMapper {
    @Override
    public boolean supports(String code) {
        return code.startsWith("EXAMPLE_") || code.startsWith("ORDER_");
    }

    @Override
    public MappedError map(DomainException ex, Locale locale) {
        if (code.startsWith("EXAMPLE_")) {
            // Example 도메인 처리
        } else if (code.startsWith("ORDER_")) {
            // Order 도메인 처리
        }
        // ❌ 도메인이 추가될 때마다 if-else 증가
    }
}
```

#### 2. Prefix 기반 supports() 메서드

**✅ 권장 (Prefix)**:

```java
@Override
public boolean supports(String code) {
    return code != null && code.startsWith("EXAMPLE_");
}
```

**장점**:
- 새로운 에러 코드 추가 시 supports() 수정 불필요
- OCP(Open-Closed Principle) 준수

**❌ Anti-Pattern (Exact Match)**:

```java
@Override
public boolean supports(String code) {
    return Set.of("EXAMPLE_NOT_FOUND", "EXAMPLE_DUPLICATE_KEY", "EXAMPLE_INVALID_STATE")
        .contains(code);
}
// ❌ 새로운 에러 코드 추가 시 supports() 수정 필요
```

#### 3. MessageSource 활용

**✅ 권장**:

```java
@Component
public class ExampleErrorMapper implements ErrorMapper {
    private final MessageSource messageSource;  // ✅ MessageSource 주입

    public ExampleErrorMapper(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public MappedError map(DomainException ex, Locale locale) {
        // ✅ MessageSource로 국제화 메시지 조회
        String title = messageSource.getMessage(
            "problem.title." + ex.code().toLowerCase(),
            null,
            defaultTitle,
            locale
        );
        // ...
    }
}
```

**❌ Anti-Pattern (하드코딩)**:

```java
@Override
public MappedError map(DomainException ex, Locale locale) {
    // ❌ 메시지 하드코딩 (국제화 불가능)
    String title = switch (ex.code()) {
        case "EXAMPLE_NOT_FOUND" -> "리소스를 찾을 수 없습니다";
        case "EXAMPLE_DUPLICATE_KEY" -> "중복된 데이터";
        default -> "Bad Request";
    };
}
```

#### 4. type URI 설계

**✅ 권장 (사내 문서 URL)**:

```java
private static final String TYPE_BASE = "https://api.example.com/problems/";

URI type = URI.create(TYPE_BASE + code.toLowerCase().replace('_', '-'));
// 예: https://api.example.com/problems/example-not-found
```

**장점**:
- 클라이언트가 에러 상세 문서를 확인 가능
- 에러 해결 방법 제공
- API 문서화 품질 향상

**❌ Anti-Pattern (about:blank)**:

```java
URI type = URI.create("about:blank");
// ❌ 클라이언트에게 도움이 되지 않음
```

### 10.2 GlobalExceptionHandler 패턴

#### 1. ErrorMapperRegistry만 의존

**✅ 권장**:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    private final ErrorMapperRegistry errorMapperRegistry;  // ✅ Registry만 의존

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> handleDomain(DomainException ex, Locale locale) {
        // ✅ Registry가 적절한 Mapper 선택
        var mapped = errorMapperRegistry.map(ex, locale)
            .orElseGet(() -> errorMapperRegistry.defaultMapping(ex));
        // ...
    }
}
```

**❌ Anti-Pattern (개별 Mapper 의존)**:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    private final ExampleErrorMapper exampleErrorMapper;
    private final OrderErrorMapper orderErrorMapper;
    // ❌ 도메인이 추가될 때마다 필드 추가 필요

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> handleDomain(DomainException ex, Locale locale) {
        // ❌ 수동으로 Mapper 선택
        if (exampleErrorMapper.supports(ex.code())) {
            return exampleErrorMapper.map(ex, locale);
        } else if (orderErrorMapper.supports(ex.code())) {
            return orderErrorMapper.map(ex, locale);
        }
        // ❌ 도메인이 추가될 때마다 if-else 증가
    }
}
```

#### 2. 로깅 레벨 분리

**✅ 권장**:

```java
if (mapped.status().is5xxServerError()) {
    log.error("DomainException (Server Error): code={}, status={}, detail={}, args={}",
        ex.code(), mapped.status().value(), mapped.detail(), ex.args(), ex);
} else if (mapped.status() == HttpStatus.NOT_FOUND) {
    log.debug("DomainException (Not Found): code={}, status={}, detail={}, args={}",
        ex.code(), mapped.status().value(), mapped.detail(), ex.args());
} else {
    log.warn("DomainException (Client Error): code={}, status={}, detail={}, args={}",
        ex.code(), mapped.status().value(), mapped.detail(), ex.args());
}
```

**❌ Anti-Pattern (모두 ERROR 레벨)**:

```java
log.error("DomainException: code={}, status={}, detail={}, args={}",
    ex.code(), mapped.status().value(), mapped.detail(), ex.args(), ex);
// ❌ 404 Not Found도 ERROR 레벨로 로깅 → 로그 노이즈 발생
```

### 10.3 테스트 전략

#### 1. ErrorMapper 단위 테스트

```java
@ExtendWith(MockitoExtension.class)
class ExampleErrorMappingTest {

    @Mock
    private MessageSource messageSource;

    private ExampleErrorMapping exampleErrorMapping;

    @BeforeEach
    void setUp() {
        exampleErrorMapping = new ExampleErrorMapping(messageSource);
    }

    @Test
    @DisplayName("EXAMPLE_NOT_FOUND는 404 상태 코드로 매핑")
    void shouldMapToNotFound() {
        // Given
        DomainException ex = new DomainException("EXAMPLE_NOT_FOUND", List.of("123"));
        when(messageSource.getMessage(anyString(), any(), anyString(), any(Locale.class)))
            .thenReturn("Resource Not Found", "Example not found (id=123)");

        // When
        ErrorMapper.MappedError mapped = exampleErrorMapping.map(ex, Locale.KOREA);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, mapped.status());
        assertEquals("Resource Not Found", mapped.title());
        assertEquals("Example not found (id=123)", mapped.detail());
        assertTrue(mapped.type().toString().contains("example-not-found"));
    }

    @Test
    @DisplayName("EXAMPLE_DUPLICATE_KEY는 409 상태 코드로 매핑")
    void shouldMapToConflict() {
        // Given
        DomainException ex = new DomainException("EXAMPLE_DUPLICATE_KEY", List.of("example-123"));

        // When
        ErrorMapper.MappedError mapped = exampleErrorMapping.map(ex, Locale.KOREA);

        // Then
        assertEquals(HttpStatus.CONFLICT, mapped.status());
    }

    @Test
    @DisplayName("EXAMPLE_ 접두어를 가진 코드를 지원")
    void shouldSupportExamplePrefix() {
        // When & Then
        assertTrue(exampleErrorMapping.supports("EXAMPLE_NOT_FOUND"));
        assertTrue(exampleErrorMapping.supports("EXAMPLE_DUPLICATE_KEY"));
        assertFalse(exampleErrorMapping.supports("ORDER_NOT_FOUND"));
    }
}
```

#### 2. ErrorMapperRegistry 통합 테스트

```java
@SpringBootTest
class ErrorMapperRegistryTest {

    @Autowired
    private ErrorMapperRegistry errorMapperRegistry;

    @Test
    @DisplayName("Example 도메인 예외를 적절한 Mapper로 매핑")
    void shouldMapExampleException() {
        // Given
        DomainException ex = new DomainException("EXAMPLE_NOT_FOUND", List.of("123"));

        // When
        Optional<ErrorMapper.MappedError> mapped = errorMapperRegistry.map(ex, Locale.KOREA);

        // Then
        assertTrue(mapped.isPresent());
        assertEquals(HttpStatus.NOT_FOUND, mapped.get().status());
    }

    @Test
    @DisplayName("매칭되는 Mapper가 없으면 기본 매핑 반환")
    void shouldReturnDefaultMapping() {
        // Given
        DomainException ex = new DomainException("UNKNOWN_CODE", List.of());

        // When
        ErrorMapper.MappedError mapped = errorMapperRegistry.defaultMapping(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, mapped.status());
        assertEquals("Bad Request", mapped.title());
    }
}
```

---

## 요약

### 핵심 원칙

1. **OCP 준수**: 도메인 추가 시 GlobalExceptionHandler 수정 불필요
2. **단일 책임**: 각 ErrorMapper는 자신의 도메인 에러 매핑만 책임
3. **테스트 가능**: 도메인별 ErrorMapper를 독립적으로 단위 테스트
4. **RFC 7807 표준**: Problem Details 형식으로 일관된 에러 응답
5. **국제화 지원**: MessageSource 기반 다국어 메시지
6. **로깅 전략**: 5xx(ERROR), 404(DEBUG), 4xx(WARN) 레벨 분리

### 디렉토리 구조

```
adapter-in/rest-api/
├── common/
│   ├── mapper/
│   │   └── ErrorMapper.java  # 인터페이스
│   ├── error/
│   │   └── ErrorMapperRegistry.java  # Registry
│   └── controller/
│       └── GlobalExceptionHandler.java  # 예외 처리
├── config/
│   └── ErrorHandlingConfig.java  # Bean 등록
├── example/
│   └── error/
│       └── ExampleErrorMapping.java  # Example 도메인 Mapper
├── order/
│   └── error/
│       └── OrderErrorMapping.java  # Order 도메인 Mapper
└── resources/
    ├── messages_ko.properties  # 한국어 메시지
    └── messages_en.properties  # 영어 메시지
```

**✅ ErrorMapper 패턴을 사용하면 확장 가능하고 유지보수하기 쉬운 에러 처리 시스템을 구축할 수 있습니다.**
