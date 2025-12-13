package com.ryuqq.fileflow.adapter.in.rest.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
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
 * Security 예외 통합 핸들러.
 *
 * <p>Spring Security의 인증/인가 실패를 처리합니다. GlobalExceptionHandler와 동일한 RFC 7807 Problem Details 형식으로
 * 응답합니다.
 *
 * <p><strong>처리하는 예외</strong>:
 *
 * <ul>
 *   <li>AuthenticationException: 401 Unauthorized (인증 실패)
 *   <li>AccessDeniedException: 403 Forbidden (인가 실패)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(SecurityExceptionHandler.class);

    private final ObjectMapper objectMapper;

    public SecurityExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 인증 실패 처리 (401 Unauthorized).
     *
     * <p>인증이 필요한 엔드포인트에 인증 없이 접근할 때 호출됩니다.
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param authException 인증 예외
     * @throws IOException 응답 작성 실패 시
     * @throws ServletException 서블릿 예외 시
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException, ServletException {

        log.warn(
                "Authentication failed: uri={}, message={}",
                request.getRequestURI(),
                authException.getMessage());

        writeResponse(
                response,
                request,
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                "UNAUTHORIZED",
                "인증이 필요합니다. 유효한 토큰을 제공해주세요.");
    }

    /**
     * 접근 거부 처리 (403 Forbidden).
     *
     * <p>인증은 되었지만 권한이 없는 리소스에 접근할 때 호출됩니다.
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param accessDeniedException 접근 거부 예외
     * @throws IOException 응답 작성 실패 시
     * @throws ServletException 서블릿 예외 시
     */
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        log.warn(
                "Access denied: uri={}, message={}",
                request.getRequestURI(),
                accessDeniedException.getMessage());

        writeResponse(
                response,
                request,
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "ACCESS_DENIED",
                "이 리소스에 대한 접근 권한이 없습니다.");
    }

    /**
     * RFC 7807 Problem Details 형식으로 응답을 작성합니다.
     *
     * <p>GlobalExceptionHandler와 동일한 형식을 사용합니다.
     *
     * @param response HTTP 응답
     * @param request HTTP 요청
     * @param status HTTP 상태 코드
     * @param title 에러 제목
     * @param code 에러 코드
     * @param detail 에러 상세 메시지
     * @throws IOException 응답 작성 실패 시
     */
    private void writeResponse(
            HttpServletResponse response,
            HttpServletRequest request,
            HttpStatus status,
            String title,
            String code,
            String detail)
            throws IOException {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setType(URI.create("about:blank"));

        // RFC 7807 extension members
        problemDetail.setProperty("code", code);
        problemDetail.setProperty("timestamp", Instant.now().toString());

        // 요청 경로를 instance로
        String uri = request.getRequestURI();
        if (request.getQueryString() != null && !request.getQueryString().isBlank()) {
            uri = uri + "?" + request.getQueryString();
        }
        problemDetail.setInstance(URI.create(uri));

        // tracing (MDC에서 추출)
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
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), problemDetail);
    }
}
