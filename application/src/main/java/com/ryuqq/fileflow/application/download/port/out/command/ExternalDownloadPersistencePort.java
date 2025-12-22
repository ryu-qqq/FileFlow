package com.ryuqq.fileflow.application.download.port.out.command;

import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;

/**
 * ExternalDownload 영속화 포트.
 *
 * <p>CQRS Command Side - ExternalDownload 저장
 */
public interface ExternalDownloadPersistencePort {

    /**
     * ExternalDownload를 저장합니다.
     *
     * @param externalDownload 저장할 ExternalDownload
     * @return 저장된 ExternalDownload (version 갱신됨)
     */
    ExternalDownload persist(ExternalDownload externalDownload);
}
