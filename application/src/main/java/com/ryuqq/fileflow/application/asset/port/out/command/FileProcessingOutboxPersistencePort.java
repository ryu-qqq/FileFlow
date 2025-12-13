package com.ryuqq.fileflow.application.asset.port.out.command;

import com.ryuqq.fileflow.domain.asset.aggregate.FileProcessingOutbox;
import com.ryuqq.fileflow.domain.asset.vo.FileProcessingOutboxId;

/**
 * FileProcessingOutbox 영속화 포트.
 *
 * <p>파일 처리 이벤트를 위한 Outbox 패턴 영속화를 담당한다.
 *
 * <p>persist()는 신규/수정을 JPA merge로 통합 처리한다.
 */
public interface FileProcessingOutboxPersistencePort {

    /**
     * FileProcessingOutbox를 저장한다.
     *
     * <p>신규 저장 및 상태 업데이트를 통합 처리한다 (JPA merge 활용).
     *
     * @param outbox 저장할 FileProcessingOutbox
     * @return 저장된 FileProcessingOutbox ID
     */
    FileProcessingOutboxId persist(FileProcessingOutbox outbox);
}
