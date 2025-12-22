package com.ryuqq.fileflow.application.common.metrics.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Session 메트릭 수집을 위한 어노테이션.
 *
 * <p>세션 라이프사이클 메트릭을 AOP로 수집합니다.
 *
 * <p>수집 메트릭:
 *
 * <ul>
 *   <li>session.{operation}.count - 작업 카운터
 *   <li>session.duration - 작업 소요시간
 * </ul>
 *
 * <p>사용 예시:
 *
 * <pre>{@code
 * @SessionMetric(operation = "complete", type = "single")
 * public CompleteSingleUploadResponse execute(CompleteSingleUploadCommand command) {
 *     // 비즈니스 로직만 작성
 * }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SessionMetric {

    /**
     * 세션 작업 유형.
     *
     * <p>예: initiate, complete, abort
     */
    String operation();

    /**
     * 세션 타입.
     *
     * <p>예: single, multipart
     */
    String type();

    /** 실패 시 abort 메트릭 기록 여부. */
    boolean recordAbortOnFailure() default true;
}
