package com.ryuqq.fileflow.application.session.manager.command;

import com.ryuqq.fileflow.application.session.port.out.command.MultipartUploadSessionPersistencePort;
import com.ryuqq.fileflow.application.session.port.out.command.SingleUploadSessionPersistencePort;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SessionCommandManager {

    private final SingleUploadSessionPersistencePort singlePersistencePort;
    private final MultipartUploadSessionPersistencePort multipartPersistencePort;

    public SessionCommandManager(
            SingleUploadSessionPersistencePort singlePersistencePort,
            MultipartUploadSessionPersistencePort multipartPersistencePort) {
        this.singlePersistencePort = singlePersistencePort;
        this.multipartPersistencePort = multipartPersistencePort;
    }

    @Transactional
    public void persist(SingleUploadSession session) {
        singlePersistencePort.persist(session);
    }

    @Transactional
    public void persist(MultipartUploadSession session) {
        multipartPersistencePort.persist(session);
    }
}
