# 메시지 국제화 (i18n) 가이드 - Message Internationalization

> **목적**: Spring Boot MessageSource를 사용한 다국어 메시지 관리 패턴
>
> **위치**: `adapter-in/rest-api/src/main/resources/messages*.properties`
>
> **관련 문서**:
> - `./01_application-yml-guide.md` - application.yml 설정
> - `../exception-handling/01_global-exception-handler.md` - 예외 처리에서 메시지 사용
>
> **필수 버전**: Spring Boot 3.5+, Java 21+

---

## 📌 핵심 원칙

### 1. 메시지 국제화의 목적

**다국어 지원 + 메시지 중앙 관리**

```
messages_ko.properties  (한국어)
messages_en.properties  (영어)
messages_ja.properties  (일본어)
         ↓
  MessageSource
         ↓
GlobalExceptionHandler / Controller / Service
         ↓
사용자 Locale에 맞는 메시지 반환
```

**장점**:
- 🌏 **다국어 지원**: 사용자 로케일에 맞는 메시지
- 🎯 **중앙 관리**: 메시지 변경 시 코드 수정 불필요
- 🔧 **유지보수성**: 메시지 파일만 수정
- 📝 **일관성**: 동일한 메시지 키로 여러 언어 지원

### 2. MessageSource 설정

**application.yml**:
```yaml
spring:
  messages:
    # 메시지 번들 베이스 이름 (messages_{locale}.properties 로딩)
    basename: messages
    # 한글 깨짐 방지: UTF-8 고정
    encoding: UTF-8
    # 서버 시스템 로케일에 의존하지 않음
    fallback-to-system-locale: false
```

**동작**:
- `basename: messages` → `messages_ko.properties`, `messages_en.properties` 자동 로딩
- `encoding: UTF-8` → 한글 깨짐 방지
- `fallback-to-system-locale: false` → 명시적 로케일만 사용 (예측 가능성)

### 3. 파일 명명 규칙

**표준 형식**: `messages_{locale}.properties`

```
src/main/resources/
├── messages.properties          # 기본 (fallback) - 영어 권장
├── messages_ko.properties       # 한국어
├── messages_en.properties       # 영어
├── messages_ja.properties       # 일본어
└── messages_zh_CN.properties    # 중국어 (간체)
```

**규칙**:
- ✅ `messages.properties`: Locale이 없을 때 fallback으로 사용
- ✅ `messages_ko.properties`: 한국어 (`ko`, `ko_KR`)
- ✅ `messages_en.properties`: 영어 (`en`, `en_US`, `en_GB`)
- ❌ `messages-ko.properties`: 하이픈 사용 금지 (언더스코어 사용)

---

## 🔧 메시지 파일 구조

### 표준 messages_ko.properties

```properties
# ===============================================
# 한국어 에러 메시지 (messages_ko.properties)
# ===============================================
# Spring Boot MessageSource에 의해 로딩
# UTF-8 인코딩 필수
#
# 사용 예시:
# messageSource.getMessage("error.example.not_found", args, locale)
#
# 파라미터 사용:
# {0}, {1}, {2} 형식으로 args 배열 값 참조
#
# @author windsurf
# @since 1.0.0
# ===============================================

# ===============================================
# Example 도메인 에러 메시지
# ===============================================

# Example을 찾을 수 없음 (404)
# 파라미터: {0} = ID
error.example.not_found=요청한 Example(ID: {0})을 찾을 수 없습니다.

# Example이 이미 존재함 (409)
# 파라미터: {0} = message 또는 ID
error.example.already_exists=이미 존재하는 Example입니다. (중복: {0})

# Example 상태가 유효하지 않음 (400)
# 파라미터: {0} = currentStatus, {1} = attemptedStatus
error.example.invalid_status=유효하지 않은 상태 전환입니다. (현재: {0}, 시도: {1})

# Example 메시지가 비어있음 (400)
error.example.empty_message=Example 메시지는 비어 있을 수 없습니다.

# Example 메시지가 너무 김 (400)
# 파라미터: {0} = 최대 길이, {1} = 현재 길이
error.example.message_too_long=Example 메시지가 최대 길이를 초과했습니다. (최대: {0}자, 현재: {1}자)

# Example을 삭제할 수 없음 (409)
# 파라미터: {0} = ID
error.example.cannot_delete=삭제할 수 없는 Example입니다. (ID: {0})

# ===============================================
# 공통 에러 메시지
# ===============================================

# 일반적인 400 Bad Request
error.common.bad_request=잘못된 요청입니다.

# 일반적인 401 Unauthorized
error.common.unauthorized=인증이 필요합니다.

# 일반적인 403 Forbidden
error.common.forbidden=접근 권한이 없습니다.

# 일반적인 404 Not Found
error.common.not_found=요청한 리소스를 찾을 수 없습니다.

# 일반적인 409 Conflict
error.common.conflict=리소스 충돌이 발생했습니다.

# 일반적인 500 Internal Server Error
error.common.internal_server_error=서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.
```

