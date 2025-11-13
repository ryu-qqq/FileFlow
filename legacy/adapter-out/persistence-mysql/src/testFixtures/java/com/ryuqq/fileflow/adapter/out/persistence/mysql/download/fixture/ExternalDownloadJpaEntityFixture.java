package com.ryuqq.fileflow.adapter.out.persistence.mysql.download.fixture;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.download.entity.ExternalDownloadJpaEntity;
import com.ryuqq.fileflow.domain.download.ExternalDownloadStatus;

import java.time.LocalDateTime;

/**
 * ExternalDownloadJpaEntity Test Fixture
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class ExternalDownloadJpaEntityFixture {

    private static final Long DEFAULT_UPLOAD_SESSION_ID = 1L;
    private static final String DEFAULT_SOURCE_URL = "https://example.com/file.jpg";
    private static final ExternalDownloadStatus DEFAULT_STATUS = ExternalDownloadStatus.INIT;
    private static final LocalDateTime DEFAULT_CREATED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime DEFAULT_UPDATED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);

    private ExternalDownloadJpaEntityFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static ExternalDownloadJpaEntity create() {
        return ExternalDownloadJpaEntity.create(
            DEFAULT_UPLOAD_SESSION_ID,
            DEFAULT_SOURCE_URL,
            DEFAULT_STATUS
        );
    }

    public static ExternalDownloadJpaEntity createWithId(Long id) {
        return ExternalDownloadJpaEntity.reconstitute(
            id,
            DEFAULT_UPLOAD_SESSION_ID,
            DEFAULT_SOURCE_URL,
            0L,
            null,
            DEFAULT_STATUS,
            0,
            null,
            null,
            null,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT
        );
    }

    public static ExternalDownloadJpaEntity[] createMultiple(int count) {
        ExternalDownloadJpaEntity[] entities = new ExternalDownloadJpaEntity[count];
        for (int i = 0; i < count; i++) {
            entities[i] = createWithId((long) (i + 1));
        }
        return entities;
    }
}
