package com.ryuqq.fileflow.adapter.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler - 전역 예외 처리 핸들러
 *
 * <p>REST API에서 발생하는 모든 예외를 처리하여 RFC 7807 표준을 준수하는
 * Problem Details 형식으로 응답을 반환합니다.</p>
 *
 * <p><strong>RFC 7807 준수:</strong></p>
 * <ul>
 *   <li>✅ {@code ProblemDetail} 사용 (Spring 6.0+ 표준 지원)</li>
 *   <li>✅ Content-Type: application/problem+json</li>
 *   <li>✅ type, title, status, detail, instance 필드 제공</li>
 *   <li>✅ 추가 필드 (timestamp, errors) 확장 가능</li>
 * </ul>
 *
 * <p><strong>처리하는 예외:</strong></p>
 * <ul>
 *   <li>400 Bad Request: {@code MethodArgumentNotValidException} (Validation 실패)</li>
 *   <li>400 Bad Request: {@code IllegalArgumentException} (잘못된 인자)</li>
 *   <li>409 Conflict: {@code IllegalStateException} (중복, 상태 충돌)</li>
 *   <li>500 Internal Server Error: {@code Exception} (기타 모든 예외)</li>
 * </ul>
 *
 * <p><strong>Response Example (RFC 7807):</strong></p>
 * <pre>{@code
 * {
 *   "type": "about:blank",
 *   "title": "Bad Request",
 *   "status": 400,
 *   "detail": "Validation failed for request",
 *   "instance": "/api/v1/tenants",
 *   "timestamp": "2025-10-22T10:30:00Z",
 *   "errors": {
 *     "name": "Tenant 이름은 필수입니다"
 *   }
 * }
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Validation 실패 처리 (400 Bad Request)
     *
     * <p>{@code @Valid} 검증 실패 시 발생하는 {@code MethodArgumentNotValidException}을 처리합니다.</p>
     *
     * <p><strong>Response Example:</strong></p>
     * <pre>{@code
     * {
     *   "type": "about:blank",
     *   "title": "Bad Request",
     *   "status": 400,
     *   "detail": "Validation failed for request",
     *   "timestamp": "2025-10-22T10:30:00Z",
     *   "errors": {
     *     "name": "Tenant 이름은 필수입니다",
     *     "orgCode": "조직 코드는 필수입니다"
     *   }
     * }
     * }</pre>
     *
     * @param ex {@code MethodArgumentNotValidException}
     * @return RFC 7807 ProblemDetail (400 Bad Request)
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Validation failed for request"
        );
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setTitle("Bad Request");
        problemDetail.setProperty("timestamp", Instant.now().toString());

        // Validation 에러 필드 수집
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

    /**
     * 잘못된 인자 처리 (400 Bad Request)
     *
     * <p>{@code IllegalArgumentException}을 처리합니다.</p>
     *
     * <p><strong>Response Example:</strong></p>
     * <pre>{@code
     * {
     *   "type": "about:blank",
     *   "title": "Bad Request",
     *   "status": 400,
     *   "detail": "Tenant ID는 필수입니다",
     *   "timestamp": "2025-10-22T10:30:00Z"
     * }
     * }</pre>
     *
     * @param ex {@code IllegalArgumentException}
     * @return RFC 7807 ProblemDetail (400 Bad Request)
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setTitle("Bad Request");
        problemDetail.setProperty("timestamp", Instant.now().toString());

        return problemDetail;
    }

    /**
     * 중복/상태 충돌 처리 (409 Conflict)
     *
     * <p>{@code IllegalStateException}을 처리합니다. (예: 중복된 Tenant 이름)</p>
     *
     * <p><strong>Response Example:</strong></p>
     * <pre>{@code
     * {
     *   "type": "about:blank",
     *   "title": "Conflict",
     *   "status": 409,
     *   "detail": "동일한 이름의 Tenant가 이미 존재합니다: my-tenant",
     *   "timestamp": "2025-10-22T10:30:00Z"
     * }
     * }</pre>
     *
     * @param ex {@code IllegalStateException}
     * @return RFC 7807 ProblemDetail (409 Conflict)
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalStateException(IllegalStateException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
        );
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setTitle("Conflict");
        problemDetail.setProperty("timestamp", Instant.now().toString());

        return problemDetail;
    }

    /**
     * 기타 모든 예외 처리 (500 Internal Server Error)
     *
     * <p>처리되지 않은 모든 예외를 처리합니다.</p>
     *
     * <p><strong>Response Example:</strong></p>
     * <pre>{@code
     * {
     *   "type": "about:blank",
     *   "title": "Internal Server Error",
     *   "status": 500,
     *   "detail": "An unexpected error occurred",
     *   "timestamp": "2025-10-22T10:30:00Z"
     * }
     * }</pre>
     *
     * @param ex {@code Exception}
     * @return RFC 7807 ProblemDetail (500 Internal Server Error)
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGlobalException(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred"
        );
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setProperty("timestamp", Instant.now().toString());
        // TODO: Logging 추가 (실제 에러 메시지는 로그에만 기록)

        return problemDetail;
    }
}
