package com.ryuqq.fileflow.domain.asset.vo;

/**
 * 이미지 변형 정보.
 *
 * <p>이미지 처리 시 생성되는 변형 이미지의 타입과 파일명 접미사를 정의한다.
 *
 * <p><strong>사용 예시</strong>:
 *
 * <ul>
 *   <li>product.jpg → product_large.jpg (LARGE)
 *   <li>product.jpg → product_medium.jpg (MEDIUM)
 *   <li>product.jpg → product_thumb.jpg (THUMBNAIL)
 * </ul>
 *
 * @param type   변형 타입 (ORIGINAL, LARGE, MEDIUM, THUMBNAIL)
 * @param suffix 파일명에 추가될 접미사 (예: "_large", "_thumb")
 */
public record ImageVariant(ImageVariantType type, String suffix) {

    // ========================================
    // 표준 상수 정의
    // ========================================

    /** 원본 이미지 (리사이징 없음). */
    public static final ImageVariant ORIGINAL = new ImageVariant(ImageVariantType.ORIGINAL, "");

    /** 대형 이미지 (상세 보기용). */
    public static final ImageVariant LARGE = new ImageVariant(ImageVariantType.LARGE, "_large");

    /** 중형 이미지 (목록 보기용). */
    public static final ImageVariant MEDIUM = new ImageVariant(ImageVariantType.MEDIUM, "_medium");

    /** 썸네일 이미지 (미리보기용). */
    public static final ImageVariant THUMBNAIL = new ImageVariant(ImageVariantType.THUMBNAIL, "_thumb");

    /** Compact Constructor (검증 로직). */
    public ImageVariant {
        if (type == null) {
            throw new IllegalArgumentException("이미지 변형 타입은 null일 수 없습니다.");
        }
        if (suffix == null) {
            throw new IllegalArgumentException("이미지 변형 suffix는 null일 수 없습니다.");
        }
    }

    /**
     * 정적 팩토리 메서드.
     *
     * @param type   변형 타입
     * @param suffix 파일명 접미사
     * @return ImageVariant
     */
    public static ImageVariant of(ImageVariantType type, String suffix) {
        return new ImageVariant(type, suffix);
    }

    /**
     * 리사이즈가 필요한지 확인한다.
     *
     * @return ORIGINAL이 아니면 true
     */
    public boolean requiresResize() {
        return type != ImageVariantType.ORIGINAL;
    }
}
