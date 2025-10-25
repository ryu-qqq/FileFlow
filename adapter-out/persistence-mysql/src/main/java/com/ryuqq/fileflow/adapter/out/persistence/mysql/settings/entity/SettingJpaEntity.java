package com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.entity;

import com.ryuqq.fileflow.domain.settings.SettingLevel;
import com.ryuqq.fileflow.domain.settings.SettingType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/**
 * Setting JPA Entity (Persistence Layer)
 *
 * <p><strong>역할</strong>: EAV (Entity-Attribute-Value) 모델 DB 매핑</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/settings/entity/</p>
 * <p><strong>변환</strong>: {@code SettingEntityMapper}를 통해 Domain {@code Setting}과 상호 변환</p>
 *
 * <h3>EAV 스키마 설계</h3>
 * <pre>
 * settings 테이블:
 * - id (PK, Auto Increment)
 * - setting_key (VARCHAR(100), Index)
 * - setting_value (TEXT)
 * - setting_type (ENUM: STRING, NUMBER, BOOLEAN, JSON_OBJECT, JSON_ARRAY)
 * - level (ENUM: ORG, TENANT, DEFAULT)
 * - context_id (BIGINT, NULL 허용 - DEFAULT는 NULL)
 * - is_secret (BOOLEAN, 비밀 키 여부)
 * - created_at (TIMESTAMP)
 * - updated_at (TIMESTAMP)
 *
 * UNIQUE INDEX: (setting_key, level, context_id)
 * </pre>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ JPA 어노테이션만 사용 (비즈니스 로직 없음)</li>
 *   <li>✅ Long FK 전략 (JPA 관계 어노테이션 금지)</li>
 *   <li>✅ {@code contextId}는 Long FK (Org/Tenant 객체 참조 금지)</li>
 *   <li>✅ Getter만 제공 (Setter 금지)</li>
 *   <li>✅ {@code private final} 필드 (변경 불가능한 필드)</li>
 *   <li>✅ Static Factory Methods: {@code create()}, {@code reconstitute()}</li>
 *   <li>❌ Lombok 금지</li>
 *   <li>❌ JPA 관계 어노테이션 금지 - Long contextId 사용</li>
 * </ul>
 *
 * @see com.ryuqq.fileflow.domain.settings.Setting Domain Model
 * @author ryu-qqq
 * @since 2025-10-25
 */
@Entity
@Table(
    name = "settings",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_setting_key_level_context",
            columnNames = {"setting_key", "level", "context_id"}
        )
    }
)
public class SettingJpaEntity {

