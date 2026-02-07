package com.ryuqq.fileflow.domain.session.aggregate;

import java.util.Objects;

/**
 * 멀티파트 업로드에서 완료된 개별 파트.
 * MultipartUploadSession의 자식 Entity.
 *
 * @param partNumber 파트 번호 (1부터 시작)
 * @param etag S3에서 반환한 ETag
 * @param size 파트 크기 (bytes)
 */
public record CompletedPart(
        int partNumber,
        String etag,
        long size
) {

    public CompletedPart {
        if (partNumber < 1) {
            throw new IllegalArgumentException("partNumber must be >= 1, got: " + partNumber);
        }
        Objects.requireNonNull(etag, "etag must not be null");
        if (size <= 0) {
            throw new IllegalArgumentException("size must be > 0, got: " + size);
        }
    }

    public static CompletedPart of(int partNumber, String etag, long size) {
        return new CompletedPart(partNumber, etag, size);
    }
}
