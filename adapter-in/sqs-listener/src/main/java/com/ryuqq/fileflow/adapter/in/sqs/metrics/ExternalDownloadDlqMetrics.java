package com.ryuqq.fileflow.adapter.in.sqs.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

/**
 * External Download DLQ 메트릭 수집기.
 *
 * <p>DLQ Listener의 최종 실패 처리 관련 메트릭을 수집합니다.
 *
 * <p><strong>수집 메트릭</strong>:
 *
 * <ul>
 *   <li>{@code external.download.dlq.total} - DLQ 메시지 수신 수
 *   <li>{@code external.download.dlq.processed} - 실패 처리 완료 수
 *   <li>{@code external.download.dlq.error} - DLQ 처리 중 에러 발생 수
 *   <li>{@code external.download.dlq.duration} - DLQ 처리 시간
 * </ul>
 *
 * <p><strong>Grafana/Prometheus 쿼리 예시</strong>:
 *
 * <pre>{@code
 * # DLQ 메시지 유입률
 * rate(external_download_dlq_total_total[5m])
 *
 * # DLQ 처리 성공률
 * rate(external_download_dlq_processed_total[5m]) / rate(external_download_dlq_total_total[5m])
 * }</pre>
 */
@Component
public class ExternalDownloadDlqMetrics {

    private static final String METRIC_PREFIX = "external.download.dlq";

    private final Counter totalCounter;
    private final Counter processedCounter;
    private final Counter errorCounter;
    private final Timer processingTimer;

    public ExternalDownloadDlqMetrics(MeterRegistry meterRegistry) {
        this.totalCounter =
                Counter.builder(METRIC_PREFIX + ".total")
                        .description("Total DLQ messages received")
                        .register(meterRegistry);

        this.processedCounter =
                Counter.builder(METRIC_PREFIX + ".processed")
                        .description("Successfully processed DLQ messages (marked as FAILED)")
                        .register(meterRegistry);

        this.errorCounter =
                Counter.builder(METRIC_PREFIX + ".error")
                        .description("Errors during DLQ processing")
                        .register(meterRegistry);

        this.processingTimer =
                Timer.builder(METRIC_PREFIX + ".duration")
                        .description("DLQ message processing duration")
                        .publishPercentiles(0.5, 0.95, 0.99)
                        .register(meterRegistry);
    }

    /** DLQ 메시지 수신 시 호출. */
    public void recordMessageReceived() {
        totalCounter.increment();
    }

    /** 실패 처리 완료 시 호출. */
    public void recordProcessed() {
        processedCounter.increment();
    }

    /** DLQ 처리 중 에러 발생 시 호출. */
    public void recordError() {
        errorCounter.increment();
    }

    /**
     * DLQ 처리 시간 기록.
     *
     * @param durationMs 처리 시간 (밀리초)
     */
    public void recordDuration(long durationMs) {
        processingTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }
}
