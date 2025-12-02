package com.ryuqq.fileflow.domain.asset.vo;

/**
 * 이미지 포맷 정보.
 *
 * <p>이미지 처리 시 사용되는 포맷의 타입, 확장자, MIME 타입을 정의한다.
 *
 * <p><strong>사용 예시</strong>:
 *
 * <ul>
 *   <li>WEBP: 최신 웹 포맷 (높은 압축률, 투명도 지원)
 *   <li>JPEG: 손실 압축 포맷 (사진에 적합)
 *   <li>PNG: 무손실 압축 포맷 (투명도 필요 시)
 * </ul>
 *
 * @param type      포맷 타입 (WEBP, JPEG, PNG)
 * @param extension 파일 확장자 (예: "webp", "jpg", "png")
 * @param mimeType  MIME 타입 (예: "image/webp", "image/jpeg")
 */
public record ImageFormat(ImageFormatType type, String extension, String mimeType) {

    // ========================================
    // 표준 상수 정의
    // ========================================

    /** WebP 포맷. 높은 압축률과 품질. */
    public static final ImageFormat WEBP = new ImageFormat(ImageFormatType.WEBP, "webp", "image/webp");

    /** JPEG 포맷. 손실 압축, 사진에 적합. */
    public static final ImageFormat JPEG = new ImageFormat(ImageFormatType.JPEG, "jpg", "image/jpeg");

    /** PNG 포맷. 무손실 압축, 투명도 지원. */
    public static final ImageFormat PNG = new ImageFormat(ImageFormatType.PNG, "png", "image/png");

    /** Compact Constructor (검증 로직). */
    public ImageFormat {
        if (type == null) {
            throw new IllegalArgumentException("이미지 포맷 타입은 null일 수 없습니다.");
        }
        if (extension == null || extension.isBlank()) {
            throw new IllegalArgumentException("이미지 포맷 확장자는 null이거나 빈 값일 수 없습니다.");
        }
        if (mimeType == null || mimeType.isBlank()) {
            throw new IllegalArgumentException("이미지 포맷 MIME 타입은 null이거나 빈 값일 수 없습니다.");
        }
    }

    /**
     * 정적 팩토리 메서드.
     *
     * @param type      포맷 타입
     * @param extension 파일 확장자
     * @param mimeType  MIME 타입
     * @return ImageFormat
     */
    public static ImageFormat of(ImageFormatType type, String extension, String mimeType) {
        return new ImageFormat(type, extension, mimeType);
    }

    /**
     * 원본 확장자로부터 ImageFormat을 생성한다.
     *
     * <p>PNG 확장자는 PNG 포맷으로, WEBP는 WEBP 포맷으로,
     * 그 외(JPG, JPEG, GIF 등)는 JPEG 포맷으로 반환한다.
     *
     * @param originalExtension 원본 파일 확장자
     * @return ImageFormat
     */
    public static ImageFormat fromOriginal(String originalExtension) {
        String lowerExt = originalExtension.toLowerCase();
        return switch (lowerExt) {
            case "png" -> new ImageFormat(ImageFormatType.PNG, lowerExt, "image/png");
            case "webp" -> new ImageFormat(ImageFormatType.WEBP, lowerExt, "image/webp");
            case "jpg", "jpeg" -> new ImageFormat(ImageFormatType.JPEG, lowerExt, "image/jpeg");
            default -> new ImageFormat(ImageFormatType.JPEG, lowerExt, "image/jpeg");
        };
    }
}
