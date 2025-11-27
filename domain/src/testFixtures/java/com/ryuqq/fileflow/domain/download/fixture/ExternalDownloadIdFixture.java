package com.ryuqq.fileflow.domain.download.fixture;

import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;

/** ExternalDownloadId 테스트 Fixture. */
public final class ExternalDownloadIdFixture {

    private ExternalDownloadIdFixture() {}

    /** 기본 ExternalDownloadId 생성. */
    public static ExternalDownloadId defaultExternalDownloadId() {
        return ExternalDownloadId.of(1L);
    }

    /** 고정된 ID로 ExternalDownloadId 생성. */
    public static ExternalDownloadId fixedExternalDownloadId() {
        return ExternalDownloadId.of(100L);
    }

    /** 특정 값으로 ExternalDownloadId 생성. */
    public static ExternalDownloadId customExternalDownloadId(Long value) {
        return ExternalDownloadId.of(value);
    }

    /** 신규 ExternalDownloadId 생성. */
    public static ExternalDownloadId newExternalDownloadId() {
        return ExternalDownloadId.forNew();
    }
}
