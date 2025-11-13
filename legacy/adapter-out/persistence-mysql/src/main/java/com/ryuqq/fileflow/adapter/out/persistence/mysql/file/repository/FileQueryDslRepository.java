package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity.FileAssetJpaEntity;
import com.ryuqq.fileflow.application.file.dto.query.FileMetadataQuery;
import com.ryuqq.fileflow.application.file.dto.query.ListFilesQuery;
import com.ryuqq.fileflow.domain.file.asset.FileStatus;
import com.ryuqq.fileflow.domain.file.asset.Visibility;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity.QFileAssetJpaEntity.fileAssetJpaEntity;

/**
 * FileQueryDslRepository - FileAsset QueryDSL 전용 Repository
 *
 * <p>QueryDSL을 사용한 동적 쿼리 실행 구현체입니다.</p>
 *
 * <p><strong>책임</strong>:</p>
 * <ul>
 *   <li>QueryDSL 기반 동적 쿼리 실행</li>
 *   <li>다중 필터 조합 지원</li>
 *   <li>성능 최적화된 조회 쿼리 제공</li>
 * </ul>
 *
 * <p><strong>설계 원칙</strong>:</p>
 * <ul>
 *   <li>✅ {@code @Component} 사용 (Spring Bean 등록)</li>
 *   <li>✅ 구현체만 존재 (인터페이스 추상화 불필요)</li>
 *   <li>✅ JPAQueryFactory를 통한 QueryDSL 쿼리 실행</li>
 *   <li>✅ Helper 메서드를 통한 동적 조건 생성</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ CQRS Query Side 전용 (조회만)</li>
 * </ul>
 *
 * <p><strong>성능 최적화</strong>:</p>
 * <ul>
 *   <li>인덱스 활용: idx_tenant_org_uploaded, idx_owner, idx_status</li>
 *   <li>QueryDSL 동적 쿼리: 다중 필터 조합 지원</li>
 *   <li>DB 레벨 페이징: offset/limit</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class FileQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * Constructor - JPAQueryFactory 주입
     *
     * @param queryFactory JPA queryFactory
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public FileQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 파일 메타데이터 단건 조회
     *
     * <p><strong>쿼리 조건</strong>:</p>
     * <ul>
     *   <li>fileId (필수)</li>
     *   <li>tenantId (보안 스코프 - 필수)</li>
     *   <li>organizationId (선택)</li>
     *   <li>deleted_at IS NULL (자동 필터)</li>
     * </ul>
     *
     * @param query 파일 메타데이터 조회 Query
     * @return FileAssetJpaEntity (Optional)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public Optional<FileAssetJpaEntity> findByQuery(FileMetadataQuery query) {
        if (query == null) {
            return Optional.empty();
        }

        Long fileId = query.fileId().value();
        Long tenantId = query.tenantId().value();
        Long organizationId = query.organizationId();

        FileAssetJpaEntity entity = queryFactory
            .selectFrom(fileAssetJpaEntity)
            .where(
                fileAssetJpaEntity.id.eq(fileId),
                eqTenantId(tenantId),
                eqOrganizationId(organizationId),
                isNotDeleted()
            )
            .fetchOne();

        return Optional.ofNullable(entity);
    }

    /**
     * 파일 목록 조회 (페이징 & 필터링)
     *
     * <p><strong>조회 조건</strong>:</p>
     * <ul>
     *   <li>tenantId (필수)</li>
     *   <li>organizationId (선택)</li>
     *   <li>ownerUserId (선택)</li>
     *   <li>status (선택)</li>
     *   <li>visibility (선택)</li>
     *   <li>uploadedAfter/uploadedBefore (선택)</li>
     * </ul>
     *
     * <p><strong>정렬</strong>: uploaded_at DESC (최근 업로드 순)</p>
     *
     * @param query 파일 목록 조회 Query
     * @return FileAssetJpaEntity 목록
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public List<FileAssetJpaEntity> findAllByQuery(ListFilesQuery query) {
        if (query == null) {
            return List.of();
        }

        return queryFactory
            .selectFrom(fileAssetJpaEntity)
            .where(
                eqTenantId(query.tenantId().value()),
                eqOrganizationId(query.organizationId()),
                eqOwnerUserId(query.ownerUserId()),
                eqStatus(query.status()),
                eqVisibility(query.visibility()),
                betweenUploadedAt(query.uploadedAfter(), query.uploadedBefore()),
                isNotDeleted()
            )
            .orderBy(fileAssetJpaEntity.uploadedAt.desc())
            .offset(query.offset())
            .limit(query.limit())
            .fetch();
    }

    /**
     * 파일 목록 전체 개수 조회
     *
     * <p>페이징 처리를 위한 전체 개수 조회</p>
     *
     * @param query 파일 목록 조회 Query
     * @return 전체 개수
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public long countByQuery(ListFilesQuery query) {
        if (query == null) {
            return 0;
        }

        Long count = queryFactory
            .select(fileAssetJpaEntity.count())
            .from(fileAssetJpaEntity)
            .where(
                eqTenantId(query.tenantId().value()),
                eqOrganizationId(query.organizationId()),
                eqOwnerUserId(query.ownerUserId()),
                eqStatus(query.status()),
                eqVisibility(query.visibility()),
                betweenUploadedAt(query.uploadedAfter(), query.uploadedBefore()),
                isNotDeleted()
            )
            .fetchOne();

        return count != null ? count : 0L;
    }

    /**
     * ID로 FileAsset 조회 (단순 조회)
     *
     * <p><strong>사용 시나리오</strong>:</p>
     * <ul>
     *   <li>파일 상세 정보 조회</li>
     *   <li>Pipeline 처리 시 FileAsset 조회</li>
     *   <li>보안 검증 없는 내부 조회</li>
     * </ul>
     *
     * @param id FileAsset ID
     * @return FileAssetJpaEntity (Optional)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public Optional<FileAssetJpaEntity> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        FileAssetJpaEntity entity = queryFactory
            .selectFrom(fileAssetJpaEntity)
            .where(
                fileAssetJpaEntity.id.eq(id),
                isNotDeleted()
            )
            .fetchOne();

        return Optional.ofNullable(entity);
    }

    /**
     * Upload Session ID로 파일 조회
     *
     * @param uploadSessionId Upload Session ID
     * @return FileAssetJpaEntity (Optional)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public Optional<FileAssetJpaEntity> findByUploadSessionId(Long uploadSessionId) {
        if (uploadSessionId == null) {
            return Optional.empty();
        }

        FileAssetJpaEntity entity = queryFactory
            .selectFrom(fileAssetJpaEntity)
            .where(
                fileAssetJpaEntity.uploadSessionId.eq(uploadSessionId),
                isNotDeleted()
            )
            .fetchOne();

        return Optional.ofNullable(entity);
    }

    // ========================================
    // Private Helper Methods (동적 쿼리 조건)
    // ========================================

    /**
     * Tenant ID 일치 조건
     *
     * @param tenantId Tenant ID (null이면 조건 제외)
     * @return BooleanExpression
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    private BooleanExpression eqTenantId(Long tenantId) {
        if (tenantId == null || tenantId <= 0) {
            return null;
        }
        return fileAssetJpaEntity.tenantId.eq(tenantId);
    }

    /**
     * Organization ID 일치 조건
     *
     * @param organizationId Organization ID (null이면 조건 제외)
     * @return BooleanExpression
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    private BooleanExpression eqOrganizationId(Long organizationId) {
        if (organizationId == null || organizationId <= 0) {
            return null;
        }
        return fileAssetJpaEntity.organizationId.eq(organizationId);
    }

    /**
     * Owner User ID 일치 조건
     *
     * @param ownerUserId Owner User ID (null이면 조건 제외)
     * @return BooleanExpression
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    private BooleanExpression eqOwnerUserId(Long ownerUserId) {
        if (ownerUserId == null || ownerUserId <= 0) {
            return null;
        }
        return fileAssetJpaEntity.ownerUserId.eq(ownerUserId);
    }

    /**
     * FileStatus 일치 조건
     *
     * @param status FileStatus (null이면 조건 제외)
     * @return BooleanExpression
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    private BooleanExpression eqStatus(FileStatus status) {
        if (status == null) {
            return null;
        }
        return fileAssetJpaEntity.status.eq(status);
    }

    /**
     * FileVisibility 일치 조건
     *
     * @param visibility FileVisibility (null이면 조건 제외)
     * @return BooleanExpression
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    private BooleanExpression eqVisibility(Visibility visibility) {
        if (visibility == null) {
            return null;
        }
        return fileAssetJpaEntity.visibility.eq(visibility);
    }

    /**
     * Uploaded At 범위 조건 (Between)
     *
     * @param uploadedAfter 시작 시간 (null이면 조건 제외)
     * @param uploadedBefore 종료 시간 (null이면 조건 제외)
     * @return BooleanExpression
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    private BooleanExpression betweenUploadedAt(LocalDateTime uploadedAfter, LocalDateTime uploadedBefore) {
        if (uploadedAfter == null && uploadedBefore == null) {
            return null;
        }

        if (uploadedAfter != null && uploadedBefore != null) {
            return fileAssetJpaEntity.uploadedAt.between(uploadedAfter, uploadedBefore);
        } else if (uploadedAfter != null) {
            return fileAssetJpaEntity.uploadedAt.goe(uploadedAfter);
        } else {
            return fileAssetJpaEntity.uploadedAt.loe(uploadedBefore);
        }
    }

    /**
     * Soft Delete 필터 조건 (deleted_at IS NULL)
     *
     * @return BooleanExpression
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    private BooleanExpression isNotDeleted() {
        return fileAssetJpaEntity.deletedAt.isNull();
    }
}
