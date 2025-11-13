package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.repository;

import static com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.QUploadPartJpaEntity.uploadPartJpaEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.UploadPartJpaEntity;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * UploadPartQueryDslRepository - UploadPart QueryDSL 전용 Repository
 *
 * <p>QueryDSL을 사용한 동적 쿼리 구현입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>QueryDSL 기반 동적 쿼리 실행</li>
 *   <li>복잡한 조회 조건 처리</li>
 *   <li>배치 조회를 통한 N+1 문제 방지</li>
 *   <li>타입 안전한 쿼리 작성</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ {@code @Component} 사용 (Spring Bean 등록)</li>
 *   <li>✅ 구현체만 존재 (인터페이스 추상화 불필요)</li>
 *   <li>✅ JPAQueryFactory를 통한 QueryDSL 쿼리 실행</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ CQRS Query Side 전용 (조회만)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class UploadPartQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * Constructor - JPAQueryFactory 주입
     *
     * @param queryFactory JPA queryFactory
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public UploadPartQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * Multipart Upload ID로 Upload Part 조회
     *
     * <p><strong>QueryDSL 쿼리:</strong></p>
     * <pre>
     * SELECT p
     * FROM UploadPartJpaEntity p
     * WHERE p.multipartUploadId = :multipartUploadId
     * ORDER BY p.partNumber ASC
     * </pre>
     *
     * @param multipartUploadId Multipart Upload ID
     * @return Upload Part 엔티티 목록 (Part Number 오름차순)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public List<UploadPartJpaEntity> findByMultipartUploadId(Long multipartUploadId) {
        if (multipartUploadId == null) {
            return List.of();
        }

        return queryFactory
            .selectFrom(uploadPartJpaEntity)
            .where(uploadPartJpaEntity.multipartUploadId.eq(multipartUploadId))
            .orderBy(uploadPartJpaEntity.partNumber.asc())
            .fetch();
    }

    /**
     * 여러 Multipart Upload ID로 Upload Part 배치 조회
     *
     * <p>N+1 문제를 방지하기 위한 배치 조회입니다.</p>
     *
     * <p><strong>QueryDSL 쿼리:</strong></p>
     * <pre>
     * SELECT p
     * FROM UploadPartJpaEntity p
     * WHERE p.multipartUploadId IN :multipartIds
     * ORDER BY p.multipartUploadId ASC, p.partNumber ASC
     * </pre>
     *
     * @param multipartIds Multipart Upload ID 목록
     * @return Upload Part 엔티티 목록 (Multipart Upload ID, Part Number 오름차순)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public List<UploadPartJpaEntity> findByMultipartUploadIds(List<Long> multipartIds) {
        if (multipartIds == null || multipartIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
            .selectFrom(uploadPartJpaEntity)
            .where(uploadPartJpaEntity.multipartUploadId.in(multipartIds))
            .orderBy(
                uploadPartJpaEntity.multipartUploadId.asc(),
                uploadPartJpaEntity.partNumber.asc()
            )
            .fetch();
    }
}
