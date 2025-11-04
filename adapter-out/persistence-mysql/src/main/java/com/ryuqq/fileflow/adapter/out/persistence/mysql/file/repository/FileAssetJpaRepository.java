package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity.FileAssetJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.querydsl.FileAssetQueryDslRepository;
import com.ryuqq.fileflow.domain.file.asset.FileStatus;
import com.ryuqq.fileflow.domain.file.asset.Visibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * FileAsset JPA Repository
 *
 * <p>CQRS Query Side 최적화된 Repository</p>
 *
 * <p><strong>책임</strong>:</p>
 * <ul>
 *   <li>FileAsset 조회 전용 쿼리 (CQRS Query Side)</li>
 *   <li>Spring Data JPA 기본 CRUD 메서드</li>
 *   <li>단건 조회 커스텀 메서드 (ID 기반)</li>
 *   <li>QueryDSL 동적 쿼리 (FileAssetQueryDslRepository)</li>
 * </ul>
 *
 * <p><strong>성능 최적화</strong>:</p>
 * <ul>
 *   <li>인덱스 활용: idx_tenant_org_uploaded, idx_owner, idx_status</li>
 *   <li>QueryDSL: 동적 쿼리 및 다중 필터 조합</li>
 *   <li>DB 레벨 페이징: offset/limit</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Repository
public interface FileAssetJpaRepository extends JpaRepository<FileAssetJpaEntity, Long>, FileAssetQueryDslRepository {

    /**
     * 파일 단건 조회 (Soft Delete 필터링)
     *
     * <p><strong>쿼리 조건</strong>:</p>
     * <ul>
     *   <li>id = :fileId</li>
     *   <li>tenantId = :tenantId (보안 스코프)</li>
     *   <li>deletedAt IS NULL (Soft Delete 필터)</li>
     * </ul>
     *
     * @param fileId File ID
     * @param tenantId Tenant ID
     * @return FileAssetJpaEntity (Optional)
     */
    @Query("""
        SELECT f
        FROM FileAssetJpaEntity f
        WHERE f.id = :fileId
          AND f.tenantId = :tenantId
          AND f.deletedAt IS NULL
        """)
    Optional<FileAssetJpaEntity> findByIdAndTenantId(
        @Param("fileId") Long fileId,
        @Param("tenantId") Long tenantId
    );

    /**
     * 파일 단건 조회 (Organization 포함, Soft Delete 필터링)
     *
     * @param fileId File ID
     * @param tenantId Tenant ID
     * @param organizationId Organization ID
     * @return FileAssetJpaEntity (Optional)
     */
    @Query("""
        SELECT f
        FROM FileAssetJpaEntity f
        WHERE f.id = :fileId
          AND f.tenantId = :tenantId
          AND f.organizationId = :organizationId
          AND f.deletedAt IS NULL
        """)
    Optional<FileAssetJpaEntity> findByIdAndTenantIdAndOrganizationId(
        @Param("fileId") Long fileId,
        @Param("tenantId") Long tenantId,
        @Param("organizationId") Long organizationId
    );

    /**
     * 파일 목록 조회 (Tenant 스코프, Soft Delete 필터링)
     *
     * <p><strong>정렬</strong>: uploaded_at DESC (최근 업로드 순)</p>
     *
     * @param tenantId Tenant ID
     * @return List of FileAssetJpaEntity
     */
    @Query("""
        SELECT f
        FROM FileAssetJpaEntity f
        WHERE f.tenantId = :tenantId
          AND f.deletedAt IS NULL
        ORDER BY f.uploadedAt DESC
        """)
    List<FileAssetJpaEntity> findAllByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 파일 목록 조회 (Organization 포함)
     *
     * @param tenantId Tenant ID
     * @param organizationId Organization ID
     * @return List of FileAssetJpaEntity
     */
    @Query("""
        SELECT f
        FROM FileAssetJpaEntity f
        WHERE f.tenantId = :tenantId
          AND f.organizationId = :organizationId
          AND f.deletedAt IS NULL
        ORDER BY f.uploadedAt DESC
        """)
    List<FileAssetJpaEntity> findAllByTenantIdAndOrganizationId(
        @Param("tenantId") Long tenantId,
        @Param("organizationId") Long organizationId
    );

