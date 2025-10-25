package com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.SettingJpaRepository;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.entity.SettingJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.mapper.SettingEntityMapper;
import com.ryuqq.fileflow.domain.settings.Setting;
import com.ryuqq.fileflow.domain.settings.SettingKey;
import com.ryuqq.fileflow.domain.settings.SettingLevel;
import com.ryuqq.fileflow.domain.settings.SettingRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SettingRepositoryAdapter - Setting 전용 Persistence Adapter (DEPRECATED)
 *
 * <p><strong>⚠️ DEPRECATED:</strong> CQRS 패턴 적용으로 대체됨</p>
 * <ul>
 *   <li>Query 작업은 {@link SettingQueryAdapter} 사용 (LoadSettingsPort 구현)</li>
 *   <li>Command 작업은 {@link SettingCommandAdapter} 사용 (SaveSettingPort 구현)</li>
 * </ul>
 *
 * <p><strong>마이그레이션 가이드:</strong></p>
 * <pre>
 * // 기존 (Deprecated)
 * {@literal @}Autowired
 * private SettingRepository settingRepository;
 *
 * // 신규 (CQRS 패턴)
 * {@literal @}Autowired
 * private LoadSettingsPort loadSettingsPort;  // Query 작업
 *
 * {@literal @}Autowired
 * private SaveSettingPort saveSettingPort;    // Command 작업
 * </pre>
 *
 * <p><strong>CQRS 분리 이유:</strong></p>
 * <ul>
 *   <li>✅ Query/Command 책임 분리 (SRP 준수)</li>
 *   <li>✅ Query 최적화 (DTO Projection, Read-Only)</li>
 *   <li>✅ Command 최적화 (Batch Insert, Bulk Delete)</li>
 *   <li>✅ 트랜잭션 관리 명확화 (Command만 트랜잭션)</li>
 * </ul>
 *
 * <p><strong>제거 예정:</strong> 2026-01-01 이후 삭제 예정</p>
 *
 * @deprecated CQRS 패턴 적용으로 {@link SettingQueryAdapter}와 {@link SettingCommandAdapter}로 분리됨
 * @see SettingQueryAdapter Query 작업 (LoadSettingsPort 구현)
 * @see SettingCommandAdapter Command 작업 (SaveSettingPort 구현)
 * @author ryu-qqq
 * @since 2025-10-25
 */
@Deprecated(since = "2025-10-25", forRemoval = true)
@Component
public class SettingRepositoryAdapter implements SettingRepository {

    private final SettingJpaRepository jpaRepository;

