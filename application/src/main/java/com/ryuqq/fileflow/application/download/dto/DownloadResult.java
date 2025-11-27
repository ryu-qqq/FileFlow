package com.ryuqq.fileflow.application.download.dto;

/**
 * HTTP 다운로드 결과 DTO.
 *
 * @param content 다운로드된 파일 콘텐츠
 * @param contentType Content-Type (MIME type)
 * @param contentLength 콘텐츠 길이 (bytes)
 */
public record DownloadResult(byte[] content, String contentType, long contentLength) {

    public DownloadResult {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("content must not be empty");
        }
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("contentType must not be blank");
        }
        if (contentLength <= 0) {
            throw new IllegalArgumentException("contentLength must be positive");
        }
    }

    /**
     * Content-Type에서 확장자를 추출합니다.
     *
     * @return 확장자 (예: "jpg", "png", "gif")
     */
    public String getExtension() {
        return switch (contentType.toLowerCase()) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            case "image/svg+xml" -> "svg";
            case "image/bmp" -> "bmp";
            case "image/tiff" -> "tiff";
            default -> {
                int slashIndex = contentType.lastIndexOf('/');
                yield slashIndex >= 0 ? contentType.substring(slashIndex + 1) : "bin";
            }
        };
    }
}
