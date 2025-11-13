package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.repository;

import static com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.QMultipartUploadJpaEntity.multipartUploadJpaEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.MultipartUploadJpaEntity;
import com.ryuqq.fileflow.domain.upload.MultipartUpload;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * MultipartUploadQueryDslRepository - MultipartUpload QueryDSL 전용 Repository
 *
 * <p>QueryDSL을 사용한 동적 쿼리 구현입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>QueryDSL 기반 동적 쿼리 실행</li>
 *   <li>복잡한 조회 조건 처리</li>
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
public class MultipartUploadQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * Constructor - JPAQueryFactory 주입
     *
     * @param queryFactory JPA queryFactory
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public MultipartUploadQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * ID로 Multipart Upload 조회
     *
     * <p><strong>QueryDSL 쿼리:</strong></p>
     * <pre>
     * SELECT m
     * FROM MultipartUploadJpaEntity m
     * WHERE m.id = :id
     * </pre>
     *
     * @param id Multipart Upload ID
     * @return Multipart Upload 엔티티 (Optional)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public Optional<MultipartUploadJpaEntity> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        MultipartUploadJpaEntity entity = queryFactory
            .selectFrom(multipartUploadJpaEntity)
            .where(multipartUploadJpaEntity.id.eq(id))
            .fetchOne();

        return Optional.ofNullable(entity);
    }

    /**
     * Upload Session ID로 Multipart Upload 조회
     *
     * <p><strong>QueryDSL 쿼리:</strong></p>
     * <pre>
     * SELECT m
     * FROM MultipartUploadJpaEntity m
     * WHERE m.uploadSessionId = :uploadSessionId
     * ORDER BY m.createdAt DESC
     * LIMIT 1
     * </pre>
     *
     * @param uploadSessionId Upload Session ID
     * @return Multipart Upload 엔티티 (Optional)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public Optional<MultipartUploadJpaEntity> findByUploadSessionId(Long uploadSessionId) {
        if (uploadSessionId == null) {
            return Optional.empty();
        }

        MultipartUploadJpaEntity entity = queryFactory
            .selectFrom(multipartUploadJpaEntity)
            .where(multipartUploadJpaEntity.uploadSessionId.eq(uploadSessionId))
            .orderBy(multipartUploadJpaEntity.createdAt.desc())
            .fetchFirst();

        return Optional.ofNullable(entity);
    }

    /**
     * 상태별 Multipart Upload 목록 조회
     *
     * <p><strong>QueryDSL 쿼리:</strong></p>
     * <pre>
     * SELECT m
     * FROM MultipartUploadJpaEntity m
     * WHERE m.status = :status
     * ORDER BY m.createdAt ASC
     * </pre>
     *
     * @param status Multipart 상태
     * @return Multipart Upload 엔티티 목록
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public List<MultipartUploadJpaEntity> findByStatus(MultipartUpload.MultipartStatus status) {
        if (status == null) {
            return List.of();
        }

        return queryFactory
            .selectFrom(multipartUploadJpaEntity)
            .where(multipartUploadJpaEntity.status.eq(status))
            .orderBy(multipartUploadJpaEntity.createdAt.asc())
            .fetch();
    }
}
