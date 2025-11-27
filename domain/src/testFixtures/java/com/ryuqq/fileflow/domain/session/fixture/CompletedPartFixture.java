package com.ryuqq.fileflow.domain.session.fixture;

import com.ryuqq.fileflow.domain.common.fixture.ClockFixture;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.PartNumber;
import com.ryuqq.fileflow.domain.session.vo.PresignedUrl;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.time.Clock;
import java.time.LocalDateTime;

/**
 * CompletedPart Aggregate Test Fixture
 *
 * @author development-team
 * @since 1.0.0
 */
public class CompletedPartFixture {

    private CompletedPartFixture() {
        throw new AssertionError("Utility class");
    }

    /** 기본 CompletedPart Fixture (PENDING 상태, 신규 생성) */
    public static CompletedPart defaultCompletedPart() {
        return CompletedPart.forNew(
                UploadSessionIdFixture.defaultUploadSessionId(),
                PartNumberFixture.defaultPartNumber(),
                PresignedUrlFixture.defaultPresignedUrl(),
                ClockFixture.defaultClock());
    }

    /** 완료된 CompletedPart Fixture (COMPLETED 상태) */
    public static CompletedPart completedCompletedPart() {
        CompletedPart part =
                CompletedPart.forNew(
                        UploadSessionIdFixture.defaultUploadSessionId(),
                        PartNumberFixture.defaultPartNumber(),
                        PresignedUrlFixture.defaultPresignedUrl(),
                        ClockFixture.defaultClock());
        part.complete(ETagFixture.defaultETag(), 10 * 1024 * 1024L); // 10MB
        return part;
    }

    /** 영속화된 CompletedPart Fixture (ID 포함) */
    public static CompletedPart existingCompletedPart() {
        return CompletedPart.of(
                1L,
                UploadSessionIdFixture.defaultUploadSessionId(),
                PartNumberFixture.defaultPartNumber(),
                PresignedUrlFixture.defaultPresignedUrl(),
                ETagFixture.defaultETag(),
                10 * 1024 * 1024L,
                LocalDateTime.now(),
                ClockFixture.defaultClock());
    }

    /** Custom CompletedPart Fixture */
    public static CompletedPart customCompletedPart(
            UploadSessionId sessionId,
            PartNumber partNumber,
            PresignedUrl presignedUrl,
            Clock clock) {
        return CompletedPart.forNew(sessionId, partNumber, presignedUrl, clock);
    }

    /** Custom 완료된 CompletedPart Fixture */
    public static CompletedPart customCompletedPart(
            Long id,
            UploadSessionId sessionId,
            PartNumber partNumber,
            PresignedUrl presignedUrl,
            ETag etag,
            long size,
            LocalDateTime uploadedAt,
            Clock clock) {
        return CompletedPart.of(
                id, sessionId, partNumber, presignedUrl, etag, size, uploadedAt, clock);
    }
}
