package com.ryuqq.fileflow.adapter.out.client.sqs.client;

import com.ryuqq.fileflow.adapter.out.client.sqs.config.SqsPublisherProperties;
import com.ryuqq.fileflow.application.transform.port.out.client.TransformQueueClient;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class TransformQueueSqsPublisher implements TransformQueueClient {

    private static final Logger log = LoggerFactory.getLogger(TransformQueueSqsPublisher.class);

    private final SqsTemplate sqsTemplate;
    private final SqsPublisherProperties properties;

    public TransformQueueSqsPublisher(SqsTemplate sqsTemplate, SqsPublisherProperties properties) {
        this.sqsTemplate = sqsTemplate;
        this.properties = properties;
    }

    @Override
    public void enqueue(String transformRequestId) {
        String queueName = properties.transformQueue();
        String traceId = MDC.get("traceId");
        log.info(
                "변환 큐 발행: requestId={}, queue={}, traceId={}",
                transformRequestId,
                queueName,
                traceId);

        sqsTemplate.send(
                to ->
                        to.queue(queueName)
                                .payload(transformRequestId)
                                .header("traceId", traceId != null ? traceId : ""));

        log.info("변환 큐 발행 완료: requestId={}", transformRequestId);
    }
}
