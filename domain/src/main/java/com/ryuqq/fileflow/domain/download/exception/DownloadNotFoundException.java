package com.ryuqq.fileflow.domain.download.exception;

import com.ryuqq.fileflow.domain.common.DomainException;
import com.ryuqq.fileflow.domain.download.ExternalDownloadId;

/**
 * DownloadNotFoundException - Download를 찾을 수 없을 때 발생하는 예외
 *
 * <p>Download 조회 시 해당 ID의 Download가 존재하지 않을 때 발생합니다.</p>
 *
 * <p><strong>HTTP 응답:</strong></p>
 * <ul>
 *   <li>Status Code: 404 NOT FOUND</li>
 *   <li>Error Code: DOWNLOAD-001</li>
 *   <li>Message: "Download not found: {downloadId}"</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * ExternalDownload download = downloadPort.findById(downloadId)
 *     .orElseThrow(() -> new DownloadNotFoundException(downloadId));
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
public class DownloadNotFoundException extends DomainException {

    /**
     * Constructor - ExternalDownloadId Value Object를 사용한 예외 생성
     *
     * <p>에러 메시지에 찾지 못한 Download ID를 포함시킵니다.</p>
     * <p>도메인 모델의 일관성을 위해 Value Object를 사용합니다.</p>
     *
     * @param downloadId 찾지 못한 Download ID (Value Object)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public DownloadNotFoundException(ExternalDownloadId downloadId) {
        super(
            DownloadErrorCode.DOWNLOAD_NOT_FOUND,
            "Download not found: " + downloadId.value()
        );
    }

    /**
     * Constructor - Long ID를 사용한 예외 생성 (하위 호환성)
     *
     * <p>기존 코드와의 호환성을 위해 유지합니다.</p>
     * <p>새로운 코드는 {@link #DownloadNotFoundException(ExternalDownloadId)}를 사용하세요.</p>
     *
     * @param downloadId 찾지 못한 Download ID (Long)
     * @author Sangwon Ryu
     * @since 1.0.0
     * @deprecated Use {@link #DownloadNotFoundException(ExternalDownloadId)} instead
     */
    @Deprecated
    public DownloadNotFoundException(Long downloadId) {
        this(ExternalDownloadId.of(downloadId));
    }

    /**
     * Constructor - 기본 에러 메시지 사용
     *
     * <p>DownloadErrorCode의 기본 메시지를 사용합니다.</p>
     *
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public DownloadNotFoundException() {
        super(DownloadErrorCode.DOWNLOAD_NOT_FOUND);
    }
}

