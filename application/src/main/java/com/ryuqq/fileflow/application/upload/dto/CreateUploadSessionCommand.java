package com.ryuqq.fileflow.application.upload.dto;

import com.ryuqq.fileflow.domain.policy.PolicyKey;

/**
 * 업로드 세션 생성 Command
 *
 * 업로드 세션을 생성하기 위한 데이터를 전달하는 Command 객체입니다.
 *
 * @param policyKeyValue 정책 키 값
 * @param fileName 업로드할 파일명
 * @param fileSize 파일 크기 (bytes)
 * @param contentType 파일 Content-Type
 * @param uploaderId 업로더 ID
 * @param expirationMinutes 세션 만료 시간 (분)
 * @author sangwon-ryu
 */
public record CreateUploadSessionCommand(
        String policyKeyValue,
        String fileName,
        long fileSize,
        String contentType,
        String uploaderId,
        int expirationMinutes
) {
    /**
     * Command를 검증하고 도메인 객체 생성에 필요한 PolicyKey를 반환합니다.
     * PolicyKeyValue 형식: {tenantId}:{userType}:{serviceType}
     */
    public PolicyKey getPolicyKey() {
        String[] parts = policyKeyValue.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException(
                    "Invalid PolicyKeyValue format. Expected: {tenantId}:{userType}:{serviceType}"
            );
        }
        return PolicyKey.of(parts[0], parts[1], parts[2]);
    }

    /**
     * Compact constructor로 검증 로직 수행
     */
    public CreateUploadSessionCommand {
        validatePolicyKeyValue(policyKeyValue);
        validateFileName(fileName);
        validateFileSize(fileSize);
        validateContentType(contentType);
        validateUploaderId(uploaderId);
        validateExpirationMinutes(expirationMinutes);
    }

    private static void validatePolicyKeyValue(String policyKeyValue) {
        if (policyKeyValue == null || policyKeyValue.trim().isEmpty()) {
            throw new IllegalArgumentException("PolicyKeyValue cannot be null or empty");
        }
    }

    private static void validateFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("FileName cannot be null or empty");
        }
    }

    private static void validateFileSize(long fileSize) {
        if (fileSize <= 0) {
            throw new IllegalArgumentException("FileSize must be positive");
        }
    }

    private static void validateContentType(String contentType) {
        if (contentType == null || contentType.trim().isEmpty()) {
            throw new IllegalArgumentException("ContentType cannot be null or empty");
        }
    }

    private static void validateUploaderId(String uploaderId) {
        if (uploaderId == null || uploaderId.trim().isEmpty()) {
            throw new IllegalArgumentException("UploaderId cannot be null or empty");
        }
    }

    private static void validateExpirationMinutes(int expirationMinutes) {
        if (expirationMinutes <= 0) {
            throw new IllegalArgumentException("ExpirationMinutes must be positive");
        }
        if (expirationMinutes > 1440) { // 24시간
            throw new IllegalArgumentException("ExpirationMinutes cannot exceed 1440 (24 hours)");
        }
    }
}
