package com.ryuqq.fileflow.application.settings.port.out;

import com.ryuqq.fileflow.domain.settings.Setting;
import com.ryuqq.fileflow.domain.settings.SettingLevel;

import java.util.List;

/**
 * Save Setting Port - Command 용 Outbound Port
 *
 * <p>CQRS 패턴의 Command 측면을 담당하는 Outbound Port입니다.
 * 설정 저장(생성/수정) 및 삭제 작업을 정의합니다.</p>
 *
 * <p><strong>CQRS 분리:</strong></p>
 * <ul>
 *   <li>✅ Write 작업만 정의 (save, delete)</li>
 *   <li>✅ Query 작업은 {@link LoadSettingsPort}에서 분리</li>
 * </ul>
 *
 * <p><strong>트랜잭션 관리:</strong></p>
 * <ul>
 *   <li>✅ Application Layer (UseCase)에서 `@Transactional` 관리</li>
 *   <li>❌ Persistence Adapter에 `@Transactional` 절대 금지</li>
 * </ul>
 *
 * <p><strong>구현 위치:</strong></p>
 * <ul>
 *   <li>Adapter: `adapter-out-persistence-mysql/settings/adapter/SettingPersistenceAdapter.java`</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
public interface SaveSettingPort {

    /**
     * Setting 저장 (생성 또는 수정)
     *
     * <p>Setting Aggregate를 영속화합니다. ID가 없으면 생성, 있으면 수정입니다.</p>
     *
     * <p><strong>규칙:</strong></p>
     * <ul>
     *   <li>✅ 트랜잭션 경계는 Application Layer(UseCase)에서 관리</li>
     *   <li>✅ Mapper를 통해 Domain → Entity 변환</li>
     *   <li>❌ 비즈니스 로직 없음 (순수 저장만)</li>
     * </ul>
     *
     * @param setting 저장할 Setting Aggregate (필수)
     * @return 저장된 Setting (DB 생성 ID 포함)
     * @throws IllegalArgumentException setting이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    Setting save(Setting setting);

    /**
     * Setting 여러 개 일괄 저장
     *
     * <p>여러 Setting을 Batch로 저장합니다. 성능 최적화를 위해 사용됩니다.</p>
     *
     * <p><strong>Batch 저장 규칙:</strong></p>
     * <ul>
     *   <li>✅ JPA Batch Insert 사용</li>
     *   <li>✅ 트랜잭션 경계는 Application Layer에서 관리</li>
     *   <li>⚠️ Batch Size는 `spring.jpa.properties.hibernate.jdbc.batch_size` 설정 참조</li>
     * </ul>
     *
     * @param settings 저장할 Setting 목록 (필수)
     * @return 저장된 Setting 목록 (DB 생성 ID 포함)
     * @throws IllegalArgumentException settings가 null이거나 비어있는 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    List<Setting> saveAll(List<Setting> settings);

    /**
     * Setting ID로 삭제
     *
     * <p>Setting을 ID로 삭제합니다.</p>
     *
     * <p><strong>삭제 규칙:</strong></p>
     * <ul>
     *   <li>✅ Hard Delete (물리적 삭제)</li>
     *   <li>✅ 존재하지 않는 ID도 에러 없이 처리 (Idempotent)</li>
     *   <li>❌ 비즈니스 검증 없음 (Application Layer에서 수행)</li>
     * </ul>
     *
     * @param id 삭제할 Setting ID (필수)
     * @throws IllegalArgumentException id가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    void deleteById(Long id);

    /**
     * Level과 Context로 Setting 삭제
     *
     * <p>특정 Level과 Context에 속한 모든 Setting을 삭제합니다.</p>
     *
     * <p><strong>사용 예시:</strong></p>
     * <ul>
     *   <li>ORG Level 설정 전체 삭제: `deleteByLevelAndContext(ORG, orgId)`</li>
     *   <li>TENANT Level 설정 전체 삭제: `deleteByLevelAndContext(TENANT, tenantId)`</li>
     *   <li>DEFAULT Level 설정 전체 삭제: `deleteByLevelAndContext(DEFAULT, null)`</li>
     * </ul>
     *
     * @param level 삭제할 Setting Level (필수)
     * @param contextId Context ID (ORG/TENANT는 필수, DEFAULT는 null)
     * @throws IllegalArgumentException level이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    void deleteByLevelAndContext(SettingLevel level, Long contextId);
}
