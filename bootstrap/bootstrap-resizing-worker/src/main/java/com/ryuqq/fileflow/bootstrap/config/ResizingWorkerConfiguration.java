package com.ryuqq.fileflow.bootstrap.config;

import java.util.concurrent.ForkJoinPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 리사이징 워커 설정.
 *
 * <p>CPU 바운드 이미지 리사이징 작업을 위한 ForkJoinPool을 구성합니다.
 *
 * <p><strong>설계 원칙</strong>:
 *
 * <ul>
 *   <li>Platform Thread 사용 (Virtual Thread 금지)
 *   <li>커스텀 ForkJoinPool (parallelStream에서 사용)
 *   <li>외부 설정 기반 구성
 * </ul>
 */
@Configuration
@EnableConfigurationProperties(ResizingWorkerProperties.class)
public class ResizingWorkerConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ResizingWorkerConfiguration.class);

    /**
     * 리사이징 작업용 ForkJoinPool 빈.
     *
     * <p>parallelStream()에서 사용되는 커스텀 스레드 풀입니다.
     *
     * @param properties 워커 설정 프로퍼티
     * @return ForkJoinPool
     */
    @Bean(destroyMethod = "shutdown")
    public ForkJoinPool resizingForkJoinPool(ResizingWorkerProperties properties) {
        log.info("리사이징 ForkJoinPool 생성: poolSize={}", properties.getPoolSize());

        return new ForkJoinPool(properties.getPoolSize());
    }
}
