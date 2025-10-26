package com.ryuqq.fileflow.domain.settings;

/**
 * Setting Type Enum
 *
 * <p>설정 값의 데이터 타입을 정의합니다.</p>
 * <p>JSON 스키마 검증 시 타입별로 다른 검증 로직을 적용합니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Domain 불변 객체 - 타입 정의</li>
 *   <li>✅ JSON 스키마 검증 지원</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
public enum SettingType {
    /**
     * 문자열 타입
     */
    STRING,

    /**
     * 숫자 타입 (정수, 실수 포함)
     */
    NUMBER,

    /**
     * 불린 타입
     */
    BOOLEAN,

    /**
     * JSON 객체 타입
     */
    JSON_OBJECT,

    /**
     * JSON 배열 타입
     */
    JSON_ARRAY;

    /**
     * 문자열로부터 SettingType을 생성합니다.
     *
     * <p>대소문자 무시하고 변환합니다.</p>
     *
     * @param typeStr 타입 문자열
     * @return SettingType
     * @throws IllegalArgumentException 유효하지 않은 타입인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static SettingType fromString(String typeStr) {
        if (typeStr == null || typeStr.isBlank()) {
            throw new IllegalArgumentException("Setting 타입은 필수입니다");
        }

        try {
            return SettingType.valueOf(typeStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "유효하지 않은 Setting 타입입니다: " + typeStr +
                ". 사용 가능한 타입: STRING, NUMBER, BOOLEAN, JSON_OBJECT, JSON_ARRAY"
            );
        }
    }

    /**
     * 주어진 값이 현재 타입과 호환되는지 검증합니다.
     *
     * <p>Law of Demeter 준수: 타입 검증 로직 캡슐화</p>
     *
     * @param value 검증할 값
     * @return 타입과 호환되면 true
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public boolean isCompatibleWith(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        return switch (this) {
            case STRING -> true; // 모든 문자열 허용
            case NUMBER -> isNumeric(value);
            case BOOLEAN -> isBooleanValue(value);
            case JSON_OBJECT -> isJsonObject(value);
            case JSON_ARRAY -> isJsonArray(value);
        };
    }

    /**
     * 문자열이 숫자인지 검증합니다.
     *
     * @param value 검증할 값
     * @return 숫자이면 true
     * @author ryu-qqq
     * @since 2025-10-25
     */
    private static boolean isNumeric(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 문자열이 불린 값인지 검증합니다.
     *
     * @param value 검증할 값
     * @return 불린 값이면 true
     * @author ryu-qqq
     * @since 2025-10-25
     */
    private static boolean isBooleanValue(String value) {
        String lowerValue = value.toLowerCase();
        return "true".equals(lowerValue) || "false".equals(lowerValue);
    }

    /**
     * 문자열이 JSON 객체인지 검증합니다.
     *
     * @param value 검증할 값
     * @return JSON 객체이면 true
     * @author ryu-qqq
     * @since 2025-10-25
     */
    private static boolean isJsonObject(String value) {
        String trimmed = value.trim();
        return trimmed.startsWith("{") && trimmed.endsWith("}");
    }

    /**
     * 문자열이 JSON 배열인지 검증합니다.
     *
     * @param value 검증할 값
     * @return JSON 배열이면 true
     * @author ryu-qqq
     * @since 2025-10-25
     */
    private static boolean isJsonArray(String value) {
        String trimmed = value.trim();
        return trimmed.startsWith("[") && trimmed.endsWith("]");
    }
}
