package com.ryuqq.fileflow.application.session.internal;

import com.ryuqq.fileflow.application.session.dto.bundle.MultipartSessionCreationBundle;
import com.ryuqq.fileflow.application.session.dto.command.CreateMultipartUploadSessionCommand;
import com.ryuqq.fileflow.application.session.factory.command.MultipartSessionCommandFactory;
import com.ryuqq.fileflow.application.session.manager.client.MultipartUploadManager;
import com.ryuqq.fileflow.application.session.manager.client.PresignedUploadManager;
import com.ryuqq.fileflow.application.session.manager.client.SessionExpirationManager;
import com.ryuqq.fileflow.application.session.manager.command.SessionCommandManager;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import org.springframework.stereotype.Component;

/**
 * 멀티파트 업로드 세션 생성 코디네이터.
 *
 * <p>팩토리(순수 도메인 계산) → 외부 데이터 해결 → 영속화 → 만료 등록까지 오케스트레이션합니다.
 *
 * <pre>
 * Service → Coordinator.create(command)
 *             ├─ Factory.create(command)                    // 순수 도메인 계산
 *             ├─ presignedUploadManager.getBucket()         // bucket 해결
 *             ├─ multipartUploadManager.createMultipart()   // uploadId 해결
 *             ├─ bundle.withBucket().withUploadId()         // 번들 enrichment
 *             ├─ bundle.toSession()                         // 도메인 객체 생성
 *             ├─ sessionCommandManager.persist()            // 영속화
 *             ├─ sessionExpirationManager.register()        // 만료 등록
 *             └─ return session
 * </pre>
 */
@Component
public class MultipartSessionCreationCoordinator {

    private final MultipartSessionCommandFactory multipartFactory;
    private final PresignedUploadManager presignedUploadManager;
    private final MultipartUploadManager multipartUploadManager;
    private final SessionCommandManager sessionCommandManager;
    private final SessionExpirationManager sessionExpirationManager;

    public MultipartSessionCreationCoordinator(
            MultipartSessionCommandFactory multipartFactory,
            PresignedUploadManager presignedUploadManager,
            MultipartUploadManager multipartUploadManager,
            SessionCommandManager sessionCommandManager,
            SessionExpirationManager sessionExpirationManager) {
        this.multipartFactory = multipartFactory;
        this.presignedUploadManager = presignedUploadManager;
        this.multipartUploadManager = multipartUploadManager;
        this.sessionCommandManager = sessionCommandManager;
        this.sessionExpirationManager = sessionExpirationManager;
    }

    public MultipartUploadSession create(CreateMultipartUploadSessionCommand command) {
        MultipartSessionCreationBundle bundle = multipartFactory.create(command);

        String bucket = presignedUploadManager.getBucket();
        String uploadId =
                multipartUploadManager.createMultipartUpload(bundle.s3Key(), command.contentType());

        MultipartUploadSession session =
                bundle.withBucket(bucket).withUploadId(uploadId).toSession();

        sessionCommandManager.persist(session);
        sessionExpirationManager.registerExpiration(bundle.expiration());

        return session;
    }
}
