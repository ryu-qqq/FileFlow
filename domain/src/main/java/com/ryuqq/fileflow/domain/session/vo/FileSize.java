package com.ryuqq.fileflow.domain.session.vo;

import com.ryuqq.fileflow.domain.session.exception.FileSizeExceededException;

/**
 * 파일 크기 Value Object.
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>파일 크기는 0보다 커야 한다.
 *   <li>Single Upload: 5GB 이하
 *   <li>Multipart Upload: 5MB ~ 5TB
 * </ul>
 *
 * @param size 파일 크기 (바이트)
 */
public record FileSize(long size) {

    private static final long MIN_BYTES = 1L;
    private static final long MAX_SINGLE_UPLOAD_BYTES = 5L * 1024 * 1024 * 1024; // 5GB
    private static final long MIN_MULTIPART_BYTES = 5L * 1024 * 1024; // 5MB
    private static final long MAX_MULTIPART_BYTES = 5L * 1024 * 1024 * 1024 * 1024; // 5TB

    /** Compact Constructor (검증 로직). */
    public FileSize {
        if (size < MIN_BYTES) {
            throw new IllegalArgumentException("파일 크기는 0보다 커야 합니다: " + size);
        }

        if (size > MAX_MULTIPART_BYTES) {
            throw new FileSizeExceededException(size, MAX_MULTIPART_BYTES);
        }
    }

    /**
     * 값 기반 생성.
     *
     * @param size 파일 크기 (바이트, 양수)
     * @return FileSize
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static FileSize of(long size) {
        return new FileSize(size);
    }

    /**
     * MB 단위로 파일 크기 생성.
     *
     * @param megaBytes MB 단위 크기
     * @return FileSize
     */
    public static FileSize ofMegaBytes(long megaBytes) {
        return new FileSize(megaBytes * 1024 * 1024);
    }

    /**
     * GB 단위로 파일 크기 생성.
     *
     * @param gigaBytes GB 단위 크기
     * @return FileSize
     */
    public static FileSize ofGigaBytes(long gigaBytes) {
        return new FileSize(gigaBytes * 1024 * 1024 * 1024);
    }

    /**
     * Single Upload가 필요한지 확인한다 (5GB 이하).
     *
     * @return Single Upload 가능하면 true
     */
    public boolean requiresSingleUpload() {
        return size <= MAX_SINGLE_UPLOAD_BYTES;
    }

    /**
     * Multipart Upload가 필요한지 확인한다 (5GB 초과).
     *
     * @return Multipart Upload 필요하면 true
     */
    public boolean requiresMultipartUpload() {
        return size > MAX_SINGLE_UPLOAD_BYTES;
    }

    /**
     * Multipart Upload가 가능한 크기인지 확인한다 (5MB 이상).
     *
     * @return Multipart Upload 가능하면 true
     */
    public boolean isValidForMultipart() {
        return size >= MIN_MULTIPART_BYTES && size <= MAX_MULTIPART_BYTES;
    }

    /**
     * MB 단위로 변환.
     *
     * @return MB 단위 크기
     */
    public long toMegaBytes() {
        return size / 1024 / 1024;
    }

    /**
     * GB 단위로 변환.
     *
     * @return GB 단위 크기
     */
    public double toGigaBytes() {
        return (double) size / 1024 / 1024 / 1024;
    }

    /**
     * 사람이 읽기 쉬운 형식으로 변환 (예: "1.5 GB", "512 MB").
     *
     * @return 포맷된 파일 크기 문자열
     */
    public String toHumanReadable() {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", (double) size / 1024);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", (double) size / 1024 / 1024);
        } else if (size < 1024L * 1024 * 1024 * 1024) {
            return String.format("%.2f GB", (double) size / 1024 / 1024 / 1024);
        } else {
            return String.format("%.2f TB", (double) size / 1024 / 1024 / 1024 / 1024);
        }
    }
}