    /**
     * Setting 고유 식별자 (Primary Key, Auto Increment)
     *
     * <p>Domain {@code Setting.id}와 직접 매핑됩니다.</p>
     * <p><strong>생성 전략</strong>: MySQL Auto Increment</p>
     *
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Setting Key (EAV 패턴의 Attribute)
     *
     * <p>Domain {@code SettingKey} Value Object의 String 값과 매핑됩니다.</p>
     * <p><strong>제약</strong>: NOT NULL, 최대 100자, Index 설정 (조회 성능 최적화)</p>
     *
     * <p><strong>예시</strong>:
     * <ul>
     *   <li>app.max_upload_size</li>
     *   <li>feature.enable_notifications</li>
     *   <li>api.rate_limit</li>
     * </ul>
     * </p>
     *
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Column(name = "setting_key", nullable = false, length = 100)
    private final String settingKey;

    /**
     * Setting Value (EAV 패턴의 Value)
     *
     * <p>Domain {@code SettingValue}의 원본 값과 매핑됩니다.</p>
     * <p><strong>저장 형식</strong>: 모든 타입을 String으로 저장 (타입 변환은 Domain Layer에서)</p>
     * <p><strong>제약</strong>: NOT NULL, TEXT 타입 (큰 JSON 데이터 저장 가능)</p>
     *
     * <p><strong>타입별 저장 예시</strong>:
     * <ul>
     *   <li>STRING: "hello world"</li>
     *   <li>NUMBER: "1024"</li>
     *   <li>BOOLEAN: "true"</li>
     *   <li>JSON_OBJECT: "{\"key\":\"value\"}"</li>
     *   <li>JSON_ARRAY: "[1,2,3]"</li>
     * </ul>
     * </p>
     *
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Column(name = "setting_value", nullable = false, columnDefinition = "TEXT")
    private String settingValue;

    /**
     * Setting 데이터 타입
     *
     * <p>Domain {@code SettingType} enum과 직접 매핑됩니다.</p>
     * <p><strong>가능한 값</strong>: STRING, NUMBER, BOOLEAN, JSON_OBJECT, JSON_ARRAY</p>
     * <p><strong>저장 방식</strong>: ENUM STRING (DB에 "STRING", "NUMBER" 등으로 저장)</p>
     *
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "setting_type", nullable = false, length = 20)
    private final SettingType settingType;

    /**
     * Setting Level (우선순위 레벨)
     *
     * <p>Domain {@code SettingLevel} enum과 직접 매핑됩니다.</p>
     * <p><strong>가능한 값</strong>: ORG (조직), TENANT (테넌트), DEFAULT (기본)</p>
     * <p><strong>우선순위</strong>: ORG(1) > TENANT(2) > DEFAULT(3)</p>
     * <p><strong>저장 방식</strong>: ENUM STRING (DB에 "ORG", "TENANT", "DEFAULT"로 저장)</p>
     *
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 20)
    private final SettingLevel level;

    /**
     * Context ID (레벨별 컨텍스트 식별자)
     *
     * <p><strong>Long FK 전략</strong>: Org/Tenant ID를 Long으로 직접 저장</p>
     * <p><strong>레벨별 의미</strong>:
     * <ul>
     *   <li>ORG 레벨: Organization ID (Long FK)</li>
     *   <li>TENANT 레벨: Tenant ID (Long FK - Tenant 테이블이 Long PK 사용 시)</li>
     *   <li>DEFAULT 레벨: NULL (전역 기본값)</li>
     * </ul>
     * </p>
     *
     * <p><strong>이유</strong>:
     * <ul>
     *   <li>Law of Demeter 준수 ({@code setting.getOrg().getId()} 금지)</li>
     *   <li>N+1 문제 방지</li>
     *   <li>불필요한 Join 방지</li>
     * </ul>
     * </p>
     *
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Column(name = "context_id")
    private final Long contextId;

    /**
     * 비밀 키 여부
     *
     * <p><strong>의미</strong>:
     * <ul>
     *   <li>true: API 응답 시 "********"로 마스킹</li>
     *   <li>false: 실제 값 노출</li>
     * </ul>
     * </p>
     *
     * <p><strong>사용 예시</strong>:
     * <ul>
     *   <li>비밀 키 (true): API 키, 비밀번호, Token</li>
     *   <li>일반 설정 (false): 기능 플래그, UI 설정, 일반 텍스트</li>
     * </ul>
     * </p>
     *
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Column(name = "is_secret", nullable = false)
    private final boolean isSecret;

    /**
     * 생성 일시
     *
     * <p><strong>불변 필드</strong>: Entity 생성 시점에만 설정</p>
     *
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private final LocalDateTime createdAt;

    /**
     * 최종 수정 일시
     *
     * <p><strong>변경 가능 필드</strong>: Entity 수정 시마다 업데이트</p>
     *
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * JPA 전용 기본 생성자 (Protected)
     *
     * <p>JPA Proxy 생성을 위해 필요합니다. 직접 호출 금지!</p>
     *
     * @author ryu-qqq
     * @since 2025-10-25
     */
    protected SettingJpaEntity() {
        this.settingKey = null;
        this.settingType = null;
        this.level = null;
        this.contextId = null;
        this.isSecret = false;
        this.createdAt = null;
    }

