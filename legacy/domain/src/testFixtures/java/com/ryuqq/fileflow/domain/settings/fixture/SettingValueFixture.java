package com.ryuqq.fileflow.domain.settings.fixture;

import com.ryuqq.fileflow.domain.settings.*;

/**
 * SettingValue Test Fixture
 *
 * <p>테스트에서 SettingValue 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
public class SettingValueFixture {

    private static final String DEFAULT_STRING_VALUE = "default-value";
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private SettingValueFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final String DEFAULT_NUMBER_VALUE = "100";
    private static final String DEFAULT_BOOLEAN_VALUE = "true";
    private static final String DEFAULT_JSON_OBJECT = "{\"key\":\"value\"}";
    private static final String DEFAULT_JSON_ARRAY = "[\"item1\",\"item2\"]";

    public static SettingValue create() {
        return SettingValue.of(DEFAULT_STRING_VALUE, SettingType.STRING);
    }

    public static SettingValue create(String value, SettingType type) {
        return SettingValue.of(value, type);
    }

    public static SettingValue createString(String value) {
        return SettingValue.of(value, SettingType.STRING);
    }

    public static SettingValue createNumber(String value) {
        return SettingValue.of(value, SettingType.NUMBER);
    }

    public static SettingValue createBoolean(String value) {
        return SettingValue.of(value, SettingType.BOOLEAN);
    }

    public static SettingValue createJsonObject(String value) {
        return SettingValue.of(value, SettingType.JSON_OBJECT);
    }

    public static SettingValue createJsonArray(String value) {
        return SettingValue.of(value, SettingType.JSON_ARRAY);
    }

    public static SettingValue createSecret(String value, SettingType type) {
        return SettingValue.secret(value, type);
    }

    public static SettingValue createDefaultString() {
        return SettingValue.of(DEFAULT_STRING_VALUE, SettingType.STRING);
    }

    public static SettingValue createDefaultNumber() {
        return SettingValue.of(DEFAULT_NUMBER_VALUE, SettingType.NUMBER);
    }

    public static SettingValue createDefaultBoolean() {
        return SettingValue.of(DEFAULT_BOOLEAN_VALUE, SettingType.BOOLEAN);
    }

    public static SettingValue createDefaultJsonObject() {
        return SettingValue.of(DEFAULT_JSON_OBJECT, SettingType.JSON_OBJECT);
    }

    public static SettingValue createDefaultJsonArray() {
        return SettingValue.of(DEFAULT_JSON_ARRAY, SettingType.JSON_ARRAY);
    }

    public static SettingValue createSecretApiKey() {
        return SettingValue.secret("secret-api-key-12345", SettingType.STRING);
    }

    public static SettingValue createSecretPassword() {
        return SettingValue.secret("P@ssw0rd!123", SettingType.STRING);
    }

    public static java.util.List<SettingValue> createMultiple(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> SettingValue.of("value-" + i, SettingType.STRING))
            .toList();
    }

    public static java.util.List<SettingValue> createMultiple(SettingType type, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> {
                String value = switch (type) {
                    case STRING -> "value-" + i;
                    case NUMBER -> String.valueOf(i);
                    case BOOLEAN -> i % 2 == 0 ? "true" : "false";
                    case JSON_OBJECT -> "{\"key" + i + "\":\"value" + i + "\"}";
                    case JSON_ARRAY -> "[\"item" + i + "\"]";
                };
                return SettingValue.of(value, type);
            })
            .toList();
    }
}
