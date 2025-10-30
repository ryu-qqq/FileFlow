package com.ryuqq.fileflow.application.settings.service;

import com.ryuqq.fileflow.application.settings.port.in.GetMergedSettingsUseCase;
import com.ryuqq.fileflow.application.settings.port.out.LoadSettingsPort;
import com.ryuqq.fileflow.application.settings.service.query.GetMergedSettingsService;
import com.ryuqq.fileflow.domain.settings.Setting;
import com.ryuqq.fileflow.domain.settings.fixture.SettingDomainFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GetMergedSettingsUseCase 테스트
 *
 * <p>3레벨 병합(ORG > TENANT > DEFAULT) UseCase의 정확성을 검증합니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
@Tag("unit")
@Tag("application")
@Tag("fast")
@DisplayName("GetMergedSettingsUseCase 테스트")
class GetMergedSettingsUseCaseTest {

    private LoadSettingsPort loadSettingsPort;
    private GetMergedSettingsService getMergedSettingsService;

    @BeforeEach
    void setUp() {
        loadSettingsPort = mock(LoadSettingsPort.class);
        getMergedSettingsService = new GetMergedSettingsService(loadSettingsPort);
    }

    @Nested
    @DisplayName("정상 시나리오")
    class SuccessScenarios {

        @Test
        @DisplayName("ORG, TENANT, DEFAULT 모두 있을 때 ORG 우선순위로 병합된다")
        void shouldMergeWithOrgPriority() {
            // Arrange
            Long orgId = 1L;
            Long tenantId = 100L;
            GetMergedSettingsUseCase.Query query = new GetMergedSettingsUseCase.Query(orgId, tenantId);

            Setting orgSetting = SettingDomainFixture.createOrgSetting(orgId);           // MAX_UPLOAD_SIZE = 200MB
            Setting tenantSetting = SettingDomainFixture.createTenantSetting(tenantId);  // MAX_UPLOAD_SIZE = 50MB
            Setting defaultSetting = SettingDomainFixture.createDefaultSetting();        // MAX_UPLOAD_SIZE = 100MB

            List<Setting> orgSettings = List.of(orgSetting);
            List<Setting> tenantSettings = List.of(tenantSetting);
            List<Setting> defaultSettings = List.of(defaultSetting);

            LoadSettingsPort.SettingsForMerge settingsForMerge = new LoadSettingsPort.SettingsForMerge(
                orgSettings, tenantSettings, defaultSettings
            );

            when(loadSettingsPort.findAllForMerge(eq(orgId), eq(tenantId)))
                .thenReturn(settingsForMerge);

            // Act
            GetMergedSettingsUseCase.Response response = getMergedSettingsService.execute(query);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.settings()).containsEntry("MAX_UPLOAD_SIZE", "200MB"); // ORG 우선

            verify(loadSettingsPort).findAllForMerge(eq(orgId), eq(tenantId));
        }

        @Test
        @DisplayName("ORG가 없으면 TENANT > DEFAULT 우선순위로 병합된다")
        void shouldMergeWithTenantPriorityWhenNoOrg() {
            // Arrange
            Long tenantId = 100L;
            GetMergedSettingsUseCase.Query query = new GetMergedSettingsUseCase.Query(null, tenantId);

            Setting tenantSetting = SettingDomainFixture.createTenantSetting(tenantId);  // MAX_UPLOAD_SIZE = 50MB
            Setting defaultSetting = SettingDomainFixture.createDefaultSetting();        // MAX_UPLOAD_SIZE = 100MB

            List<Setting> orgSettings = List.of();
            List<Setting> tenantSettings = List.of(tenantSetting);
            List<Setting> defaultSettings = List.of(defaultSetting);

            LoadSettingsPort.SettingsForMerge settingsForMerge = new LoadSettingsPort.SettingsForMerge(
                orgSettings, tenantSettings, defaultSettings
            );

            when(loadSettingsPort.findAllForMerge(eq(null), eq(tenantId)))
                .thenReturn(settingsForMerge);

            // Act
            GetMergedSettingsUseCase.Response response = getMergedSettingsService.execute(query);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.settings()).containsEntry("MAX_UPLOAD_SIZE", "50MB"); // TENANT 우선

