package com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.entity.SettingJpaEntity;
import com.ryuqq.fileflow.domain.settings.SettingLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Setting Spring Data JPA Repository
 *
 * <p><strong>역할</strong>: Setting Entity에 대한 기본 CRUD 및 쿼리 메서드 제공</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/settings/repository/</p>
 * <p><strong>CQRS</strong>: Command Side (CUD 작업 전용)</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ Spring Data JPA 인터페이스 (구현체 자동 생성)</li>
 *   <li>✅ 메서드 네이밍 규칙 준수 (Spring Data JPA Query Methods)</li>
 *   <li>✅ Command Side 전용 (복잡한 조회는 Query Adapter)</li>
 *   <li>❌ {@code @Query} 어노테이션 금지 (Query Adapter로 이동)</li>
 *   <li>❌ {@code @Repository} 어노테이션 불필요 (JpaRepository 상속 시 자동)</li>
 * </ul>
 *
 * <h3>Query Adapter로 이동 필요</h3>
 * <ul>
 *   <li>비밀 키 조회 (보안 감사)</li>
 *   <li>우선순위 정렬 조회 (복잡한 정렬 로직)</li>
 *   <li>통계 및 집계 쿼리</li>
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
     * 특정 레벨의 모든 Setting을 조회합니다.
     *
     * <p>Spring Data JPA 메서드 네이밍 규칙으로 레벨별 조회를 처리합니다.</p>
     *
     * <p><strong>사용 예시:</strong></p>
     * <ul>
     *   <li>DEFAULT 레벨: {@code findAllByLevel(SettingLevel.DEFAULT)}</li>
     * </ul>
     *
     * @param level Setting 레벨
     * @return Setting 목록
     * @author ryu-qqq
     * @since 2025-10-25
     */
    List<SettingJpaEntity> findAllByLevel(SettingLevel level);

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
     * 특정 레벨과 Context ID의 Setting 개수를 조회합니다.
     *
     * <p>Spring Data JPA 메서드 네이밍 규칙으로 레벨별 개수를 처리합니다.</p>
     *
     * <p><strong>사용 예시:</strong></p>
     * <ul>
     *   <li>ORG 레벨: {@code countByLevelAndContextId(SettingLevel.ORG, orgId)}</li>
     *   <li>TENANT 레벨: {@code countByLevelAndContextId(SettingLevel.TENANT, tenantId)}</li>
     * </ul>
     *
     * @param level Setting 레벨
     * @param contextId Context ID
     * @return Setting 개수
     * @author ryu-qqq
     * @since 2025-10-25
     */
    long countByLevelAndContextId(SettingLevel level, Long contextId);
}
