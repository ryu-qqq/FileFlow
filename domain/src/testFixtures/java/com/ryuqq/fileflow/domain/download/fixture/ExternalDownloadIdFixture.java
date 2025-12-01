package com.ryuqq.fileflow.domain.download.fixture;

import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import java.util.UUID;

/** ExternalDownloadId 테스트 Fixture. */
public final class ExternalDownloadIdFixture {

    private ExternalDownloadIdFixture() {}

    /** 기본 ExternalDownloadId 생성. */
    public static ExternalDownloadId defaultExternalDownloadId() {
        return ExternalDownloadId.of("00000000-0000-0000-0000-000000000001");
    }

    /** 고정된 ID로 ExternalDownloadId 생성. */
    public static ExternalDownloadId fixedExternalDownloadId() {
        return ExternalDownloadId.of("00000000-0000-0000-0000-000000000100");
    }

    /** 특정 값으로 ExternalDownloadId 생성. */
    public static ExternalDownloadId customExternalDownloadId(String value) {
        return ExternalDownloadId.of(value);
    }

    /** 특정 UUID로 ExternalDownloadId 생성. */
    public static ExternalDownloadId customExternalDownloadId(UUID value) {
        return ExternalDownloadId.of(value);
    }

    /** 신규 ExternalDownloadId 생성 (UUID v7). */
    public static ExternalDownloadId newExternalDownloadId() {
        return ExternalDownloadId.forNew();
    }
}
