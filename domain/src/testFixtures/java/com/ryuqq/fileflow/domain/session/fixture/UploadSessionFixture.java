package com.ryuqq.fileflow.domain.session.fixture;

import java.time.Clock;
import java.time.LocalDateTime;

import com.ryuqq.fileflow.domain.file.vo.FileName;
import com.ryuqq.fileflow.domain.file.vo.FileSize;
import com.ryuqq.fileflow.domain.file.vo.MimeType;
import com.ryuqq.fileflow.domain.file.vo.S3Path;
import com.ryuqq.fileflow.domain.file.vo.UploadType;
import com.ryuqq.fileflow.domain.session.UploadSession;
import com.ryuqq.fileflow.domain.session.vo.SessionId;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import com.ryuqq.fileflow.domain.session.vo.UserRole;

/**
 * UploadSession 테스트 데이터 생성 Fixture.
 */
public final class UploadSessionFixture {

    private UploadSessionFixture() {}

    /**
     * 신규 업로드 세션 Fixture (ID 자동 생성).
     *
     * @return 신규 UploadSession
     */
    public static UploadSession forNew() {
        return UploadSession.forNew(
            UploadType.SINGLE,
            "uploads",
            FileName.from("test.jpg"),
            FileSize.of(1024 * 1024), // 1MB
            MimeType.of("image/jpeg"),
            1L, // userId
            1L, // tenantId
            UserRole.DEFAULT,
            "seller1", // sellerName
            Clock.systemDefaultZone()
        );
    }

    /**
     * ID 기반 업로드 세션 Fixture.
     *
     * @param sessionId 세션 ID
     * @return UploadSession
     */
    public static UploadSession of(SessionId sessionId) {
        return UploadSession.of(
            sessionId,
            UploadType.SINGLE,
            "uploads",
            FileName.from("test.jpg"),
            FileSize.of(1024 * 1024), // 1MB
            MimeType.of("image/jpeg"),
            1L, // userId
            1L, // tenantId
            UserRole.DEFAULT,
            "seller1", // sellerName
            Clock.systemDefaultZone()
        );
    }

    /**
     * 영속성 복원용 업로드 세션 Fixture.
     *
     * @param sessionId 세션 ID
     * @param status 세션 상태
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     * @param expiresAt 만료 시각
     * @param completedAt 완료 시각 (Nullable)
     * @return UploadSession
     */
    public static UploadSession reconstitute(
        SessionId sessionId,
        SessionStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime expiresAt,
        LocalDateTime completedAt
    ) {
        Long tenantId = 1L;
        UserRole role = UserRole.DEFAULT;
        String sellerName = "seller1";
        MimeType mimeType = MimeType.of("image/jpeg");
        S3Path s3Path = S3Path.from(
            role,
            tenantId,
            sellerName,
            "uploads",
            sessionId.value(),
            mimeType.value()
        );

        return UploadSession.reconstitute(
            sessionId,
            1L, // userId
            tenantId,
            role,
            sellerName,
            UploadType.SINGLE,
            "uploads",
            FileName.from("test.jpg"),
            FileSize.of(1024 * 1024), // 1MB
            mimeType,
            s3Path,
            status,
            Clock.systemDefaultZone(),
            createdAt,
            updatedAt,
            expiresAt,
            completedAt
        );
    }
}

