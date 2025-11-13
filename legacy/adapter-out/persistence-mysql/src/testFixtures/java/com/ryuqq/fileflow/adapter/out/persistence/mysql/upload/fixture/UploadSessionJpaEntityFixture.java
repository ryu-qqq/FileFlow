package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.fixture;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.UploadSessionJpaEntity;
import com.ryuqq.fileflow.domain.upload.SessionStatus;
import com.ryuqq.fileflow.domain.upload.UploadType;

import java.time.LocalDateTime;

/**
 * UploadSessionJpaEntity Test Fixture
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class UploadSessionJpaEntityFixture {

    private static final String DEFAULT_SESSION_KEY = "session-key-123";
    private static final Long DEFAULT_TENANT_ID = 1L;
    private static final String DEFAULT_FILE_NAME = "test-file.jpg";
    private static final Long DEFAULT_FILE_SIZE = 1024000L;
    private static final UploadType DEFAULT_UPLOAD_TYPE = UploadType.MULTIPART;
    private static final String DEFAULT_STORAGE_KEY = "uploads/2024/01/test-file.jpg";
    private static final SessionStatus DEFAULT_STATUS = SessionStatus.IN_PROGRESS;
    private static final LocalDateTime DEFAULT_CREATED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime DEFAULT_UPDATED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);

    private UploadSessionJpaEntityFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static UploadSessionJpaEntity create() {
        return UploadSessionJpaEntity.create(
            DEFAULT_SESSION_KEY,
            DEFAULT_TENANT_ID,
            DEFAULT_FILE_NAME,
            DEFAULT_FILE_SIZE,
            DEFAULT_UPLOAD_TYPE,
            DEFAULT_STORAGE_KEY,
            DEFAULT_STATUS
        );
    }

    public static UploadSessionJpaEntity createWithId(Long id) {
        return UploadSessionJpaEntity.reconstitute(
            id,
            DEFAULT_SESSION_KEY,
            DEFAULT_TENANT_ID,
            DEFAULT_FILE_NAME,
            DEFAULT_FILE_SIZE,
            DEFAULT_UPLOAD_TYPE,
            DEFAULT_STORAGE_KEY,
            DEFAULT_STATUS,
            null,
            null,
            null,
            null,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT
        );
    }

    public static UploadSessionJpaEntity[] createMultiple(int count) {
        UploadSessionJpaEntity[] entities = new UploadSessionJpaEntity[count];
        for (int i = 0; i < count; i++) {
            entities[i] = UploadSessionJpaEntity.reconstitute(
                (long) (i + 1),
                DEFAULT_SESSION_KEY + "-" + (i + 1),
                DEFAULT_TENANT_ID,
                DEFAULT_FILE_NAME,
                DEFAULT_FILE_SIZE,
                DEFAULT_UPLOAD_TYPE,
                DEFAULT_STORAGE_KEY,
                DEFAULT_STATUS,
                null,
                null,
                null,
                null,
                DEFAULT_CREATED_AT,
                DEFAULT_UPDATED_AT
            );
        }
        return entities;
    }
}
