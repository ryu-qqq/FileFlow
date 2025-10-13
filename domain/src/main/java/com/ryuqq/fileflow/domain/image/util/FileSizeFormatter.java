package com.ryuqq.fileflow.domain.image.util;

/**
 * 파일 크기를 사람이 읽기 쉬운 형식으로 변환하는 유틸리티 클래스
 *
 * 불변성:
 * - 모든 메서드는 static
 * - 상태를 가지지 않음
 *
 * 비즈니스 규칙:
 * - 1024 단위로 KB, MB 변환
 * - 소수점 둘째 자리까지 표시
 */
public final class FileSizeFormatter {

    private static final long KILOBYTE = 1024;
    private static final long MEGABYTE = KILOBYTE * 1024;

    private FileSizeFormatter() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 파일 크기를 사람이 읽기 쉬운 형식으로 변환합니다.
     *
     * @param bytes 파일 크기 (bytes)
     * @return 포맷된 문자열 (예: "10.5 MB", "512 KB", "256 B")
     * @throws IllegalArgumentException bytes가 음수인 경우
     */
    public static String format(long bytes) {
        if (bytes < 0) {
            throw new IllegalArgumentException("File size cannot be negative, but was: " + bytes);
        }

        if (bytes < KILOBYTE) {
            return bytes + " B";
        } else if (bytes < MEGABYTE) {
            return String.format("%.2f KB", bytes / (double) KILOBYTE);
        } else {
            return String.format("%.2f MB", bytes / (double) MEGABYTE);
        }
    }
}
