package com.ryuqq.fileflow.application.common.metric.aspect;

import com.ryuqq.fileflow.application.common.metric.FileFlowMetrics;
import com.ryuqq.fileflow.application.common.metric.annotation.BusinessMetric;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class BusinessMetricAspect {

    private final FileFlowMetrics metrics;

    public BusinessMetricAspect(FileFlowMetrics metrics) {
        this.metrics = metrics;
    }

    @Around("@annotation(businessMetric)")
    public Object around(ProceedingJoinPoint joinPoint, BusinessMetric businessMetric)
            throws Throwable {
        String metricName = businessMetric.value();
        String operation = businessMetric.operation();

        Timer.Sample sample = metrics.startTimer();
        try {
            Object result = joinPoint.proceed();
            metrics.stopTimer(
                    sample,
                    metricName + "_duration_seconds",
                    "operation",
                    operation,
                    "outcome",
                    "success");
            metrics.incrementCounter(
                    metricName + "_total", "operation", operation, "outcome", "success");
            return result;
        } catch (Exception e) {
            metrics.stopTimer(
                    sample,
                    metricName + "_duration_seconds",
                    "operation",
                    operation,
                    "outcome",
                    "error");
            metrics.incrementCounter(
                    metricName + "_total", "operation", operation, "outcome", "error");
            metrics.incrementCounter(
                    metricName + "_errors_total",
                    "operation",
                    operation,
                    "exception",
                    e.getClass().getSimpleName());
            throw e;
        }
    }
}
