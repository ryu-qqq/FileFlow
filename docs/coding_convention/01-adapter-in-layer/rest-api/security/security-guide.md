# Security Guide — **REST API 보안 아키텍처**

> **목적**: REST API Layer의 인증/인가 보안 아키텍처 및 구현 가이드
>
> **철학**: 업계 표준 준수, 컴포넌트 분리, Zero-Trust 보안 모델

---

## 1️⃣ 보안 아키텍처 개요

### 전체 구조

```
┌─────────────────────────────────────────────────────────────────┐
│                        Security Layer                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────────────┐ │
│  │ MdcLogging  │ → │ JwtAuth     │ → │ Security            │ │
│  │ Filter      │    │ Filter      │    │ FilterChain         │ │
│  └─────────────┘    └─────────────┘    └─────────────────────┘ │
│         │                  │                                    │
│         ↓                  ↓                                    │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────────────┐ │
│  │ MdcContext  │    │ TokenCookie │    │ AuthenticationError │ │
│  │ Holder      │    │ Writer      │    │ Handler             │ │
│  └─────────────┘    └─────────────┘    └─────────────────────┘ │
│                            │                     │              │
│                            ↓                     ↓              │
│                     ┌─────────────┐    ┌─────────────────────┐ │
│                     │ Security    │    │ RFC 7807            │ │
│                     │ Context     │    │ ProblemDetail       │ │
│                     │ Authenticator│    │                     │ │
│                     └─────────────┘    └─────────────────────┘ │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│                     Configuration Layer                         │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────────────┐ │
│  │ ApiPaths    │    │ Security    │    │ SecurityConfig      │ │
│  │ (Constants) │    │ Paths       │    │ (@Configuration)    │ │
│  └─────────────┘    └─────────────┘    └─────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### 컴포넌트 역할

| 컴포넌트 | 책임 | 패키지 위치 |
|----------|------|-------------|
| **ApiPaths** | API 경로 상수 정의 | `auth/paths/` |
| **SecurityPaths** | 보안 정책별 경로 그룹화 | `auth/paths/` |
| **SecurityConfig** | Spring Security 설정 | `auth/config/` |
| **JwtAuthenticationFilter** | JWT 토큰 검증 + Silent Refresh | `auth/filter/` |
| **MdcLoggingFilter** | Request ID 추적 | `auth/filter/` |
| **SecurityContextAuthenticator** | SecurityContext 인증 설정 | `auth/component/` |
| **TokenCookieWriter** | HttpOnly 쿠키 관리 | `auth/component/` |
| **MdcContextHolder** | MDC 컨텍스트 관리 | `auth/component/` |
| **AuthenticationErrorHandler** | 인증/인가 에러 처리 | `auth/handler/` |

---

## 2️⃣ 패키지 구조

### 권장 구조

```
adapter-in/rest-api/src/main/java/com/company/adapter/in/rest/
├── auth/
│   ├── config/
│   │   └── SecurityConfig.java          # Spring Security 설정
│   ├── paths/
│   │   ├── ApiPaths.java                 # API 경로 상수
│   │   └── SecurityPaths.java            # 보안 정책별 경로 그룹
│   ├── filter/
│   │   ├── JwtAuthenticationFilter.java  # JWT 인증 필터
│   │   └── MdcLoggingFilter.java         # MDC 로깅 필터
│   ├── component/
│   │   ├── SecurityContextAuthenticator.java  # 인증 설정
│   │   ├── TokenCookieWriter.java        # 쿠키 관리
│   │   └── MdcContextHolder.java         # MDC 컨텍스트
│   ├── handler/
│   │   ├── AuthenticationErrorHandler.java    # 인증 에러 처리
│   │   └── OAuth2SuccessHandler.java     # OAuth2 성공 처리
│   └── utils/
│       └── CookieUtils.java              # 쿠키 유틸리티
└── ...
```

---

## 3️⃣ 핵심 컴포넌트 상세

### 3.1 ApiPaths (경로 상수)

> **상세 가이드**: [api-paths-guide.md](./api-paths-guide.md)

```java
package com.company.adapter.in.rest.auth.paths;

