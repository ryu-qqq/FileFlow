package com.ryuqq.fileflow.application.session.internal;

import com.ryuqq.fileflow.application.session.dto.bundle.SingleSessionCreationBundle;
import com.ryuqq.fileflow.application.session.dto.command.CreateSingleUploadSessionCommand;
import com.ryuqq.fileflow.application.session.factory.command.SingleSessionCommandFactory;
import com.ryuqq.fileflow.application.session.manager.client.PresignedUploadManager;
import com.ryuqq.fileflow.application.session.manager.client.SessionExpirationManager;
import com.ryuqq.fileflow.application.session.manager.command.SessionCommandManager;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import java.time.Duration;
import org.springframework.stereotype.Component;

/**
 * 단건 업로드 세션 생성 코디네이터.
 *
 * <p>팩토리(순수 도메인 계산) → 외부 데이터 해결 → 영속화 → 만료 등록까지 오케스트레이션합니다.
 *
 * <pre>
 * Service → Coordinator.create(command)
 *             ├─ Factory.create(command)                    // 순수 도메인 계산
 *             ├─ presignedUploadManager                     // bucket, presignedUrl 해결
 *             ├─ bundle.withBucket().withPresignedUrl()      // 번들 enrichment
 *             ├─ bundle.toSession()                         // 도메인 객체 생성
 *             ├─ sessionCommandManager.persist()            // 영속화
 *             ├─ sessionExpirationManager.register()        // 만료 등록
 *             └─ return session
 * </pre>
 */
@Component
public class SingleSessionCreationCoordinator {

    private final SingleSessionCommandFactory singleFactory;
    private final PresignedUploadManager presignedUploadManager;
    private final SessionCommandManager sessionCommandManager;
    private final SessionExpirationManager sessionExpirationManager;

    public SingleSessionCreationCoordinator(
            SingleSessionCommandFactory singleFactory,
            PresignedUploadManager presignedUploadManager,
            SessionCommandManager sessionCommandManager,
            SessionExpirationManager sessionExpirationManager) {
        this.singleFactory = singleFactory;
        this.presignedUploadManager = presignedUploadManager;
        this.sessionCommandManager = sessionCommandManager;
        this.sessionExpirationManager = sessionExpirationManager;
    }

    public SingleUploadSession create(CreateSingleUploadSessionCommand command) {
        SingleSessionCreationBundle bundle = singleFactory.create(command);

        String bucket = presignedUploadManager.getBucket();
        Duration ttl = Duration.between(bundle.createdAt(), bundle.expiresAt());
        String presignedUrl =
                presignedUploadManager.generatePresignedUploadUrl(
                        bundle.s3Key(), command.contentType(), ttl);

        SingleUploadSession session =
                bundle.withBucket(bucket).withPresignedUrl(presignedUrl).toSession();

        sessionCommandManager.persist(session);
        sessionExpirationManager.registerExpiration(bundle.expiration());

        return session;
    }
}
