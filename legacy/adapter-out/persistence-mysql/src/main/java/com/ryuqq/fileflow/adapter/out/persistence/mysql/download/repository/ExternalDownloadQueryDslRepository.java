package com.ryuqq.fileflow.adapter.out.persistence.mysql.download.repository;

import static com.ryuqq.fileflow.adapter.out.persistence.mysql.download.entity.QExternalDownloadJpaEntity.externalDownloadJpaEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.download.entity.ExternalDownloadJpaEntity;
import com.ryuqq.fileflow.domain.download.ExternalDownloadStatus;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ExternalDownloadQueryDslRepository - ExternalDownload QueryDSL 전용 Repository
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
public class ExternalDownloadQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * Constructor - JPAQueryFactory 주입
     *
     * @param queryFactory JPA queryFactory
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public ExternalDownloadQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * ID로 External Download 조회
     *
     * <p><strong>QueryDSL 쿼리:</strong></p>
     * <pre>
     * SELECT e
     * FROM ExternalDownloadJpaEntity e
     * WHERE e.id = :id
     * </pre>
     *
     * @param id External Download ID
     * @return External Download 엔티티 (Optional)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public Optional<ExternalDownloadJpaEntity> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        ExternalDownloadJpaEntity entity = queryFactory
            .selectFrom(externalDownloadJpaEntity)
            .where(externalDownloadJpaEntity.id.eq(id))
            .fetchOne();

        return Optional.ofNullable(entity);
    }

    /**
     * Upload Session ID로 External Download 조회
     *
     * <p><strong>QueryDSL 쿼리:</strong></p>
     * <pre>
     * SELECT e
     * FROM ExternalDownloadJpaEntity e
     * WHERE e.uploadSessionId = :uploadSessionId
     * ORDER BY e.createdAt DESC
     * LIMIT 1
     * </pre>
     *
     * @param uploadSessionId Upload Session ID
     * @return External Download 엔티티 (Optional)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public Optional<ExternalDownloadJpaEntity> findByUploadSessionId(Long uploadSessionId) {
        if (uploadSessionId == null) {
            return Optional.empty();
        }

        ExternalDownloadJpaEntity entity = queryFactory
            .selectFrom(externalDownloadJpaEntity)
            .where(externalDownloadJpaEntity.uploadSessionId.eq(uploadSessionId))
            .orderBy(externalDownloadJpaEntity.createdAt.desc())
            .fetchFirst();

        return Optional.ofNullable(entity);
    }

    /**
     * 상태별 External Download 목록 조회
     *
     * <p><strong>QueryDSL 쿼리:</strong></p>
     * <pre>
     * SELECT e
     * FROM ExternalDownloadJpaEntity e
     * WHERE e.status = :status
     * ORDER BY e.createdAt ASC
     * </pre>
     *
     * @param status External Download 상태
     * @return External Download 엔티티 목록
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public List<ExternalDownloadJpaEntity> findByStatus(ExternalDownloadStatus status) {
        if (status == null) {
            return List.of();
        }

        return queryFactory
            .selectFrom(externalDownloadJpaEntity)
            .where(externalDownloadJpaEntity.status.eq(status))
            .orderBy(externalDownloadJpaEntity.createdAt.asc())
            .fetch();
    }

    /**
     * 재시도 가능한 External Download 목록 조회
     *
     * <p>다음 조건을 모두 만족하는 다운로드를 조회합니다:</p>
     * <ul>
     *   <li>DOWNLOADING 상태</li>
     *   <li>재시도 횟수가 최대값 미만</li>
     *   <li>마지막 재시도 시간이 retryAfter 이전</li>
     * </ul>
     *
     * <p><strong>QueryDSL 쿼리:</strong></p>
     * <pre>
     * SELECT e
     * FROM ExternalDownloadJpaEntity e
     * WHERE e.status = 'DOWNLOADING'
     *   AND e.retryCount < :maxRetry
     *   AND e.lastRetryAt < :retryAfter
     * ORDER BY e.createdAt ASC
     * </pre>
     *
     * @param maxRetry 최대 재시도 횟수
     * @param retryAfter 재시도 가능 시간 (이 시간 이전에 마지막 재시도한 것만)
     * @return 재시도 가능한 External Download 엔티티 목록
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public List<ExternalDownloadJpaEntity> findRetryableDownloads(
        Integer maxRetry,
        LocalDateTime retryAfter
    ) {
        if (maxRetry == null || retryAfter == null) {
            return List.of();
        }

        return queryFactory
            .selectFrom(externalDownloadJpaEntity)
            .where(
                externalDownloadJpaEntity.status.eq(ExternalDownloadStatus.DOWNLOADING),
                externalDownloadJpaEntity.retryCount.lt(maxRetry),
                externalDownloadJpaEntity.lastRetryAt.before(retryAfter)
            )
            .orderBy(externalDownloadJpaEntity.createdAt.asc())
            .fetch();
    }
}
