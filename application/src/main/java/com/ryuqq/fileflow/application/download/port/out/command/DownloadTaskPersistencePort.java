package com.ryuqq.fileflow.application.download.port.out.command;

import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import java.time.Instant;

public interface DownloadTaskPersistencePort {

    long persist(DownloadTask downloadTask);

    /** 도메인 모델 로딩 없이 ID 기반으로 FAILED 상태 전환. corrupted 데이터 처리용. */
    void markFailedById(String downloadTaskId, String errorMessage, Instant failedAt);
}
