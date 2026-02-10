package com.ryuqq.fileflow.application.session.port.out.command;

import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;

/** SingleUploadSession 영속화 포트 (Command) */
public interface SingleUploadSessionPersistencePort {

    /**
     * 세션 저장 (신규 생성 또는 수정)
     *
     * @param session 저장할 세션
     */
    void persist(SingleUploadSession session);
}
