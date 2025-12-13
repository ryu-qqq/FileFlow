package com.ryuqq.fileflow.application.session.facade;

import com.ryuqq.fileflow.application.common.config.TransactionEventRegistry;
import com.ryuqq.fileflow.application.session.factory.command.UploadSessionCommandFactory;
import com.ryuqq.fileflow.application.session.manager.UploadSessionCacheManager;
import com.ryuqq.fileflow.application.session.manager.command.CompletedPartTransactionManager;
import com.ryuqq.fileflow.application.session.manager.command.MultipartUploadSessionTransactionManager;
import com.ryuqq.fileflow.application.session.manager.command.SingleUploadSessionTransactionManager;
import com.ryuqq.fileflow.application.session.port.out.client.SessionS3ClientPort;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.event.FileUploadCompletedEvent;
import com.ryuqq.fileflow.domain.session.vo.PresignedUrl;
import com.ryuqq.fileflow.domain.session.vo.S3UploadId;
import com.ryuqq.fileflow.domain.session.vo.S3UploadMetadata;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 업로드 세션 Facade.
 *
 * <p>세션 생성, 저장, 활성화, Presigned URL 발급을 조율합니다.
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>세션 생성 및 저장 (RDB + Redis Cache)
 *   <li>S3 Presigned URL 발급
 *   <li>세션 활성화 (PREPARING → ACTIVE)
 * </ul>
 *
 * <p><strong>트랜잭션 전략</strong>:
 *
 * <ul>
 *   <li>RDB: Manager를 통한 트랜잭션 처리
 *   <li>Redis: CacheManager를 통한 Best-Effort 캐싱 (실패 시 RDB 폴백)
 *   <li>S3: 외부 API (트랜잭션 외부, 멱등성 보장)
 * </ul>
 */
@Component
public class UploadSessionFacade {

    private static final Logger log = LoggerFactory.getLogger(UploadSessionFacade.class);
    private static final Duration SINGLE_UPLOAD_TTL = Duration.ofMinutes(15);
    private static final Duration MULTIPART_UPLOAD_TTL = Duration.ofHours(24);
    private static final Duration PART_URL_EXPIRATION = Duration.ofHours(24);

    private final SingleUploadSessionTransactionManager singleUploadSessionTransactionManager;
    private final MultipartUploadSessionTransactionManager multipartUploadSessionTransactionManager;
    private final CompletedPartTransactionManager completedPartTransactionManager;
    private final UploadSessionCacheManager uploadSessionCacheManager;
    private final SessionS3ClientPort sessionS3ClientPort;
    private final TransactionEventRegistry transactionEventRegistry;
    private final UploadSessionCommandFactory commandFactory;

    public UploadSessionFacade(
            SingleUploadSessionTransactionManager singleUploadSessionTransactionManager,
            MultipartUploadSessionTransactionManager multipartUploadSessionTransactionManager,
            CompletedPartTransactionManager completedPartTransactionManager,
            UploadSessionCacheManager uploadSessionCacheManager,
            SessionS3ClientPort sessionS3ClientPort,
            TransactionEventRegistry transactionEventRegistry,
            UploadSessionCommandFactory commandFactory) {
        this.singleUploadSessionTransactionManager = singleUploadSessionTransactionManager;
        this.multipartUploadSessionTransactionManager = multipartUploadSessionTransactionManager;
        this.completedPartTransactionManager = completedPartTransactionManager;
        this.uploadSessionCacheManager = uploadSessionCacheManager;
        this.sessionS3ClientPort = sessionS3ClientPort;
        this.transactionEventRegistry = transactionEventRegistry;
        this.commandFactory = commandFactory;
    }

    /**
     * S3 Multipart Upload를 초기화하고 Upload ID를 반환합니다.
     *
     * <p>S3OutPort를 통해 Multipart Upload를 시작하고 S3가 발급한 Upload ID를 반환합니다.
     *
     * @param s3Metadata S3 업로드 메타데이터 (Bucket, S3Key, ContentType)
     * @return S3UploadId (S3가 발급한 Upload ID)
     */
    public S3UploadId initiateMultipartUpload(S3UploadMetadata s3Metadata) {
        String uploadId =
                sessionS3ClientPort.initiateMultipartUpload(
                        s3Metadata.getBucket(), s3Metadata.getS3Key(), s3Metadata.getContentType());

        return S3UploadId.of(uploadId);
    }

