package com.ryuqq.fileflow.application.settings.fixture;

import com.ryuqq.fileflow.application.settings.dto.SettingResponse;

import java.time.LocalDateTime;

/**
 * SettingResponse Test Fixture
 *
 * <p>테스트에서 SettingResponse 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
public class SettingResponseFixture {

    private static final Long DEFAULT_ID = 1L;
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private SettingResponseFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final String DEFAULT_KEY = "app.config.default";
    private static final String DEFAULT_VALUE = "default-value";
    private static final String DEFAULT_TYPE = "STRING";
    private static final String DEFAULT_LEVEL = "DEFAULT";

    public static SettingResponse create() {
        LocalDateTime now = LocalDateTime.now();
        return new SettingResponse(
            DEFAULT_ID,
            DEFAULT_KEY,
            DEFAULT_VALUE,
            DEFAULT_TYPE,
            DEFAULT_LEVEL,
            null,
            false,
            now,
            now
        );
    }

    public static SettingResponse create(
        Long id,
        String key,
        String value,
        String type,
        String level,
        Long contextId,
        boolean isSecret
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new SettingResponse(
            id,
            key,
            value,
            type,
            level,
            contextId,
            isSecret,
            now,
            now
        );
    }

    public static SettingResponse createDefaultLevel() {
        LocalDateTime now = LocalDateTime.now();
        return new SettingResponse(
            1L,
            "app.default.config",
            "default-value",
            "STRING",
            "DEFAULT",
            null,
            false,
            now,
            now
        );
    }

    public static SettingResponse createTenantLevel(Long tenantId) {
        LocalDateTime now = LocalDateTime.now();
        return new SettingResponse(
            1L,
            "app.tenant.config",
            "tenant-value",
            "STRING",
            "TENANT",
            tenantId,
            false,
            now,
            now
        );
    }

    public static SettingResponse createOrgLevel(Long orgId) {
        LocalDateTime now = LocalDateTime.now();
        return new SettingResponse(
            1L,
            "app.org.config",
            "org-value",
            "STRING",
            "ORG",
            orgId,
            false,
            now,
            now
        );
    }

    public static SettingResponse createSecretSetting() {
        LocalDateTime now = LocalDateTime.now();
        return new SettingResponse(
            1L,
            "api.key",
            "********",
            "STRING",
            "DEFAULT",
            null,
            true,
            now,
            now
        );
    }

    public static SettingResponse createNumberSetting() {
        LocalDateTime now = LocalDateTime.now();
        return new SettingResponse(
            1L,
            "file.max-size",
            "1024",
            "NUMBER",
            "DEFAULT",
            null,
            false,
            now,
            now
        );
    }

    public static SettingResponse createBooleanSetting() {
        LocalDateTime now = LocalDateTime.now();
        return new SettingResponse(
            1L,
            "feature.enabled",
            "true",
            "BOOLEAN",
            "DEFAULT",
            null,
            false,
            now,
            now
        );
    }

    public static SettingResponse createJsonObjectSetting() {
        LocalDateTime now = LocalDateTime.now();
        return new SettingResponse(
            1L,
            "app.json-config",
            "{\"key\":\"value\"}",
            "JSON_OBJECT",
            "DEFAULT",
            null,
            false,
            now,
            now
        );
    }

    public static java.util.List<SettingResponse> createMultiple(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        LocalDateTime now = LocalDateTime.now();
        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> new SettingResponse(
                (long) i,
                "setting.key-" + i,
                "value-" + i,
                "STRING",
                "DEFAULT",
                null,
                false,
                now,
                now
            ))
            .toList();
    }

    public static java.util.List<SettingResponse> createMultiple(String level, Long contextId, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        LocalDateTime now = LocalDateTime.now();
        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> new SettingResponse(
                (long) i,
                "setting.key-" + i,
                "value-" + i,
                "STRING",
                level,
                contextId,
                false,
                now,
                now
            ))
            .toList();
    }

    public static SettingResponseBuilder builder() {
        return new SettingResponseBuilder();
    }

    public static class SettingResponseBuilder {
        private Long id = DEFAULT_ID;
        private String key = DEFAULT_KEY;
        private String value = DEFAULT_VALUE;
        private String type = DEFAULT_TYPE;
        private String level = DEFAULT_LEVEL;
        private Long contextId = null;
        private boolean isSecret = false;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public SettingResponseBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public SettingResponseBuilder key(String key) {
            this.key = key;
            return this;
        }

        public SettingResponseBuilder value(String value) {
            this.value = value;
            return this;
        }

        public SettingResponseBuilder type(String type) {
            this.type = type;
            return this;
        }

        public SettingResponseBuilder level(String level) {
            this.level = level;
            return this;
        }

        public SettingResponseBuilder contextId(Long contextId) {
            this.contextId = contextId;
            return this;
        }

        public SettingResponseBuilder secret(boolean isSecret) {
            this.isSecret = isSecret;
            return this;
        }

        public SettingResponseBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public SettingResponseBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public SettingResponse build() {
            return new SettingResponse(
                id,
                key,
                value,
                type,
                level,
                contextId,
                isSecret,
                createdAt,
                updatedAt
            );
        }
    }
}
