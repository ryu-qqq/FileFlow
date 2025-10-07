package com.ryuqq.fileflow.domain.upload.vo;

import com.ryuqq.fileflow.domain.policy.UploadPolicy;
import com.ryuqq.fileflow.domain.policy.exception.PolicyViolationException;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.command.FileUploadCommand;
import com.ryuqq.fileflow.domain.upload.exception.UploadValidationException;

/**
 * 업로드 검증 로직을 담당하는 Domain Service
 * 파일 크기, 타입, 테넌트 정책 등의 비즈니스 규칙을 검증합니다.
 */
public final class UploadValidator {

    private UploadValidator() {
        // Utility class - prevent instantiation
    }

    /**
     * FileUploadCommand를 정책에 따라 검증합니다.
     *
     * @param command 업로드 명령
     * @param policy 업로드 정책
     * @throws UploadValidationException 검증 실패 시
     */
    public static void validate(FileUploadCommand command, UploadPolicy policy) {
        validatePolicyActive(policy);
        validateFileAttributes(command, policy);
    }

    /**
     * 정책이 활성 상태인지 검증합니다.
     *
     * @param policy 업로드 정책
     * @throws UploadValidationException 정책이 비활성 상태인 경우
     */
    private static void validatePolicyActive(UploadPolicy policy) {
        if (!policy.isActive()) {
            throw new UploadValidationException(
                    UploadValidationException.ValidationType.POLICY_VIOLATION,
                    "Upload policy is not active: " + policy.getPolicyKey()
            );
        }

        if (!policy.isEffectiveNow()) {
            throw new UploadValidationException(
                    UploadValidationException.ValidationType.POLICY_VIOLATION,
                    String.format("Upload policy is not effective at current time. Policy: %s, Effective period: %s to %s",
                            policy.getPolicyKey(),
                            policy.getEffectiveFrom(),
                            policy.getEffectiveUntil())
            );
        }
    }

    /**
     * 파일 속성(크기, 타입)을 정책에 따라 검증합니다.
     *
     * @param command 업로드 명령
     * @param policy 업로드 정책
     * @throws UploadValidationException 검증 실패 시
     */
    private static void validateFileAttributes(FileUploadCommand command, UploadPolicy policy) {
        try {
            policy.validateFile(
                    command.fileType(),
                    null, // fileFormat은 command에 없으므로 null 전달
                    command.fileSizeBytes(),
                    1 // 단일 파일 업로드
            );
        } catch (PolicyViolationException e) {
            UploadValidationException.ValidationType validationType = mapViolationType(e.getViolationType());
            throw new UploadValidationException(
                    validationType,
                    "File validation failed: " + e.getMessage()
            );
        }
    }

    /**
     * Rate Limit을 검증합니다.
     *
     * @param policy 업로드 정책
     * @param currentRequestCount 현재 시간당 요청 횟수
     * @param currentUploadCount 현재 일일 업로드 횟수
     * @throws UploadValidationException Rate Limit 초과 시
     */
    public static void validateRateLimit(
            UploadPolicy policy,
            int currentRequestCount,
            int currentUploadCount
    ) {
        try {
            policy.validateRateLimit(currentRequestCount, currentUploadCount);
        } catch (PolicyViolationException e) {
            throw new UploadValidationException(
                    UploadValidationException.ValidationType.RATE_LIMIT_EXCEEDED,
                    "Rate limit validation failed: " + e.getMessage()
            );
        }
    }

    /**
     * 업로드 세션이 유효한지 검증합니다.
     *
     * @param session 업로드 세션
     * @throws UploadValidationException 세션이 만료된 경우
     */
    public static void validateSession(UploadSession session) {
        if (session.isExpired()) {
            throw new UploadValidationException(
                    UploadValidationException.ValidationType.SESSION_EXPIRED,
                    "Upload session has expired. SessionId: " + session.getSessionId()
            );
        }

        if (!session.isActive()) {
            throw new UploadValidationException(
                    UploadValidationException.ValidationType.SESSION_EXPIRED,
                    "Upload session is not active. SessionId: " + session.getSessionId() +
                    ", Status: " + session.getStatus()
            );
        }
    }

    /**
     * PolicyViolationException의 ViolationType을 UploadValidationException의 ValidationType으로 매핑합니다.
     *
     * @param violationType PolicyViolationException의 위반 유형
     * @return 매핑된 UploadValidationException의 검증 유형
     */
    private static UploadValidationException.ValidationType mapViolationType(
            PolicyViolationException.ViolationType violationType
    ) {
        return switch (violationType) {
            case FILE_SIZE_EXCEEDED -> UploadValidationException.ValidationType.FILE_SIZE_EXCEEDED;
            case FILE_COUNT_EXCEEDED -> UploadValidationException.ValidationType.POLICY_VIOLATION;
            case INVALID_FORMAT -> UploadValidationException.ValidationType.INVALID_FILE_TYPE;
            case DIMENSION_EXCEEDED -> UploadValidationException.ValidationType.POLICY_VIOLATION;
            case RATE_LIMIT_EXCEEDED -> UploadValidationException.ValidationType.POLICY_VIOLATION;
        };
    }
}
