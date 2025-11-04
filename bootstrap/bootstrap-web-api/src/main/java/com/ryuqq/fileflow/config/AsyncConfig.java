package com.ryuqq.fileflow.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async and Scheduling Configuration
 * 비동기 처리 및 스케줄링 설정 (ExternalDownloadWorker 최적화)
 *
 * <p><strong>설계 목표:</strong></p>
 * <ul>
 *   <li>대용량 파일 다운로드 처리 (평균 5-10분 소요)</li>
 *   <li>동시 다운로드 수 제어 (서버 리소스 보호)</li>
 *   <li>큐 대기 작업 수 제한 (OOM 방지)</li>
 *   <li>긴급 종료 시 진행 중 작업 완료 대기</li>
 * </ul>
 *
 * <p><strong>비동기 실행 대상:</strong></p>
 * <ul>
 *   <li>ExternalDownloadWorker.startDownload(): 외부 파일 다운로드 (Long-running)</li>
 * </ul>
 *
 * <p><strong>스케줄링 실행 대상:</strong></p>
 * <ul>
 *   <li>ExternalDownloadRetryScheduler: 매 10분마다 실패한 다운로드 재시도</li>
 *   <li>UploadSessionExpirationBatchJob: 매일 새벽 2시 세션 만료 처리</li>
 * </ul>
 *
 * <p><strong>Thread Pool 설정 근거:</strong></p>
 * <ul>
 *   <li>corePoolSize=5: 최소 5개 동시 다운로드 (idle 상태에서도 유지)</li>
 *   <li>maxPoolSize=20: 최대 20개 동시 다운로드 (피크 시간 대응)</li>
 *   <li>queueCapacity=100: 대기 큐 100개 (추가 요청은 RejectedExecutionException)</li>
 *   <li>keepAliveSeconds=60: idle 스레드 60초 후 종료 (리소스 절약)</li>
 *   <li>awaitTerminationSeconds=300: 종료 시 5분 대기 (진행 중 작업 완료)</li>
 * </ul>
 *
 * <p><strong>거부 정책 (RejectedExecutionHandler):</strong></p>
 * <ul>
 *   <li>기본값: AbortPolicy (RejectedExecutionException 발생)</li>
 *   <li>호출자가 예외 처리 후 재시도 또는 에러 응답</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig implements AsyncConfigurer {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    /**
     * 비동기 작업용 Executor 설정
     *
     * <p>ExternalDownloadWorker의 @Async 메서드가 이 Executor를 사용합니다.</p>
     *
     * <p><strong>성능 특성:</strong></p>
     * <ul>
     *   <li>CPU-bound 아님: HTTP 다운로드는 I/O-bound (네트워크 대기)</li>
     *   <li>Long-running: 평균 5-10분 소요 (대용량 파일)</li>
     *   <li>동시성: 20개까지 동시 다운로드 가능</li>
     *   <li>대기 큐: 100개까지 대기 (초과 시 즉시 거부)</li>
     * </ul>
     *
     * @return ThreadPoolTaskExecutor
     */
    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 기본 스레드 풀 크기 (idle 상태에서도 유지)
        executor.setCorePoolSize(5);

        // 최대 스레드 풀 크기 (피크 시간 대응)
        executor.setMaxPoolSize(20);

        // 큐 용량 (대기 작업 수 제한, OOM 방지)
        // corePoolSize 초과 시 큐에 대기, 큐 초과 시 maxPoolSize까지 스레드 생성
        executor.setQueueCapacity(100);

        // 스레드 이름 접두사 (로그 추적 용이)
        executor.setThreadNamePrefix("async-download-");

        // idle 스레드 유지 시간 (초)
        // corePoolSize 초과 스레드가 이 시간 동안 작업 없으면 종료
        executor.setKeepAliveSeconds(60);

        // 애플리케이션 종료 시 진행 중 작업 완료 대기
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 종료 대기 시간 (초)
        // 5분 대기 후에도 완료 안 되면 강제 종료
        executor.setAwaitTerminationSeconds(300);

        // 거부 정책 (큐 + maxPoolSize 초과 시)
        // 기본값: AbortPolicy (RejectedExecutionException 발생)
        // 대안: CallerRunsPolicy (호출 스레드에서 실행), DiscardPolicy (무시)
        executor.setRejectedExecutionHandler((task, exec) -> {
            log.error("Async task rejected: queue full (capacity={}), active threads={}, pool size={}",
                exec.getQueue().size(), exec.getActiveCount(), exec.getPoolSize());
            throw new java.util.concurrent.RejectedExecutionException(
                "Download task rejected: server is too busy. Please try again later."
            );
        });

        executor.initialize();

        log.info("AsyncExecutor initialized: corePoolSize={}, maxPoolSize={}, queueCapacity={}",
            executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }

    /**
     * 비동기 작업 중 발생한 예외 처리
     *
     * <p>@Async 메서드에서 발생한 예외는 호출자에게 전파되지 않으므로
     * 여기서 로깅 등 처리를 수행합니다.</p>
     *
     * <p><strong>예외 처리 전략:</strong></p>
     * <ul>
     *   <li>모든 예외를 로깅 (트러블슈팅 용이)</li>
     *   <li>ExternalDownloadWorker 내부에서 이미 예외 처리 완료 (DB 저장)</li>
     *   <li>여기서는 추가적인 알림 또는 모니터링 연동 가능</li>
     * </ul>
     *
     * @return AsyncUncaughtExceptionHandler
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            log.error("Uncaught exception in async method: {}.{}()",
                method.getDeclaringClass().getSimpleName(),
                method.getName(),
                throwable);

            // 필요시 알림 발송 또는 모니터링 시스템 연동
            // 예: Slack, Email, Prometheus Alert 등
        };
    }

}
