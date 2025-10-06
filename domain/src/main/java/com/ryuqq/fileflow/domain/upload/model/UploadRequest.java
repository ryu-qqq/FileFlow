package com.ryuqq.fileflow.domain.upload.model;

import com.ryuqq.fileflow.domain.policy.FileType;

/**
 * 파일 업로드 요청 정보를 나타내는 Value Object
 * Presigned URL 발급을 위한 필수 정보를 포함합니다.
 *
 * 불변성:
 * - record 타입으로 모든 필드는 final이며 생성 후 변경 불가
 * - 방어적 복사를 통해 외부 변경으로부터 보호
 */
public record UploadRequest(
        String fileName,
        FileType fileType,
        long fileSizeBytes,
        String contentType
) {

    /**
     * Compact constructor로 검증 로직 수행
     */
    public UploadRequest {
        validateFileName(fileName);
        validateFileType(fileType);
        validateFileSizeBytes(fileSizeBytes);
        validateContentType(contentType);
    }

    /**
     * UploadRequest를 생성합니다.
     *
     * @param fileName 파일명
     * @param fileType 파일 타입
     * @param fileSizeBytes 파일 크기 (bytes)
     * @param contentType MIME 타입
     * @return UploadRequest 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    public static UploadRequest of(
            String fileName,
            FileType fileType,
            long fileSizeBytes,
            String contentType
    ) {
        return new UploadRequest(fileName, fileType, fileSizeBytes, contentType);
    }

    // ========== Validation Methods ==========

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
}
