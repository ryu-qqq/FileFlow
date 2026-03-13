package com.ryuqq.fileflow.adapter.out.client.sqs.client;

import com.ryuqq.fileflow.adapter.out.client.sqs.config.SqsPublisherProperties;
import com.ryuqq.fileflow.application.common.dto.result.OutboxBatchSendResult;
import com.ryuqq.fileflow.application.transform.port.out.client.TransformQueueClient;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.BatchResultErrorEntry;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchResultEntry;

@Component
public class TransformQueueSqsPublisher implements TransformQueueClient {

    private static final Logger log = LoggerFactory.getLogger(TransformQueueSqsPublisher.class);

    private final SqsTemplate sqsTemplate;
    private final SqsAsyncClient sqsAsyncClient;
    private final SqsPublisherProperties properties;

    private volatile String cachedQueueUrl;

    public TransformQueueSqsPublisher(
            SqsTemplate sqsTemplate,
            SqsAsyncClient sqsAsyncClient,
            SqsPublisherProperties properties) {
        this.sqsTemplate = sqsTemplate;
        this.sqsAsyncClient = sqsAsyncClient;
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

    @Override
    public OutboxBatchSendResult enqueueBatch(List<String> transformRequestIds) {
        if (transformRequestIds.isEmpty()) {
            return OutboxBatchSendResult.allSuccess(List.of());
        }

        String queueUrl = getQueueUrl();
        String traceId = MDC.get("traceId");

        List<String> allSuccessIds = new ArrayList<>();
        List<OutboxBatchSendResult.FailedEntry> allFailedEntries = new ArrayList<>();

        List<List<String>> chunks = partition(transformRequestIds, 10);

        for (List<String> chunk : chunks) {
            List<SendMessageBatchRequestEntry> entries = new ArrayList<>();
            for (int i = 0; i < chunk.size(); i++) {
                String requestId = chunk.get(i);
                Map<String, MessageAttributeValue> attributes = new HashMap<>();
                if (traceId != null && !traceId.isBlank()) {
                    attributes.put(
                            "traceId",
                            MessageAttributeValue.builder()
                                    .dataType("String")
                                    .stringValue(traceId)
                                    .build());
                }
                entries.add(
                        SendMessageBatchRequestEntry.builder()
                                .id(String.valueOf(i))
                                .messageBody(requestId)
                                .messageAttributes(attributes)
                                .build());
            }

            try {
                SendMessageBatchResponse response =
                        sqsAsyncClient
                                .sendMessageBatch(
                                        SendMessageBatchRequest.builder()
                                                .queueUrl(queueUrl)
                                                .entries(entries)
                                                .build())
                                .join();

                for (SendMessageBatchResultEntry success : response.successful()) {
                    int idx = Integer.parseInt(success.id());
                    allSuccessIds.add(chunk.get(idx));
                }
                for (BatchResultErrorEntry error : response.failed()) {
                    int idx = Integer.parseInt(error.id());
                    allFailedEntries.add(
                            new OutboxBatchSendResult.FailedEntry(chunk.get(idx), error.message()));
                }
            } catch (Exception e) {
                for (String requestId : chunk) {
                    allFailedEntries.add(
                            new OutboxBatchSendResult.FailedEntry(requestId, e.getMessage()));
                }
            }
        }

        log.info(
                "변환 큐 배치 발행 완료: success={}, failed={}",
                allSuccessIds.size(),
                allFailedEntries.size());
        return OutboxBatchSendResult.of(allSuccessIds, allFailedEntries);
    }

    private String getQueueUrl() {
        if (cachedQueueUrl == null) {
            cachedQueueUrl = resolveQueueUrl(properties.transformQueue());
        }
        return cachedQueueUrl;
    }

    private String resolveQueueUrl(String queueNameOrUrl) {
        if (queueNameOrUrl.startsWith("https://")) {
            return queueNameOrUrl;
        }
        try {
            return sqsAsyncClient
                    .getQueueUrl(GetQueueUrlRequest.builder().queueName(queueNameOrUrl).build())
                    .join()
                    .queueUrl();
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve queue URL for: " + queueNameOrUrl, e);
        }
    }

    private static <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }
}
