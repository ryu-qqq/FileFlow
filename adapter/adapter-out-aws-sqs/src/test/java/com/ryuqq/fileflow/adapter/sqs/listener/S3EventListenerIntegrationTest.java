package com.ryuqq.fileflow.adapter.sqs.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.sqs.config.SqsProperties;
import com.ryuqq.fileflow.adapter.sqs.handler.S3UploadEventHandler;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.vo.CheckSum;
import com.ryuqq.fileflow.domain.upload.vo.FileId;
import com.ryuqq.fileflow.domain.upload.vo.FileSize;
import com.ryuqq.fileflow.domain.upload.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.upload.vo.TenantId;
import com.ryuqq.fileflow.domain.upload.vo.UploadRequest;
import com.ryuqq.fileflow.domain.upload.vo.UploadStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.retry.support.RetryTemplate;

/**
 * S3EventListener 통합 테스트
 *
 * LocalStack을 사용하여 실제 SQS 메시지 수신 및 처리를 검증합니다.
 *
 * 테스트 전략:
 * - Testcontainers LocalStack을 사용한 SQS 시뮬레이션
 * - 실제 메시지 폴링 및 처리 흐름 검증
 * - 핸들러 호출 및 메시지 삭제 검증
 * - 에러 처리 및 재시도 로직 검증
 *
 * 주의:
 * - Docker가 실행 중이어야 합니다
 * - 비동기 처리로 인해 Awaitility를 사용합니다
 * - 테스트 실행 시간이 다소 소요될 수 있습니다
 */
@Testcontainers
@DisplayName("S3EventListener 통합 테스트 (LocalStack)")
class S3EventListenerIntegrationTest {

    private static final String TEST_REGION = "ap-northeast-2";
    private static final String TEST_QUEUE_NAME = "test-s3-event-queue";

