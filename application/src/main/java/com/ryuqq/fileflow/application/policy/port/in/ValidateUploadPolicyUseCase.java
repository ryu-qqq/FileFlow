package com.ryuqq.fileflow.application.policy.port.in;

import com.ryuqq.fileflow.domain.policy.FileType;

/**
 * 업로드 정책 검증을 위한 Inbound Port (Use Case)
 *
 * Hexagonal Architecture의 Inbound Port로서,
 * 외부(Adapter)에서 정책 검증 기능을 호출하기 위한 인터페이스입니다.
 *
 * 사용 시나리오:
 * - Presigned URL 발급 전 정책 검증
 * - 파일 업로드 요청 시 정책 준수 여부 확인
 * - Rate Limiting 검증
 *
 * @author sangwon-ryu
 */
public interface ValidateUploadPolicyUseCase {

    /**
     * 업로드 정책을 검증합니다.
     *
     * @param command 검증 요청 커맨드
     * @throws com.ryuqq.fileflow.domain.policy.exception.PolicyNotFoundException 정책을 찾을 수 없는 경우
     * @throws com.ryuqq.fileflow.domain.policy.exception.PolicyViolationException 정책 위반 시
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    void validate(ValidateUploadPolicyCommand command);

    /**
     * 업로드 정책 검증 요청을 나타내는 Command 객체
     *
     * @param tenantId 테넌트 ID
     * @param userType 사용자 타입
     * @param serviceType 서비스 타입
     * @param fileType 파일 타입
     * @param fileFormat 파일 포맷 (예: "jpg", "png", "pdf", Optional)
     * @param fileSizeBytes 파일 크기 (bytes)
     * @param fileCount 파일 개수
     * @param currentRequestCount 현재 시간당 요청 횟수 (Rate Limiting 검증용, Optional)
     * @param currentUploadCount 현재 일일 업로드 횟수 (Rate Limiting 검증용, Optional)
     */
    record ValidateUploadPolicyCommand(
            String tenantId,
            String userType,
            String serviceType,
            FileType fileType,
            String fileFormat,
            long fileSizeBytes,
            int fileCount,
            Integer currentRequestCount,
            Integer currentUploadCount
    ) {
        public ValidateUploadPolicyCommand {
            // tenantId, userType, serviceType 검증은 PolicyKey.of()에서 수행됨
            if (fileType == null) {
                throw new IllegalArgumentException("FileType must not be null");
            }
            if (fileSizeBytes <= 0) {
                throw new IllegalArgumentException("FileSizeBytes must be greater than 0");
            }
            if (fileCount <= 0) {
                throw new IllegalArgumentException("FileCount must be greater than 0");
            }
        }

        /**
         * Rate Limiting 검증이 필요한지 확인합니다.
         *
         * @return Rate Limiting 검증 필요 여부
         */
        public boolean shouldValidateRateLimit() {
            return currentRequestCount != null && currentUploadCount != null;
        }
    }
}
