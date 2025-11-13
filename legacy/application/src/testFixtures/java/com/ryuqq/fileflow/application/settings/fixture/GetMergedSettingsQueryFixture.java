package com.ryuqq.fileflow.application.settings.fixture;

import com.ryuqq.fileflow.application.settings.port.in.GetMergedSettingsUseCase;

import java.util.Map;

/**
 * GetMergedSettings Query Test Fixture
 *
 * <p>테스트에서 GetMergedSettingsUseCase.Query 및 Response 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
public class GetMergedSettingsQueryFixture {
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private GetMergedSettingsQueryFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }


    public static GetMergedSettingsUseCase.Query createQuery() {
        return new GetMergedSettingsUseCase.Query(null, null);
    }

    public static GetMergedSettingsUseCase.Query createQuery(Long orgId, Long tenantId) {
        return new GetMergedSettingsUseCase.Query(orgId, tenantId);
    }

    public static GetMergedSettingsUseCase.Query createQueryWithOrgOnly(Long orgId) {
        return new GetMergedSettingsUseCase.Query(orgId, null);
    }

    public static GetMergedSettingsUseCase.Query createQueryWithTenantOnly(Long tenantId) {
        return new GetMergedSettingsUseCase.Query(null, tenantId);
    }

    public static GetMergedSettingsUseCase.Query createQueryWithBoth(Long orgId, Long tenantId) {
        return new GetMergedSettingsUseCase.Query(orgId, tenantId);
    }

    public static GetMergedSettingsUseCase.Response createResponse() {
        return new GetMergedSettingsUseCase.Response(
            Map.of(
                "app.config.default", "default-value",
                "file.max-size", "1024",
                "feature.enabled", "true"
            )
        );
    }

    public static GetMergedSettingsUseCase.Response createResponse(Map<String, String> settings) {
        return new GetMergedSettingsUseCase.Response(settings);
    }

    public static GetMergedSettingsUseCase.Response createEmptyResponse() {
        return new GetMergedSettingsUseCase.Response(Map.of());
    }

    public static GetMergedSettingsUseCase.Response createDefaultOnlyResponse() {
        return new GetMergedSettingsUseCase.Response(
            Map.of(
                "app.default.config", "default-value",
                "file.max-size", "1024"
            )
        );
    }

    public static GetMergedSettingsUseCase.Response createTenantMergedResponse() {
        return new GetMergedSettingsUseCase.Response(
            Map.of(
                "app.default.config", "default-value",
                "app.tenant.config", "tenant-value",
                "file.max-size", "2048"
            )
        );
    }

    public static GetMergedSettingsUseCase.Response createOrgMergedResponse() {
        return new GetMergedSettingsUseCase.Response(
            Map.of(
                "app.default.config", "default-value",
                "app.org.config", "org-value",
                "file.max-size", "4096"
            )
        );
    }

    public static GetMergedSettingsUseCase.Response createFullMergedResponse() {
        return new GetMergedSettingsUseCase.Response(
            Map.of(
                "app.default.config", "default-value",
                "app.tenant.config", "tenant-value",
                "app.org.config", "org-value",
                "file.max-size", "4096",
                "feature.enabled", "true"
            )
        );
    }

    public static GetMergedSettingsUseCase.Response createWithSecretMasked() {
        return new GetMergedSettingsUseCase.Response(
            Map.of(
                "api.key", "********",
                "db.password", "********",
                "app.config", "normal-value"
            )
        );
    }

    public static ResponseBuilder responseBuilder() {
        return new ResponseBuilder();
    }

    public static class ResponseBuilder {
        private final java.util.Map<String, String> settings = new java.util.HashMap<>();

        public ResponseBuilder addSetting(String key, String value) {
            this.settings.put(key, value);
            return this;
        }

        public ResponseBuilder addSettings(Map<String, String> settings) {
            this.settings.putAll(settings);
            return this;
        }

        public GetMergedSettingsUseCase.Response build() {
            return new GetMergedSettingsUseCase.Response(Map.copyOf(settings));
        }
    }
}