    /**
     * 소유자별 파일 목록 조회
     *
     * @param tenantId Tenant ID
     * @param ownerUserId Owner User ID
     * @return List of FileAssetJpaEntity
     */
    @Query("""
        SELECT f
        FROM FileAssetJpaEntity f
        WHERE f.tenantId = :tenantId
          AND f.ownerUserId = :ownerUserId
          AND f.deletedAt IS NULL
        ORDER BY f.uploadedAt DESC
        """)
    List<FileAssetJpaEntity> findAllByTenantIdAndOwnerUserId(
        @Param("tenantId") Long tenantId,
        @Param("ownerUserId") Long ownerUserId
    );

    /**
     * 상태별 파일 목록 조회
     *
     * @param tenantId Tenant ID
     * @param status File Status
     * @return List of FileAssetJpaEntity
     */
    @Query("""
        SELECT f
        FROM FileAssetJpaEntity f
        WHERE f.tenantId = :tenantId
          AND f.status = :status
          AND f.deletedAt IS NULL
        ORDER BY f.uploadedAt DESC
        """)
    List<FileAssetJpaEntity> findAllByTenantIdAndStatus(
        @Param("tenantId") Long tenantId,
        @Param("status") FileStatus status
    );

    /**
     * 가시성별 파일 목록 조회
     *
     * @param tenantId Tenant ID
     * @param visibility Visibility
     * @return List of FileAssetJpaEntity
     */
    @Query("""
        SELECT f
        FROM FileAssetJpaEntity f
        WHERE f.tenantId = :tenantId
          AND f.visibility = :visibility
          AND f.deletedAt IS NULL
        ORDER BY f.uploadedAt DESC
        """)
    List<FileAssetJpaEntity> findAllByTenantIdAndVisibility(
        @Param("tenantId") Long tenantId,
        @Param("visibility") Visibility visibility
    );

    /**
     * 기간별 파일 목록 조회
     *
     * @param tenantId Tenant ID
     * @param uploadedAfter Uploaded After (inclusive)
     * @param uploadedBefore Uploaded Before (exclusive)
     * @return List of FileAssetJpaEntity
     */
    @Query("""
        SELECT f
        FROM FileAssetJpaEntity f
        WHERE f.tenantId = :tenantId
          AND f.uploadedAt >= :uploadedAfter
          AND f.uploadedAt < :uploadedBefore
          AND f.deletedAt IS NULL
        ORDER BY f.uploadedAt DESC
        """)
    List<FileAssetJpaEntity> findAllByTenantIdAndUploadedAtBetween(
        @Param("tenantId") Long tenantId,
        @Param("uploadedAfter") LocalDateTime uploadedAfter,
        @Param("uploadedBefore") LocalDateTime uploadedBefore
    );

    /**
     * 파일 개수 조회 (Tenant 스코프)
     *
     * @param tenantId Tenant ID
     * @return Count
     */
    @Query("""
        SELECT COUNT(f)
        FROM FileAssetJpaEntity f
        WHERE f.tenantId = :tenantId
          AND f.deletedAt IS NULL
        """)
    long countByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 파일 개수 조회 (Organization 포함)
     *
     * @param tenantId Tenant ID
     * @param organizationId Organization ID
     * @return Count
     */
    @Query("""
        SELECT COUNT(f)
        FROM FileAssetJpaEntity f
        WHERE f.tenantId = :tenantId
          AND f.organizationId = :organizationId
          AND f.deletedAt IS NULL
        """)
    long countByTenantIdAndOrganizationId(
        @Param("tenantId") Long tenantId,
        @Param("organizationId") Long organizationId
    );

    /**
     * Upload Session ID로 파일 조회
     *
     * @param uploadSessionId Upload Session ID
     * @return FileAssetJpaEntity (Optional)
     */
    @Query("""
        SELECT f
        FROM FileAssetJpaEntity f
        WHERE f.uploadSessionId = :uploadSessionId
          AND f.deletedAt IS NULL
        """)
    Optional<FileAssetJpaEntity> findByUploadSessionId(@Param("uploadSessionId") Long uploadSessionId);
}
