package com.ryuqq.fileflow.application.common.config;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 비동기 처리 설정.
 *
 * <p>@Async 메서드 실행을 위한 ThreadPool 설정과 MDC 컨텍스트 전파를 구성합니다.
 *
 * <p><strong>주요 기능</strong>:
 *
 * <ul>
 *   <li>MdcTaskDecorator를 통한 MDC 컨텍스트 전파
 *   <li>ThreadPool 크기 및 큐 용량 설정
 *   <li>비동기 예외 처리 및 로깅
 *   <li>RejectedExecutionHandler를 통한 큐 포화 시 대응
 * </ul>
 *
 * <p><strong>스레드 풀 설정</strong>:
 *
 * <ul>
 *   <li>corePoolSize: 5 (기본 스레드 수)
 *   <li>maxPoolSize: 20 (최대 스레드 수)
 *   <li>queueCapacity: 100 (대기 큐 용량)
 *   <li>threadNamePrefix: "async-" (스레드 이름 접두사)
 *   <li>rejectedExecutionHandler: CallerRunsPolicy (큐 포화 시 호출자 스레드에서 실행)
 * </ul>
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 20;
    private static final int QUEUE_CAPACITY = 100;
    private static final String THREAD_NAME_PREFIX = "async-";

    @Override
    public Executor getAsyncExecutor() {
        return asyncTaskExecutor();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new LoggingAsyncUncaughtExceptionHandler();
    }

    /**
     * MDC 컨텍스트를 전파하는 비동기 TaskExecutor.
     *
     * @return ThreadPoolTaskExecutor
     */
    @Bean("asyncTaskExecutor")
    public ThreadPoolTaskExecutor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.setRejectedExecutionHandler(new LoggingCallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * 비동기 예외 로깅 핸들러.
     *
     * <p>@Async 메서드에서 발생한 예외를 로깅합니다.
     */
    private class LoggingAsyncUncaughtExceptionHandler implements AsyncUncaughtExceptionHandler {

        @Override
        public void handleUncaughtException(Throwable ex, Method method, Object... params) {
            log.error("[Async Exception] method={}.{}, params={}, error={}",
                    method.getDeclaringClass().getSimpleName(),
                    method.getName(),
                    params,
                    ex.getMessage(),
                    ex);
        }
    }

    /**
     * 큐 포화 시 호출자 스레드에서 실행하는 정책 (로깅 포함).
     *
     * <p>ThreadPool 큐가 가득 찼을 때 작업을 거부하지 않고 호출자 스레드에서 실행합니다.
     * 이 정책은 시스템 과부하 시에도 작업 손실을 방지합니다.
     */
    private static class LoggingCallerRunsPolicy implements RejectedExecutionHandler {

        private static final Logger policyLog = LoggerFactory.getLogger(LoggingCallerRunsPolicy.class);

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (!executor.isShutdown()) {
                policyLog.warn("[Async Queue Full] 큐 포화로 호출자 스레드에서 실행: "
                                + "poolSize={}, activeCount={}, queueSize={}, completedTasks={}",
                        executor.getPoolSize(),
                        executor.getActiveCount(),
                        executor.getQueue().size(),
                        executor.getCompletedTaskCount());
                r.run();
            }
        }
    }
}
