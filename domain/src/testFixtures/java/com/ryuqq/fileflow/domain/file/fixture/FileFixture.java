package com.ryuqq.fileflow.domain.file.fixture;

import java.time.Clock;

import com.ryuqq.fileflow.domain.file.File;
import com.ryuqq.fileflow.domain.session.UploadSession;
import com.ryuqq.fileflow.domain.session.fixture.UploadSessionFixture;
import com.ryuqq.fileflow.domain.session.vo.SessionId;

/**
 * File 테스트 데이터 생성 Fixture.
 */
public final class FileFixture {

    private FileFixture() {}

    /**
     * 신규 파일 Fixture (ID 자동 생성).
     *
     * @return 신규 File
     */
    public static File forNew() {
        UploadSession session = UploadSessionFixture.forNew();
        return File.forNew(session, Clock.systemDefaultZone());
    }

    /**
     * ID 기반 파일 Fixture.
     *
     * @param fileId 파일 ID
     * @return File
     */
    public static File of(SessionId fileId) {
        UploadSession session = UploadSessionFixture.forNew();
        return File.of(fileId, session, Clock.systemDefaultZone());
    }

    /**
     * 영속성 복원용 파일 Fixture.
     *
     * @param fileId 파일 ID
     * @param deleted 삭제 여부
     * @return File
     */
    public static File reconstitute(SessionId fileId, boolean deleted) {
        UploadSession session = UploadSessionFixture.forNew();
        java.time.LocalDateTime uploadedAt = java.time.LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        java.time.LocalDateTime updatedAt = java.time.LocalDateTime.of(2024, 1, 1, 11, 0, 0);
        java.time.LocalDateTime deletedAt = deleted ? java.time.LocalDateTime.of(2024, 1, 1, 12, 0, 0) : null;

        return File.reconstitute(
            fileId,
            session.getUserId(),
            session.getTenantId(),
            session.getRole(),
            session.getFileName(),
            session.getFileSize(),
            session.getMimeType(),
            session.getS3Path(),
            session.getUploadType(),
            Clock.systemDefaultZone(),
            uploadedAt,
            updatedAt,
            deleted,
            deletedAt
        );
    }
}

