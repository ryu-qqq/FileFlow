package com.ryuqq.fileflow.adapter.out.aws.sqs.adapter;

import com.ryuqq.fileflow.adapter.out.aws.sqs.config.SqsPublishProperties;
import com.ryuqq.fileflow.application.asset.dto.message.FileProcessingMessage;
import com.ryuqq.fileflow.application.asset.port.out.client.FileProcessingSqsPublishPort;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 파일 가공 SQS 발행 어댑터.
 *
 * <p>FileProcessingMessage를 Resizing Worker 큐로 발행합니다.
 *
 * <p><strong>특징</strong>:
 *
 * <ul>
 *   <li>SqsTemplate 기반 메시지 발행
 *   <li>SqsTemplate이 자동으로 JSON 직렬화 수행
 *   <li>실패 시 false 반환 (예외를 던지지 않음)
 * </ul>
 *
 * <p><strong>활성화 조건</strong>: {@code sqs.publish.enabled=true}
 */
@Component
@ConditionalOnProperty(name = "sqs.publish.enabled", havingValue = "true")
public class FileProcessingSqsPublishAdapter implements FileProcessingSqsPublishPort {

    private static final Logger log =
            LoggerFactory.getLogger(FileProcessingSqsPublishAdapter.class);

    private final SqsTemplate sqsTemplate;
    private final SqsPublishProperties properties;

    public FileProcessingSqsPublishAdapter(
            @Qualifier("sqsPublishTemplate") SqsTemplate sqsTemplate,
            SqsPublishProperties properties) {
        this.sqsTemplate = sqsTemplate;
        this.properties = properties;
    }

    @Override
    public boolean publish(FileProcessingMessage message) {
        try {
            sqsTemplate.send(properties.getFileProcessingQueueUrl(), message);
            log.debug(
                    "SQS 메시지 발행 성공: fileAssetId={}, outboxId={}, queueUrl={}",
                    message.fileAssetId(),
                    message.outboxId(),
                    properties.getFileProcessingQueueUrl());
            return true;
        } catch (Exception e) {
            log.error(
                    "SQS 메시지 발행 실패: fileAssetId={}, outboxId={}",
                    message.fileAssetId(),
                    message.outboxId(),
                    e);
            return false;
        }
    }
}
