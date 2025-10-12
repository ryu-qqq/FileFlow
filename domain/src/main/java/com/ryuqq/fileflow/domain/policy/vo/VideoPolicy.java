package com.ryuqq.fileflow.domain.policy.vo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * VideoPolicy Value Object
 * 비디오 파일 정책을 정의하는 불변 객체
 */
public record VideoPolicy(int maxFileSizeMB, int maxFileCount, List<String> allowedFormats, int maxDurationSeconds) {
    private static final int DEFAULT_MAX_FILE_SIZE_MB = 500;
    private static final int DEFAULT_MAX_FILE_COUNT = 5;
    private static final List<String> DEFAULT_ALLOWED_FORMATS = createDefaultFormats();
    private static final int DEFAULT_MAX_DURATION_SECONDS = 600; // 10 minutes

    private static List<String> createDefaultFormats() {
        List<String> formats = new ArrayList<>();
        formats.add("mp4");
        formats.add("avi");
        formats.add("mov");
        return Collections.unmodifiableList(formats);
    }

    public VideoPolicy {
        if (maxFileSizeMB <= 0) {
            throw new IllegalArgumentException("Max file size must be positive: " + maxFileSizeMB);
        }
        if (maxFileCount <= 0) {
            throw new IllegalArgumentException("Max file count must be positive: " + maxFileCount);
        }
        if (allowedFormats == null || allowedFormats.isEmpty()) {
            throw new IllegalArgumentException("Allowed formats cannot be null or empty");
        }
        if (maxDurationSeconds <= 0) {
            throw new IllegalArgumentException("Max duration must be positive: " + maxDurationSeconds);
        }
        // Normalize formats to lowercase for case-insensitive validation
        List<String> normalizedFormats = allowedFormats.stream()
                .map(format -> format.toLowerCase(Locale.ROOT).trim())
                .toList();
        allowedFormats = Collections.unmodifiableList(normalizedFormats);
    }

    /**
     * 기본값으로 VideoPolicy 생성
     */
    public static VideoPolicy createDefault() {
        return new VideoPolicy(
            DEFAULT_MAX_FILE_SIZE_MB,
            DEFAULT_MAX_FILE_COUNT,
            new ArrayList<>(DEFAULT_ALLOWED_FORMATS),
            DEFAULT_MAX_DURATION_SECONDS
        );
    }

    /**
     * 비디오 파일이 정책을 만족하는지 검증
     *
     * @param format    파일 포맷 (예: "mp4", "avi")
     * @param sizeBytes 파일 크기 (바이트)
     * @param durationSeconds 비디오 재생 시간 (초)
     * @throws IllegalArgumentException 정책 위반 시
     */
    public void validate(String format, long sizeBytes, Integer durationSeconds) {
        if (format == null || format.trim().isEmpty()) {
            throw new IllegalArgumentException("Format cannot be null or empty");
        }

        String normalizedFormat = format.toLowerCase(Locale.ROOT).trim();
        if (!allowedFormats.contains(normalizedFormat)) {
            throw new IllegalArgumentException(
                "Format not allowed: " + format + ". Allowed formats: " + allowedFormats
            );
        }

        long maxSizeBytes = (long) maxFileSizeMB * 1024 * 1024;
        if (sizeBytes > maxSizeBytes) {
            throw new IllegalArgumentException(
                "File size exceeds limit: " + sizeBytes + " bytes. Max allowed: " + maxSizeBytes + " bytes"
            );
        }

        if (durationSeconds != null && durationSeconds > maxDurationSeconds) {
            throw new IllegalArgumentException(
                "Video duration exceeds limit: " + durationSeconds + " seconds. Max allowed: " + maxDurationSeconds + " seconds"
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VideoPolicy that = (VideoPolicy) o;
        return maxFileSizeMB == that.maxFileSizeMB &&
            maxFileCount == that.maxFileCount &&
            maxDurationSeconds == that.maxDurationSeconds &&
            allowedFormats.equals(that.allowedFormats);
    }

    @Override
    public String toString() {
        return "VideoPolicy{" +
            "maxFileSizeMB=" + maxFileSizeMB +
            ", maxFileCount=" + maxFileCount +
            ", allowedFormats=" + allowedFormats +
            ", maxDurationSeconds=" + maxDurationSeconds +
            '}';
    }
}
