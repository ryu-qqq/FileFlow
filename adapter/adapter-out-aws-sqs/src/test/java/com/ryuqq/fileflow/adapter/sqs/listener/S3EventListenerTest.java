package com.ryuqq.fileflow.adapter.sqs.listener;

import com.ryuqq.fileflow.adapter.sqs.config.SqsProperties;
import com.ryuqq.fileflow.adapter.sqs.handler.S3UploadEventHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * S3EventListener 단위 테스트
 *
 * 테스트 범위:
 * - 메시지 폴링 로직
 * - 메시지 처리 흐름
 * - 에러 핸들링
 * - 메시지 삭제 로직
 * - 빈 큐 처리
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("S3EventListener 단위 테스트")
class S3EventListenerTest {

    private static final String TEST_QUEUE_URL = "https://sqs.ap-northeast-2.amazonaws.com/123456789/test-queue";
    private static final String TEST_REGION = "ap-northeast-2";

    @Mock
    private SqsAsyncClient sqsAsyncClient;

    @Mock
    private S3UploadEventHandler eventHandler;

    @Mock
    private Executor messageProcessorExecutor;

    private S3EventListener listener;
    private SqsProperties properties;

    @BeforeEach
    void setUp() {
        properties = new SqsProperties();
        properties.setRegion(TEST_REGION);
        properties.setS3EventQueueUrl(TEST_QUEUE_URL);
        properties.setMaxNumberOfMessages(10);
        properties.setWaitTimeSeconds(20);
        properties.setVisibilityTimeout(30);

        listener = new S3EventListener(sqsAsyncClient, properties, eventHandler, messageProcessorExecutor);
    }

