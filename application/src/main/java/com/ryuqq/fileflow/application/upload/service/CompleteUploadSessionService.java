package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.common.port.out.DomainEventPublisher;
import com.ryuqq.fileflow.application.upload.dto.UploadSessionResponse;
import com.ryuqq.fileflow.application.upload.port.in.CompleteUploadSessionUseCase;
import com.ryuqq.fileflow.application.upload.port.out.SaveFileAssetPort;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.event.UploadCompletedEvent;
import com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException;
import com.ryuqq.fileflow.domain.upload.vo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 업로드 세션 완료 Service
 *
 * Hexagonal Architecture의 UseCase 구현체로서,
 * 업로드 세션을 완료 처리하고 FileAsset을 생성합니다.
 *
 * 처리 흐름:
 * 1. 세션 조회
 * 2. 세션 상태를 COMPLETED로 전환
 * 3. FileAsset 생성 및 저장
 * 4. 세션 저장
 * 5. UploadCompletedEvent 발행
 *
 * @author sangwon-ryu
 */
@Service
public class CompleteUploadSessionService implements CompleteUploadSessionUseCase {

    private final UploadSessionPort uploadSessionPort;
    private final SaveFileAssetPort saveFileAssetPort;
    private final DomainEventPublisher eventPublisher;
    private final String s3BucketName;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param uploadSessionPort 세션 저장소
     * @param saveFileAssetPort 파일 자산 저장소
     * @param eventPublisher 도메인 이벤트 발행자
     * @param s3BucketName S3 버킷명 (외부 설정에서 주입)
     */
    public CompleteUploadSessionService(
            UploadSessionPort uploadSessionPort,
            SaveFileAssetPort saveFileAssetPort,
            DomainEventPublisher eventPublisher,
            String s3BucketName
    ) {
        this.uploadSessionPort = Objects.requireNonNull(
                uploadSessionPort,
                "UploadSessionPort must not be null"
        );
        this.saveFileAssetPort = Objects.requireNonNull(
                saveFileAssetPort,
                "SaveFileAssetPort must not be null"
        );
        this.eventPublisher = Objects.requireNonNull(
                eventPublisher,
                "DomainEventPublisher must not be null"
        );
        this.s3BucketName = Objects.requireNonNull(
                s3BucketName,
                "s3BucketName must not be null"
        );
    }

    /**
     * 업로드 세션을 완료 처리합니다.
     *
     * @param sessionId 세션 ID
     * @return 완료된 세션 정보
     * @throws IllegalArgumentException sessionId가 null이거나 빈 문자열인 경우
     * @throws UploadSessionNotFoundException 세션을 찾을 수 없는 경우
     * @throws IllegalStateException 완료 가능한 상태가 아니거나 세션이 만료된 경우
     */
    @Override
    @Transactional
    public UploadSessionResponse completeSession(String sessionId) {
        // 1. 세션 조회
        UploadSession session = findSessionById(sessionId);

        // 2. 세션 상태를 COMPLETED로 전환
        UploadSession completedSession = session.complete();

        // 3. FileAsset 생성
        FileAsset fileAsset = createFileAsset(completedSession);

        // 4. FileAsset 저장
        FileAsset savedFileAsset = saveFileAssetPort.save(fileAsset);

        // 5. 세션 저장
        UploadSession savedSession = uploadSessionPort.save(completedSession);

        // 6. UploadCompletedEvent 발행
        UploadCompletedEvent event = UploadCompletedEvent.of(
                savedSession.getSessionId(),
                savedSession.getUploaderId(),
                savedFileAsset.getFileId().value(),
                savedFileAsset.getS3Uri()
        );
        eventPublisher.publish(event);

        // 7. Response 생성
        return UploadSessionResponse.from(savedSession);
    }

    // ========== Helper Methods ==========

    /**
     * 세션 ID를 검증하고 세션을 조회합니다.
     *
     * @param sessionId 세션 ID
     * @return 조회된 UploadSession
     * @throws IllegalArgumentException sessionId가 null이거나 빈 문자열인 경우
     * @throws UploadSessionNotFoundException 세션을 찾을 수 없는 경우
     */
    private UploadSession findSessionById(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("SessionId must not be null or empty");
        }
        return uploadSessionPort.findById(sessionId)
                .orElseThrow(() -> new UploadSessionNotFoundException(sessionId));
    }

    /**
     * UploadSession으로부터 FileAsset을 생성합니다.
     *
     * @param session 업로드 세션
     * @return 생성된 FileAsset
     */
    private FileAsset createFileAsset(UploadSession session) {
        // S3 Location 생성
        String s3Key = buildS3Key(session);
        S3Location s3Location = S3Location.of(s3BucketName, s3Key);

        // TenantId 생성
        TenantId tenantId = TenantId.of(session.getPolicyKey().getTenantId());

        // CheckSum 생성 (실제로는 S3에서 조회하거나 세션에 저장되어 있어야 함)
        // TODO: 실제 구현에서는 업로드된 파일의 체크섬을 가져와야 함
        CheckSum checksum = session.getUploadRequest().checksum(); // 요청에 포함된 체크섬 사용 (null 가능)

        // FileSize 생성
        FileSize fileSize = FileSize.ofBytes(session.getUploadRequest().fileSizeBytes());

        // ContentType 생성
        ContentType contentType = ContentType.of(session.getUploadRequest().contentType());

        return FileAsset.create(
                session.getSessionId(),
                tenantId,
                s3Location,
                checksum,
                fileSize,
                contentType
        );
    }

    /**
     * S3 객체 키를 생성합니다.
     *
     * @param session 업로드 세션
     * @return S3 객체 키
     */
    private String buildS3Key(UploadSession session) {
        return String.join(
                "/",
                session.getPolicyKey().getTenantId(),
                session.getSessionId(),
                session.getUploadRequest().fileName()
        );
    }
}
