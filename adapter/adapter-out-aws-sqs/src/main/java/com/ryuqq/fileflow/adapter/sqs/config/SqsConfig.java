package com.ryuqq.fileflow.adapter.sqs.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.net.URI;
import java.util.concurrent.Executor;

/**
 * AWS SQS 클라이언트 설정
 *
 * SQS 메시지 수신을 위한 비동기 클라이언트를 구성합니다.
 * LocalStack 환경과 AWS 환경 모두 지원합니다.
 *
 * @author sangwon-ryu
 */
@Configuration
@EnableConfigurationProperties(SqsProperties.class)
public class SqsConfig {

    private final SqsProperties sqsProperties;

    public SqsConfig(SqsProperties sqsProperties) {
        this.sqsProperties = sqsProperties;
    }

    /**
     * SQS 메시지 처리 전용 Executor를 생성합니다.
     *
     * ForkJoinPool.commonPool() 사용을 피하고, 메시지 처리를 위한
     * 격리된 스레드 풀을 제공하여 애플리케이션 전체의 성능에 영향을 주지 않도록 합니다.
     *
     * @return SQS 메시지 처리 전용 Executor
     */
    @Bean(name = "sqsMessageProcessorExecutor")
    public Executor sqsMessageProcessorExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("sqs-msg-proc-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * SQS 비동기 클라이언트를 생성합니다.
     *
     * @return SqsAsyncClient 인스턴스
     */
    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        AwsCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();

        var builder = SqsAsyncClient.builder()
                .region(Region.of(sqsProperties.getRegion()))
                .credentialsProvider(credentialsProvider);

        // LocalStack 환경 설정
        if (StringUtils.hasText(sqsProperties.getEndpoint())) {
            builder.endpointOverride(URI.create(sqsProperties.getEndpoint()));
        }

        return builder.build();
    }
}