/**
 * API 경로 상수 정의
 *
 * <p>모든 REST API 엔드포인트 경로를 상수로 관리합니다.
 *
 * <p>장점:
 * <ul>
 *   <li>컴파일 타임 검증 - 오타 방지</li>
 *   <li>IDE 자동완성/리팩토링 지원</li>
 *   <li>Controller와 Security 경로 동기화 보장</li>
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public final class ApiPaths {

    public static final String API_V1 = "/api/v1";

    private ApiPaths() {
        // 인스턴스화 방지
    }

    /**
     * Member 도메인 경로
     */
    public static final class Members {
        public static final String BASE = API_V1 + "/members";
        public static final String REGISTER = BASE;                    // POST
        public static final String BY_ID = BASE + "/{id}";            // GET, PUT, DELETE
        public static final String PASSWORD_RESET = BASE + "/password/reset";  // POST

        private Members() {}
    }

    /**
     * Auth 도메인 경로
     */
    public static final class Auth {
        public static final String BASE = API_V1 + "/auth";
        public static final String LOGIN = BASE + "/login";           // POST
        public static final String LOGOUT = BASE + "/logout";         // POST
        public static final String REFRESH = BASE + "/refresh";       // POST

        private Auth() {}
    }

    /**
     * Order 도메인 경로
     */
    public static final class Orders {
        public static final String BASE = API_V1 + "/orders";
        public static final String BY_ID = BASE + "/{id}";
        public static final String CANCEL = BASE + "/{id}/cancel";
        public static final String CONFIRM = BASE + "/{id}/confirm";

        private Orders() {}
    }
}
```

### 3.2 SecurityPaths (보안 정책 그룹)

```java
package com.company.adapter.in.rest.auth.paths;

/**
 * 보안 정책별 경로 그룹화
 *
 * <p>인증/인가 정책에 따라 경로를 그룹화합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public final class SecurityPaths {

    private SecurityPaths() {}

    /**
     * 인증 불필요 엔드포인트 (Public)
     *
     * <p>로그인, 회원가입 등 인증 없이 접근 가능한 경로
     */
    public static final String[] PUBLIC_ENDPOINTS = {
        ApiPaths.Members.REGISTER,
        ApiPaths.Members.PASSWORD_RESET,
        ApiPaths.Auth.LOGIN,
        ApiPaths.Auth.REFRESH
    };

    /**
     * 인증 불필요 패턴 (Public Patterns)
     *
     * <p>와일드카드가 필요한 공개 경로
     */
    public static final String[] PUBLIC_PATTERNS = {
        "/oauth2/**",
        "/login/oauth2/**",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/actuator/**",
        ApiPaths.API_V1 + "/health"
    };

    /**
     * 관리자 전용 엔드포인트 (ROLE_ADMIN)
     */
    public static final String[] ADMIN_ENDPOINTS = {
        ApiPaths.API_V1 + "/admin/**"
    };

    /**
     * 리소스 소유자 검증 필요 엔드포인트
     *
     * <p>Method Security로 추가 검증 필요
     */
    public static final String[] OWNER_VERIFICATION_REQUIRED = {
        ApiPaths.Members.BY_ID,
        ApiPaths.Orders.BY_ID,
        ApiPaths.Orders.CANCEL
    };
}
```

### 3.3 SecurityConfig (Spring Security 설정)

```java
package com.company.adapter.in.rest.auth.config;