    /**
     * Private 전체 생성자
     *
     * <p>Static Factory Method에서만 사용합니다.</p>
     *
     * @param settingKey Setting 키
     * @param settingValue Setting 값
     * @param settingType Setting 타입
     * @param level Setting 레벨
     * @param contextId Context ID (ORG/TENANT ID, DEFAULT는 null)
     * @param isSecret 비밀 키 여부
     * @param createdAt 생성 일시
     * @param updatedAt 최종 수정 일시
     * @author ryu-qqq
     * @since 2025-10-25
     */
    private SettingJpaEntity(
        String settingKey,
        String settingValue,
        SettingType settingType,
        SettingLevel level,
        Long contextId,
        boolean isSecret,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.settingType = settingType;
        this.level = level;
        this.contextId = contextId;
        this.isSecret = isSecret;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 새로운 Setting Entity 생성 (Static Factory Method)
     *
     * <p>신규 Setting 생성 시 사용합니다.</p>
     *
     * <p><strong>검증</strong>: 필수 필드 null 체크만 수행 (비즈니스 검증은 Domain Layer에서)</p>
     *
     * @param settingKey Setting 키 (필수)
     * @param settingValue Setting 값 (필수)
     * @param settingType Setting 타입 (필수)
     * @param level Setting 레벨 (필수)
     * @param contextId Context ID (ORG/TENANT ID, DEFAULT는 null 허용)
     * @param isSecret 비밀 키 여부
     * @param createdAt 생성 일시 (필수)
     * @return 새로운 SettingJpaEntity
     * @throws IllegalArgumentException 필수 필드가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static SettingJpaEntity create(
        String settingKey,
        String settingValue,
        SettingType settingType,
        SettingLevel level,
        Long contextId,
        boolean isSecret,
        LocalDateTime createdAt
    ) {
        if (settingKey == null || settingValue == null || settingType == null || level == null || createdAt == null) {
            throw new IllegalArgumentException(
                "Required fields (settingKey, settingValue, settingType, level, createdAt) must not be null"
            );
        }

        // DEFAULT 레벨은 contextId가 null이어야 함
        if (level == SettingLevel.DEFAULT && contextId != null) {
            throw new IllegalArgumentException("DEFAULT 레벨은 contextId가 null이어야 합니다");
        }

        // ORG/TENANT 레벨은 contextId가 필수
        if ((level == SettingLevel.ORG || level == SettingLevel.TENANT) && contextId == null) {
            throw new IllegalArgumentException(level + " 레벨은 contextId가 필수입니다");
        }

        return new SettingJpaEntity(
            settingKey,
            settingValue,
            settingType,
            level,
            contextId,
            isSecret,
            createdAt,
            createdAt  // updatedAt = createdAt (초기값)
        );
    }

    /**
     * DB에서 조회한 데이터로 Entity 재구성 (Static Factory Method)
     *
     * <p>DB 조회 결과를 Entity로 변환할 때 사용합니다.</p>
     *
     * @param id Setting ID
     * @param settingKey Setting 키
     * @param settingValue Setting 값
     * @param settingType Setting 타입
     * @param level Setting 레벨
     * @param contextId Context ID (ORG/TENANT ID, DEFAULT는 null)
     * @param isSecret 비밀 키 여부
     * @param createdAt 생성 일시
     * @param updatedAt 최종 수정 일시
     * @return 재구성된 SettingJpaEntity
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static SettingJpaEntity reconstitute(
        Long id,
        String settingKey,
        String settingValue,
        SettingType settingType,
        SettingLevel level,
        Long contextId,
        boolean isSecret,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        SettingJpaEntity entity = new SettingJpaEntity(
            settingKey,
            settingValue,
            settingType,
            level,
            contextId,
            isSecret,
            createdAt,
            updatedAt
        );
        entity.id = id;  // ID는 setter 없이 직접 할당 (reconstitute 전용)
        return entity;
    }

    /**
     * Setting 값을 업데이트합니다.
     *
     * <p>Entity 레벨에서 허용된 유일한 변경 메서드입니다.</p>
     * <p>updatedAt도 함께 갱신됩니다.</p>
     *
     * @param newValue 새로운 Setting 값
     * @param updatedAt 업데이트 시각
     * @throws IllegalArgumentException newValue가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public void updateValue(String newValue, LocalDateTime updatedAt) {
        if (newValue == null) {
            throw new IllegalArgumentException("Setting 값은 null일 수 없습니다");
        }
        if (updatedAt == null) {
            throw new IllegalArgumentException("updatedAt는 null일 수 없습니다");
        }
        this.settingValue = newValue;
        this.updatedAt = updatedAt;
    }

    // ========================================
    // Getters (Public, 비즈니스 메서드 없음!)
    // ========================================

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
     * Setting Key를 반환합니다.
     *
     * @return Setting Key
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public String getSettingKey() {
        return settingKey;
    }

    /**
     * Setting Value를 반환합니다.
     *
     * @return Setting Value (원본 값)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public String getSettingValue() {
        return settingValue;
    }

    /**
     * Setting Type을 반환합니다.
     *
     * @return Setting Type
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public SettingType getSettingType() {
        return settingType;
    }

    /**
     * Setting Level을 반환합니다.
     *
     * @return Setting Level
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public SettingLevel getLevel() {
        return level;
    }

    /**
     * Context ID를 반환합니다.
     *
     * @return Context ID (DEFAULT 레벨은 null)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public Long getContextId() {
        return contextId;
    }

    /**
     * 비밀 키 여부를 반환합니다.
     *
     * @return 비밀 키 여부 (true: 비밀 키, false: 일반 설정)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public boolean isSecret() {
        return isSecret;
    }

    /**
     * 생성 일시를 반환합니다.
     *
     * @return 생성 일시
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 최종 수정 일시를 반환합니다.
     *
     * @return 최종 수정 일시
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
