package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity.ExtractedDataJpaEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity.QExtractedDataJpaEntity.extractedDataJpaEntity;

/**
 * ExtractedDataQueryDslRepository - ExtractedData QueryDSL 전용 Repository
 *
 * <p>QueryDSL을 사용한 ExtractedData 조회 쿼리 구현입니다.
 * JPAQueryFactory를 사용하여 동적 쿼리를 실행합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>QueryDSL 기반 동적 쿼리 실행</li>
 *   <li>복잡한 조회 조건 처리</li>
 *   <li>Soft Delete 필터링 자동 적용</li>
 *   <li>Helper 메서드를 통한 쿼리 조건 캡슐화</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ {@code @Component} 사용 (Spring Bean 등록)</li>
 *   <li>✅ 구현체만 존재 (인터페이스 추상화 불필요)</li>
 *   <li>✅ JPAQueryFactory를 통한 QueryDSL 쿼리 실행</li>
 *   <li>✅ JpaEntity 반환 (Domain 변환은 Adapter에서)</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ CQRS Query Side 전용 (조회만)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class ExtractedDataQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * Constructor - JPAQueryFactory 초기화
     *
     * <p> JPAQueryFactory를 생성합니다.</p>
     *
     * @param queryFactory JPA queryFactory
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public ExtractedDataQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * fileId로 모든 ExtractedData 조회
     *
     * <p>특정 FileAsset에서 추출된 모든 메타데이터를 조회합니다.
     * Soft Delete된 데이터는 자동으로 필터링됩니다.</p>
     *
     * <p>QueryDSL 쿼리 예시:</p>
     * <pre>{@code
     * SELECT *
     * FROM extracted_data
     * WHERE file_id = ?
     *   AND deleted_at IS NULL
     * ORDER BY extracted_at ASC
     * }</pre>
     *
     * @param fileId FileAsset의 ID (Long FK, 필수)
     * @return ExtractedDataJpaEntity 목록 (빈 리스트 가능)
     * @throws IllegalArgumentException fileId가 null이거나 0 이하인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public List<ExtractedDataJpaEntity> findAllByFileId(Long fileId) {
        if (fileId == null || fileId <= 0) {
            throw new IllegalArgumentException("fileId는 필수이며 양수여야 합니다");
        }

        return queryFactory
            .selectFrom(extractedDataJpaEntity)
            .where(
                eqFileId(fileId),
                isNotDeleted()
            )
            .orderBy(extractedDataJpaEntity.extractedAt.asc())
            .fetch();
    }

    // ========================================
    // Private Helper Methods (동적 쿼리 조건)
    // ========================================

    /**
     * fileId 일치 조건
     *
     * <p>fileId가 null이거나 0 이하이면 조건을 제외합니다.</p>
     *
     * @param fileId File ID (Long FK, nullable)
     * @return BooleanExpression (null 가능)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    private BooleanExpression eqFileId(Long fileId) {
        if (fileId == null || fileId <= 0) {
            return null;
        }
        return extractedDataJpaEntity.fileId.eq(fileId);
    }

    /**
     * Soft Delete 필터링 조건 (deletedAt IS NULL)
     *
     * <p>삭제되지 않은 데이터만 조회합니다.</p>
     *
     * @return BooleanExpression
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    private BooleanExpression isNotDeleted() {
        return extractedDataJpaEntity.deletedAt.isNull();
    }
}
