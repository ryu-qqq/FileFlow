package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity.FileAssetJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * FileAsset Spring Data JPA Repository
 *
 * <p><strong>역할</strong>: FileAsset Entity에 대한 기본 CRUD 메서드 제공 (CQRS Command Side)</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/file/repository/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ Spring Data JPA 인터페이스 (구현체 자동 생성)</li>
 *   <li>✅ CQRS Command Side 전용 (기본 CRUD만 제공)</li>
 *   <li>✅ Long FK 전략 (tenantId, organizationId는 Long)</li>
 *   <li>✅ 소프트 삭제 고려 (deletedAt IS NULL 조건 추가)</li>
 *   <li>✅ 메서드 네이밍 규칙 준수 (Spring Data JPA Query Methods)</li>
 *   <li>❌ {@code @Repository} 어노테이션 불필요 (JpaRepository 상속 시 자동)</li>
 *   <li>❌ {@code @Query} 어노테이션 사용하지 않음 (메서드 명명 규칙으로 대체)</li>
 * </ul>
 *
 * <h3>CQRS 분리 전략</h3>
 * <ul>
 *   <li><strong>Command Side</strong> (이 Repository): {@code save()}, {@code delete()} 등 CUD 작업</li>
 *   <li><strong>Query Side</strong>: {@code FileAssetQueryDslRepository} (복잡한 조회 쿼리)</li>
 * </ul>
 *
 * <h3>Query Side 이동 메서드</h3>
 * <p>다음 조회 메서드들은 {@code FileAssetQueryDslRepository}로 이동되었습니다:</p>
 * <ul>
 *   <li>{@code findByIdAndTenantId()} → QueryDSL 동적 쿼리</li>
 *   <li>{@code findByIdAndTenantIdAndOrganizationId()} → QueryDSL 동적 쿼리</li>
 *   <li>{@code findAllByTenantId()} → QueryDSL 동적 쿼리</li>
 *   <li>{@code findAllByTenantIdAndOrganizationId()} → QueryDSL 동적 쿼리</li>
 *   <li>{@code findAllByTenantIdAndOwnerUserId()} → QueryDSL 동적 쿼리</li>
 *   <li>{@code findAllByTenantIdAndStatus()} → QueryDSL 동적 쿼리</li>
 *   <li>{@code findAllByTenantIdAndVisibility()} → QueryDSL 동적 쿼리</li>
 *   <li>{@code findAllByTenantIdAndUploadedAtBetween()} → QueryDSL 동적 쿼리</li>
 *   <li>{@code countByTenantId()} → QueryDSL 동적 쿼리</li>
 *   <li>{@code countByTenantIdAndOrganizationId()} → QueryDSL 동적 쿼리</li>
 *   <li>{@code findByUploadSessionId()} → QueryDSL 동적 쿼리</li>
 * </ul>
 *
 * @since 1.0.0
 */
public interface FileAssetJpaRepository extends JpaRepository<FileAssetJpaEntity, Long> {

    /**
     * ID로 활성 FileAsset 조회
     *
     * <p>소프트 삭제되지 않은 FileAsset만 조회합니다.</p>
     *
     * @param id FileAsset ID
     * @return FileAsset Entity (삭제되었거나 존재하지 않으면 {@code Optional.empty()})
     */
    Optional<FileAssetJpaEntity> findByIdAndDeletedAtIsNull(Long id);

    /**
     * Upload Session ID로 활성 FileAsset 조회
     *
     * <p>특정 업로드 세션에 연결된 활성 FileAsset을 조회합니다.
     * Upload Session ID는 업로드 프로세스 추적에 사용됩니다.</p>
     *
     * @param uploadSessionId Upload Session ID
     * @return FileAsset Entity (삭제되었거나 존재하지 않으면 {@code Optional.empty()})
     */
    Optional<FileAssetJpaEntity> findByUploadSessionIdAndDeletedAtIsNull(Long uploadSessionId);

    /**
     * Upload Session ID 존재 여부 확인 (활성 FileAsset 기준)
     *
     * <p>특정 업로드 세션에 연결된 활성 FileAsset이 존재하는지 확인합니다.
     * 소프트 삭제된 FileAsset은 제외됩니다.</p>
     *
     * @param uploadSessionId Upload Session ID
     * @return 존재하면 {@code true}, 없으면 {@code false}
     */
    boolean existsByUploadSessionIdAndDeletedAtIsNull(Long uploadSessionId);
}
