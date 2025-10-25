package com.ryuqq.fileflow.application.settings.service;

import com.ryuqq.fileflow.application.settings.dto.GetMergedSettingsQuery;
import com.ryuqq.fileflow.application.settings.dto.MergedSettingsResponse;
import com.ryuqq.fileflow.domain.settings.Setting;
import com.ryuqq.fileflow.domain.settings.SettingMerger;
import com.ryuqq.fileflow.fixtures.SettingFixtures;
import com.ryuqq.fileflow.domain.settings.SettingRepository;
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

    private SettingRepository settingRepository;
    private SettingMerger settingMerger;
    private GetMergedSettingsUseCase getMergedSettingsUseCase;

    @BeforeEach
    void setUp() {
        settingRepository = mock(SettingRepository.class);
        settingMerger = new SettingMerger();
        getMergedSettingsUseCase = new GetMergedSettingsUseCase(settingRepository, settingMerger);
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
            GetMergedSettingsQuery query = new GetMergedSettingsQuery(orgId, tenantId);

            Setting orgSetting = SettingFixtures.createOrgSetting(orgId);           // MAX_UPLOAD_SIZE = 200MB
            Setting tenantSetting = SettingFixtures.createTenantSetting(tenantId);  // MAX_UPLOAD_SIZE = 50MB
            Setting defaultSetting = SettingFixtures.createDefaultSetting();        // MAX_UPLOAD_SIZE = 100MB

            List<Setting> orgSettings = List.of(orgSetting);
            List<Setting> tenantSettings = List.of(tenantSetting);
            List<Setting> defaultSettings = List.of(defaultSetting);

            SettingRepository.SettingsForMerge settingsForMerge = new SettingRepository.SettingsForMerge(
                orgSettings, tenantSettings, defaultSettings
            );

            when(settingRepository.findAllForMerge(eq(orgId), eq(tenantId)))
                .thenReturn(settingsForMerge);

            // Act
            MergedSettingsResponse response = getMergedSettingsUseCase.execute(query);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getSettings()).containsEntry("MAX_UPLOAD_SIZE", "200MB"); // ORG 우선

            verify(settingRepository).findAllForMerge(eq(orgId), eq(tenantId));
        }

        @Test
        @DisplayName("ORG가 없으면 TENANT > DEFAULT 우선순위로 병합된다")
        void shouldMergeWithTenantPriorityWhenNoOrg() {
            // Arrange
            Long tenantId = 100L;
            GetMergedSettingsQuery query = new GetMergedSettingsQuery(null, tenantId);

            Setting tenantSetting = SettingFixtures.createTenantSetting(tenantId);  // MAX_UPLOAD_SIZE = 50MB
            Setting defaultSetting = SettingFixtures.createDefaultSetting();        // MAX_UPLOAD_SIZE = 100MB

            List<Setting> orgSettings = List.of();
            List<Setting> tenantSettings = List.of(tenantSetting);
            List<Setting> defaultSettings = List.of(defaultSetting);

            SettingRepository.SettingsForMerge settingsForMerge = new SettingRepository.SettingsForMerge(
                orgSettings, tenantSettings, defaultSettings
            );

            when(settingRepository.findAllForMerge(eq(null), eq(tenantId)))
                .thenReturn(settingsForMerge);

            // Act
            MergedSettingsResponse response = getMergedSettingsUseCase.execute(query);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getSettings()).containsEntry("MAX_UPLOAD_SIZE", "50MB"); // TENANT 우선

            verify(settingRepository).findAllForMerge(eq(null), eq(tenantId));
        }

        @Test
        @DisplayName("ORG와 TENANT가 없으면 DEFAULT만 반환된다")
        void shouldReturnOnlyDefaultWhenNoOrgAndTenant() {
            // Arrange
            GetMergedSettingsQuery query = new GetMergedSettingsQuery(null, null);

            Setting defaultSetting = SettingFixtures.createDefaultSetting(); // MAX_UPLOAD_SIZE = 100MB

            List<Setting> orgSettings = List.of();
            List<Setting> tenantSettings = List.of();
            List<Setting> defaultSettings = List.of(defaultSetting);

            SettingRepository.SettingsForMerge settingsForMerge = new SettingRepository.SettingsForMerge(
                orgSettings, tenantSettings, defaultSettings
            );

            when(settingRepository.findAllForMerge(eq(null), eq(null)))
                .thenReturn(settingsForMerge);

            // Act
            MergedSettingsResponse response = getMergedSettingsUseCase.execute(query);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getSettings()).containsEntry("MAX_UPLOAD_SIZE", "100MB"); // DEFAULT만

            verify(settingRepository).findAllForMerge(eq(null), eq(null));
        }

        @Test
        @DisplayName("비밀 설정은 마스킹되어 병합된다")
        void shouldMaskSecretSettings() {
            // Arrange
            GetMergedSettingsQuery query = new GetMergedSettingsQuery(null, null);

            Setting defaultSecret = SettingFixtures.createDefaultSecretSetting(); // API_KEY = secret-key-123 (masked)
            Setting defaultNormal = SettingFixtures.createDefaultSetting();       // MAX_UPLOAD_SIZE = 100MB

            List<Setting> orgSettings = List.of();
            List<Setting> tenantSettings = List.of();
            List<Setting> defaultSettings = List.of(defaultSecret, defaultNormal);

            SettingRepository.SettingsForMerge settingsForMerge = new SettingRepository.SettingsForMerge(
                orgSettings, tenantSettings, defaultSettings
            );

            when(settingRepository.findAllForMerge(any(), any()))
                .thenReturn(settingsForMerge);

            // Act
            MergedSettingsResponse response = getMergedSettingsUseCase.execute(query);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getSettings())
                .containsEntry("API_KEY", "********")          // 마스킹됨
                .containsEntry("MAX_UPLOAD_SIZE", "100MB");    // 일반 값
        }

        @Test
        @DisplayName("모든 레벨이 비어있으면 빈 Map을 반환한다")
        void shouldReturnEmptyMapWhenAllLevelsEmpty() {
            // Arrange
            GetMergedSettingsQuery query = new GetMergedSettingsQuery(null, null);

            List<Setting> orgSettings = List.of();
            List<Setting> tenantSettings = List.of();
            List<Setting> defaultSettings = List.of();

            SettingRepository.SettingsForMerge settingsForMerge = new SettingRepository.SettingsForMerge(
                orgSettings, tenantSettings, defaultSettings
            );

            when(settingRepository.findAllForMerge(any(), any()))
                .thenReturn(settingsForMerge);

            // Act
            MergedSettingsResponse response = getMergedSettingsUseCase.execute(query);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getSettings()).isEmpty();
        }
    }

    @Nested
    @DisplayName("예외 시나리오")
    class ExceptionScenarios {

        @Test
        @DisplayName("Query가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenQueryIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> getMergedSettingsUseCase.execute(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Query는 필수입니다");
        }
    }

    @Nested
    @DisplayName("Query 검증")
    class QueryValidationScenarios {

        @Test
        @DisplayName("orgId와 tenantId가 모두 null이어도 Query 생성 가능")
        void shouldCreateQueryWithBothIdsNull() {
            // Act
            GetMergedSettingsQuery query = new GetMergedSettingsQuery(null, null);

            // Assert
            assertThat(query).isNotNull();
            assertThat(query.getOrgId()).isNull();
            assertThat(query.getTenantId()).isNull();
        }

        @Test
        @DisplayName("orgId만 있어도 Query 생성 가능")
        void shouldCreateQueryWithOnlyOrgId() {
            // Act
            GetMergedSettingsQuery query = new GetMergedSettingsQuery(1L, null);

            // Assert
            assertThat(query).isNotNull();
            assertThat(query.getOrgId()).isEqualTo(1L);
            assertThat(query.getTenantId()).isNull();
        }

        @Test
        @DisplayName("tenantId만 있어도 Query 생성 가능")
        void shouldCreateQueryWithOnlyTenantId() {
            // Act
            GetMergedSettingsQuery query = new GetMergedSettingsQuery(null, 100L);

            // Assert
            assertThat(query).isNotNull();
            assertThat(query.getOrgId()).isNull();
            assertThat(query.getTenantId()).isEqualTo(100L);
        }
    }
}
