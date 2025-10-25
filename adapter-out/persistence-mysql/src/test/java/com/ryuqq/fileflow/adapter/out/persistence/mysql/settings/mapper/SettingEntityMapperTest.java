package com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.entity.SettingJpaEntity;
import com.ryuqq.fileflow.domain.settings.Setting;
import com.ryuqq.fileflow.domain.settings.SettingKey;
import com.ryuqq.fileflow.domain.settings.SettingLevel;
import com.ryuqq.fileflow.domain.settings.SettingType;
import com.ryuqq.fileflow.domain.settings.SettingValue;
import com.ryuqq.fileflow.fixtures.SettingFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SettingEntityMapper 테스트
 *
 * <p>Domain Setting ↔ JPA SettingJpaEntity 변환 로직의 정확성을 검증합니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
@Tag("unit")
@Tag("persistence")
@Tag("fast")
@DisplayName("SettingEntityMapper 테스트")
class SettingEntityMapperTest {

    @Nested
    @DisplayName("toEntity() - Domain → Entity 변환")
    class ToEntityTest {

        @Test
        @DisplayName("DEFAULT 레벨 Setting을 Entity로 변환한다")
        void shouldConvertDefaultSettingToEntity() {
            // Arrange
            Setting setting = SettingFixtures.createDefaultSetting(); // MAX_UPLOAD_SIZE = 100MB

            // Act
            SettingJpaEntity entity = SettingEntityMapper.toEntity(setting);

            // Assert
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isNull(); // 신규 생성이므로 ID는 null
            assertThat(entity.getSettingKey()).isEqualTo("MAX_UPLOAD_SIZE");
            assertThat(entity.getSettingValue()).isEqualTo("100MB");
            assertThat(entity.getSettingType()).isEqualTo(SettingType.STRING);
            assertThat(entity.getLevel()).isEqualTo(SettingLevel.DEFAULT);
            assertThat(entity.getContextId()).isNull();
            assertThat(entity.isSecret()).isFalse();
            assertThat(entity.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("ORG 레벨 Setting을 Entity로 변환한다")
        void shouldConvertOrgSettingToEntity() {
            // Arrange
            Long orgId = 1L;
            Setting setting = SettingFixtures.createOrgSetting(orgId); // MAX_UPLOAD_SIZE = 200MB

            // Act
            SettingJpaEntity entity = SettingEntityMapper.toEntity(setting);

            // Assert
            assertThat(entity.getLevel()).isEqualTo(SettingLevel.ORG);
            assertThat(entity.getContextId()).isEqualTo(orgId);
            assertThat(entity.getSettingValue()).isEqualTo("200MB");
        }

        @Test
        @DisplayName("TENANT 레벨 Setting을 Entity로 변환한다")
        void shouldConvertTenantSettingToEntity() {
            // Arrange
            Long tenantId = 100L;
            Setting setting = SettingFixtures.createTenantSetting(tenantId); // MAX_UPLOAD_SIZE = 50MB

            // Act
            SettingJpaEntity entity = SettingEntityMapper.toEntity(setting);

            // Assert
            assertThat(entity.getLevel()).isEqualTo(SettingLevel.TENANT);
            assertThat(entity.getContextId()).isEqualTo(tenantId);
            assertThat(entity.getSettingValue()).isEqualTo("50MB");
        }

        @Test
        @DisplayName("비밀 Setting을 Entity로 변환할 때 원본 값을 저장한다")
        void shouldStoreRawValueForSecretSetting() {
            // Arrange
            Setting setting = SettingFixtures.createDefaultSecretSetting(); // API_KEY = secret-key-123

            // Act
            SettingJpaEntity entity = SettingEntityMapper.toEntity(setting);

            // Assert
            assertThat(entity.isSecret()).isTrue();
            assertThat(entity.getSettingValue()).isEqualTo("secret-key-123"); // 원본 값 저장
        }

        @Test
        @DisplayName("Setting이 null이면 예외 발생")
        void shouldThrowExceptionWhenSettingIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> SettingEntityMapper.toEntity(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Setting은 필수입니다");
        }
    }

    @Nested
    @DisplayName("toEntityForUpdate() - Domain → Entity 변환 (업데이트)")
    class ToEntityForUpdateTest {

        @Test
        @DisplayName("ID가 있는 Setting을 Entity로 변환한다")
        void shouldConvertSettingWithIdToEntity() {
            // Arrange
            Setting setting = SettingFixtures.reconstituteDefaultSetting(1L);

            // Act
            SettingJpaEntity entity = SettingEntityMapper.toEntityForUpdate(setting);

            // Assert
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(1L); // ID 포함
            assertThat(entity.getSettingKey()).isEqualTo("MAX_UPLOAD_SIZE");
            assertThat(entity.getSettingValue()).isEqualTo("100MB");
        }

        @Test
        @DisplayName("Setting이 null이면 예외 발생")
        void shouldThrowExceptionWhenSettingIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> SettingEntityMapper.toEntityForUpdate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Setting은 필수입니다");
        }

        @Test
        @DisplayName("Setting ID가 null이면 예외 발생")
        void shouldThrowExceptionWhenSettingIdIsNull() {
            // Arrange
            Setting setting = SettingFixtures.createDefaultSetting(); // ID가 null

            // Act & Assert
            assertThatThrownBy(() -> SettingEntityMapper.toEntityForUpdate(setting))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("업데이트를 위해서는 Setting ID가 필요합니다");
        }
    }

