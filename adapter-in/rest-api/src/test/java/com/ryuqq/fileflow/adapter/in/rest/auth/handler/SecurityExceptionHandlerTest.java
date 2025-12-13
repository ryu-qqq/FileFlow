package com.ryuqq.fileflow.adapter.in.rest.auth.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

/**
 * SecurityExceptionHandler 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("SecurityExceptionHandler")
class SecurityExceptionHandlerTest {

    private SecurityExceptionHandler handler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        handler = new SecurityExceptionHandler(objectMapper);
    }

    @Nested
    @DisplayName("commence() - AuthenticationException 처리")
    class CommenceTest {

        @Test
        @DisplayName("401 Unauthorized 응답 반환")
        void returns401Unauthorized() throws Exception {
            // given
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getRequestURI()).thenReturn("/api/v1/file/file-assets");
            when(request.getQueryString()).thenReturn(null);

            MockHttpServletResponse response = new MockHttpServletResponse();

            AuthenticationException authException = new AuthenticationException("Invalid token") {};

            // when
            handler.commence(request, response, authException);

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
            assertThat(response.getContentType())
                    .startsWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

            String content = response.getContentAsString();
            assertThat(content).contains("\"status\":401");
            assertThat(content).contains("\"title\":\"Unauthorized\"");
            assertThat(content).contains("\"code\":\"UNAUTHORIZED\"");
        }

        @Test
        @DisplayName("쿼리 스트링이 있으면 instance에 포함")
        void includesQueryStringInInstance() throws Exception {
            // given
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getRequestURI()).thenReturn("/api/v1/file/file-assets");
            when(request.getQueryString()).thenReturn("status=COMPLETED");

            MockHttpServletResponse response = new MockHttpServletResponse();

            AuthenticationException authException = new AuthenticationException("Invalid token") {};

            // when
            handler.commence(request, response, authException);

            // then
            String content = response.getContentAsString();
            assertThat(content).contains("/api/v1/file/file-assets?status=COMPLETED");
        }

        @Test
        @DisplayName("MDC에 traceId가 있으면 응답에 포함")
        void includesTraceIdFromMDC() throws Exception {
            // given
            MDC.put("traceId", "test-trace-id");
            MDC.put("requestId", "test-request-id");

            try {
                HttpServletRequest request = mock(HttpServletRequest.class);
                when(request.getRequestURI()).thenReturn("/api/v1/file/file-assets");
                when(request.getQueryString()).thenReturn(null);

                MockHttpServletResponse response = new MockHttpServletResponse();

                AuthenticationException authException =
                        new AuthenticationException("Invalid token") {};

                // when
                handler.commence(request, response, authException);

                // then
                String content = response.getContentAsString();
                assertThat(content).contains("\"traceId\":\"test-trace-id\"");
                assertThat(content).contains("\"requestId\":\"test-request-id\"");
            } finally {
                MDC.clear();
            }
        }
    }

    @Nested
    @DisplayName("handle() - AccessDeniedException 처리")
    class HandleTest {

        @Test
        @DisplayName("403 Forbidden 응답 반환")
        void returns403Forbidden() throws Exception {
            // given
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getRequestURI()).thenReturn("/api/v1/file/file-assets/123/delete");
            when(request.getQueryString()).thenReturn(null);

            MockHttpServletResponse response = new MockHttpServletResponse();

            AccessDeniedException accessDeniedException =
                    new AccessDeniedException("Access denied");

            // when
            handler.handle(request, response, accessDeniedException);

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
            assertThat(response.getContentType())
                    .startsWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

            String content = response.getContentAsString();
            assertThat(content).contains("\"status\":403");
            assertThat(content).contains("\"title\":\"Forbidden\"");
            assertThat(content).contains("\"code\":\"ACCESS_DENIED\"");
        }

        @Test
        @DisplayName("응답에 timestamp가 포함됨")
        void includesTimestamp() throws Exception {
            // given
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getRequestURI()).thenReturn("/api/v1/file/upload-sessions");
            when(request.getQueryString()).thenReturn(null);

            MockHttpServletResponse response = new MockHttpServletResponse();

            AccessDeniedException accessDeniedException =
                    new AccessDeniedException("Access denied");

            // when
            handler.handle(request, response, accessDeniedException);

            // then
            String content = response.getContentAsString();
            assertThat(content).contains("\"timestamp\":");
        }

        @Test
        @DisplayName("한국어 에러 메시지가 포함됨")
        void includesKoreanDetailMessage() throws Exception {
            // given
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getRequestURI()).thenReturn("/api/v1/file/external-downloads");
            when(request.getQueryString()).thenReturn(null);

            MockHttpServletResponse response = new MockHttpServletResponse();

            AccessDeniedException accessDeniedException =
                    new AccessDeniedException("Access denied");

            // when
            handler.handle(request, response, accessDeniedException);

            // then
            String content = response.getContentAsString();
            assertThat(content).contains("이 리소스에 대한 접근 권한이 없습니다");
        }
    }

    @Nested
    @DisplayName("RFC 7807 형식 검증")
    class Rfc7807FormatTest {

        @Test
        @DisplayName("type, title, status, detail, instance 필드 포함")
        void includesRequiredFields() throws Exception {
            // given
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getRequestURI()).thenReturn("/api/v1/test");
            when(request.getQueryString()).thenReturn(null);

            MockHttpServletResponse response = new MockHttpServletResponse();

            AuthenticationException authException = new AuthenticationException("Test error") {};

            // when
            handler.commence(request, response, authException);

            // then
            String content = response.getContentAsString();
            assertThat(content).contains("\"type\":");
            assertThat(content).contains("\"title\":");
            assertThat(content).contains("\"status\":");
            assertThat(content).contains("\"detail\":");
            assertThat(content).contains("\"instance\":");
        }

        @Test
        @DisplayName("Content-Type이 application/problem+json임")
        void contentTypeIsProblemJson() throws Exception {
            // given
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getRequestURI()).thenReturn("/api/v1/test");
            when(request.getQueryString()).thenReturn(null);

            MockHttpServletResponse response = new MockHttpServletResponse();

            AccessDeniedException accessDeniedException =
                    new AccessDeniedException("Access denied");

            // when
            handler.handle(request, response, accessDeniedException);

            // then
            assertThat(response.getContentType())
                    .startsWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");
        }
    }
}
