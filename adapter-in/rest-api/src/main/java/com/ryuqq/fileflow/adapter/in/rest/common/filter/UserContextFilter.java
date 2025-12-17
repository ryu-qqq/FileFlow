package com.ryuqq.fileflow.adapter.in.rest.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.in.rest.auth.paths.SecurityPaths;
import com.ryuqq.fileflow.adapter.in.rest.config.properties.ServiceTokenProperties;
import com.ryuqq.fileflow.application.common.context.UserContextHolder;
import com.ryuqq.fileflow.domain.iam.vo.Organization;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.Tenant;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import com.ryuqq.fileflow.domain.iam.vo.UserId;
import com.ryuqq.fileflow.domain.iam.vo.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Gateway 헤더와 JWT 토큰을 파싱하여 UserContext를 생성하는 Filter.
 *
 * <p>Gateway에서 전달받은 헤더 정보를 우선 사용하고, JWT Payload에서 추가 정보(tenant_name, org_name, email)를 추출합니다.
 *
 * <p><strong>Gateway 헤더</strong>:
 *
 * <ul>
 *   <li>X-User-Id: 사용자 ID (UUIDv7)
 *   <li>X-Tenant-Id: 테넌트 ID (UUIDv7)
 *   <li>X-Organization-Id: 조직 ID (UUIDv7)
 *   <li>X-User-Roles: 역할 목록 (콤마 구분, 예: "SUPER_ADMIN,ADMIN")
 *   <li>X-User-Permissions: 권한 목록 (콤마 구분, 예: "file:read,file:write")
 * </ul>
 *
 * <p><strong>JWT Payload 필드</strong> (이름 정보 추출용):
 *
 * <ul>
 *   <li>tid: 테넌트 ID
 *   <li>tenant_name: 테넌트명
 *   <li>oid: 조직 ID
 *   <li>org_name: 조직명
 *   <li>email: 이메일
 * </ul>
 *
 * <p><strong>MDC 설정 항목</strong>:
 *
 * <ul>
 *   <li>userId: 사용자 식별자
 *   <li>tenantId: 테넌트 ID
 *   <li>organizationId: 조직 ID
 *   <li>roles: 역할 목록
 *   <li>serviceName: 호출 서비스 이름 (Service Token 인증 시에만 설정)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public class UserContextFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(UserContextFilter.class);

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    // Authorization Header
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    // Service Token Headers (서버 간 내부 통신용)
    private static final String HEADER_SERVICE_TOKEN = SecurityPaths.Headers.SERVICE_TOKEN;
    private static final String HEADER_SERVICE_NAME = SecurityPaths.Headers.SERVICE_NAME;

    // Gateway Headers
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_TENANT_ID = "X-Tenant-Id";
    private static final String HEADER_ORGANIZATION_ID = "X-Organization-Id";
    private static final String HEADER_ROLES = "X-User-Roles";
    private static final String HEADER_PERMISSIONS = "X-User-Permissions";

    // JWT Payload Claims
    private static final String CLAIM_USER_ID = "sub";
    private static final String CLAIM_TENANT_ID = "tid";
    private static final String CLAIM_TENANT_NAME = "tenant_name";
    private static final String CLAIM_ORGANIZATION_ID = "oid";
    private static final String CLAIM_ORGANIZATION_NAME = "org_name";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_PERMISSIONS = "permissions";

    // MDC Keys
    private static final String MDC_USER_ID = "userId";
    private static final String MDC_TENANT_ID = "tenantId";
    private static final String MDC_ORGANIZATION_ID = "organizationId";
    private static final String MDC_ROLES = "roles";
    private static final String MDC_SERVICE_NAME = "serviceName";

    private final ObjectMapper objectMapper;
    private final ServiceTokenProperties serviceTokenProperties;

    public UserContextFilter(
            ObjectMapper objectMapper, ServiceTokenProperties serviceTokenProperties) {
        this.objectMapper = objectMapper;
        this.serviceTokenProperties = serviceTokenProperties;
    }

    /**
     * Public 및 Docs 경로는 필터를 건너뜁니다.
     *
     * <p>Actuator, 헬스체크, 에러 페이지, API 문서 등 인증이 필요 없는 경로는 필터링하지 않습니다.
     *
     * @param request HTTP 요청
     * @return true면 필터 건너뜀
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Public 경로 (헬스체크, 에러 페이지)
        boolean isPublic =
                SecurityPaths.Public.PATTERNS.stream()
                        .anyMatch(pattern -> pathMatcher.match(pattern, path));
        // Docs 경로 (API 문서, Swagger)
        boolean isDocs =
                SecurityPaths.Docs.PATTERNS.stream()
                        .anyMatch(pattern -> pathMatcher.match(pattern, path));
        return isPublic || isDocs;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            UserContext userContext;

            // 1. Service Token 확인 (서버 간 내부 통신)
            String serviceToken = request.getHeader(HEADER_SERVICE_TOKEN);
            if (serviceTokenProperties.isValidToken(serviceToken)) {
                String serviceName = request.getHeader(HEADER_SERVICE_NAME);

                // Phase 2: X-Service-Name 필수 검증 (requireServiceName=true인 경우)
                if (serviceTokenProperties.isRequireServiceName()
                        && (serviceName == null || serviceName.isBlank())) {
                    log.warn("Service Token 인증 실패 - X-Service-Name 헤더가 필수입니다.");
                    sendErrorResponse(
                            request,
                            response,
                            HttpStatus.BAD_REQUEST,
                            "MISSING_SERVICE_NAME",
                            "X-Service-Name 헤더가 필수입니다.");
                    return;
                }

                // 화이트리스트 검증 (allowedServices가 설정된 경우)
                if (serviceName != null
                        && !serviceName.isBlank()
                        && !serviceTokenProperties.isAllowedService(serviceName)) {
                    log.warn("Service Token 인증 실패 - 허용되지 않은 서비스: {}", serviceName);
                    sendErrorResponse(
                            request,
                            response,
                            HttpStatus.FORBIDDEN,
                            "UNKNOWN_SERVICE",
                            "허용되지 않은 서비스입니다: " + serviceName);
                    return;
                }

                userContext = createSystemContext(serviceName);
                log.debug(
                        "Service Token 인증 성공 - SYSTEM UserContext 사용 (serviceName={})",
                        serviceName != null ? serviceName : "unknown");
            }
            // 2. Gateway 헤더 확인
            else {
                String headerTenantId = request.getHeader(HEADER_TENANT_ID);

                if (headerTenantId != null && !headerTenantId.isBlank()) {
                    // Gateway 경유: 헤더 기반 UserContext 생성
                    try {
                        userContext = createUserContextFromHeaders(request);
                    } catch (IllegalArgumentException e) {
                        log.warn("Gateway 헤더 파싱 실패: {}", e.getMessage());
                        sendErrorResponse(
                                request,
                                response,
                                HttpStatus.BAD_REQUEST,
                                "INVALID_HEADERS",
                                e.getMessage());
                        return;
                    }
                } else {
                    // Gateway 미경유: JWT 직접 파싱
                    String token = extractToken(request);
                    if (token == null) {
                        // 인증 정보 없음: 401 Unauthorized 응답
                        log.warn("인증 실패 - Gateway 헤더 또는 JWT 토큰이 필요합니다.");
                        sendErrorResponse(
                                request,
                                response,
                                HttpStatus.UNAUTHORIZED,
                                "MISSING_AUTHENTICATION",
                                "인증 정보가 필요합니다. Gateway 헤더 또는 JWT 토큰을 제공해주세요.");
                        return;
                    }
                    try {
                        userContext = parseTokenAndCreateUserContext(token);
                    } catch (IllegalArgumentException e) {
                        log.warn("JWT 토큰 파싱 실패: {}", e.getMessage());
                        sendErrorResponse(
                                request,
                                response,
                                HttpStatus.BAD_REQUEST,
                                "INVALID_TOKEN",
                                e.getMessage());
                        return;
                    }
                }
            }

            UserContextHolder.set(userContext);

            // Spring Security Context 동기화 (roles + permissions)
            synchronizeWithSpringSecurityContext(userContext);

            // MDC 설정
            setMdc(userContext);

            log.debug("UserContext 설정 완료: {}", userContext.getUserIdentifier());

            filterChain.doFilter(request, response);

        } finally {
            // ThreadLocal 및 MDC 정리
            UserContextHolder.clear();
            SecurityContextHolder.clearContext();
            clearMdc();
        }
    }

    /**
     * Gateway 헤더에서 UserContext를 생성합니다.
     *
     * @param request HTTP 요청
     * @return UserContext
     * @throws IllegalArgumentException 필수 헤더가 없거나 파싱 실패 시
     */
    @SuppressWarnings("unchecked")
    private UserContext createUserContextFromHeaders(HttpServletRequest request) {
        // 필수 헤더 추출
        String tenantIdStr = request.getHeader(HEADER_TENANT_ID);
        String organizationIdStr = request.getHeader(HEADER_ORGANIZATION_ID);
        String userIdStr = request.getHeader(HEADER_USER_ID);
        String rolesJson = request.getHeader(HEADER_ROLES);
        String permissionsStr = request.getHeader(HEADER_PERMISSIONS);

        if (tenantIdStr == null || tenantIdStr.isBlank()) {
            throw new IllegalArgumentException("X-Tenant-Id 헤더가 필수입니다.");
        }

        // JWT에서 추가 정보 추출 (tenant_name, org_name, email)
        String token = extractToken(request);
        Map<String, Object> claims =
                token != null ? parseJwtPayload(token) : Collections.emptyMap();

        String tenantName = extractString(claims, CLAIM_TENANT_NAME, "Unknown");
        String organizationName = extractString(claims, CLAIM_ORGANIZATION_NAME, "Unknown");
        String email = extractString(claims, CLAIM_EMAIL, null);

        // Roles 파싱 (JSON 배열)
        List<String> roles = parseRoles(rolesJson);

        // Permissions 파싱 (콤마 구분)
        List<String> permissions = parsePermissions(permissionsStr);

        // 가장 높은 우선순위 역할 결정
        UserRole primaryRole = UserRole.highestPriority(roles);

        // Tenant 생성
        TenantId tenantId = TenantId.of(tenantIdStr);
        Tenant tenant = Tenant.of(tenantId, tenantName);

        // Organization 생성
        OrganizationId organizationId =
                (organizationIdStr != null && !organizationIdStr.isBlank())
                        ? OrganizationId.of(organizationIdStr)
                        : null;
        Organization organization =
                createOrganization(organizationId, organizationName, primaryRole);

        // UserId 생성
        UserId userId = (userIdStr != null && !userIdStr.isBlank()) ? UserId.of(userIdStr) : null;

        return UserContext.of(tenant, organization, email, userId, roles, permissions);
    }

    /**
     * JWT 토큰을 파싱하여 UserContext를 생성합니다 (Gateway 미경유 시).
     *
     * @param token JWT 토큰
     * @return UserContext
     * @throws IllegalArgumentException 토큰 파싱 실패 시
     */
    @SuppressWarnings("unchecked")
    private UserContext parseTokenAndCreateUserContext(String token) {
        Map<String, Object> claims = parseJwtPayload(token);

        // 필수 필드 추출
        String tenantIdStr = extractString(claims, CLAIM_TENANT_ID, null);
        String tenantName = extractString(claims, CLAIM_TENANT_NAME, "Unknown");
        String organizationIdStr = extractString(claims, CLAIM_ORGANIZATION_ID, null);
        String organizationName = extractString(claims, CLAIM_ORGANIZATION_NAME, "Unknown");
        String userIdStr = extractString(claims, CLAIM_USER_ID, null);
        String email = extractString(claims, CLAIM_EMAIL, null);

        // Roles 추출
        List<String> roles = extractList(claims, CLAIM_ROLES);

        // Permissions 추출
        List<String> permissions = extractList(claims, CLAIM_PERMISSIONS);

        // 가장 높은 우선순위 역할 결정
        UserRole primaryRole = UserRole.highestPriority(roles);

        // Tenant 생성 (tenantId가 없으면 Connectly 기본값)
        Tenant tenant;
        if (tenantIdStr != null && !tenantIdStr.isBlank()) {
            tenant = Tenant.of(TenantId.of(tenantIdStr), tenantName);
        } else {
            tenant = Tenant.connectly();
        }

        // Organization 생성
        OrganizationId organizationId =
                (organizationIdStr != null && !organizationIdStr.isBlank())
                        ? OrganizationId.of(organizationIdStr)
                        : null;
        Organization organization =
                createOrganization(organizationId, organizationName, primaryRole);

        // UserId 생성
        UserId userId = (userIdStr != null && !userIdStr.isBlank()) ? UserId.of(userIdStr) : null;

        return UserContext.of(tenant, organization, email, userId, roles, permissions);
    }

    /**
     * JWT Payload를 파싱합니다.
     *
     * @param token JWT 토큰
     * @return Payload claims map
     * @throws IllegalArgumentException 파싱 실패 시
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJwtPayload(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                throw new IllegalArgumentException("잘못된 JWT 토큰 형식입니다.");
            }

            String payload =
                    new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

            return objectMapper.readValue(payload, Map.class);
        } catch (Exception e) {
            log.error("JWT 토큰 파싱 실패: {}", e.getMessage());
            throw new IllegalArgumentException("JWT 토큰 파싱에 실패했습니다.", e);
        }
    }

    /**
     * Role에 따른 Organization을 생성합니다.
     *
     * @param organizationId 조직 ID (nullable)
     * @param organizationName 조직명
     * @param role 사용자 역할
     * @return Organization
     */
    private Organization createOrganization(
            OrganizationId organizationId, String organizationName, UserRole role) {
        return switch (role) {
            case SYSTEM -> Organization.system();
            case SUPER_ADMIN, ADMIN -> Organization.admin();
            case SELLER -> {
                if (organizationId == null) {
                    throw new IllegalArgumentException("Seller는 OrganizationId가 필수입니다.");
                }
                yield Organization.seller(organizationId, organizationName);
            }
            case DEFAULT -> Organization.customer();
        };
    }

    /**
     * Roles 콤마 구분 문자열을 파싱합니다.
     *
     * <p>Gateway에서 콤마로 구분된 역할 문자열을 파싱합니다. ROLE_ prefix가 있으면 제거합니다 (UserRole enum은 prefix 없이 매칭).
     *
     * @param rolesStr 콤마 구분 문자열 (예: "SUPER_ADMIN,ADMIN" 또는 "ROLE_SUPER_ADMIN")
     * @return 역할 목록 (prefix 없이)
     */
    private List<String> parseRoles(String rolesStr) {
        if (rolesStr == null || rolesStr.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(rolesStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .toList();
    }

    /**
     * Permissions 콤마 구분 문자열을 파싱합니다.
     *
     * @param permissionsStr 콤마 구분 문자열 (예: "file:read,file:write")
     * @return 권한 목록
     */
    private List<String> parsePermissions(String permissionsStr) {
        if (permissionsStr == null || permissionsStr.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(permissionsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /**
     * Claims에서 List를 추출합니다.
     *
     * @param claims JWT claims
     * @param key 키
     * @return List (없으면 빈 리스트)
     */
    @SuppressWarnings("unchecked")
    private List<String> extractList(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        if (value instanceof List) {
            return ((List<?>) value)
                    .stream()
                            .filter(item -> item instanceof String)
                            .map(item -> (String) item)
                            .toList();
        }
        return Collections.emptyList();
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
     * System 내부 호출용 UserContext를 생성합니다.
     *
     * <p>Service Token 인증 성공 시 사용됩니다. 최상위 권한을 가지며 모든 리소스에 접근 가능합니다.
     *
     * @param serviceName 호출 서비스 이름 (nullable, X-Service-Name 헤더 값)
     * @return System UserContext
     */
    private UserContext createSystemContext(String serviceName) {
        if (serviceName != null && !serviceName.isBlank()) {
            return UserContext.system(serviceName);
        }
        return UserContext.system();
    }

    /**
     * MDC에 사용자 컨텍스트 정보를 설정합니다.
     *
     * @param userContext 사용자 컨텍스트
     */
    private void setMdc(UserContext userContext) {
        MDC.put(MDC_USER_ID, userContext.getUserIdentifier());
        MDC.put(MDC_TENANT_ID, userContext.getTenantId().value());

        OrganizationId orgId = userContext.getOrganizationId();
        MDC.put(MDC_ORGANIZATION_ID, orgId != null ? orgId.value() : "N/A");

        MDC.put(MDC_ROLES, String.join(",", userContext.roles()));

        // Service Name 추가 (서비스 호출 추적용)
        String serviceName = userContext.getServiceName();
        if (serviceName != null && !serviceName.isBlank()) {
            MDC.put(MDC_SERVICE_NAME, serviceName);
        }
    }

    /** MDC를 정리합니다. */
    private void clearMdc() {
        MDC.remove(MDC_USER_ID);
        MDC.remove(MDC_TENANT_ID);
        MDC.remove(MDC_ORGANIZATION_ID);
        MDC.remove(MDC_ROLES);
        MDC.remove(MDC_SERVICE_NAME);
    }

    /**
     * UserContext를 Spring Security Context와 동기화합니다.
     *
     * <p>Roles를 GrantedAuthority로 변환하여 Spring Security에 설정합니다. 이를 통해 @PreAuthorize 어노테이션이 작동합니다.
     *
     * <p>Authority 매핑:
     *
     * <ul>
     *   <li>Roles: "ROLE_" prefix 추가 (예: ADMIN → ROLE_ADMIN)
     *   <li>Permissions: 그대로 사용 (예: file:read)
     * </ul>
     *
     * @param userContext 사용자 컨텍스트
     */
    private void synchronizeWithSpringSecurityContext(UserContext userContext) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Roles를 ROLE_ prefix와 함께 GrantedAuthority로 변환
        userContext.roles().stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);

        // Permissions를 GrantedAuthority로 변환
        userContext.permissions().stream()
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);

        // Principal: userId 또는 email
        String principal = userContext.getUserIdentifier();

        // Authentication 생성 및 설정
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug(
                "Spring Security Context 설정 완료: principal={}, authorities={}",
                principal,
                authorities.size());
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

        String uri = request.getRequestURI();
        if (request.getQueryString() != null && !request.getQueryString().isBlank()) {
            uri = uri + "?" + request.getQueryString();
        }
        problemDetail.setInstance(URI.create(uri));

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
}
