package com.ryuqq.fileflow.application.download.manager;

import com.ryuqq.fileflow.application.download.port.out.command.ExternalDownloadPersistencePort;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 외부 다운로드 영속화 Manager.
 *
 * <p>ExternalDownload Aggregate의 영속화를 담당합니다.
 *
 * <p><strong>단일 책임</strong>: ExternalDownloadPersistencePort만 의존
 */
@Component
public class ExternalDownloadManager {

    private final ExternalDownloadPersistencePort persistencePort;

    public ExternalDownloadManager(ExternalDownloadPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    /**
     * ExternalDownload를 저장합니다.
     *
     * @param externalDownload 저장할 ExternalDownload
     * @return 생성된 ExternalDownloadId
     */
    @Transactional
    public ExternalDownloadId save(ExternalDownload externalDownload) {
        return persistencePort.persist(externalDownload);
    }
}
