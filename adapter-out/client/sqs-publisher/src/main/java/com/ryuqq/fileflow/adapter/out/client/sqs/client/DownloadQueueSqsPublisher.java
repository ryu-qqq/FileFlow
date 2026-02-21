package com.ryuqq.fileflow.adapter.out.client.sqs.client;

import com.ryuqq.fileflow.adapter.out.client.sqs.config.SqsPublisherProperties;
import com.ryuqq.fileflow.application.download.port.out.client.DownloadQueueClient;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class DownloadQueueSqsPublisher implements DownloadQueueClient {

    private static final Logger log = LoggerFactory.getLogger(DownloadQueueSqsPublisher.class);

    private final SqsTemplate sqsTemplate;
    private final SqsPublisherProperties properties;

    public DownloadQueueSqsPublisher(SqsTemplate sqsTemplate, SqsPublisherProperties properties) {
        this.sqsTemplate = sqsTemplate;
        this.properties = properties;
    }

    @Override
    public void enqueue(String downloadTaskId) {
        String queueName = properties.downloadQueue();
        String traceId = MDC.get("traceId");
        log.info("다운로드 큐 발행: taskId={}, queue={}, traceId={}", downloadTaskId, queueName, traceId);

        sqsTemplate.send(
                to ->
                        to.queue(queueName)
                                .payload(downloadTaskId)
                                .header("traceId", traceId != null ? traceId : ""));

        log.info("다운로드 큐 발행 완료: taskId={}", downloadTaskId);
    }
}
