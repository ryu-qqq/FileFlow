package com.ryuqq.fileflow.domain.session.vo;

/**
 * Multipart Upload의 Part 크기 Value Object.
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>Part 크기는 5MB 이상이어야 한다 (S3 제약, 마지막 Part 제외).
 *   <li>Part 크기는 5GB를 초과할 수 없다 (S3 제약).
 * </ul>
 *
 * @param bytes Part 크기 (바이트)
 */
public record PartSize(long bytes) {

    private static final long MIN_BYTES = 5L * 1024 * 1024; // 5MB
    private static final long MAX_BYTES = 5L * 1024 * 1024 * 1024; // 5GB

    /** Compact Constructor (검증 로직). */
    public PartSize {
        if (bytes < MIN_BYTES || bytes > MAX_BYTES) {
            throw new IllegalArgumentException(
                    String.format(
                            "Part 크기는 %dMB ~ %dGB 사이여야 합니다: %d bytes",
                            MIN_BYTES / 1024 / 1024, MAX_BYTES / 1024 / 1024 / 1024, bytes));
        }
    }

    /**
     * 값 기반 생성.
     *
     * @param bytes Part 크기 (5MB ~ 5GB)
     * @return PartSize
     * @throws IllegalArgumentException bytes가 5MB ~ 5GB 범위를 벗어난 경우
     */
    public static PartSize of(long bytes) {
        return new PartSize(bytes);
    }

    /**
     * MB 단위로 Part 크기 생성.
     *
     * @param megaBytes MB 단위 크기
     * @return PartSize
     */
    public static PartSize ofMegaBytes(long megaBytes) {
        return new PartSize(megaBytes * 1024 * 1024);
    }

    /**
     * MB 단위로 변환.
     *
     * @return MB 단위 크기
     */
    public long toMegaBytes() {
        return bytes / 1024 / 1024;
    }
}
