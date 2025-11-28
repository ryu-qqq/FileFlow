package com.ryuqq.fileflow.application.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

/**
 * Scheduler Job 메트릭 수집기.
 *
 * <p>스케줄러 작업의 실행 현황을 추적합니다.
 *
 * <p><strong>수집 메트릭</strong>:
 *
 * <ul>
 *   <li>{@code scheduler.job.runs.total} - Job 실행 횟수
 *   <li>{@code scheduler.job.success.total} - Job 성공 횟수
 *   <li>{@code scheduler.job.failure.total} - Job 실패 횟수
 *   <li>{@code scheduler.job.duration} - Job 실행 시간 (Timer)
 *   <li>{@code scheduler.job.items.processed} - 처리된 항목 수
 * </ul>
 *
 * <p><strong>Grafana/Prometheus 쿼리 예시</strong>:
 *
 * <pre>{@code
 * # Job 성공률
 * sum(rate(scheduler_job_success_total[5m])) by (job)
 * / sum(rate(scheduler_job_runs_total[5m])) by (job)
 *
 * # Job 평균 실행 시간
 * rate(scheduler_job_duration_seconds_sum[5m])
 * / rate(scheduler_job_duration_seconds_count[5m])
 *
 * # Job 실패 알림 (5분간 3회 이상 실패)
 * sum(increase(scheduler_job_failure_total[5m])) by (job) > 3
 * }</pre>
 */
@Component
public class SchedulerMetrics {

    private static final String METRIC_PREFIX = "scheduler";

    private final MeterRegistry meterRegistry;

    public SchedulerMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Job 실행 시작을 기록하고 타이머를 시작합니다.
     *
     * @param jobName Job 이름
     * @return 타이머 샘플
     */
    public Timer.Sample startJob(String jobName) {
        Counter.builder(METRIC_PREFIX + ".job.runs.total")
                .description("Total job executions")
                .tag("job", jobName)
                .register(meterRegistry)
                .increment();

        return Timer.start(meterRegistry);
    }

    /**
     * Job 성공 완료를 기록합니다.
     *
     * @param jobName Job 이름
     * @param sample 시작 시 반환받은 타이머 샘플
     */
    public void recordJobSuccess(String jobName, Timer.Sample sample) {
        Counter.builder(METRIC_PREFIX + ".job.success.total")
                .description("Successful job executions")
                .tag("job", jobName)
                .register(meterRegistry)
                .increment();

        sample.stop(
                Timer.builder(METRIC_PREFIX + ".job.duration")
                        .description("Job execution duration")
                        .tag("job", jobName)
                        .tag("status", "success")
                        .publishPercentiles(0.5, 0.95, 0.99)
                        .register(meterRegistry));
    }

    /**
     * Job 실패를 기록합니다.
     *
     * @param jobName Job 이름
     * @param sample 시작 시 반환받은 타이머 샘플
     * @param errorType 에러 타입 (예외 클래스명)
     */
    public void recordJobFailure(String jobName, Timer.Sample sample, String errorType) {
        Counter.builder(METRIC_PREFIX + ".job.failure.total")
                .description("Failed job executions")
                .tag("job", jobName)
                .tag("error", errorType)
                .register(meterRegistry)
                .increment();

        sample.stop(
                Timer.builder(METRIC_PREFIX + ".job.duration")
                        .description("Job execution duration")
                        .tag("job", jobName)
                        .tag("status", "failure")
                        .publishPercentiles(0.5, 0.95, 0.99)
                        .register(meterRegistry));
    }

    /**
     * Job에서 처리한 항목 수를 기록합니다.
     *
     * @param jobName Job 이름
     * @param count 처리된 항목 수
     */
    public void recordJobItemsProcessed(String jobName, int count) {
        Counter.builder(METRIC_PREFIX + ".job.items.processed")
                .description("Number of items processed by job")
                .tag("job", jobName)
                .register(meterRegistry)
                .increment(count);
    }

    /**
     * Job 실행 시간을 직접 기록합니다.
     *
     * @param jobName Job 이름
     * @param durationMs 실행 시간 (밀리초)
     * @param success 성공 여부
     */
    public void recordJobDuration(String jobName, long durationMs, boolean success) {
        Timer.builder(METRIC_PREFIX + ".job.duration")
                .description("Job execution duration")
                .tag("job", jobName)
                .tag("status", success ? "success" : "failure")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }
}
