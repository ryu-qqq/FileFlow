package com.ryuqq.fileflow.domain.policy.vo;

/**
 * HtmlPolicy Value Object
 * HTML 파일 정책을 정의하는 불변 객체
 */
public record HtmlPolicy(int maxFileSizeMB, int maxImageCount, boolean downloadExternalImages) {
    public HtmlPolicy {
        if (maxFileSizeMB
            <= 0) {
            throw new IllegalArgumentException("Max file size must be positive: "
                + maxFileSizeMB);
        }
        if (maxImageCount
            <= 0) {
            throw new IllegalArgumentException("Max image count must be positive: "
                + maxImageCount);
        }
    }

    /**
     * HTML 파일이 정책을 만족하는지 검증
     *
     * @param sizeBytes  파일 크기 (바이트)
     * @param imageCount HTML 내 이미지 개수
     * @throws IllegalArgumentException 정책 위반 시
     */
    public void validate(long sizeBytes, int imageCount) {
        long maxSizeBytes = (long) maxFileSizeMB
            * 1024
            * 1024;
        if (sizeBytes
            > maxSizeBytes) {
            throw new IllegalArgumentException(
                "File size exceeds limit: "
                    + sizeBytes
                    + " bytes. Max allowed: "
                    + maxSizeBytes
                    + " bytes"
            );
        }

        if (imageCount
            > maxImageCount) {
            throw new IllegalArgumentException(
                "Image count exceeds limit: "
                    + imageCount
                    + ". Max allowed: "
                    + maxImageCount
            );
        }
    }

    @Override
    public String toString() {
        return "HtmlPolicy{"
            +
            "maxFileSizeMB="
            + maxFileSizeMB
            +
            ", maxImageCount="
            + maxImageCount
            +
            ", downloadExternalImages="
            + downloadExternalImages
            +
            '}';
    }
}
