package com.ryuqq.fileflow.domain.session.vo;

import java.time.Instant;
import java.util.Objects;

/**
 * 멀티파트 업로드에서 완료된 개별 파트. MultipartUploadSession의 자식 Entity.
 *
 * @param partNumber 파트 번호 (1부터 시작)
 * @param etag S3에서 반환한 ETag
 * @param size 파트 크기 (bytes)
 * @param createdAt 파트 완료 시각
 */
public record CompletedPart(int partNumber, String etag, long size, Instant createdAt) {

    public CompletedPart {
        if (partNumber < 1) {
            throw new IllegalArgumentException("partNumber must be >= 1, got: " + partNumber);
        }
        Objects.requireNonNull(etag, "etag must not be null");
        if (size <= 0) {
            throw new IllegalArgumentException("size must be > 0, got: " + size);
        }
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    public static CompletedPart of(int partNumber, String etag, long size, Instant createdAt) {
        return new CompletedPart(partNumber, etag, size, createdAt);
    }
}
