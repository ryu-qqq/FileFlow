package com.ryuqq.fileflow.application.download.validator;

import com.ryuqq.fileflow.application.download.manager.query.DownloadReadManager;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import org.springframework.stereotype.Component;

@Component
public class DownloadTaskValidator {

    private final DownloadReadManager downloadReadManager;

    public DownloadTaskValidator(DownloadReadManager downloadReadManager) {
        this.downloadReadManager = downloadReadManager;
    }

    /** 다운로드 태스크 존재 여부를 검증하고 반환합니다. 없으면 DownloadTaskNotFoundException을 던집니다. */
    public DownloadTask getExistingTask(String downloadTaskId) {
        return downloadReadManager.getDownloadTask(downloadTaskId);
    }
}
