package com.ryuqq.fileflow.adapter.in.rest.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * Request/Response 로깅 Filter.
 *
 * <p>모든 HTTP 요청과 응답을 로깅하며, MDC에 요청 추적 정보를 설정합니다.
 *
 * <p><strong>MDC 설정 항목</strong>:
 *
 * <ul>
 *   <li>requestId: 고유 요청 ID (UUID)
 *   <li>spanId: Span ID (OpenTelemetry 미사용 시 자동 생성)
 *   <li>method: HTTP 메서드
 *   <li>uri: 요청 URI
 *   <li>clientIp: 클라이언트 IP 주소
 * </ul>
 *
 * <p><strong>로깅 내용</strong>:
 *
 * <ul>
 *   <li>요청: 메서드, URI, 쿼리스트링, 클라이언트 IP
 *   <li>응답: 상태 코드, 처리 시간
 * </ul>
 *
 * <p><strong>spanId 생성 규칙</strong>:
 *
 * <ul>
 *   <li>X-Span-Id 헤더가 있으면 해당 값 사용
 *   <li>MDC에 spanId가 이미 설정되어 있으면 유지 (OpenTelemetry Agent 사용 시)
 *   <li>그 외의 경우 16자리 hex 문자열 생성 (OpenTelemetry 표준 형식)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    private static final String MDC_REQUEST_ID = "requestId";
    private static final String MDC_SPAN_ID = "spanId";
    private static final String MDC_METHOD = "method";
    private static final String MDC_URI = "uri";
    private static final String MDC_CLIENT_IP = "clientIp";

    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String HEADER_X_REQUEST_ID = "X-Request-Id";
    private static final String HEADER_X_SPAN_ID = "X-Span-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        String requestId = extractOrGenerateRequestId(request);
        String spanId = extractOrGenerateSpanId(request);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String clientIp = extractClientIp(request);

        // MDC 설정
        MDC.put(MDC_REQUEST_ID, requestId);
        MDC.put(MDC_SPAN_ID, spanId);
        MDC.put(MDC_METHOD, method);
        MDC.put(MDC_URI, uri);
        MDC.put(MDC_CLIENT_IP, clientIp);

        long startTime = System.currentTimeMillis();

        try {
            // 요청 로깅
            logRequest(method, uri, queryString, clientIp);

            filterChain.doFilter(wrappedRequest, wrappedResponse);

        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // 응답 로깅
            logResponse(method, uri, wrappedResponse.getStatus(), duration);

            // Response body 복사 (필수)
            wrappedResponse.copyBodyToResponse();

            // MDC 정리
            MDC.remove(MDC_REQUEST_ID);
            MDC.remove(MDC_SPAN_ID);
            MDC.remove(MDC_METHOD);
            MDC.remove(MDC_URI);
            MDC.remove(MDC_CLIENT_IP);
        }
    }

    /**
     * 요청 ID를 추출하거나 생성합니다.
     *
     * @param request HTTP 요청
     * @return 요청 ID
     */
    private String extractOrGenerateRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(HEADER_X_REQUEST_ID);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
        }
        return requestId;
    }

    /**
     * Span ID를 추출하거나 생성합니다.
     *
     * <p>우선순위:
     *
     * <ol>
     *   <li>X-Span-Id 헤더에서 추출
     *   <li>MDC에 이미 설정된 spanId 사용 (OpenTelemetry Agent 사용 시)
     *   <li>새로운 spanId 생성 (16자리 hex, OpenTelemetry 표준 형식)
     * </ol>
     *
     * @param request HTTP 요청
     * @return Span ID
     */
    private String extractOrGenerateSpanId(HttpServletRequest request) {
        // 1. 헤더에서 추출
        String spanId = request.getHeader(HEADER_X_SPAN_ID);
        if (spanId != null && !spanId.isBlank()) {
            return spanId;
        }

        // 2. MDC에서 확인 (OpenTelemetry Agent가 설정한 경우)
        spanId = MDC.get(MDC_SPAN_ID);
        if (spanId != null && !spanId.isBlank()) {
            return spanId;
        }

        // 3. 새로운 spanId 생성 (16자리 hex - OpenTelemetry 표준)
        return generateSpanId();
    }

    /**
     * OpenTelemetry 표준 형식의 Span ID를 생성합니다.
     *
     * <p>16자리 hex 문자열 (8바이트)
     *
     * @return 생성된 Span ID
     */
    private String generateSpanId() {
        UUID uuid = UUID.randomUUID();
        // 8바이트(64비트)를 16자리 hex로 변환 (앞쪽 0 패딩 포함)
        return String.format("%016x", uuid.getMostSignificantBits());
    }

    /**
     * 클라이언트 IP를 추출합니다.
     *
     * @param request HTTP 요청
     * @return 클라이언트 IP
     */
    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader(HEADER_X_FORWARDED_FOR);
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * 요청을 로깅합니다.
     *
     * @param method HTTP 메서드
     * @param uri 요청 URI
     * @param queryString 쿼리 스트링
     * @param clientIp 클라이언트 IP
     */
    private void logRequest(String method, String uri, String queryString, String clientIp) {
        String fullUri = queryString != null ? uri + "?" + queryString : uri;
        log.info("[REQ] {} {} from {}", method, fullUri, clientIp);
    }

    /**
     * 응답을 로깅합니다.
     *
     * @param method HTTP 메서드
     * @param uri 요청 URI
     * @param status 응답 상태 코드
     * @param duration 처리 시간 (ms)
     */
    private void logResponse(String method, String uri, int status, long duration) {
        log.info("[RES] {} {} - {} ({}ms)", method, uri, status, duration);
    }

    /**
     * 요청 본문을 추출합니다 (디버그용).
     *
     * @param request 캐싱된 요청
     * @return 요청 본문 문자열
     */
    private String extractRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            return new String(content, StandardCharsets.UTF_8);
        }
        return "";
    }

    /**
     * 응답 본문을 추출합니다 (디버그용).
     *
     * @param response 캐싱된 응답
     * @return 응답 본문 문자열
     */
    private String extractResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            return new String(content, StandardCharsets.UTF_8);
        }
        return "";
    }
}
