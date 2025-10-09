package com.ryuqq.fileflow.domain.file;

/**
 * 파일 메타데이터의 값 타입을 정의하는 Enum
 *
 * 메타데이터는 키-값 쌍으로 저장되며, 값의 타입을 명시하여
 * 클라이언트에서 적절한 타입 변환을 수행할 수 있도록 합니다.
 *
 * 사용 예시:
 * - IMAGE 파일: width(NUMBER), height(NUMBER), format(STRING)
 * - VIDEO 파일: duration(NUMBER), codec(STRING), bitrate(NUMBER)
 * - DOCUMENT 파일: page_count(NUMBER), author(STRING), title(STRING)
 * - 복잡한 메타데이터: EXIF 정보(JSON), 위치 정보(JSON)
 *
 * @author sangwon-ryu
 */
public enum MetadataType {
    /**
     * 문자열 타입
     * 예: format="JPEG", author="John Doe", codec="H.264"
     */
    STRING,

    /**
     * 숫자 타입 (정수 또는 실수)
     * 예: width=1920, height=1080, duration=120.5, page_count=42
     */
    NUMBER,

    /**
     * 불리언 타입
     * 예: has_alpha=true, is_progressive=false
     */
    BOOLEAN,

    /**
     * JSON 타입 (복잡한 객체)
     * 예: exif_data={...}, location={lat: 37.5, lng: 127.0}
     */
    JSON;

    /**
     * 주어진 값이 이 타입과 호환되는지 검증합니다.
     *
     * @param value 검증할 값
     * @return 호환되면 true
     */
    public boolean isCompatible(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        return switch (this) {
            case STRING -> true; // 모든 문자열은 STRING 타입과 호환
            case NUMBER -> isNumeric(value);
            case BOOLEAN -> isBoolean(value);
            case JSON -> isJson(value);
        };
    }

    /**
     * 문자열이 숫자인지 확인합니다.
     */
    private boolean isNumeric(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 문자열이 불리언 값인지 확인합니다.
     */
    private boolean isBoolean(String value) {
        String lower = value.toLowerCase();
        return "true".equals(lower) || "false".equals(lower);
    }

    /**
     * 문자열이 JSON 형식인지 확인합니다.
     * 간단한 검증: { 또는 [로 시작하는지 확인
     */
    private boolean isJson(String value) {
        String trimmed = value.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}"))
                || (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }

    /**
     * 타입 이름을 반환합니다.
     *
     * @return 타입 이름 (소문자)
     */
    public String getTypeName() {
        return this.name().toLowerCase();
    }
}
