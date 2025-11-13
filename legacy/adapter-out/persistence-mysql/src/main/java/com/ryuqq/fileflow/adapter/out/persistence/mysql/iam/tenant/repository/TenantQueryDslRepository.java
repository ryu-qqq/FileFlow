package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.tenant.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.tenant.entity.TenantJpaEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.tenant.entity.QTenantJpaEntity.tenantJpaEntity;

/**
 * TenantQueryDslRepository - Tenant QueryDSL 전용 Repository
 *
 * <p>QueryDSL JPAQueryFactory를 사용하여 동적 쿼리를 실행합니다.</p>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ {@code @Component} 사용 (Spring Bean 등록)</li>
 *   <li>✅ 구현체만 존재 (인터페이스 추상화 불필요)</li>
 *   <li>✅ JPAQueryFactory를 통한 QueryDSL 쿼리 실행</li>
 *   <li>✅ BooleanExpression Helper 메서드로 동적 쿼리 조건 관리</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ CQRS Query Side 전용 (조회만)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-11
 */
@Component
public class TenantQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * Constructor - JPAQueryFactory 생성
     *
     * @param queryFactory JPA EntityManager
     * @author ryu-qqq
     * @since 2025-11-11
     */
    public TenantQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * Tenant ID로 단건 조회
     *
     * @param tenantId Tenant ID
     * @return Optional<TenantJpaEntity>
     * @author ryu-qqq
     * @since 2025-11-11
     */
    public Optional<TenantJpaEntity> findById(Long tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("TenantId는 필수입니다");
        }

        TenantJpaEntity entity = queryFactory
            .selectFrom(tenantJpaEntity)
            .where(
                tenantJpaEntity.id.eq(tenantId),
                tenantJpaEntity.deleted.isFalse()
            )
            .fetchOne();

        return Optional.ofNullable(entity);
    }

    /**
     * Tenant 목록 조회 (Offset-based Pagination)
     *
     * @param nameContains 이름 검색어 (부분 일치, null 허용)
     * @param deleted 삭제 여부 필터 (null이면 전체 조회)
     * @param offset 시작 위치 (0부터 시작)
     * @param limit 조회 개수
     * @return TenantJpaEntity 목록
     * @author ryu-qqq
     * @since 2025-11-11
     */
    public List<TenantJpaEntity> findAllWithOffset(
        String nameContains,
        Boolean deleted,
        int offset,
        int limit
    ) {
        return queryFactory
            .selectFrom(tenantJpaEntity)
            .where(
                containsName(nameContains),
                eqDeleted(deleted)
            )
            .orderBy(tenantJpaEntity.createdAt.asc())
            .offset(offset)
            .limit(limit)
            .fetch();
    }

    /**
     * Tenant 목록 총 개수 조회
     *
     * @param nameContains 이름 검색어 (부분 일치, null 허용)
     * @param deleted 삭제 여부 필터 (null이면 전체 조회)
     * @return 전체 개수
     * @author ryu-qqq
     * @since 2025-11-11
     */
    public long countAll(String nameContains, Boolean deleted) {
        Long count = queryFactory
            .select(tenantJpaEntity.count())
            .from(tenantJpaEntity)
            .where(
                containsName(nameContains),
                eqDeleted(deleted)
            )
            .fetchOne();

        return count != null ? count : 0L;
    }

    /**
     * Tenant 목록 조회 (Cursor-based Pagination)
     *
     * <p>Cursor는 Base64로 인코딩된 "createdAt|id" 복합 키입니다.
     * UUID는 순차적이지 않으므로 createdAt + id 복합 정렬을 사용하여 일관된 순서를 보장합니다.</p>
     *
     * @param nameContains 이름 검색어 (부분 일치, null 허용)
     * @param deleted 삭제 여부 필터 (null이면 전체 조회)
     * @param cursor 커서 값 (null이면 처음부터 조회)
     * @param limit 조회 개수
     * @return TenantJpaEntity 목록 (limit + 1개까지 조회)
     * @author ryu-qqq
     * @since 2025-11-11
     */
    public List<TenantJpaEntity> findAllWithCursor(
        String nameContains,
        Boolean deleted,
        String cursor,
        int limit
    ) {
        return queryFactory
            .selectFrom(tenantJpaEntity)
            .where(
                containsName(nameContains),
                eqDeleted(deleted),
                gtCursor(cursor)
            )
            .orderBy(
                tenantJpaEntity.createdAt.asc(),
                tenantJpaEntity.id.asc()
            )
            .limit(limit)
            .fetch();
    }

    // ========================================
    // Private Helper Methods (동적 쿼리 조건)
    // ========================================

    /**
     * 이름 부분 일치 조건
     *
     * @param nameContains 검색어 (null이면 조건 제외)
     * @return BooleanExpression
     * @author ryu-qqq
     * @since 2025-11-11
     */
    private BooleanExpression containsName(String nameContains) {
        if (nameContains == null || nameContains.isBlank()) {
            return null;
        }
        return tenantJpaEntity.name.containsIgnoreCase(nameContains);
    }

    /**
     * 삭제 여부 일치 조건
     *
     * @param deleted 삭제 여부 (null이면 조건 제외)
     * @return BooleanExpression
     * @author ryu-qqq
     * @since 2025-11-11
     */
    private BooleanExpression eqDeleted(Boolean deleted) {
        if (deleted == null) {
            return null;
        }
        return tenantJpaEntity.deleted.eq(deleted);
    }

    /**
     * Cursor보다 큰 (createdAt, id) 복합 조건 (Cursor-based Pagination)
     *
     * <p>UUID는 순차적이지 않으므로 createdAt + id 복합 정렬을 사용합니다.
     * Cursor 형식: Base64("createdAt|id")</p>
     *
     * <p>SQL WHERE 조건: {@code (createdAt, id) > (cursor_createdAt, cursor_id)}</p>
     *
     * @param cursor Base64 인코딩된 "createdAt|id" (null이면 조건 제외)
     * @return BooleanExpression
     * @author ryu-qqq
     * @since 2025-11-11
     */
    private BooleanExpression gtCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        try {
            String decodedCursor = new String(Base64.getUrlDecoder().decode(cursor));
            String[] parts = decodedCursor.split("\\|");

            if (parts.length != 2) {
                // 잘못된 cursor 형식은 무시
                return null;
            }

            String createdAtStr = parts[0];
            Long id = Long.parseLong(parts[1]);

            // (createdAt, id) > (cursor_createdAt, cursor_id) 복합 비교
            // createdAt이 더 크거나, 같으면서 id가 더 큰 경우
            return tenantJpaEntity.createdAt.gt(java.time.LocalDateTime.parse(createdAtStr))
                .or(
                    tenantJpaEntity.createdAt.eq(java.time.LocalDateTime.parse(createdAtStr))
                        .and(tenantJpaEntity.id.gt(id))
                );
        } catch (Exception e) {
            // 잘못된 cursor는 무시하고 처음부터 조회
            return null;
        }
    }
}
