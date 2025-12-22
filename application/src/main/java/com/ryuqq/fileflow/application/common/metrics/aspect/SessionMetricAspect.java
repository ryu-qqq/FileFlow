package com.ryuqq.fileflow.application.common.metrics.aspect;

import com.ryuqq.fileflow.application.common.metrics.SessionMetrics;
import com.ryuqq.fileflow.application.common.metrics.annotation.SessionMetric;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Session 메트릭 수집 Aspect.
 *
 * <p>{@link SessionMetric} 어노테이션이 적용된 메서드의 메트릭을 자동으로 수집합니다.
 *
 * <p>수집 항목:
 *
 * <ul>
 *   <li>작업 성공 시: session.{operation}.count 증가, session.duration 기록
 *   <li>작업 실패 시: session.abort.count 증가 (recordAbortOnFailure=true 시)
 * </ul>
 */
@Aspect
@Component
public class SessionMetricAspect {

    private static final Logger log = LoggerFactory.getLogger(SessionMetricAspect.class);

    private final SessionMetrics sessionMetrics;

    public SessionMetricAspect(SessionMetrics sessionMetrics) {
        this.sessionMetrics = sessionMetrics;
    }

    @Around("@annotation(sessionMetric)")
    public Object recordSessionMetric(ProceedingJoinPoint joinPoint, SessionMetric sessionMetric)
            throws Throwable {
        String operation = sessionMetric.operation();
        String type = sessionMetric.type();

        Timer.Sample sample = sessionMetrics.startTimer();

        try {
            Object result = joinPoint.proceed();

            recordSuccessMetric(operation, type);
            sessionMetrics.stopTimer(sample, operation, type);

            return result;
        } catch (Throwable e) {
            if (sessionMetric.recordAbortOnFailure()) {
                sessionMetrics.recordSessionAbort(type, e.getClass().getSimpleName());
            }
            sessionMetrics.stopTimer(sample, operation + "-failed", type);

            log.debug(
                    "Session metric recorded for failed operation: operation={}, type={}, error={}",
                    operation,
                    type,
                    e.getMessage());

            throw e;
        }
    }

    private void recordSuccessMetric(String operation, String type) {
        switch (operation) {
            case "initiate" -> sessionMetrics.recordSessionInitiate(type);
            case "complete" -> sessionMetrics.recordSessionComplete(type);
            case "abort", "cancel" -> sessionMetrics.recordSessionAbort(type, "user-cancelled");
            default -> log.debug("Unknown session operation: {}", operation);
        }
    }
}
