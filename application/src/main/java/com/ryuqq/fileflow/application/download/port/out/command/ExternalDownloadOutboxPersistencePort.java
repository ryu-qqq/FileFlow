package com.ryuqq.fileflow.application.download.port.out.command;

import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadOutboxId;

/**
 * ExternalDownloadOutbox 영속화 포트.
 *
 * <p>CQRS Command Side - Outbox 저장
 */
public interface ExternalDownloadOutboxPersistencePort {

    /**
     * ExternalDownloadOutbox를 저장합니다.
     *
     * @param outbox 저장할 ExternalDownloadOutbox
     * @return 생성된 ExternalDownloadOutboxId
     */
    ExternalDownloadOutboxId persist(ExternalDownloadOutbox outbox);
}