    @Nested
    @DisplayName("toDomain() - Entity → Domain 변환")
    class ToDomainTest {

        @Test
        @DisplayName("DEFAULT 레벨 Entity를 Setting으로 변환한다")
        void shouldConvertDefaultEntityToSetting() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            SettingJpaEntity entity = SettingJpaEntity.reconstitute(
                1L, "MAX_UPLOAD_SIZE", "100MB", SettingType.STRING,
                SettingLevel.DEFAULT, null, false, now, now
            );

            // Act
            Setting setting = SettingEntityMapper.toDomain(entity);

            // Assert
            assertThat(setting).isNotNull();
            assertThat(setting.getId()).isEqualTo(1L);
            assertThat(setting.getKeyValue()).isEqualTo("MAX_UPLOAD_SIZE");
            assertThat(setting.getDisplayValue()).isEqualTo("100MB");
            assertThat(setting.getValueType()).isEqualTo(SettingType.STRING);
            assertThat(setting.getLevel()).isEqualTo(SettingLevel.DEFAULT);
            assertThat(setting.getContextId()).isNull();
            assertThat(setting.isSecret()).isFalse();
        }

        @Test
        @DisplayName("ORG 레벨 Entity를 Setting으로 변환한다")
        void shouldConvertOrgEntityToSetting() {
            // Arrange
            Long orgId = 1L;
            LocalDateTime now = LocalDateTime.now();
            SettingJpaEntity entity = SettingJpaEntity.reconstitute(
                2L, "MAX_UPLOAD_SIZE", "200MB", SettingType.STRING,
                SettingLevel.ORG, orgId, false, now, now
            );

            // Act
            Setting setting = SettingEntityMapper.toDomain(entity);

            // Assert
            assertThat(setting.getLevel()).isEqualTo(SettingLevel.ORG);
            assertThat(setting.getContextId()).isEqualTo(orgId);
            assertThat(setting.getDisplayValue()).isEqualTo("200MB");
        }

        @Test
        @DisplayName("TENANT 레벨 Entity를 Setting으로 변환한다")
        void shouldConvertTenantEntityToSetting() {
            // Arrange
            Long tenantId = 100L;
            LocalDateTime now = LocalDateTime.now();
            SettingJpaEntity entity = SettingJpaEntity.reconstitute(
                3L, "MAX_UPLOAD_SIZE", "50MB", SettingType.STRING,
                SettingLevel.TENANT, tenantId, false, now, now
            );

            // Act
            Setting setting = SettingEntityMapper.toDomain(entity);

            // Assert
            assertThat(setting.getLevel()).isEqualTo(SettingLevel.TENANT);
            assertThat(setting.getContextId()).isEqualTo(tenantId);
            assertThat(setting.getDisplayValue()).isEqualTo("50MB");
        }

