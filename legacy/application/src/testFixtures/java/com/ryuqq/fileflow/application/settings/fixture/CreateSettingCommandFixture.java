package com.ryuqq.fileflow.application.settings.fixture;

import com.ryuqq.fileflow.application.settings.port.in.CreateSettingUseCase;

/**
 * CreateSetting Command Test Fixture
 *
 * <p>테스트에서 CreateSettingUseCase.Command 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
public class CreateSettingCommandFixture {

    private static final String DEFAULT_KEY = "app.config.default";
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private CreateSettingCommandFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final String DEFAULT_VALUE = "default-value";
    private static final String DEFAULT_LEVEL = "DEFAULT";
    private static final String DEFAULT_VALUE_TYPE = "STRING";

    public static CreateSettingUseCase.Command create() {
        return new CreateSettingUseCase.Command(
            DEFAULT_KEY,
            DEFAULT_VALUE,
            DEFAULT_LEVEL,
            null,
            DEFAULT_VALUE_TYPE,
            false
        );
    }

    public static CreateSettingUseCase.Command create(
        String key,
        String value,
        String level,
        Long contextId,
        String valueType,
        boolean secret
    ) {
        return new CreateSettingUseCase.Command(
            key,
            value,
            level,
            contextId,
            valueType,
            secret
        );
    }

    public static CreateSettingUseCase.Command createDefaultLevel() {
        return new CreateSettingUseCase.Command(
            "app.default.config",
            "default-value",
            "DEFAULT",
            null,
            "STRING",
            false
        );
    }

    public static CreateSettingUseCase.Command createTenantLevel(Long tenantId) {
        return new CreateSettingUseCase.Command(
            "app.tenant.config",
            "tenant-value",
            "TENANT",
            tenantId,
            "STRING",
            false
        );
    }

    public static CreateSettingUseCase.Command createOrgLevel(Long orgId) {
        return new CreateSettingUseCase.Command(
            "app.org.config",
            "org-value",
            "ORG",
            orgId,
            "STRING",
            false
        );
    }

    public static CreateSettingUseCase.Command createSecretSetting() {
        return new CreateSettingUseCase.Command(
            "api.key",
            "secret-api-key-12345",
            "DEFAULT",
            null,
            "STRING",
            true
        );
    }

    public static CreateSettingUseCase.Command createNumberSetting() {
        return new CreateSettingUseCase.Command(
            "file.max-size",
            "1024",
            "DEFAULT",
            null,
            "NUMBER",
            false
        );
    }

    public static CreateSettingUseCase.Command createBooleanSetting() {
        return new CreateSettingUseCase.Command(
            "feature.enabled",
            "true",
            "DEFAULT",
            null,
            "BOOLEAN",
            false
        );
    }

    public static CreateSettingUseCase.Command createJsonObjectSetting() {
        return new CreateSettingUseCase.Command(
            "app.json-config",
            "{\"key\":\"value\"}",
            "DEFAULT",
            null,
            "JSON_OBJECT",
            false
        );
    }

    public static java.util.List<CreateSettingUseCase.Command> createMultiple(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> new CreateSettingUseCase.Command(
                "setting.key-" + i,
                "value-" + i,
                "DEFAULT",
                null,
                "STRING",
                false
            ))
            .toList();
    }

    public static CommandBuilder builder() {
        return new CommandBuilder();
    }

    public static class CommandBuilder {
        private String key = DEFAULT_KEY;
        private String value = DEFAULT_VALUE;
        private String level = DEFAULT_LEVEL;
        private Long contextId = null;
        private String valueType = DEFAULT_VALUE_TYPE;
        private boolean secret = false;

        public CommandBuilder key(String key) {
            this.key = key;
            return this;
        }

        public CommandBuilder value(String value) {
            this.value = value;
            return this;
        }

        public CommandBuilder level(String level) {
            this.level = level;
            return this;
        }

        public CommandBuilder contextId(Long contextId) {
            this.contextId = contextId;
            return this;
        }

        public CommandBuilder valueType(String valueType) {
            this.valueType = valueType;
            return this;
        }

        public CommandBuilder secret(boolean secret) {
            this.secret = secret;
            return this;
        }

        public CreateSettingUseCase.Command build() {
            return new CreateSettingUseCase.Command(
                key,
                value,
                level,
                contextId,
                valueType,
                secret
            );
        }
    }
}
