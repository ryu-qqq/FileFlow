package com.ryuqq.fileflow.adapter.out.aws.sqs.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * SQS Publish 설정.
 *
 * <p><strong>용도</strong>: Spring Cloud AWS SQS Publisher 설정
 *
 * <p>Spring Cloud AWS SQS가 자동으로 SqsTemplate을 구성하므로 별도 Bean 정의 없이 Properties만 활성화
 */
@Configuration
@EnableConfigurationProperties(SqsPublishProperties.class)
public class SqsPublishConfig {}
