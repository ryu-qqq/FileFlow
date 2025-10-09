package com.ryuqq.fileflow.adapter.sqs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.net.URI;

/**
 * AWS SQS 클라이언트 설정
 *
 * SQS 메시지 수신을 위한 비동기 클라이언트를 구성합니다.
 * LocalStack 환경과 AWS 환경 모두 지원합니다.
 *
 * @author sangwon-ryu
 */
@Configuration
public class SqsConfig {

    private final SqsProperties sqsProperties;

    public SqsConfig(SqsProperties sqsProperties) {
        this.sqsProperties = sqsProperties;
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
        if (sqsProperties.getEndpoint() != null && !sqsProperties.getEndpoint().isEmpty()) {
            builder.endpointOverride(URI.create(sqsProperties.getEndpoint()));
        }

        return builder.build();
    }
}
