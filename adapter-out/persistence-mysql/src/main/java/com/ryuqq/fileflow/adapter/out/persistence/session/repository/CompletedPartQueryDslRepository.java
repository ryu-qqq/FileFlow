package com.ryuqq.fileflow.adapter.out.persistence.session.repository;

import static com.ryuqq.fileflow.adapter.out.persistence.session.entity.QCompletedPartJpaEntity.completedPartJpaEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.CompletedPartJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * CompletedPart QueryDSL Repository.
 *
 * <p>Query 작업용 QueryDSL Repository입니다.
 *
 * <p><strong>설계 원칙:</strong>
 *
 * <ul>
 *   <li>JPAQueryFactory 직접 주입
 *   <li>조회 전용 (Command는 JpaRepository 사용)
 *   <li>DTO Projection 지향
 * </ul>
 */
@Repository
public class CompletedPartQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public CompletedPartQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 세션 ID와 Part 번호로 CompletedPart를 조회한다.
     *
     * @param sessionId 세션 ID
     * @param partNumber Part 번호
     * @return CompletedPartJpaEntity (없으면 empty)
     */
    public Optional<CompletedPartJpaEntity> findBySessionIdAndPartNumber(
            String sessionId, int partNumber) {
        CompletedPartJpaEntity result =
                queryFactory
                        .selectFrom(completedPartJpaEntity)
                        .where(
                                completedPartJpaEntity.sessionId.eq(sessionId),
                                completedPartJpaEntity.partNumber.eq(partNumber))
                        .fetchOne();

        return Optional.ofNullable(result);
    }

    /**
     * 세션 ID로 완료된 Part 목록을 조회한다.
     *
     * @param sessionId 세션 ID
     * @return CompletedPartJpaEntity 목록
     */
    public List<CompletedPartJpaEntity> findAllBySessionId(String sessionId) {
        return queryFactory
                .selectFrom(completedPartJpaEntity)
                .where(completedPartJpaEntity.sessionId.eq(sessionId))
                .orderBy(completedPartJpaEntity.partNumber.asc())
                .fetch();
    }
}