### 표준 messages_en.properties

```properties
# ===============================================
# English Error Messages (messages_en.properties)
# ===============================================
# Used by Spring Boot MessageSource
# UTF-8 encoding required
#
# Usage:
# messageSource.getMessage("error.example.not_found", args, locale)
#
# Parameter usage:
# {0}, {1}, {2} format to reference args array values
#
# @author windsurf
# @since 1.0.0
# ===============================================

# ===============================================
# Example Domain Error Messages
# ===============================================

# Example not found (404)
# Parameters: {0} = ID
error.example.not_found=Example not found (ID: {0})

# Example already exists (409)
# Parameters: {0} = message or ID
error.example.already_exists=Example already exists (duplicate: {0})

# Example status is invalid (400)
# Parameters: {0} = currentStatus, {1} = attemptedStatus
error.example.invalid_status=Invalid status transition (current: {0}, attempted: {1})

# Example message is empty (400)
error.example.empty_message=Example message cannot be empty

# Example message is too long (400)
# Parameters: {0} = max length, {1} = current length
error.example.message_too_long=Example message exceeds maximum length (max: {0}, current: {1})

# Example cannot be deleted (409)
# Parameters: {0} = ID
error.example.cannot_delete=Cannot delete Example (ID: {0})

# ===============================================
# Common Error Messages
# ===============================================

# Generic 400 Bad Request
error.common.bad_request=Bad request

# Generic 401 Unauthorized
error.common.unauthorized=Authentication required

# Generic 403 Forbidden
error.common.forbidden=Access forbidden

# Generic 404 Not Found
error.common.not_found=Resource not found

# Generic 409 Conflict
error.common.conflict=Resource conflict occurred

# Generic 500 Internal Server Error
error.common.internal_server_error=Internal server error. Please try again later
```

---

## 🎯 메시지 키 네이밍 규칙

### 계층적 네이밍

**형식**: `{category}.{domain}.{error_type}`

```properties
# ✅ Good: 계층적 구조
error.order.not_found=주문을 찾을 수 없습니다. (ID: {0})
error.order.invalid_status=유효하지 않은 주문 상태입니다.
error.customer.not_found=고객을 찾을 수 없습니다. (ID: {0})
error.customer.duplicate_email=이미 존재하는 이메일입니다. ({0})

# ❌ Bad: 비계층적 구조
orderNotFound=주문을 찾을 수 없습니다.
order_error=주문 에러
```

### 카테고리별 키

**카테고리**:
1. `error.*` - 에러 메시지
2. `validation.*` - 검증 메시지
3. `success.*` - 성공 메시지
4. `info.*` - 안내 메시지

```properties
# 에러
error.order.not_found=주문을 찾을 수 없습니다.

# 검증
validation.order.amount.min=주문 금액은 {0}원 이상이어야 합니다.
validation.order.amount.max=주문 금액은 {0}원 이하여야 합니다.

# 성공
success.order.created=주문이 생성되었습니다. (ID: {0})

# 안내
info.order.processing=주문 처리 중입니다...
```

---

## 🔧 MessageSource 사용 방법

### 1. GlobalExceptionHandler에서 사용

