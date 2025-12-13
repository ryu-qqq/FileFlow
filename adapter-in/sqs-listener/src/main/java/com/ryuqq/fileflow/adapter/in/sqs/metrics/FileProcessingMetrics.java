package com.ryuqq.fileflow.adapter.in.sqs.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

/**
 * File Processing 메트릭 수집기.
 *
 * <p>SQS Listener의 파일 가공 처리 관련 메트릭을 수집합니다.
 *
 * <p><strong>수집 메트릭</strong>:
 *
 * <ul>
 *   <li>{@code file.processing.total} - 총 처리 요청 수
 *   <li>{@code file.processing.success} - 성공 처리 수
 *   <li>{@code file.processing.failure} - 실패 처리 수
 *   <li>{@code file.processing.lock.skipped} - 락 획득 실패로 스킵된 수
 *   <li>{@code file.processing.duration} - 파일 가공 처리 시간 (Timer)
 *   <li>{@code file.processing.lock.duration} - 락 점유 시간 (Timer)
 * </ul>
 */
@Component
public class FileProcessingMetrics {

    private static final String METRIC_PREFIX = "file.processing";

    private final Counter totalCounter;
    private final Counter successCounter;
    private final Counter failureCounter;
    private final Counter lockSkippedCounter;
    private final Timer processingTimer;
    private final Timer lockTimer;

    public FileProcessingMetrics(MeterRegistry meterRegistry) {
        this.totalCounter =
                Counter.builder(METRIC_PREFIX + ".total")
                        .description("Total file processing requests received")
                        .register(meterRegistry);

        this.successCounter =
                Counter.builder(METRIC_PREFIX + ".success")
                        .description("Successful file processing completions")
                        .register(meterRegistry);

        this.failureCounter =
                Counter.builder(METRIC_PREFIX + ".failure")
                        .description("Failed file processing attempts")
                        .register(meterRegistry);

        this.lockSkippedCounter =
                Counter.builder(METRIC_PREFIX + ".lock.skipped")
                        .description("File processing skipped due to lock contention")
                        .register(meterRegistry);

        this.processingTimer =
                Timer.builder(METRIC_PREFIX + ".duration")
                        .description("File processing duration")
                        .publishPercentiles(0.5, 0.95, 0.99)
                        .register(meterRegistry);

        this.lockTimer =
                Timer.builder(METRIC_PREFIX + ".lock.duration")
                        .description("Lock holding duration for file processing")
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
     * 처리 시간 기록.
     *
     * @param durationMs 처리 시간 (밀리초)
     */
    public void recordProcessingDuration(long durationMs) {
        processingTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 락 점유 시간 기록.
     *
     * @param durationMs 락 점유 시간 (밀리초)
     */
    public void recordLockDuration(long durationMs) {
        lockTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }
}
