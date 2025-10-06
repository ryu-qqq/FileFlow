package com.ryuqq.fileflow.domain.upload.model;

import com.ryuqq.fileflow.domain.policy.FileType;

import java.util.Objects;

/**
 * 파일 업로드 요청 정보를 나타내는 Value Object
 * Presigned URL 발급을 위한 필수 정보를 포함합니다.
 *
 * 불변성:
 * - 모든 필드는 final이며 생성 후 변경 불가
 * - 방어적 복사를 통해 외부 변경으로부터 보호
 */
public final class UploadRequest {

    private final String fileName;
    private final FileType fileType;
    private final long fileSizeBytes;
    private final String contentType;

    private UploadRequest(
            String fileName,
            FileType fileType,
            long fileSizeBytes,
            String contentType
    ) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSizeBytes = fileSizeBytes;
        this.contentType = contentType;
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
        validateFileName(fileName);
        validateFileType(fileType);
        validateFileSizeBytes(fileSizeBytes);
        validateContentType(contentType);

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

    // ========== Getters ==========

    public String getFileName() {
        return fileName;
    }

    public FileType getFileType() {
        return fileType;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public String getContentType() {
        return contentType;
    }

    // ========== Object Methods ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UploadRequest that = (UploadRequest) o;
        return fileSizeBytes == that.fileSizeBytes &&
               Objects.equals(fileName, that.fileName) &&
               fileType == that.fileType &&
               Objects.equals(contentType, that.contentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, fileType, fileSizeBytes, contentType);
    }

    @Override
    public String toString() {
        return "UploadRequest{" +
                "fileName='" + fileName + '\'' +
                ", fileType=" + fileType +
                ", fileSizeBytes=" + fileSizeBytes +
                ", contentType='" + contentType + '\'' +
                '}';
    }
}
