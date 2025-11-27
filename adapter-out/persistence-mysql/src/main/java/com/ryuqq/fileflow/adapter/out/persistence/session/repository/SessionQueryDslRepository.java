package com.ryuqq.fileflow.adapter.out.persistence.session.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.MultipartUploadSessionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.QCompletedPartJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.QMultipartUploadSessionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.QSingleUploadSessionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.SingleUploadSessionJpaEntity;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Session QueryDSL Repository.
 *
 * <p>Upload Session 조회를 담당하는 QueryDSL Repository입니다.
 */
@Repository
public class SessionQueryDslRepository {

    private static final QSingleUploadSessionJpaEntity single =
            QSingleUploadSessionJpaEntity.singleUploadSessionJpaEntity;
    private static final QMultipartUploadSessionJpaEntity multipart =
            QMultipartUploadSessionJpaEntity.multipartUploadSessionJpaEntity;
    private static final QCompletedPartJpaEntity part =
            QCompletedPartJpaEntity.completedPartJpaEntity;

    private final JPAQueryFactory queryFactory;

    public SessionQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * Single Upload Session을 ID로 조회한다.
     *
     * @param sessionId 세션 ID
     * @return SingleUploadSessionJpaEntity Optional
     */
    public Optional<SingleUploadSessionJpaEntity> findSingleUploadById(String sessionId) {
        SingleUploadSessionJpaEntity result =
                queryFactory.selectFrom(single).where(single.id.eq(sessionId)).fetchOne();
        return Optional.ofNullable(result);
    }

    /**
     * Single Upload Session을 IdempotencyKey로 조회한다.
     *
     * @param idempotencyKey Idempotency Key
     * @return SingleUploadSessionJpaEntity Optional
     */
    public Optional<SingleUploadSessionJpaEntity> findSingleUploadByIdempotencyKey(
            String idempotencyKey) {
        SingleUploadSessionJpaEntity result =
                queryFactory
                        .selectFrom(single)
                        .where(single.idempotencyKey.eq(idempotencyKey))
                        .fetchOne();
        return Optional.ofNullable(result);
    }

    /**
     * Multipart Upload Session을 ID로 조회한다.
     *
     * @param sessionId 세션 ID
     * @return MultipartUploadSessionJpaEntity Optional
     */
    public Optional<MultipartUploadSessionJpaEntity> findMultipartUploadById(String sessionId) {
        MultipartUploadSessionJpaEntity result =
                queryFactory.selectFrom(multipart).where(multipart.id.eq(sessionId)).fetchOne();
        return Optional.ofNullable(result);
    }

    /**
     * 만료 시간이 지난 Single Upload Session 목록을 조회한다.
     *
     * <p>PREPARING 또는 ACTIVE 상태이면서 expiresAt이 지정된 시간 이전인 세션을 조회한다.
     *
     * @param expiredBefore 이 시간 이전에 만료된 세션을 조회
     * @param limit 최대 조회 개수
     * @return 만료된 세션 목록
     */
    public List<SingleUploadSessionJpaEntity> findExpiredSingleUploads(
            Instant expiredBefore, int limit) {
        LocalDateTime expiredBeforeLocal =
                LocalDateTime.ofInstant(expiredBefore, ZoneId.systemDefault());

        return queryFactory
                .selectFrom(single)
                .where(
                        single.expiresAt.before(expiredBeforeLocal),
                        single.status.in(SessionStatus.PREPARING, SessionStatus.ACTIVE))
                .limit(limit)
                .fetch();
    }

    /**
     * 만료 시간이 지난 Multipart Upload Session 목록을 조회한다.
     *
     * <p>PREPARING 또는 ACTIVE 상태이면서 expiresAt이 지정된 시간 이전인 세션을 조회한다.
     *
     * @param expiredBefore 이 시간 이전에 만료된 세션을 조회
     * @param limit 최대 조회 개수
     * @return 만료된 세션 목록
     */
    public List<MultipartUploadSessionJpaEntity> findExpiredMultipartUploads(
            Instant expiredBefore, int limit) {
        LocalDateTime expiredBeforeLocal =
                LocalDateTime.ofInstant(expiredBefore, ZoneId.systemDefault());

        return queryFactory
                .selectFrom(multipart)
                .where(
                        multipart.expiresAt.before(expiredBeforeLocal),
                        multipart.status.in(SessionStatus.PREPARING, SessionStatus.ACTIVE))
                .limit(limit)
                .fetch();
    }

