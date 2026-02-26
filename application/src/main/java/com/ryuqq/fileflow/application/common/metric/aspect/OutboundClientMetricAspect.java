package com.ryuqq.fileflow.application.common.metric.aspect;

import com.ryuqq.fileflow.application.common.metric.FileFlowMetrics;
import com.ryuqq.fileflow.application.common.metric.annotation.OutboundClientMetric;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class OutboundClientMetricAspect {

    private final FileFlowMetrics metrics;

    public OutboundClientMetricAspect(FileFlowMetrics metrics) {
        this.metrics = metrics;
    }

    @Around("@annotation(outboundClientMetric)")
    public Object around(ProceedingJoinPoint joinPoint, OutboundClientMetric outboundClientMetric)
            throws Throwable {
        String system = outboundClientMetric.system();
        String operation = outboundClientMetric.operation();

        Timer.Sample sample = metrics.startTimer();
        try {
            Object result = joinPoint.proceed();
            metrics.stopTimer(
                    sample,
                    "outbound_client_duration_seconds",
                    "system",
                    system,
                    "operation",
                    operation,
                    "outcome",
                    "success");
            metrics.incrementCounter(
                    "outbound_client_total",
                    "system",
                    system,
                    "operation",
                    operation,
                    "outcome",
                    "success");
            return result;
        } catch (Exception e) {
            metrics.stopTimer(
                    sample,
                    "outbound_client_duration_seconds",
                    "system",
                    system,
                    "operation",
                    operation,
                    "outcome",
                    "error");
            metrics.incrementCounter(
                    "outbound_client_total",
                    "system",
                    system,
                    "operation",
                    operation,
                    "outcome",
                    "error");
            metrics.incrementCounter(
                    "outbound_client_errors_total",
                    "system",
                    system,
                    "operation",
                    operation,
                    "exception",
                    e.getClass().getSimpleName());
            throw e;
        }
    }
}
