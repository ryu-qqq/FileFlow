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
        if (value == null) {
            return false;
        }

        return switch (this) {
            case STRING -> true; // 모든 문자열(빈 문자열 포함)은 STRING 타입과 호환
            case NUMBER -> !value.trim().isEmpty() && isNumeric(value);
            case BOOLEAN -> !value.trim().isEmpty() && isBoolean(value);
            case JSON -> !value.trim().isEmpty() && isJson(value);
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
     *
     * 검증 항목:
     * - JSON 객체({...}) 또는 배열([...]) 형식
     * - 최소 길이 검증 (빈 객체/배열 {} [] 포함)
     * - 따옴표 쌍 일치 검증 (escape되지 않은 따옴표가 짝수개)
     */
    private boolean isJson(String value) {
        String trimmed = value.trim();

        // JSON 객체 또는 배열 형식 확인
        boolean isObject = trimmed.startsWith("{") && trimmed.endsWith("}");
        boolean isArray = trimmed.startsWith("[") && trimmed.endsWith("]");

        if (!isObject && !isArray) {
            return false;
        }

        // 최소 길이 검증 (빈 객체 {} 또는 빈 배열 [] 포함)
        if (trimmed.length() < 2) {
            return false;
        }

        // 따옴표 불일치 검증: escape되지 않은 따옴표가 짝수개여야 함
        int quoteCount = 0;
        boolean escaped = false;

        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);

            if (escaped) {
                escaped = false;
                continue;
            }

            if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                quoteCount++;
            }
        }

        // 따옴표가 홀수개면 잘못된 JSON
        return quoteCount % 2 == 0;
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