```java
package com.ryuqq.adapter.in.rest.common.controller;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> handleDomain(
            DomainException ex,
            HttpServletRequest req,
            Locale locale) {

        // ✅ MessageSource를 사용한 다국어 메시지 로딩
        String message = messageSource.getMessage(
            ex.code(),           // 메시지 키 (예: "error.order.not_found")
            ex.args(),           // 파라미터 배열
            ex.getMessage(),     // 기본 메시지 (키가 없을 때)
            locale               // 사용자 로케일
        );

        var mapped = new MappedError(
            ex.getHttpStatus(),
            "Error",
            message,              // 로케일에 맞는 메시지
            URI.create("about:blank")
        );

        var res = build(mapped.status(), mapped.title(), mapped.detail(), req);
        return ResponseEntity.status(mapped.status()).body(res.getBody());
    }
}
```

### 2. ErrorMapper에서 사용

```java
package com.ryuqq.adapter.in.rest.example.error;

import com.ryuqq.adapter.in.rest.common.mapper.ErrorMapper;
import com.ryuqq.domain.common.DomainException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Locale;

/**
 * Example 도메인 ErrorMapper
 *
 * <p>Example 도메인 예외를 HTTP 응답으로 변환합니다.</p>
 */
@Component
public class ExampleErrorMapper implements ErrorMapper {

    private final MessageSource messageSource;

    public ExampleErrorMapper(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public boolean supports(String code) {
        return code.startsWith("EXAMPLE_");
    }

    @Override
    public MappedError map(DomainException ex, Locale locale) {
        // ✅ MessageSource를 사용한 다국어 메시지 로딩
        String message = messageSource.getMessage(
            "error.example." + ex.code().toLowerCase().replace("_", "."),
            ex.args(),
            ex.getMessage(),
            locale
        );

        HttpStatus status = switch (ex.code()) {
            case "EXAMPLE_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "EXAMPLE_ALREADY_EXISTS" -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };

        return new MappedError(
            status,
            "Example Error",
            message,
            URI.create("about:blank")
        );
    }
}
```

### 3. Validation 메시지에서 사용

```java
package com.ryuqq.adapter.in.rest.order.dto.request;

import jakarta.validation.constraints.*;

/**
 * 주문 생성 요청 DTO
 */
public record CreateOrderApiRequest(

    @NotNull(message = "{validation.order.customer_id.not_null}")
    @Positive(message = "{validation.order.customer_id.positive}")
    Long customerId,

    @NotNull(message = "{validation.order.amount.not_null}")
    @Min(value = 1000, message = "{validation.order.amount.min}")
    @Max(value = 10000000, message = "{validation.order.amount.max}")
    Long amount

) {}
```

**messages_ko.properties**:
```properties
validation.order.customer_id.not_null=고객 ID는 필수입니다.
validation.order.customer_id.positive=고객 ID는 양수여야 합니다.
validation.order.amount.not_null=주문 금액은 필수입니다.
validation.order.amount.min=주문 금액은 {value}원 이상이어야 합니다.
validation.order.amount.max=주문 금액은 {value}원 이하여야 합니다.
```

---

## 🎯 메시지 파라미터 활용

### 파라미터 전달 방법

```java
// ✅ Good: 파라미터 배열 전달
String message = messageSource.getMessage(
    "error.order.not_found",
    new Object[]{orderId},           // {0} → orderId
    locale
);

// ✅ Good: 여러 파라미터 전달
String message = messageSource.getMessage(
    "error.order.invalid_status",
    new Object[]{currentStatus, attemptedStatus},  // {0}, {1}
    locale
);
```

### 메시지 파일에서 파라미터 사용

```properties
# 단일 파라미터
error.order.not_found=주문을 찾을 수 없습니다. (ID: {0})
# 결과: "주문을 찾을 수 없습니다. (ID: 123)"

# 복수 파라미터
error.order.invalid_status=유효하지 않은 상태 전환입니다. (현재: {0}, 시도: {1})
# 결과: "유효하지 않은 상태 전환입니다. (현재: PENDING, 시도: CANCELLED)"

# 순서 재배치
error.order.message_too_long=메시지가 최대 {0}자를 초과했습니다. (현재: {1}자)
# 결과: "메시지가 최대 100자를 초과했습니다. (현재: 150자)"
```