import com.company.adapter.in.rest.auth.filter.JwtAuthenticationFilter;
import com.company.adapter.in.rest.auth.handler.AuthenticationErrorHandler;
import com.company.adapter.in.rest.auth.paths.SecurityPaths;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정
 *
 * <p>JWT 기반 Stateless 인증 설정을 구성합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // @PreAuthorize 활성화
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationErrorHandler authenticationErrorHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AuthenticationErrorHandler authenticationErrorHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationErrorHandler = authenticationErrorHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            // CSRF 비활성화 (JWT 사용)
            .csrf(AbstractHttpConfigurer::disable)

            // Session Stateless
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 인가 설정
            .authorizeHttpRequests(auth -> auth
                // Public Endpoints (인증 불필요)
                .requestMatchers(SecurityPaths.PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers(SecurityPaths.PUBLIC_PATTERNS).permitAll()

                // Admin Endpoints (ROLE_ADMIN 필요)
                .requestMatchers(SecurityPaths.ADMIN_ENDPOINTS).hasRole("ADMIN")

                // 나머지는 인증 필요
                .anyRequest().authenticated()
            )

            // JWT 필터 추가
            .addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class)

            // 에러 핸들러 설정
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(authenticationErrorHandler)  // 401
                .accessDeniedHandler(authenticationErrorHandler)       // 403
            )

            .build();
    }
}
```

### 3.4 JwtAuthenticationFilter (JWT 인증 + Silent Refresh)

```java
package com.company.adapter.in.rest.auth.filter;

