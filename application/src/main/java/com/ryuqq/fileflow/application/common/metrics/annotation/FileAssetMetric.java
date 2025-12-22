package com.ryuqq.fileflow.application.common.metrics.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * FileAsset 메트릭 수집을 위한 어노테이션.
 *
 * <p>에셋 처리 메트릭을 AOP로 수집합니다.
 *
 * <p>수집 메트릭:
 *
 * <ul>
 *   <li>asset.{operation}.count - 작업 카운터
 *   <li>asset.duration - 작업 소요시간
 *   <li>asset.bytes.total - 처리된 바이트 수 (recordBytes=true 시)
 * </ul>
 *
 * <p>사용 예시:
 *
 * <pre>{@code
 * @FileAssetMetric(operation = "process", recordBytes = true)
 * public ProcessFileAssetResponse execute(ProcessFileAssetCommand command) {
 *     // 비즈니스 로직만 작성
 * }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FileAssetMetric {

    /**
     * 에셋 작업 유형.
     *
     * <p>예: create, process, copy, replace, delete
     */
    String operation();

    /**
     * 바이트 수 기록 여부.
     *
     * <p>true일 경우 Command에서 파일 크기를 추출하여 기록합니다.
     */
    boolean recordBytes() default false;
}
