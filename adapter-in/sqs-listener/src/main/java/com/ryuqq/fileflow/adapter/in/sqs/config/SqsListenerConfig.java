package com.ryuqq.fileflow.adapter.in.sqs.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * SQS Listener 설정
 *
 * <p><strong>용도</strong>: Spring Cloud AWS SQS 리스너 설정
 *
 * <p>Spring Cloud AWS SQS가 자동으로 SqsClient를 구성하므로 별도 Bean 정의 없이 Properties만 활성화
 *
 * @author development-team
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(SqsListenerProperties.class)
public class SqsListenerConfig {}