    @Container
    private static final LocalStackContainer localStack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:3.0")
    ).withServices(SQS);

    private static SqsAsyncClient sqsAsyncClient;
    private static String queueUrl;

    private UploadSessionPort uploadSessionPort;
    private S3UploadEventHandler eventHandler;
    private S3EventListener listener;
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll() throws Exception {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                localStack.getAccessKey(),
                localStack.getSecretKey()
        );

        sqsAsyncClient = SqsAsyncClient.builder()
                .endpointOverride(localStack.getEndpoint())
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(TEST_REGION))
                .build();

        // 테스트용 큐 생성
        CreateQueueResponse response = sqsAsyncClient.createQueue(
                CreateQueueRequest.builder()
                        .queueName(TEST_QUEUE_NAME)
                        .build()
        ).get(10, TimeUnit.SECONDS);

        queueUrl = response.queueUrl();
    }

    @AfterAll
    static void afterAll() {
        if (sqsAsyncClient != null) {
            sqsAsyncClient.close();
        }
    }

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        uploadSessionPort = mock(UploadSessionPort.class);
        RetryTemplate retryTemplate = mock(RetryTemplate.class);
        CircuitBreaker circuitBreaker = mock(CircuitBreaker.class);

        SqsProperties properties = new SqsProperties();
        properties.setRegion(TEST_REGION);
        properties.setS3EventQueueUrl(queueUrl);
        properties.setMaxNumberOfMessages(10);
        properties.setWaitTimeSeconds(1);
        properties.setVisibilityTimeout(30);

        eventHandler = new S3UploadEventHandler(objectMapper, uploadSessionPort, retryTemplate, circuitBreaker);

        Executor executor = Executors.newFixedThreadPool(5);
        listener = new S3EventListener(sqsAsyncClient, properties, eventHandler, executor);
    }

    @Test
    @DisplayName("유효한 S3 이벤트 메시지를 수신하고 처리한다")
    void shouldReceiveAndProcessValidS3Event() throws Exception {
        // Given
        String sessionId = "test-session-123";
        String messageBody = createS3EventMessage(sessionId, "test-file.jpg");

        UploadSession session = createTestSession(sessionId);
        when(uploadSessionPort.findById(sessionId)).thenReturn(Optional.of(session));
        when(uploadSessionPort.save(any(UploadSession.class))).thenAnswer(i -> i.getArgument(0));

        // When - SQS에 메시지 전송
        sqsAsyncClient.sendMessage(
                SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(messageBody)
                        .build()
        ).get(5, TimeUnit.SECONDS);

        // Then - 리스너가 메시지를 폴링하고 처리
        listener.pollMessages();

        // 비동기 처리 대기
        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(uploadSessionPort, times(1)).findById(sessionId);
                    verify(uploadSessionPort, times(1)).save(any(UploadSession.class));
                });

        // 메시지가 큐에서 삭제되었는지 확인
        verifyMessageDeleted();
    }

    @Test
    @DisplayName("배치로 여러 메시지를 동시에 처리한다")
    void shouldProcessMultipleMessagesInBatch() throws Exception {
        // Given
        String sessionId1 = "session-001";
        String sessionId2 = "session-002";
        String sessionId3 = "session-003";

        UploadSession session1 = createTestSession(sessionId1);
        UploadSession session2 = createTestSession(sessionId2);
        UploadSession session3 = createTestSession(sessionId3);

        when(uploadSessionPort.findById(sessionId1)).thenReturn(Optional.of(session1));
        when(uploadSessionPort.findById(sessionId2)).thenReturn(Optional.of(session2));
        when(uploadSessionPort.findById(sessionId3)).thenReturn(Optional.of(session3));
        when(uploadSessionPort.save(any(UploadSession.class))).thenAnswer(i -> i.getArgument(0));

        // When - 3개의 메시지 전송
        sqsAsyncClient.sendMessage(
                SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(createS3EventMessage(sessionId1, "file1.jpg"))
                        .build()
        ).get(5, TimeUnit.SECONDS);

        sqsAsyncClient.sendMessage(
                SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(createS3EventMessage(sessionId2, "file2.jpg"))
                        .build()
        ).get(5, TimeUnit.SECONDS);

        sqsAsyncClient.sendMessage(
                SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(createS3EventMessage(sessionId3, "file3.jpg"))
                        .build()
        ).get(5, TimeUnit.SECONDS);

        // Then - 리스너가 배치로 처리
        listener.pollMessages();

        await().atMost(15, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(uploadSessionPort, times(1)).findById(sessionId1);
                    verify(uploadSessionPort, times(1)).findById(sessionId2);
                    verify(uploadSessionPort, times(1)).findById(sessionId3);
                    verify(uploadSessionPort, times(3)).save(any(UploadSession.class));
                });
    }

    @Test
    @DisplayName("잘못된 형식의 메시지는 에러 로그를 남기고 처리 실패한다")
    void shouldHandleInvalidMessageFormat() throws Exception {
        // Given
        String invalidMessageBody = "{ invalid json format }";

        // When
        sqsAsyncClient.sendMessage(
                SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(invalidMessageBody)
                        .build()
        ).get(5, TimeUnit.SECONDS);

        listener.pollMessages();

        // Then - 핸들러가 호출되지 않음
        await().atMost(10, TimeUnit.SECONDS)
                .pollDelay(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(uploadSessionPort, never()).save(any(UploadSession.class));
                });

        // Note: 메시지는 visibility timeout 후 재시도되거나 DLQ로 이동됨
        // 실제 운영 환경에서는 SQS 설정(max receive count, DLQ)으로 처리됨
    }

    @Test
    @DisplayName("존재하지 않는 세션 ID로 인한 에러를 처리한다")
    void shouldHandleSessionNotFoundError() throws Exception {
        // Given
        String nonExistentSessionId = "non-existent-session";
        String messageBody = createS3EventMessage(nonExistentSessionId, "test-file.jpg");

        when(uploadSessionPort.findById(nonExistentSessionId)).thenReturn(Optional.empty());

        // When
        sqsAsyncClient.sendMessage(
                SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(messageBody)
                        .build()
        ).get(5, TimeUnit.SECONDS);

        listener.pollMessages();

        // Then
        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(uploadSessionPort, times(1)).findById(nonExistentSessionId);
                    verify(uploadSessionPort, never()).save(any(UploadSession.class));
                });

        // Note: 메시지는 visibility timeout 후 재시도되거나 DLQ로 이동됨
        // 실제 운영 환경에서는 SQS 설정(max receive count, DLQ)으로 처리됨
    }

    @Test
    @DisplayName("비활성 세션은 업데이트를 건너뛰고 메시지를 삭제한다")
    void shouldSkipInactiveSessionAndDeleteMessage() throws Exception {
        // Given
        String sessionId = "inactive-session";
        String messageBody = createS3EventMessage(sessionId, "test-file.jpg");

        UploadSession inactiveSession = createTestSession(sessionId).fail();
        when(uploadSessionPort.findById(sessionId)).thenReturn(Optional.of(inactiveSession));

        // When
        sqsAsyncClient.sendMessage(
                SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(messageBody)
                        .build()
        ).get(5, TimeUnit.SECONDS);

        listener.pollMessages();

        // Then
        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(uploadSessionPort, times(1)).findById(sessionId);
                    verify(uploadSessionPort, never()).save(any(UploadSession.class));
                });

        // 비활성 세션이지만 정상 처리되어 메시지 삭제
        verifyMessageDeleted();
    }

    @Test
    @DisplayName("큐가 비어있을 때 정상적으로 처리한다")
    void shouldHandleEmptyQueue() {
        // Given - 빈 큐

        // When
        listener.pollMessages();

        // Then - 에러 없이 정상 처리
        await().atMost(5, TimeUnit.SECONDS)
                .pollDelay(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(uploadSessionPort, never()).findById(any());
                    verify(uploadSessionPort, never()).save(any());
                });
    }

    @Test
    @DisplayName("세션 완료 처리 후 상태를 COMPLETED로 업데이트한다")
    void shouldUpdateSessionToCompletedAfterProcessing() throws Exception {
        // Given
        String sessionId = "complete-session";
        String messageBody = createS3EventMessage(sessionId, "test-file.jpg");

        UploadSession session = createTestSession(sessionId);
        when(uploadSessionPort.findById(sessionId)).thenReturn(Optional.of(session));
        when(uploadSessionPort.save(any(UploadSession.class))).thenAnswer(i -> i.getArgument(0));

        // When
        sqsAsyncClient.sendMessage(
                SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(messageBody)
                        .build()
        ).get(5, TimeUnit.SECONDS);

        listener.pollMessages();

        // Then
        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    ArgumentCaptor<UploadSession> sessionCaptor = ArgumentCaptor.forClass(UploadSession.class);
                    verify(uploadSessionPort).save(sessionCaptor.capture());

                    UploadSession savedSession = sessionCaptor.getValue();
                    assertThat(savedSession.getStatus()).isEqualTo(UploadStatus.COMPLETED);
                });
    }

    // ========== Helper Methods ==========

    /**
     * 테스트용 S3 이벤트 메시지 생성
     */
    private String createS3EventMessage(String sessionId, String filename) {
        return String.format("""
                {
                  "Records": [
                    {
                      "eventVersion": "2.1",
                      "eventSource": "aws:s3",
                      "awsRegion": "%s",
                      "eventTime": "2024-01-01T00:00:00.000Z",
                      "eventName": "ObjectCreated:Put",
                      "s3": {
                        "bucket": {
                          "name": "test-bucket",
                          "arn": "arn:aws:s3:::test-bucket"
                        },
                        "object": {
                          "key": "uploads/%s/%s",
                          "size": 1024,
                          "eTag": "test-etag",
                          "sequencer": "test-sequencer"
                        }
                      }
                    }
                  ]
                }
                """, TEST_REGION, sessionId, filename);
    }

    /**
     * 테스트용 업로드 세션 생성
     */
    private UploadSession createTestSession(String sessionId) {
        PolicyKey policyKey = PolicyKey.of("tenant-1", "CONSUMER", "UPLOAD");
        TenantId tenantId = TenantId.of("tenant-1");
        FileId fileId = FileId.generate();
        FileSize fileSize = FileSize.ofBytes(1024L);
        CheckSum checkSum = CheckSum.sha256("a".repeat(64));
        IdempotencyKey idempotencyKey = IdempotencyKey.generate();

        UploadRequest uploadRequest = UploadRequest.of(
                "test-file.jpg",
                com.ryuqq.fileflow.domain.policy.FileType.IMAGE,
                1024L,
                "image/jpeg",
                checkSum,
                idempotencyKey
        );

        return UploadSession.reconstitute(
                sessionId,
                policyKey,
                uploadRequest,
                "test-uploader",
                UploadStatus.PENDING,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1)
        );
    }

    /**
     * 큐에 메시지가 삭제되었는지 확인
     */
    private void verifyMessageDeleted() {
        await().atMost(10, TimeUnit.SECONDS)
                .pollDelay(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    int messageCount = getQueueMessageCount();
                    assertThat(messageCount).isEqualTo(0);
                });
    }

    /**
     * 큐의 메시지 수 조회
     */
    private int getQueueMessageCount() {
        try {
            var response = sqsAsyncClient.getQueueAttributes(
                    GetQueueAttributesRequest.builder()
                            .queueUrl(queueUrl)
                            .attributeNames(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES)
                            .build()
            ).get(5, TimeUnit.SECONDS);

            String count = response.attributes().get(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES);
            return count != null ? Integer.parseInt(count) : 0;
        } catch (Exception e) {
            return -1;
        }
    }
}
