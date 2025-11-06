package com.ryuqq.fileflow.adapter.out.persistence.mysql.download.fixture;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.download.entity.ExternalDownloadOutboxJpaEntity;
import com.ryuqq.fileflow.domain.common.OutboxStatus;

import java.time.LocalDateTime;

/**
 * ExternalDownloadOutboxJpaEntity Test Fixture
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class ExternalDownloadOutboxJpaEntityFixture {

    private static final String DEFAULT_IDEMPOTENCY_KEY = "idempotency-key-123";
    private static final Long DEFAULT_DOWNLOAD_ID = 1L;
    private static final Long DEFAULT_UPLOAD_SESSION_ID = 1L;
    private static final OutboxStatus DEFAULT_STATUS = OutboxStatus.PENDING;
    private static final LocalDateTime DEFAULT_CREATED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);

    private ExternalDownloadOutboxJpaEntityFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static ExternalDownloadOutboxJpaEntity create() {
        return ExternalDownloadOutboxJpaEntity.create(
            DEFAULT_IDEMPOTENCY_KEY,
            DEFAULT_DOWNLOAD_ID,
            DEFAULT_UPLOAD_SESSION_ID
        );
    }

    public static ExternalDownloadOutboxJpaEntity createWithId(Long id) {
        return ExternalDownloadOutboxJpaEntity.reconstitute(
            id,
            DEFAULT_IDEMPOTENCY_KEY,
            DEFAULT_DOWNLOAD_ID,
            DEFAULT_UPLOAD_SESSION_ID,
            DEFAULT_STATUS,
            0,
            DEFAULT_CREATED_AT
        );
    }

    public static ExternalDownloadOutboxJpaEntity[] createMultiple(int count) {
        ExternalDownloadOutboxJpaEntity[] entities = new ExternalDownloadOutboxJpaEntity[count];
        for (int i = 0; i < count; i++) {
            entities[i] = ExternalDownloadOutboxJpaEntity.reconstitute(
                (long) (i + 1),
                DEFAULT_IDEMPOTENCY_KEY + "-" + (i + 1),
                DEFAULT_DOWNLOAD_ID,
                DEFAULT_UPLOAD_SESSION_ID,
                DEFAULT_STATUS,
                0,
                DEFAULT_CREATED_AT
            );
        }
        return entities;
    }
}
