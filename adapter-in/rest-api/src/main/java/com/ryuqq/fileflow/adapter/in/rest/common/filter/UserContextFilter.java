package com.ryuqq.fileflow.adapter.in.rest.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.application.common.context.UserContextHolder;
import com.ryuqq.fileflow.domain.iam.vo.Organization;
import com.ryuqq.fileflow.domain.iam.vo.Tenant;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import com.ryuqq.fileflow.domain.iam.vo.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT 토큰을 파싱하여 UserContext를 생성하는 Filter.
 *
 * <p>Authorization 헤더에서 JWT 토큰을 추출하고 파싱하여 UserContext를 생성한 후 ThreadLocal과 MDC에 설정합니다.
 *
 * <p><strong>JWT Payload 필드</strong>:
 *
 * <ul>
 *   <li>tenantId: 테넌트 ID
 *   <li>organizationId: 조직 ID
 *   <li>organizationName: 조직명 (선택)
 *   <li>role: 사용자 역할 (ADMIN, SELLER, DEFAULT)
 *   <li>email: 이메일 (Admin/Seller)
 *   <li>userId: 사용자 ID (Customer)
 * </ul>
 *
 * <p><strong>MDC 설정 항목</strong>:
 *
 * <ul>
 *   <li>userId: 사용자 식별자
 *   <li>organizationId: 조직 ID
 *   <li>role: 사용자 역할
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public class UserContextFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(UserContextFilter.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private static final String MDC_USER_ID = "userId";
    private static final String MDC_ORGANIZATION_ID = "organizationId";
    private static final String MDC_ROLE = "role";

    private final ObjectMapper objectMapper;

    public UserContextFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = extractToken(request);

            if (token == null) {
                sendErrorResponse(
                        request,
                        response,
                        HttpStatus.UNAUTHORIZED,
                        "MISSING_TOKEN",
                        "Authorization token is required");
                return;
            }

            try {
                UserContext userContext = parseToken(token);
                UserContextHolder.set(userContext);

                // MDC 설정
                MDC.put(MDC_USER_ID, userContext.getUserIdentifier());
                MDC.put(MDC_ORGANIZATION_ID, String.valueOf(userContext.getOrganizationId()));
                MDC.put(MDC_ROLE, userContext.getRole().name());

                log.debug("UserContext 설정 완료: {}", userContext.getUserIdentifier());

            } catch (IllegalArgumentException e) {
                log.warn("JWT 토큰 파싱 실패: {}", e.getMessage());
                sendErrorResponse(
                        request, response, HttpStatus.BAD_REQUEST, "INVALID_TOKEN", e.getMessage());
                return;
            }

            filterChain.doFilter(request, response);

        } finally {
            // ThreadLocal 및 MDC 정리
            UserContextHolder.clear();
            MDC.remove(MDC_USER_ID);
            MDC.remove(MDC_ORGANIZATION_ID);
            MDC.remove(MDC_ROLE);
        }
    }

    /**
     * RFC 7807 ProblemDetail 형식으로 에러 응답을 전송합니다.
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param status HTTP 상태
     * @param code 에러 코드
     * @param detail 에러 상세 메시지
     * @throws IOException 응답 작성 실패 시
     */
    private void sendErrorResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String code,
            String detail)
            throws IOException {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(status.getReasonPhrase());
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setProperty("code", code);
        problemDetail.setProperty("timestamp", Instant.now().toString());

        // 요청 경로를 instance로
        String uri = request.getRequestURI();
        if (request.getQueryString() != null && !request.getQueryString().isBlank()) {
            uri = uri + "?" + request.getQueryString();
        }
        problemDetail.setInstance(URI.create(uri));

        // tracing
        String traceId = MDC.get("traceId");
        String requestId = MDC.get("requestId");
        if (traceId != null) {
            problemDetail.setProperty("traceId", traceId);
        }
        if (requestId != null) {
            problemDetail.setProperty("requestId", requestId);
        }

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(response.getWriter(), problemDetail);
    }

    /**
     * Authorization 헤더에서 JWT 토큰을 추출합니다.
     *
     * @param request HTTP 요청
     * @return JWT 토큰 또는 null
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * JWT 토큰을 파싱하여 UserContext를 생성합니다.
     *
     * <p>JWT 서명 검증은 별도의 인증 서버에서 수행되었다고 가정합니다. 이 필터는 Payload만 파싱합니다.
     *
     * @param token JWT 토큰
     * @return UserContext
     * @throws IllegalArgumentException 토큰 파싱 실패 시
     */
    @SuppressWarnings("unchecked")
    private UserContext parseToken(String token) {
        try {
            // JWT는 header.payload.signature 형식
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                throw new IllegalArgumentException("잘못된 JWT 토큰 형식입니다.");
            }

            // Payload (Base64 URL 디코딩)
            String payload =
                    new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

            Map<String, Object> claims = objectMapper.readValue(payload, Map.class);

            return createUserContext(claims);

        } catch (Exception e) {
            log.error("JWT 토큰 파싱 실패: {}", e.getMessage());
            throw new IllegalArgumentException("JWT 토큰 파싱에 실패했습니다.", e);
        }
    }

    /**
     * JWT Claims에서 UserContext를 생성합니다.
     *
     * @param claims JWT Payload claims
     * @return UserContext
     */
    private UserContext createUserContext(Map<String, Object> claims) {
        // 필수 필드 추출
        Long tenantId = extractLong(claims, "tenantId");
        Long organizationId = extractLong(claims, "organizationId");
        String organizationName =
                extractString(claims, "organizationName", "Organization-" + organizationId);
        String roleStr = extractString(claims, "role", "DEFAULT");
        UserRole role = UserRole.valueOf(roleStr.toUpperCase());

        // Tenant 생성 (현재는 Connectly 고정, 추후 확장 가능)
        Tenant tenant = Tenant.connectly();

        // Organization 생성
        Organization organization = createOrganization(organizationId, organizationName, role);

        // Role에 따른 UserContext 생성
        String email = extractString(claims, "email", null);
        Long userId = extractLongOrNull(claims, "userId");

        return UserContext.of(tenant, organization, email, userId);
    }

    /**
     * Role에 따른 Organization을 생성합니다.
     *
     * @param organizationId 조직 ID
     * @param organizationName 조직명
     * @param role 사용자 역할
     * @return Organization
     */
    private Organization createOrganization(
            Long organizationId, String organizationName, UserRole role) {
        return switch (role) {
            case ADMIN -> Organization.admin();
            case SELLER -> Organization.seller(organizationId, organizationName);
            case DEFAULT -> Organization.customer();
        };
    }

    /**
     * Claims에서 Long 값을 추출합니다.
     *
     * @param claims JWT claims
     * @param key 키
     * @return Long 값
     * @throws IllegalArgumentException 키가 없거나 변환 실패 시
     */
    private Long extractLong(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        if (value == null) {
            throw new IllegalArgumentException("JWT에 필수 필드가 없습니다: " + key);
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(value.toString());
    }

    /**
     * Claims에서 Long 값을 추출합니다 (nullable).
     *
     * @param claims JWT claims
     * @param key 키
     * @return Long 값 또는 null
     */
    private Long extractLongOrNull(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(value.toString());
    }

    /**
     * Claims에서 String 값을 추출합니다.
     *
     * @param claims JWT claims
     * @param key 키
     * @param defaultValue 기본값
     * @return String 값
     */
    private String extractString(Map<String, Object> claims, String key, String defaultValue) {
        Object value = claims.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value.toString();
    }
}
