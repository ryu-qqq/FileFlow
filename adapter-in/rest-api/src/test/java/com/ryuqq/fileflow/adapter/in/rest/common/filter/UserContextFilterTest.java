package com.ryuqq.fileflow.adapter.in.rest.common.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.application.common.context.UserContextHolder;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.iam.vo.UserId;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.MDC;

/**
 * UserContextFilter 단위 테스트
 *
 * <p>Gateway 헤더 기반 파싱 및 JWT fallback을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UserContextFilter 테스트")
class UserContextFilterTest {

    private UserContextFilter filter;
    private ObjectMapper objectMapper;

    @Mock private HttpServletRequest request;

    @Mock private HttpServletResponse response;

    @Mock private FilterChain filterChain;

    // 테스트용 UUIDv7 값 (실제 UUIDv7 형식)
    private static final String TEST_TENANT_ID = TenantId.generate().value();
    private static final String TEST_ORG_ID = OrganizationId.generate().value();
    private static final String TEST_USER_ID = UserId.generate().value();

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        filter = new UserContextFilter(objectMapper);
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
        MDC.clear();
    }

    @Nested
    @DisplayName("Gateway 헤더 기반 파싱")
    class GatewayHeaderParsing {

        @Test
        @DisplayName("유효한 Gateway 헤더로 Admin UserContext 생성")
        void shouldCreateAdminContextFromGatewayHeaders() throws Exception {
            // given
            when(request.getHeader("X-Tenant-Id")).thenReturn(TEST_TENANT_ID);
            when(request.getHeader("X-Organization-Id")).thenReturn(null);
            when(request.getHeader("X-User-Id")).thenReturn(null);
            when(request.getHeader("X-User-Roles")).thenReturn("ADMIN");
            when(request.getHeader("X-Permissions")).thenReturn("read,write,delete");
            when(request.getHeader("Authorization"))
                    .thenReturn("Bearer " + createJwtTokenWithEmail("admin@test.com"));

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("유효한 Gateway 헤더로 Seller UserContext 생성")
        void shouldCreateSellerContextFromGatewayHeaders() throws Exception {
            // given
            when(request.getHeader("X-Tenant-Id")).thenReturn(TEST_TENANT_ID);
            when(request.getHeader("X-Organization-Id")).thenReturn(TEST_ORG_ID);
            when(request.getHeader("X-User-Id")).thenReturn(null);
            when(request.getHeader("X-User-Roles")).thenReturn("SELLER");
            when(request.getHeader("X-Permissions")).thenReturn("read,write");
            when(request.getHeader("Authorization"))
                    .thenReturn(
                            "Bearer "
                                    + createJwtTokenWithOrgName("Seller Shop", "seller@shop.com"));

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("유효한 Gateway 헤더로 Customer UserContext 생성")
        void shouldCreateCustomerContextFromGatewayHeaders() throws Exception {
            // given
            when(request.getHeader("X-Tenant-Id")).thenReturn(TEST_TENANT_ID);
            when(request.getHeader("X-Organization-Id")).thenReturn(null);
            when(request.getHeader("X-User-Id")).thenReturn(TEST_USER_ID);
            when(request.getHeader("X-User-Roles")).thenReturn("DEFAULT");
            when(request.getHeader("X-Permissions")).thenReturn("");
            when(request.getHeader("Authorization")).thenReturn(null);

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("SUPER_ADMIN 역할 파싱")
        void shouldParseSuperAdminRole() throws Exception {
            // given
            when(request.getHeader("X-Tenant-Id")).thenReturn(TEST_TENANT_ID);
            when(request.getHeader("X-Organization-Id")).thenReturn(null);
            when(request.getHeader("X-User-Id")).thenReturn(null);
            when(request.getHeader("X-User-Roles")).thenReturn("SUPER_ADMIN,ADMIN");
            when(request.getHeader("X-Permissions")).thenReturn("*");
            when(request.getHeader("Authorization"))
                    .thenReturn("Bearer " + createJwtTokenWithEmail("super@test.com"));

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("빈 X-User-Roles 헤더는 빈 역할 목록으로 처리")
        void shouldHandleEmptyRolesHeader() throws Exception {
            // given
            when(request.getHeader("X-Tenant-Id")).thenReturn(TEST_TENANT_ID);
            when(request.getHeader("X-Organization-Id")).thenReturn(null);
            when(request.getHeader("X-User-Id")).thenReturn(TEST_USER_ID);
            when(request.getHeader("X-User-Roles")).thenReturn("");
            when(request.getHeader("X-Permissions")).thenReturn(null);
            when(request.getHeader("Authorization")).thenReturn(null);

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("여러 역할이 콤마 구분으로 전달되면 모두 파싱된다")
        void shouldParseMultipleCommaSeparatedRoles() throws Exception {
            // given - SUPER_ADMIN/ADMIN은 userId가 null이어야 하고 email이 필수
            when(request.getHeader("X-Tenant-Id")).thenReturn(TEST_TENANT_ID);
            when(request.getHeader("X-Organization-Id")).thenReturn(null);
            when(request.getHeader("X-User-Id")).thenReturn(null);
            when(request.getHeader("X-User-Roles")).thenReturn("SUPER_ADMIN,ADMIN");
            when(request.getHeader("X-Permissions")).thenReturn(null);
            when(request.getHeader("Authorization"))
                    .thenReturn("Bearer " + createJwtTokenWithEmail("admin@test.com"));

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("개발 모드 (헤더/토큰 없음)")
    class DevelopmentMode {

        @Test
        @DisplayName("X-Tenant-Id 헤더가 없으면 개발 모드로 기본 Admin 생성")
        void shouldCreateDefaultAdminWhenNoTenantIdHeader() throws Exception {
            // given
            when(request.getHeader("X-Tenant-Id")).thenReturn(null);
            when(request.getHeader("Authorization")).thenReturn(null);

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("빈 X-Tenant-Id 헤더는 개발 모드로 처리")
        void shouldTreatEmptyTenantIdAsDevelopmentMode() throws Exception {
            // given
            when(request.getHeader("X-Tenant-Id")).thenReturn("");
            when(request.getHeader("Authorization")).thenReturn(null);

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("JWT Fallback 파싱")
    class JwtFallbackParsing {

        @Test
        @DisplayName("X-Tenant-Id 없고 JWT만 있으면 JWT에서 파싱")
        void shouldFallbackToJwtWhenNoGatewayHeaders() throws Exception {
            // given
            when(request.getHeader("X-Tenant-Id")).thenReturn(null);

            Map<String, Object> payload =
                    Map.of(
                            "tid",
                            TEST_TENANT_ID,
                            "tenant_name",
                            "Connectly",
                            "oid",
                            TEST_ORG_ID,
                            "org_name",
                            "Seller Shop",
                            "email",
                            "seller@shop.com",
                            "roles",
                            List.of("SELLER"),
                            "permissions",
                            List.of("read", "write"));
            String token = createJwtToken(payload);

            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("JWT에서 Customer 파싱 (sub 필드 사용)")
        void shouldParseCustomerFromJwt() throws Exception {
            // given
            when(request.getHeader("X-Tenant-Id")).thenReturn(null);

            Map<String, Object> payload =
                    Map.of(
                            "sub",
                            TEST_USER_ID,
                            "tid",
                            TEST_TENANT_ID,
                            "tenant_name",
                            "Connectly",
                            "roles",
                            List.of("DEFAULT"),
                            "permissions",
                            List.of());
            String token = createJwtToken(payload);

            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("잘못된 요청 처리")
    class InvalidRequestHandling {

        @Test
        @DisplayName("잘못된 형식의 JWT 토큰 - 400 응답")
        void shouldReturn400ForMalformedToken() throws Exception {
            // given
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);

            when(request.getHeader("X-Tenant-Id")).thenReturn(null);
            when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token");
            when(request.getRequestURI()).thenReturn("/api/v1/test");
            when(request.getQueryString()).thenReturn(null);
            when(response.getWriter()).thenReturn(printWriter);

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            verify(response).setStatus(400);
            verify(filterChain, never()).doFilter(any(), any());
        }

        @Test
        @DisplayName("JWT에 tid(tenantId) 누락 시 개발 모드로 처리")
        void shouldUseDevelopmentModeWhenJwtMissingTenantId() throws Exception {
            // given
            when(request.getHeader("X-Tenant-Id")).thenReturn(null);

            Map<String, Object> payload =
                    Map.of(
                            "oid",
                            TEST_ORG_ID,
                            "roles",
                            List.of("SELLER"),
                            "email",
                            "seller@shop.com");
            String token = createJwtToken(payload);

            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            // JWT 파싱 실패 시 개발 모드로 fallback
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("MDC 설정")
    class MdcSettings {

        @Test
        @DisplayName("필터 완료 후 MDC 정리")
        void shouldClearMdcAfterFilter() throws Exception {
            // given
            when(request.getHeader("X-Tenant-Id")).thenReturn(null);
            when(request.getHeader("Authorization")).thenReturn(null);

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            assertThat(MDC.get("userId")).isNull();
            assertThat(MDC.get("organizationId")).isNull();
            assertThat(MDC.get("role")).isNull();
        }

        @Test
        @DisplayName("예외 발생 시에도 MDC 정리")
        void shouldClearMdcEvenOnException() throws Exception {
            // given
            when(request.getHeader("X-Tenant-Id")).thenReturn(null);
            when(request.getHeader("Authorization")).thenReturn(null);
            org.mockito.Mockito.doThrow(new RuntimeException("Test exception"))
                    .when(filterChain)
                    .doFilter(request, response);

            // when & then
            try {
                filter.doFilterInternal(request, response, filterChain);
            } catch (RuntimeException ignored) {
                // expected
            }

            assertThat(MDC.get("userId")).isNull();
            assertThat(MDC.get("organizationId")).isNull();
            assertThat(MDC.get("role")).isNull();
        }
    }

    @Nested
    @DisplayName("UserContextHolder 정리")
    class UserContextHolderCleanup {

        @Test
        @DisplayName("필터 완료 후 UserContextHolder 정리")
        void shouldClearUserContextHolderAfterFilter() throws Exception {
            // given
            when(request.getHeader("X-Tenant-Id")).thenReturn(null);
            when(request.getHeader("Authorization")).thenReturn(null);

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            assertThat(UserContextHolder.get()).isNull();
        }
    }

    /** 테스트용 JWT 토큰 생성 (전체 페이로드) */
    private String createJwtToken(Map<String, Object> payload) throws Exception {
        String header =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(
                                "{\"alg\":\"HS256\",\"typ\":\"JWT\"}"
                                        .getBytes(StandardCharsets.UTF_8));
        String payloadJson = objectMapper.writeValueAsString(payload);
        String encodedPayload =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signature =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString("test-signature".getBytes(StandardCharsets.UTF_8));

        return header + "." + encodedPayload + "." + signature;
    }

    /** 이메일만 포함하는 간단한 JWT 토큰 생성 */
    private String createJwtTokenWithEmail(String email) throws Exception {
        Map<String, Object> payload = Map.of("email", email);
        return createJwtToken(payload);
    }

    /** 조직명과 이메일을 포함하는 JWT 토큰 생성 */
    private String createJwtTokenWithOrgName(String orgName, String email) throws Exception {
        Map<String, Object> payload = Map.of("org_name", orgName, "email", email);
        return createJwtToken(payload);
    }
}
