package com.ryuqq.fileflow.adapter.in.scheduler.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 스케줄러 작업을 표시하는 어노테이션.
 *
 * <p>이 어노테이션이 붙은 메서드는 SchedulerLoggingAspect에 의해:
 *
 * <ul>
 *   <li>TraceId 자동 생성 (MDC 설정)
 *   <li>시작/종료 로깅
 *   <li>BatchProcessingResult 기반 성공/실패 로깅
 * </ul>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SchedulerJob {

    /** 스케줄러 작업명. 로깅에 사용됩니다. */
    String value();
}
