package com.ryuqq.fileflow.adapter.out.persistence.asset.repository;

import static com.ryuqq.fileflow.adapter.out.persistence.asset.entity.QProcessedFileAssetJpaEntity.processedFileAssetJpaEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.ProcessedFileAssetJpaEntity;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * ProcessedFileAsset QueryDSL Repository.
 *
 * <p>Entity를 반환하고 Adapter에서 Domain으로 변환합니다.
 */
@Repository
public class ProcessedFileAssetQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public ProcessedFileAssetQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 원본 FileAsset ID로 ProcessedFileAsset 목록 조회.
     *
     * @param originalAssetId 원본 FileAsset ID
     * @return ProcessedFileAssetJpaEntity 목록
     */
    public List<ProcessedFileAssetJpaEntity> findByOriginalAssetId(String originalAssetId) {
        return queryFactory
                .selectFrom(processedFileAssetJpaEntity)
                .where(processedFileAssetJpaEntity.originalAssetId.eq(originalAssetId))
                .orderBy(processedFileAssetJpaEntity.createdAt.desc())
                .fetch();
    }

    /**
     * 부모 ProcessedFileAsset ID로 하위 목록 조회.
     *
     * @param parentAssetId 부모 ProcessedFileAsset ID
     * @return ProcessedFileAssetJpaEntity 목록
     */
    public List<ProcessedFileAssetJpaEntity> findByParentAssetId(String parentAssetId) {
        return queryFactory
                .selectFrom(processedFileAssetJpaEntity)
                .where(processedFileAssetJpaEntity.parentAssetId.eq(parentAssetId))
                .orderBy(processedFileAssetJpaEntity.createdAt.desc())
                .fetch();
    }
}
