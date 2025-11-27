package com.ryuqq.fileflow.adapter.out.aws.sqs.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.out.aws.sqs.config.SqsPublishProperties;
import com.ryuqq.fileflow.application.download.dto.ExternalDownloadMessage;
import com.ryuqq.fileflow.application.download.port.out.client.SqsPublishPort;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
 *   <li>JSON 직렬화를 통한 메시지 전송
 *   <li>실패 시 false 반환 (예외를 던지지 않음)
 * </ul>
 *
 * <p><strong>활성화 조건</strong>: SqsTemplate 빈이 존재할 때만 활성화
 */
@Component
@ConditionalOnBean(SqsTemplate.class)
public class SqsPublishAdapter implements SqsPublishPort {

    private static final Logger log = LoggerFactory.getLogger(SqsPublishAdapter.class);

    private final SqsTemplate sqsTemplate;
    private final SqsPublishProperties properties;
    private final ObjectMapper objectMapper;

    public SqsPublishAdapter(
            SqsTemplate sqsTemplate, SqsPublishProperties properties, ObjectMapper objectMapper) {
        this.sqsTemplate = sqsTemplate;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean publish(ExternalDownloadMessage message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            sqsTemplate.send(properties.getExternalDownloadQueueUrl(), jsonMessage);
            log.debug(
                    "SQS 메시지 발행 성공: externalDownloadId={}, queueUrl={}",
                    message.externalDownloadId(),
                    properties.getExternalDownloadQueueUrl());
            return true;
        } catch (JsonProcessingException e) {
            log.error("SQS 메시지 직렬화 실패: externalDownloadId={}", message.externalDownloadId(), e);
            return false;
        } catch (Exception e) {
            log.error("SQS 메시지 발행 실패: externalDownloadId={}", message.externalDownloadId(), e);
            return false;
        }
    }
}
