package com.ryuqq.fileflow.domain.upload.vo;

/**
 * 파일 크기 Value Object
 *
 * 불변성:
 * - record 타입으로 모든 필드는 final이며 생성 후 변경 불가
 * - 바이트 단위의 파일 크기를 표현
 *
 * 용도:
 * - 파일 크기 검증 및 할당량 관리
 * - 스토리지 사용량 추적
 */
public record FileSize(long bytes) {

    private static final long KB = 1024L;
    private static final long MB = KB * 1024L;
    private static final long GB = MB * 1024L;

    /**
     * Compact constructor로 검증 로직 수행
     */
    public FileSize {
        validateBytes(bytes);
    }

    /**
     * 바이트 크기로 FileSize를 생성합니다.
     *
     * @param bytes 바이트 크기
     * @return FileSize 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 값인 경우
     */
    public static FileSize ofBytes(long bytes) {
        return new FileSize(bytes);
    }

    /**
     * KB 단위로 FileSize를 생성합니다.
     *
     * @param kilobytes KB 크기
     * @return FileSize 인스턴스
     */
    public static FileSize ofKilobytes(long kilobytes) {
        return new FileSize(kilobytes * KB);
    }

    /**
     * MB 단위로 FileSize를 생성합니다.
     *
     * @param megabytes MB 크기
     * @return FileSize 인스턴스
     */
    public static FileSize ofMegabytes(long megabytes) {
        return new FileSize(megabytes * MB);
    }

    /**
     * GB 단위로 FileSize를 생성합니다.
     *
     * @param gigabytes GB 크기
     * @return FileSize 인스턴스
     */
    public static FileSize ofGigabytes(long gigabytes) {
        return new FileSize(gigabytes * GB);
    }

    /**
     * 사람이 읽기 쉬운 형식으로 반환합니다.
     *
     * @return 예: "10.5 MB", "1.2 GB"
     */
    public String toHumanReadable() {
        if (bytes >= GB) {
            return String.format("%.2f GB", bytes / (double) GB);
        } else if (bytes >= MB) {
            return String.format("%.2f MB", bytes / (double) MB);
        } else if (bytes >= KB) {
            return String.format("%.2f KB", bytes / (double) KB);
        } else {
            return bytes + " bytes";
        }
    }

    /**
     * 주어진 크기보다 큰지 확인합니다.
     *
     * @param other 비교할 FileSize
     * @return 더 크면 true
     */
    public boolean isGreaterThan(FileSize other) {
        return this.bytes > other.bytes;
    }

    /**
     * 주어진 크기보다 작은지 확인합니다.
     *
     * @param other 비교할 FileSize
     * @return 더 작으면 true
     */
    public boolean isLessThan(FileSize other) {
        return this.bytes < other.bytes;
    }

    // ========== Validation Methods ==========

    private static void validateBytes(long bytes) {
        if (bytes < 0) {
            throw new IllegalArgumentException("File size cannot be negative");
        }

        if (bytes == 0) {
            throw new IllegalArgumentException("File size cannot be zero");
        }
    }
}