import com.company.adapter.in.rest.auth.component.MdcContextHolder;
import com.company.adapter.in.rest.auth.component.SecurityContextAuthenticator;
import com.company.adapter.in.rest.auth.component.TokenCookieWriter;
import com.company.adapter.in.rest.auth.utils.CookieUtils;
import com.company.application.member.dto.response.TokenPairResponse;
import com.company.application.member.port.out.TokenProviderPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT Authentication Filter
 *
 * <p>쿠키에서 Access Token을 추출하여 인증을 처리하는 필터
 *
 * <p>동작 방식:
 * <ol>
 *   <li>쿠키에서 Access Token 추출</li>
 *   <li>Access Token 유효 → 인증 성공</li>
 *   <li>Access Token 만료 + Refresh Token 유효 → Silent Refresh (자동 갱신)</li>
 *   <li>둘 다 없거나 만료 → 인증 실패 (다음 필터로 전달)</li>
 * </ol>
 *
 * <p>Silent Refresh:
 * <ul>
 *   <li>Access Token이 만료되었지만 Refresh Token이 유효한 경우</li>
 *   <li>새로운 Access Token을 발급하여 쿠키에 설정</li>
 *   <li>사용자는 401 없이 계속 서비스 이용 가능</li>
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenProviderPort tokenProviderPort;
    private final TokenCookieWriter tokenCookieWriter;
    private final SecurityContextAuthenticator securityContextAuthenticator;
    private final MdcContextHolder mdcContextHolder;

    public JwtAuthenticationFilter(
            TokenProviderPort tokenProviderPort,
            TokenCookieWriter tokenCookieWriter,
            SecurityContextAuthenticator securityContextAuthenticator,
            MdcContextHolder mdcContextHolder) {
        this.tokenProviderPort = tokenProviderPort;
        this.tokenCookieWriter = tokenCookieWriter;
        this.securityContextAuthenticator = securityContextAuthenticator;
        this.mdcContextHolder = mdcContextHolder;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        Optional<String> accessToken = extractAccessToken(request);

        if (accessToken.isPresent()) {
            String token = accessToken.get();

            if (tokenProviderPort.validateAccessToken(token)) {
                // Access Token 유효 → 인증 성공
                authenticateAndSetMdc(request, token);
            } else if (tokenProviderPort.isAccessTokenExpired(token)) {
                // Access Token 만료 → Silent Refresh 시도
                trySilentRefresh(request, response);
            }
        } else {
            // Access Token 없음 → Refresh Token으로 Silent Refresh 시도
            trySilentRefresh(request, response);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * SecurityContext 인증 및 MDC 설정
     */
    private void authenticateAndSetMdc(HttpServletRequest request, String accessToken) {
        String memberId = securityContextAuthenticator.authenticate(request, accessToken);
        mdcContextHolder.setMemberId(memberId);
    }

    /**
     * Silent Refresh 시도
     *
     * <p>Refresh Token이 유효한 경우 새로운 토큰 쌍을 발급하고 쿠키에 설정
     */
    private void trySilentRefresh(HttpServletRequest request, HttpServletResponse response) {
        CookieUtils.getRefreshToken(request)
            .filter(tokenProviderPort::validateRefreshToken)
            .ifPresent(refreshToken -> {
                String memberId = tokenProviderPort.extractMemberIdFromRefreshToken(refreshToken);
                TokenPairResponse newTokens = tokenProviderPort.generateTokenPair(memberId);

                // 새 토큰을 쿠키에 설정
                tokenCookieWriter.addTokenCookies(
                    response,
                    newTokens.accessToken(),
                    newTokens.refreshToken(),
                    newTokens.accessTokenExpiresIn(),
                    newTokens.refreshTokenExpiresIn());

                // 새 Access Token으로 인증 설정
                authenticateAndSetMdc(request, newTokens.accessToken());
            });
    }

    /**
     * Access Token 추출
     *
     * <p>우선순위:
     * <ol>
     *   <li>쿠키에서 추출 (access_token)</li>
     *   <li>Authorization 헤더에서 추출 (Bearer ...)</li>
     * </ol>
     */
    private Optional<String> extractAccessToken(HttpServletRequest request) {
        // 1. 쿠키에서 추출 시도
        Optional<String> cookieToken = CookieUtils.getAccessToken(request);
        if (cookieToken.isPresent()) {
            return cookieToken;
        }

        // 2. Authorization 헤더에서 추출 시도
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return Optional.of(authHeader.substring(BEARER_PREFIX.length()));
        }

        return Optional.empty();
    }
}
```

### 3.5 AuthenticationErrorHandler (RFC 7807 에러 처리)

```java
package com.company.adapter.in.rest.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Authentication Error Handler
 *
 * <p>인증/인가 에러를 RFC 7807 ProblemDetail 형식으로 처리
 *
 * <p>역할:
 * <ul>
 *   <li>AuthenticationEntryPoint: 인증 실패 (401 Unauthorized)</li>
 *   <li>AccessDeniedHandler: 인가 실패 (403 Forbidden)</li>
 * </ul>
 *
 * <p>응답 형식:
 * <ul>
 *   <li>RFC 7807 ProblemDetail 표준 준수</li>
 *   <li>GlobalExceptionHandler와 동일한 형식 유지</li>
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class AuthenticationErrorHandler
        implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationErrorHandler.class);

    private final ObjectMapper objectMapper;

    public AuthenticationErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 인증 실패 처리 (401 Unauthorized)
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        log.debug("Authentication failed: {}", authException.getMessage());

        ProblemDetail problemDetail = buildProblemDetail(
            request,
            HttpStatus.UNAUTHORIZED,
            "Unauthorized",
            "인증이 필요합니다. 로그인 후 다시 시도해주세요.",
            "AUTH_REQUIRED");

        writeResponse(response, HttpStatus.UNAUTHORIZED, problemDetail);
    }

    /**
     * 인가 실패 처리 (403 Forbidden)
     */
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {

        log.warn("Access denied: {}", accessDeniedException.getMessage());

        ProblemDetail problemDetail = buildProblemDetail(
            request,
            HttpStatus.FORBIDDEN,
            "Forbidden",
            "해당 리소스에 대한 접근 권한이 없습니다.",
            "ACCESS_DENIED");

        writeResponse(response, HttpStatus.FORBIDDEN, problemDetail);
    }

    /**
     * RFC 7807 ProblemDetail 생성
     */
    private ProblemDetail buildProblemDetail(
            HttpServletRequest request,
            HttpStatus status,
            String title,
            String detail,
            String code) {

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setType(URI.create("about:blank"));

        // 표준 확장 필드
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("code", code);

        // instance (요청 경로)
        String uri = request.getRequestURI();
        if (request.getQueryString() != null && !request.getQueryString().isBlank()) {
            uri = uri + "?" + request.getQueryString();
        }
        pd.setInstance(URI.create(uri));

        // Tracing 정보 (MDC에서)
        String traceId = MDC.get("traceId");
        String requestId = MDC.get("requestId");
        if (traceId != null) {
            pd.setProperty("traceId", traceId);
        }
        if (requestId != null) {
            pd.setProperty("requestId", requestId);
        }

        return pd;
    }

    /**
     * JSON 응답 작성
     */
    private void writeResponse(
            HttpServletResponse response,
            HttpStatus status,
            ProblemDetail problemDetail) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(response.getWriter(), problemDetail);
    }
}
```

### 3.6 TokenCookieWriter (HttpOnly 쿠키 관리)

```java
package com.company.adapter.in.rest.auth.component;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

