package com.ryuqq.fileflow.adapter.out.persistence.mysql.download.adapter;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.download.entity.ExternalDownloadJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.download.mapper.ExternalDownloadEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.download.repository.ExternalDownloadJpaRepository;
import com.ryuqq.fileflow.application.download.port.out.ExternalDownloadQueryPort;
import com.ryuqq.fileflow.domain.download.ExternalDownload;
import com.ryuqq.fileflow.domain.download.ExternalDownloadStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ryuqq.fileflow.adapter.out.persistence.mysql.download.entity.QExternalDownloadJpaEntity.externalDownloadJpaEntity;

/**
 * External Download Query Adapter (CQRS - Query Side)
 *
 * <p>Application Layer의 {@link ExternalDownloadQueryPort}를 구현하는 Persistence Adapter입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>ExternalDownload Domain Aggregate의 조회 (읽기 전용)</li>
 *   <li>QueryDSL을 통한 DB 접근</li>
 *   <li>Mapper를 통한 Entity → Domain 변환</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Port & Adapter 패턴 준수</li>
 *   <li>✅ @Transactional(readOnly = true) 적용</li>
 *   <li>✅ QueryDSL 사용 (성능 최적화)</li>
 *   <li>✅ Mapper를 통한 명시적 변환</li>
 *   <li>✅ Long FK Strategy (JPA 관계 없음)</li>
 *   <li>✅ CQRS - Query 전용 (읽기만)</li>
 *   <li>❌ 비즈니스 로직 포함 금지</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
@Transactional(readOnly = true)
public class ExternalDownloadQueryAdapter implements ExternalDownloadQueryPort {

    private final JPAQueryFactory queryFactory;
    private final ExternalDownloadJpaRepository repository;

    /**
     * 생성자
     *
     * @param queryFactory QueryDSL JPAQueryFactory
     * @param repository External Download JPA Repository (복잡한 쿼리용)
     */
    public ExternalDownloadQueryAdapter(
        JPAQueryFactory queryFactory,
        ExternalDownloadJpaRepository repository
    ) {
        this.queryFactory = queryFactory;
        this.repository = repository;
    }

    /**
     * ID로 External Download 조회
     *
     * @param id External Download ID
     * @return External Download (Optional)
     */
    @Override
    public Optional<ExternalDownload> findById(Long id) {
        ExternalDownloadJpaEntity entity = queryFactory
            .selectFrom(externalDownloadJpaEntity)
            .where(externalDownloadJpaEntity.id.eq(id))
            .fetchOne();

        return Optional.ofNullable(entity)
            .map(ExternalDownloadEntityMapper::toDomain);
    }

    /**
     * Upload Session ID로 External Download 조회
     *
     * @param uploadSessionId Upload Session ID
     * @return External Download (Optional)
     */
    @Override
    public Optional<ExternalDownload> findByUploadSessionId(Long uploadSessionId) {
        ExternalDownloadJpaEntity entity = queryFactory
            .selectFrom(externalDownloadJpaEntity)
            .where(externalDownloadJpaEntity.uploadSessionId.eq(uploadSessionId))
            .fetchOne();

        return Optional.ofNullable(entity)
            .map(ExternalDownloadEntityMapper::toDomain);
    }

    /**
     * 상태별 External Download 목록 조회
     *
     * @param status External Download 상태
     * @return External Download 목록
     */
    @Override
    public List<ExternalDownload> findByStatus(ExternalDownloadStatus status) {
        List<ExternalDownloadJpaEntity> entities = queryFactory
            .selectFrom(externalDownloadJpaEntity)
            .where(externalDownloadJpaEntity.status.eq(status))
            .orderBy(externalDownloadJpaEntity.createdAt.desc())
            .fetch();

        return entities.stream()
            .map(ExternalDownloadEntityMapper::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * 재시도 가능한 다운로드 목록 조회
     *
     * <p>다음 조건을 만족하는 다운로드를 조회합니다:</p>
     * <ul>
     *   <li>DOWNLOADING 상태</li>
     *   <li>재시도 횟수가 최대값 미만</li>
     *   <li>마지막 재시도 시간이 retryAfter 이전</li>
     * </ul>
     *
     * <p>복잡한 쿼리는 Repository의 @Query를 활용합니다.</p>
     *
     * @param maxRetry 최대 재시도 횟수
     * @param retryAfter 재시도 가능 시간 (이 시간 이전에 마지막 재시도한 것만)
     * @return 재시도 가능한 External Download 목록
     */
    @Override
    public List<ExternalDownload> findRetryableDownloads(Integer maxRetry, LocalDateTime retryAfter) {
        // Repository의 @Query 메서드 활용 (복잡한 조건)
        return repository.findRetryableDownloads(maxRetry, retryAfter)
            .stream()
            .map(ExternalDownloadEntityMapper::toDomain)
            .collect(Collectors.toList());
    }
}

