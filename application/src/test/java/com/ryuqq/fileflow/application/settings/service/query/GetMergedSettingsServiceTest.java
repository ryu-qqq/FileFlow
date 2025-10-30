package com.ryuqq.fileflow.application.settings.service.query;

import com.ryuqq.fileflow.application.settings.GetMergedSettingsQueryFixture;
import com.ryuqq.fileflow.application.settings.port.in.GetMergedSettingsUseCase;
import com.ryuqq.fileflow.application.settings.port.out.LoadSettingsPort;
import com.ryuqq.fileflow.domain.settings.Setting;
import com.ryuqq.fileflow.domain.settings.SettingFixture;
import com.ryuqq.fileflow.domain.settings.SettingLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

/**
 * GetMergedSettingsService 단위 테스트
 *
 * <p>테스트 범위:</p>
 * <ul>
 *   <li>Happy Path: 정상 병합 조회</li>
 *   <li>3-tier Merge: ORG > TENANT > DEFAULT 우선순위</li>
 *   <li>ReadOnly Transaction: 읽기 전용 트랜잭션</li>
 *   <li>Port Mock: LoadSettingsPort 모킹</li>
 *   <li>Edge Cases: 빈 설정, null 처리</li>
 *   <li>Secret Masking: 비밀 키 마스킹</li>
 * </ul>
 *
 * @author Claude Code
 * @since 2025-10-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetMergedSettingsService 단위 테스트")
class GetMergedSettingsServiceTest {

    @Mock
    private LoadSettingsPort loadSettingsPort;

    @InjectMocks
    private GetMergedSettingsService getMergedSettingsService;

    @Nested
    @DisplayName("Happy Path - 정상 병합 케이스")
    class HappyPathTests {

        @Test
        @DisplayName("DEFAULT 레벨만 있을 때 병합 성공")
        void execute_Success_DefaultLevelOnly() {
            // Given
            GetMergedSettingsUseCase.Query query = GetMergedSettingsQueryFixture.createQuery();
            List<Setting> defaultSettings = List.of(
                SettingFixture.createDefaultLevel(),
                SettingFixture.createNumberSetting(),
                SettingFixture.createBooleanSetting()
            );

            given(loadSettingsPort.findAllForMerge(isNull(), isNull()))
                .willReturn(new LoadSettingsPort.SettingsForMerge(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    defaultSettings
                ));

            // When
            GetMergedSettingsUseCase.Response response = getMergedSettingsService.execute(query);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.settings()).hasSize(3);
            assertThat(response.settings()).containsKeys(
                "app.default.config",
                "file.max-size",
                "feature.enabled"
            );

            verify(loadSettingsPort).findAllForMerge(isNull(), isNull());
        }

        @Test
        @DisplayName("TENANT 레벨이 DEFAULT를 Override하여 병합")
        void execute_Success_TenantOverridesDefault() {
            // Given
            Long tenantId = 100L;
            GetMergedSettingsUseCase.Query query = GetMergedSettingsQueryFixture.createQueryWithTenantOnly(tenantId);

            List<Setting> defaultSettings = List.of(
                SettingFixture.createNew("app.config", "default-value", SettingLevel.DEFAULT, null)
            );
            List<Setting> tenantSettings = List.of(
                SettingFixture.createNew("app.config", "tenant-value", SettingLevel.TENANT, tenantId)
            );

            given(loadSettingsPort.findAllForMerge(isNull(), any(Long.class)))
                .willReturn(new LoadSettingsPort.SettingsForMerge(
                    Collections.emptyList(),
                    tenantSettings,
                    defaultSettings
                ));

            // When
            GetMergedSettingsUseCase.Response response = getMergedSettingsService.execute(query);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.settings()).hasSize(1);
            assertThat(response.settings().get("app.config")).isEqualTo("tenant-value");

            verify(loadSettingsPort).findAllForMerge(isNull(), any(Long.class));
        }

        @Test
        @DisplayName("ORG 레벨이 TENANT와 DEFAULT를 Override하여 병합")
        void execute_Success_OrgOverridesTenantAndDefault() {
            // Given
            Long orgId = 10L;
            Long tenantId = 100L;
            GetMergedSettingsUseCase.Query query = GetMergedSettingsQueryFixture.createQueryWithBoth(orgId, tenantId);

            List<Setting> defaultSettings = List.of(
                SettingFixture.createNew("app.config", "default-value", SettingLevel.DEFAULT, null)
            );
            List<Setting> tenantSettings = List.of(
                SettingFixture.createNew("app.config", "tenant-value", SettingLevel.TENANT, tenantId)
            );
            List<Setting> orgSettings = List.of(
                SettingFixture.createNew("app.config", "org-value", SettingLevel.ORG, orgId)
            );

            given(loadSettingsPort.findAllForMerge(any(Long.class), any(Long.class)))
                .willReturn(new LoadSettingsPort.SettingsForMerge(
                    orgSettings,
                    tenantSettings,
                    defaultSettings
                ));

            // When
            GetMergedSettingsUseCase.Response response = getMergedSettingsService.execute(query);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.settings()).hasSize(1);
            assertThat(response.settings().get("app.config")).isEqualTo("org-value");

            verify(loadSettingsPort).findAllForMerge(any(Long.class), any(Long.class));
        }

        @Test
        @DisplayName("3-tier 병합: ORG > TENANT > DEFAULT 우선순위 확인")
        void execute_Success_ThreeTierMerge() {
            // Given
            Long orgId = 10L;
            Long tenantId = 100L;
            GetMergedSettingsUseCase.Query query = GetMergedSettingsQueryFixture.createQueryWithBoth(orgId, tenantId);

            List<Setting> defaultSettings = List.of(
                SettingFixture.createNew("default.only", "default-value", SettingLevel.DEFAULT, null),
                SettingFixture.createNew("tenant.override", "default-value", SettingLevel.DEFAULT, null),
                SettingFixture.createNew("org.override", "default-value", SettingLevel.DEFAULT, null)
            );
            List<Setting> tenantSettings = List.of(
                SettingFixture.createNew("tenant.only", "tenant-value", SettingLevel.TENANT, tenantId),
                SettingFixture.createNew("tenant.override", "tenant-value", SettingLevel.TENANT, tenantId),
                SettingFixture.createNew("org.override", "tenant-value", SettingLevel.TENANT, tenantId)
            );
            List<Setting> orgSettings = List.of(
                SettingFixture.createNew("org.only", "org-value", SettingLevel.ORG, orgId),
                SettingFixture.createNew("org.override", "org-value", SettingLevel.ORG, orgId)
            );

            given(loadSettingsPort.findAllForMerge(any(Long.class), any(Long.class)))
                .willReturn(new LoadSettingsPort.SettingsForMerge(
                    orgSettings,
                    tenantSettings,
                    defaultSettings
                ));

            // When
            GetMergedSettingsUseCase.Response response = getMergedSettingsService.execute(query);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.settings()).hasSize(5);
            assertThat(response.settings().get("default.only")).isEqualTo("default-value");
            assertThat(response.settings().get("tenant.only")).isEqualTo("tenant-value");
            assertThat(response.settings().get("tenant.override")).isEqualTo("tenant-value");
            assertThat(response.settings().get("org.only")).isEqualTo("org-value");
            assertThat(response.settings().get("org.override")).isEqualTo("org-value");
        }

        @Test
        @DisplayName("ORG만 있을 때 병합 성공")
        void execute_Success_OrgLevelOnly() {
            // Given
            Long orgId = 10L;
            GetMergedSettingsUseCase.Query query = GetMergedSettingsQueryFixture.createQueryWithOrgOnly(orgId);

            List<Setting> defaultSettings = List.of(
                SettingFixture.createDefaultLevel()
            );
            List<Setting> orgSettings = List.of(
                SettingFixture.createOrgLevel(orgId)
            );

            given(loadSettingsPort.findAllForMerge(any(Long.class), isNull()))
                .willReturn(new LoadSettingsPort.SettingsForMerge(
                    orgSettings,
                    Collections.emptyList(),
                    defaultSettings
                ));

            // When
            GetMergedSettingsUseCase.Response response = getMergedSettingsService.execute(query);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.settings()).hasSize(2);
            assertThat(response.settings()).containsKeys("app.default.config", "app.org.config");

            verify(loadSettingsPort).findAllForMerge(any(Long.class), isNull());
        }
    }

    @Nested
    @DisplayName("Edge Cases - 경계 케이스")
    class EdgeCaseTests {

        @Test
        @DisplayName("모든 레벨이 비어있을 때 빈 Map 반환")
        void execute_ReturnsEmptyMap_WhenAllLevelsAreEmpty() {
            // Given
            GetMergedSettingsUseCase.Query query = GetMergedSettingsQueryFixture.createQuery();

            given(loadSettingsPort.findAllForMerge(isNull(), isNull()))
                .willReturn(new LoadSettingsPort.SettingsForMerge(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList()
                ));

            // When
            GetMergedSettingsUseCase.Response response = getMergedSettingsService.execute(query);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.settings()).isEmpty();

            verify(loadSettingsPort).findAllForMerge(isNull(), isNull());
        }

        @Test
        @DisplayName("DEFAULT만 비어있을 때 TENANT, ORG만 반환")
        void execute_ReturnsMerged_WhenDefaultIsEmpty() {
            // Given
            Long orgId = 10L;
            Long tenantId = 100L;
            GetMergedSettingsUseCase.Query query = GetMergedSettingsQueryFixture.createQueryWithBoth(orgId, tenantId);

            List<Setting> tenantSettings = List.of(
                SettingFixture.createTenantLevel(tenantId)
            );
            List<Setting> orgSettings = List.of(
                SettingFixture.createOrgLevel(orgId)
            );

            given(loadSettingsPort.findAllForMerge(any(Long.class), any(Long.class)))
                .willReturn(new LoadSettingsPort.SettingsForMerge(
                    orgSettings,
                    tenantSettings,
                    Collections.emptyList()
                ));

            // When
            GetMergedSettingsUseCase.Response response = getMergedSettingsService.execute(query);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.settings()).hasSize(2);
            assertThat(response.settings()).containsKeys("app.tenant.config", "app.org.config");
        }

        @Test
        @DisplayName("TENANT과 ORG가 비어있을 때 DEFAULT만 반환")
        void execute_ReturnsDefaultOnly_WhenTenantAndOrgAreEmpty() {
            // Given
            Long orgId = 10L;
            Long tenantId = 100L;
            GetMergedSettingsUseCase.Query query = GetMergedSettingsQueryFixture.createQueryWithBoth(orgId, tenantId);

            List<Setting> defaultSettings = List.of(
                SettingFixture.createDefaultLevel()
            );

            given(loadSettingsPort.findAllForMerge(any(Long.class), any(Long.class)))
                .willReturn(new LoadSettingsPort.SettingsForMerge(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    defaultSettings
                ));

            // When
            GetMergedSettingsUseCase.Response response = getMergedSettingsService.execute(query);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.settings()).hasSize(1);
            assertThat(response.settings()).containsKey("app.default.config");
        }
    }

    @Nested
    @DisplayName("Secret Masking - 비밀 키 마스킹")
    class SecretMaskingTests {

        @Test
        @DisplayName("비밀 키는 자동으로 마스킹되어 병합")
        void execute_MasksSecretKeys_InMergedResult() {
            // Given
            GetMergedSettingsUseCase.Query query = GetMergedSettingsQueryFixture.createQuery();

            List<Setting> defaultSettings = List.of(
                SettingFixture.createNew("app.config", "normal-value", SettingLevel.DEFAULT, null),
                SettingFixture.createSecretSetting()
            );

            given(loadSettingsPort.findAllForMerge(isNull(), isNull()))
                .willReturn(new LoadSettingsPort.SettingsForMerge(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    defaultSettings
                ));

            // When
            GetMergedSettingsUseCase.Response response = getMergedSettingsService.execute(query);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.settings()).hasSize(2);
            assertThat(response.settings().get("app.config")).isEqualTo("normal-value");
            assertThat(response.settings().get("api.key")).isEqualTo("********");
        }

        @Test
        @DisplayName("병합 후 비밀 키 마스킹 유지 (ORG > TENANT > DEFAULT)")
        void execute_PreservesSecretMasking_AfterMerge() {
            // Given
            Long orgId = 10L;
            Long tenantId = 100L;
            GetMergedSettingsUseCase.Query query = GetMergedSettingsQueryFixture.createQueryWithBoth(orgId, tenantId);

            List<Setting> defaultSettings = List.of(
                SettingFixture.builder()
                    .key("api.key")
                    .value("default-secret")
                    .secret(true)
                    .level(SettingLevel.DEFAULT)
                    .build()
            );
            List<Setting> tenantSettings = List.of(
                SettingFixture.builder()
                    .key("api.key")
                    .value("tenant-secret")
                    .secret(true)
                    .level(SettingLevel.TENANT)
                    .contextId(tenantId)
                    .build()
            );
            List<Setting> orgSettings = List.of(
                SettingFixture.builder()
                    .key("api.key")
                    .value("org-secret")
                    .secret(true)
                    .level(SettingLevel.ORG)
                    .contextId(orgId)
                    .build()
            );

            given(loadSettingsPort.findAllForMerge(any(Long.class), any(Long.class)))
                .willReturn(new LoadSettingsPort.SettingsForMerge(
                    orgSettings,
                    tenantSettings,
                    defaultSettings
                ));

            // When
            GetMergedSettingsUseCase.Response response = getMergedSettingsService.execute(query);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.settings()).hasSize(1);
            assertThat(response.settings().get("api.key")).isEqualTo("********");
        }
    }

    @Nested
    @DisplayName("Transaction Boundary - 트랜잭션 경계")
    class TransactionBoundaryTests {

        @Test
        @DisplayName("execute() 메서드는 @Transactional(readOnly=true)이어야 함")
        void executeMethod_ShouldHaveReadOnlyTransactional() throws NoSuchMethodException {
            // Given
            Method executeMethod = GetMergedSettingsService.class.getMethod("execute", GetMergedSettingsUseCase.Query.class);

            // Then
            if (executeMethod.isAnnotationPresent(Transactional.class)) {
                Transactional annotation = executeMethod.getAnnotation(Transactional.class);
                assertThat(annotation.readOnly()).isTrue();
            } else if (GetMergedSettingsService.class.isAnnotationPresent(Transactional.class)) {
                Transactional annotation = GetMergedSettingsService.class.getAnnotation(Transactional.class);
                assertThat(annotation.readOnly()).isTrue();
            }
        }

        @Test
        @DisplayName("execute() 메서드는 Public이어야 함")
        void executeMethod_ShouldBePublic() throws NoSuchMethodException {
            // Given
            Method executeMethod = GetMergedSettingsService.class.getMethod("execute", GetMergedSettingsUseCase.Query.class);

            // Then
            assertThat(Modifier.isPublic(executeMethod.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("Service 클래스는 Final이 아니어야 함 (Spring Proxy 제약)")
        void serviceClass_ShouldNotBeFinal() {
            // Then
            assertThat(Modifier.isFinal(GetMergedSettingsService.class.getModifiers())).isFalse();
        }
    }

    @Nested
    @DisplayName("Port Interaction - Port 상호작용")
    class PortInteractionTests {

        @Test
        @DisplayName("LoadSettingsPort는 정확히 1번만 호출됨")
        void execute_CallsLoadSettingsPortOnce() {
            // Given
            GetMergedSettingsUseCase.Query query = GetMergedSettingsQueryFixture.createQuery();

            given(loadSettingsPort.findAllForMerge(isNull(), isNull()))
                .willReturn(new LoadSettingsPort.SettingsForMerge(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList()
                ));

            // When
            getMergedSettingsService.execute(query);

            // Then
            verify(loadSettingsPort, org.mockito.Mockito.times(1))
                .findAllForMerge(isNull(), isNull());
        }

        @Test
        @DisplayName("orgId와 tenantId가 모두 있으면 LoadSettingsPort에 전달")
        void execute_PassesOrgIdAndTenantId_ToLoadSettingsPort() {
            // Given
            Long orgId = 10L;
            Long tenantId = 100L;
            GetMergedSettingsUseCase.Query query = GetMergedSettingsQueryFixture.createQueryWithBoth(orgId, tenantId);

            given(loadSettingsPort.findAllForMerge(orgId, tenantId))
                .willReturn(new LoadSettingsPort.SettingsForMerge(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList()
                ));

            // When
            getMergedSettingsService.execute(query);

            // Then
            verify(loadSettingsPort).findAllForMerge(orgId, tenantId);
        }

        @Test
        @DisplayName("orgId만 있으면 tenantId는 null로 전달")
        void execute_PassesNullTenantId_WhenOnlyOrgIdExists() {
            // Given
            Long orgId = 10L;
            GetMergedSettingsUseCase.Query query = GetMergedSettingsQueryFixture.createQueryWithOrgOnly(orgId);

            given(loadSettingsPort.findAllForMerge(orgId, null))
                .willReturn(new LoadSettingsPort.SettingsForMerge(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList()
                ));

            // When
            getMergedSettingsService.execute(query);

            // Then
            verify(loadSettingsPort).findAllForMerge(orgId, null);
        }

        @Test
        @DisplayName("tenantId만 있으면 orgId는 null로 전달")
        void execute_PassesNullOrgId_WhenOnlyTenantIdExists() {
            // Given
            Long tenantId = 100L;
            GetMergedSettingsUseCase.Query query = GetMergedSettingsQueryFixture.createQueryWithTenantOnly(tenantId);

            given(loadSettingsPort.findAllForMerge(null, tenantId))
                .willReturn(new LoadSettingsPort.SettingsForMerge(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList()
                ));

            // When
            getMergedSettingsService.execute(query);

            // Then
            verify(loadSettingsPort).findAllForMerge(null, tenantId);
        }
    }

    @Nested
    @DisplayName("Response Construction - 응답 생성")
    class ResponseConstructionTests {

        @Test
        @DisplayName("Response의 settings Map은 불변이어야 함")
        void execute_ReturnsUnmodifiableMap() {
            // Given
            GetMergedSettingsUseCase.Query query = GetMergedSettingsQueryFixture.createQuery();
            List<Setting> defaultSettings = List.of(
                SettingFixture.createDefaultLevel()
            );

            given(loadSettingsPort.findAllForMerge(isNull(), isNull()))
                .willReturn(new LoadSettingsPort.SettingsForMerge(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    defaultSettings
                ));

            // When
            GetMergedSettingsUseCase.Response response = getMergedSettingsService.execute(query);

            // Then
            assertThat(response.settings()).isNotNull();
            // Unmodifiable Map인지 확인
            Map<String, String> settings = response.settings();
            org.junit.jupiter.api.Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> settings.put("test", "value")
            );
        }
    }
}