        @Test
        @DisplayName("비밀 Entity를 Setting으로 변환하면 마스킹된다")
        void shouldConvertSecretEntityToMaskedSetting() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            SettingJpaEntity entity = SettingJpaEntity.reconstitute(
                4L, "API_KEY", "secret-key-123", SettingType.STRING,
                SettingLevel.DEFAULT, null, true, now, now
            );

            // Act
            Setting setting = SettingEntityMapper.toDomain(entity);

            // Assert
            assertThat(setting.isSecret()).isTrue();
            assertThat(setting.getDisplayValue()).isEqualTo("********"); // 마스킹됨
            assertThat(setting.getRawValue()).isEqualTo("secret-key-123"); // 원본 값 보존
        }

        @Test
        @DisplayName("Entity가 null이면 예외 발생")
        void shouldThrowExceptionWhenEntityIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> SettingEntityMapper.toDomain(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SettingJpaEntity는 필수입니다");
        }
    }

    @Nested
    @DisplayName("updateEntityValue() - Entity 값 업데이트")
    class UpdateEntityValueTest {

        @Test
        @DisplayName("Entity 값을 업데이트한다")
        void shouldUpdateEntityValue() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            SettingJpaEntity entity = SettingJpaEntity.reconstitute(
                1L, "MAX_UPLOAD_SIZE", "100MB", SettingType.STRING,
                SettingLevel.DEFAULT, null, false, now, now
            );
            String newValue = "200MB";
            LocalDateTime updatedAt = now.plusHours(1);

            // Act
            SettingEntityMapper.updateEntityValue(entity, newValue, updatedAt);

            // Assert
            assertThat(entity.getSettingValue()).isEqualTo("200MB");
            assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("Entity가 null이면 예외 발생")
        void shouldThrowExceptionWhenEntityIsNull() {
            // Act & Assert
            assertThatThrownBy(() ->
                SettingEntityMapper.updateEntityValue(null, "value", LocalDateTime.now())
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SettingJpaEntity는 필수입니다");
        }

        @Test
        @DisplayName("새로운 값이 null이면 예외 발생")
        void shouldThrowExceptionWhenNewValueIsNull() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            SettingJpaEntity entity = SettingJpaEntity.reconstitute(
                1L, "MAX_UPLOAD_SIZE", "100MB", SettingType.STRING,
                SettingLevel.DEFAULT, null, false, now, now
            );

            // Act & Assert
            assertThatThrownBy(() ->
                SettingEntityMapper.updateEntityValue(entity, null, LocalDateTime.now())
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("새로운 Setting 값은 필수입니다");
        }

        @Test
        @DisplayName("updatedAt이 null이면 예외 발생")
        void shouldThrowExceptionWhenUpdatedAtIsNull() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            SettingJpaEntity entity = SettingJpaEntity.reconstitute(
                1L, "MAX_UPLOAD_SIZE", "100MB", SettingType.STRING,
                SettingLevel.DEFAULT, null, false, now, now
            );

            // Act & Assert
            assertThatThrownBy(() ->
                SettingEntityMapper.updateEntityValue(entity, "200MB", null)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("updatedAt는 필수입니다");
        }
    }

    @Nested
    @DisplayName("양방향 변환 테스트")
    class BidirectionalConversionTest {

        @Test
        @DisplayName("Setting → Entity → Setting 변환 시 데이터가 보존된다")
        void shouldPreserveDataInBidirectionalConversion() {
            // Arrange
            Setting originalSetting = SettingFixtures.reconstituteDefaultSetting(1L);

            // Act
            SettingJpaEntity entity = SettingEntityMapper.toEntityForUpdate(originalSetting);
            Setting convertedSetting = SettingEntityMapper.toDomain(entity);

            // Assert
            assertThat(convertedSetting.getId()).isEqualTo(originalSetting.getId());
            assertThat(convertedSetting.getKeyValue()).isEqualTo(originalSetting.getKeyValue());
            assertThat(convertedSetting.getDisplayValue()).isEqualTo(originalSetting.getDisplayValue());
            assertThat(convertedSetting.getValueType()).isEqualTo(originalSetting.getValueType());
            assertThat(convertedSetting.getLevel()).isEqualTo(originalSetting.getLevel());
            assertThat(convertedSetting.getContextId()).isEqualTo(originalSetting.getContextId());
            assertThat(convertedSetting.isSecret()).isEqualTo(originalSetting.isSecret());
        }

        @Test
        @DisplayName("비밀 Setting → Entity → Setting 변환 시 마스킹이 유지된다")
        void shouldPreserveMaskingInBidirectionalConversionForSecret() {
            // Arrange
            Setting secretSetting = SettingFixtures.createDefaultSecretSetting(); // API_KEY = secret-key-123

            // Act - Domain → Entity 변환 (원본 값 저장)
            SettingJpaEntity entity = SettingEntityMapper.toEntity(secretSetting);

            // Assert - Entity에는 원본 값 저장됨
            assertThat(entity.getSettingValue()).isEqualTo("secret-key-123");
            assertThat(entity.isSecret()).isTrue();

            // Act - Entity → Domain 변환 (마스킹 적용)
            Setting convertedSetting = SettingEntityMapper.toDomain(entity);

            // Assert - Domain에서는 마스킹됨
            assertThat(convertedSetting.isSecret()).isTrue();
            assertThat(convertedSetting.getDisplayValue()).isEqualTo("********");
            assertThat(convertedSetting.getRawValue()).isEqualTo("secret-key-123");
        }
    }

    @Nested
    @DisplayName("다양한 타입 변환 테스트")
    class VariousTypesConversionTest {

        @Test
        @DisplayName("STRING 타입 Setting을 변환한다")
        void shouldConvertStringSetting() {
            // Arrange
            Setting setting = SettingFixtures.createDefaultSetting(); // STRING 타입

            // Act
            SettingJpaEntity entity = SettingEntityMapper.toEntity(setting);
            Setting converted = SettingEntityMapper.toDomain(entity);

            // Assert
            assertThat(converted.getValueType()).isEqualTo(SettingType.STRING);
        }

        @Test
        @DisplayName("NUMBER 타입 Setting을 변환한다")
        void shouldConvertNumberSetting() {
            // Arrange
            Setting setting = SettingFixtures.createDefaultNumberSetting(); // NUMBER 타입

            // Act
            SettingJpaEntity entity = SettingEntityMapper.toEntity(setting);
            Setting converted = SettingEntityMapper.toDomain(entity);

            // Assert
            assertThat(converted.getValueType()).isEqualTo(SettingType.NUMBER);
            assertThat(converted.getDisplayValue()).isEqualTo("30");
        }

        @Test
        @DisplayName("BOOLEAN 타입 Setting을 변환한다")
        void shouldConvertBooleanSetting() {
            // Arrange
            Setting setting = SettingFixtures.createDefaultBooleanSetting(); // BOOLEAN 타입

            // Act
            SettingJpaEntity entity = SettingEntityMapper.toEntity(setting);
            Setting converted = SettingEntityMapper.toDomain(entity);

            // Assert
            assertThat(converted.getValueType()).isEqualTo(SettingType.BOOLEAN);
            assertThat(converted.getDisplayValue()).isEqualTo("true");
        }

        @Test
        @DisplayName("JSON_OBJECT 타입 Setting을 변환한다")
        void shouldConvertJsonObjectSetting() {
            // Arrange
            Setting setting = SettingFixtures.createDefaultJsonSetting(); // JSON_OBJECT 타입

            // Act
            SettingJpaEntity entity = SettingEntityMapper.toEntity(setting);
            Setting converted = SettingEntityMapper.toDomain(entity);

            // Assert
            assertThat(converted.getValueType()).isEqualTo(SettingType.JSON_OBJECT);
            assertThat(converted.getDisplayValue()).contains("host", "localhost");
        }
    }
}