---

## 🌏 Locale 결정 방법

### 1. Accept-Language 헤더 사용 (기본)

```java
// Spring이 자동으로 Accept-Language 헤더를 Locale로 변환
@ExceptionHandler(DomainException.class)
public ResponseEntity<?> handleDomain(
        DomainException ex,
        Locale locale) {  // ← Spring이 자동 주입

    String message = messageSource.getMessage(ex.code(), ex.args(), locale);
    return ResponseEntity.badRequest().body(message);
}
```

**HTTP 요청**:
```http
GET /api/v1/orders/123
Accept-Language: ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7
```

**결과**: `messages_ko.properties` 사용

### 2. LocaleResolver 커스터마이징

```java
package com.ryuqq.adapter.in.rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

@Configuration
public class LocaleConfig {

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.KOREAN);  // 기본 로케일: 한국어
        return resolver;
    }
}
```

### 3. 쿼리 파라미터로 Locale 지정 (선택적)

```java
@Configuration
public class LocaleConfig {

    @Bean
    public LocaleResolver localeResolver() {
        // 쿼리 파라미터로 로케일 지정 가능
        // 예: /api/v1/orders?lang=en
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.KOREAN);
        return resolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");  // ?lang=ko, ?lang=en
        return interceptor;
    }
}
```

---

## 🚨 Do / Don't

### ❌ Bad Examples

```properties
# ❌ 하드코딩된 메시지 (코드에서)
throw new DomainException("ORDER_NOT_FOUND", "주문을 찾을 수 없습니다.");

# ❌ 비일관적인 키 네이밍
orderNotFound=주문 없음
order_error_001=에러
errorOrder=주문 에러

# ❌ 파라미터 누락
error.order.message_too_long=메시지가 너무 깁니다.
// {0}, {1} 파라미터 없음

# ❌ 로케일별 메시지 불일치
# messages_ko.properties
error.order.not_found=주문을 찾을 수 없습니다. (ID: {0})

# messages_en.properties
error.order.not_found=Order not found
// 파라미터 개수 불일치!

# ❌ UTF-8 인코딩 누락 (한글 깨짐)
error.order.not_found=\uC8FC\uBB38\uC744 \uCC3E\uC744 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4.
```

### ✅ Good Examples

```properties
# ✅ 계층적 키 네이밍
error.order.not_found=주문을 찾을 수 없습니다. (ID: {0})
error.order.invalid_status=유효하지 않은 상태 전환입니다.
error.customer.not_found=고객을 찾을 수 없습니다. (ID: {0})

# ✅ 파라미터 명확히 표시
# 파라미터: {0} = ID
error.order.not_found=주문을 찾을 수 없습니다. (ID: {0})

# ✅ 로케일별 메시지 일치
# messages_ko.properties
error.order.not_found=주문을 찾을 수 없습니다. (ID: {0})

# messages_en.properties
error.order.not_found=Order not found (ID: {0})
// 파라미터 개수 일치!

# ✅ UTF-8 인코딩 사용
error.order.not_found=주문을 찾을 수 없습니다.
```

---

## 📋 체크리스트

메시지 국제화 구현 시:
- [ ] `application.yml`에 MessageSource 설정 (`encoding: UTF-8`)
- [ ] `messages.properties` (fallback 영어) 생성
- [ ] `messages_ko.properties` (한국어) 생성
- [ ] `messages_en.properties` (영어) 생성
- [ ] 계층적 키 네이밍 (`error.{domain}.{type}`)
- [ ] 모든 로케일에서 파라미터 개수 일치
- [ ] 주석으로 파라미터 의미 설명
- [ ] GlobalExceptionHandler에서 MessageSource 사용
- [ ] ErrorMapper에서 MessageSource 사용
- [ ] Validation 메시지에 `{...}` 플레이스홀더 사용
- [ ] LocaleResolver 설정 (기본 로케일 지정)
- [ ] UTF-8 인코딩 확인 (한글 깨짐 방지)

---

**작성자**: Development Team
**최종 수정일**: 2025-11-04
**버전**: 1.0.0
