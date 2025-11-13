package com.ryuqq.fileflow.domain.download;

/**
 * External Download 상태 Enum
 */
public enum ExternalDownloadStatus {
    INIT,
    DOWNLOADING,
    COMPLETED,
    FAILED,
    ABORTED
}
