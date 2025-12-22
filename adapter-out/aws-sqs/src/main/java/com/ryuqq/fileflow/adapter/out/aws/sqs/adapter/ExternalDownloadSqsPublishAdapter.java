package com.ryuqq.fileflow.adapter.out.aws.sqs.adapter;

import com.ryuqq.fileflow.adapter.out.aws.sqs.config.SqsPublishProperties;
import com.ryuqq.fileflow.application.download.dto.ExternalDownloadMessage;
import com.ryuqq.fileflow.application.download.port.out.client.ExternalDownloadSqsPublishPort;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * SQS 발행 어댑터.
 *
 * <p>Spring Cloud AWS SqsTemplate을 사용한 SqsPublishPort 구현체.
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
public class ExternalDownloadSqsPublishAdapter implements ExternalDownloadSqsPublishPort {

    private static final Logger log =
            LoggerFactory.getLogger(ExternalDownloadSqsPublishAdapter.class);

    private final SqsTemplate sqsTemplate;
    private final SqsPublishProperties properties;

    public ExternalDownloadSqsPublishAdapter(
            @Qualifier("sqsPublishTemplate") SqsTemplate sqsTemplate,
            SqsPublishProperties properties) {
        this.sqsTemplate = sqsTemplate;
        this.properties = properties;
    }

    @Override
    public boolean publish(ExternalDownloadMessage message) {
        try {
            sqsTemplate.send(properties.getExternalDownloadQueueUrl(), message);
            log.debug(
                    "SQS 메시지 발행 성공: externalDownloadId={}, queueUrl={}",
                    message.externalDownloadId(),
                    properties.getExternalDownloadQueueUrl());
            return true;
        } catch (Exception e) {
            log.error("SQS 메시지 발행 실패: externalDownloadId={}", message.externalDownloadId(), e);
            return false;
        }
    }
}
