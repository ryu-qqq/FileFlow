package com.ryuqq.fileflow.domain.download.fixture;

import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadOutboxId;
import java.util.UUID;

/** ExternalDownloadOutboxId 테스트 Fixture. */
public final class ExternalDownloadOutboxIdFixture {

    private ExternalDownloadOutboxIdFixture() {}

    /** 기본 ExternalDownloadOutboxId 생성. */
    public static ExternalDownloadOutboxId defaultId() {
        return ExternalDownloadOutboxId.of("00000000-0000-0000-0000-000000000001");
    }

    /** 신규 ExternalDownloadOutboxId 생성 (UUID v7). */
    public static ExternalDownloadOutboxId newId() {
        return ExternalDownloadOutboxId.forNew();
    }

    /**
     * 특정 값으로 ExternalDownloadOutboxId 생성.
     *
     * @param value ID 값 (UUID 문자열)
     */
    public static ExternalDownloadOutboxId withValue(String value) {
        return ExternalDownloadOutboxId.of(value);
    }

    /**
     * 특정 UUID로 ExternalDownloadOutboxId 생성.
     *
     * @param value UUID 값
     */
    public static ExternalDownloadOutboxId withValue(UUID value) {
        return ExternalDownloadOutboxId.of(value);
    }
}
