package com.ryuqq.fileflow.domain.policy.vo;

/**
 * ExcelPolicy Value Object
 * Excel 파일 정책을 정의하는 불변 객체
 */
public record ExcelPolicy(int maxFileSizeMB, int maxSheetCount) {
    public ExcelPolicy {
        if (maxFileSizeMB
            <= 0) {
            throw new IllegalArgumentException("Max file size must be positive: "
                + maxFileSizeMB);
        }
        if (maxSheetCount
            <= 0) {
            throw new IllegalArgumentException("Max sheet count must be positive: "
                + maxSheetCount);
        }
    }

    /**
     * Excel 파일이 정책을 만족하는지 검증
     *
     * @param sizeBytes  파일 크기 (바이트)
     * @param sheetCount 시트 개수
     * @throws IllegalArgumentException 정책 위반 시
     */
    public void validate(long sizeBytes, int sheetCount) {
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

        if (sheetCount
            > maxSheetCount) {
            throw new IllegalArgumentException(
                "Sheet count exceeds limit: "
                    + sheetCount
                    + ". Max allowed: "
                    + maxSheetCount
            );
        }
    }

    @Override
    public String toString() {
        return "ExcelPolicy{"
            +
            "maxFileSizeMB="
            + maxFileSizeMB
            +
            ", maxSheetCount="
            + maxSheetCount
            +
            '}';
    }
}
