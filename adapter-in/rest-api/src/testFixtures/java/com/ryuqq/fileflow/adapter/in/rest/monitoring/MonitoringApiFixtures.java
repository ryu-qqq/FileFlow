package com.ryuqq.fileflow.adapter.in.rest.monitoring;

import com.ryuqq.fileflow.adapter.in.rest.monitoring.dto.response.OutboxQueueStatusApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.monitoring.dto.response.OutboxStatusApiResponse;
import com.ryuqq.fileflow.application.monitoring.dto.query.OutboxStatusSearchParams;
import com.ryuqq.fileflow.application.monitoring.dto.response.OutboxQueueStatusResponse;
import com.ryuqq.fileflow.application.monitoring.dto.response.OutboxStatusResponse;
import com.ryuqq.fileflow.domain.common.vo.DateRange;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Monitoring API 테스트 Fixtures.
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
public final class MonitoringApiFixtures {

    private MonitoringApiFixtures() {}

    // ===== 공통 상수 =====
    public static final long DOWNLOAD_PENDING = 5L;
    public static final long DOWNLOAD_SENT = 1000L;
    public static final long DOWNLOAD_FAILED = 2L;
    public static final long TRANSFORM_PENDING = 3L;
    public static final long TRANSFORM_SENT = 800L;
    public static final long TRANSFORM_FAILED = 1L;
    public static final Instant CHECKED_AT = Instant.parse("2026-02-20T10:00:00Z");
    public static final String CHECKED_AT_FORMATTED = "2026-02-20T19:00:00+09:00";
    public static final LocalDate START_DATE = LocalDate.of(2026, 2, 19);
    public static final LocalDate END_DATE = LocalDate.of(2026, 2, 20);

    // ===== SearchParams Fixtures =====

    public static OutboxStatusSearchParams outboxStatusSearchParams() {
        return OutboxStatusSearchParams.of(DateRange.of(START_DATE, END_DATE));
    }

    // ===== Application Response Fixtures =====

    public static OutboxQueueStatusResponse downloadQueueStatusResponse() {
        return new OutboxQueueStatusResponse(DOWNLOAD_PENDING, DOWNLOAD_SENT, DOWNLOAD_FAILED);
    }

    public static OutboxQueueStatusResponse transformQueueStatusResponse() {
        return new OutboxQueueStatusResponse(TRANSFORM_PENDING, TRANSFORM_SENT, TRANSFORM_FAILED);
    }

    public static OutboxStatusResponse outboxStatusResponse() {
        return new OutboxStatusResponse(
                downloadQueueStatusResponse(), transformQueueStatusResponse(), CHECKED_AT);
    }

    // ===== API Response Fixtures =====

    public static OutboxQueueStatusApiResponse downloadQueueStatusApiResponse() {
        return new OutboxQueueStatusApiResponse(DOWNLOAD_PENDING, DOWNLOAD_SENT, DOWNLOAD_FAILED);
    }

    public static OutboxQueueStatusApiResponse transformQueueStatusApiResponse() {
        return new OutboxQueueStatusApiResponse(
                TRANSFORM_PENDING, TRANSFORM_SENT, TRANSFORM_FAILED);
    }

    public static OutboxStatusApiResponse outboxStatusApiResponse() {
        return new OutboxStatusApiResponse(
                downloadQueueStatusApiResponse(),
                transformQueueStatusApiResponse(),
                CHECKED_AT_FORMATTED);
    }
}
