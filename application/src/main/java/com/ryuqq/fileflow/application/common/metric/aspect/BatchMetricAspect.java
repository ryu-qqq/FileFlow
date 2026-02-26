package com.ryuqq.fileflow.application.common.metric.aspect;

import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.common.metric.FileFlowMetrics;
import com.ryuqq.fileflow.application.common.metric.annotation.BatchMetric;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class BatchMetricAspect {

    private final FileFlowMetrics metrics;

    public BatchMetricAspect(FileFlowMetrics metrics) {
        this.metrics = metrics;
    }

    @Around("@annotation(batchMetric)")
    public Object around(ProceedingJoinPoint joinPoint, BatchMetric batchMetric) throws Throwable {
        String metricName = batchMetric.value();
        String category = batchMetric.category();

        Timer.Sample sample = metrics.startTimer();
        try {
            Object result = joinPoint.proceed();
            metrics.stopTimer(
                    sample,
                    metricName + "_duration_seconds",
                    "category",
                    category,
                    "outcome",
                    "success");
            metrics.incrementCounter(
                    metricName + "_total", "category", category, "outcome", "success");

            if (result instanceof SchedulerBatchProcessingResult batchResult) {
                metrics.recordBatchResult(metricName, category, batchResult);
            }

            return result;
        } catch (Exception e) {
            metrics.stopTimer(
                    sample,
                    metricName + "_duration_seconds",
                    "category",
                    category,
                    "outcome",
                    "error");
            metrics.incrementCounter(
                    metricName + "_total", "category", category, "outcome", "error");
            throw e;
        }
    }
}
