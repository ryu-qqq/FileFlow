package com.ryuqq.fileflow.adapter.in.sqs.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

/**
 * External Download 메트릭 수집기.
 *
 * <p>SQS Listener의 다운로드 처리 관련 메트릭을 수집합니다.
 *
 * <p><strong>수집 메트릭</strong>:
 *
 * <ul>
 *   <li>{@code external.download.total} - 총 처리 요청 수
 *   <li>{@code external.download.success} - 성공 처리 수
 *   <li>{@code external.download.failure} - 실패 처리 수
 *   <li>{@code external.download.lock.skipped} - 락 획득 실패로 스킵된 수
 *   <li>{@code external.download.duration} - 다운로드 처리 시간 (Timer)
 *   <li>{@code external.download.lock.duration} - 락 점유 시간 (Timer)
 * </ul>
 *
 * <p><strong>Grafana/Prometheus 쿼리 예시</strong>:
 *
 * <pre>{@code
 * # 성공률
 * rate(external_download_success_total[5m]) / rate(external_download_total_total[5m])
 *
 * # 평균 처리 시간
 * rate(external_download_duration_seconds_sum[5m]) / rate(external_download_duration_seconds_count[5m])
 *
 * # 락 스킵 비율
 * rate(external_download_lock_skipped_total[5m]) / rate(external_download_total_total[5m])
 * }</pre>
 */
@Component
public class ExternalDownloadMetrics {

    private static final String METRIC_PREFIX = "external.download";

    private final Counter totalCounter;
    private final Counter successCounter;
    private final Counter failureCounter;
    private final Counter lockSkippedCounter;
    private final Timer downloadTimer;
    private final Timer lockTimer;

    public ExternalDownloadMetrics(MeterRegistry meterRegistry) {
        this.totalCounter =
                Counter.builder(METRIC_PREFIX + ".total")
                        .description("Total external download requests received")
                        .register(meterRegistry);

        this.successCounter =
                Counter.builder(METRIC_PREFIX + ".success")
                        .description("Successful external download completions")
                        .register(meterRegistry);

        this.failureCounter =
                Counter.builder(METRIC_PREFIX + ".failure")
                        .description("Failed external download attempts")
                        .register(meterRegistry);

        this.lockSkippedCounter =
                Counter.builder(METRIC_PREFIX + ".lock.skipped")
                        .description("External downloads skipped due to lock contention")
                        .register(meterRegistry);

        this.downloadTimer =
                Timer.builder(METRIC_PREFIX + ".duration")
                        .description("External download processing duration")
                        .publishPercentiles(0.5, 0.95, 0.99)
                        .register(meterRegistry);

        this.lockTimer =
                Timer.builder(METRIC_PREFIX + ".lock.duration")
                        .description("Lock holding duration for external downloads")
                        .publishPercentiles(0.5, 0.95, 0.99)
                        .register(meterRegistry);
    }

    /** 메시지 수신 시 호출. */
    public void recordMessageReceived() {
        totalCounter.increment();
    }

    /** 처리 성공 시 호출. */
    public void recordSuccess() {
        successCounter.increment();
    }

    /** 처리 실패 시 호출. */
    public void recordFailure() {
        failureCounter.increment();
    }

    /** 락 획득 실패로 스킵 시 호출. */
    public void recordLockSkipped() {
        lockSkippedCounter.increment();
    }

    /**
     * 다운로드 처리 시간 기록.
     *
     * @param durationMs 처리 시간 (밀리초)
     */
    public void recordDownloadDuration(long durationMs) {
        downloadTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 락 점유 시간 기록.
     *
     * @param durationMs 락 점유 시간 (밀리초)
     */
    public void recordLockDuration(long durationMs) {
        lockTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Timer.Sample을 시작합니다.
     *
     * @param meterRegistry MeterRegistry
     * @return Timer.Sample
     */
    public Timer.Sample startTimer(MeterRegistry meterRegistry) {
        return Timer.start(meterRegistry);
    }

    /**
     * 다운로드 Timer를 반환합니다.
     *
     * @return download Timer
     */
    public Timer getDownloadTimer() {
        return downloadTimer;
    }

    /**
     * 락 Timer를 반환합니다.
     *
     * @return lock Timer
     */
    public Timer getLockTimer() {
        return lockTimer;
    }
}