    /**
     * Single Upload Session을 ID와 tenantId로 조회한다.
     *
     * @param sessionId 세션 ID
     * @param tenantId 테넌트 ID
     * @return SingleUploadSessionJpaEntity Optional
     */
    public Optional<SingleUploadSessionJpaEntity> findSingleUploadByIdAndTenantId(
            String sessionId, Long tenantId) {
        SingleUploadSessionJpaEntity result =
                queryFactory
                        .selectFrom(single)
                        .where(single.id.eq(sessionId), single.tenantId.eq(tenantId))
                        .fetchOne();
        return Optional.ofNullable(result);
    }

    /**
     * Multipart Upload Session을 ID와 tenantId로 조회한다.
     *
     * @param sessionId 세션 ID
     * @param tenantId 테넌트 ID
     * @return MultipartUploadSessionJpaEntity Optional
     */
    public Optional<MultipartUploadSessionJpaEntity> findMultipartUploadByIdAndTenantId(
            String sessionId, Long tenantId) {
        MultipartUploadSessionJpaEntity result =
                queryFactory
                        .selectFrom(multipart)
                        .where(multipart.id.eq(sessionId), multipart.tenantId.eq(tenantId))
                        .fetchOne();
        return Optional.ofNullable(result);
    }

    /**
     * Single Upload Session을 검색 조건으로 조회한다.
     *
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID (nullable)
     * @param status 상태 (nullable)
     * @param offset 오프셋
     * @param limit 조회 개수
     * @return SingleUploadSessionJpaEntity 목록
     */
    public List<SingleUploadSessionJpaEntity> findSingleUploadsByCriteria(
            Long tenantId, Long organizationId, SessionStatus status, long offset, int limit) {
        return queryFactory
                .selectFrom(single)
                .where(
                        single.tenantId.eq(tenantId),
                        eqOrganizationId(single.organizationId, organizationId),
                        eqStatus(single.status, status))
                .orderBy(single.createdAt.desc())
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    /**
     * Multipart Upload Session을 검색 조건으로 조회한다.
     *
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID (nullable)
     * @param status 상태 (nullable)
     * @param offset 오프셋
     * @param limit 조회 개수
     * @return MultipartUploadSessionJpaEntity 목록
     */
    public List<MultipartUploadSessionJpaEntity> findMultipartUploadsByCriteria(
            Long tenantId, Long organizationId, SessionStatus status, long offset, int limit) {
        return queryFactory
                .selectFrom(multipart)
                .where(
                        multipart.tenantId.eq(tenantId),
                        eqOrganizationId(multipart.organizationId, organizationId),
                        eqStatus(multipart.status, status))
                .orderBy(multipart.createdAt.desc())
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    /**
     * Single Upload Session 개수를 검색 조건으로 조회한다.
     *
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID (nullable)
     * @param status 상태 (nullable)
     * @return 개수
     */
    public long countSingleUploadsByCriteria(
            Long tenantId, Long organizationId, SessionStatus status) {
        Long count =
                queryFactory
                        .select(single.count())
                        .from(single)
                        .where(
                                single.tenantId.eq(tenantId),
                                eqOrganizationId(single.organizationId, organizationId),
                                eqStatus(single.status, status))
                        .fetchOne();
        return count != null ? count : 0L;
    }

    /**
     * Multipart Upload Session 개수를 검색 조건으로 조회한다.
     *
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID (nullable)
     * @param status 상태 (nullable)
     * @return 개수
     */
    public long countMultipartUploadsByCriteria(
            Long tenantId, Long organizationId, SessionStatus status) {
        Long count =
                queryFactory
                        .select(multipart.count())
                        .from(multipart)
                        .where(
                                multipart.tenantId.eq(tenantId),
                                eqOrganizationId(multipart.organizationId, organizationId),
                                eqStatus(multipart.status, status))
                        .fetchOne();
        return count != null ? count : 0L;
    }

    private BooleanExpression eqOrganizationId(
            com.querydsl.core.types.dsl.NumberPath<Long> path, Long organizationId) {
        return organizationId != null ? path.eq(organizationId) : null;
    }

    private BooleanExpression eqStatus(
            com.querydsl.core.types.dsl.EnumPath<SessionStatus> path, SessionStatus status) {
        return status != null ? path.eq(status) : null;
    }
}
