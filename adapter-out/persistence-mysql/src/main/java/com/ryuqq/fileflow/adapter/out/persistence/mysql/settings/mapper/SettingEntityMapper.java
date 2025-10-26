package com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.entity.SettingJpaEntity;
import com.ryuqq.fileflow.domain.settings.Setting;
import com.ryuqq.fileflow.domain.settings.SettingKey;
import com.ryuqq.fileflow.domain.settings.SettingValue;

import java.time.LocalDateTime;

/**
 * Setting Entity Mapper
 *
 * <p>Domain {@code Setting}과 JPA {@code SettingJpaEntity} 간 변환을 담당하는 Mapper입니다.</p>
 *
 * <p><strong>변환 방향:</strong></p>
 * <ul>
 *   <li>Domain → Entity: {@code toEntity()} - 신규 생성 또는 업데이트 시</li>
 *   <li>Entity → Domain: {@code toDomain()} - DB 조회 후 Domain으로 재구성</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Static Utility Class (인스턴스 생성 불가)</li>
 *   <li>✅ Pure Function (부수 효과 없음, 같은 입력 → 같은 출력)</li>
 *   <li>✅ Null 안전성 (null 입력 시 IllegalArgumentException)</li>
 *   <li>✅ Law of Demeter 준수 (Getter 체이닝 금지)</li>
 *   <li>❌ Lombok 금지</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
public final class SettingEntityMapper {

    /**
     * Private Constructor - 인스턴스 생성 방지
     *
     * <p>Static Utility Class이므로 인스턴스 생성을 막습니다.</p>
     *
     * @throws AssertionError 생성자 호출 시
     * @author ryu-qqq
     * @since 2025-10-25
     */
    private SettingEntityMapper() {
        throw new AssertionError("Cannot instantiate SettingEntityMapper");
    }

    /**
     * Domain Setting → JPA SettingJpaEntity 변환 (신규 생성)
     *
     * <p>신규 Setting 생성 시 사용합니다. ID는 null이며, JPA가 자동 생성합니다.</p>
     *
     * <p><strong>변환 규칙:</strong></p>
     * <ul>
     *   <li>{@code SettingKey} → {@code String settingKey}</li>
     *   <li>{@code SettingValue} → {@code String settingValue} (원본 값)</li>
     *   <li>{@code SettingType} → {@code SettingType} (Enum 그대로)</li>
     *   <li>{@code SettingLevel} → {@code SettingLevel} (Enum 그대로)</li>
     *   <li>{@code contextId} → {@code Long contextId}</li>
     *   <li>{@code isSecret} → {@code boolean isSecret}</li>
     *   <li>{@code createdAt}, {@code updatedAt} → {@code LocalDateTime}</li>
     * </ul>
     *
     * @param setting Domain Setting (필수)
     * @return JPA SettingJpaEntity (ID는 null)
     * @throws IllegalArgumentException setting이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static SettingJpaEntity toEntity(Setting setting) {
        if (setting == null) {
            throw new IllegalArgumentException("Setting은 필수입니다");
        }

        // Domain → Entity 변환 (Law of Demeter 준수)
        return SettingJpaEntity.create(
            setting.getKeyValue(),           // SettingKey → String
            setting.getRawValue(),           // SettingValue → String (원본 값, 마스킹 안함!)
            setting.getValueType(),          // SettingType (Enum)
            setting.getLevel(),              // SettingLevel (Enum)
            setting.getContextId(),          // Long contextId
            setting.isSecret(),              // boolean isSecret
            setting.getCreatedAt()           // LocalDateTime createdAt
        );
    }

    /**
     * Domain Setting → JPA SettingJpaEntity 변환 (업데이트용)
     *
     * <p>기존 Setting 업데이트 시 사용합니다. ID를 포함하여 전체 필드를 매핑합니다.</p>
     *
     * <p><strong>주의:</strong> 업데이트 시 {@code updatedAt}은 현재 시각으로 갱신됩니다.</p>
     *
     * @param setting Domain Setting (필수, ID 포함)
     * @return JPA SettingJpaEntity (ID 포함)
     * @throws IllegalArgumentException setting이 null이거나 ID가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static SettingJpaEntity toEntityForUpdate(Setting setting) {
        if (setting == null) {
            throw new IllegalArgumentException("Setting은 필수입니다");
        }
        if (setting.getId() == null) {
            throw new IllegalArgumentException("업데이트를 위해서는 Setting ID가 필요합니다");
        }

        // Domain → Entity 변환 (reconstitute - ID 포함)
        return SettingJpaEntity.reconstitute(
            setting.getId(),                 // Long id (업데이트 대상 식별)
            setting.getKeyValue(),           // SettingKey → String
            setting.getRawValue(),           // SettingValue → String (원본 값)
            setting.getValueType(),          // SettingType (Enum)
            setting.getLevel(),              // SettingLevel (Enum)
            setting.getContextId(),          // Long contextId
            setting.isSecret(),              // boolean isSecret
            setting.getCreatedAt(),          // LocalDateTime createdAt (불변)
            setting.getUpdatedAt()           // LocalDateTime updatedAt (변경됨)
        );
    }

    /**
     * JPA SettingJpaEntity → Domain Setting 변환
     *
     * <p>DB 조회 결과를 Domain 객체로 재구성할 때 사용합니다.</p>
     *
     * <p><strong>변환 규칙:</strong></p>
     * <ul>
     *   <li>{@code String settingKey} → {@code SettingKey.of()}</li>
     *   <li>{@code String settingValue} + {@code boolean isSecret} → {@code SettingValue.of()} or {@code SettingValue.secret()}</li>
     *   <li>{@code SettingType} → {@code SettingType} (Enum 그대로)</li>
     *   <li>{@code SettingLevel} → {@code SettingLevel} (Enum 그대로)</li>
     *   <li>{@code Long contextId} → {@code Long contextId}</li>
     *   <li>{@code LocalDateTime} → {@code LocalDateTime} (그대로)</li>
     * </ul>
     *
     * @param entity JPA SettingJpaEntity (필수)
     * @return Domain Setting
     * @throws IllegalArgumentException entity가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static Setting toDomain(SettingJpaEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("SettingJpaEntity는 필수입니다");
        }

        // Entity → Domain Value Objects 변환
        SettingKey key = SettingKey.of(entity.getSettingKey());

        SettingValue value = entity.isSecret()
            ? SettingValue.secret(entity.getSettingValue(), entity.getSettingType())
            : SettingValue.of(entity.getSettingValue(), entity.getSettingType());

        // Domain Setting 재구성 (reconstitute)
        return Setting.reconstitute(
            entity.getId(),                  // Long id
            key,                             // SettingKey
            value,                           // SettingValue (비밀 여부 포함)
            entity.getLevel(),               // SettingLevel
            entity.getContextId(),           // Long contextId
            entity.getCreatedAt(),           // LocalDateTime createdAt
            entity.getUpdatedAt()            // LocalDateTime updatedAt
        );
    }

    /**
     * Setting 값만 업데이트합니다 (Entity 수정)
     *
     * <p>기존 Entity의 값만 변경하고 싶을 때 사용합니다. Domain 변경사항을 Entity에 반영합니다.</p>
     *
     * <p><strong>주의:</strong> 이 메서드는 Entity를 직접 수정합니다.</p>
     *
     * @param entity 업데이트할 JPA Entity (필수)
     * @param newValue 새로운 Setting 값 (필수)
     * @param updatedAt 업데이트 시각 (필수)
     * @throws IllegalArgumentException 필수 파라미터가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static void updateEntityValue(SettingJpaEntity entity, String newValue, LocalDateTime updatedAt) {
        if (entity == null) {
            throw new IllegalArgumentException("SettingJpaEntity는 필수입니다");
        }
        if (newValue == null) {
            throw new IllegalArgumentException("새로운 Setting 값은 필수입니다");
        }
        if (updatedAt == null) {
            throw new IllegalArgumentException("updatedAt는 필수입니다");
        }

        entity.updateValue(newValue, updatedAt);
    }
}
