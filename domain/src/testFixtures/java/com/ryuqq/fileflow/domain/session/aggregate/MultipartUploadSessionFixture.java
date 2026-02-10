package com.ryuqq.fileflow.domain.session.aggregate;

import com.ryuqq.fileflow.domain.session.id.MultipartUploadSessionId;
import com.ryuqq.fileflow.domain.session.vo.CompletedPart;
import com.ryuqq.fileflow.domain.session.vo.MultipartUploadSessionUpdateData;
import com.ryuqq.fileflow.domain.session.vo.UploadTargetFixture;
import java.time.Duration;
import java.time.Instant;

public class MultipartUploadSessionFixture {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant EXPIRES_AT = NOW.plus(Duration.ofHours(1));

    public static MultipartUploadSession anInitiatedSession() {
        return MultipartUploadSession.forNew(
                MultipartUploadSessionId.of("multipart-session-001"),
                UploadTargetFixture.anUploadTarget(),
                "upload-id-001",
                5_242_880L,
                "product-image",
                "commerce-service",
                EXPIRES_AT,
                NOW);
    }

    public static MultipartUploadSession anUploadingSession() {
        MultipartUploadSession session = anInitiatedSession();
        session.addCompletedPart(
                CompletedPart.of(1, "etag-part-1", 5_242_880L, NOW.plusSeconds(10)));
        return session;
    }

    public static MultipartUploadSession aCompletedSession() {
        MultipartUploadSession session = anUploadingSession();
        session.complete(
                MultipartUploadSessionUpdateData.of(10_485_760L, "etag-final"),
                NOW.plusSeconds(60));
        session.pollEvents();
        return session;
    }

    public static MultipartUploadSession anAbortedSession() {
        MultipartUploadSession session = anInitiatedSession();
        session.abort(NOW.plusSeconds(30));
        return session;
    }

    public static MultipartUploadSession anExpiredSession() {
        MultipartUploadSession session = anInitiatedSession();
        session.expire(EXPIRES_AT.plusSeconds(1));
        return session;
    }

    public static Instant defaultNow() {
        return NOW;
    }

    public static Instant defaultExpiresAt() {
        return EXPIRES_AT;
    }
}
