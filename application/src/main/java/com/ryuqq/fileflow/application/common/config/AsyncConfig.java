package com.ryuqq.fileflow.application.common.config;

import java.util.concurrent.Executor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
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
 *   <li>비동기 예외 처리
 * </ul>
 *
 * <p><strong>스레드 풀 설정</strong>:
 *
 * <ul>
 *   <li>corePoolSize: 5 (기본 스레드 수)
 *   <li>maxPoolSize: 20 (최대 스레드 수)
 *   <li>queueCapacity: 100 (대기 큐 용량)
 *   <li>threadNamePrefix: "async-" (스레드 이름 접두사)
 * </ul>
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

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
        return new SimpleAsyncUncaughtExceptionHandler();
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
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
