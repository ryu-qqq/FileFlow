package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity.FileVariantJpaEntity;
import com.ryuqq.fileflow.domain.file.variant.VariantType;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity.QFileVariantJpaEntity.fileVariantJpaEntity;

/**
 * FileVariantQueryDslRepository - FileVariant QueryDSL 전용 Repository
 *
 * <p>QueryDSL JPAQueryFactory를 사용하여 FileVariant 조회 쿼리를 실행합니다.
 * 동적 쿼리 조건을 활용하여 성능 최적화된 조회를 제공합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>QueryDSL을 사용한 FileVariant 조회 쿼리 실행</li>
 *   <li>동적 쿼리 조건 생성 및 적용</li>
 *   <li>Parent FileAsset ID 기반 조회 (Long FK 전략)</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ {@code @Component} 사용 (Spring Bean 등록)</li>
 *   <li>✅ 구현체만 존재 (인터페이스 추상화 불필요)</li>
 *   <li>✅ JPAQueryFactory를 통한 QueryDSL 쿼리 실행</li>
 *   <li>✅ EntityManager를 생성자로 받아서 JPAQueryFactory 생성</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ Helper 메서드를 통한 동적 쿼리 조건 생성</li>
 *   <li>✅ CQRS Query Side 전용 (조회만)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class FileVariantQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * Constructor - JPAQueryFactory 생성
     *
     * @param queryFactory JPA EntityManager
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public FileVariantQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * fileId로 모든 FileVariant 조회
     *
     * <p>QueryDSL을 사용하여 Parent FileAsset ID로 모든 Variant를 조회합니다.
     * createdAt 내림차순으로 정렬하여 최신 순으로 반환합니다.</p>
     *
     * @param fileId FileAsset의 ID (Long FK)
     * @return FileVariantJpaEntity 목록 (빈 리스트 가능)
     * @throws IllegalArgumentException fileId가 null이거나 0 이하인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */

    public List<FileVariantJpaEntity> findAllByFileId(Long fileId) {
        if (fileId == null || fileId <= 0) {
            throw new IllegalArgumentException("FileId는 필수입니다");
        }

        return queryFactory
            .selectFrom(fileVariantJpaEntity)
            .where(eqParentFileAssetId(fileId))
            .orderBy(fileVariantJpaEntity.createdAt.desc())
            .fetch();
    }

    /**
     * fileId와 variantType으로 FileVariant 조회
     *
     * <p>QueryDSL을 사용하여 Parent FileAsset ID와 Variant Type으로 단건 조회합니다.</p>
     *
     * @param fileId FileAsset의 ID (Long FK)
     * @param variantType Variant Type
     * @return FileVariantJpaEntity (있으면)
     * @throws IllegalArgumentException fileId가 null이거나 0 이하이거나 variantType이 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */

    public Optional<FileVariantJpaEntity> findByFileIdAndVariantType(Long fileId, VariantType variantType) {
        if (fileId == null || fileId <= 0) {
            throw new IllegalArgumentException("FileId는 필수입니다");
        }
        if (variantType == null) {
            throw new IllegalArgumentException("VariantType은 필수입니다");
        }

        FileVariantJpaEntity entity = queryFactory
            .selectFrom(fileVariantJpaEntity)
            .where(
                eqParentFileAssetId(fileId),
                eqVariantType(variantType)
            )
            .fetchOne();

        return Optional.ofNullable(entity);
    }

    /**
     * fileId와 variantType으로 존재 여부 확인
     *
     * <p>QueryDSL을 사용하여 Parent FileAsset ID와 Variant Type으로 존재 여부를 확인합니다.</p>
     *
     * @param fileId FileAsset의 ID (Long FK)
     * @param variantType Variant Type
     * @return 존재 여부
     * @throws IllegalArgumentException fileId가 null이거나 0 이하이거나 variantType이 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */

    public boolean existsByFileIdAndVariantType(Long fileId, VariantType variantType) {
        if (fileId == null || fileId <= 0) {
            throw new IllegalArgumentException("FileId는 필수입니다");
        }
        if (variantType == null) {
            throw new IllegalArgumentException("VariantType은 필수입니다");
        }

        Long count = queryFactory
            .select(fileVariantJpaEntity.count())
            .from(fileVariantJpaEntity)
            .where(
                eqParentFileAssetId(fileId),
                eqVariantType(variantType)
            )
            .fetchOne();

        return count != null && count > 0;
    }

    // ========================================
    // Private Helper Methods (동적 쿼리 조건)
    // ========================================

    /**
     * Parent FileAsset ID 일치 조건
     *
     * @param fileId Parent FileAsset ID (Long FK, null이면 조건 제외)
     * @return BooleanExpression
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    private BooleanExpression eqParentFileAssetId(Long fileId) {
        if (fileId == null || fileId <= 0) {
            return null;
        }
        return fileVariantJpaEntity.parentFileAssetId.eq(fileId);
    }

    /**
     * Variant Type 일치 조건
     *
     * @param variantType Variant Type (null이면 조건 제외)
     * @return BooleanExpression
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    private BooleanExpression eqVariantType(VariantType variantType) {
        if (variantType == null) {
            return null;
        }
        return fileVariantJpaEntity.variantType.eq(variantType);
    }
}
