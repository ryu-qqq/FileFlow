package com.ryuqq.fileflow.application.upload.dto;

import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.vo.IdempotencyKey;

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
 * @param idempotencyKey 멱등성 키 (선택적 - null이면 자동 생성)
 * @author sangwon-ryu
 */
public record CreateUploadSessionCommand(
        String policyKeyValue,
        String fileName,
        long fileSize,
        String contentType,
        String uploaderId,
        int expirationMinutes,
        String idempotencyKey
) {
    private static final int MAX_EXPIRATION_MINUTES = 24 * 60; // 24 hours
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
     * 멱등성 키를 생성하거나 검증합니다.
     *
     * @return IdempotencyKey 인스턴스
     */
    public IdempotencyKey getOrGenerateIdempotencyKey() {
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            return IdempotencyKey.generate();
        }
        return IdempotencyKey.of(idempotencyKey);
    }

    /**
     * 멱등성 키가 제공되었는지 확인합니다.
     *
     * @return 멱등성 키 존재 여부
     */
    public boolean hasIdempotencyKey() {
        return idempotencyKey != null && !idempotencyKey.trim().isEmpty();
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
        // idempotencyKey는 선택적이므로 null 허용
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
        if (expirationMinutes > MAX_EXPIRATION_MINUTES) {
            throw new IllegalArgumentException("ExpirationMinutes cannot exceed " + MAX_EXPIRATION_MINUTES + " (24 hours)");
        }
    }
}
