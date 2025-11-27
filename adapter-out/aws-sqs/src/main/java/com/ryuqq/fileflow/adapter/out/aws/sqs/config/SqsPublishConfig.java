package com.ryuqq.fileflow.adapter.out.aws.sqs.config;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

/**
 * SQS Publish 설정.
 *
 * <p><strong>용도</strong>: Spring Cloud AWS SQS Publisher 설정
 *
 * <p><strong>활성화 조건</strong>: {@code sqs.publish.enabled=true}
 */
@Configuration
@EnableConfigurationProperties(SqsPublishProperties.class)
@ConditionalOnProperty(name = "sqs.publish.enabled", havingValue = "true")
public class SqsPublishConfig {

    /**
     * SqsTemplate 빈.
     *
     * <p>SqsAsyncClient를 사용하여 SqsTemplate 생성
     */
    @Bean
    @ConditionalOnMissingBean
    public SqsTemplate sqsTemplate(SqsAsyncClient sqsAsyncClient) {
        return SqsTemplate.builder().sqsAsyncClient(sqsAsyncClient).build();
    }
}
