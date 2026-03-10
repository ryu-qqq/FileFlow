package com.ryuqq.fileflow.adapter.out.client.sqs.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ryuqq.fileflow.adapter.out.client.sqs.config.SqsPublisherProperties;
import com.ryuqq.fileflow.application.common.dto.result.OutboxBatchSendResult;
import com.ryuqq.fileflow.application.download.port.out.client.DownloadQueueClient;
import io.awspring.cloud.sqs.operations.SendResult;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchResultEntry;

@Tag("unit")
@DisplayName("DownloadQueueSqsPublisher 단위 테스트")
class DownloadQueueSqsPublisherTest {

    private SqsTemplate sqsTemplate;
    private SqsAsyncClient sqsAsyncClient;
    private SqsPublisherProperties properties;
    private DownloadQueueSqsPublisher sut;

    private static final String QUEUE_NAME = "fileflow-download-queue";

    @BeforeEach
    void setUp() {
        sqsTemplate = mock(SqsTemplate.class);
        sqsAsyncClient = mock(SqsAsyncClient.class);
        properties = mock(SqsPublisherProperties.class);
        given(properties.downloadQueue()).willReturn(QUEUE_NAME);
        given(sqsTemplate.send(any(Consumer.class))).willReturn(mock(SendResult.class));
        sut = new DownloadQueueSqsPublisher(sqsTemplate, sqsAsyncClient, properties);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Nested
    @DisplayName("enqueue 메서드")
    class Enqueue {

        @Test
        @DisplayName("성공: SQS 큐에 다운로드 태스크 ID를 발행한다")
        void shouldPublishDownloadTaskIdToSqsQueue() {
            // given
            String downloadTaskId = "task-001";

            // when
            sut.enqueue(downloadTaskId);

            // then
            verify(sqsTemplate).send(any(Consumer.class));
        }

        @Test
        @DisplayName("성공: MDC traceId가 있으면 SQS 헤더에 포함하여 발행한다")
        void shouldPublishWithTraceIdHeader() {
            // given
            String downloadTaskId = "task-002";
            MDC.put("traceId", "scheduler-abc12345");

            // when
            sut.enqueue(downloadTaskId);

            // then
            verify(sqsTemplate).send(any(Consumer.class));
        }

        @Test
        @DisplayName("성공: MDC traceId가 없어도 정상 발행된다")
        void shouldPublishWithoutTraceId() {
            // given
            String downloadTaskId = "task-003";

            // when
            sut.enqueue(downloadTaskId);

            // then
            verify(sqsTemplate).send(any(Consumer.class));
        }

        @Test
        @DisplayName("성공: DownloadQueueClient 인터페이스를 구현한다")
        void shouldImplementDownloadQueueClient() {
            assertThat(sut).isInstanceOf(DownloadQueueClient.class);
        }
    }

    @Nested
    @DisplayName("enqueueBatch 메서드")
    class EnqueueBatch {

        private static final String QUEUE_URL =
                "https://sqs.us-east-1.amazonaws.com/123456789/fileflow-download-queue";

        private void stubQueueUrl() {
            CompletableFuture<GetQueueUrlResponse> queueUrlFuture =
                    CompletableFuture.completedFuture(
                            GetQueueUrlResponse.builder().queueUrl(QUEUE_URL).build());
            given(sqsAsyncClient.getQueueUrl(any(GetQueueUrlRequest.class)))
                    .willReturn(queueUrlFuture);
        }

        @Test
        @DisplayName("빈 리스트 전달 시 allSuccess(빈 리스트)를 반환한다")
        void enqueueBatch_EmptyList_ReturnsAllSuccessEmpty() {
            OutboxBatchSendResult result = sut.enqueueBatch(List.of());

            assertThat(result.successIds()).isEmpty();
            assertThat(result.failedEntries()).isEmpty();
            assertThat(result.hasFailures()).isFalse();
        }

        @Test
        @DisplayName("성공: SqsAsyncClient.sendMessageBatch 성공 시 successIds를 반환한다")
        void enqueueBatch_Success_ReturnsSuccessIds() {
            stubQueueUrl();
            SendMessageBatchResponse batchResponse =
                    SendMessageBatchResponse.builder()
                            .successful(
                                    SendMessageBatchResultEntry.builder()
                                            .id("0")
                                            .messageId("msg-1")
                                            .build(),
                                    SendMessageBatchResultEntry.builder()
                                            .id("1")
                                            .messageId("msg-2")
                                            .build())
                            .build();
            given(sqsAsyncClient.sendMessageBatch(any(SendMessageBatchRequest.class)))
                    .willReturn(CompletableFuture.completedFuture(batchResponse));

            OutboxBatchSendResult result = sut.enqueueBatch(List.of("task-001", "task-002"));

            assertThat(result.successIds()).containsExactly("task-001", "task-002");
            assertThat(result.hasFailures()).isFalse();
        }

        @Test
        @DisplayName("실패: SqsAsyncClient에서 예외 발생 시 failedEntries를 반환한다")
        void enqueueBatch_Exception_ReturnsFailedEntries() {
            stubQueueUrl();
            given(sqsAsyncClient.sendMessageBatch(any(SendMessageBatchRequest.class)))
                    .willReturn(
                            CompletableFuture.failedFuture(
                                    new RuntimeException("SQS connection error")));

            OutboxBatchSendResult result = sut.enqueueBatch(List.of("task-001", "task-002"));

            assertThat(result.successIds()).isEmpty();
            assertThat(result.failedEntries()).hasSize(2);
            assertThat(result.failedEntries().get(0).id()).isEqualTo("task-001");
            assertThat(result.failedEntries().get(1).id()).isEqualTo("task-002");
        }

        @Test
        @DisplayName("10개 초과 시 partition되어 sendMessageBatch가 2번 호출된다")
        void enqueueBatch_MoreThan10_PartitionsIntoMultipleBatches() {
            stubQueueUrl();

            List<String> taskIds = new ArrayList<>();
            IntStream.rangeClosed(1, 12)
                    .forEach(i -> taskIds.add("task-" + String.format("%03d", i)));

            SendMessageBatchResponse firstBatchResponse =
                    SendMessageBatchResponse.builder()
                            .successful(
                                    IntStream.range(0, 10)
                                            .mapToObj(
                                                    i ->
                                                            SendMessageBatchResultEntry.builder()
                                                                    .id(String.valueOf(i))
                                                                    .messageId("msg-" + i)
                                                                    .build())
                                            .toList())
                            .build();
            SendMessageBatchResponse secondBatchResponse =
                    SendMessageBatchResponse.builder()
                            .successful(
                                    IntStream.range(0, 2)
                                            .mapToObj(
                                                    i ->
                                                            SendMessageBatchResultEntry.builder()
                                                                    .id(String.valueOf(i))
                                                                    .messageId("msg-" + (10 + i))
                                                                    .build())
                                            .toList())
                            .build();

            given(sqsAsyncClient.sendMessageBatch(any(SendMessageBatchRequest.class)))
                    .willReturn(CompletableFuture.completedFuture(firstBatchResponse))
                    .willReturn(CompletableFuture.completedFuture(secondBatchResponse));

            OutboxBatchSendResult result = sut.enqueueBatch(taskIds);

            assertThat(result.successIds()).hasSize(12);
            assertThat(result.hasFailures()).isFalse();
            then(sqsAsyncClient)
                    .should(times(2))
                    .sendMessageBatch(any(SendMessageBatchRequest.class));
        }
    }
}
