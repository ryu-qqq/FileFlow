package com.ryuqq.fileflow.domain.asset.vo;

import java.util.Objects;

/**
 * 파일 기본 정보.
 *
 * @param fileName 원본 파일명
 * @param fileSize 파일 크기 (bytes)
 * @param contentType MIME 타입 (예: "image/jpeg")
 * @param etag S3 ETag (무결성 검증)
 * @param extension 파일 확장자 (예: "jpg")
 */
public record FileInfo(
        String fileName,
        long fileSize,
        String contentType,
        String etag,
        String extension
) {

    public FileInfo {
        Objects.requireNonNull(fileName, "fileName must not be null");
        Objects.requireNonNull(contentType, "contentType must not be null");
        Objects.requireNonNull(etag, "etag must not be null");
        Objects.requireNonNull(extension, "extension must not be null");
        if (fileSize <= 0) {
            throw new IllegalArgumentException("fileSize must be > 0, got: " + fileSize);
        }
    }

    public static FileInfo of(String fileName, long fileSize, String contentType,
                               String etag, String extension) {
        return new FileInfo(fileName, fileSize, contentType, etag, extension);
    }
}
