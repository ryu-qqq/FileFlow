package com.ryuqq.fileflow.domain.common.vo;

/**
 * 파일 접근 유형.
 *
 * <p>PUBLIC: CDN(CloudFront)을 통한 직접 접근 가능. 상품 이미지, 배너 등.
 *
 * <p>INTERNAL: Signed URL을 통해서만 접근 가능. 엑셀, 내부 문서 등.
 */
public enum AccessType {
    PUBLIC("공개"),
    INTERNAL("내부");

    private final String displayName;

    AccessType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
