package com.ryuqq.fileflow.domain.session.fixture;

import java.time.Clock;

import com.ryuqq.fileflow.domain.file.vo.FileName;
import com.ryuqq.fileflow.domain.file.vo.FileSize;
import com.ryuqq.fileflow.domain.file.vo.MimeType;
import com.ryuqq.fileflow.domain.file.vo.UploadType;
import com.ryuqq.fileflow.domain.session.UploadSession;
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
}

