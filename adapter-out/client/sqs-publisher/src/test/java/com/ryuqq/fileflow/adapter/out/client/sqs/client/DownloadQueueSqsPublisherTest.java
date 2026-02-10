package com.ryuqq.fileflow.adapter.out.client.sqs.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.ryuqq.fileflow.adapter.out.client.sqs.config.SqsPublisherProperties;
import com.ryuqq.fileflow.application.download.port.out.client.DownloadQueueClient;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("DownloadQueueSqsPublisher 단위 테스트")
class DownloadQueueSqsPublisherTest {

    private SqsTemplate sqsTemplate;
    private SqsPublisherProperties properties;
    private DownloadQueueSqsPublisher sut;

    private static final String QUEUE_NAME = "fileflow-download-queue";

    @BeforeEach
    void setUp() {
        sqsTemplate = mock(SqsTemplate.class);
        properties = mock(SqsPublisherProperties.class);
        given(properties.downloadQueue()).willReturn(QUEUE_NAME);
        sut = new DownloadQueueSqsPublisher(sqsTemplate, properties);
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
            verify(sqsTemplate).send(QUEUE_NAME, downloadTaskId);
        }

        @Test
        @DisplayName("성공: DownloadQueueClient 인터페이스를 구현한다")
        void shouldImplementDownloadQueueClient() {
            assertThat(sut).isInstanceOf(DownloadQueueClient.class);
        }
    }
}
