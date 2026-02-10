package com.ryuqq.fileflow.adapter.in.scheduler.aspect;

import com.ryuqq.fileflow.adapter.in.scheduler.annotation.SchedulerJob;
import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * 스케줄러 작업에 대한 AOP Aspect.
 *
 * <p>@SchedulerJob 어노테이션이 붙은 메서드에 대해:
 *
 * <ul>
 *   <li>TraceId 자동 생성 및 MDC 설정
 *   <li>작업 시작/종료 로깅
 *   <li>BatchProcessingResult 기반 요약 로깅
 *   <li>예외 발생 시 에러 로깅 (Sentry 자동 전송)
 * </ul>
 */
@Aspect
@Component
public class SchedulerLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(SchedulerLoggingAspect.class);
    private static final String TRACE_ID_KEY = "traceId";

    @Around("@annotation(schedulerJob)")
    public Object around(ProceedingJoinPoint joinPoint, SchedulerJob schedulerJob)
            throws Throwable {
        String jobName = schedulerJob.value();
        String traceId = generateTraceId();

        MDC.put(TRACE_ID_KEY, traceId);
        try {
            log.info("[{}] 스케줄러 작업 시작", jobName);

            Object result = joinPoint.proceed();

            logResult(jobName, result);

            return result;
        } catch (Exception e) {
            log.error("[{}] 스케줄러 작업 실패 - error: {}", jobName, e.getMessage(), e);
            throw e;
        } finally {
            MDC.remove(TRACE_ID_KEY);
        }
    }

    private String generateTraceId() {
        return "scheduler-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private void logResult(String jobName, Object result) {
        if (result instanceof SchedulerBatchProcessingResult batchResult) {
            logBatchResult(jobName, batchResult);
        } else {
            log.info("[{}] 스케줄러 작업 완료", jobName);
        }
    }

    private void logBatchResult(String jobName, SchedulerBatchProcessingResult result) {
        if (result.total() == 0) {
            log.info("[{}] 스케줄러 작업 완료 - 처리 대상 없음", jobName);
            return;
        }

        if (result.hasFailures()) {
            log.warn(
                    "[{}] 스케줄러 작업 완료 - total: {}, success: {}, failed: {}",
                    jobName,
                    result.total(),
                    result.success(),
                    result.failed());
        } else {
            log.info(
                    "[{}] 스케줄러 작업 완료 - total: {}, success: {}",
                    jobName,
                    result.total(),
                    result.success());
        }
    }
}