    /**
     * Constructor - 의존성 주입
     *
     * @param jpaRepository Setting JPA Repository
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public SettingRepositoryAdapter(SettingJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /**
     * Setting을 저장합니다.
     *
     * <p>신규 생성 또는 업데이트를 수행합니다.</p>
     *
     * <p><strong>저장 로직:</strong></p>
     * <ul>
     *   <li>ID가 null이면 신규 생성</li>
     *   <li>ID가 있으면 업데이트</li>
     * </ul>
     *
     * @param setting 저장할 Setting (필수)
     * @return 저장된 Setting (ID 포함)
     * @throws IllegalArgumentException setting이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public Setting save(Setting setting) {
        if (setting == null) {
            throw new IllegalArgumentException("Setting은 필수입니다");
        }

        // Domain → Entity 변환
        SettingJpaEntity entity = (setting.getId() == null)
            ? SettingEntityMapper.toEntity(setting)              // 신규 생성
            : SettingEntityMapper.toEntityForUpdate(setting);    // 업데이트

        // JPA 저장
        SettingJpaEntity savedEntity = jpaRepository.save(entity);

        // Entity → Domain 변환
        return SettingEntityMapper.toDomain(savedEntity);
    }

    /**
     * Setting을 Key, Level, ContextId로 조회합니다.
     *
     * <p>유일 제약 조건: (setting_key, level, context_id) UNIQUE INDEX</p>
     *
     * @param key Setting 키 (필수)
     * @param level Setting 레벨 (필수)
     * @param contextId Context ID (DEFAULT는 null)
     * @return Optional<Setting>
     * @throws IllegalArgumentException key 또는 level이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public Optional<Setting> findByKeyAndLevel(SettingKey key, SettingLevel level, Long contextId) {
        if (key == null) {
            throw new IllegalArgumentException("SettingKey는 필수입니다");
        }
        if (level == null) {
            throw new IllegalArgumentException("SettingLevel은 필수입니다");
        }

        return jpaRepository.findBySettingKeyAndLevelAndContextId(key.getValue(), level, contextId)
            .map(SettingEntityMapper::toDomain);
    }

    /**
     * 조직(ORG) 레벨의 모든 Setting을 조회합니다.
     *
     * @param orgId Organization ID (필수)
     * @return ORG 레벨 Setting 목록 (불변)
     * @throws IllegalArgumentException orgId가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public List<Setting> findByOrg(Long orgId) {
        if (orgId == null) {
            throw new IllegalArgumentException("Organization ID는 필수입니다");
        }

        return jpaRepository.findAllByOrg(orgId)
            .stream()
            .map(SettingEntityMapper::toDomain)
            .collect(Collectors.toUnmodifiableList());
    }

    /**
     * 테넌트(TENANT) 레벨의 모든 Setting을 조회합니다.
     *
     * @param tenantId Tenant ID (Long FK, 필수)
     * @return TENANT 레벨 Setting 목록 (불변)
     * @throws IllegalArgumentException tenantId가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public List<Setting> findByTenant(Long tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID는 필수입니다");
        }

        return jpaRepository.findAllByTenant(tenantId)
            .stream()
            .map(SettingEntityMapper::toDomain)
            .collect(Collectors.toUnmodifiableList());
    }

    /**
     * 기본(DEFAULT) 레벨의 모든 Setting을 조회합니다.
     *
     * @return DEFAULT 레벨 Setting 목록 (불변)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public List<Setting> findDefaults() {
        return jpaRepository.findAllDefaults()
            .stream()
            .map(SettingEntityMapper::toDomain)
            .collect(Collectors.toUnmodifiableList());
    }

    /**
     * 3레벨 병합을 위한 설정을 한 번에 조회합니다.
     *
     * <p>성능 최적화: 3개의 쿼리를 병렬로 실행하여 N+1 문제를 방지합니다.</p>
     *
     * <p><strong>조회 전략:</strong></p>
     * <ul>
     *   <li>orgId가 null이면 ORG 레벨 조회 안함 (빈 리스트 반환)</li>
     *   <li>tenantId가 null이면 TENANT 레벨 조회 안함 (빈 리스트 반환)</li>
     *   <li>DEFAULT 레벨은 항상 조회 (전역 기본값)</li>
     * </ul>
     *
     * @param orgId Organization ID (null 허용)
     * @param tenantId Tenant ID (null 허용)
     * @return SettingsForMerge (3레벨 설정 묶음)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public SettingsForMerge findAllForMerge(Long orgId, Long tenantId) {
        // 1. ORG 레벨 조회 (orgId가 있을 때만)
        List<Setting> orgSettings = (orgId != null)
            ? findByOrg(orgId)
            : Collections.emptyList();

        // 2. TENANT 레벨 조회 (tenantId가 있을 때만)
        List<Setting> tenantSettings = (tenantId != null)
            ? findByTenant(tenantId)
            : Collections.emptyList();

        // 3. DEFAULT 레벨 조회 (항상 조회)
        List<Setting> defaultSettings = findDefaults();

        // SettingsForMerge DTO 생성
        return new SettingsForMerge(orgSettings, tenantSettings, defaultSettings);
    }

    /**
     * 여러 Setting을 일괄 저장합니다.
     *
     * <p>대량 저장 작업을 수행합니다.</p>
     *
     * @param settings 저장할 Setting 목록 (필수)
     * @return 저장된 Setting 목록 (ID 포함)
     * @throws IllegalArgumentException settings가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public List<Setting> saveAll(List<Setting> settings) {
        if (settings == null) {
            throw new IllegalArgumentException("Settings는 필수입니다");
        }

        List<SettingJpaEntity> entities = settings.stream()
            .map(setting -> (setting.getId() == null)
                ? SettingEntityMapper.toEntity(setting)
                : SettingEntityMapper.toEntityForUpdate(setting))
            .collect(Collectors.toList());

        List<SettingJpaEntity> savedEntities = jpaRepository.saveAll(entities);

        return savedEntities.stream()
            .map(SettingEntityMapper::toDomain)
            .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Setting ID로 조회합니다.
     *
     * <p>관리 목적의 단건 조회에 사용합니다.</p>
     *
     * @param id Setting ID (필수)
     * @return Optional<Setting>
     * @throws IllegalArgumentException id가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public Optional<Setting> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Setting ID는 필수입니다");
        }

        return jpaRepository.findById(id)
            .map(SettingEntityMapper::toDomain);
    }

    /**
     * 특정 레벨과 컨텍스트의 모든 Setting을 조회합니다.
     *
     * @param level 설정 레벨 (필수)
     * @param contextId 컨텍스트 ID (DEFAULT 레벨은 null)
     * @return 조회된 Setting 목록 (불변)
     * @throws IllegalArgumentException level이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public List<Setting> findByLevelAndContext(SettingLevel level, Long contextId) {
        if (level == null) {
            throw new IllegalArgumentException("SettingLevel은 필수입니다");
        }

        return jpaRepository.findAllByLevelAndContextId(level, contextId)
            .stream()
            .map(SettingEntityMapper::toDomain)
            .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Setting을 삭제합니다.
     *
     * <p><strong>주의:</strong> 물리적 삭제를 수행합니다. 소프트 삭제가 필요한 경우 별도 구현 필요.</p>
     *
     * @param id Setting ID (필수)
     * @throws IllegalArgumentException id가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Setting ID는 필수입니다");
        }

        jpaRepository.deleteById(id);
    }

    /**
     * 특정 컨텍스트의 모든 Setting을 삭제합니다.
     *
     * @param level 설정 레벨 (필수)
     * @param contextId 컨텍스트 ID (필수)
     * @throws IllegalArgumentException level이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public void deleteByLevelAndContext(SettingLevel level, Long contextId) {
        if (level == null) {
            throw new IllegalArgumentException("SettingLevel은 필수입니다");
        }

        List<SettingJpaEntity> entities = jpaRepository.findAllByLevelAndContextId(level, contextId);
        jpaRepository.deleteAll(entities);
    }
}
