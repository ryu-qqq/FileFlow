package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.iam.context.IamContext;
import com.ryuqq.fileflow.application.iam.context.IamContextFacade;
import com.ryuqq.fileflow.application.upload.assembler.MultipartUploadAssembler;
import com.ryuqq.fileflow.application.upload.config.PresignedUrlProperties;
import com.ryuqq.fileflow.application.upload.dto.command.InitMultipartCommand;
import com.ryuqq.fileflow.application.upload.dto.response.InitMultipartResponse;
import com.ryuqq.fileflow.application.upload.dto.response.S3InitResultResponse;
import com.ryuqq.fileflow.application.upload.facade.S3MultipartFacade;
import com.ryuqq.fileflow.application.upload.manager.MultipartUploadManager;
import com.ryuqq.fileflow.application.upload.manager.UploadSessionManager;
import com.ryuqq.fileflow.application.upload.port.in.InitMultipartUploadUseCase;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionCachePort;
import com.ryuqq.fileflow.domain.upload.MultipartUpload;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Multipart 업로드 초기화 Service
 *
 * <p>100MB 이상 대용량 파일의 Multipart 업로드를 위한 세션을 초기화합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>UploadSession 생성 (MULTIPART_UPLOAD 타입)</li>
 *   <li>S3 Multipart Upload 초기화</li>
 *   <li>MultipartUpload 상태 관리</li>
 *   <li>파트별 업로드 URL 생성 준비</li>
 * </ul>
 *
 * <p><strong>리팩토링 개선:</strong></p>
 * <ul>
 *   <li>✅ IamContextFacade: IAM 컨텍스트 통합 조회</li>
 *   <li>✅ S3MultipartFacade: S3 Multipart 초기화 전담</li>
 *   <li>✅ UploadSessionManager: 세션 저장 및 상태 관리 전담</li>
 *   <li>✅ MultipartUploadManager: MultipartUpload 저장 및 상태 관리 전담</li>
 *   <li>✅ MultipartUploadAssembler: DTO-Domain 변환 전담</li>
 *   <li>✅ Thin Service: 오케스트레이션만 담당 (Private 메서드 제거)</li>
 * </ul>
 *
 * <p><strong>Transaction 경계:</strong></p>
 * <ul>
 *   <li>✅ S3 Multipart 초기화: 트랜잭션 밖 (외부 API 호출)</li>
 *   <li>✅ UploadSession/MultipartUpload 저장: 트랜잭션 내 (Manager)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Service
public class InitMultipartUploadService implements InitMultipartUploadUseCase {

    private final IamContextFacade iamContextFacade;
    private final S3MultipartFacade s3MultipartFacade;
    private final UploadSessionManager uploadSessionManager;
    private final MultipartUploadManager multipartUploadManager;
    private final UploadSessionCachePort uploadSessionCachePort;
    private final PresignedUrlProperties presignedUrlProperties;

    /**
     * 생성자
     *
     * @param iamContextFacade IAM Context Facade
     * @param s3MultipartFacade S3 Multipart Facade
     * @param uploadSessionManager Upload Session Manager
     * @param multipartUploadManager Multipart Upload Manager
     * @param uploadSessionCachePort Upload Session Cache Port (Hexagonal Architecture)
     * @param presignedUrlProperties Presigned URL 설정 (Type-Safe Configuration)
     */
    public InitMultipartUploadService(
        IamContextFacade iamContextFacade,
        S3MultipartFacade s3MultipartFacade,
        UploadSessionManager uploadSessionManager,
        MultipartUploadManager multipartUploadManager,
        UploadSessionCachePort uploadSessionCachePort,
        PresignedUrlProperties presignedUrlProperties
    ) {
        this.iamContextFacade = iamContextFacade;
        this.s3MultipartFacade = s3MultipartFacade;
        this.uploadSessionManager = uploadSessionManager;
        this.multipartUploadManager = multipartUploadManager;
        this.uploadSessionCachePort = uploadSessionCachePort;
        this.presignedUrlProperties = presignedUrlProperties;
    }

