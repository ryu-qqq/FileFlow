package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.policy.port.in.ValidateUploadPolicyUseCase;
import com.ryuqq.fileflow.application.upload.dto.CreateUploadSessionCommand;
import com.ryuqq.fileflow.application.upload.dto.PresignedUrlResponse;
import com.ryuqq.fileflow.application.upload.dto.UploadSessionResponse;
import com.ryuqq.fileflow.application.upload.port.in.CancelUploadSessionUseCase;
import com.ryuqq.fileflow.application.upload.port.in.CompleteUploadSessionUseCase;
import com.ryuqq.fileflow.application.upload.port.in.CreateUploadSessionUseCase;
import com.ryuqq.fileflow.application.upload.port.in.GetUploadSessionUseCase;
import com.ryuqq.fileflow.application.upload.port.out.GeneratePresignedUrlPort;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.domain.policy.FileType;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.command.FileUploadCommand;
import com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException;
import com.ryuqq.fileflow.domain.upload.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.upload.vo.PresignedUrlInfo;
import com.ryuqq.fileflow.domain.upload.vo.UploadRequest;
import com.ryuqq.fileflow.domain.upload.UploadSession;

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

    private final UploadSessionPort uploadSessionPort;
    private final GeneratePresignedUrlPort generatePresignedUrlPort;
    private final ValidateUploadPolicyUseCase validateUploadPolicyUseCase;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param uploadSessionPort 세션 저장소
     * @param generatePresignedUrlPort Presigned URL 생성 Port
     * @param validateUploadPolicyUseCase 정책 검증 UseCase
     */
    public UploadSessionService(
            UploadSessionPort uploadSessionPort,
            GeneratePresignedUrlPort generatePresignedUrlPort,
            ValidateUploadPolicyUseCase validateUploadPolicyUseCase
    ) {
        this.uploadSessionPort = Objects.requireNonNull(
            uploadSessionPort,
                " must not be null"
        );
        this.generatePresignedUrlPort = Objects.requireNonNull(
                generatePresignedUrlPort,
                "GeneratePresignedUrlPort must not be null"
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

        // 1. 멱등성 키 생성 또는 추출
        IdempotencyKey idempotencyKey = command.getOrGenerateIdempotencyKey();

        // 2. 멱등성 키로 기존 세션 확인
        if (command.hasIdempotencyKey()) {
            java.util.Optional<UploadSession> existingSession = uploadSessionPort.findByIdempotencyKey(idempotencyKey);

            if (existingSession.isPresent()) {
                UploadSession session = existingSession.get();

                // 이미 완료된 세션이면 에러
                if (session.getStatus() == com.ryuqq.fileflow.domain.upload.vo.UploadStatus.COMPLETED) {
                    throw new IllegalStateException(
                            "Session already completed for idempotency key: " + idempotencyKey.value()
                    );
                }

                // 기존 세션과 URL 정보 반환 (만료 여부와 관계없이 새 URL 발급)
                PresignedUrlInfo presignedUrlInfo = generatePresignedUrlForCommand(command);

                return new UploadSessionWithUrlResponse(
                        UploadSessionResponse.from(session),
                        PresignedUrlResponse.from(presignedUrlInfo)
                );
            }
        }

        // 3. FileType 추출
        FileType fileType = FileType.fromContentType(command.contentType());

        // 4. 정책 검증 (Epic 1)
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

        // 5. UploadRequest 생성 (멱등성 키 사용)
        UploadRequest uploadRequest = UploadRequest.of(
                command.fileName(),
                fileType,
                command.fileSize(),
                command.contentType(),
                idempotencyKey
        );

        // 6. UploadSession 생성
        UploadSession session = UploadSession.create(
                policyKey,
                uploadRequest,
                command.uploaderId(),
                command.expirationMinutes()
        );

        // 7. Presigned URL 발급
        PresignedUrlInfo presignedUrlInfo = generatePresignedUrlForCommand(command);

        // 8. 세션 저장
        UploadSession savedSession = uploadSessionPort.save(session);

        // 9. Response 생성
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
        UploadSession session = findSessionById(sessionId);
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
        UploadSession session = findSessionById(sessionId);

        // 도메인 로직으로 상태 전이 (검증 포함)
        UploadSession completedSession = session.complete();

        // 변경된 세션 저장
        UploadSession savedSession = uploadSessionPort.save(completedSession);

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
        UploadSession session = findSessionById(sessionId);

        // 도메인 로직으로 상태 전이 (검증 포함)
        UploadSession cancelledSession = session.cancel();

        // 변경된 세션 저장
        UploadSession savedSession = uploadSessionPort.save(cancelledSession);

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
     * 파일명에서 파일 포맷을 추출합니다.
     *
     * @param fileName 파일명
     * @return 파일 포맷 (확장자)
     */
    private String extractFileFormat(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase(java.util.Locale.ROOT);
        }
        return "";
    }

    /**
     * CreateUploadSessionCommand로부터 Presigned URL을 생성합니다.
     * 기존 세션과 신규 세션 생성 시 공통으로 사용되는 로직을 추출하여
     * 코드 중복을 제거합니다.
     *
     * @param command 세션 생성 Command
     * @return 생성된 Presigned URL 정보
     */
    private PresignedUrlInfo generatePresignedUrlForCommand(CreateUploadSessionCommand command) {
        PolicyKey policyKey = command.getPolicyKey();
        FileType fileType = FileType.fromContentType(command.contentType());
        FileUploadCommand fileUploadCommand = FileUploadCommand.of(
                policyKey,
                command.uploaderId(),
                command.fileName(),
                fileType,
                command.fileSize(),
                command.contentType()
        );
        return generatePresignedUrlPort.generate(fileUploadCommand);
    }
}
