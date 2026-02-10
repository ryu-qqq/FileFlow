package com.ryuqq.fileflow.domain.download.exception;

import java.util.Map;

public class DownloadExceptionFixture {

    public static DownloadException aDownloadException() {
        return new DownloadException(DownloadErrorCode.INVALID_DOWNLOAD_STATUS);
    }

    public static DownloadException aDownloadExceptionWithDetail() {
        return new DownloadException(
                DownloadErrorCode.INVALID_DOWNLOAD_STATUS,
                "Cannot start download in status: DOWNLOADING");
    }

    public static DownloadException aDownloadExceptionWithArgs() {
        return new DownloadException(
                DownloadErrorCode.DOWNLOAD_TASK_NOT_FOUND,
                "다운로드 작업을 찾을 수 없습니다",
                Map.of("taskId", "download-001"));
    }
}