    /**
     * Multipart 업로드 초기화
     *
     * <p><strong>개선된 처리 흐름 (Thin Service):</strong></p>
     * <ol>
     *   <li>IAM 컨텍스트 통합 조회 (IamContextFacade)</li>
     *   <li>UploadSession 생성 (Assembler 활용)</li>
     *   <li>UploadSession 저장 (UploadSessionManager, 트랜잭션 내)</li>
     *   <li>S3 Multipart 초기화 (S3MultipartFacade, 트랜잭션 밖)</li>
     *   <li>MultipartUpload 생성 및 저장 (Assembler + Manager, 트랜잭션 내)</li>
     *   <li>Response 생성 (Assembler 활용)</li>
     * </ol>
     *
     * <p><strong>리팩토링 효과:</strong></p>
     * <ul>
     *   <li>✅ generateStorageKey() 제거 → StorageContext.generateStorageKey()</li>
     *   <li>✅ determineBucket() 제거 → StorageContext.generateBucketName()</li>
     *   <li>✅ calculatePartCount() 제거 → S3MultipartFacade</li>
     *   <li>✅ loadTenant/Organization/UserContext() 제거 → IamContextFacade</li>
     *   <li>✅ uploadSessionPort.save() 제거 → UploadSessionManager</li>
     *   <li>✅ multipartUploadPort.save() 제거 → MultipartUploadManager</li>
     *   <li>✅ Service가 10줄로 단순화 (오케스트레이션만 담당)</li>
     * </ul>
     *
     * <p><strong>규칙 준수:</strong></p>
     * <ul>
     *   <li>✅ SRP: 각 컴포넌트가 단일 책임</li>
     *   <li>✅ DRY: 중복 코드 제거 (Single 서비스와 공유)</li>
     *   <li>✅ Law of Demeter: Getter 체이닝 없음</li>
     *   <li>✅ Transaction 경계: 외부 API는 트랜잭션 밖</li>
     * </ul>
     *
     * @param command Multipart 업로드 초기화 명령
     * @return 세션 키, Upload ID, 파트 수, 저장 경로
     */
    @Override
    public InitMultipartResponse execute(InitMultipartCommand command) {
        // 1. IAM 컨텍스트 통합 조회
        IamContext iamContext = iamContextFacade.loadContext(
            command.tenantId(),
            command.organizationId(),
            command.userContextId()
        );

        // 2. UploadSession 생성 (Assembler 활용)
        UploadSession session = MultipartUploadAssembler.toUploadSession(
            command,
            iamContext.tenant(),
            iamContext.organization(),
            iamContext.userContext()
        );

        // 3. Presigned URL Expiration 설정
        LocalDateTime expiresAt = LocalDateTime.now().plus(presignedUrlProperties.getMultipartPartDuration());
        session.setExpiresAt(expiresAt);

        // 4. UploadSession 저장 (트랜잭션 내)
        UploadSession savedSession = uploadSessionManager.save(session);

        // 5. S3 Multipart 초기화 (트랜잭션 밖, S3 외부 API 호출)
        S3InitResultResponse s3Result = s3MultipartFacade.initializeMultipart(
            iamContext,
            savedSession.getStorageKey(),
            command.fileName(),
            command.fileSize(),
            command.contentType()
        );

        // 6. MultipartUpload 생성 및 저장 (트랜잭션 내)
        MultipartUpload multipartUpload = MultipartUploadAssembler.toMultipartUpload(
            savedSession,
            s3Result.uploadId(),
            s3Result.partCount()
        );
        multipartUploadManager.save(multipartUpload);

        // 7. Redis 캐시 등록 (TTL 기반 만료 추적)
        uploadSessionCachePort.trackSession(
            savedSession.getSessionKey().value(),
            presignedUrlProperties.getMultipartPartDuration()
        );

        // 8. Response 생성 (Assembler 활용)
        return MultipartUploadAssembler.toInitMultipartResponse(
            savedSession,
            multipartUpload
        );
    }
}