/**
 * Token Cookie Writer
 *
 * <p>JWT 토큰을 HttpOnly 쿠키로 관리하는 컴포넌트
 *
 * <p>쿠키 보안 설정:
 * <ul>
 *   <li>HttpOnly: true - JavaScript 접근 차단 (XSS 방지)</li>
 *   <li>Secure: 환경 설정에 따름 - HTTPS 전용 (프로덕션)</li>
 *   <li>SameSite: Lax - CSRF 방지</li>
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class TokenCookieWriter {

    public static final String ACCESS_TOKEN_COOKIE = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private static final String ROOT_PATH = "/";

    private final boolean secure;
    private final String domain;
    private final String sameSite;

    public TokenCookieWriter(SecurityProperties securityProperties) {
        this.secure = securityProperties.getCookie().isSecure();
        this.domain = securityProperties.getCookie().getDomain();
        this.sameSite = securityProperties.getCookie().getSameSite();
    }

    /**
     * 토큰 쿠키들을 Response에 추가
     */
    public void addTokenCookies(
            HttpServletResponse response,
            String accessToken,
            String refreshToken,
            long accessTokenExpiry,
            long refreshTokenExpiry) {
        addAccessTokenCookie(response, accessToken, accessTokenExpiry);
        addRefreshTokenCookie(response, refreshToken, refreshTokenExpiry);
    }

    /**
     * Access Token 쿠키 추가
     */
    public void addAccessTokenCookie(
            HttpServletResponse response,
            String accessToken,
            long accessTokenExpiry) {
        String cookieValue = buildCookieValue(
            ACCESS_TOKEN_COOKIE, accessToken, ROOT_PATH, accessTokenExpiry);
        response.addHeader("Set-Cookie", cookieValue);
    }

    /**
     * Refresh Token 쿠키 추가
     */
    public void addRefreshTokenCookie(
            HttpServletResponse response,
            String refreshToken,
            long refreshTokenExpiry) {
        String cookieValue = buildCookieValue(
            REFRESH_TOKEN_COOKIE, refreshToken, ROOT_PATH, refreshTokenExpiry);
        response.addHeader("Set-Cookie", cookieValue);
    }

    /**
     * 토큰 쿠키들 삭제 (로그아웃)
     */
    public void deleteTokenCookies(HttpServletResponse response) {
        String accessCookie = buildCookieValue(ACCESS_TOKEN_COOKIE, "", ROOT_PATH, 0);
        String refreshCookie = buildCookieValue(REFRESH_TOKEN_COOKIE, "", ROOT_PATH, 0);
        response.addHeader("Set-Cookie", accessCookie);
        response.addHeader("Set-Cookie", refreshCookie);
    }

    /**
     * Set-Cookie 헤더 값 생성
     */
    private String buildCookieValue(String name, String value, String path, long maxAge) {
        StringBuilder cookie = new StringBuilder();
        cookie.append(name).append("=").append(value);
        cookie.append("; Path=").append(path);
        cookie.append("; Max-Age=").append(maxAge);
        cookie.append("; HttpOnly");

        if (secure) {
            cookie.append("; Secure");
        }

        if (domain != null && !domain.isBlank() && !"localhost".equals(domain)) {
            cookie.append("; Domain=").append(domain);
        }

        cookie.append("; SameSite=").append(capitalize(sameSite));

        return cookie.toString();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase(java.util.Locale.ROOT)
            + str.substring(1).toLowerCase(java.util.Locale.ROOT);
    }
}
```

### 3.7 MdcLoggingFilter (요청 추적)

```java
package com.company.adapter.in.rest.auth.filter;

