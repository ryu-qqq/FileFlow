package com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.SettingJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.entity.SettingJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.repository.SettingJpaRepository;
import com.ryuqq.fileflow.application.settings.port.out.LoadSettingsPort.SettingsForMerge;
import com.ryuqq.fileflow.domain.settings.Setting;
import com.ryuqq.fileflow.domain.settings.SettingKey;
import com.ryuqq.fileflow.domain.settings.SettingLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * SettingQueryRepositoryAdapter 단위 테스트
 *
 * <p><strong>테스트 대상</strong>: {@link SettingQueryRepositoryAdapter}</p>
 * <p><strong>테스트 전략</strong>: Mockito 기반 단위 테스트</p>
 *
 * <h3>테스트 범위</h3>
 * <ul>
 *   <li>✅ Happy Path: 정상 조회 시나리오</li>
 *   <li>✅ Edge Cases: 빈 결과, Optional.empty() 처리</li>
 *   <li>✅ Exception Cases: null 입력, IllegalArgumentException</li>
 *   <li>✅ 3레벨 병합 조회 (ORG + TENANT + DEFAULT)</li>
 *   <li>✅ Mapper 호출 검증 (Entity → Domain 변환)</li>
 * </ul>
 *
 * <h3>테스트 패턴</h3>
 * <ul>
 *   <li>✅ Given-When-Then 구조</li>
 *   <li>✅ @Nested를 활용한 논리적 그룹화</li>
 *   <li>✅ @DisplayName으로 테스트 의도 명확화 (한글)</li>
 *   <li>✅ AssertJ를 활용한 Fluent Assertion</li>
 *   <li>✅ BDDMockito를 활용한 Given 설정</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SettingQueryRepositoryAdapter 단위 테스트")
class SettingQueryRepositoryAdapterTest {

    @Mock
    private SettingJpaRepository jpaRepository;

    @InjectMocks
    private SettingQueryRepositoryAdapter adapter;

    private SettingJpaEntity defaultEntity;
    private SettingJpaEntity tenantEntity;
    private SettingJpaEntity orgEntity;

    @BeforeEach
    void setUp() {
        defaultEntity = SettingJpaEntityFixture.createDefaultWithId(1L, "app.name", "FileFlow");
        tenantEntity = SettingJpaEntityFixture.createTenantWithId(2L, 100L, "app.name", "Tenant App");
        orgEntity = SettingJpaEntityFixture.createOrgWithId(3L, 10L, "app.name", "Org App");
    }

    @Nested
    @DisplayName("findById() - Setting ID로 조회")
    class FindByIdTests {

        @Test
        @DisplayName("정상: Setting ID로 조회 성공")
        void shouldFindByIdSuccessfully() {
            // Given
            Long id = 1L;
            given(jpaRepository.findById(id)).willReturn(Optional.of(defaultEntity));

            // When
            Optional<Setting> result = adapter.findById(id);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getIdValue()).isEqualTo(1L);
            assertThat(result.get().getKeyValue()).isEqualTo("app.name");
            verify(jpaRepository, times(1)).findById(id);
        }

        @Test
        @DisplayName("정상: Setting이 존재하지 않으면 Optional.empty() 반환")
        void shouldReturnEmptyWhenSettingNotFound() {
            // Given
            Long id = 999L;
            given(jpaRepository.findById(id)).willReturn(Optional.empty());

            // When
            Optional<Setting> result = adapter.findById(id);

            // Then
            assertThat(result).isEmpty();
            verify(jpaRepository, times(1)).findById(id);
        }

        @Test
        @DisplayName("예외: ID가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenIdIsNull() {
            // When & Then
            assertThatThrownBy(() -> adapter.findById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Setting ID는 필수입니다");
        }
    }

    @Nested
    @DisplayName("findByKeyAndLevel() - Setting Key와 Level로 조회")
    class FindByKeyAndLevelTests {

        @Test
        @DisplayName("정상: Key와 Level로 조회 성공")
        void shouldFindByKeyAndLevelSuccessfully() {
            // Given
            SettingKey key = SettingKey.of("app.name");
            SettingLevel level = SettingLevel.DEFAULT;
            given(jpaRepository.findBySettingKeyAndLevelAndContextId("app.name", level, null))
                .willReturn(Optional.of(defaultEntity));

            // When
            Optional<Setting> result = adapter.findByKeyAndLevel(key, level, null);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getKey().getValue()).isEqualTo("app.name");
            assertThat(result.get().getLevel()).isEqualTo(SettingLevel.DEFAULT);
            verify(jpaRepository, times(1)).findBySettingKeyAndLevelAndContextId("app.name", level, null);
        }

