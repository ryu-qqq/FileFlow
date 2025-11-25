package com.ryuqq.fileflow.domain.session.vo;

/**
 * S3 Presigned URL VO.
 *
 * <p>Presigned URL의 유효성을 검증하고 비즈니스 로직을 캡슐화합니다.
 */
public record PresignedUrl(String value) {

    /**
     * Presigned URL 생성 팩토리 메서드.
     *
     * @param url Presigned URL 문자열
     * @return PresignedUrl
     * @throws IllegalArgumentException URL이 null이거나 빈 문자열인 경우
     */
    public static PresignedUrl of(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Presigned URL은 null 또는 빈 문자열일 수 없습니다.");
        }
        if (!url.startsWith("https://")) {
            throw new IllegalArgumentException("Presigned URL은 HTTPS로 시작해야 합니다.");
        }
        return new PresignedUrl(url);
    }

    /**
     * Presigned URL이 유효한지 확인합니다.
     *
     * @return 유효하면 true
     */
    public boolean isValid() {
        return value != null && !value.isBlank() && value.startsWith("https://");
    }
}
