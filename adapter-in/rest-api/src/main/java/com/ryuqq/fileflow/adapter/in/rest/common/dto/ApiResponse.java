package com.ryuqq.fileflow.adapter.in.rest.common.dto;

import com.ryuqq.fileflow.adapter.in.rest.common.util.DateTimeFormatUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.slf4j.MDC;

/**
 * 표준 API 성공 응답 래퍼.
 *
 * @param <T> 응답 데이터 타입
 */
@Schema(description = "표준 API 성공 응답")
public record ApiResponse<T>(
        @Schema(description = "응답 데이터", nullable = true) T data,
        @Schema(description = "응답 시간 (ISO 8601 형식)", example = "2025-01-23T10:30:00+09:00")
                String timestamp,
        @Schema(description = "요청 ID", example = "550e8400-e29b-41d4-a716-446655440000")
                String requestId) {

    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data, DateTimeFormatUtils.nowIso8601(), generateRequestId());
    }

    public static <T> ApiResponse<T> of() {
        return of(null);
    }

    private static String generateRequestId() {
        String traceId = MDC.get("traceId");
        if (traceId != null && !traceId.isBlank()) {
            return traceId;
        }
        return UUID.randomUUID().toString();
    }
}
