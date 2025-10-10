package com.ryuqq.fileflow.application.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;

/**
 * CloudWatch 메트릭을 전송하는 공통 재시도 리스너
 *
 * 재시도 시작, 재시도 횟수, 재시도 간격 등을 로깅하고
 * CloudWatch 메트릭으로 전송합니다.
 *
 * @author sangwon-ryu
 */
public class MetricsRetryListener implements RetryListener {

    private static final Logger logger = LoggerFactory.getLogger(MetricsRetryListener.class);

    private final MeterRegistry meterRegistry;
    private final String serviceName;
    private final int maxAttempts;

    /**
     * Constructor
     *
     * @param meterRegistry CloudWatch 메트릭 전송을 위한 MeterRegistry
     * @param serviceName 서비스 이름 (메트릭 태그용)
     * @param maxAttempts 최대 재시도 횟수
     */
    public MetricsRetryListener(MeterRegistry meterRegistry, String serviceName, int maxAttempts) {
        this.meterRegistry = meterRegistry;
        this.serviceName = serviceName;
        this.maxAttempts = maxAttempts;
    }

    @Override
    public <T, E extends Throwable> void onError(
            RetryContext context,
            RetryCallback<T, E> callback,
            Throwable throwable) {

        int retryCount = context.getRetryCount();
        String exceptionType = throwable.getClass().getSimpleName();

        logger.warn("{} processing failed (attempt {}/{}): {} - {}",
                serviceName,
                retryCount,
                maxAttempts,
                exceptionType,
                throwable.getMessage());

        // CloudWatch 메트릭 전송
        if (meterRegistry != null) {
            meterRegistry.counter(serviceName + ".retry.attempts",
                    "exception", exceptionType,
                    "attempt", String.valueOf(retryCount)
            ).increment();
        }
    }

    @Override
    public <T, E extends Throwable> void close(
            RetryContext context,
            RetryCallback<T, E> callback,
            Throwable throwable) {

        if (throwable != null) {
            // 최종 실패
            logger.error("{} processing finally failed after {} attempts: {}",
                    serviceName,
                    context.getRetryCount(),
                    throwable.getMessage());

            if (meterRegistry != null) {
                meterRegistry.counter(serviceName + ".retry.exhausted",
                        "exception", throwable.getClass().getSimpleName()
                ).increment();
            }
        } else {
            // 성공
            if (context.getRetryCount() > 0) {
                logger.info("{} processing succeeded after {} retries",
                        serviceName,
                        context.getRetryCount());

                if (meterRegistry != null) {
                    meterRegistry.counter(serviceName + ".retry.success",
                            "retries", String.valueOf(context.getRetryCount())
                    ).increment();
                }
            }
        }
    }
}