    /**
     * 신규 단일 업로드 세션을 생성, 활성화, 저장합니다.
     *
     * <p><strong>처리 순서</strong>:
     *
     * <ol>
     *   <li>PREPARING 상태 세션을 RDB에 저장
     *   <li>Presigned URL 발급 및 세션 활성화 (내부에서 처리)
     *   <li>ACTIVE 상태 세션을 RDB에 업데이트
     *   <li>Redis 캐싱 (TTL 기반 자동 만료용)
     * </ol>
     *
     * @param session 생성할 세션 (status: PREPARING)
     * @return 활성화된 세션 (status: ACTIVE, presignedUrl 포함)
     */
    public SingleUploadSession createAndActivateSingleUpload(SingleUploadSession session) {
        // Step 1: PREPARING 상태로 RDB 저장 (ID 발급)
        SingleUploadSession preparedSession =
                singleUploadSessionTransactionManager.persist(session);

        // Step 2: Presigned URL 발급 및 활성화 (내부에서 activate 호출)
        SingleUploadSession activatedSession = generateSingleUploadPresignedUrl(preparedSession);

        // Step 3: ACTIVE 상태로 RDB 업데이트
        SingleUploadSession savedSession =
                singleUploadSessionTransactionManager.persist(activatedSession);

        // Step 4: Redis 캐싱 (TTL 자동 만료용, 한 번만 저장)
        uploadSessionCacheManager.cacheSingleUpload(savedSession, SINGLE_UPLOAD_TTL);

        return savedSession;
    }

    /**
     * 신규 Multipart 업로드 세션을 생성, 활성화, 저장합니다.
     *
     * <p><strong>처리 순서</strong>:
     *
     * <ol>
     *   <li>PREPARING 상태 세션을 RDB에 저장 (ID 발급)
     *   <li>세션 활성화 (PREPARING → ACTIVE)
     *   <li>ACTIVE 상태 세션을 RDB에 업데이트
     *   <li>Redis 캐싱 (TTL 기반 자동 만료용)
     * </ol>
     *
     * <p><strong>Part Presigned URLs 처리</strong>:
     *
     * <ul>
     *   <li>Part URLs는 Domain에 저장하지 않음 (일시적 데이터)
     *   <li>초기화 Response로만 클라이언트에게 전달
     *   <li>클라이언트가 Part URLs로 직접 S3에 업로드
     *   <li>완료 시 클라이언트가 ETag 리스트를 서버에 전달
     *   <li>서버는 S3 Upload ID + ETag로 S3에 병합 요청
     * </ul>
     *
     * <p><strong>Single Upload와의 일관성</strong>:
     *
     * <ul>
     *   <li>Single: RDB 저장 → Presigned URL 발급 → 활성화 → RDB 업데이트 → Redis
     *   <li>Multipart: RDB 저장 → 활성화 → RDB 업데이트 → Redis (Part URL은 Service에서 생성)
     *   <li>공통: RDB 저장 → 활성화 → RDB 업데이트 → Redis
     * </ul>
     *
     * @param session 생성할 세션 (status: PREPARING, S3 Upload ID 포함)
     * @return 활성화된 세션 (status: ACTIVE)
     */
    public MultipartUploadSession createAndActivateMultipartUpload(MultipartUploadSession session) {
        // Step 1: PREPARING 상태로 RDB 저장 (ID 발급)
        MultipartUploadSession preparedSession =
                multipartUploadSessionTransactionManager.persist(session);

        // Step 2: 세션 활성화 (PREPARING → ACTIVE)
        preparedSession.activate();

        // Step 3: ACTIVE 상태로 RDB 업데이트
        MultipartUploadSession savedSession =
                multipartUploadSessionTransactionManager.persist(preparedSession);

        // Step 4: Part별 Presigned URL 생성 및 CompletedPart 초기화 (CommandFactory 사용)
        List<CompletedPart> initialParts =
                commandFactory.createInitialCompletedParts(
                        savedSession,
                        (bucket, s3Key, uploadId, partNumber) ->
                                sessionS3ClientPort.generatePresignedUploadPartUrl(
                                        bucket, s3Key, uploadId, partNumber, PART_URL_EXPIRATION));

        // Step 5: 초기화된 파트들 RDB 저장 (한 트랜잭션)
        completedPartTransactionManager.persistAll(savedSession.getId(), initialParts);

        // Step 6: Redis 캐싱 (TTL 자동 만료용)
        uploadSessionCacheManager.cacheMultipartUpload(savedSession, MULTIPART_UPLOAD_TTL);

        return savedSession;
    }

