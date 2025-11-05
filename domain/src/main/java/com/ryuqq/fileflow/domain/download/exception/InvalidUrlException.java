package com.ryuqq.fileflow.domain.download.exception;

import com.ryuqq.fileflow.domain.common.DomainException;

/**
 * InvalidUrlException - 유효하지 않은 URL일 때 발생하는 예외
 *
 * <p>URL 검증 실패 시 발생합니다 (null, 빈 문자열, 잘못된 형식, HTTP/HTTPS가 아닌 프로토콜).</p>
 *
 * <p><strong>HTTP 응답:</strong></p>
 * <ul>
 *   <li>Status Code: 400 BAD REQUEST</li>
 *   <li>Error Code: DOWNLOAD-003</li>
 *   <li>Message: "Invalid URL: {detailMessage}"</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * if (url == null || url.isBlank()) {
 *     throw new InvalidUrlException("URL은 필수입니다");
 * }
 * }</pre>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ DomainException 상속</li>
 *   <li>✅ DownloadErrorCode 사용</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class InvalidUrlException extends DomainException {

    /**
     * Constructor - 상세 메시지를 포함한 예외 생성
     *
     * @param detailMessage 상세 메시지
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public InvalidUrlException(String detailMessage) {
        super(
            DownloadErrorCode.INVALID_URL,
            "Invalid URL: " + detailMessage
        );
    }

    /**
     * Constructor - 원인 예외를 포함한 예외 생성
     *
     * @param detailMessage 상세 메시지
     * @param cause 원인 예외 (MalformedURLException 등)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public InvalidUrlException(String detailMessage, Throwable cause) {
        super(
            DownloadErrorCode.INVALID_URL,
            "Invalid URL: " + detailMessage,
            cause
        );
    }

    /**
     * Constructor - 기본 에러 메시지 사용
     *
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public InvalidUrlException() {
        super(DownloadErrorCode.INVALID_URL);
    }
}

