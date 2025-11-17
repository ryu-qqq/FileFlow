package com.ryuqq.fileflow.domain.common.exception;

/**
 * Domain Exception ErrorCode 인터페이스
 * <p>
 * 모든 ErrorCode Enum이 구현해야 하는 인터페이스입니다.
 * </p>
 *
 * <p>
 * <strong>구현 규칙</strong>:
 * <ul>
 *   <li>ErrorCode Enum은 이 인터페이스를 구현해야 함</li>
 *   <li>코드 형식: {BC}-{3자리 숫자} (예: FILE-001, MIME-001)</li>
 *   <li>HTTP Status는 int 타입 사용 (Spring HttpStatus 의존 금지)</li>
 *   <li>Lombok 사용 금지</li>
 * </ul>
 * </p>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface ErrorCode {

    /**
     * 에러 코드 반환
     * <p>
     * 형식: {BC}-{3자리 숫자}
     * 예시: FILE-001, MIME-001, SIZE-001
     * </p>
     *
     * @return 에러 코드
     */
    String getCode();

    /**
     * 에러 메시지 반환
     * <p>
     * 사용자에게 표시될 에러 메시지
     * </p>
     *
     * @return 에러 메시지
     */
    String getMessage();

    /**
     * HTTP 상태 코드 반환
     * <p>
     * Spring HttpStatus 의존을 피하기 위해 int 타입 사용
     * </p>
     *
     * @return HTTP 상태 코드 (예: 400, 404, 500)
     */
    int getHttpStatus();
}
