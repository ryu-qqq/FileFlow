package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.policy.port.in.ValidateUploadPolicyUseCase;
import com.ryuqq.fileflow.application.upload.dto.CreateUploadSessionCommand;
import com.ryuqq.fileflow.application.upload.dto.MultipartUploadResponse;
import com.ryuqq.fileflow.application.upload.dto.PresignedUrlResponse;
import com.ryuqq.fileflow.application.upload.dto.UploadSessionResponse;
import com.ryuqq.fileflow.application.upload.port.in.CancelUploadSessionUseCase;
import com.ryuqq.fileflow.application.upload.port.in.CompleteUploadSessionUseCase;
import com.ryuqq.fileflow.application.upload.port.in.CreateUploadSessionUseCase;
import com.ryuqq.fileflow.application.upload.port.in.GetUploadSessionUseCase;
import com.ryuqq.fileflow.application.upload.port.out.GeneratePresignedUrlPort;
import com.ryuqq.fileflow.application.upload.port.out.MultipartProgressPort;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.domain.policy.FileType;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.command.FileUploadCommand;
import com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException;
import com.ryuqq.fileflow.domain.upload.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.upload.vo.MultipartUploadInfo;
import com.ryuqq.fileflow.domain.upload.vo.PresignedUrlInfo;
import com.ryuqq.fileflow.domain.upload.vo.UploadRequest;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 업로드 세션 관리 Service
 *
 * Hexagonal Architecture의 UseCase 구현체로서,
 * 업로드 세션 생성, 조회, 완료, 취소 비즈니스 로직을 처리합니다.
 *
 * @author sangwon-ryu
 */
