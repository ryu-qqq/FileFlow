package com.ryuqq.fileflow.domain.image.vo;

import java.util.Arrays;

/**
 * 지원되는 이미지 포맷을 정의하는 Enum
 *
 * 비즈니스 규칙:
 * - JPEG, PNG, GIF, WebP 포맷을 지원
 * - WebP는 최적화 대상 포맷
 * - 각 포맷은 MIME 타입과 매핑
 */
public enum ImageFormat {

    JPEG("image/jpeg", "jpg", true, false),
    PNG("image/png", "png", true, true),
    GIF("image/gif", "gif", false, true),
    WEBP("image/webp", "webp", true, true);

    private final String mimeType;
    private final String extension;
    private final boolean compressible;
    private final boolean supportsTransparency;

    ImageFormat(String mimeType, String extension, boolean compressible, boolean supportsTransparency) {
        this.mimeType = mimeType;
        this.extension = extension;
        this.compressible = compressible;
        this.supportsTransparency = supportsTransparency;
    }

    /**
     * MIME 타입으로부터 ImageFormat을 찾습니다.
     *
     * @param mimeType MIME 타입
     * @return ImageFormat
     * @throws IllegalArgumentException 지원하지 않는 포맷인 경우
     */
    public static ImageFormat fromMimeType(String mimeType) {
        if (mimeType == null || mimeType.trim().isEmpty()) {
            throw new IllegalArgumentException("MimeType cannot be null or empty");
        }

        String normalizedMimeType = mimeType.toLowerCase().trim();

        return Arrays.stream(values())
                .filter(format -> format.mimeType.equals(normalizedMimeType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported image format: " + mimeType +
                        ". Supported formats: JPEG, PNG, GIF, WebP"
                ));
    }

    /**
     * 파일 확장자로부터 ImageFormat을 찾습니다.
     *
     * @param extension 파일 확장자
     * @return ImageFormat
     * @throws IllegalArgumentException 지원하지 않는 포맷인 경우
     */
    public static ImageFormat fromExtension(String extension) {
        if (extension == null || extension.trim().isEmpty()) {
            throw new IllegalArgumentException("Extension cannot be null or empty");
        }

        String normalizedExtension = extension.toLowerCase().trim()
                .replaceFirst("^\\.", ""); // 앞의 점 제거

        return Arrays.stream(values())
                .filter(format -> format.extension.equals(normalizedExtension))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported image extension: " + extension +
                        ". Supported extensions: jpg, png, gif, webp"
                ));
    }

    /**
     * WebP 포맷으로 변환 가능한지 확인합니다.
     *
     * @return WebP 변환 가능 여부
     */
    public boolean isConvertibleToWebP() {
        return this != WEBP && compressible;
    }

    /**
     * 이미 WebP 포맷인지 확인합니다.
     *
     * @return WebP 포맷 여부
     */
    public boolean isWebP() {
        return this == WEBP;
    }

    /**
     * 압축 가능한 포맷인지 확인합니다.
     *
     * @return 압축 가능 여부
     */
    public boolean isCompressible() {
        return compressible;
    }

    /**
     * 투명도를 지원하는 포맷인지 확인합니다.
     *
     * @return 투명도 지원 여부
     */
    public boolean supportsTransparency() {
        return supportsTransparency;
    }

    /**
     * MIME 타입을 반환합니다.
     *
     * @return MIME 타입
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * 파일 확장자를 반환합니다.
     *
     * @return 파일 확장자
     */
    public String getExtension() {
        return extension;
    }
}
