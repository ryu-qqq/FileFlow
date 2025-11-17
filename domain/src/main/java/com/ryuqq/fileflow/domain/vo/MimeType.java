package com.ryuqq.fileflow.domain.vo;

import java.util.Set;

/**
 * MimeType Value Object
 * <p>
 * MIME 타입을 검증하고 캡슐화합니다.
 * </p>
 *
 * <p>
 * 검증 규칙:
 * - 허용 목록 기반 검증 (화이트리스트)
 * - Null/Empty 불가
 * </p>
 *
 * <p>
 * 허용된 MIME 타입:
 * - 이미지: image/jpeg, image/png, image/gif, image/webp
 * - 문서: application/pdf, text/plain, text/html
 * - 데이터: application/json, application/xml
 * - 미디어: video/mp4, audio/mpeg
 * </p>
 */
public record MimeType(String value) {

    /**
     * 허용된 MIME 타입 목록 (화이트리스트)
     */
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            // 이미지
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/svg+xml",

            // 문서
            "application/pdf",
            "text/plain",
            "text/html",
            "text/css",
            "text/csv",

            // 데이터
            "application/json",
            "application/xml",
            "text/xml",

            // 미디어
            "video/mp4",
            "video/mpeg",
            "audio/mpeg",
            "audio/wav",
            "audio/mp3",

            // Office
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    /**
     * 이미지 MIME 타입 접두사
     */
    private static final String IMAGE_PREFIX = "image/";

    /**
     * PDF MIME 타입
     */
    private static final String PDF_MIME_TYPE = "application/pdf";

    /**
     * Compact Constructor (Record 검증 패턴)
     * <p>
     * MIME 타입 검증 로직을 수행합니다.
     * </p>
     */
    public MimeType {
        validateNotNullOrEmpty(value);
        validateAllowedMimeType(value);
    }

    /**
     * 정적 팩토리 메서드 (of 패턴)
     *
     * @param value MIME 타입
     * @return MimeType VO
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static MimeType of(String value) {
        return new MimeType(value);
    }

    /**
     * Null 또는 Empty 검증
     */
    private static void validateNotNullOrEmpty(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("MIME 타입은 null이거나 빈 값일 수 없습니다");
        }
    }

    /**
     * 허용 목록 검증 (화이트리스트)
     */
    private static void validateAllowedMimeType(String value) {
        if (!ALLOWED_MIME_TYPES.contains(value)) {
            throw new IllegalArgumentException(
                    String.format("허용되지 않은 MIME 타입입니다: %s", value)
            );
        }
    }

    /**
     * 이미지 MIME 타입 여부 확인
     *
     * @return 이미지 타입이면 true, 아니면 false
     */
    public boolean isImage() {
        return value.startsWith(IMAGE_PREFIX);
    }

    /**
     * PDF MIME 타입 여부 확인
     *
     * @return PDF 타입이면 true, 아니면 false
     */
    public boolean isPdf() {
        return PDF_MIME_TYPE.equals(value);
    }

    /**
     * MIME 타입 값 조회
     *
     * @return MIME 타입 문자열
     */
    public String getValue() {
        return value;
    }
}
