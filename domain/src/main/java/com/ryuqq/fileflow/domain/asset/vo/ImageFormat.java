package com.ryuqq.fileflow.domain.asset.vo;

public record ImageFormat(ImageFormatType type, String extension, String mimeType) {

    public static final ImageFormat WEBP = new ImageFormat(ImageFormatType.WEBP, "webp", "image/webp");
    public static final ImageFormat JPEG = new ImageFormat(ImageFormatType.JPEG, "jpg", "image/jpeg");
    public static final ImageFormat PNG = new ImageFormat(ImageFormatType.PNG, "png", "image/png");

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

    public static ImageFormat of(ImageFormatType type, String extension, String mimeType) {
        return new ImageFormat(type, extension, mimeType);
    }

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
