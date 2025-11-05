package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.iam.context.IamContext;
import com.ryuqq.fileflow.application.iam.context.IamContextFacade;
import com.ryuqq.fileflow.application.upload.assembler.UploadSessionAssembler;
import com.ryuqq.fileflow.application.upload.config.PresignedUrlProperties;
import com.ryuqq.fileflow.application.upload.dto.command.InitSingleUploadCommand;
import com.ryuqq.fileflow.application.upload.dto.response.SingleUploadResponse;
import com.ryuqq.fileflow.application.upload.facade.S3PresignedUrlFacade;
import com.ryuqq.fileflow.application.upload.manager.UploadSessionManager;
import com.ryuqq.fileflow.application.upload.port.in.InitSingleUploadUseCase;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionCachePort;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 단일 업로드 초기화 Service
 *
 * <p>100MB 미만 파일의 단일 업로드를 위한 세션을 초기화합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>UploadSession 생성 (SINGLE_UPLOAD 타입)</li>
 *   <li>S3 Presigned PUT URL 발급</li>
 *   <li>클라이언트가 단일 HTTP PUT으로 업로드 가능하도록 지원</li>
 * </ul>
 *
 * <p><strong>리팩토링 개선:</strong></p>
 * <ul>
 *   <li>✅ IamContextFacade: IAM 컨텍스트 통합 조회</li>
 *   <li>✅ S3PresignedUrlFacade: Presigned URL 생성 전담</li>
 *   <li>✅ UploadSessionManager: 세션 저장 및 상태 관리 전담</li>
 *   <li>✅ Thin Service: 오케스트레이션만 담당 (7줄 → 비즈니스 로직 위임)</li>
 * </ul>
 *
 * <p><strong>Transaction 경계:</strong></p>
 * <ul>
 *   <li>✅ Presigned URL 생성: 트랜잭션 밖 (외부 API 호출)</li>
 *   <li>✅ UploadSession 저장: 트랜잭션 내 (UploadSessionManager)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Service
public class InitSingleUploadService implements InitSingleUploadUseCase {

    private final IamContextFacade iamContextFacade;
    private final S3PresignedUrlFacade s3PresignedUrlFacade;
    private final UploadSessionManager uploadSessionManager;
    private final UploadSessionCachePort uploadSessionCachePort;
    private final PresignedUrlProperties presignedUrlProperties;

    /**
     * 생성자
     *
     * @param iamContextFacade IAM Context Facade
     * @param s3PresignedUrlFacade S3 Presigned URL Facade
     * @param uploadSessionManager Upload Session Manager
     * @param uploadSessionCachePort Upload Session Cache Port (Hexagonal Architecture)
     * @param presignedUrlProperties Presigned URL 설정 (Type-Safe Configuration)
     */
    public InitSingleUploadService(
        IamContextFacade iamContextFacade,
        S3PresignedUrlFacade s3PresignedUrlFacade,
        UploadSessionManager uploadSessionManager,
        UploadSessionCachePort uploadSessionCachePort,
        PresignedUrlProperties presignedUrlProperties
    ) {
        this.iamContextFacade = iamContextFacade;
        this.s3PresignedUrlFacade = s3PresignedUrlFacade;
        this.uploadSessionManager = uploadSessionManager;
        this.uploadSessionCachePort = uploadSessionCachePort;
        this.presignedUrlProperties = presignedUrlProperties;
    }

    /**
     * 단일 업로드 초기화
     *
     * <p><strong>개선된 처리 흐름 (Thin Service):</strong></p>
     * <ol>
     *   <li>IAM 컨텍스트 통합 조회 (IamContextFacade)</li>
     *   <li>UploadSession 생성 (Assembler 활용)</li>
     *   <li>Presigned PUT URL 생성 (S3PresignedUrlFacade, 트랜잭션 밖)</li>
     *   <li>UploadSession 저장 (UploadSessionManager, 트랜잭션 내)</li>
     *   <li>Response 생성 (Assembler 활용)</li>
     * </ol>
     *
     * <p><strong>리팩토링 효과:</strong></p>
     * <ul>
     *   <li>✅ validateFileSize() 제거 → Bean Validation으로 이동 (@Max)</li>
     *   <li>✅ loadTenant/Organization/UserContext() 제거 → IamContextFacade</li>
     *   <li>✅ generatePresignedUrl() 제거 → S3PresignedUrlFacade</li>
     *   <li>✅ uploadSessionPort.save() 제거 → UploadSessionManager</li>
     *   <li>✅ Service가 7줄로 단순화 (오케스트레이션만 담당)</li>
     * </ul>
     *
     * <p><strong>규칙 준수:</strong></p>
     * <ul>
     *   <li>✅ SRP: 각 컴포넌트가 단일 책임</li>
     *   <li>✅ DRY: 중복 코드 제거 (Multipart 서비스와 공유)</li>
     *   <li>✅ Law of Demeter: Getter 체이닝 없음</li>
     *   <li>✅ Transaction 경계: 외부 API는 트랜잭션 밖</li>
     * </ul>
     *
     * @param command 단일 업로드 초기화 명령
     * @return 세션 키, Presigned URL, 저장 경로
     */
    @Override
    public SingleUploadResponse execute(InitSingleUploadCommand command) {
        // 1. IAM 컨텍스트 통합 조회
        IamContext iamContext = iamContextFacade.loadContext(
            command.tenantId(),
            command.organizationId(),
            command.userContextId()
        );

        // 2. UploadSession 생성 (Assembler 활용)
        UploadSession session = UploadSessionAssembler.toUploadSession(
            command,
            iamContext.tenant(),
            iamContext.organization(),
            iamContext.userContext()
        );

        // 3. Presigned URL Expiration 설정
        LocalDateTime expiresAt = LocalDateTime.now().plus(presignedUrlProperties.getSingleUploadDuration());
        session.setExpiresAt(expiresAt);

        // 4. Presigned PUT URL 생성 (트랜잭션 밖, S3 외부 API 호출)
        String presignedUrl = s3PresignedUrlFacade.generateSingleUploadUrl(
            iamContext,
            session.getStorageKey(),
            command.contentType()
        );

        // 5. UploadSession 저장 (트랜잭션 내)
        UploadSession savedSession = uploadSessionManager.save(session);

        // 6. Redis 캐시 등록 (TTL 기반 만료 추적)
        uploadSessionCachePort.trackSession(
            savedSession.getSessionKey().value(),
            presignedUrlProperties.getSingleUploadDuration()
        );

        // 7. Response 생성 (Assembler 활용)
        return UploadSessionAssembler.toSingleUploadResponse(
            savedSession,
            presignedUrl
        );
    }


}