    /**
     * Presigned URL을 발급하고 세션을 활성화합니다.
     *
     * @param session PREPARING 상태 세션
     * @return ACTIVE 상태 세션 (presignedUrl 포함)
     */
    private SingleUploadSession generateSingleUploadPresignedUrl(SingleUploadSession session) {
        // S3 Presigned URL 발급
        String presignedUrlString =
                sessionS3ClientPort.generatePresignedPutUrl(
                        session.getBucket(),
                        session.getS3Key(),
                        session.getContentType(),
                        SINGLE_UPLOAD_TTL);

        // PresignedUrl VO 생성
        PresignedUrl presignedUrl = PresignedUrl.of(presignedUrlString);

        // 세션 활성화 (PREPARING → ACTIVE)
        commandFactory.activateSingleUpload(session, presignedUrl);

        return session;
    }

    // ==================== 완료 처리 및 이벤트 발행 ====================

    /**
     * SingleUploadSession 저장 후 도메인 이벤트 등록.
     *
     * <p>세션 저장 후 도메인에서 생성된 이벤트를 커밋 후 발행하도록 등록합니다 (APP-ER-002, APP-ER-005).
     *
     * <p><strong>캐시 삭제</strong>: 완료된 세션은 Redis에서 즉시 삭제하여 불필요한 TTL 만료 이벤트를 방지합니다.
     *
     * @param session 완료 처리된 세션
     * @return 저장된 세션
     */
    public SingleUploadSession saveAndPublishEvents(SingleUploadSession session) {
        // 1. 이벤트 추출 (저장 전에 추출)
        List<FileUploadCompletedEvent> events = session.pollDomainEvents();
        log.debug(
                "Extracted {} domain events from session: {}", events.size(), session.getIdValue());

        // 2. 세션 저장
        SingleUploadSession savedSession = singleUploadSessionTransactionManager.persist(session);

        // 3. Redis 캐시 삭제 (완료된 세션은 불필요한 만료 이벤트 방지)
        if (session.isCompleted()) {
            uploadSessionCacheManager.deleteSingleUploadSession(session.getId());
        }

        // 4. 이벤트 등록 (커밋 후 발행)
        for (FileUploadCompletedEvent event : events) {
            log.info(
                    "Registering FileUploadCompletedEvent: sessionId={}, bucket={}, key={}",
                    event.sessionId().getValue(),
                    event.bucket().bucketName(),
                    event.s3Key().key());
            transactionEventRegistry.registerObjectForPublish(event);
        }

        return savedSession;
    }

    /**
     * MultipartUploadSession 저장 후 도메인 이벤트 등록.
     *
     * <p>세션 저장 후 도메인에서 생성된 이벤트를 커밋 후 발행하도록 등록합니다 (APP-ER-002, APP-ER-005).
     *
     * @param session 완료 처리된 세션
     * @return 저장된 세션
     */
    public MultipartUploadSession saveAndPublishEvents(MultipartUploadSession session) {
        // 1. 이벤트 추출 (저장 전에 추출)
        List<FileUploadCompletedEvent> events = session.pollDomainEvents();

        // 2. 세션 저장
        MultipartUploadSession savedSession =
                multipartUploadSessionTransactionManager.persist(session);

        // 3. 이벤트 등록 (커밋 후 발행)
        for (FileUploadCompletedEvent event : events) {
            transactionEventRegistry.registerObjectForPublish(event);
        }

        return savedSession;
    }
}
