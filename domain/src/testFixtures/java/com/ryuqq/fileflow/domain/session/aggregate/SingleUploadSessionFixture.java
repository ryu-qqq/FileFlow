package com.ryuqq.fileflow.domain.session.aggregate;

import com.ryuqq.fileflow.domain.session.id.SingleUploadSessionId;
import com.ryuqq.fileflow.domain.session.vo.SingleUploadSessionUpdateData;
import com.ryuqq.fileflow.domain.session.vo.UploadTargetFixture;
import java.time.Duration;
import java.time.Instant;

public class SingleUploadSessionFixture {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant EXPIRES_AT = NOW.plus(Duration.ofHours(1));

    public static SingleUploadSession aCreatedSession() {
        return SingleUploadSession.forNew(
                SingleUploadSessionId.of("single-session-001"),
                UploadTargetFixture.anUploadTarget(),
                "https://s3.presigned-url.com/test",
                "product-image",
                "commerce-service",
                EXPIRES_AT,
                NOW);
    }

    public static SingleUploadSession aCompletedSession() {
        SingleUploadSession session = aCreatedSession();
        session.complete(SingleUploadSessionUpdateData.of(1024L, "etag-123"), NOW.plusSeconds(30));
        session.pollEvents();
        return session;
    }

    public static SingleUploadSession anExpiredSession() {
        SingleUploadSession session = aCreatedSession();
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
