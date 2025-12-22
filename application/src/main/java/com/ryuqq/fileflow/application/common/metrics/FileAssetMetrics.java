package com.ryuqq.fileflow.application.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

/**
 * 파일 Asset 메트릭 수집기.
 *
 * <p>Asset 생성, 복사, 변환 작업의 성능을 추적합니다.
 *
 * <p><strong>수집 메트릭</strong>:
 *
 * <ul>
 *   <li>{@code asset.create.total} - Asset 생성 횟수
 *   <li>{@code asset.copy.total} - Asset 복사 횟수
 *   <li>{@code asset.replace.total} - Asset 소스 교체 횟수
 *   <li>{@code asset.delete.total} - Asset 삭제 횟수
 *   <li>{@code asset.duration} - Asset 작업 시간 (Timer)
 *   <li>{@code asset.bytes.total} - 처리된 총 바이트 수
 * </ul>
 *
 * <p><strong>Grafana/Prometheus 쿼리 예시</strong>:
 *
 * <pre>{@code
 * # Asset 생성 비율 by 타입
 * sum(rate(asset_create_total[5m])) by (asset_type)
 *
 * # Asset 평균 처리 시간
 * rate(asset_duration_seconds_sum[5m])
 * / rate(asset_duration_seconds_count[5m])
 *
 * # Asset 처리량 (bytes/sec)
 * rate(asset_bytes_total[5m])
 * }</pre>
 */
@Component
public class FileAssetMetrics {

    private static final String METRIC_PREFIX = "asset";

    private final MeterRegistry meterRegistry;

    public FileAssetMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Asset 생성을 기록합니다.
     *
     * @param assetType Asset 타입 (IMAGE, VIDEO, DOCUMENT, OTHER)
     */
    public void recordAssetCreate(String assetType) {
        Counter.builder(METRIC_PREFIX + ".create.total")
                .description("Total asset creations")
                .tag("asset_type", assetType)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Asset 복사를 기록합니다.
     *
     * @param assetType Asset 타입
     */
    public void recordAssetCopy(String assetType) {
        Counter.builder(METRIC_PREFIX + ".copy.total")
                .description("Total asset copies")
                .tag("asset_type", assetType)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Asset 소스 교체를 기록합니다.
     *
     * @param assetType Asset 타입
     */
    public void recordAssetReplace(String assetType) {
        Counter.builder(METRIC_PREFIX + ".replace.total")
                .description("Total asset source replacements")
                .tag("asset_type", assetType)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Asset 삭제를 기록합니다.
     *
     * @param assetType Asset 타입
     */
    public void recordAssetDelete(String assetType) {
        Counter.builder(METRIC_PREFIX + ".delete.total")
                .description("Total asset deletions")
                .tag("asset_type", assetType)
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
     * Asset 작업 시간을 기록합니다.
     *
     * @param sample Timer.Sample
     * @param operation 작업 유형 (create, copy, replace, delete, download)
     * @param assetType Asset 타입
     */
    public void stopTimer(Timer.Sample sample, String operation, String assetType) {
        sample.stop(
                Timer.builder(METRIC_PREFIX + ".duration")
                        .description("Asset operation duration")
                        .tag("operation", operation)
                        .tag("asset_type", assetType)
                        .publishPercentiles(0.5, 0.95, 0.99)
                        .register(meterRegistry));
    }

    /**
     * Asset 작업 시간을 직접 기록합니다.
     *
     * @param operation 작업 유형
     * @param assetType Asset 타입
     * @param durationMs 소요 시간 (밀리초)
     */
    public void recordDuration(String operation, String assetType, long durationMs) {
        Timer.builder(METRIC_PREFIX + ".duration")
                .description("Asset operation duration")
                .tag("operation", operation)
                .tag("asset_type", assetType)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 처리된 바이트 수를 기록합니다.
     *
     * @param assetType Asset 타입
     * @param bytes 바이트 수
     */
    public void recordBytes(String assetType, long bytes) {
        Counter.builder(METRIC_PREFIX + ".bytes.total")
                .description("Total bytes processed")
                .tag("asset_type", assetType)
                .register(meterRegistry)
                .increment(bytes);
    }

    /**
     * Presigned URL 생성을 기록합니다.
     *
     * @param assetType Asset 타입
     */
    public void recordPresignedUrlGeneration(String assetType) {
        Counter.builder(METRIC_PREFIX + ".presigned.url.total")
                .description("Total presigned URL generations")
                .tag("asset_type", assetType)
                .register(meterRegistry)
                .increment();
    }
}
