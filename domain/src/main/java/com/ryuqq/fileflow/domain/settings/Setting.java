package com.ryuqq.fileflow.domain.settings;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Setting Aggregate Root
 *
 * <p>EAV(Entity-Attribute-Value) 기반 설정 시스템의 집합 루트입니다.</p>
 * <p>각 Setting은 특정 레벨(ORG/TENANT/DEFAULT)에서 정의되며, 3단계 우선순위 병합을 지원합니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Law of Demeter - Getter 체이닝 방지</li>
 *   <li>✅ Tell, Don't Ask 패턴 적용</li>
 *   <li>✅ Long FK 전략 - JPA 관계 어노테이션 금지</li>
 *   <li>✅ Aggregate 경계 내에서 일관성 보장</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
public class Setting {

    private final Long id;
    private final SettingKey key;
    private final SettingLevel level;
    private final Long contextId;
    private final Clock clock;
    private SettingValue value;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Setting을 생성합니다 (Static Factory Method).
     *
     * <p>생성 시 모든 필수 필드를 검증하고 초기 상태를 설정합니다.</p>
     * <p>ID가 null이면 신규 Setting (DB 저장 전 상태), 아니면 저장 후 상태입니다.</p>
     *
     * @param id Setting 식별자 (null 가능 - 신규 생성 시)
     * @param key 설정 키
     * @param value 설정 값
     * @param level 설정 레벨 (ORG/TENANT/DEFAULT)
     * @param contextId 컨텍스트 ID (ORG/TENANT의 경우 해당 ID, DEFAULT는 null)
     * @return 생성된 Setting
     * @throws IllegalArgumentException 필수 필드가 null이거나 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static Setting of(
        Long id,
        SettingKey key,
        SettingValue value,
        SettingLevel level,
        Long contextId
    ) {
        return new Setting(id, key, value, level, contextId, Clock.systemDefaultZone());
    }

