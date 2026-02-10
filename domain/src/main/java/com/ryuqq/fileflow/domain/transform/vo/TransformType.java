package com.ryuqq.fileflow.domain.transform.vo;

/**
 * 이미지 변환 유형.
 *
 * <p>모든 변환은 이미지 파일에만 적용됩니다.
 */
public enum TransformType {
    RESIZE("리사이즈"),
    CONVERT("포맷 변환"),
    COMPRESS("압축"),
    THUMBNAIL("썸네일 생성");

    private final String displayName;

    TransformType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