    @Test
    @DisplayName("유효한 메시지를 수신하고 핸들러를 호출한다")
    void shouldReceiveMessageAndInvokeHandler() {
        // Given
        String messageBody = createTestS3EventMessage();
        Message message = Message.builder()
                .messageId("msg-001")
                .body(messageBody)
                .receiptHandle("receipt-001")
                .build();

        ReceiveMessageResponse receiveResponse = ReceiveMessageResponse.builder()
                .messages(message)
                .build();

        when(sqsAsyncClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(receiveResponse));

        when(sqsAsyncClient.deleteMessage(any(DeleteMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(DeleteMessageResponse.builder().build()));

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(messageProcessorExecutor).execute(any(Runnable.class));

        // When
        listener.pollMessages();

        // Then
        ArgumentCaptor<ReceiveMessageRequest> receiveCaptor = ArgumentCaptor.forClass(ReceiveMessageRequest.class);
        verify(sqsAsyncClient).receiveMessage(receiveCaptor.capture());

        ReceiveMessageRequest capturedRequest = receiveCaptor.getValue();
        assertThat(capturedRequest.queueUrl()).isEqualTo(TEST_QUEUE_URL);
        assertThat(capturedRequest.maxNumberOfMessages()).isEqualTo(10);
        assertThat(capturedRequest.waitTimeSeconds()).isEqualTo(20);
        assertThat(capturedRequest.visibilityTimeout()).isEqualTo(30);

        verify(eventHandler).handleS3Event(messageBody);
        verify(sqsAsyncClient).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    @DisplayName("여러 메시지를 배치로 처리한다")
    void shouldProcessMultipleMessagesInBatch() {
        // Given
        Message message1 = Message.builder()
                .messageId("msg-001")
                .body(createTestS3EventMessage())
                .receiptHandle("receipt-001")
                .build();

        Message message2 = Message.builder()
                .messageId("msg-002")
                .body(createTestS3EventMessage())
                .receiptHandle("receipt-002")
                .build();

        Message message3 = Message.builder()
                .messageId("msg-003")
                .body(createTestS3EventMessage())
                .receiptHandle("receipt-003")
                .build();

        ReceiveMessageResponse receiveResponse = ReceiveMessageResponse.builder()
                .messages(message1, message2, message3)
                .build();

        when(sqsAsyncClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(receiveResponse));

        when(sqsAsyncClient.deleteMessage(any(DeleteMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(DeleteMessageResponse.builder().build()));

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(messageProcessorExecutor).execute(any(Runnable.class));

        // When
        listener.pollMessages();

        // Then
        verify(eventHandler, times(3)).handleS3Event(any(String.class));
        verify(sqsAsyncClient, times(3)).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    @DisplayName("빈 큐에서는 핸들러를 호출하지 않는다")
    void shouldNotInvokeHandlerWhenQueueIsEmpty() {
        // Given
        ReceiveMessageResponse emptyResponse = ReceiveMessageResponse.builder()
                .messages(List.of())
                .build();

        when(sqsAsyncClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(emptyResponse));

        // When
        listener.pollMessages();

        // Then
        verify(sqsAsyncClient).receiveMessage(any(ReceiveMessageRequest.class));
        verify(eventHandler, never()).handleS3Event(any(String.class));
        verify(sqsAsyncClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    @DisplayName("큐 URL이 설정되지 않은 경우 메시지 폴링을 건너뛴다")
    void shouldSkipPollingWhenQueueUrlIsNotConfigured() {
        // Given
        properties.setS3EventQueueUrl(null);

        // When
        listener.pollMessages();

        // Then
        verify(sqsAsyncClient, never()).receiveMessage(any(ReceiveMessageRequest.class));
        verify(eventHandler, never()).handleS3Event(any(String.class));
    }

    @Test
    @DisplayName("빈 문자열 큐 URL인 경우 메시지 폴링을 건너뛴다")
    void shouldSkipPollingWhenQueueUrlIsEmpty() {
        // Given
        properties.setS3EventQueueUrl("");

        // When
        listener.pollMessages();

        // Then
        verify(sqsAsyncClient, never()).receiveMessage(any(ReceiveMessageRequest.class));
        verify(eventHandler, never()).handleS3Event(any(String.class));
    }

    @Test
    @DisplayName("핸들러에서 예외 발생 시 메시지를 삭제하지 않는다")
    void shouldNotDeleteMessageWhenHandlerThrowsException() {
        // Given
        String messageBody = createTestS3EventMessage();
        Message message = Message.builder()
                .messageId("msg-001")
                .body(messageBody)
                .receiptHandle("receipt-001")
                .build();

        ReceiveMessageResponse receiveResponse = ReceiveMessageResponse.builder()
                .messages(message)
                .build();

        when(sqsAsyncClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(receiveResponse));

        doThrow(new RuntimeException("Handler error"))
                .when(eventHandler).handleS3Event(messageBody);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(messageProcessorExecutor).execute(any(Runnable.class));

        // When
        listener.pollMessages();

        // Then
        verify(eventHandler).handleS3Event(messageBody);
        verify(sqsAsyncClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    @DisplayName("메시지 삭제 실패 시 예외를 로그로 남기고 계속 진행한다")
    void shouldLogAndContinueWhenDeleteMessageFails() {
        // Given
        String messageBody = createTestS3EventMessage();
        Message message = Message.builder()
                .messageId("msg-001")
                .body(messageBody)
                .receiptHandle("receipt-001")
                .build();

        ReceiveMessageResponse receiveResponse = ReceiveMessageResponse.builder()
                .messages(message)
                .build();

        when(sqsAsyncClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(receiveResponse));

        CompletableFuture<DeleteMessageResponse> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Delete failed"));

        when(sqsAsyncClient.deleteMessage(any(DeleteMessageRequest.class)))
                .thenReturn(failedFuture);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(messageProcessorExecutor).execute(any(Runnable.class));

        // When & Then - 예외가 발생하지 않고 정상 처리
        assertThatCode(() -> listener.pollMessages()).doesNotThrowAnyException();

        verify(eventHandler).handleS3Event(messageBody);
        verify(sqsAsyncClient).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    @DisplayName("SQS 폴링 중 예외 발생 시 로그로 남기고 계속 진행한다")
    void shouldLogAndContinueWhenPollingFails() {
        // Given
        CompletableFuture<ReceiveMessageResponse> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Polling failed"));

        when(sqsAsyncClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(failedFuture);

        // When & Then - 예외가 발생하지 않고 정상 처리
        assertThatCode(() -> listener.pollMessages()).doesNotThrowAnyException();

        verify(sqsAsyncClient).receiveMessage(any(ReceiveMessageRequest.class));
        verify(eventHandler, never()).handleS3Event(any(String.class));
    }

    @Test
    @DisplayName("메시지 삭제 요청이 올바른 파라미터로 호출된다")
    void shouldCallDeleteMessageWithCorrectParameters() {
        // Given
        String messageBody = createTestS3EventMessage();
        String receiptHandle = "test-receipt-handle";

        Message message = Message.builder()
                .messageId("msg-001")
                .body(messageBody)
                .receiptHandle(receiptHandle)
                .build();

        ReceiveMessageResponse receiveResponse = ReceiveMessageResponse.builder()
                .messages(message)
                .build();

        when(sqsAsyncClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(receiveResponse));

        when(sqsAsyncClient.deleteMessage(any(DeleteMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(DeleteMessageResponse.builder().build()));

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(messageProcessorExecutor).execute(any(Runnable.class));

        // When
        listener.pollMessages();

        // Then
        ArgumentCaptor<DeleteMessageRequest> deleteCaptor = ArgumentCaptor.forClass(DeleteMessageRequest.class);
        verify(sqsAsyncClient).deleteMessage(deleteCaptor.capture());

        DeleteMessageRequest capturedRequest = deleteCaptor.getValue();
        assertThat(capturedRequest.queueUrl()).isEqualTo(TEST_QUEUE_URL);
        assertThat(capturedRequest.receiptHandle()).isEqualTo(receiptHandle);
    }

    @Test
    @DisplayName("메시지 처리가 비동기 Executor로 실행된다")
    void shouldProcessMessageAsynchronously() {
        // Given
        String messageBody = createTestS3EventMessage();
        Message message = Message.builder()
                .messageId("msg-001")
                .body(messageBody)
                .receiptHandle("receipt-001")
                .build();

        ReceiveMessageResponse receiveResponse = ReceiveMessageResponse.builder()
                .messages(message)
                .build();

        when(sqsAsyncClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(receiveResponse));

        // When
        listener.pollMessages();

        // Then
        verify(messageProcessorExecutor).execute(any(Runnable.class));
    }

    // ========== Helper Methods ==========

    private String createTestS3EventMessage() {
        return """
                {
                  "Records": [
                    {
                      "eventVersion": "2.1",
                      "eventSource": "aws:s3",
                      "awsRegion": "ap-northeast-2",
                      "eventTime": "2024-01-01T00:00:00.000Z",
                      "eventName": "ObjectCreated:Put",
                      "s3": {
                        "bucket": {
                          "name": "test-bucket",
                          "arn": "arn:aws:s3:::test-bucket"
                        },
                        "object": {
                          "key": "uploads/session-123/test-file.jpg",
                          "size": 1024,
                          "eTag": "test-etag",
                          "sequencer": "test-sequencer"
                        }
                      }
                    }
                  ]
                }
                """;
    }
}
