package com.ryuqq.fileflow.domain.session.vo;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * 멀티파트 파트별 Presigned URL 생성 스펙.
 *
 * <p>TTL 계산 로직을 내부에 캡슐화합니다. 세션 남은 시간과 기본 TTL 중 짧은 값을 적용합니다.
 *
 * @param s3Key S3 객체 키
 * @param uploadId S3 멀티파트 업로드 ID
 * @param partNumber 파트 번호
 * @param ttl Presigned URL 유효 기간
 * @param createdAt 스펙 생성 시각 (검증용)
 */
public record PartPresignedUrlSpec(
        String s3Key, String uploadId, int partNumber, Duration ttl, Instant createdAt) {

    private static final Duration DEFAULT_PART_URL_TTL = Duration.ofHours(1);

    public PartPresignedUrlSpec {
        Objects.requireNonNull(s3Key, "s3Key must not be null");
        Objects.requireNonNull(uploadId, "uploadId must not be null");
        Objects.requireNonNull(ttl, "ttl must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        if (partNumber < 1) {
            throw new IllegalArgumentException("partNumber must be positive");
        }
        if (ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("ttl must be positive");
        }
    }

    public static PartPresignedUrlSpec of(
            String s3Key, String uploadId, int partNumber, Instant expiresAt, Instant now) {
        Duration remaining = Duration.between(now, expiresAt);
        Duration resolvedTtl =
                remaining.compareTo(DEFAULT_PART_URL_TTL) < 0 ? remaining : DEFAULT_PART_URL_TTL;
        return new PartPresignedUrlSpec(s3Key, uploadId, partNumber, resolvedTtl, now);
    }

    public long ttlSeconds() {
        return ttl.toSeconds();
    }
}
