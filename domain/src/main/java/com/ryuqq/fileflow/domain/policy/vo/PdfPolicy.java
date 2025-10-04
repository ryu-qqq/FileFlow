package com.ryuqq.fileflow.domain.policy.vo;

/**
 * PdfPolicy Value Object
 * PDF 파일 정책을 정의하는 불변 객체
 */
public record PdfPolicy(int maxFileSizeMB, int maxPageCount) {
    public PdfPolicy {
        if (maxFileSizeMB
            <= 0) {
            throw new IllegalArgumentException("Max file size must be positive: "
                + maxFileSizeMB);
        }
        if (maxPageCount
            <= 0) {
            throw new IllegalArgumentException("Max page count must be positive: "
                + maxPageCount);
        }
    }

    /**
     * PDF 파일이 정책을 만족하는지 검증
     *
     * @param sizeBytes 파일 크기 (바이트)
     * @param pageCount 페이지 개수
     * @throws IllegalArgumentException 정책 위반 시
     */
    public void validate(long sizeBytes, int pageCount) {
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

        if (pageCount
            > maxPageCount) {
            throw new IllegalArgumentException(
                "Page count exceeds limit: "
                    + pageCount
                    + ". Max allowed: "
                    + maxPageCount
            );
        }
    }

    @Override
    public String toString() {
        return "PdfPolicy{"
            +
            "maxFileSizeMB="
            + maxFileSizeMB
            +
            ", maxPageCount="
            + maxPageCount
            +
            '}';
    }
}
