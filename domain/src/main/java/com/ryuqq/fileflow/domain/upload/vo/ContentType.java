package com.ryuqq.fileflow.domain.upload.vo;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * 파일 MIME 타입 Value Object
 *
 * 불변성:
 * - record 타입으로 모든 필드는 final이며 생성 후 변경 불가
 * - MIME 타입 형식 검증
 *
 * 용도:
 * - 파일 타입 검증 및 분류
 * - Content-Type 헤더 설정
 */
public record ContentType(String value) {

    // MIME 타입 형식 검증용 Pattern (성능 최적화)
    private static final Pattern MIME_TYPE_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9][a-zA-Z0-9!#$&^_.+-]*/[a-zA-Z0-9][a-zA-Z0-9!#$&^_.+-]*$"
    );

    // 일반적인 이미지 MIME 타입
    private static final Set<String> IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/svg+xml"
    );

    // 일반적인 비디오 MIME 타입
    private static final Set<String> VIDEO_TYPES = Set.of(
            "video/mp4",
            "video/mpeg",
            "video/webm",
            "video/quicktime"
    );

    // 일반적인 문서 MIME 타입
    private static final Set<String> DOCUMENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    /**
     * Compact constructor로 검증 로직 수행
     */
    public ContentType {
        validateValue(value);
    }

    /**
     * 주어진 문자열로부터 ContentType을 생성합니다.
     *
     * @param value MIME 타입 문자열
     * @return ContentType 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 값인 경우
     */
    public static ContentType of(String value) {
        return new ContentType(value);
    }

    /**
     * 이미지 타입인지 확인합니다.
     *
     * @return 이미지 타입이면 true
     */
    public boolean isImage() {
        return IMAGE_TYPES.contains(value.toLowerCase());
    }

    /**
     * 비디오 타입인지 확인합니다.
     *
     * @return 비디오 타입이면 true
     */
    public boolean isVideo() {
        return VIDEO_TYPES.contains(value.toLowerCase());
    }

    /**
     * 문서 타입인지 확인합니다.
     *
     * @return 문서 타입이면 true
     */
    public boolean isDocument() {
        return DOCUMENT_TYPES.contains(value.toLowerCase());
    }

    /**
     * 주 타입을 반환합니다 (예: image/jpeg → image).
     *
     * @return 주 타입 문자열
     */
    public String getMainType() {
        int slashIndex = value.indexOf('/');
        if (slashIndex > 0) {
            return value.substring(0, slashIndex);
        }
        return value;
    }

    /**
     * 서브 타입을 반환합니다 (예: image/jpeg → jpeg).
     *
     * @return 서브 타입 문자열
     */
    public String getSubType() {
        int slashIndex = value.indexOf('/');
        if (slashIndex > 0 && slashIndex < value.length() - 1) {
            return value.substring(slashIndex + 1);
        }
        return "";
    }

    // ========== Validation Methods ==========

    private static void validateValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("ContentType cannot be null or empty");
        }

        // MIME 타입 형식 검증: type/subtype (컴파일된 Pattern 사용으로 성능 최적화)
        if (!MIME_TYPE_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "ContentType must follow MIME type format (e.g., 'image/jpeg')"
            );
        }
    }
}
