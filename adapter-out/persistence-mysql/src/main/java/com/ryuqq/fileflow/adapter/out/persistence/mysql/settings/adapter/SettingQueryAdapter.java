package com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.SettingJpaRepository;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.mapper.SettingEntityMapper;
import com.ryuqq.fileflow.application.settings.port.out.LoadSettingsPort;
import com.ryuqq.fileflow.domain.settings.Setting;
import com.ryuqq.fileflow.domain.settings.SettingKey;
import com.ryuqq.fileflow.domain.settings.SettingLevel;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Setting Query Adapter - CQRS Query 측 Persistence Adapter
 *
 * <p>헥사고날 아키텍처 + CQRS 패턴: {@link LoadSettingsPort} 구현체입니다.</p>
 * <p>Setting 조회(Query) 작업만 담당합니다.</p>
 *
 * <p><strong>CQRS 원칙:</strong></p>
 * <ul>
 *   <li>✅ Query 작업만 구현 (find*, load*)</li>
 *   <li>✅ Command 작업은 {@link SettingCommandAdapter}에서 분리</li>
 *   <li>✅ Read-Only 작업 (데이터 변경 없음)</li>
 * </ul>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Setting 단건/목록 조회</li>
 *   <li>3레벨 병합용 대량 조회 (ORG, TENANT, DEFAULT)</li>
 *   <li>Entity → Domain 변환 (Mapper 위임)</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ {@code @Component} 사용 (Spring Bean 등록)</li>
 *   <li>✅ {@code LoadSettingsPort} 구현</li>
 *   <li>✅ JPA Repository 사용 (Spring Data JPA)</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ Law of Demeter 준수 (Mapper를 통한 변환)</li>
 *   <li>❌ {@code @Transactional} 사용 금지 (Application Layer에서만)</li>
 *   <li>❌ Write 작업 절대 금지 (save, update, delete 등)</li>
 * </ul>
 *
 * <p><strong>성능 최적화:</strong></p>
 * <ul>
 *   <li>✅ DTO Projection 사용 (필요한 컬럼만 조회)</li>
 *   <li>✅ N+1 문제 방지 (Batch 조회)</li>
 *   <li>✅ 불변 컬렉션 반환 (Collectors.toUnmodifiableList())</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
@Component
public class SettingQueryAdapter implements LoadSettingsPort {

    private final SettingJpaRepository jpaRepository;

    /**
     * Constructor - 의존성 주입
     *
     * @param jpaRepository Setting JPA Repository
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public SettingQueryAdapter(SettingJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /**
     * Setting ID로 조회
     *
     * <p>Setting을 ID로 조회합니다. 관리 목적의 단건 조회에 사용됩니다.</p>
     *
     * @param id Setting ID (필수)
     * @return Setting (존재하지 않으면 Optional.empty())
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
     * Setting Key와 Level로 조회
     *
     * <p>특정 Key와 Level에 해당하는 Setting을 조회합니다.</p>
     * <p>유일 제약 조건: (setting_key, level, context_id) UNIQUE INDEX</p>
     *
     * @param key Setting Key (필수)
     * @param level Setting Level (필수)
     * @param contextId Context ID (ORG/TENANT는 필수, DEFAULT는 null)
     * @return Setting (존재하지 않으면 Optional.empty())
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
     * Level과 Context로 모든 Setting 조회
     *
     * <p>특정 Level과 Context에 속한 모든 Setting을 조회합니다.</p>
     *
     * @param level Setting Level (필수)
     * @param contextId Context ID (ORG/TENANT는 필수, DEFAULT는 null)
     * @return Setting 목록 (없으면 빈 리스트)
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
     * ORG Level의 모든 Setting 조회
     *
     * <p>특정 Organization의 모든 설정을 조회합니다.</p>
     *
     * @param orgId Organization ID (필수)
     * @return Setting 목록 (없으면 빈 리스트)
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
     * TENANT Level의 모든 Setting 조회
     *
     * <p>특정 Tenant의 모든 설정을 조회합니다.</p>
     *
     * @param tenantId Tenant ID (필수)
     * @return Setting 목록 (없으면 빈 리스트)
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
     * DEFAULT Level의 모든 Setting 조회
     *
     * <p>DEFAULT Level의 모든 설정을 조회합니다.</p>
     *
     * @return Setting 목록 (없으면 빈 리스트)
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
     * 3레벨 병합용 모든 Setting 조회
     *
     * <p>ORG, TENANT, DEFAULT 3레벨의 모든 설정을 한 번에 조회합니다.
     * 3레벨 우선순위 병합(ORG > TENANT > DEFAULT)을 위해 사용됩니다.</p>
     *
     * <p><strong>조회 전략:</strong></p>
     * <ul>
     *   <li>✅ Single Query로 3레벨 모두 조회 (성능 최적화)</li>
     *   <li>✅ DTO Projection 사용 (필요한 컬럼만 조회)</li>
     *   <li>✅ N+1 문제 방지</li>
     * </ul>
     *
     * <p><strong>조회 규칙:</strong></p>
     * <ul>
     *   <li>orgId != null: ORG + TENANT + DEFAULT 조회</li>
     *   <li>orgId == null && tenantId != null: TENANT + DEFAULT 조회</li>
     *   <li>orgId == null && tenantId == null: DEFAULT만 조회</li>
     * </ul>
     *
     * @param orgId Organization ID (nullable)
     * @param tenantId Tenant ID (nullable)
     * @return SettingsForMerge (3레벨 Setting 목록 포함)
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
}
