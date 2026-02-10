package com.ryuqq.fileflow.domain.common.exception;

/**
 * ErrorCode - 비즈니스 예외 에러 코드 인터페이스
 *
 * <p>모든 비즈니스 예외는 ErrorCode를 구현하여 일관된 에러 정보를 제공합니다.
 *
 * <p><strong>설계 원칙:</strong>
 *
 * <ul>
 *   <li>Bounded Context별 ErrorCode enum 구현
 *   <li>HTTP Status와 에러 코드 매핑
 *   <li>명확한 에러 메시지 제공
 * </ul>
 *
 * <p><strong>구현 예시:</strong>
 *
 * <pre>{@code
 * public enum SessionErrorCode implements ErrorCode {
 *     SESSION_NOT_FOUND("SESSION-001", 404, "Session not found");
 *     // ...
 * }
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
public interface ErrorCode {

    /**
     * 에러 코드 반환
     *
     * <p>형식: {CONTEXT}-{NUMBER} (예: SESSION-001, ASSET-002)
     *
     * @return 에러 코드 문자열
     */
    String getCode();

    /**
     * HTTP 상태 코드 반환
     *
     * <p>RESTful API 응답에 사용될 HTTP 상태 코드
     *
     * @return HTTP 상태 코드 (예: 404, 400, 500)
     */
    int getHttpStatus();

    /**
     * 에러 메시지 반환
     *
     * <p>사용자에게 표시될 에러 메시지
     *
     * @return 에러 메시지 문자열
     */
    String getMessage();
}
