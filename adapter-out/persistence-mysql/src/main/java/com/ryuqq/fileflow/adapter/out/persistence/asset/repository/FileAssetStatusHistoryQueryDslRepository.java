package com.ryuqq.fileflow.adapter.out.persistence.asset.repository;

import static com.ryuqq.fileflow.adapter.out.persistence.asset.entity.QFileAssetStatusHistoryJpaEntity.fileAssetStatusHistoryJpaEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.FileAssetStatusHistoryJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * FileAssetStatusHistory QueryDSL Repository.
 *
 * <p>Entity를 반환하고 Adapter에서 Domain으로 변환합니다.
 */
@Repository
public class FileAssetStatusHistoryQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public FileAssetStatusHistoryQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * FileAsset ID로 상태 변경 이력 목록 조회 (시간순 정렬).
     *
     * @param fileAssetId 파일 에셋 ID
     * @return 상태 변경 이력 목록
     */
    public List<FileAssetStatusHistoryJpaEntity> findByFileAssetId(String fileAssetId) {
        return queryFactory
                .selectFrom(fileAssetStatusHistoryJpaEntity)
                .where(fileAssetStatusHistoryJpaEntity.fileAssetId.eq(fileAssetId))
                .orderBy(fileAssetStatusHistoryJpaEntity.changedAt.asc())
                .fetch();
    }

    /**
     * FileAsset ID로 최신 상태 변경 이력 조회.
     *
     * @param fileAssetId 파일 에셋 ID
     * @return 최신 상태 변경 이력
     */
    public Optional<FileAssetStatusHistoryJpaEntity> findLatestByFileAssetId(String fileAssetId) {
        FileAssetStatusHistoryJpaEntity result =
                queryFactory
                        .selectFrom(fileAssetStatusHistoryJpaEntity)
                        .where(fileAssetStatusHistoryJpaEntity.fileAssetId.eq(fileAssetId))
                        .orderBy(fileAssetStatusHistoryJpaEntity.changedAt.desc())
                        .fetchFirst();

        return Optional.ofNullable(result);
    }

    /**
     * SLA 초과 상태 변경 이력 조회.
     *
     * @param slaMillis SLA 기준 시간 (밀리초)
     * @param limit 최대 조회 개수
     * @return SLA 초과 이력 목록
     */
    public List<FileAssetStatusHistoryJpaEntity> findExceedingSla(long slaMillis, int limit) {
        return queryFactory
                .selectFrom(fileAssetStatusHistoryJpaEntity)
                .where(fileAssetStatusHistoryJpaEntity.durationMillis.gt(slaMillis))
                .orderBy(fileAssetStatusHistoryJpaEntity.changedAt.desc())
                .limit(limit)
                .fetch();
    }
}