@Service
public class UploadSessionService implements
        CreateUploadSessionUseCase,
        GetUploadSessionUseCase,
        CompleteUploadSessionUseCase,
        CancelUploadSessionUseCase {

    private static final int SINGLE_FILE_UPLOAD_COUNT = 1;
    private static final long MULTIPART_THRESHOLD_BYTES = 100 * 1024 * 1024; // 100MB

    private final UploadSessionPort uploadSessionPort;
    private final GeneratePresignedUrlPort generatePresignedUrlPort;
    private final ValidateUploadPolicyUseCase validateUploadPolicyUseCase;
    private final UploadSessionPersistenceService persistenceService;
    private final MultipartProgressPort multipartProgressPort;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param uploadSessionPort 세션 저장소
     * @param generatePresignedUrlPort Presigned URL 생성 Port
     * @param validateUploadPolicyUseCase 정책 검증 UseCase
     * @param persistenceService 영속성 전용 Service
     * @param multipartProgressPort 멀티파트 진행률 추적 Port
     */
    public UploadSessionService(
            UploadSessionPort uploadSessionPort,
            GeneratePresignedUrlPort generatePresignedUrlPort,
            ValidateUploadPolicyUseCase validateUploadPolicyUseCase,
            UploadSessionPersistenceService persistenceService,
            MultipartProgressPort multipartProgressPort
    ) {
        this.uploadSessionPort = Objects.requireNonNull(
            uploadSessionPort,
                "UploadSessionPort must not be null"
        );
        this.generatePresignedUrlPort = Objects.requireNonNull(
                generatePresignedUrlPort,
                "GeneratePresignedUrlPort must not be null"
        );
        this.validateUploadPolicyUseCase = Objects.requireNonNull(
                validateUploadPolicyUseCase,
                "ValidateUploadPolicyUseCase must not be null"
        );
        this.persistenceService = Objects.requireNonNull(
                persistenceService,
                "UploadSessionPersistenceService must not be null"
        );
        this.multipartProgressPort = Objects.requireNonNull(
                multipartProgressPort,
                "MultipartProgressPort must not be null"
        );
    }

    /**
     * 새로운 업로드 세션을 생성하고 Presigned URL을 발급합니다.
     *
     * 트랜잭션 처리 전략:
     * 1. 정책 검증 (외부 호출 없음)
     * 2. 도메인 객체 생성 (메모리 작업)
     * 3. S3 Presigned URL 발급 (외부 API 호출 - 트랜잭션 밖)
     * 4. DB 저장 (별도 트랜잭션 - persistenceService)
     *
     * 장점:
     * - S3 API 호출이 DB 커넥션을 점유하지 않음
     * - 트랜잭션이 빠르게 커밋됨
     * - 각 단계의 실패를 명확하게 처리 가능
     *
     * @param command 세션 생성 Command
     * @return 생성된 세션 정보와 Presigned URL
     * @throws IllegalArgumentException command가 null이거나 유효하지 않은 경우
     * @throws com.ryuqq.fileflow.domain.policy.exception.PolicyNotFoundException 정책을 찾을 수 없는 경우
     * @throws com.ryuqq.fileflow.domain.policy.exception.PolicyViolationException 정책 검증 실패 시
     * @throws com.ryuqq.fileflow.domain.upload.exception.PresignedUrlGenerationException Presigned URL 생성 실패 시
     */
    @Override
    // ❌ @Transactional 제거 - 외부 API 호출이 있으므로 트랜잭션 범위에 포함시키면 안 됨
    public UploadSessionWithUrlResponse createSession(CreateUploadSessionCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("CreateUploadSessionCommand must not be null");
        }

        // 1. 멱등성 키 생성 또는 추출
        IdempotencyKey idempotencyKey = command.getOrGenerateIdempotencyKey();

        // 2. 멱등성 키로 기존 세션 확인
        if (command.hasIdempotencyKey()) {
            UploadSessionWithUrlResponse existingResponse = handleExistingSession(idempotencyKey, command);
            if (existingResponse != null) {
                return existingResponse;
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

        // 6. UploadSession 생성 (메모리 작업)
        UploadSession session = UploadSession.create(
                policyKey,
                uploadRequest,
                command.uploaderId(),
                command.expirationMinutes()
        );

        // 7. 파일 크기에 따라 단일 파일 업로드 또는 멀티파트 업로드 선택
        boolean isMultipart = command.fileSize() >= MULTIPART_THRESHOLD_BYTES;

        PresignedUrlResponse presignedUrlResponse = null;
        MultipartUploadResponse multipartUploadResponse = null;

        try {
            if (isMultipart) {
                // 멀티파트 업로드 (100MB 이상)
                MultipartUploadInfo multipartInfo = initiateMultipartUploadForCommand(command);
                multipartUploadResponse = MultipartUploadResponse.from(multipartInfo);

                // ✅ 세션에 멀티파트 정보 설정
                session = session.withMultipartInfo(multipartInfo);

                // ✅ Redis에 멀티파트 진행 상태 초기화 (새 세션에만 적용)
                // TTL은 세션의 실제 만료 시간 기준으로 계산
                Duration ttl = Duration.between(LocalDateTime.now(), session.getExpiresAt());
                multipartProgressPort.initializeProgress(
                        session.getSessionId(),
                        multipartInfo.totalParts(),
                        ttl
                );
            } else {
                // 단일 파일 업로드 (100MB 미만)
                PresignedUrlInfo presignedUrlInfo = generatePresignedUrlForCommand(command);
                presignedUrlResponse = PresignedUrlResponse.from(presignedUrlInfo);
            }
        } catch (Exception e) {
            throw new com.ryuqq.fileflow.domain.upload.exception.PresignedUrlGenerationException(
                    "Failed to generate " + (isMultipart ? "multipart upload" : "presigned URL") +
                    " for session: " + session.getSessionId(),
                    e
            );
        }

        // 8. 세션 저장 (별도 트랜잭션 - persistenceService 사용)
        UploadSession savedSession = persistenceService.saveSession(session);

        // 9. Response 생성
        return new UploadSessionWithUrlResponse(
                UploadSessionResponse.from(savedSession),
                presignedUrlResponse,
                multipartUploadResponse
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
     * 기존 세션을 처리합니다.
     *
     * @param idempotencyKey 멱등성 키
     * @param command 세션 생성 Command
     * @return 기존 세션이 있으면 Response, 없으면 null
     * @throws IllegalStateException 완료된 세션인 경우
     */
    private UploadSessionWithUrlResponse handleExistingSession(
            IdempotencyKey idempotencyKey,
            CreateUploadSessionCommand command
    ) {
        java.util.Optional<UploadSession> existingSession =
                uploadSessionPort.findByIdempotencyKey(idempotencyKey);

        if (existingSession.isEmpty()) {
            return null;
        }

        UploadSession session = existingSession.get();

        // 이미 완료된 세션이면 에러
        if (session.getStatus() == com.ryuqq.fileflow.domain.upload.vo.UploadStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Session already completed for idempotency key: " + idempotencyKey.value()
            );
        }

        // 기존 세션과 URL 정보 반환 (만료 여부와 관계없이 새 URL 발급)
        // 파일 크기에 따라 단일/멀티파트 업로드 선택
        boolean isMultipart = command.fileSize() >= MULTIPART_THRESHOLD_BYTES;

        PresignedUrlResponse presignedUrlResponse = null;
        MultipartUploadResponse multipartUploadResponse = null;

        if (isMultipart) {
            MultipartUploadInfo multipartInfo = initiateMultipartUploadForCommand(command);
            multipartUploadResponse = MultipartUploadResponse.from(multipartInfo);
        } else {
            PresignedUrlInfo presignedUrlInfo = generatePresignedUrlForCommand(command);
            presignedUrlResponse = PresignedUrlResponse.from(presignedUrlInfo);
        }

        return new UploadSessionWithUrlResponse(
                UploadSessionResponse.from(session),
                presignedUrlResponse,
                multipartUploadResponse
        );
    }

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
                command.contentType(),
                command.expirationMinutes()
        );
        return generatePresignedUrlPort.generate(fileUploadCommand);
    }

    /**
     * CreateUploadSessionCommand로부터 멀티파트 업로드를 시작합니다.
     * 100MB 이상 대용량 파일 업로드 시 사용됩니다.
     *
     * @param command 세션 생성 Command
     * @return 멀티파트 업로드 정보
     */
    private MultipartUploadInfo initiateMultipartUploadForCommand(CreateUploadSessionCommand command) {
        PolicyKey policyKey = command.getPolicyKey();
        FileType fileType = FileType.fromContentType(command.contentType());
        FileUploadCommand fileUploadCommand = FileUploadCommand.of(
                policyKey,
                command.uploaderId(),
                command.fileName(),
                fileType,
                command.fileSize(),
                command.contentType(),
                command.expirationMinutes()
        );
        return generatePresignedUrlPort.initiateMultipartUpload(fileUploadCommand);
    }
}
