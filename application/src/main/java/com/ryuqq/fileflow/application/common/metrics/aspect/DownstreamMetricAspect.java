package com.ryuqq.fileflow.application.common.metrics.aspect;

import com.ryuqq.fileflow.application.common.metrics.DownstreamMetrics;
import com.ryuqq.fileflow.application.common.metrics.annotation.DownstreamMetric;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Downstream 시스템 메트릭 수집 Aspect.
 *
 * <p>{@link DownstreamMetric} 어노테이션이 적용된 메서드의 메트릭을 자동으로 수집합니다.
 *
 * <p>지원 대상:
 * <ul>
 *   <li>s3: S3 작업 레이턴시</li>
 *   <li>redis: Redis 작업 레이턴시</li>
 *   <li>external-api: 외부 API 호출 레이턴시</li>
 * </ul>
 */
@Aspect
@Component
public class DownstreamMetricAspect {

    private static final Logger log = LoggerFactory.getLogger(DownstreamMetricAspect.class);

    private final DownstreamMetrics downstreamMetrics;

    public DownstreamMetricAspect(DownstreamMetrics downstreamMetrics) {
        this.downstreamMetrics = downstreamMetrics;
    }

    @Around("@annotation(downstreamMetric)")
    public Object recordDownstreamMetric(
            ProceedingJoinPoint joinPoint, DownstreamMetric downstreamMetric) throws Throwable {
        String target = downstreamMetric.target();
        String operation = downstreamMetric.operation();

        Timer.Sample sample = downstreamMetrics.startTimer();

        try {
            Object result = joinPoint.proceed();
            stopTimer(sample, target, operation, downstreamMetric);
            return result;
        } catch (Throwable e) {
            stopTimer(sample, target, operation + "-failed", downstreamMetric);
            throw e;
        }
    }

    private void stopTimer(
            Timer.Sample sample, String target, String operation, DownstreamMetric annotation) {
        switch (target) {
            case "s3" -> downstreamMetrics.stopS3Timer(sample, operation);
            case "redis" -> downstreamMetrics.stopRedisTimer(sample, operation);
            case "external-api" -> {
                String service = annotation.service().isEmpty() ? "unknown" : annotation.service();
                String endpoint =
                        annotation.endpoint().isEmpty() ? operation : annotation.endpoint();
                downstreamMetrics.stopExternalApiTimer(sample, service, endpoint);
            }
            default -> log.warn("Unknown downstream target: {}", target);
        }
    }
}
