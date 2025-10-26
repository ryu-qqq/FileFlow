package com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.entity.SettingJpaEntity;
import com.ryuqq.fileflow.domain.settings.SettingLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Setting JPA Repository Interface
 *
 * <p>Spring Data JPA Repository로 기본 CRUD 및 커스텀 쿼리를 제공합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>기본 CRUD 작업 (save, findById, findAll, delete 등)</li>
 *   <li>커스텀 쿼리 메서드 정의 (Spring Data JPA 메서드 네이밍 규칙)</li>
 *   <li>3레벨 병합을 위한 대량 조회 메서드</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Spring Data JPA 인터페이스</li>
 *   <li>✅ 메서드 네이밍 규칙 따르기 (findBy, existsBy 등)</li>
 *   <li>✅ {@code @Query} 어노테이션으로 복잡한 쿼리 정의</li>
 *   <li>✅ JPQL 사용 (Native Query 지양)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
public interface SettingJpaRepository extends JpaRepository<SettingJpaEntity, Long> {

    /**
     * Setting을 Key, Level, ContextId로 조회합니다.
     *
     * <p>유일 제약 조건: (setting_key, level, context_id) UNIQUE INDEX</p>
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <ul>
     *   <li>특정 조직의 특정 설정 조회: key="app.max_size", level=ORG, contextId=1</li>
     *   <li>기본 설정 조회: key="app.max_size", level=DEFAULT, contextId=null</li>
     * </ul>
     *
     * @param settingKey Setting 키
     * @param level Setting 레벨
     * @param contextId Context ID (DEFAULT는 null)
     * @return Optional<SettingJpaEntity>
     * @author ryu-qqq
     * @since 2025-10-25
     */
    Optional<SettingJpaEntity> findBySettingKeyAndLevelAndContextId(
        String settingKey,
        SettingLevel level,
        Long contextId
    );

    /**
     * 조직(ORG) 레벨의 모든 Setting을 조회합니다.
     *
     * <p>특정 조직의 설정을 모두 가져올 때 사용합니다.</p>
     *
     * @param orgId Organization ID
     * @return ORG 레벨 Setting 목록
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Query("SELECT s FROM SettingJpaEntity s WHERE s.level = 'ORG' AND s.contextId = :orgId")
    List<SettingJpaEntity> findAllByOrg(@Param("orgId") Long orgId);

    /**
     * 테넌트(TENANT) 레벨의 모든 Setting을 조회합니다.
     *
     * <p>특정 테넌트의 설정을 모두 가져올 때 사용합니다.</p>
     *
     * @param tenantId Tenant ID (Long FK)
     * @return TENANT 레벨 Setting 목록
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Query("SELECT s FROM SettingJpaEntity s WHERE s.level = 'TENANT' AND s.contextId = :tenantId")
    List<SettingJpaEntity> findAllByTenant(@Param("tenantId") Long tenantId);

    /**
     * 기본(DEFAULT) 레벨의 모든 Setting을 조회합니다.
     *
     * <p>전역 기본 설정을 모두 가져올 때 사용합니다.</p>
     *
     * @return DEFAULT 레벨 Setting 목록
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Query("SELECT s FROM SettingJpaEntity s WHERE s.level = 'DEFAULT'")
    List<SettingJpaEntity> findAllDefaults();

    /**
     * 특정 Level과 ContextId로 모든 Setting을 조회합니다.
     *
     * <p>범용 조회 메서드로, 레벨별 조회를 단일 메서드로 처리합니다.</p>
     *
     * @param level Setting 레벨
     * @param contextId Context ID (DEFAULT는 null)
     * @return Setting 목록
     * @author ryu-qqq
     * @since 2025-10-25
     */
    List<SettingJpaEntity> findAllByLevelAndContextId(SettingLevel level, Long contextId);

    /**
     * 특정 Setting이 존재하는지 확인합니다.
     *
     * <p>중복 생성 방지를 위한 존재 여부 체크에 사용합니다.</p>
     *
     * @param settingKey Setting 키
     * @param level Setting 레벨
     * @param contextId Context ID (DEFAULT는 null)
     * @return 존재 여부 (true: 존재, false: 미존재)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    boolean existsBySettingKeyAndLevelAndContextId(
        String settingKey,
        SettingLevel level,
        Long contextId
    );

    /**
     * 특정 조직의 Setting 개수를 조회합니다.
     *
     * <p>조직별 설정 개수 통계에 사용합니다.</p>
     *
     * @param orgId Organization ID
     * @return ORG 레벨 Setting 개수
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Query("SELECT COUNT(s) FROM SettingJpaEntity s WHERE s.level = 'ORG' AND s.contextId = :orgId")
    long countByOrg(@Param("orgId") Long orgId);

    /**
     * 특정 테넌트의 Setting 개수를 조회합니다.
     *
     * <p>테넌트별 설정 개수 통계에 사용합니다.</p>
     *
     * @param tenantId Tenant ID
     * @return TENANT 레벨 Setting 개수
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Query("SELECT COUNT(s) FROM SettingJpaEntity s WHERE s.level = 'TENANT' AND s.contextId = :tenantId")
    long countByTenant(@Param("tenantId") Long tenantId);

    /**
     * 비밀 키 Setting 목록을 조회합니다.
     *
     * <p>보안 감사 및 비밀 키 관리에 사용합니다.</p>
     *
     * @return 비밀 키 Setting 목록
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Query("SELECT s FROM SettingJpaEntity s WHERE s.isSecret = true")
    List<SettingJpaEntity> findAllSecretSettings();

    /**
     * 특정 Setting Key로 모든 레벨의 Setting을 조회합니다.
     *
     * <p>동일 키의 레벨별 설정을 비교하거나 오버라이드 상황을 확인할 때 사용합니다.</p>
     *
     * <p><strong>반환 순서</strong>: ORG → TENANT → DEFAULT (우선순위 높은 순)</p>
     *
     * @param settingKey Setting 키
     * @return 모든 레벨의 Setting 목록 (우선순위 순)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Query("""
        SELECT s FROM SettingJpaEntity s
        WHERE s.settingKey = :settingKey
        ORDER BY CASE s.level
            WHEN com.ryuqq.fileflow.domain.settings.SettingLevel.ORG THEN 1
            WHEN com.ryuqq.fileflow.domain.settings.SettingLevel.TENANT THEN 2
            WHEN com.ryuqq.fileflow.domain.settings.SettingLevel.DEFAULT THEN 3
        END
        """)
    List<SettingJpaEntity> findAllBySettingKeyOrderByPriority(@Param("settingKey") String settingKey);
}
