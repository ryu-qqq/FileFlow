package com.ryuqq.fileflow.adapter.in.rest.common.dto;

import java.time.LocalDateTime;

/**
 * ApiResponse - 표준 API 성공 응답 래퍼.
 *
 * <p>모든 REST API 성공 응답의 일관된 형식을 제공합니다.
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>{@code
 * // 데이터가 있는 성공 응답
 * ApiResponse<UserDto> response = ApiResponse.ofSuccess(userDto);
 *
 * // 데이터가 없는 성공 응답
 * ApiResponse<Void> response = ApiResponse.ofSuccess();
 * }</pre>
 *
 * <p><strong>응답 형식:</strong>
 *
 * <pre>{@code
 * {
 *   "success": true,
 *   "data": { ... },
 *   "timestamp": "2025-10-23T10:30:00",
 *   "requestId": "req-123456"
 * }
 * }</pre>
 *
 * <p><strong>에러 응답:</strong> 에러 응답은 GlobalExceptionHandler에서 RFC 7807 ProblemDetail 형식으로 처리됩니다.
 *
 * @param <T> 응답 데이터 타입
 * @author ryu-qqq
 * @since 2025-10-23
 */
public record ApiResponse<T>(boolean success, T data, LocalDateTime timestamp, String requestId) {

    /**
     * 성공 응답 생성.
     *
     * @param data 응답 데이터
     * @param <T> 데이터 타입
     * @return 성공 ApiResponse
     */
    public static <T> ApiResponse<T> ofSuccess(T data) {
        return new ApiResponse<>(true, data, LocalDateTime.now(), generateRequestId());
    }

    /**
     * 성공 응답 생성 (데이터 없음).
     *
     * @param <T> 데이터 타입
     * @return 성공 ApiResponse
     */
    public static <T> ApiResponse<T> ofSuccess() {
        return ofSuccess(null);
    }

    /**
     * Request ID 생성.
     *
     * <p>실제 운영 환경에서는 MDC나 분산 추적 시스템의 Trace ID를 사용하는 것이 좋습니다.
     *
     * @return Request ID
     */
    private static String generateRequestId() {
        // TODO: MDC or Distributed Tracing ID 사용 권장
        return "req-" + System.currentTimeMillis();
    }
}
