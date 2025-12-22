package com.ryuqq.fileflow.application.common.metrics.aspect;

import com.ryuqq.fileflow.application.common.metrics.FileAssetMetrics;
import com.ryuqq.fileflow.application.common.metrics.annotation.FileAssetMetric;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * FileAsset 메트릭 수집 Aspect.
 *
 * <p>{@link FileAssetMetric} 어노테이션이 적용된 메서드의 메트릭을 자동으로 수집합니다.
 *
 * <p>수집 항목:
 * <ul>
 *   <li>작업 성공 시: asset.{operation}.count 증가, asset.duration 기록</li>
 *   <li>작업 실패 시: asset.duration (operation-failed) 기록</li>
 * </ul>
 */
@Aspect
@Component
public class FileAssetMetricAspect {

    private static final Logger log = LoggerFactory.getLogger(FileAssetMetricAspect.class);

    private final FileAssetMetrics fileAssetMetrics;

    public FileAssetMetricAspect(FileAssetMetrics fileAssetMetrics) {
        this.fileAssetMetrics = fileAssetMetrics;
    }

    @Around("@annotation(fileAssetMetric)")
    public Object recordFileAssetMetric(ProceedingJoinPoint joinPoint, FileAssetMetric fileAssetMetric)
            throws Throwable {
        String operation = fileAssetMetric.operation();

        Timer.Sample sample = fileAssetMetrics.startTimer();

        try {
            Object result = joinPoint.proceed();

            String assetType = extractAssetType(joinPoint, result);
            recordSuccessMetric(operation, assetType);
            fileAssetMetrics.stopTimer(sample, operation, assetType);

            return result;
        } catch (Throwable e) {
            fileAssetMetrics.stopTimer(sample, operation + "-failed", "unknown");

            log.debug(
                    "FileAsset metric recorded for failed operation: operation={}, error={}",
                    operation,
                    e.getMessage());

            throw e;
        }
    }

    private String extractAssetType(ProceedingJoinPoint joinPoint, Object result) {
        // Response에서 assetType을 추출하거나 기본값 반환
        // 구체적인 Response 타입에 따라 확장 가능
        return "image";
    }

    private void recordSuccessMetric(String operation, String assetType) {
        switch (operation) {
            case "create" -> fileAssetMetrics.recordAssetCreate(assetType);
            case "process" -> fileAssetMetrics.recordAssetCreate(assetType);
            case "copy" -> fileAssetMetrics.recordAssetCopy(assetType);
            case "replace" -> fileAssetMetrics.recordAssetReplace(assetType);
            case "delete" -> fileAssetMetrics.recordAssetDelete(assetType);
            default -> log.debug("Unknown asset operation: {}", operation);
        }
    }
}