import com.company.adapter.in.rest.auth.component.MdcContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * MDC Logging Filter
 *
 * <p>요청 추적을 위한 MDC(Mapped Diagnostic Context) 설정 필터
 *
 * <p>Gateway에서 전달된 X-Request-Id를 MDC에 설정하고, 없으면 새로 생성
 *
 * <p>설정되는 MDC 키:
 * <ul>
 *   <li>requestId: 요청 추적 ID (Gateway에서 전달 또는 자체 생성)</li>
 *   <li>memberId: 인증된 사용자 ID (JwtAuthenticationFilter에서 설정)</li>
 * </ul>
 *
 * <p>필터 순서: 가장 먼저 실행되어야 함 (Ordered.HIGHEST_PRECEDENCE)
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcLoggingFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String MDC_REQUEST_ID = "requestId";

    private final MdcContextHolder mdcContextHolder;

    public MdcLoggingFilter(MdcContextHolder mdcContextHolder) {
        this.mdcContextHolder = mdcContextHolder;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // Gateway에서 전달된 Request ID 사용, 없으면 생성
            String requestId = request.getHeader(REQUEST_ID_HEADER);
            if (requestId == null || requestId.isBlank()) {
                requestId = generateRequestId();
            }

            // MDC에 requestId 설정
            MDC.put(MDC_REQUEST_ID, requestId);

            // Response Header에도 추가 (클라이언트 디버깅용)
            response.setHeader(REQUEST_ID_HEADER, requestId);

            filterChain.doFilter(request, response);
        } finally {
            // 요청 완료 후 MDC 정리
            MDC.remove(MDC_REQUEST_ID);
            mdcContextHolder.clearMemberId();
        }
    }

    /**
     * Request ID 생성 (UUID 앞 8자리)
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
```

---

## 4️⃣ Method Security (리소스 소유자 검증)

### @PreAuthorize 사용

```java
package com.company.adapter.in.rest.order.controller;

import com.company.adapter.in.rest.auth.paths.ApiPaths;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Order Command Controller
 */
@RestController
@RequestMapping(ApiPaths.Orders.BASE)
public class OrderCommandController {

    /**
     * 주문 취소 - 리소스 소유자만 가능
     */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("@orderSecurityChecker.isOwner(#id, authentication.principal.memberId)")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(@PathVariable Long id) {
        // ...
    }

    /**
     * 관리자 전용 - 강제 취소
     */
    @PatchMapping("/{id}/force-cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> forceCancelOrder(@PathVariable Long id) {
        // ...
    }
}
```

### Security Checker Bean

```java
package com.company.adapter.in.rest.auth.component;

import com.company.application.order.port.in.query.OrderQueryUseCase;
import org.springframework.stereotype.Component;

/**
 * Order 리소스 소유자 검증
 */
@Component("orderSecurityChecker")
public class OrderSecurityChecker {

    private final OrderQueryUseCase orderQueryUseCase;

    public OrderSecurityChecker(OrderQueryUseCase orderQueryUseCase) {
        this.orderQueryUseCase = orderQueryUseCase;
    }

