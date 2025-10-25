package com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.SettingJpaRepository;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.entity.SettingJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.mapper.SettingEntityMapper;
import com.ryuqq.fileflow.application.settings.port.out.SaveSettingPort;
import com.ryuqq.fileflow.domain.settings.Setting;
import com.ryuqq.fileflow.domain.settings.SettingLevel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Setting Command Adapter - CQRS Command 측 Persistence Adapter
 *
 * <p>헥사고날 아키텍처 + CQRS 패턴: {@link SaveSettingPort} 구현체입니다.</p>
 * <p>Setting 변경(Command) 작업만 담당합니다.</p>
 *
 * <p><strong>CQRS 원칙:</strong></p>
 * <ul>
 *   <li>✅ Command 작업만 구현 (save*, delete*)</li>
 *   <li>✅ Query 작업은 {@link SettingQueryAdapter}에서 분리</li>
 *   <li>✅ Write 작업 (데이터 변경)</li>
 * </ul>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Setting 저장 (생성/수정)</li>
 *   <li>Setting 삭제 (단건/목록)</li>
 *   <li>Domain → Entity 변환 (Mapper 위임)</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ {@code @Component} 사용 (Spring Bean 등록)</li>
 *   <li>✅ {@code SaveSettingPort} 구현</li>
 *   <li>✅ JPA Repository 사용 (Spring Data JPA)</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ Law of Demeter 준수 (Mapper를 통한 변환)</li>
 *   <li>❌ {@code @Transactional} 사용 금지 (Application Layer에서만)</li>
 *   <li>❌ Read 작업 절대 금지 (find*, load* 등)</li>
 * </ul>
 *
 * <p><strong>트랜잭션 관리:</strong></p>
 * <ul>
 *   <li>✅ Application Layer (UseCase)에서 {@code @Transactional} 관리</li>
 *   <li>❌ Persistence Adapter에 {@code @Transactional} 절대 금지</li>
 *   <li>⚠️ 트랜잭션 경계는 Application Layer가 책임</li>
 * </ul>
 *
 * <p><strong>성능 최적화:</strong></p>
 * <ul>
 *   <li>✅ Batch Insert/Update 지원 (saveAll)</li>
 *   <li>✅ Bulk Delete 지원 (deleteByLevelAndContext)</li>
 *   <li>✅ JPA Batch Size 설정 참조</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
@Component
public class SettingCommandAdapter implements SaveSettingPort {

    private final SettingJpaRepository jpaRepository;

    /**
     * Constructor - 의존성 주입
     *
     * @param jpaRepository Setting JPA Repository
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public SettingCommandAdapter(SettingJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /**
     * Setting 저장 (생성 또는 수정)
     *
     * <p>Setting Aggregate를 영속화합니다. ID가 없으면 생성, 있으면 수정입니다.</p>
     *
     * <p><strong>저장 로직:</strong></p>
     * <ul>
     *   <li>ID가 null이면 신규 생성 (SettingEntityMapper.toEntity)</li>
     *   <li>ID가 있으면 업데이트 (SettingEntityMapper.toEntityForUpdate)</li>
     * </ul>
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
     * Setting 여러 개 일괄 저장
     *
     * <p>여러 Setting을 Batch로 저장합니다. 성능 최적화를 위해 사용됩니다.</p>
     *
     * <p><strong>Batch 저장 규칙:</strong></p>
     * <ul>
     *   <li>✅ JPA Batch Insert 사용</li>
     *   <li>✅ 트랜잭션 경계는 Application Layer에서 관리</li>
     *   <li>⚠️ Batch Size는 {@code spring.jpa.properties.hibernate.jdbc.batch_size} 설정 참조</li>
     * </ul>
     *
     * <p><strong>성능 최적화:</strong></p>
     * <ul>
     *   <li>단건 저장 대비 N배 빠름 (N = settings.size())</li>
     *   <li>DB Round Trip 최소화</li>
     * </ul>
     *
     * @param settings 저장할 Setting 목록 (필수)
     * @return 저장된 Setting 목록 (DB 생성 ID 포함)
     * @throws IllegalArgumentException settings가 null이거나 비어있는 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public List<Setting> saveAll(List<Setting> settings) {
        if (settings == null || settings.isEmpty()) {
            throw new IllegalArgumentException("Settings는 필수이며 비어있을 수 없습니다");
        }

        // Domain → Entity 변환 (Batch)
        List<SettingJpaEntity> entities = settings.stream()
            .map(setting -> (setting.getId() == null)
                ? SettingEntityMapper.toEntity(setting)
                : SettingEntityMapper.toEntityForUpdate(setting))
            .collect(Collectors.toList());

        // JPA Batch 저장
        List<SettingJpaEntity> savedEntities = jpaRepository.saveAll(entities);

        // Entity → Domain 변환 (Batch)
        return savedEntities.stream()
            .map(SettingEntityMapper::toDomain)
            .collect(Collectors.toUnmodifiableList());
    }

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
     * <p><strong>Idempotent (멱등성):</strong></p>
     * <ul>
     *   <li>동일한 ID로 여러 번 호출해도 결과가 동일</li>
     *   <li>이미 삭제된 ID를 다시 삭제해도 에러 없음</li>
     * </ul>
     *
     * @param id 삭제할 Setting ID (필수)
     * @throws IllegalArgumentException id가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Setting ID는 필수입니다");
        }

        // JPA deleteById는 존재하지 않아도 에러 없음 (Idempotent)
        jpaRepository.deleteById(id);
    }

    /**
     * Level과 Context로 Setting 삭제
     *
     * <p>특정 Level과 Context에 속한 모든 Setting을 삭제합니다.</p>
     *
     * <p><strong>사용 예시:</strong></p>
     * <ul>
     *   <li>ORG Level 설정 전체 삭제: {@code deleteByLevelAndContext(ORG, orgId)}</li>
     *   <li>TENANT Level 설정 전체 삭제: {@code deleteByLevelAndContext(TENANT, tenantId)}</li>
     *   <li>DEFAULT Level 설정 전체 삭제: {@code deleteByLevelAndContext(DEFAULT, null)}</li>
     * </ul>
     *
     * <p><strong>삭제 전략:</strong></p>
     * <ul>
     *   <li>✅ Bulk Delete 사용 (성능 최적화)</li>
     *   <li>✅ 트랜잭션 경계는 Application Layer에서 관리</li>
     *   <li>⚠️ 삭제 전 조회 필요 (JPA Repository 제약)</li>
     * </ul>
     *
     * <p><strong>주의사항:</strong></p>
     * <ul>
     *   <li>⚠️ DEFAULT Level 삭제 시 전체 기본값이 삭제됨</li>
     *   <li>⚠️ Application Layer에서 삭제 전 검증 필요</li>
     * </ul>
     *
     * @param level 삭제할 Setting Level (필수)
     * @param contextId Context ID (ORG/TENANT는 필수, DEFAULT는 null)
     * @throws IllegalArgumentException level이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public void deleteByLevelAndContext(SettingLevel level, Long contextId) {
        if (level == null) {
            throw new IllegalArgumentException("SettingLevel은 필수입니다");
        }

        // JPA Repository 제약: 삭제 전 조회 필요
        List<SettingJpaEntity> entities = jpaRepository.findAllByLevelAndContextId(level, contextId);

        // Bulk Delete
        if (!entities.isEmpty()) {
            jpaRepository.deleteAll(entities);
        }
    }
}
