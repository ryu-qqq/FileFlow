package com.ryuqq.fileflow.domain.settings.fixture;

import com.ryuqq.fileflow.domain.settings.*;

/**
 * SettingKey Test Fixture
 *
 * <p>테스트에서 SettingKey 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
public class SettingKeyFixture {

    private static final String DEFAULT_KEY = "app.config.default";
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private SettingKeyFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }


    public static SettingKey create() {
        return SettingKey.of(DEFAULT_KEY);
    }

    public static SettingKey create(String value) {
        return SettingKey.of(value);
    }

    public static SettingKey createApiKey() {
        return SettingKey.of("api.key");
    }

    public static SettingKey createPassword() {
        return SettingKey.of("db.password");
    }

    public static SettingKey createSecret() {
        return SettingKey.of("app.secret");
    }

    public static SettingKey createMaxFileSize() {
        return SettingKey.of("file.max-size");
    }

    public static SettingKey createEnableFeature() {
        return SettingKey.of("feature.enabled");
    }

    public static SettingKey createJsonConfig() {
        return SettingKey.of("app.json-config");
    }

    public static java.util.List<SettingKey> createMultiple(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> SettingKey.of("setting.key-" + i))
            .toList();
    }

    public static java.util.List<SettingKey> createMultiple(String prefix, int count) {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("prefix는 필수입니다");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> SettingKey.of(prefix + "." + i))
            .toList();
    }

    public static String invalidKeyTooLong() {
        return "a".repeat(101);
    }

    public static String invalidKeySpecialChars() {
        return "invalid@key";
    }

    public static String invalidKeyBlank() {
        return "";
    }
}
