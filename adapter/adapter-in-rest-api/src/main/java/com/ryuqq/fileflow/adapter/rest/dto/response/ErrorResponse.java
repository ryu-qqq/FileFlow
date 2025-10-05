package com.ryuqq.fileflow.adapter.rest.dto.response;

import java.time.LocalDateTime;

/**
 * Error Response DTO
 *
 * API 에러 응답을 전달하는 DTO
 *
 * @param timestamp 에러 발생 시각
 * @param status HTTP 상태 코드
 * @param error 에러 타입
 * @param message 에러 메시지
 * @param path 요청 경로
 * @author sangwon-ryu
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
    /**
     * ErrorResponse 생성 팩토리 메서드
     *
     * @param status HTTP 상태 코드
     * @param error 에러 타입
     * @param message 에러 메시지
     * @param path 요청 경로
     * @return ErrorResponse
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                path
        );
    }
}
