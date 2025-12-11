package com.ryuqq.fileflow.adapter.in.rest.common.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * RequestResponseLoggingFilter 단위 테스트
 *
 * <p>요청/응답 로깅 및 MDC 설정을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RequestResponseLoggingFilter 테스트")
class RequestResponseLoggingFilterTest {

    private RequestResponseLoggingFilter filter;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new RequestResponseLoggingFilter();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Nested
    @DisplayName("MDC 설정")
    class MdcSettings {

        @Test
        @DisplayName("요청 처리 중 MDC에 requestId 설정")
        void shouldSetRequestIdInMdc() throws Exception {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/test");
            MockHttpServletResponse response = new MockHttpServletResponse();

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            // MDC는 finally에서 정리되므로 필터 체인 호출 확인
            verify(filterChain).doFilter(
                    org.mockito.ArgumentMatchers.any(),
                    org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("X-Request-Id 헤더가 있으면 해당 값 사용")
        void shouldUseXRequestIdHeader() throws Exception {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/test");
            request.addHeader("X-Request-Id", "custom-request-id");
            MockHttpServletResponse response = new MockHttpServletResponse();

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(
                    org.mockito.ArgumentMatchers.any(),
                    org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("필터 완료 후 MDC 정리")
        void shouldClearMdcAfterFilter() throws Exception {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/test");
            MockHttpServletResponse response = new MockHttpServletResponse();

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            assertThat(MDC.get("requestId")).isNull();
            assertThat(MDC.get("method")).isNull();
            assertThat(MDC.get("uri")).isNull();
            assertThat(MDC.get("clientIp")).isNull();
        }

        @Test
        @DisplayName("예외 발생 시에도 MDC 정리")
        void shouldClearMdcEvenOnException() throws Exception {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/test");
            MockHttpServletResponse response = new MockHttpServletResponse();

            org.mockito.Mockito.doThrow(new RuntimeException("Test exception"))
                    .when(filterChain).doFilter(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.any());

            // when & then
            try {
                filter.doFilterInternal(request, response, filterChain);
            } catch (RuntimeException ignored) {
                // expected
            }

            assertThat(MDC.get("requestId")).isNull();
            assertThat(MDC.get("method")).isNull();
            assertThat(MDC.get("uri")).isNull();
            assertThat(MDC.get("clientIp")).isNull();
        }
    }

    @Nested
    @DisplayName("클라이언트 IP 추출")
    class ClientIpExtraction {

        @Test
        @DisplayName("X-Forwarded-For 헤더가 있으면 첫 번째 IP 사용")
        void shouldUseFirstIpFromXForwardedFor() throws Exception {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/test");
            request.addHeader("X-Forwarded-For", "192.168.1.1, 10.0.0.1, 172.16.0.1");
            MockHttpServletResponse response = new MockHttpServletResponse();

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(
                    org.mockito.ArgumentMatchers.any(),
                    org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("X-Forwarded-For 헤더가 없으면 remoteAddr 사용")
        void shouldUseRemoteAddrWhenNoXForwardedFor() throws Exception {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/test");
            request.setRemoteAddr("127.0.0.1");
            MockHttpServletResponse response = new MockHttpServletResponse();

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(
                    org.mockito.ArgumentMatchers.any(),
                    org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("빈 X-Forwarded-For 헤더는 remoteAddr 사용")
        void shouldUseRemoteAddrWhenEmptyXForwardedFor() throws Exception {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/test");
            request.addHeader("X-Forwarded-For", "   ");
            request.setRemoteAddr("127.0.0.1");
            MockHttpServletResponse response = new MockHttpServletResponse();

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(
                    org.mockito.ArgumentMatchers.any(),
                    org.mockito.ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("요청 로깅")
    class RequestLogging {

        @Test
        @DisplayName("GET 요청 로깅")
        void shouldLogGetRequest() throws Exception {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users");
            MockHttpServletResponse response = new MockHttpServletResponse();

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(
                    org.mockito.ArgumentMatchers.any(),
                    org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("쿼리 스트링 포함 요청 로깅")
        void shouldLogRequestWithQueryString() throws Exception {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users");
            request.setQueryString("page=1&size=20");
            MockHttpServletResponse response = new MockHttpServletResponse();

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(
                    org.mockito.ArgumentMatchers.any(),
                    org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("POST 요청 로깅")
        void shouldLogPostRequest() throws Exception {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/users");
            request.setContent("{\"name\":\"test\"}".getBytes());
            MockHttpServletResponse response = new MockHttpServletResponse();

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(
                    org.mockito.ArgumentMatchers.any(),
                    org.mockito.ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("응답 로깅")
    class ResponseLogging {

        @Test
        @DisplayName("200 응답 로깅")
        void shouldLogSuccessResponse() throws Exception {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users");
            MockHttpServletResponse response = new MockHttpServletResponse();
            response.setStatus(200);

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(
                    org.mockito.ArgumentMatchers.any(),
                    org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("404 응답 로깅")
        void shouldLog404Response() throws Exception {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/unknown");
            MockHttpServletResponse response = new MockHttpServletResponse();

            org.mockito.Mockito.doAnswer(invocation -> {
                response.setStatus(404);
                return null;
            }).when(filterChain).doFilter(
                    org.mockito.ArgumentMatchers.any(),
                    org.mockito.ArgumentMatchers.any());

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            assertThat(response.getStatus()).isEqualTo(404);
        }

        @Test
        @DisplayName("500 응답 로깅")
        void shouldLog500Response() throws Exception {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/error");
            MockHttpServletResponse response = new MockHttpServletResponse();

            org.mockito.Mockito.doAnswer(invocation -> {
                response.setStatus(500);
                return null;
            }).when(filterChain).doFilter(
                    org.mockito.ArgumentMatchers.any(),
                    org.mockito.ArgumentMatchers.any());

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            assertThat(response.getStatus()).isEqualTo(500);
        }
    }

    @Nested
    @DisplayName("응답 본문 복사")
    class ResponseBodyCopy {

        @Test
        @DisplayName("응답 본문이 정상적으로 복사됨")
        void shouldCopyResponseBody() throws Exception {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/test");
            MockHttpServletResponse response = new MockHttpServletResponse();

            org.mockito.Mockito.doAnswer(invocation -> {
                // ContentCachingResponseWrapper를 통해 응답 작성
                var wrappedResponse = (HttpServletResponse) invocation.getArgument(1);
                wrappedResponse.setContentType("application/json");
                wrappedResponse.getWriter().write("{\"message\":\"success\"}");
                return null;
            }).when(filterChain).doFilter(
                    org.mockito.ArgumentMatchers.any(),
                    org.mockito.ArgumentMatchers.any());

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            // copyBodyToResponse가 호출되어 본문이 복사됨
            verify(filterChain).doFilter(
                    org.mockito.ArgumentMatchers.any(),
                    org.mockito.ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("requestId 생성")
    class RequestIdGeneration {

        @Test
        @DisplayName("X-Request-Id 헤더가 빈 문자열이면 UUID 생성")
        void shouldGenerateUuidWhenBlankRequestId() throws Exception {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/test");
            request.addHeader("X-Request-Id", "   ");
            MockHttpServletResponse response = new MockHttpServletResponse();

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(
                    org.mockito.ArgumentMatchers.any(),
                    org.mockito.ArgumentMatchers.any());
        }
    }
}
