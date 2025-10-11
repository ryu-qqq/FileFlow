package com.ryuqq.fileflow.domain.upload.command;

import com.ryuqq.fileflow.domain.policy.FileType;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.vo.CheckSum;

/**
 * 파일 업로드 명령을 나타내는 Command Object
 * Presigned URL 발급 요청을 캡슐화합니다.
 *
 * CQRS 패턴:
 * - Command와 Query를 명확히 분리
 * - 의도를 명시적으로 표현
 */
public record FileUploadCommand(
        PolicyKey policyKey,
        String uploaderId,
        String fileName,
        FileType fileType,
        long fileSizeBytes,
        String contentType,
        CheckSum checksum,
        int expirationMinutes
) {

    /**
     * Compact constructor로 검증 로직 수행
     */
    public FileUploadCommand {
        validatePolicyKey(policyKey);
        validateUploaderId(uploaderId);
        validateFileName(fileName);
        validateFileType(fileType);
        validateFileSizeBytes(fileSizeBytes);
        validateContentType(contentType);
        validateExpirationMinutes(expirationMinutes);
        // CheckSum은 optional이므로 null 허용
    }

    /**
     * FileUploadCommand를 생성합니다.
     *
     * @param policyKey 정책 키
     * @param uploaderId 업로더 ID
     * @param fileName 파일명
     * @param fileType 파일 타입
     * @param fileSizeBytes 파일 크기 (bytes)
     * @param contentType MIME 타입
     * @param expirationMinutes 만료 시간 (분)
     * @return FileUploadCommand 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    public static FileUploadCommand of(
            PolicyKey policyKey,
            String uploaderId,
            String fileName,
            FileType fileType,
            long fileSizeBytes,
            String contentType,
            int expirationMinutes
    ) {
        return new FileUploadCommand(
                policyKey,
                uploaderId,
                fileName,
                fileType,
                fileSizeBytes,
                contentType,
                null,
                expirationMinutes
        );
    }

    /**
     * CheckSum을 포함한 FileUploadCommand를 생성합니다.
     *
     * @param policyKey 정책 키
     * @param uploaderId 업로더 ID
     * @param fileName 파일명
     * @param fileType 파일 타입
     * @param fileSizeBytes 파일 크기 (bytes)
     * @param contentType MIME 타입
     * @param checksum 파일 체크섬 (optional)
     * @param expirationMinutes 만료 시간 (분)
     * @return FileUploadCommand 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    public static FileUploadCommand of(
            PolicyKey policyKey,
            String uploaderId,
            String fileName,
            FileType fileType,
            long fileSizeBytes,
            String contentType,
            CheckSum checksum,
            int expirationMinutes
    ) {
        return new FileUploadCommand(
                policyKey,
                uploaderId,
                fileName,
                fileType,
                fileSizeBytes,
                contentType,
                checksum,
                expirationMinutes
        );
    }

    // ========== Validation Methods ==========

    private static void validatePolicyKey(PolicyKey policyKey) {
        if (policyKey == null) {
            throw new IllegalArgumentException("PolicyKey cannot be null");
        }
    }

    private static void validateUploaderId(String uploaderId) {
        if (uploaderId == null || uploaderId.trim().isEmpty()) {
            throw new IllegalArgumentException("UploaderId cannot be null or empty");
        }
    }

    private static void validateFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("FileName cannot be null or empty");
        }
        if (fileName.length() > 255) {
            throw new IllegalArgumentException("FileName cannot exceed 255 characters");
        }
    }

    private static void validateFileType(FileType fileType) {
        if (fileType == null) {
            throw new IllegalArgumentException("FileType cannot be null");
        }
    }

    private static void validateFileSizeBytes(long fileSizeBytes) {
        if (fileSizeBytes <= 0) {
            throw new IllegalArgumentException("FileSizeBytes must be positive");
        }
    }

    private static void validateContentType(String contentType) {
        if (contentType == null || contentType.trim().isEmpty()) {
            throw new IllegalArgumentException("ContentType cannot be null or empty");
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
