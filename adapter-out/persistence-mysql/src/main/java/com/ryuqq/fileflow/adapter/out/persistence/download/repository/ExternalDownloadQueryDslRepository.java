package com.ryuqq.fileflow.adapter.out.persistence.download.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.download.entity.ExternalDownloadJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.download.entity.QExternalDownloadJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * ExternalDownload QueryDSL Repository.
 *
 * <p>ExternalDownload 조회를 담당하는 QueryDSL Repository입니다.
 */
@Repository
public class ExternalDownloadQueryDslRepository {

    private static final QExternalDownloadJpaEntity download =
            QExternalDownloadJpaEntity.externalDownloadJpaEntity;

    private final JPAQueryFactory queryFactory;

    public ExternalDownloadQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * ID로 ExternalDownload를 조회한다.
     *
     * @param id ExternalDownload ID (UUID)
     * @return ExternalDownloadJpaEntity Optional
     */
    public Optional<ExternalDownloadJpaEntity> findById(UUID id) {
        ExternalDownloadJpaEntity result =
                queryFactory.selectFrom(download).where(download.id.eq(id)).fetchOne();
        return Optional.ofNullable(result);
    }

    /**
     * ID와 테넌트 ID로 ExternalDownload를 조회한다.
     *
     * @param id ExternalDownload ID (UUID)
     * @param tenantId 테넌트 ID (UUIDv7 문자열)
     * @return ExternalDownloadJpaEntity Optional
     */
    public Optional<ExternalDownloadJpaEntity> findByIdAndTenantId(UUID id, String tenantId) {
        ExternalDownloadJpaEntity result =
                queryFactory
                        .selectFrom(download)
                        .where(download.id.eq(id), download.tenantId.eq(tenantId))
                        .fetchOne();
        return Optional.ofNullable(result);
    }

    /**
     * ID로 존재 여부를 확인한다.
     *
     * @param id ExternalDownload ID (UUID)
     * @return 존재 여부
     */
    public boolean existsById(UUID id) {
        Integer result =
                queryFactory.selectOne().from(download).where(download.id.eq(id)).fetchFirst();
        return result != null;
    }
}
