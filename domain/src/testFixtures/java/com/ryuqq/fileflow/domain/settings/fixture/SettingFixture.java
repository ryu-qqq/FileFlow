package com.ryuqq.fileflow.domain.settings.fixture;

import com.ryuqq.fileflow.domain.settings.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Setting Test Fixture
 *
 * <p>테스트에서 Setting 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
public class SettingFixture {

    private static final String DEFAULT_KEY = "app.config.default";
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private SettingFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final String DEFAULT_VALUE = "default-value";

    public static Setting createNew() {
        return Setting.forNew(
            SettingKey.of(DEFAULT_KEY),
            SettingValue.of(DEFAULT_VALUE, SettingType.STRING),
            SettingLevel.DEFAULT,
            null
        );
    }

    public static Setting createNew(String key, String value, SettingLevel level, Long contextId) {
        return Setting.forNew(
            SettingKey.of(key),
            SettingValue.of(value, SettingType.STRING),
            level,
            contextId
        );
    }

    public static Setting createWithId(Long id) {
        return Setting.of(
            SettingId.of(id),
            SettingKey.of(DEFAULT_KEY),
            SettingValue.of(DEFAULT_VALUE, SettingType.STRING),
            SettingLevel.DEFAULT,
            null
        );
    }

    public static Setting createWithId(Long id, String key, String value, SettingLevel level, Long contextId) {
        return Setting.of(
            SettingId.of(id),
            SettingKey.of(key),
            SettingValue.of(value, SettingType.STRING),
            level,
            contextId
        );
    }

    public static Setting createDefaultLevel() {
        return Setting.forNew(
            SettingKey.of("app.default.config"),
            SettingValue.of("default-value", SettingType.STRING),
            SettingLevel.DEFAULT,
            null
        );
    }

    public static Setting createTenantLevel(Long tenantId) {
        return Setting.forNew(
            SettingKey.of("app.tenant.config"),
            SettingValue.of("tenant-value", SettingType.STRING),
            SettingLevel.TENANT,
            tenantId
        );
    }

    public static Setting createOrgLevel(Long orgId) {
        return Setting.forNew(
            SettingKey.of("app.org.config"),
            SettingValue.of("org-value", SettingType.STRING),
            SettingLevel.ORG,
            orgId
        );
    }

    public static Setting createSecretSetting() {
        return Setting.forNew(
            SettingKey.of("api.key"),
            SettingValue.secret("secret-api-key-12345", SettingType.STRING),
            SettingLevel.DEFAULT,
            null
        );
    }

    public static Setting createNumberSetting() {
        return Setting.forNew(
            SettingKey.of("file.max-size"),
            SettingValue.of("1024", SettingType.NUMBER),
            SettingLevel.DEFAULT,
            null
        );
    }

    public static Setting createBooleanSetting() {
        return Setting.forNew(
            SettingKey.of("feature.enabled"),
            SettingValue.of("true", SettingType.BOOLEAN),
            SettingLevel.DEFAULT,
            null
        );
    }

    public static Setting createJsonObjectSetting() {
        return Setting.forNew(
            SettingKey.of("app.json-config"),
            SettingValue.of("{\"key\":\"value\"}", SettingType.JSON_OBJECT),
            SettingLevel.DEFAULT,
            null
        );
    }

    public static Setting createJsonArraySetting() {
        return Setting.forNew(
            SettingKey.of("app.array-config"),
            SettingValue.of("[\"item1\",\"item2\"]", SettingType.JSON_ARRAY),
            SettingLevel.DEFAULT,
            null
        );
    }

    public static java.util.List<Setting> createMultiple(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> Setting.of(
                SettingId.of((long) i),
                SettingKey.of("setting.key-" + i),
                SettingValue.of("value-" + i, SettingType.STRING),
                SettingLevel.DEFAULT,
                null
            ))
            .toList();
    }

    public static java.util.List<Setting> createMultiple(SettingLevel level, Long contextId, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> Setting.of(
                SettingId.of((long) i),
                SettingKey.of("setting.key-" + i),
                SettingValue.of("value-" + i, SettingType.STRING),
                level,
                contextId
            ))
            .toList();
    }

    public static Setting reconstitute(
        Long id,
        String key,
        String value,
        SettingType type,
        SettingLevel level,
        Long contextId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return Setting.reconstitute(
            SettingId.of(id),
            SettingKey.of(key),
            SettingValue.of(value, type),
            level,
            contextId,
            createdAt,
            updatedAt
        );
    }

    public static SettingBuilder builder() {
        return new SettingBuilder();
    }

    public static class SettingBuilder {
        private Long id;
        private String key = DEFAULT_KEY;
        private String value = DEFAULT_VALUE;
        private SettingType type = SettingType.STRING;
        private boolean isSecret = false;
        private SettingLevel level = SettingLevel.DEFAULT;
        private Long contextId = null;
        private Clock clock = Clock.systemDefaultZone();
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public SettingBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public SettingBuilder key(String key) {
            this.key = key;
            return this;
        }

        public SettingBuilder value(String value) {
            this.value = value;
            return this;
        }

        public SettingBuilder type(SettingType type) {
            this.type = type;
            return this;
        }

        public SettingBuilder secret(boolean isSecret) {
            this.isSecret = isSecret;
            return this;
        }

        public SettingBuilder level(SettingLevel level) {
            this.level = level;
            return this;
        }

        public SettingBuilder contextId(Long contextId) {
            this.contextId = contextId;
            return this;
        }

        public SettingBuilder clock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public SettingBuilder fixedClock(Instant instant) {
            this.clock = Clock.fixed(instant, ZoneId.systemDefault());
            return this;
        }

        public SettingBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public SettingBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Setting build() {
            SettingValue settingValue = isSecret
                ? SettingValue.secret(value, type)
                : SettingValue.of(value, type);

            if (id == null) {
                return Setting.forNew(
                    SettingKey.of(key),
                    settingValue,
                    level,
                    contextId
                );
            }

            if (createdAt == null && updatedAt == null) {
                return Setting.of(
                    SettingId.of(id),
                    SettingKey.of(key),
                    settingValue,
                    level,
                    contextId
                );
            }

            LocalDateTime now = LocalDateTime.now(clock);
            return Setting.reconstitute(
                SettingId.of(id),
                SettingKey.of(key),
                settingValue,
                level,
                contextId,
                createdAt != null ? createdAt : now,
                updatedAt != null ? updatedAt : now
            );
        }
    }
}
