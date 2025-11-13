package com.ryuqq.fileflow.application.file.scheduler;

import com.ryuqq.fileflow.application.file.config.PipelineOutboxProperties;
import com.ryuqq.fileflow.application.file.manager.PipelineOutboxManager;
import com.ryuqq.fileflow.domain.common.OutboxStatus;
import com.ryuqq.fileflow.domain.download.ProcessResult;
import com.ryuqq.fileflow.domain.pipeline.PipelineOutbox;
import com.ryuqq.fileflow.domain.pipeline.PipelineResult;
import com.ryuqq.fileflow.domain.pipeline.fixture.PipelineOutboxFixture;
import com.ryuqq.fileflow.domain.pipeline.fixture.PipelineResultFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * PipelineOutboxScheduler 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>PENDING 메시지 처리 성공</li>
 *   <li>Worker 결과에 따른 상태 업데이트 (COMPLETED/FAILED)</li>
 *   <li>재시도 가능한 FAILED 메시지 처리</li>
 *   <li>오래된 PROCESSING 메시지 재처리</li>
 *   <li>최대 재시도 횟수 초과 시 영구 실패</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Tag("unit")
@Tag("application")
@ExtendWith(MockitoExtension.class)
class PipelineOutboxSchedulerTest {

    @InjectMocks
    private PipelineOutboxScheduler scheduler;

    @Mock
    private PipelineOutboxManager outboxManager;

    @Mock
    private PipelineWorker pipelineWorker;

    @Mock
    private PipelineOutboxProperties properties;

    @BeforeEach
    void setUp() {
        // 기본 설정값
        given(properties.getBatchSize()).willReturn(10);
        given(properties.getMaxRetryCount()).willReturn(3);
        given(properties.getStaleMinutes()).willReturn(5);
        given(properties.getRetryBaseDelaySeconds()).willReturn(60);
    }

    @Test
    @DisplayName("PENDING 메시지 처리 성공 시 COMPLETED로 상태 변경")
    void processPendingMessageSuccess() {
        // Given
        PipelineOutbox outbox = PipelineOutboxFixture.createPending(1L);
        List<PipelineOutbox> pendingMessages = List.of(outbox);

        given(outboxManager.findNewMessages(10))
            .willReturn(pendingMessages);
        given(outboxManager.findRetryableFailedMessages(anyInt(), any(LocalDateTime.class), anyInt()))
            .willReturn(List.of());
        given(outboxManager.findStaleProcessingMessages(any(LocalDateTime.class), anyInt()))
            .willReturn(List.of());

        given(outboxManager.markProcessing(eq(outbox)))
            .willReturn(outbox);

        given(pipelineWorker.startPipeline(outbox.getFileAssetIdValue()))
            .willReturn(PipelineResultFixture.success());

        given(outboxManager.markProcessed(eq(outbox)))
            .willReturn(outbox);

        // When
        scheduler.processOutboxMessages();

        // Then
        verify(outboxManager).markProcessing(eq(outbox));
        verify(pipelineWorker).startPipeline(outbox.getFileAssetIdValue());
        verify(outboxManager).markProcessed(eq(outbox));
        verify(outboxManager, never()).markFailed(any(), anyString());
    }

    @Test
    @DisplayName("Worker 실패 시 FAILED로 상태 변경하고 재시도 가능")
    void processPendingMessageFailure() {
        // Given
        PipelineOutbox outbox = PipelineOutboxFixture.createPending(1L);
        List<PipelineOutbox> pendingMessages = List.of(outbox);

        given(outboxManager.findNewMessages(10))
            .willReturn(pendingMessages);
        given(outboxManager.findRetryableFailedMessages(anyInt(), any(LocalDateTime.class), anyInt()))
            .willReturn(List.of());
        given(outboxManager.findStaleProcessingMessages(any(LocalDateTime.class), anyInt()))
            .willReturn(List.of());

        given(outboxManager.markProcessing(eq(outbox)))
            .willReturn(outbox);

        given(pipelineWorker.startPipeline(outbox.getFileAssetIdValue()))
            .willReturn(PipelineResultFixture.failure("Worker error"));

        // 재시도 가능 (retryCount < maxRetryCount)
        // getRetryCount()는 0이므로 maxRetryCount(3)보다 작음
        given(properties.getMaxRetryCount()).willReturn(3);

        given(outboxManager.markFailed(eq(outbox), anyString()))
            .willReturn(outbox);

        // When
        scheduler.processOutboxMessages();

        // Then
        verify(outboxManager).markFailed(eq(outbox), anyString());
        verify(outboxManager, never()).markProcessed(any());
        verify(outboxManager, never()).markPermanentlyFailed(any(), anyString());
    }