    /**
     * 주문 소유자 검증
     */
    public boolean isOwner(Long orderId, String memberId) {
        return orderQueryUseCase.isOrderOwner(orderId, Long.parseLong(memberId));
    }
}
```

---

## 5️⃣ 환경별 설정

### application.yml (공통)

```yaml
security:
  jwt:
    secret: ${JWT_SECRET:must-be-changed-in-production}
    access-token-expiration: 3600      # 1 hour
    refresh-token-expiration: 604800   # 7 days

  cookie:
    domain: ${COOKIE_DOMAIN:localhost}
    secure: ${COOKIE_SECURE:false}
    same-site: lax

  cors:
    allowed-origins:
      - http://localhost:3000
      - http://localhost:8080
    allowed-methods:
      - GET
      - POST
      - PUT
      - PATCH
      - DELETE
      - OPTIONS
    allowed-headers:
      - "*"
    exposed-headers:
      - Authorization
      - Set-Cookie
      - X-Request-Id
    allow-credentials: true
```

### application-prod.yml (운영)

```yaml
security:
  cookie:
    secure: true   # HTTPS 전용
    domain: example.com
```

---

## 6️⃣ Do / Don't

### ✅ Good Patterns

```java
// ✅ 1. Constants로 경로 관리
@RequestMapping(ApiPaths.Orders.BASE)

// ✅ 2. SecurityPaths로 정책 그룹화
.requestMatchers(SecurityPaths.PUBLIC_ENDPOINTS).permitAll()

// ✅ 3. Method Security로 리소스 소유자 검증
@PreAuthorize("@orderSecurityChecker.isOwner(#id, authentication.principal.memberId)")

// ✅ 4. RFC 7807 ProblemDetail 사용
ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, detail)

// ✅ 5. HttpOnly 쿠키로 토큰 저장
cookie.append("; HttpOnly");
cookie.append("; SameSite=Lax");
```

### ❌ Anti-Patterns

```java
// ❌ 1. 하드코딩 경로
@RequestMapping("/api/v1/orders")

// ❌ 2. YAML에만 경로 정의 (SpEL 방식)
@RequestMapping("${api.endpoints.order.base}")

// ❌ 3. Controller에서 직접 인가 로직
if (!order.getMemberId().equals(currentUser.getId())) {
    throw new AccessDeniedException("...");
}

// ❌ 4. 일반 JSON 에러 응답 (RFC 7807 미준수)
response.getWriter().write("{\"error\": \"Unauthorized\"}");

// ❌ 5. Authorization 헤더로만 토큰 전달 (XSS 취약)
localStorage.setItem("token", accessToken);
```

---

## 7️⃣ 체크리스트

- [ ] `ApiPaths` 클래스에 모든 API 경로 상수 정의
- [ ] `SecurityPaths` 클래스에 보안 정책별 그룹화
- [ ] `SecurityConfig`에서 Constants 참조 (하드코딩 금지)
- [ ] JWT 토큰을 HttpOnly 쿠키로 저장
- [ ] Silent Refresh 메커니즘 구현
- [ ] 인증/인가 에러에 RFC 7807 ProblemDetail 사용
- [ ] MDC에 requestId, memberId 설정
- [ ] Method Security로 리소스 소유자 검증
- [ ] CORS 설정 (allowed-origins 명시)
- [ ] 환경별 설정 분리 (secure, domain)

---

## 8️⃣ 관련 가이드

- **[API Paths Guide](./api-paths-guide.md)** - Constants 방식 경로 관리 상세
- **[Security ArchUnit](./security-archunit.md)** - ArchUnit 테스트
- **[Security Test Guide](./security-test-guide.md)** - 테스트 가이드
- **[Error Guide](../error/error-guide.md)** - RFC 7807 에러 처리

---

**작성자**: Development Team
**최종 수정일**: 2025-11-13
**버전**: 1.0.0
