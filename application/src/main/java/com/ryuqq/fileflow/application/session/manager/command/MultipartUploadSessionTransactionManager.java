package com.ryuqq.fileflow.application.session.manager.command;

import com.ryuqq.fileflow.application.session.port.out.command.MultipartUploadSessionPersistencePort;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * MultipartUploadSession 영속화 TransactionManager.
 *
 * <p>MultipartUploadSession의 영속화를 담당합니다.
 *
 * <p><strong>컨벤션</strong>:
 *
 * <ul>
 *   <li>단일 PersistencePort 의존성
 *   <li>persist* 메서드만 허용
 *   <li>@Component + @Transactional 필수
 * </ul>
 */
@Component
@Transactional
public class MultipartUploadSessionTransactionManager {

    private final MultipartUploadSessionPersistencePort multipartUploadSessionPersistencePort;

    public MultipartUploadSessionTransactionManager(
            MultipartUploadSessionPersistencePort multipartUploadSessionPersistencePort) {
        this.multipartUploadSessionPersistencePort = multipartUploadSessionPersistencePort;
    }

    /**
     * Multipart 업로드 세션을 저장합니다.
     *
     * <p>RDB(upload_session_multipart)와 Redis Cache에 모두 저장되며, Cache에는 TTL(24시간)이 설정됩니다.
     *
     * @param session 저장할 세션
     * @return 저장된 세션 (ID 포함)
     */
    public MultipartUploadSession persist(MultipartUploadSession session) {
        return multipartUploadSessionPersistencePort.persist(session);
    }
}
