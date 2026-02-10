package com.ryuqq.fileflow.domain.download.exception;

import java.util.Map;

public class DownloadTaskNotFoundException extends DownloadException {

    public DownloadTaskNotFoundException(String downloadTaskId) {
        super(
                DownloadErrorCode.DOWNLOAD_TASK_NOT_FOUND,
                "다운로드 작업을 찾을 수 없습니다. downloadTaskId: " + downloadTaskId,
                Map.of("downloadTaskId", downloadTaskId != null ? downloadTaskId : "null"));
    }
}
