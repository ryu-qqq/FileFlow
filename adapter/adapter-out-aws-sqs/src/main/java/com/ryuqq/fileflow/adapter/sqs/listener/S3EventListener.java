package com.ryuqq.fileflow.adapter.sqs.listener;

import com.ryuqq.fileflow.adapter.sqs.config.SqsProperties;
import com.ryuqq.fileflow.adapter.sqs.handler.S3UploadEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * S3 Event SQS Listener
 *
 * SQS 큐로부터 S3 이벤트 메시지를 수신하고 처리합니다.
 * 배치 처리를 지원하며 비동기적으로 동작합니다.
 *
 * @author sangwon-ryu
 */
@Component
public class S3EventListener {

    private static final Logger log = LoggerFactory.getLogger(S3EventListener.class);

    private final SqsAsyncClient sqsAsyncClient;
    private final SqsProperties sqsProperties;
    private final S3UploadEventHandler eventHandler;

    public S3EventListener(
            SqsAsyncClient sqsAsyncClient,
            SqsProperties sqsProperties,
            S3UploadEventHandler eventHandler
    ) {
        this.sqsAsyncClient = sqsAsyncClient;
        this.sqsProperties = sqsProperties;
        this.eventHandler = eventHandler;
    }

    /**
     * SQS 메시지를 주기적으로 폴링합니다.
     *
     * 5초마다 실행되며, 최대 10개의 메시지를 배치로 수신합니다.
     */
    @Async
    @Scheduled(fixedDelay = 5000)
    public void pollMessages() {
        String queueUrl = sqsProperties.getS3EventQueueUrl();

        if (queueUrl == null || queueUrl.isEmpty()) {
            log.warn("S3 Event Queue URL is not configured. Skipping message polling.");
            return;
        }

        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(sqsProperties.getMaxNumberOfMessages())
                .waitTimeSeconds(sqsProperties.getWaitTimeSeconds())
                .visibilityTimeout(sqsProperties.getVisibilityTimeout())
                .build();

        sqsAsyncClient.receiveMessage(receiveRequest)
                .thenCompose(this::processMessages)
                .exceptionally(throwable -> {
                    log.error("Error polling SQS messages", throwable);
                    return null;
                });
    }

    /**
     * 수신한 메시지들을 처리합니다.
     *
     * @param response SQS 메시지 수신 응답
     * @return 처리 완료 Future
     */
    private CompletableFuture<Void> processMessages(ReceiveMessageResponse response) {
        List<Message> messages = response.messages();

        if (messages.isEmpty()) {
            log.debug("No messages received from SQS");
            return CompletableFuture.completedFuture(null);
        }

        log.info("Received {} messages from SQS", messages.size());

        List<CompletableFuture<Void>> futures = messages.stream()
                .map(this::processMessage)
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    /**
     * 개별 메시지를 처리합니다.
     *
     * @param message SQS 메시지
     * @return 처리 완료 Future
     */
    private CompletableFuture<Void> processMessage(Message message) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.debug("Processing message: {}", message.messageId());

                // 메시지 본문 추출
                String messageBody = message.body();

                // 이벤트 핸들러로 처리
                eventHandler.handleS3Event(messageBody);

                // 처리 성공 시 메시지 삭제
                deleteMessage(message);

                log.info("Successfully processed message: {}", message.messageId());

            } catch (Exception e) {
                log.error("Failed to process message: {}. Error: {}",
                        message.messageId(), e.getMessage(), e);

                // 메시지 처리 실패 시 visibility timeout 후 재시도
                // DLQ는 SQS 설정에서 처리
            }
        });
    }

    /**
     * 처리된 메시지를 큐에서 삭제합니다.
     *
     * @param message 삭제할 메시지
     */
    private void deleteMessage(Message message) {
        String queueUrl = sqsProperties.getS3EventQueueUrl();

        DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build();

        sqsAsyncClient.deleteMessage(deleteRequest)
                .thenAccept(response -> log.debug("Message deleted: {}", message.messageId()))
                .exceptionally(throwable -> {
                    log.error("Failed to delete message: {}", message.messageId(), throwable);
                    return null;
                });
    }
}
