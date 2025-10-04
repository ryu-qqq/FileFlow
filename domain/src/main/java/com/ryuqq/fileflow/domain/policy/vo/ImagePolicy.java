package com.ryuqq.fileflow.domain.policy.vo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * ImagePolicy Value Object
 * 이미지 파일 정책을 정의하는 불변 객체
 */
public record ImagePolicy(int maxFileSizeMB, int maxFileCount, List<String> allowedFormats, Dimension maxDimension) {
    private static final int DEFAULT_MAX_FILE_SIZE_MB = 10;
    private static final int DEFAULT_MAX_FILE_COUNT = 5;
    private static final List<String> DEFAULT_ALLOWED_FORMATS = createDefaultFormats();
    private static final Dimension DEFAULT_MAX_DIMENSION = Dimension.of(4096, 4096);

    private static List<String> createDefaultFormats() {
        List<String> formats = new ArrayList<>();
        formats.add("jpg");
        formats.add("jpeg");
        formats.add("png");
        return Collections.unmodifiableList(formats);
    }

    public ImagePolicy {
        if (maxFileSizeMB <= 0) {
            throw new IllegalArgumentException("Max file size must be positive: " + maxFileSizeMB);
        }
        if (maxFileCount <= 0) {
            throw new IllegalArgumentException("Max file count must be positive: " + maxFileCount);
        }
        if (allowedFormats == null || allowedFormats.isEmpty()) {
            throw new IllegalArgumentException("Allowed formats cannot be null or empty");
        }
        if (maxDimension == null) {
            throw new IllegalArgumentException("Max dimension cannot be null");
        }
        allowedFormats = Collections.unmodifiableList(new ArrayList<>(allowedFormats));
    }

    /**
     * 기본값으로 ImagePolicy 생성
     */
    public static ImagePolicy createDefault() {
        return new ImagePolicy(
            DEFAULT_MAX_FILE_SIZE_MB,
            DEFAULT_MAX_FILE_COUNT,
            new ArrayList<>(DEFAULT_ALLOWED_FORMATS),
            DEFAULT_MAX_DIMENSION
        );
    }

    /**
     * 이미지 파일이 정책을 만족하는지 검증
     *
     * @param format    파일 포맷 (예: "jpg", "png")
     * @param sizeBytes 파일 크기 (바이트)
     * @param dimension 이미지 크기
     * @throws IllegalArgumentException 정책 위반 시
     */
    public void validate(String format, long sizeBytes, Dimension dimension) {
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

        if (dimension != null && !dimension.isWithin(maxDimension)) {
            throw new IllegalArgumentException(
                "Image dimension exceeds limit: " + dimension + ". Max allowed: " + maxDimension
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImagePolicy that = (ImagePolicy) o;
        return maxFileSizeMB == that.maxFileSizeMB &&
            maxFileCount == that.maxFileCount &&
            allowedFormats.equals(that.allowedFormats) &&
            maxDimension.equals(that.maxDimension);
    }

    @Override
    public String toString() {
        return "ImagePolicy{" +
            "maxFileSizeMB=" + maxFileSizeMB +
            ", maxFileCount=" + maxFileCount +
            ", allowedFormats=" + allowedFormats +
            ", maxDimension=" + maxDimension +
            '}';
    }
}
