package com.ryuqq.fileflow.application.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

/**
 * 업로드 세션 메트릭 수집기.
 *
 * <p>업로드 세션의 생명주기와 성능을 추적합니다.
 *
 * <p><strong>수집 메트릭</strong>:
 *
 * <ul>
 *   <li>{@code session.initiate.total} - 세션 생성 횟수
 *   <li>{@code session.complete.total} - 세션 완료 횟수
 *   <li>{@code session.abort.total} - 세션 취소 횟수
 *   <li>{@code session.part.upload.total} - 파트 업로드 횟수
 *   <li>{@code session.duration} - 세션 처리 시간 (Timer)
 *   <li>{@code session.active} - 현재 활성 세션 수 (Gauge)
 * </ul>
 *
 * <p><strong>Grafana/Prometheus 쿼리 예시</strong>:
 *
 * <pre>{@code
 * # 세션 완료율
 * sum(rate(session_complete_total[5m])) by (type)
 * / sum(rate(session_initiate_total[5m])) by (type)
 *
 * # 세션 평균 처리 시간
 * rate(session_duration_seconds_sum{operation="complete"}[5m])
 * / rate(session_duration_seconds_count{operation="complete"}[5m])
 *
 * # 세션 실패 알림 (5분간 취소율 > 10%)
 * sum(rate(session_abort_total[5m])) by (type)
 * / sum(rate(session_initiate_total[5m])) by (type) > 0.1
 * }</pre>
 */
@Component
public class SessionMetrics {

    private static final String METRIC_PREFIX = "session";

    private final MeterRegistry meterRegistry;

    public SessionMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * 세션 생성을 기록합니다.
     *
     * @param sessionType 세션 타입 (single, multipart)
     */
    public void recordSessionInitiate(String sessionType) {
        Counter.builder(METRIC_PREFIX + ".initiate.total")
                .description("Total session initiations")
                .tag("type", sessionType)
                .register(meterRegistry)
                .increment();
    }

    /**
     * 세션 완료를 기록합니다.
     *
     * @param sessionType 세션 타입 (single, multipart)
     */
    public void recordSessionComplete(String sessionType) {
        Counter.builder(METRIC_PREFIX + ".complete.total")
                .description("Total session completions")
                .tag("type", sessionType)
                .register(meterRegistry)
                .increment();
    }

    /**
     * 세션 취소를 기록합니다.
     *
     * @param sessionType 세션 타입 (single, multipart)
     * @param reason 취소 사유
     */
    public void recordSessionAbort(String sessionType, String reason) {
        Counter.builder(METRIC_PREFIX + ".abort.total")
                .description("Total session aborts")
                .tag("type", sessionType)
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }

    /**
     * 파트 업로드를 기록합니다.
     *
     * @param partNumber 파트 번호
     */
    public void recordPartUpload(int partNumber) {
        Counter.builder(METRIC_PREFIX + ".part.upload.total")
                .description("Total part uploads")
                .register(meterRegistry)
                .increment();
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
     * 세션 작업 시간을 기록합니다.
     *
     * @param sample Timer.Sample
     * @param operation 작업 유형 (initiate, complete, abort, part-upload)
     * @param sessionType 세션 타입 (single, multipart)
     */
    public void stopTimer(Timer.Sample sample, String operation, String sessionType) {
        sample.stop(
                Timer.builder(METRIC_PREFIX + ".duration")
                        .description("Session operation duration")
                        .tag("operation", operation)
                        .tag("type", sessionType)
                        .publishPercentiles(0.5, 0.95, 0.99)
                        .register(meterRegistry));
    }

    /**
     * 세션 작업 시간을 직접 기록합니다.
     *
     * @param operation 작업 유형
     * @param sessionType 세션 타입
     * @param durationMs 소요 시간 (밀리초)
     */
    public void recordDuration(String operation, String sessionType, long durationMs) {
        Timer.builder(METRIC_PREFIX + ".duration")
                .description("Session operation duration")
                .tag("operation", operation)
                .tag("type", sessionType)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 세션 크기(바이트)를 기록합니다.
     *
     * @param sessionType 세션 타입
     * @param bytes 바이트 수
     */
    public void recordSessionBytes(String sessionType, long bytes) {
        Counter.builder(METRIC_PREFIX + ".bytes.total")
                .description("Total bytes uploaded through sessions")
                .tag("type", sessionType)
                .register(meterRegistry)
                .increment(bytes);
    }
}
