package com.ryuqq.fileflow.application.session;

import com.ryuqq.fileflow.application.session.dto.response.MultipartUploadSessionResponse;
import com.ryuqq.fileflow.application.session.dto.response.SingleUploadSessionResponse;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Session Application Response 테스트 Fixtures.
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
public final class SessionResponseFixtures {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant EXPIRES_AT = NOW.plus(Duration.ofHours(1));

    private SessionResponseFixtures() {}

    // ===== Single Session Response Fixtures =====

    public static SingleUploadSessionResponse singleResponse() {
        return singleResponse("single-session-001");
    }

    public static SingleUploadSessionResponse singleResponse(String sessionId) {
        return new SingleUploadSessionResponse(
                sessionId,
                "https://s3.presigned-url.com/test",
                "public/2026/01/" + sessionId + ".jpg",
                "fileflow-bucket",
                AccessType.PUBLIC,
                "product-image.jpg",
                "image/jpeg",
                "CREATED",
                EXPIRES_AT,
                NOW);
    }

    // ===== Multipart Session Response Fixtures =====

    public static MultipartUploadSessionResponse multipartResponse() {
        return multipartResponse("multipart-session-001");
    }

    public static MultipartUploadSessionResponse multipartResponse(String sessionId) {
        return new MultipartUploadSessionResponse(
                sessionId,
                "upload-id-001",
                "public/2026/01/" + sessionId + ".jpg",
                "fileflow-bucket",
                AccessType.PUBLIC,
                "large-file.jpg",
                "image/jpeg",
                5_242_880L,
                "INITIATED",
                0,
                List.of(),
                EXPIRES_AT,
                NOW);
    }
}
