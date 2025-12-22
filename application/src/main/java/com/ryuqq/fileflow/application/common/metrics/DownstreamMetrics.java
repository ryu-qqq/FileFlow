package com.ryuqq.fileflow.application.common.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

/**
 * Downstream 서비스 Latency 메트릭 수집기.
 *
 * <p>외부 의존성(Redis, DB, S3, SQS, 외부 API) 호출 latency를 측정하여 병목을 감지합니다.
 *
 * <p><strong>수집 메트릭</strong>:
 *
 * <ul>
 *   <li>{@code downstream.redis.latency} - Redis 작업 latency
 *   <li>{@code downstream.db.latency} - Database 쿼리 latency
 *   <li>{@code downstream.s3.latency} - S3 작업 latency
 *   <li>{@code downstream.sqs.publish.latency} - SQS 메시지 발행 latency
 *   <li>{@code downstream.external.api.latency} - 외부 API 호출 latency
 * </ul>
 *
 * <p><strong>Grafana/Prometheus 쿼리 예시</strong>:
 *
 * <pre>{@code
 * # Redis 평균 latency by operation
 * avg by (operation) (
 *   rate(downstream_redis_latency_seconds_sum[5m])
 *   / rate(downstream_redis_latency_seconds_count[5m])
 * )
 *
 * # S3 업로드 p99 latency
 * histogram_quantile(0.99,
 *   rate(downstream_s3_latency_seconds_bucket{operation="upload"}[5m])
 * )
 *
 * # DB 쿼리 latency by table
 * sum by (table) (
 *   rate(downstream_db_latency_seconds_sum[5m])
 * )
 * }</pre>
 */
@Component
public class DownstreamMetrics {

    private static final String METRIC_PREFIX = "downstream";

    private final MeterRegistry meterRegistry;

    public DownstreamMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Redis 작업 latency를 기록합니다.
     *
     * @param operation 작업 유형 (get, set, delete, lock, unlock)
     * @param durationMs 소요 시간 (밀리초)
     */
    public void recordRedisLatency(String operation, long durationMs) {
        Timer.builder(METRIC_PREFIX + ".redis.latency")
                .description("Redis operation latency")
                .tag("operation", operation)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Database 쿼리 latency를 기록합니다.
     *
     * @param operation 작업 유형 (select, insert, update, delete)
     * @param table 테이블명
     * @param durationMs 소요 시간 (밀리초)
     */
    public void recordDbLatency(String operation, String table, long durationMs) {
        Timer.builder(METRIC_PREFIX + ".db.latency")
                .description("Database query latency")
                .tag("operation", operation)
                .tag("table", table)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * S3 작업 latency를 기록합니다.
     *
     * @param operation 작업 유형 (upload, download, presign, delete)
     * @param durationMs 소요 시간 (밀리초)
     */
    public void recordS3Latency(String operation, long durationMs) {
        Timer.builder(METRIC_PREFIX + ".s3.latency")
                .description("S3 operation latency")
                .tag("operation", operation)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * SQS 메시지 발행 latency를 기록합니다.
     *
     * @param queue 큐 이름
     * @param durationMs 소요 시간 (밀리초)
     */
    public void recordSqsPublishLatency(String queue, long durationMs) {
        Timer.builder(METRIC_PREFIX + ".sqs.publish.latency")
                .description("SQS message publish latency")
                .tag("queue", queue)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * SQS 메시지 소비 latency를 기록합니다.
     *
     * @param queue 큐 이름
     * @param durationMs 소요 시간 (밀리초)
     */
    public void recordSqsConsumeLatency(String queue, long durationMs) {
        Timer.builder(METRIC_PREFIX + ".sqs.consume.latency")
                .description("SQS message consume latency")
                .tag("queue", queue)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 외부 API 호출 latency를 기록합니다.
     *
     * @param service 서비스명
     * @param endpoint 엔드포인트
     * @param durationMs 소요 시간 (밀리초)
     */
    public void recordExternalApiLatency(String service, String endpoint, long durationMs) {
        Timer.builder(METRIC_PREFIX + ".external.api.latency")
                .description("External API call latency")
                .tag("service", service)
                .tag("endpoint", endpoint)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Timer.Sample을 시작합니다.
     *
     * @return Timer.Sample
     */
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * Timer.Sample을 종료하고 Redis latency로 기록합니다.
     *
     * @param sample Timer.Sample
     * @param operation 작업 유형
     */
    public void stopRedisTimer(Timer.Sample sample, String operation) {
        sample.stop(
                Timer.builder(METRIC_PREFIX + ".redis.latency")
                        .description("Redis operation latency")
                        .tag("operation", operation)
                        .publishPercentiles(0.5, 0.95, 0.99)
                        .register(meterRegistry));
    }

    /**
     * Timer.Sample을 종료하고 S3 latency로 기록합니다.
     *
     * @param sample Timer.Sample
     * @param operation 작업 유형
     */
    public void stopS3Timer(Timer.Sample sample, String operation) {
        sample.stop(
                Timer.builder(METRIC_PREFIX + ".s3.latency")
                        .description("S3 operation latency")
                        .tag("operation", operation)
                        .publishPercentiles(0.5, 0.95, 0.99)
                        .register(meterRegistry));
    }

    /**
     * Timer.Sample을 종료하고 External API latency로 기록합니다.
     *
     * @param sample Timer.Sample
     * @param service 서비스명
     * @param endpoint 엔드포인트
     */
    public void stopExternalApiTimer(Timer.Sample sample, String service, String endpoint) {
        sample.stop(
                Timer.builder(METRIC_PREFIX + ".external.api.latency")
                        .description("External API call latency")
                        .tag("service", service)
                        .tag("endpoint", endpoint)
                        .publishPercentiles(0.5, 0.95, 0.99)
                        .register(meterRegistry));
    }
}
