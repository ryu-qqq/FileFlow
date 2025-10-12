package com.ryuqq.fileflow.domain.policy.vo;

/**
 * FileAttributes - 파일 검증에 필요한 속성을 담는 Value Object
 *
 * 목적:
 * - 파일 타입별 검증에 필요한 모든 속성을 하나의 객체로 캡슐화
 * - FileTypePolicies.validate() 메서드의 파라미터 간소화
 * - 확장성 있는 검증 속성 관리
 *
 * 설계 원칙:
 * - Immutability: record를 통한 불변성 보장
 * - Self-Validation: 생성 시 필수 속성 검증
 * - Flexible: nullable 속성을 통해 파일 타입별 다른 요구사항 지원
 *
 * 사용 예시:
 * <pre>{@code
 * // 이미지 파일 속성
 * FileAttributes imageAttrs = FileAttributes.builder()
 *     .sizeBytes(1024000)
 *     .fileCount(1)
 *     .format("jpg")
 *     .dimension(Dimension.of(1920, 1080))
 *     .build();
 *
 * // HTML 파일 속성
 * FileAttributes htmlAttrs = FileAttributes.builder()
 *     .sizeBytes(512000)
 *     .imageCount(5)
 *     .build();
 * }</pre>
 */
public record FileAttributes(
    long sizeBytes,
    int fileCount,
    String format,
    Dimension dimension,
    Integer imageCount,
    Integer sheetCount,
    Integer pageCount,
    Integer durationSeconds
) {

    public FileAttributes {
        if (sizeBytes < 0) {
            throw new IllegalArgumentException("sizeBytes must not be negative: " + sizeBytes);
        }
        if (fileCount < 0) {
            throw new IllegalArgumentException("fileCount must not be negative: " + fileCount);
        }
    }

    /**
     * Builder 패턴을 위한 정적 팩토리 메서드
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * FileAttributes Builder
     */
    public static class Builder {
        private long sizeBytes;
        private int fileCount = 1;
        private String format;
        private Dimension dimension;
        private Integer imageCount;
        private Integer sheetCount;
        private Integer pageCount;
        private Integer durationSeconds;

        public Builder sizeBytes(long sizeBytes) {
            this.sizeBytes = sizeBytes;
            return this;
        }

        public Builder fileCount(int fileCount) {
            this.fileCount = fileCount;
            return this;
        }

        public Builder format(String format) {
            this.format = format;
            return this;
        }

        public Builder dimension(Dimension dimension) {
            this.dimension = dimension;
            return this;
        }

        public Builder imageCount(Integer imageCount) {
            this.imageCount = imageCount;
            return this;
        }

        public Builder sheetCount(Integer sheetCount) {
            this.sheetCount = sheetCount;
            return this;
        }

        public Builder pageCount(Integer pageCount) {
            this.pageCount = pageCount;
            return this;
        }

        public Builder durationSeconds(Integer durationSeconds) {
            this.durationSeconds = durationSeconds;
            return this;
        }

        public FileAttributes build() {
            return new FileAttributes(
                sizeBytes,
                fileCount,
                format,
                dimension,
                imageCount,
                sheetCount,
                pageCount,
                durationSeconds
            );
        }
    }
}
