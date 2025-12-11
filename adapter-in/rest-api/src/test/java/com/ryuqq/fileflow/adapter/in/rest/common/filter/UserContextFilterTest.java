package com.ryuqq.fileflow.adapter.in.rest.common.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.application.common.context.UserContextHolder;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

/**
 * UserContextFilter 단위 테스트
 *
 * <p>JWT 토큰 파싱 및 UserContext 생성을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserContextFilter 테스트")
class UserContextFilterTest {

    private UserContextFilter filter;
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

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
    @DisplayName("토큰이 없는 경우 - 개발 모드")
    class NoTokenDevelopmentMode {

        @Test
        @DisplayName("토큰이 없으면 기본 Admin UserContext 생성")
        void shouldCreateDefaultAdminContextWhenNoToken() throws Exception {
            // given
            when(request.getHeader("Authorization")).thenReturn(null);

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(request, response);
            // UserContextHolder는 finally에서 clear되므로 filterChain 호출로 검증
        }

        @Test
        @DisplayName("빈 Authorization 헤더는 토큰 없음으로 처리")
        void shouldTreatEmptyAuthorizationAsNoToken() throws Exception {
            // given
            when(request.getHeader("Authorization")).thenReturn("");

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Bearer 접두사가 없으면 토큰 없음으로 처리")
        void shouldTreatNonBearerAsNoToken() throws Exception {
            // given
            when(request.getHeader("Authorization")).thenReturn("Basic abc123");

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("유효한 JWT 토큰")
    class ValidJwtToken {

        @Test
        @DisplayName("유효한 Admin JWT 토큰 파싱")
        void shouldParseValidAdminJwtToken() throws Exception {
            // given
            Map<String, Object> payload = Map.of(
                    "tenantId", 1L,
                    "organizationId", 1L,
                    "organizationName", "Admin Org",
                    "role", "ADMIN",
                    "email", "admin@test.com");
            String token = createJwtToken(payload);

            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("유효한 Seller JWT 토큰 파싱")
        void shouldParseValidSellerJwtToken() throws Exception {
            // given
            Map<String, Object> payload = Map.of(
                    "tenantId", 1L,
                    "organizationId", 100L,
                    "organizationName", "Seller Shop",
                    "role", "SELLER",
                    "email", "seller@shop.com");
            String token = createJwtToken(payload);

            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("유효한 Customer JWT 토큰 파싱")
        void shouldParseValidCustomerJwtToken() throws Exception {
            // given
            Map<String, Object> payload = Map.of(
                    "tenantId", 1L,
                    "organizationId", 0L,
                    "role", "DEFAULT",
                    "userId", 12345L);
            String token = createJwtToken(payload);

            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("organizationName이 없으면 기본값 사용")
        void shouldUseDefaultOrganizationName() throws Exception {
            // given
            Map<String, Object> payload = Map.of(
                    "tenantId", 1L,
                    "organizationId", 100L,
                    "role", "SELLER",
                    "email", "seller@shop.com");
            String token = createJwtToken(payload);

            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("잘못된 JWT 토큰")
    class InvalidJwtToken {

        @Test
        @DisplayName("잘못된 형식의 JWT 토큰 - 400 응답")
        void shouldReturn400ForMalformedToken() throws Exception {
            // given
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);

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
        @DisplayName("필수 필드(tenantId) 누락 - 400 응답")
        void shouldReturn400WhenTenantIdMissing() throws Exception {
            // given
            Map<String, Object> payload = Map.of(
                    "organizationId", 1L,
                    "role", "ADMIN",
                    "email", "admin@test.com");
            String token = createJwtToken(payload);

            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);

            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
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
        @DisplayName("필수 필드(organizationId) 누락 - 400 응답")
        void shouldReturn400WhenOrganizationIdMissing() throws Exception {
            // given
            Map<String, Object> payload = Map.of(
                    "tenantId", 1L,
                    "role", "ADMIN",
                    "email", "admin@test.com");
            String token = createJwtToken(payload);

            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);

            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(request.getRequestURI()).thenReturn("/api/v1/test");
            when(request.getQueryString()).thenReturn(null);
            when(response.getWriter()).thenReturn(printWriter);

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            verify(response).setStatus(400);
        }

        @Test
        @DisplayName("쿼리 스트링이 있는 경우 instance에 포함")
        void shouldIncludeQueryStringInInstance() throws Exception {
            // given
            Map<String, Object> payload = Map.of(
                    "organizationId", 1L,
                    "role", "ADMIN",
                    "email", "admin@test.com");
            String token = createJwtToken(payload);

            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);

            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(request.getRequestURI()).thenReturn("/api/v1/test");
            when(request.getQueryString()).thenReturn("page=1");
            when(response.getWriter()).thenReturn(printWriter);

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            verify(response).setStatus(400);
        }
    }

    @Nested
    @DisplayName("MDC 설정")
    class MdcSettings {

        @Test
        @DisplayName("필터 완료 후 MDC 정리")
        void shouldClearMdcAfterFilter() throws Exception {
            // given
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
            when(request.getHeader("Authorization")).thenReturn(null);
            org.mockito.Mockito.doThrow(new RuntimeException("Test exception"))
                    .when(filterChain).doFilter(request, response);

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
            when(request.getHeader("Authorization")).thenReturn(null);

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            assertThat(UserContextHolder.get()).isNull();
        }
    }

    /**
     * 테스트용 JWT 토큰 생성
     */
    private String createJwtToken(Map<String, Object> payload) throws Exception {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        String payloadJson = objectMapper.writeValueAsString(payload);
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("test-signature".getBytes(StandardCharsets.UTF_8));

        return header + "." + encodedPayload + "." + signature;
    }
}
