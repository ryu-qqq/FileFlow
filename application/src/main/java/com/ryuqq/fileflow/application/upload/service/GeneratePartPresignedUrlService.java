package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.iam.context.IamContext;
import com.ryuqq.fileflow.application.iam.context.IamContextFacade;
import com.ryuqq.fileflow.application.upload.dto.command.GeneratePartUrlCommand;
import com.ryuqq.fileflow.application.upload.dto.response.PartPresignedUrlResponse;
import com.ryuqq.fileflow.application.upload.facade.S3PresignedUrlFacade;
import com.ryuqq.fileflow.application.upload.manager.MultipartUploadManager;
import com.ryuqq.fileflow.application.upload.port.in.GeneratePartPresignedUrlUseCase;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.domain.upload.MultipartUpload;
import com.ryuqq.fileflow.domain.upload.SessionKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * 파트 업로드 URL 생성 Service
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Service
public class GeneratePartPresignedUrlService implements GeneratePartPresignedUrlUseCase {

    private static final Duration URL_EXPIRATION = Duration.ofHours(1);

    private final UploadSessionPort uploadSessionPort;
    private final MultipartUploadManager multipartUploadManager;
    private final IamContextFacade iamContextFacade;
    private final S3PresignedUrlFacade s3PresignedUrlFacade;

    public GeneratePartPresignedUrlService(
        UploadSessionPort uploadSessionPort,
        MultipartUploadManager multipartUploadManager,
        IamContextFacade iamContextFacade,
        S3PresignedUrlFacade s3PresignedUrlFacade
    ) {
        this.uploadSessionPort = uploadSessionPort;
        this.multipartUploadManager = multipartUploadManager;
        this.iamContextFacade = iamContextFacade;
        this.s3PresignedUrlFacade = s3PresignedUrlFacade;
    }

    @Transactional(readOnly = true)
    @Override
    public PartPresignedUrlResponse execute(GeneratePartUrlCommand command) {
        // 1. 업로드 세션 조회 (트랜잭션 내)
        UploadSession session = findUploadSession(command.sessionKey());

        // 2. Multipart 정보 조회
        MultipartUpload multipart = findMultipartUpload(session.getIdValue());

        // 3. Multipart 상태 검증
        validateMultipartState(multipart, command.partNumber());

        // 4. Presigned URL 생성 (트랜잭션 밖)
        String presignedUrl = generatePresignedUrl(
            session,
            multipart,
            command.partNumber()
        );

        return buildResponse(command.partNumber(), presignedUrl);
    }

    /**
     * 업로드 세션 조회
     *
     * <p>⭐ Read-only 트랜잭션</p>
     *
     * @param sessionKey 세션 키
     * @return UploadSession
     */
    public UploadSession findUploadSession(String sessionKey) {
        return uploadSessionPort.findBySessionKey(SessionKey.of(sessionKey))
            .orElseThrow(() ->
                new IllegalArgumentException("Upload session not found: " + sessionKey)
            );
    }

    /**
     * Multipart Upload 조회
     *
     * @param sessionId 세션 ID
     * @return MultipartUpload
     */
    public MultipartUpload findMultipartUpload(Long sessionId) {
        return multipartUploadManager.findByUploadSessionId(sessionId)
            .orElseThrow(() ->
                new IllegalStateException("Not a multipart upload")
            );
    }

    /**
     * Multipart 상태 검증
     *
     * @param multipart MultipartUpload
     * @param partNumber 파트 번호
     * @throws IllegalStateException Multipart가 진행 중이 아닌 경우
     * @throws IllegalArgumentException 파트 번호가 유효하지 않은 경우
     */
    private void validateMultipartState(
        MultipartUpload multipart,
        Integer partNumber
    ) {
        if (!multipart.isInProgress()) {
            throw new IllegalStateException(
                "Multipart not in progress: " + multipart.getStatus()
            );
        }

        if (partNumber < 1 || partNumber > multipart.getTotalParts().value()) {
            throw new IllegalArgumentException(
                "Invalid part number: " + partNumber +
                " (valid range: 1-" + multipart.getTotalParts().value() + ")"
            );
        }
    }

    /**
     * Presigned URL 생성
     *
     * <p>⭐ 트랜잭션 밖에서 실행 (외부 API 호출)</p>
     *
     * @param session UploadSession
     * @param multipart MultipartUpload
     * @param partNumber 파트 번호
     * @return Presigned URL
     */
    private String generatePresignedUrl(
        UploadSession session,
        MultipartUpload multipart,
        Integer partNumber
    ) {
        // 1. IAM Context 조회
        // UploadSession은 organizationId, userContextId를 저장하지 않음
        // IamContextFacade.loadContext는 이들을 Optional로 받음 (null 허용)
        IamContext iamContext = iamContextFacade.loadContext(
            session.getTenantId(),
            null, // organizationId는 UploadSession에 저장되지 않음
            null  // userContextId는 UploadSession에 저장되지 않음
        );

        // 2. S3PresignedUrlFacade 호출 (Facade가 Bucket 이름 결정 담당)
        return s3PresignedUrlFacade.generateMultipartUploadUrl(
            iamContext,
            session.getStorageKey(),
            multipart.getProviderUploadIdValue(),
            partNumber
        );
    }

    /**
     * Response 생성
     *
     * @param partNumber 파트 번호
     * @param presignedUrl Presigned URL
     * @return PartPresignedUrlResponse
     */
    private PartPresignedUrlResponse buildResponse(
        Integer partNumber,
        String presignedUrl
    ) {
        return PartPresignedUrlResponse.of(
            partNumber,
            presignedUrl,
            URL_EXPIRATION
        );
    }
}
