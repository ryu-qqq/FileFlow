package com.ryuqq.fileflow.domain.download.fixture;

import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadOutboxId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/** ExternalDownloadOutbox 테스트 Fixture. */
public final class ExternalDownloadOutboxFixture {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2025-11-26T12:00:00Z"), ZoneId.of("UTC"));

    private ExternalDownloadOutboxFixture() {}

    /** 기본 ExternalDownloadOutbox 생성 (미발행 상태). */
    public static ExternalDownloadOutbox defaultOutbox() {
        return ExternalDownloadOutbox.forNew(
                ExternalDownloadId.of("00000000-0000-0000-0000-000000000001"), FIXED_CLOCK);
    }

    /** 미발행 ExternalDownloadOutbox 생성 (ID 있음). */
    public static ExternalDownloadOutbox unpublishedOutbox() {
        return ExternalDownloadOutbox.of(
                ExternalDownloadOutboxId.of("00000000-0000-0000-0000-000000000001"),
                ExternalDownloadId.of("00000000-0000-0000-0000-000000000001"),
                false,
                null,
                Instant.parse("2025-11-26T10:00:00Z"));
    }

    /** 발행 완료된 ExternalDownloadOutbox 생성. */
    public static ExternalDownloadOutbox publishedOutbox() {
        return ExternalDownloadOutbox.of(
                ExternalDownloadOutboxId.of("00000000-0000-0000-0000-000000000002"),
                ExternalDownloadId.of("00000000-0000-0000-0000-000000000002"),
                true,
                Instant.parse("2025-11-26T11:00:00Z"),
                Instant.parse("2025-11-26T10:00:00Z"));
    }

    /** 커스텀 ExternalDownloadOutbox 생성. */
    public static ExternalDownloadOutbox customOutbox(
            ExternalDownloadOutboxId id,
            ExternalDownloadId externalDownloadId,
            boolean published,
            Instant publishedAt) {
        return ExternalDownloadOutbox.of(
                id,
                externalDownloadId,
                published,
                publishedAt,
                Instant.parse("2025-11-26T10:00:00Z"));
    }

    /** 특정 ExternalDownloadId에 대한 Outbox 생성. */
    public static ExternalDownloadOutbox forExternalDownload(
            ExternalDownloadId externalDownloadId) {
        return ExternalDownloadOutbox.forNew(externalDownloadId, FIXED_CLOCK);
    }
}
