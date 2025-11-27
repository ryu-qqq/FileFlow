package com.ryuqq.fileflow.domain.download.fixture;

import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadOutboxId;

/** ExternalDownloadOutboxId 테스트 Fixture. */
public final class ExternalDownloadOutboxIdFixture {

    private ExternalDownloadOutboxIdFixture() {}

    /** 기본 ExternalDownloadOutboxId 생성 (ID: 1L). */
    public static ExternalDownloadOutboxId defaultId() {
        return ExternalDownloadOutboxId.of(1L);
    }

    /** 신규 ExternalDownloadOutboxId 생성 (ID: null). */
    public static ExternalDownloadOutboxId newId() {
        return ExternalDownloadOutboxId.forNew();
    }

    /**
     * 커스텀 ExternalDownloadOutboxId 생성.
     *
     * @param value ID 값
     */
    public static ExternalDownloadOutboxId withValue(Long value) {
        return ExternalDownloadOutboxId.of(value);
    }
}
