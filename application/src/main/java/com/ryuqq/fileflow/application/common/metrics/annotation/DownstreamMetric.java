package com.ryuqq.fileflow.application.common.metrics.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Downstream 시스템 메트릭 수집을 위한 어노테이션.
 *
 * <p>외부 시스템(S3, Redis, HTTP) 호출 메트릭을 AOP로 수집합니다.
 *
 * <p>수집 메트릭:
 *
 * <ul>
 *   <li>downstream.{target}.latency - 외부 시스템 레이턴시
 * </ul>
 *
 * <p>사용 예시:
 *
 * <pre>{@code
 * @DownstreamMetric(target = "s3", operation = "upload")
 * public void uploadObject(Bucket bucket, S3Key key, byte[] content) {
 *     // S3 호출 로직
 * }
 *
 * @DownstreamMetric(target = "redis", operation = "set")
 * public void set(CacheKey key, Object value, Duration ttl) {
 *     // Redis 호출 로직
 * }
 *
 * @DownstreamMetric(target = "external-api", operation = "download", service = "cdn")
 * public byte[] download(String url) {
 *     // HTTP 호출 로직
 * }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DownstreamMetric {

    /**
     * 대상 시스템 유형.
     *
     * <p>예: s3, redis, external-api
     */
    String target();

    /**
     * 작업 유형.
     *
     * <p>예: upload, download, set, get, delete
     */
    String operation();

    /**
     * 서비스명 (external-api 타입에서 사용).
     *
     * <p>예: cdn, webhook
     */
    String service() default "";

    /**
     * 엔드포인트명 (external-api 타입에서 사용).
     *
     * <p>예: download, notify
     */
    String endpoint() default "";
}