            verify(loadSettingsPort).findAllForMerge(eq(null), eq(tenantId));
        }

        @Test
        @DisplayName("ORG와 TENANT가 없으면 DEFAULT만 반환된다")
        void shouldReturnOnlyDefaultWhenNoOrgAndTenant() {
            // Arrange
            GetMergedSettingsUseCase.Query query = new GetMergedSettingsUseCase.Query(null, null);

            Setting defaultSetting = SettingDomainFixture.createDefaultSetting(); // MAX_UPLOAD_SIZE = 100MB

            List<Setting> orgSettings = List.of();
            List<Setting> tenantSettings = List.of();
            List<Setting> defaultSettings = List.of(defaultSetting);

            LoadSettingsPort.SettingsForMerge settingsForMerge = new LoadSettingsPort.SettingsForMerge(
                orgSettings, tenantSettings, defaultSettings
            );

            when(loadSettingsPort.findAllForMerge(eq(null), eq(null)))
                .thenReturn(settingsForMerge);

            // Act
            GetMergedSettingsUseCase.Response response = getMergedSettingsService.execute(query);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.settings()).containsEntry("MAX_UPLOAD_SIZE", "100MB"); // DEFAULT만

            verify(loadSettingsPort).findAllForMerge(eq(null), eq(null));
        }

        @Test
        @DisplayName("비밀 설정은 마스킹되어 병합된다")
        void shouldMaskSecretSettings() {
            // Arrange
            GetMergedSettingsUseCase.Query query = new GetMergedSettingsUseCase.Query(null, null);

            Setting defaultSecret = SettingDomainFixture.createDefaultSecretSetting(); // API_KEY = secret-key-123 (masked)
            Setting defaultNormal = SettingDomainFixture.createDefaultSetting();       // MAX_UPLOAD_SIZE = 100MB

            List<Setting> orgSettings = List.of();
            List<Setting> tenantSettings = List.of();
            List<Setting> defaultSettings = List.of(defaultSecret, defaultNormal);

            LoadSettingsPort.SettingsForMerge settingsForMerge = new LoadSettingsPort.SettingsForMerge(
                orgSettings, tenantSettings, defaultSettings
            );

            when(loadSettingsPort.findAllForMerge(any(), any()))
                .thenReturn(settingsForMerge);

            // Act
            GetMergedSettingsUseCase.Response response = getMergedSettingsService.execute(query);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.settings())
                .containsEntry("API_KEY", "********")          // 마스킹됨
                .containsEntry("MAX_UPLOAD_SIZE", "100MB");    // 일반 값
        }

        @Test
        @DisplayName("모든 레벨이 비어있으면 빈 Map을 반환한다")
        void shouldReturnEmptyMapWhenAllLevelsEmpty() {
            // Arrange
            GetMergedSettingsUseCase.Query query = new GetMergedSettingsUseCase.Query(null, null);

            List<Setting> orgSettings = List.of();
            List<Setting> tenantSettings = List.of();
            List<Setting> defaultSettings = List.of();

            LoadSettingsPort.SettingsForMerge settingsForMerge = new LoadSettingsPort.SettingsForMerge(
                orgSettings, tenantSettings, defaultSettings
            );

            when(loadSettingsPort.findAllForMerge(any(), any()))
                .thenReturn(settingsForMerge);

            // Act
            GetMergedSettingsUseCase.Response response = getMergedSettingsService.execute(query);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.settings()).isEmpty();
        }
    }

    @Nested
    @DisplayName("예외 시나리오")
    class ExceptionScenarios {

        @Test
        @DisplayName("Query가 null이면 NullPointerException 발생")
        void shouldThrowExceptionWhenQueryIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> getMergedSettingsService.execute(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Query 검증")
    class QueryValidationScenarios {

        @Test
        @DisplayName("orgId와 tenantId가 모두 null이어도 Query 생성 가능")
        void shouldCreateQueryWithBothIdsNull() {
            // Act
            GetMergedSettingsUseCase.Query query = new GetMergedSettingsUseCase.Query(null, null);

            // Assert
            assertThat(query).isNotNull();
            assertThat(query.orgId()).isNull();
            assertThat(query.tenantId()).isNull();
        }

        @Test
        @DisplayName("orgId만 있어도 Query 생성 가능")
        void shouldCreateQueryWithOnlyOrgId() {
            // Act
            GetMergedSettingsUseCase.Query query = new GetMergedSettingsUseCase.Query(1L, null);

            // Assert
            assertThat(query).isNotNull();
            assertThat(query.orgId()).isEqualTo(1L);
            assertThat(query.tenantId()).isNull();
        }

        @Test
        @DisplayName("tenantId만 있어도 Query 생성 가능")
        void shouldCreateQueryWithOnlyTenantId() {
            // Act
            GetMergedSettingsUseCase.Query query = new GetMergedSettingsUseCase.Query(null, 100L);

            // Assert
            assertThat(query).isNotNull();
            assertThat(query.orgId()).isNull();
            assertThat(query.tenantId()).isEqualTo(100L);
        }
    }
}
