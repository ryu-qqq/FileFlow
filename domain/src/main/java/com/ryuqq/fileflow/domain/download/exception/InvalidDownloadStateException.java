package com.ryuqq.fileflow.domain.download.exception;

import com.ryuqq.fileflow.domain.common.DomainException;
import com.ryuqq.fileflow.domain.download.ExternalDownloadStatus;

/**
 * InvalidDownloadStateException - 유효하지 않은 Download 상태 전이 시 발생하는 예외
 *
 * <p>Download 상태 전이가 허용되지 않는 상태일 때 발생합니다.</p>
 *
 * <p><strong>HTTP 응답:</strong></p>
 * <ul>
 *   <li>Status Code: 400 BAD REQUEST</li>
 *   <li>Error Code: DOWNLOAD-002</li>
 *   <li>Message: "Invalid download state: {currentStatus}"</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * if (status != ExternalDownloadStatus.INIT) {
 *     throw new InvalidDownloadStateException(status, "Can only start from INIT state");
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
public class InvalidDownloadStateException extends DomainException {

    /**
     * Constructor - 현재 상태와 상세 메시지를 포함한 예외 생성
     *
     * @param currentStatus 현재 Download 상태
     * @param detailMessage 상세 메시지
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public InvalidDownloadStateException(ExternalDownloadStatus currentStatus, String detailMessage) {
        super(
            DownloadErrorCode.INVALID_DOWNLOAD_STATE,
            detailMessage + ": " + currentStatus
        );
    }

    /**
     * Constructor - 현재 상태만 포함한 예외 생성
     *
     * @param currentStatus 현재 Download 상태
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public InvalidDownloadStateException(ExternalDownloadStatus currentStatus) {
        super(
            DownloadErrorCode.INVALID_DOWNLOAD_STATE,
            "Invalid download state: " + currentStatus
        );
    }

    /**
     * Constructor - 기본 에러 메시지 사용
     *
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public InvalidDownloadStateException() {
        super(DownloadErrorCode.INVALID_DOWNLOAD_STATE);
    }
}