        @Test
        @DisplayName("정상: TENANT 레벨 조회 성공")
        void shouldFindTenantLevelSetting() {
            // Given
            SettingKey key = SettingKey.of("app.name");
            SettingLevel level = SettingLevel.TENANT;
            Long tenantId = 100L;
            given(jpaRepository.findBySettingKeyAndLevelAndContextId("app.name", level, tenantId))
                .willReturn(Optional.of(tenantEntity));

            // When
            Optional<Setting> result = adapter.findByKeyAndLevel(key, level, tenantId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getLevel()).isEqualTo(SettingLevel.TENANT);
            assertThat(result.get().getContextId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("정상: Setting이 존재하지 않으면 Optional.empty() 반환")
        void shouldReturnEmptyWhenSettingNotFound() {
            // Given
            SettingKey key = SettingKey.of("non.existent.key");
            SettingLevel level = SettingLevel.DEFAULT;
            given(jpaRepository.findBySettingKeyAndLevelAndContextId(anyString(), any(), any()))
                .willReturn(Optional.empty());

            // When
            Optional<Setting> result = adapter.findByKeyAndLevel(key, level, null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("예외: SettingKey가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenKeyIsNull() {
            // When & Then
            assertThatThrownBy(() -> adapter.findByKeyAndLevel(null, SettingLevel.DEFAULT, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("SettingKey는 필수입니다");
        }

        @Test
        @DisplayName("예외: SettingLevel이 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenLevelIsNull() {
            // Given
            SettingKey key = SettingKey.of("app.name");

            // When & Then
            assertThatThrownBy(() -> adapter.findByKeyAndLevel(key, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("SettingLevel은 필수입니다");
        }
    }

    @Nested
    @DisplayName("findByLevelAndContext() - Level과 Context로 모든 Setting 조회")
    class FindByLevelAndContextTests {

        @Test
        @DisplayName("정상: DEFAULT 레벨의 모든 Setting 조회")
        void shouldFindAllDefaultLevelSettings() {
            // Given
            SettingLevel level = SettingLevel.DEFAULT;
            List<SettingJpaEntity> entities = List.of(
                SettingJpaEntityFixture.createDefaultWithId(1L, "app.name", "FileFlow"),
                SettingJpaEntityFixture.createDefaultWithId(2L, "app.version", "1.0.0")
            );
            given(jpaRepository.findAllByLevelAndContextId(level, null)).willReturn(entities);

            // When
            List<Setting> result = adapter.findByLevelAndContext(level, null);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(s -> s.getKey().getValue())
                .containsExactlyInAnyOrder("app.name", "app.version");
            verify(jpaRepository, times(1)).findAllByLevelAndContextId(level, null);
        }

        @Test
        @DisplayName("정상: TENANT 레벨의 모든 Setting 조회")
        void shouldFindAllTenantLevelSettings() {
            // Given
            SettingLevel level = SettingLevel.TENANT;
            Long tenantId = 100L;
            List<SettingJpaEntity> entities = List.of(
                SettingJpaEntityFixture.createTenantWithId(1L, tenantId, "feature.enabled", "true"),
                SettingJpaEntityFixture.createTenantWithId(2L, tenantId, "theme.color", "blue")
            );
            given(jpaRepository.findAllByLevelAndContextId(level, tenantId)).willReturn(entities);

            // When
            List<Setting> result = adapter.findByLevelAndContext(level, tenantId);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(s -> s.getLevel() == SettingLevel.TENANT);
            assertThat(result).allMatch(s -> s.getContextId().equals(tenantId));
        }

        @Test
        @DisplayName("정상: Setting이 없으면 빈 리스트 반환")
        void shouldReturnEmptyListWhenNoSettings() {
            // Given
            SettingLevel level = SettingLevel.ORG;
            Long orgId = 10L;
            given(jpaRepository.findAllByLevelAndContextId(level, orgId))
                .willReturn(Collections.emptyList());

            // When
            List<Setting> result = adapter.findByLevelAndContext(level, orgId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("예외: SettingLevel이 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenLevelIsNull() {
            // When & Then
            assertThatThrownBy(() -> adapter.findByLevelAndContext(null, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("SettingLevel은 필수입니다");
        }
    }

    @Nested
    @DisplayName("findByOrg() - ORG 레벨의 모든 Setting 조회")
    class FindByOrgTests {

        @Test
        @DisplayName("정상: Organization의 모든 Setting 조회")
        void shouldFindAllOrgSettings() {
            // Given
            Long orgId = 10L;
            List<SettingJpaEntity> entities = List.of(
                SettingJpaEntityFixture.createOrgWithId(1L, orgId, "org.name", "MyOrg"),
                SettingJpaEntityFixture.createOrgWithId(2L, orgId, "org.plan", "Enterprise")
            );
            given(jpaRepository.findAllByOrg(orgId)).willReturn(entities);

            // When
            List<Setting> result = adapter.findByOrg(orgId);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(s -> s.getLevel() == SettingLevel.ORG);
            assertThat(result).allMatch(s -> s.getContextId().equals(orgId));
            verify(jpaRepository, times(1)).findAllByOrg(orgId);
        }

        @Test
        @DisplayName("정상: Setting이 없으면 빈 리스트 반환")
        void shouldReturnEmptyListWhenNoOrgSettings() {
            // Given
            Long orgId = 999L;
            given(jpaRepository.findAllByOrg(orgId)).willReturn(Collections.emptyList());

            // When
            List<Setting> result = adapter.findByOrg(orgId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("예외: Organization ID가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenOrgIdIsNull() {
            // When & Then
            assertThatThrownBy(() -> adapter.findByOrg(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Organization ID는 필수입니다");
        }
    }

    @Nested
    @DisplayName("findByTenant() - TENANT 레벨의 모든 Setting 조회")
    class FindByTenantTests {

        @Test
        @DisplayName("정상: Tenant의 모든 Setting 조회")
        void shouldFindAllTenantSettings() {
            // Given
            Long tenantId = 100L;
            List<SettingJpaEntity> entities = List.of(
                SettingJpaEntityFixture.createTenantWithId(1L, tenantId, "tenant.name", "MyTenant"),
                SettingJpaEntityFixture.createTenantWithId(2L, tenantId, "tenant.quota", "1000")
            );
            given(jpaRepository.findAllByTenant(tenantId)).willReturn(entities);

            // When
            List<Setting> result = adapter.findByTenant(tenantId);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(s -> s.getLevel() == SettingLevel.TENANT);
            assertThat(result).allMatch(s -> s.getContextId().equals(tenantId));
            verify(jpaRepository, times(1)).findAllByTenant(tenantId);
        }

        @Test
        @DisplayName("정상: Setting이 없으면 빈 리스트 반환")
        void shouldReturnEmptyListWhenNoTenantSettings() {
            // Given
            Long tenantId = 999L;
            given(jpaRepository.findAllByTenant(tenantId)).willReturn(Collections.emptyList());

            // When
            List<Setting> result = adapter.findByTenant(tenantId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("예외: Tenant ID가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenTenantIdIsNull() {
            // When & Then
            assertThatThrownBy(() -> adapter.findByTenant(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tenant ID는 필수입니다");
        }
    }

    @Nested
    @DisplayName("findDefaults() - DEFAULT 레벨의 모든 Setting 조회")
    class FindDefaultsTests {

        @Test
        @DisplayName("정상: DEFAULT 레벨의 모든 Setting 조회")
        void shouldFindAllDefaultSettings() {
            // Given
            List<SettingJpaEntity> entities = List.of(
                SettingJpaEntityFixture.createDefaultWithId(1L, "app.name", "FileFlow"),
                SettingJpaEntityFixture.createDefaultWithId(2L, "app.version", "1.0.0"),
                SettingJpaEntityFixture.createDefaultWithId(3L, "app.lang", "en")
            );
            given(jpaRepository.findAllDefaults()).willReturn(entities);

            // When
            List<Setting> result = adapter.findDefaults();

            // Then
            assertThat(result).hasSize(3);
            assertThat(result).allMatch(s -> s.getLevel() == SettingLevel.DEFAULT);
            assertThat(result).allMatch(s -> s.getContextId() == null);
            verify(jpaRepository, times(1)).findAllDefaults();
        }

        @Test
        @DisplayName("정상: DEFAULT Setting이 없으면 빈 리스트 반환")
        void shouldReturnEmptyListWhenNoDefaultSettings() {
            // Given
            given(jpaRepository.findAllDefaults()).willReturn(Collections.emptyList());

            // When
            List<Setting> result = adapter.findDefaults();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllForMerge() - 3레벨 병합용 모든 Setting 조회")
    class FindAllForMergeTests {

        @Test
        @DisplayName("정상: ORG + TENANT + DEFAULT 모두 조회 (orgId와 tenantId 모두 있음)")
        void shouldFindAllThreeLevels() {
            // Given
            Long orgId = 10L;
            Long tenantId = 100L;

            List<SettingJpaEntity> orgEntities = List.of(
                SettingJpaEntityFixture.createOrgWithId(1L, orgId, "app.name", "OrgApp")
            );
            List<SettingJpaEntity> tenantEntities = List.of(
                SettingJpaEntityFixture.createTenantWithId(2L, tenantId, "app.theme", "dark")
            );
            List<SettingJpaEntity> defaultEntities = List.of(
                SettingJpaEntityFixture.createDefaultWithId(3L, "app.version", "1.0.0")
            );

            given(jpaRepository.findAllByOrg(orgId)).willReturn(orgEntities);
            given(jpaRepository.findAllByTenant(tenantId)).willReturn(tenantEntities);
            given(jpaRepository.findAllDefaults()).willReturn(defaultEntities);

            // When
            SettingsForMerge result = adapter.findAllForMerge(orgId, tenantId);

            // Then
            assertThat(result.orgSettings()).hasSize(1);
            assertThat(result.tenantSettings()).hasSize(1);
            assertThat(result.defaultSettings()).hasSize(1);

            verify(jpaRepository, times(1)).findAllByOrg(orgId);
            verify(jpaRepository, times(1)).findAllByTenant(tenantId);
            verify(jpaRepository, times(1)).findAllDefaults();
        }

        @Test
        @DisplayName("정상: TENANT + DEFAULT 조회 (orgId는 null)")
        void shouldFindTenantAndDefaultLevels() {
            // Given
            Long tenantId = 100L;

            List<SettingJpaEntity> tenantEntities = List.of(
                SettingJpaEntityFixture.createTenantWithId(1L, tenantId, "tenant.setting", "value")
            );
            List<SettingJpaEntity> defaultEntities = List.of(
                SettingJpaEntityFixture.createDefaultWithId(2L, "default.setting", "value")
            );

            given(jpaRepository.findAllByTenant(tenantId)).willReturn(tenantEntities);
            given(jpaRepository.findAllDefaults()).willReturn(defaultEntities);

            // When
            SettingsForMerge result = adapter.findAllForMerge(null, tenantId);

            // Then
            assertThat(result.orgSettings()).isEmpty();
            assertThat(result.tenantSettings()).hasSize(1);
            assertThat(result.defaultSettings()).hasSize(1);

            verify(jpaRepository, times(0)).findAllByOrg(anyLong());
            verify(jpaRepository, times(1)).findAllByTenant(tenantId);
            verify(jpaRepository, times(1)).findAllDefaults();
        }

        @Test
        @DisplayName("정상: DEFAULT만 조회 (orgId와 tenantId 모두 null)")
        void shouldFindDefaultLevelOnly() {
            // Given
            List<SettingJpaEntity> defaultEntities = List.of(
                SettingJpaEntityFixture.createDefaultWithId(1L, "app.name", "FileFlow")
            );

            given(jpaRepository.findAllDefaults()).willReturn(defaultEntities);

            // When
            SettingsForMerge result = adapter.findAllForMerge(null, null);

            // Then
            assertThat(result.orgSettings()).isEmpty();
            assertThat(result.tenantSettings()).isEmpty();
            assertThat(result.defaultSettings()).hasSize(1);

            verify(jpaRepository, times(0)).findAllByOrg(anyLong());
            verify(jpaRepository, times(0)).findAllByTenant(anyLong());
            verify(jpaRepository, times(1)).findAllDefaults();
        }

        @Test
        @DisplayName("정상: 모든 레벨이 비어있어도 빈 리스트 반환")
        void shouldReturnEmptyListsWhenNoSettings() {
            // Given
            Long orgId = 10L;
            Long tenantId = 100L;

            given(jpaRepository.findAllByOrg(orgId)).willReturn(Collections.emptyList());
            given(jpaRepository.findAllByTenant(tenantId)).willReturn(Collections.emptyList());
            given(jpaRepository.findAllDefaults()).willReturn(Collections.emptyList());

            // When
            SettingsForMerge result = adapter.findAllForMerge(orgId, tenantId);

            // Then
            assertThat(result.orgSettings()).isEmpty();
            assertThat(result.tenantSettings()).isEmpty();
            assertThat(result.defaultSettings()).isEmpty();
        }
    }
}