    @Test
    @DisplayName("최대 재시도 횟수 초과 시 영구 실패 처리")
    void processPermanentFailure() {
        // Given
        // retryCount가 maxRetryCount 이상인 outbox 생성
        PipelineOutbox outbox = PipelineOutboxFixture.reconstitute(
            1L,
            1L,
            "test-key",
            OutboxStatus.PROCESSING,
            3  // retryCount = maxRetryCount
        );
        List<PipelineOutbox> pendingMessages = List.of(outbox);

        given(outboxManager.findNewMessages(10))
            .willReturn(pendingMessages);
        given(outboxManager.findRetryableFailedMessages(anyInt(), any(LocalDateTime.class), anyInt()))
            .willReturn(List.of());
        given(outboxManager.findStaleProcessingMessages(any(LocalDateTime.class), anyInt()))
            .willReturn(List.of());

        given(outboxManager.markProcessing(eq(outbox)))
            .willReturn(outbox);

        given(pipelineWorker.startPipeline(outbox.getFileAssetIdValue()))
            .willReturn(PipelineResultFixture.failure("Worker error"));

        // 최대 재시도 횟수 초과
        given(properties.getMaxRetryCount()).willReturn(3);

        given(outboxManager.markPermanentlyFailed(eq(outbox), anyString()))
            .willReturn(outbox);

        // When
        scheduler.processOutboxMessages();

        // Then
        verify(outboxManager).markPermanentlyFailed(eq(outbox), anyString());
        verify(outboxManager, never()).markFailed(any(), anyString());
        verify(outboxManager, never()).markProcessed(any());
    }

    @Test
    @DisplayName("재시도 가능한 FAILED 메시지를 처리한다")
    void processRetryableFailedMessages() {
        // Given
        PipelineOutbox failedOutbox = PipelineOutboxFixture.createFailed(1L);
        List<PipelineOutbox> retryableMessages = List.of(failedOutbox);

        given(outboxManager.findNewMessages(10))
            .willReturn(List.of());
        given(outboxManager.findRetryableFailedMessages(eq(3), any(LocalDateTime.class), eq(10)))
            .willReturn(retryableMessages);
        given(outboxManager.findStaleProcessingMessages(any(LocalDateTime.class), anyInt()))
            .willReturn(List.of());

        given(outboxManager.prepareForRetry(eq(failedOutbox)))
            .willReturn(failedOutbox);
        given(outboxManager.markProcessing(eq(failedOutbox)))
            .willReturn(failedOutbox);

        given(pipelineWorker.startPipeline(failedOutbox.getFileAssetIdValue()))
            .willReturn(PipelineResultFixture.success());

        given(outboxManager.markProcessed(eq(failedOutbox)))
            .willReturn(failedOutbox);

        // When
        scheduler.processOutboxMessages();

        // Then
        verify(outboxManager).prepareForRetry(eq(failedOutbox));
        verify(outboxManager).markProcessing(eq(failedOutbox));
        verify(pipelineWorker).startPipeline(failedOutbox.getFileAssetIdValue());
        verify(outboxManager).markProcessed(eq(failedOutbox));
    }

    @Test
    @DisplayName("오래된 PROCESSING 메시지를 재처리한다")
    void processStaleProcessingMessages() {
        // Given
        PipelineOutbox staleOutbox = PipelineOutboxFixture.createProcessing(1L);
        List<PipelineOutbox> staleMessages = List.of(staleOutbox);

        given(outboxManager.findNewMessages(10))
            .willReturn(List.of());
        given(outboxManager.findRetryableFailedMessages(anyInt(), any(LocalDateTime.class), anyInt()))
            .willReturn(List.of());
        given(outboxManager.findStaleProcessingMessages(any(LocalDateTime.class), eq(10)))
            .willReturn(staleMessages);

        given(outboxManager.markProcessing(eq(staleOutbox)))
            .willReturn(staleOutbox);

        given(pipelineWorker.startPipeline(staleOutbox.getFileAssetIdValue()))
            .willReturn(PipelineResultFixture.success());

        given(outboxManager.markProcessed(eq(staleOutbox)))
            .willReturn(staleOutbox);

        // When
        scheduler.processOutboxMessages();

        // Then
        verify(outboxManager).markProcessing(eq(staleOutbox));
        verify(pipelineWorker).startPipeline(staleOutbox.getFileAssetIdValue());
        verify(outboxManager).markProcessed(eq(staleOutbox));
    }

    @Test
    @DisplayName("처리할 메시지가 없으면 아무 작업도 수행하지 않는다")
    void doNothingWhenNoMessages() {
        // Given
        given(outboxManager.findNewMessages(10))
            .willReturn(List.of());
        given(outboxManager.findRetryableFailedMessages(anyInt(), any(LocalDateTime.class), anyInt()))
            .willReturn(List.of());
        given(outboxManager.findStaleProcessingMessages(any(LocalDateTime.class), anyInt()))
            .willReturn(List.of());

        // When
        scheduler.processOutboxMessages();

        // Then
        verify(outboxManager, never()).markProcessing(any());
        verify(pipelineWorker, never()).startPipeline(anyLong());
    }
}