    /**
     * Setting 생성자 (package-private).
     *
     * @param id Setting 식별자 (null 가능 - 신규 생성 시)
     * @param key 설정 키
     * @param value 설정 값
     * @param level 설정 레벨
     * @param contextId 컨텍스트 ID
     * @param clock 시간 제공자
     * @throws IllegalArgumentException 필수 필드가 null이거나 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    Setting(
        Long id,
        SettingKey key,
        SettingValue value,
        SettingLevel level,
        Long contextId,
        Clock clock
    ) {
        validateRequiredFields(key, value, level, contextId);

        this.id = id;
        this.key = key;
        this.value = value;
        this.level = level;
        this.contextId = contextId;
        this.clock = clock;
        this.createdAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Private 전체 생성자 (reconstitute 전용)
     *
     * @param id Setting ID
     * @param key 설정 키
     * @param value 설정 값
     * @param level 설정 레벨
     * @param contextId 컨텍스트 ID
     * @param clock 시간 제공자
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @author ryu-qqq
     * @since 2025-10-25
     */
    private Setting(
        Long id,
        SettingKey key,
        SettingValue value,
        SettingLevel level,
        Long contextId,
        Clock clock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.level = level;
        this.contextId = contextId;
        this.clock = clock;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * DB에서 조회한 데이터로 Setting 재구성 (Static Factory Method)
     *
     * <p>Persistence Layer에서 DB 데이터를 Domain으로 변환할 때 사용합니다.</p>
     *
     * @param id Setting ID
     * @param key 설정 키
     * @param value 설정 값
     * @param level 설정 레벨
     * @param contextId 컨텍스트 ID
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @return 재구성된 Setting
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static Setting reconstitute(
        Long id,
        SettingKey key,
        SettingValue value,
        SettingLevel level,
        Long contextId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new Setting(
            id, key, value, level, contextId,
            Clock.systemDefaultZone(), createdAt, updatedAt
        );
    }

    /**
     * 필수 필드의 유효성을 검증합니다.
     *
     * @param key 설정 키
     * @param value 설정 값
     * @param level 설정 레벨
     * @param contextId 컨텍스트 ID
     * @throws IllegalArgumentException 필수 필드가 null이거나 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    private static void validateRequiredFields(
        SettingKey key,
        SettingValue value,
        SettingLevel level,
        Long contextId
    ) {
        if (key == null) {
            throw new IllegalArgumentException("Setting 키는 필수입니다");
        }
        if (value == null) {
            throw new IllegalArgumentException("Setting 값은 필수입니다");
        }
        if (level == null) {
            throw new IllegalArgumentException("Setting 레벨은 필수입니다");
        }
        if (level != SettingLevel.DEFAULT && contextId == null) {
            throw new IllegalArgumentException(
                level + " 레벨의 Setting은 contextId가 필수입니다"
            );
        }
        if (level == SettingLevel.DEFAULT && contextId != null) {
            throw new IllegalArgumentException(
                "DEFAULT 레벨의 Setting은 contextId가 null이어야 합니다"
            );
        }
    }

    /**
     * 설정 값을 업데이트합니다.
     *
     * <p>Law of Demeter 준수: 상태 변경 로직을 캡슐화합니다.</p>
     * <p>Value Object 불변성: 새로운 SettingValue 객체를 생성합니다.</p>
     *
     * @param newValue 새로운 설정 값
     * @throws IllegalArgumentException 새 값이 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public void updateValue(SettingValue newValue) {
        if (newValue == null) {
            throw new IllegalArgumentException("Setting 값은 null일 수 없습니다");
        }

        this.value = newValue;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 현재 Setting이 다른 Setting보다 높은 우선순위를 가지는지 확인합니다.
     *
     * <p>Law of Demeter 준수: 우선순위 비교 로직 캡슐화</p>
     * <p>❌ Bad: setting1.getLevel().hasHigherPriorityThan(setting2.getLevel())</p>
     * <p>✅ Good: setting1.hasHigherPriorityThan(setting2)</p>
     *
     * @param other 비교 대상 Setting
     * @return 현재 Setting이 더 높은 우선순위를 가지면 true
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public boolean hasHigherPriorityThan(Setting other) {
        if (other == null) {
            return true;
        }
        return this.level.hasHigherPriorityThan(other.level);
    }

    /**
     * 특정 키를 가지는지 확인합니다.
     *
     * <p>Law of Demeter 준수: 키 비교 로직 캡슐화</p>
     * <p>❌ Bad: setting.getKey().isSameAs(key)</p>
     * <p>✅ Good: setting.hasKey(key)</p>
     *
     * @param targetKey 확인할 키
     * @return 동일한 키를 가지면 true
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public boolean hasKey(SettingKey targetKey) {
        return this.key.isSameAs(targetKey);
    }

    /**
     * 특정 레벨인지 확인합니다.
     *
     * <p>Law of Demeter 준수: 레벨 확인 로직 캡슐화</p>
     * <p>❌ Bad: setting.getLevel() == SettingLevel.ORG</p>
     * <p>✅ Good: setting.isLevel(SettingLevel.ORG)</p>
     *
     * @param targetLevel 확인할 레벨
     * @return 동일한 레벨이면 true
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public boolean isLevel(SettingLevel targetLevel) {
        return this.level == targetLevel;
    }

    /**
     * 특정 컨텍스트에 속하는지 확인합니다.
     *
     * <p>Law of Demeter 준수: 컨텍스트 확인 로직 캡슐화</p>
     * <p>❌ Bad: setting.getContextId().equals(contextId)</p>
     * <p>✅ Good: setting.belongsToContext(contextId)</p>
     *
     * @param targetContextId 확인할 컨텍스트 ID
     * @return 동일한 컨텍스트에 속하면 true
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public boolean belongsToContext(Long targetContextId) {
        if (targetContextId == null) {
            return this.contextId == null;
        }
        return targetContextId.equals(this.contextId);
    }

    /**
     * Setting ID를 반환합니다.
     *
     * @return Setting ID
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public Long getId() {
        return id;
    }

    /**
     * 설정 키를 반환합니다.
     *
     * @return 설정 키
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public SettingKey getKey() {
        return key;
    }

    /**
     * 설정 키 문자열 값을 반환합니다 (Law of Demeter 준수).
     *
     * <p>❌ Bad: setting.getKey().getValue()</p>
     * <p>✅ Good: setting.getKeyValue()</p>
     *
     * @return 설정 키 문자열 값
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public String getKeyValue() {
        return key.getValue();
    }

    /**
     * 설정 값을 반환합니다.
     *
     * @return 설정 값
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public SettingValue getValue() {
        return value;
    }

    /**
     * 설정 값 문자열을 반환합니다 (Law of Demeter 준수).
     *
     * <p>❌ Bad: setting.getValue().getValue()</p>
     * <p>✅ Good: setting.getRawValue()</p>
     *
     * @return 설정 값 문자열 (원본)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public String getRawValue() {
        return value.getValue();
    }

    /**
     * 표시용 설정 값을 반환합니다 (Law of Demeter 준수).
     *
     * <p>❌ Bad: setting.getValue().getDisplayValue()</p>
     * <p>✅ Good: setting.getDisplayValue()</p>
     *
     * <p>비밀 키인 경우 마스킹된 값을 반환합니다.</p>
     *
     * @return 표시용 설정 값
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public String getDisplayValue() {
        return value.getDisplayValue();
    }

    /**
     * 설정 타입을 반환합니다 (Law of Demeter 준수).
     *
     * <p>❌ Bad: setting.getValue().getType()</p>
     * <p>✅ Good: setting.getValueType()</p>
     *
     * @return 설정 타입
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public SettingType getValueType() {
        return value.getType();
    }

    /**
     * 비밀 키 여부를 반환합니다 (Law of Demeter 준수).
     *
     * <p>❌ Bad: setting.getValue().isSecret()</p>
     * <p>✅ Good: setting.isSecret()</p>
     *
     * @return 비밀 키 여부 (true: 비밀 키, false: 일반 설정)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public boolean isSecret() {
        return value.isSecret();
    }

    /**
     * 설정 레벨을 반환합니다.
     *
     * @return 설정 레벨
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public SettingLevel getLevel() {
        return level;
    }

    /**
     * 컨텍스트 ID를 반환합니다.
     *
     * @return 컨텍스트 ID
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public Long getContextId() {
        return contextId;
    }

    /**
     * 생성 시각을 반환합니다.
     *
     * @return 생성 시각
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 최종 수정 시각을 반환합니다.
     *
     * @return 최종 수정 시각
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 동등성을 비교합니다.
     *
     * <p>동일성은 ID로만 판단합니다 (Aggregate 식별자 기반).</p>
     *
     * @param o 비교 대상 객체
     * @return 동등 여부
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Setting setting = (Setting) o;
        return Objects.equals(id, setting.id);
    }

    /**
     * 해시코드를 반환합니다.
     *
     * <p>해시코드는 ID로만 계산합니다.</p>
     *
     * @return 해시코드
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * 문자열 표현을 반환합니다.
     *
     * @return Setting 정보 문자열
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public String toString() {
        return "Setting{" +
            "id=" + id +
            ", key=" + key +
            ", value=" + value +
            ", level=" + level +
            ", contextId=" + contextId +
            '}';
    }
}
