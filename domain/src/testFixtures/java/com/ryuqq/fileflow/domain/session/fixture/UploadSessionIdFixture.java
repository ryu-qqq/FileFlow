package com.ryuqq.fileflow.domain.session.fixture;

import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.util.UUID;

/**
 * UploadSessionId Value Object Test Fixture
 *
 * @author development-team
 * @since 1.0.0
 */
public class UploadSessionIdFixture {

    private UploadSessionIdFixture() {
        throw new AssertionError("Utility class");
    }

    /** 기본 UploadSessionId Fixture (신규 생성) */
    public static UploadSessionId defaultUploadSessionId() {
        return UploadSessionId.forNew();
    }

    /** 고정된 UUID를 가진 UploadSessionId Fixture (테스트 검증용) */
    public static UploadSessionId fixedUploadSessionId() {
        return UploadSessionId.of(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
    }

    /** Custom UUID를 가진 UploadSessionId Fixture */
    public static UploadSessionId customUploadSessionId(UUID uuid) {
        return UploadSessionId.of(uuid);
    }

    /** Custom String UUID를 가진 UploadSessionId Fixture */
    public static UploadSessionId customUploadSessionId(String uuidString) {
        return UploadSessionId.of(uuidString);
    }
}
