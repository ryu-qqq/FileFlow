package com.ryuqq.fileflow.adapter.in.sqs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SQS Consumer 큐 설정 프로퍼티.
 *
 * <p>SQS Publisher와 동일한 prefix(fileflow.sqs)를 사용하여 같은 큐명을 공유합니다.
 *
 * @param downloadQueue 다운로드 작업 큐 이름
 * @param transformQueue 변환 작업 큐 이름
 */
@ConfigurationProperties(prefix = "fileflow.sqs")
public record SqsConsumerProperties(String downloadQueue, String transformQueue) {}
