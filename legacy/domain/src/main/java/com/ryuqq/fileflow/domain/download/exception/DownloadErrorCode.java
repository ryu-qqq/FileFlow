package com.ryuqq.fileflow.domain.download.exception;

import com.ryuqq.fileflow.domain.common.ErrorCode;

/**
 * DownloadErrorCode - Download Bounded Context 에러 코드
 *
 * <p>Download 도메인에서 발생하는 모든 비즈니스 예외의 에러 코드를 정의합니다.</p>
 *
 * <p><strong>에러 코드 규칙:</strong></p>
 * <ul>
 *   <li>✅ 형식: DOWNLOAD-{3자리 숫자}</li>
 *   <li>✅ HTTP 상태 코드 매핑</li>
 *   <li>✅ 명확한 에러 메시지</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * throw new DownloadNotFoundException(downloadId);
 * // → ErrorCode: DOWNLOAD-001, HTTP Status: 404
 * }</pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public enum DownloadErrorCode implements ErrorCode {

    /**
     * Download를 찾을 수 없음
     */
    DOWNLOAD_NOT_FOUND("DOWNLOAD-001", 404, "Download not found"),

    /**
     * 유효하지 않은 Download 상태
     */
    INVALID_DOWNLOAD_STATE("DOWNLOAD-002", 400, "Invalid download state"),

    /**
     * 유효하지 않은 URL
     */
    INVALID_URL("DOWNLOAD-003", 400, "Invalid URL");

    private final String code;
    private final int httpStatus;
    private final String message;

    /**
     * Constructor - ErrorCode 생성
     *
     * @param code 에러 코드 (DOWNLOAD-XXX)
     * @param httpStatus HTTP 상태 코드
     * @param message 에러 메시지
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    DownloadErrorCode(String code, int httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    /**
     * 에러 코드 반환
     *
     * @return 에러 코드 문자열 (예: DOWNLOAD-001)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    @Override
    public String getCode() {
        return code;
    }

    /**
     * HTTP 상태 코드 반환
     *
     * @return HTTP 상태 코드 (예: 404, 400, 500)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    /**
     * 에러 메시지 반환
     *
     * @return 에러 메시지 문자열
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    @Override
    public String getMessage() {
        return message;
    }
}

