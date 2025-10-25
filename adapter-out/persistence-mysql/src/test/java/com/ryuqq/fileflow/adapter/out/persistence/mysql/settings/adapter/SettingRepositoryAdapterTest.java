package com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.SettingJpaRepository;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.entity.SettingJpaEntity;
import com.ryuqq.fileflow.domain.settings.Setting;
import com.ryuqq.fileflow.domain.settings.SettingKey;
import com.ryuqq.fileflow.domain.settings.SettingLevel;
import com.ryuqq.fileflow.domain.settings.SettingRepository;
import com.ryuqq.fileflow.domain.settings.SettingType;
import com.ryuqq.fileflow.fixtures.SettingFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SettingRepositoryAdapter 테스트
 *
 * <p>SettingRepository Port 구현체의 정확성을 검증합니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
@Tag("unit")
@Tag("persistence")
@Tag("fast")
@DisplayName("SettingRepositoryAdapter 테스트")
class SettingRepositoryAdapterTest {

    private SettingJpaRepository jpaRepository;
    private SettingRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(SettingJpaRepository.class);
        adapter = new SettingRepositoryAdapter(jpaRepository);
    }

    @Nested
    @DisplayName("save() 테스트")
    class SaveTest {

        @Test
        @DisplayName("신규 Setting을 저장한다")
        void shouldSaveNewSetting() {
            // Arrange
            Setting setting = SettingFixtures.createDefaultSetting(); // ID = null
            LocalDateTime now = LocalDateTime.now();

            SettingJpaEntity savedEntity = SettingJpaEntity.reconstitute(
                1L, "MAX_UPLOAD_SIZE", "100MB", SettingType.STRING,
                SettingLevel.DEFAULT, null, false, now, now
            );

            when(jpaRepository.save(any(SettingJpaEntity.class))).thenReturn(savedEntity);

            // Act
            Setting result = adapter.save(setting);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L); // ID가 생성됨
            assertThat(result.getKeyValue()).isEqualTo("MAX_UPLOAD_SIZE");
            assertThat(result.getDisplayValue()).isEqualTo("100MB");

            verify(jpaRepository).save(any(SettingJpaEntity.class));
        }

        @Test
        @DisplayName("기존 Setting을 업데이트한다")
        void shouldUpdateExistingSetting() {
            // Arrange
            Setting setting = SettingFixtures.reconstituteDefaultSetting(1L); // ID = 1L
            LocalDateTime now = LocalDateTime.now();

            SettingJpaEntity savedEntity = SettingJpaEntity.reconstitute(
                1L, "MAX_UPLOAD_SIZE", "100MB", SettingType.STRING,
                SettingLevel.DEFAULT, null, false, now, now
            );

            when(jpaRepository.save(any(SettingJpaEntity.class))).thenReturn(savedEntity);

            // Act
            Setting result = adapter.save(setting);

            // Assert
            assertThat(result.getId()).isEqualTo(1L);
            verify(jpaRepository).save(any(SettingJpaEntity.class));
        }

        @Test
        @DisplayName("Setting이 null이면 예외 발생")
        void shouldThrowExceptionWhenSettingIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> adapter.save(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Setting은 필수입니다");

            verify(jpaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findByKeyAndLevel() 테스트")
    class FindByKeyAndLevelTest {

        @Test
        @DisplayName("Key, Level, ContextId로 Setting을 조회한다")
        void shouldFindSettingByKeyAndLevelAndContextId() {
            // Arrange
            SettingKey key = SettingKey.of("MAX_UPLOAD_SIZE");
            SettingLevel level = SettingLevel.DEFAULT;
            Long contextId = null;

            LocalDateTime now = LocalDateTime.now();
            SettingJpaEntity entity = SettingJpaEntity.reconstitute(
                1L, "MAX_UPLOAD_SIZE", "100MB", SettingType.STRING,
                SettingLevel.DEFAULT, null, false, now, now
            );

            when(jpaRepository.findBySettingKeyAndLevelAndContextId(
                eq("MAX_UPLOAD_SIZE"), eq(SettingLevel.DEFAULT), eq(null)
            )).thenReturn(Optional.of(entity));

            // Act
            Optional<Setting> result = adapter.findByKeyAndLevel(key, level, contextId);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getKeyValue()).isEqualTo("MAX_UPLOAD_SIZE");

            verify(jpaRepository).findBySettingKeyAndLevelAndContextId(
                eq("MAX_UPLOAD_SIZE"), eq(SettingLevel.DEFAULT), eq(null)
            );
        }

        @Test
        @DisplayName("존재하지 않으면 Optional.empty()를 반환한다")
        void shouldReturnEmptyWhenNotFound() {
            // Arrange
            SettingKey key = SettingKey.of("NON_EXISTENT_KEY");
            SettingLevel level = SettingLevel.DEFAULT;

            when(jpaRepository.findBySettingKeyAndLevelAndContextId(
                any(), any(), any()
            )).thenReturn(Optional.empty());

            // Act
            Optional<Setting> result = adapter.findByKeyAndLevel(key, level, null);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Key가 null이면 예외 발생")
        void shouldThrowExceptionWhenKeyIsNull() {
            // Act & Assert
            assertThatThrownBy(() ->
                adapter.findByKeyAndLevel(null, SettingLevel.DEFAULT, null)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SettingKey는 필수입니다");
        }

        @Test
        @DisplayName("Level이 null이면 예외 발생")
        void shouldThrowExceptionWhenLevelIsNull() {
            // Arrange
            SettingKey key = SettingKey.of("KEY");

            // Act & Assert
            assertThatThrownBy(() ->
                adapter.findByKeyAndLevel(key, null, null)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SettingLevel은 필수입니다");
        }
    }

    @Nested
    @DisplayName("findByOrg() 테스트")
    class FindByOrgTest {

        @Test
        @DisplayName("ORG 레벨의 모든 Setting을 조회한다")
        void shouldFindAllOrgSettings() {
            // Arrange
            Long orgId = 1L;
            LocalDateTime now = LocalDateTime.now();

            List<SettingJpaEntity> entities = List.of(
                SettingJpaEntity.reconstitute(
                    1L, "MAX_UPLOAD_SIZE", "200MB", SettingType.STRING,
                    SettingLevel.ORG, orgId, false, now, now
                )
            );

            when(jpaRepository.findAllByOrg(eq(orgId))).thenReturn(entities);

            // Act
            List<Setting> result = adapter.findByOrg(orgId);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getLevel()).isEqualTo(SettingLevel.ORG);
            assertThat(result.get(0).getContextId()).isEqualTo(orgId);

            verify(jpaRepository).findAllByOrg(eq(orgId));
        }

        @Test
        @DisplayName("orgId가 null이면 예외 발생")
        void shouldThrowExceptionWhenOrgIdIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> adapter.findByOrg(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Organization ID는 필수입니다");
        }
    }

    @Nested
    @DisplayName("findByTenant() 테스트")
    class FindByTenantTest {

        @Test
        @DisplayName("TENANT 레벨의 모든 Setting을 조회한다")
        void shouldFindAllTenantSettings() {
            // Arrange
            Long tenantId = 100L;
            LocalDateTime now = LocalDateTime.now();

            List<SettingJpaEntity> entities = List.of(
                SettingJpaEntity.reconstitute(
                    2L, "MAX_UPLOAD_SIZE", "50MB", SettingType.STRING,
                    SettingLevel.TENANT, tenantId, false, now, now
                )
            );

            when(jpaRepository.findAllByTenant(eq(tenantId))).thenReturn(entities);

            // Act
            List<Setting> result = adapter.findByTenant(tenantId);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getLevel()).isEqualTo(SettingLevel.TENANT);
            assertThat(result.get(0).getContextId()).isEqualTo(tenantId);

            verify(jpaRepository).findAllByTenant(eq(tenantId));
        }

        @Test
        @DisplayName("tenantId가 null이면 예외 발생")
        void shouldThrowExceptionWhenTenantIdIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> adapter.findByTenant(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant ID는 필수입니다");
        }
    }

    @Nested
    @DisplayName("findDefaults() 테스트")
    class FindDefaultsTest {

        @Test
        @DisplayName("DEFAULT 레벨의 모든 Setting을 조회한다")
        void shouldFindAllDefaultSettings() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();

            List<SettingJpaEntity> entities = List.of(
                SettingJpaEntity.reconstitute(
                    3L, "MAX_UPLOAD_SIZE", "100MB", SettingType.STRING,
                    SettingLevel.DEFAULT, null, false, now, now
                )
            );

            when(jpaRepository.findAllDefaults()).thenReturn(entities);

            // Act
            List<Setting> result = adapter.findDefaults();

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getLevel()).isEqualTo(SettingLevel.DEFAULT);
            assertThat(result.get(0).getContextId()).isNull();

            verify(jpaRepository).findAllDefaults();
        }

        @Test
        @DisplayName("DEFAULT Setting이 없으면 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenNoDefaults() {
            // Arrange
            when(jpaRepository.findAllDefaults()).thenReturn(Collections.emptyList());

            // Act
            List<Setting> result = adapter.findDefaults();

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllForMerge() 테스트")
    class FindAllForMergeTest {

        @Test
        @DisplayName("ORG, TENANT, DEFAULT 레벨의 Setting을 모두 조회한다")
        void shouldFindAllLevelsForMerge() {
            // Arrange
            Long orgId = 1L;
            Long tenantId = 100L;
            LocalDateTime now = LocalDateTime.now();

            List<SettingJpaEntity> orgEntities = List.of(
                SettingJpaEntity.reconstitute(
                    1L, "MAX_UPLOAD_SIZE", "200MB", SettingType.STRING,
                    SettingLevel.ORG, orgId, false, now, now
                )
            );

            List<SettingJpaEntity> tenantEntities = List.of(
                SettingJpaEntity.reconstitute(
                    2L, "API_TIMEOUT", "60", SettingType.NUMBER,
                    SettingLevel.TENANT, tenantId, false, now, now
                )
            );

            List<SettingJpaEntity> defaultEntities = List.of(
                SettingJpaEntity.reconstitute(
                    3L, "ENABLE_CACHE", "true", SettingType.BOOLEAN,
                    SettingLevel.DEFAULT, null, false, now, now
                )
            );

            when(jpaRepository.findAllByOrg(eq(orgId))).thenReturn(orgEntities);
            when(jpaRepository.findAllByTenant(eq(tenantId))).thenReturn(tenantEntities);
            when(jpaRepository.findAllDefaults()).thenReturn(defaultEntities);

            // Act
            SettingRepository.SettingsForMerge result = adapter.findAllForMerge(orgId, tenantId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getOrgSettings()).hasSize(1);
            assertThat(result.getTenantSettings()).hasSize(1);
            assertThat(result.getDefaultSettings()).hasSize(1);

            verify(jpaRepository).findAllByOrg(eq(orgId));
            verify(jpaRepository).findAllByTenant(eq(tenantId));
            verify(jpaRepository).findAllDefaults();
        }

        @Test
        @DisplayName("orgId가 null이면 ORG 레벨을 조회하지 않는다")
        void shouldSkipOrgLevelWhenOrgIdIsNull() {
            // Arrange
            Long tenantId = 100L;
            LocalDateTime now = LocalDateTime.now();

            List<SettingJpaEntity> tenantEntities = List.of(
                SettingJpaEntity.reconstitute(
                    2L, "MAX_UPLOAD_SIZE", "50MB", SettingType.STRING,
                    SettingLevel.TENANT, tenantId, false, now, now
                )
            );

            List<SettingJpaEntity> defaultEntities = List.of(
                SettingJpaEntity.reconstitute(
                    3L, "MAX_UPLOAD_SIZE", "100MB", SettingType.STRING,
                    SettingLevel.DEFAULT, null, false, now, now
                )
            );

            when(jpaRepository.findAllByTenant(eq(tenantId))).thenReturn(tenantEntities);
            when(jpaRepository.findAllDefaults()).thenReturn(defaultEntities);

            // Act
            SettingRepository.SettingsForMerge result = adapter.findAllForMerge(null, tenantId);

            // Assert
            assertThat(result.getOrgSettings()).isEmpty();
            assertThat(result.getTenantSettings()).hasSize(1);
            assertThat(result.getDefaultSettings()).hasSize(1);

            verify(jpaRepository, never()).findAllByOrg(any());
            verify(jpaRepository).findAllByTenant(eq(tenantId));
            verify(jpaRepository).findAllDefaults();
        }

        @Test
        @DisplayName("tenantId가 null이면 TENANT 레벨을 조회하지 않는다")
        void shouldSkipTenantLevelWhenTenantIdIsNull() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();

            List<SettingJpaEntity> defaultEntities = List.of(
                SettingJpaEntity.reconstitute(
                    3L, "MAX_UPLOAD_SIZE", "100MB", SettingType.STRING,
                    SettingLevel.DEFAULT, null, false, now, now
                )
            );

            when(jpaRepository.findAllDefaults()).thenReturn(defaultEntities);

            // Act
            SettingRepository.SettingsForMerge result = adapter.findAllForMerge(null, null);

            // Assert
            assertThat(result.getOrgSettings()).isEmpty();
            assertThat(result.getTenantSettings()).isEmpty();
            assertThat(result.getDefaultSettings()).hasSize(1);

            verify(jpaRepository, never()).findAllByOrg(any());
            verify(jpaRepository, never()).findAllByTenant(any());
            verify(jpaRepository).findAllDefaults();
        }
    }

    @Nested
    @DisplayName("saveAll() 테스트")
    class SaveAllTest {

        @Test
        @DisplayName("여러 Setting을 일괄 저장한다")
        void shouldSaveAllSettings() {
            // Arrange
            List<Setting> settings = List.of(
                SettingFixtures.createDefaultSetting(),
                SettingFixtures.createDefaultNumberSetting()
            );

            LocalDateTime now = LocalDateTime.now();
            List<SettingJpaEntity> savedEntities = List.of(
                SettingJpaEntity.reconstitute(
                    1L, "MAX_UPLOAD_SIZE", "100MB", SettingType.STRING,
                    SettingLevel.DEFAULT, null, false, now, now
                ),
                SettingJpaEntity.reconstitute(
                    2L, "API_TIMEOUT", "30", SettingType.NUMBER,
                    SettingLevel.DEFAULT, null, false, now, now
                )
            );

            when(jpaRepository.saveAll(anyList())).thenReturn(savedEntities);

            // Act
            List<Setting> result = adapter.saveAll(settings);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(1).getId()).isEqualTo(2L);

            verify(jpaRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("Settings가 null이면 예외 발생")
        void shouldThrowExceptionWhenSettingsIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> adapter.saveAll(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Settings는 필수입니다");
        }
    }

    @Nested
    @DisplayName("findById() 테스트")
    class FindByIdTest {

        @Test
        @DisplayName("ID로 Setting을 조회한다")
        void shouldFindSettingById() {
            // Arrange
            Long id = 1L;
            LocalDateTime now = LocalDateTime.now();

            SettingJpaEntity entity = SettingJpaEntity.reconstitute(
                id, "MAX_UPLOAD_SIZE", "100MB", SettingType.STRING,
                SettingLevel.DEFAULT, null, false, now, now
            );

            when(jpaRepository.findById(eq(id))).thenReturn(Optional.of(entity));

            // Act
            Optional<Setting> result = adapter.findById(id);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(id);

            verify(jpaRepository).findById(eq(id));
        }

        @Test
        @DisplayName("ID가 null이면 예외 발생")
        void shouldThrowExceptionWhenIdIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> adapter.findById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Setting ID는 필수입니다");
        }
    }

    @Nested
    @DisplayName("findByLevelAndContext() 테스트")
    class FindByLevelAndContextTest {

        @Test
        @DisplayName("Level과 ContextId로 Setting을 조회한다")
        void shouldFindSettingsByLevelAndContext() {
            // Arrange
            SettingLevel level = SettingLevel.ORG;
            Long contextId = 1L;
            LocalDateTime now = LocalDateTime.now();

            List<SettingJpaEntity> entities = List.of(
                SettingJpaEntity.reconstitute(
                    1L, "MAX_UPLOAD_SIZE", "200MB", SettingType.STRING,
                    level, contextId, false, now, now
                )
            );

            when(jpaRepository.findAllByLevelAndContextId(eq(level), eq(contextId)))
                .thenReturn(entities);

            // Act
            List<Setting> result = adapter.findByLevelAndContext(level, contextId);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getLevel()).isEqualTo(level);
            assertThat(result.get(0).getContextId()).isEqualTo(contextId);
        }

        @Test
        @DisplayName("Level이 null이면 예외 발생")
        void shouldThrowExceptionWhenLevelIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> adapter.findByLevelAndContext(null, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SettingLevel은 필수입니다");
        }
    }

    @Nested
    @DisplayName("deleteById() 테스트")
    class DeleteByIdTest {

        @Test
        @DisplayName("ID로 Setting을 삭제한다")
        void shouldDeleteSettingById() {
            // Arrange
            Long id = 1L;

            // Act
            adapter.deleteById(id);

            // Assert
            verify(jpaRepository).deleteById(eq(id));
        }

        @Test
        @DisplayName("ID가 null이면 예외 발생")
        void shouldThrowExceptionWhenIdIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> adapter.deleteById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Setting ID는 필수입니다");
        }
    }

    @Nested
    @DisplayName("deleteByLevelAndContext() 테스트")
    class DeleteByLevelAndContextTest {

        @Test
        @DisplayName("Level과 ContextId로 Setting을 삭제한다")
        void shouldDeleteSettingsByLevelAndContext() {
            // Arrange
            SettingLevel level = SettingLevel.ORG;
            Long contextId = 1L;
            LocalDateTime now = LocalDateTime.now();

            List<SettingJpaEntity> entities = List.of(
                SettingJpaEntity.reconstitute(
                    1L, "MAX_UPLOAD_SIZE", "200MB", SettingType.STRING,
                    level, contextId, false, now, now
                )
            );

            when(jpaRepository.findAllByLevelAndContextId(eq(level), eq(contextId)))
                .thenReturn(entities);

            // Act
            adapter.deleteByLevelAndContext(level, contextId);

            // Assert
            verify(jpaRepository).findAllByLevelAndContextId(eq(level), eq(contextId));
            verify(jpaRepository).deleteAll(eq(entities));
        }

        @Test
        @DisplayName("Level이 null이면 예외 발생")
        void shouldThrowExceptionWhenLevelIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> adapter.deleteByLevelAndContext(null, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SettingLevel은 필수입니다");
        }
    }
}
