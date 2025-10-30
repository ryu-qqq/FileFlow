package com.ryuqq.fileflow.application.settings.fixture;

import com.ryuqq.fileflow.application.settings.port.in.UpdateSettingUseCase;

/**
 * UpdateSetting Command Test Fixture
 *
 * <p>테스트에서 UpdateSettingUseCase.Command 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
public class UpdateSettingCommandFixture {

    private static final String DEFAULT_KEY = "app.config.default";
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private UpdateSettingCommandFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final String DEFAULT_VALUE = "updated-value";
    private static final String DEFAULT_LEVEL = "DEFAULT";

    public static UpdateSettingUseCase.Command create() {
        return new UpdateSettingUseCase.Command(
            DEFAULT_KEY,
            DEFAULT_VALUE,
            DEFAULT_LEVEL,
            null
        );
    }

    public static UpdateSettingUseCase.Command create(
        String key,
        String value,
        String level,
        Long contextId
    ) {
        return new UpdateSettingUseCase.Command(
            key,
            value,
            level,
            contextId
        );
    }

    public static UpdateSettingUseCase.Command createDefaultLevel() {
        return new UpdateSettingUseCase.Command(
            "app.default.config",
            "updated-default-value",
            "DEFAULT",
            null
        );
    }

    public static UpdateSettingUseCase.Command createTenantLevel(Long tenantId) {
        return new UpdateSettingUseCase.Command(
            "app.tenant.config",
            "updated-tenant-value",
            "TENANT",
            tenantId
        );
    }

    public static UpdateSettingUseCase.Command createOrgLevel(Long orgId) {
        return new UpdateSettingUseCase.Command(
            "app.org.config",
            "updated-org-value",
            "ORG",
            orgId
        );
    }

    public static UpdateSettingUseCase.Command createNumberUpdate() {
        return new UpdateSettingUseCase.Command(
            "file.max-size",
            "2048",
            "DEFAULT",
            null
        );
    }

    public static UpdateSettingUseCase.Command createBooleanUpdate() {
        return new UpdateSettingUseCase.Command(
            "feature.enabled",
            "false",
            "DEFAULT",
            null
        );
    }

    public static java.util.List<UpdateSettingUseCase.Command> createMultiple(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> new UpdateSettingUseCase.Command(
                "setting.key-" + i,
                "updated-value-" + i,
                "DEFAULT",
                null
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

        public UpdateSettingUseCase.Command build() {
            return new UpdateSettingUseCase.Command(
                key,
                value,
                level,
                contextId
            );
        }
    }
}
