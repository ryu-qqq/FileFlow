package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.policy.port.in.ValidateUploadPolicyUseCase;
import com.ryuqq.fileflow.application.upload.dto.CreateUploadSessionCommand;
import com.ryuqq.fileflow.application.upload.dto.PresignedUrlResponse;
import com.ryuqq.fileflow.application.upload.dto.UploadSessionResponse;
import com.ryuqq.fileflow.application.upload.port.in.CancelUploadSessionUseCase;
import com.ryuqq.fileflow.application.upload.port.in.CompleteUploadSessionUseCase;
import com.ryuqq.fileflow.application.upload.port.in.CreateUploadSessionUseCase;
import com.ryuqq.fileflow.application.upload.port.in.GetUploadSessionUseCase;
import com.ryuqq.fileflow.application.upload.port.out.PresignedUrlGenerator;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionRepository;
import com.ryuqq.fileflow.domain.policy.FileType;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException;
import com.ryuqq.fileflow.domain.upload.model.PresignedUrlInfo;
import com.ryuqq.fileflow.domain.upload.model.UploadRequest;
import com.ryuqq.fileflow.domain.upload.model.UploadSession;

import java.util.Objects;

/**
 * 업로드 세션 관리 Service
 *
 * Hexagonal Architecture의 UseCase 구현체로서,
 * 업로드 세션 생성, 조회, 완료, 취소 비즈니스 로직을 처리합니다.
 *
 * @author sangwon-ryu
 */
