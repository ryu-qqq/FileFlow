package com.ryuqq.fileflow.domain.asset.vo;

/**
 * 이미지 변형 타입.
 *
 * <p>이미지 처리 시 생성되는 다양한 크기의 변형 이미지를 정의한다.
 *
 * <p><strong>크기 정보</strong>:
 *
 * <ul>
 *   <li>ORIGINAL: 원본 이미지 (크기 변경 없음, maxWidth/maxHeight = null)
 *   <li>LARGE: 대형 이미지 (1200x1200)
 *   <li>MEDIUM: 중형 이미지 (600x600)
 *   <li>THUMBNAIL: 썸네일 이미지 (200x200)
 * </ul>
 *
 * <p><strong>설계 결정</strong>: 가공된 이미지의 크기는 스펙에서 결정되므로, ProcessedFileAsset에 dimension을 저장하지 않습니다. 대신
 * ImageVariantType에서 조회합니다.
 */
public enum ImageVariantType {

    /** 원본 이미지. 리사이징 없이 원본 그대로 저장. */
    ORIGINAL(null, null),

    /** 대형 이미지. 상세 보기용 (1200x1200). */
    LARGE(1200, 1200),

    /** 중형 이미지. 목록 보기용 (600x600). */
    MEDIUM(600, 600),

    /** 썸네일 이미지. 미리보기용 (200x200). */
    THUMBNAIL(200, 200);

    private final Integer maxWidth;
    private final Integer maxHeight;

    ImageVariantType(Integer maxWidth, Integer maxHeight) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    /**
     * 최대 너비를 반환합니다.
     *
     * <p>ORIGINAL의 경우 null을 반환합니다 (원본 크기 유지).
     *
     * @return 최대 너비 (px) 또는 null
     */
    public Integer maxWidth() {
        return maxWidth;
    }

    /**
     * 최대 높이를 반환합니다.
     *
     * <p>ORIGINAL의 경우 null을 반환합니다 (원본 크기 유지).
     *
     * @return 최대 높이 (px) 또는 null
     */
    public Integer maxHeight() {
        return maxHeight;
    }

    /**
     * 리사이즈가 필요한지 확인합니다.
     *
     * @return ORIGINAL이 아니면 true
     */
    public boolean requiresResize() {
        return this != ORIGINAL;
    }
}
