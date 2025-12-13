package com.ryuqq.fileflow.domain.asset.vo;

/**
 * 이미지 리사이징 명세.
 *
 * <p>이미지 변형(variant)과 포맷(format)의 조합을 정의하는 순수 도메인 VO입니다.
 *
 * <p><strong>사용 예시</strong>:
 *
 * <ul>
 *   <li>LARGE + WEBP → 대형 WebP 이미지
 *   <li>THUMBNAIL + JPEG → 썸네일 JPEG 이미지
 * </ul>
 *
 * <p><strong>DDD 원칙</strong>:
 *
 * <ul>
 *   <li>순수 도메인 개념 (기술적 의존성 없음)
 *   <li>Immutable Value Object
 *   <li>Self-validating (검증 로직 내장)
 * </ul>
 *
 * @param variant 이미지 변형 타입 (ORIGINAL, LARGE, MEDIUM, THUMBNAIL)
 * @param format 이미지 포맷 (WEBP, JPEG, PNG)
 */
public record ImageResizingSpec(ImageVariant variant, ImageFormat format) {

    /** Compact Constructor (검증 로직). */
    public ImageResizingSpec {
        if (variant == null) {
            throw new IllegalArgumentException("이미지 변형 타입은 null일 수 없습니다.");
        }
        if (format == null) {
            throw new IllegalArgumentException("이미지 포맷은 null일 수 없습니다.");
        }
    }

    /**
     * 정적 팩토리 메서드.
     *
     * @param variant 이미지 변형 타입
     * @param format 이미지 포맷
     * @return ImageResizingSpec
     */
    public static ImageResizingSpec of(ImageVariant variant, ImageFormat format) {
        return new ImageResizingSpec(variant, format);
    }

    /**
     * 리사이징 명세 식별자를 반환합니다.
     *
     * <p>예: "LARGE_WEBP", "THUMBNAIL_JPEG"
     *
     * @return "variant_format" 형식의 식별자
     */
    public String specId() {
        return variant.type().name() + "_" + format.type().name();
    }

    /**
     * 리사이즈가 필요한지 확인합니다.
     *
     * @return ORIGINAL이 아니면 true
     */
    public boolean requiresResize() {
        return variant.requiresResize();
    }

    /**
     * 파일명 접미사를 반환합니다.
     *
     * <p>예: "_large", "_thumb"
     *
     * @return 파일명에 추가될 접미사
     */
    public String suffix() {
        return variant.suffix();
    }

    /**
     * 파일 확장자를 반환합니다.
     *
     * <p>예: "webp", "jpg"
     *
     * @return 파일 확장자
     */
    public String extension() {
        return format.extension();
    }

    /**
     * MIME 타입을 반환합니다.
     *
     * <p>예: "image/webp", "image/jpeg"
     *
     * @return MIME 타입
     */
    public String mimeType() {
        return format.mimeType();
    }
}