public class UploadSessionService implements
        CreateUploadSessionUseCase,
        GetUploadSessionUseCase,
        CompleteUploadSessionUseCase,
        CancelUploadSessionUseCase {

    private static final int SINGLE_FILE_UPLOAD_COUNT = 1;

    private final UploadSessionRepository uploadSessionRepository;
    private final PresignedUrlGenerator presignedUrlGenerator;
    private final ValidateUploadPolicyUseCase validateUploadPolicyUseCase;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param uploadSessionRepository 세션 저장소
     * @param presignedUrlGenerator Presigned URL 생성기
     * @param validateUploadPolicyUseCase 정책 검증 UseCase
     */
    public UploadSessionService(
            UploadSessionRepository uploadSessionRepository,
            PresignedUrlGenerator presignedUrlGenerator,
            ValidateUploadPolicyUseCase validateUploadPolicyUseCase
    ) {
        this.uploadSessionRepository = Objects.requireNonNull(
                uploadSessionRepository,
                "UploadSessionRepository must not be null"
        );
        this.presignedUrlGenerator = Objects.requireNonNull(
                presignedUrlGenerator,
                "PresignedUrlGenerator must not be null"
        );
        this.validateUploadPolicyUseCase = Objects.requireNonNull(
                validateUploadPolicyUseCase,
                "ValidateUploadPolicyUseCase must not be null"
        );
    }

    /**
     * 새로운 업로드 세션을 생성하고 Presigned URL을 발급합니다.
     *
     * 비즈니스 로직:
     * 1. 정책 검증 (Epic 1 정책 검증 UseCase 사용)
     * 2. UploadSession 도메인 객체 생성
     * 3. Presigned URL 발급
     * 4. 세션 정보 저장
     *
     * @param command 세션 생성 Command
     * @return 생성된 세션 정보와 Presigned URL
     * @throws IllegalArgumentException command가 null이거나 유효하지 않은 경우
     * @throws com.ryuqq.fileflow.domain.policy.exception.PolicyNotFoundException 정책을 찾을 수 없는 경우
     * @throws com.ryuqq.fileflow.domain.policy.exception.PolicyViolationException 정책 검증 실패 시
     */
    @Override
    public UploadSessionWithUrlResponse createSession(CreateUploadSessionCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("CreateUploadSessionCommand must not be null");
        }

        // 1. FileType 추출
        FileType fileType = FileType.fromContentType(command.contentType());

        // 2. 정책 검증 (Epic 1)
        PolicyKey policyKey = command.getPolicyKey();
        ValidateUploadPolicyUseCase.ValidateUploadPolicyCommand validateCommand =
                new ValidateUploadPolicyUseCase.ValidateUploadPolicyCommand(
                        policyKey.getTenantId(),
                        policyKey.getUserType(),
                        policyKey.getServiceType(),
                        fileType,
                        extractFileFormat(command.fileName()),
                        command.fileSize(),
                        SINGLE_FILE_UPLOAD_COUNT,
                        null, // Rate limiting은 별도로 체크
                        null
                );
        validateUploadPolicyUseCase.validate(validateCommand);

        // 3. UploadRequest 생성
        UploadRequest uploadRequest = UploadRequest.of(
                command.fileName(),
                fileType,
                command.fileSize(),
                command.contentType()
        );

        // 3. UploadSession 생성
        UploadSession session = UploadSession.create(
                policyKey,
                uploadRequest,
                command.uploaderId(),
                command.expirationMinutes()
        );

        // 4. Presigned URL 발급
        PresignedUrlInfo presignedUrlInfo = presignedUrlGenerator.generatePresignedUrl(
                command.fileName(),
                command.contentType(),
                command.expirationMinutes()
        );

        // 5. 세션 저장
        UploadSession savedSession = uploadSessionRepository.save(session);

        // 6. Response 생성
        return new UploadSessionWithUrlResponse(
                UploadSessionResponse.from(savedSession),
                PresignedUrlResponse.from(presignedUrlInfo)
        );
    }

    /**
     * 세션 ID로 업로드 세션을 조회합니다.
     *
     * @param sessionId 세션 ID
     * @return 업로드 세션 정보
     * @throws IllegalArgumentException sessionId가 null이거나 빈 문자열인 경우
     * @throws UploadSessionNotFoundException 세션을 찾을 수 없는 경우
     */
    @Override
    public UploadSessionResponse getSession(String sessionId) {
        validateSessionId(sessionId);

        UploadSession session = uploadSessionRepository.findById(sessionId)
                .orElseThrow(() -> new UploadSessionNotFoundException(sessionId));

        return UploadSessionResponse.from(session);
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
    public UploadSessionResponse completeSession(String sessionId) {
        validateSessionId(sessionId);

        UploadSession session = uploadSessionRepository.findById(sessionId)
                .orElseThrow(() -> new UploadSessionNotFoundException(sessionId));

        // 도메인 로직으로 상태 전이 (검증 포함)
        UploadSession completedSession = session.complete();

        // 변경된 세션 저장
        UploadSession savedSession = uploadSessionRepository.save(completedSession);

        return UploadSessionResponse.from(savedSession);
    }

    /**
     * 업로드 세션을 취소합니다.
     *
     * @param sessionId 세션 ID
     * @return 취소된 세션 정보
     * @throws IllegalArgumentException sessionId가 null이거나 빈 문자열인 경우
     * @throws UploadSessionNotFoundException 세션을 찾을 수 없는 경우
     * @throws IllegalStateException 취소 불가능한 상태인 경우
     */
    @Override
    public UploadSessionResponse cancelSession(String sessionId) {
        validateSessionId(sessionId);

        UploadSession session = uploadSessionRepository.findById(sessionId)
                .orElseThrow(() -> new UploadSessionNotFoundException(sessionId));

        // 도메인 로직으로 상태 전이 (검증 포함)
        UploadSession cancelledSession = session.cancel();

        // 변경된 세션 저장
        UploadSession savedSession = uploadSessionRepository.save(cancelledSession);

        return UploadSessionResponse.from(savedSession);
    }

    // ========== Validation Methods ==========

    private void validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("SessionId must not be null or empty");
        }
    }

    // ========== Helper Methods ==========

    /**
     * 파일명에서 파일 포맷을 추출합니다.
     *
     * @param fileName 파일명
     * @return 파일 포맷 (확장자)
     */
    private String extractFileFormat(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
}
