package com.ryuqq.fileflow.domain.upload;

/**
 * FileSize Value Object
 * 파일 크기를 나타내는 값 객체
 *
 * <p>파일 크기는 바이트 단위로 저장되며, S3 업로드 정책 검증 및 Multipart Upload 필요 여부 판단에 사용됩니다.</p>
 *
 * <p><strong>비즈니스 규칙:</strong></p>
 * <ul>
 *   <li>파일 크기는 0 이상이어야 합니다</li>
 *   <li>S3 최대 객체 크기는 5TB입니다</li>
 *   <li>5MB 이상의 파일은 Multipart Upload를 사용할 수 있습니다</li>
 *   <li>100MB 이상의 파일은 Multipart Upload 사용을 권장합니다</li>
 * </ul>
 *
 * @param bytes 파일 크기 (바이트)
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record FileSize(Long bytes) {

    /**
     * 1KB (1024 바이트)
     */
    public static final long KILOBYTE = 1024L;

    /**
     * 1MB (1024 KB)
     */
    public static final long MEGABYTE = KILOBYTE * 1024L;

    /**
     * 1GB (1024 MB)
     */
    public static final long GIGABYTE = MEGABYTE * 1024L;

    /**
     * 1TB (1024 GB)
     */
    public static final long TERABYTE = GIGABYTE * 1024L;

    /**
     * S3 Multipart Upload 최소 Part 크기 (5MB, 마지막 Part 제외)
     */
    public static final long MIN_MULTIPART_PART_SIZE = 5 * MEGABYTE;

    /**
     * S3 Multipart Upload 권장 기준 (100MB)
     */
    public static final long RECOMMENDED_MULTIPART_THRESHOLD = 100 * MEGABYTE;

    /**
     * S3 최대 객체 크기 (5TB)
     */
    public static final long MAX_S3_OBJECT_SIZE = 5 * TERABYTE;

    /**
     * Compact 생성자 - 유효성 검증
     *
     * @throws IllegalArgumentException 파일 크기가 null이거나 음수이거나 S3 제한을 초과하는 경우
     */
    public FileSize {
        if (bytes == null) {
            throw new IllegalArgumentException("파일 크기는 필수입니다");
        }
        if (bytes < 0) {
            throw new IllegalArgumentException(
                    String.format("파일 크기는 0 이상이어야 합니다: %d", bytes)
            );
        }
        if (bytes > MAX_S3_OBJECT_SIZE) {
            throw new IllegalArgumentException(
                    String.format("파일 크기는 5TB를 초과할 수 없습니다: %s", toHumanReadable(bytes))
            );
        }
    }

    /**
     * Static Factory Method
     *
     * @param bytes 파일 크기 (바이트)
     * @return FileSize 인스턴스
     * @throws IllegalArgumentException 파일 크기가 유효하지 않은 경우
     */
    public static FileSize of(Long bytes) {
        return new FileSize(bytes);
    }

    /**
     * Static Factory Method - KB 단위
     *
     * @param kilobytes 파일 크기 (KB)
     * @return FileSize 인스턴스
     */
    public static FileSize ofKilobytes(long kilobytes) {
        return new FileSize(kilobytes * KILOBYTE);
    }

    /**
     * Static Factory Method - MB 단위
     *
     * @param megabytes 파일 크기 (MB)
     * @return FileSize 인스턴스
     */
    public static FileSize ofMegabytes(long megabytes) {
        return new FileSize(megabytes * MEGABYTE);
    }

    /**
     * Static Factory Method - GB 단위
     *
     * @param gigabytes 파일 크기 (GB)
     * @return FileSize 인스턴스
     */
    public static FileSize ofGigabytes(long gigabytes) {
        return new FileSize(gigabytes * GIGABYTE);
    }

    /**
     * Multipart Upload가 가능한 크기인지 확인
     * (5MB 이상)
     *
     * @return Multipart Upload 가능 시 true
     */
    public boolean canUseMultipart() {
        return bytes >= MIN_MULTIPART_PART_SIZE;
    }

    /**
     * Multipart Upload가 권장되는 크기인지 확인
     * (100MB 이상)
     *
     * @return Multipart Upload 권장 시 true
     */
    public boolean shouldUseMultipart() {
        return bytes >= RECOMMENDED_MULTIPART_THRESHOLD;
    }

    /**
     * 빈 파일인지 확인
     *
     * @return 크기가 0이면 true
     */
    public boolean isEmpty() {
        return bytes == 0;
    }

    /**
     * 사람이 읽기 쉬운 형식으로 변환
     * 예: "1.5 MB", "2.3 GB"
     *
     * @return 사람이 읽기 쉬운 파일 크기 문자열
     */
    public String toHumanReadable() {
        return toHumanReadable(bytes);
    }

    /**
     * 정적 헬퍼 메서드 - 바이트를 사람이 읽기 쉬운 형식으로 변환
     *
     * @param bytes 바이트 크기
     * @return 사람이 읽기 쉬운 파일 크기 문자열
     */
    private static String toHumanReadable(long bytes) {
        if (bytes < KILOBYTE) {
            return bytes + " B";
        } else if (bytes < MEGABYTE) {
            return String.format("%.2f KB", bytes / (double) KILOBYTE);
        } else if (bytes < GIGABYTE) {
            return String.format("%.2f MB", bytes / (double) MEGABYTE);
        } else if (bytes < TERABYTE) {
            return String.format("%.2f GB", bytes / (double) GIGABYTE);
        } else {
            return String.format("%.2f TB", bytes / (double) TERABYTE);
        }
    }

    /**
     * 두 파일 크기를 더함
     *
     * @param other 더할 파일 크기
     * @return 합산된 파일 크기
     */
    public FileSize add(FileSize other) {
        return new FileSize(this.bytes + other.bytes);
    }

    /**
     * 두 파일 크기를 뺌
     *
     * @param other 뺄 파일 크기
     * @return 차감된 파일 크기
     * @throws IllegalArgumentException 결과가 음수인 경우
     */
    public FileSize subtract(FileSize other) {
        long result = this.bytes - other.bytes;
        if (result < 0) {
            throw new IllegalArgumentException("파일 크기는 음수가 될 수 없습니다");
        }
        return new FileSize(result);
    }

    /**
     * 다른 파일 크기보다 큰지 확인
     *
     * @param other 비교할 파일 크기
     * @return 현재 크기가 더 크면 true
     */
    public boolean isGreaterThan(FileSize other) {
        return this.bytes > other.bytes;
    }

    /**
     * 다른 파일 크기보다 작은지 확인
     *
     * @param other 비교할 파일 크기
     * @return 현재 크기가 더 작으면 true
     */
    public boolean isLessThan(FileSize other) {
        return this.bytes < other.bytes;
    }
}
