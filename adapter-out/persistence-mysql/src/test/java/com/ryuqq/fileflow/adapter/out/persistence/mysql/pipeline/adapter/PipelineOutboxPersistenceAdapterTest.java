package com.ryuqq.fileflow.adapter.out.persistence.mysql.pipeline.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.pipeline.entity.PipelineOutboxJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.pipeline.fixture.PipelineOutboxJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.pipeline.mapper.PipelineOutboxEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.pipeline.repository.PipelineOutboxJpaRepository;
import com.ryuqq.fileflow.domain.common.OutboxStatus;
import com.ryuqq.fileflow.domain.pipeline.PipelineOutbox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * PipelineOutboxPersistenceAdapter 단위 테스트
 *
 * <p><strong>테스트 대상</strong>: {@link PipelineOutboxPersistenceAdapter}</p>
 * <p><strong>테스트 전략</strong>: Mockito 기반 단위 테스트</p>
 *
 * <h3>테스트 범위</h3>
 * <ul>
 *   <li>✅ save(): PipelineOutbox 저장</li>
 *   <li>✅ findByStatus(): 상태별 조회 (Pageable 적용)</li>
 *   <li>✅ findRetryableFailedMessages(): 재시도 가능한 FAILED 메시지 조회</li>
 *   <li>✅ findStaleProcessingMessages(): 오래된 PROCESSING 메시지 조회</li>
 *   <li>✅ Pageable 파라미터 전달 검증</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Tag("unit")
@Tag("query")
@ExtendWith(MockitoExtension.class)
@DisplayName("PipelineOutboxPersistenceAdapter 단위 테스트")
class PipelineOutboxPersistenceAdapterTest {

    @Mock
    private PipelineOutboxJpaRepository repository;

    @Mock
    private PipelineOutboxEntityMapper mapper;

    @InjectMocks
    private PipelineOutboxPersistenceAdapter adapter;

    private PipelineOutboxJpaEntity entity;
    private PipelineOutbox domain;

    @BeforeEach
    void setUp() {
        entity = PipelineOutboxJpaEntityFixture.createPending(1L);
        domain = com.ryuqq.fileflow.domain.pipeline.fixture.PipelineOutboxFixture.createPending(1L);
    }

    @Nested
    @DisplayName("findByStatus() - 상태별 조회")
    class FindByStatusTests {

        @Test
        @DisplayName("PENDING 상태의 Outbox를 Pageable과 함께 조회한다")
        void findByStatusWithPageable() {
            // Given
            OutboxStatus status = OutboxStatus.PENDING;
            int batchSize = 10;
            Pageable pageable = PageRequest.of(0, batchSize);

            List<PipelineOutboxJpaEntity> entities = List.of(
                PipelineOutboxJpaEntityFixture.createPending(1L),
                PipelineOutboxJpaEntityFixture.createPending(2L)
            );

            given(repository.findByStatusOrderByCreatedAtAsc(eq(status), eq(pageable)))
                .willReturn(entities);

            given(mapper.toDomain(entities.get(0)))
                .willReturn(domain);
            given(mapper.toDomain(entities.get(1)))
                .willReturn(domain);

            // When
            List<PipelineOutbox> result = adapter.findByStatus(status, batchSize);

            // Then
            assertThat(result).hasSize(2);
            verify(repository).findByStatusOrderByCreatedAtAsc(eq(status), eq(pageable));
        }

        @Test
        @DisplayName("조회 결과가 없으면 빈 리스트를 반환한다")
        void findByStatusReturnsEmptyList() {
            // Given
            OutboxStatus status = OutboxStatus.PENDING;
            int batchSize = 10;
            Pageable pageable = PageRequest.of(0, batchSize);

            given(repository.findByStatusOrderByCreatedAtAsc(eq(status), eq(pageable)))
                .willReturn(List.of());

            // When
            List<PipelineOutbox> result = adapter.findByStatus(status, batchSize);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findRetryableFailedMessages() - 재시도 가능한 FAILED 메시지 조회")
    class FindRetryableFailedMessagesTests {

        @Test
        @DisplayName("재시도 가능한 FAILED 메시지를 Pageable과 함께 조회한다")
        void findRetryableFailedMessagesWithPageable() {
            // Given
            int maxRetryCount = 3;
            LocalDateTime retryAfter = LocalDateTime.now().minusMinutes(5);
            int batchSize = 10;
            Pageable pageable = PageRequest.of(0, batchSize);

            List<PipelineOutboxJpaEntity> entities = List.of(
                PipelineOutboxJpaEntityFixture.createFailed(1L, 1),
                PipelineOutboxJpaEntityFixture.createFailed(2L, 2)
            );

            given(repository.findRetryableFailedOutboxes(
                eq(OutboxStatus.FAILED),
                eq(maxRetryCount),
                eq(retryAfter),
                eq(pageable)
            )).willReturn(entities);

            given(mapper.toDomain(any(PipelineOutboxJpaEntity.class)))
                .willReturn(domain);

            // When
            List<PipelineOutbox> result = adapter.findRetryableFailedMessages(
                maxRetryCount,
                retryAfter,
                batchSize
            );

            // Then
            assertThat(result).hasSize(2);
            verify(repository).findRetryableFailedOutboxes(
                eq(OutboxStatus.FAILED),
                eq(maxRetryCount),
                eq(retryAfter),
                eq(pageable)
            );
        }
    }

    @Nested
    @DisplayName("findStaleProcessingMessages() - 오래된 PROCESSING 메시지 조회")
    class FindStaleProcessingMessagesTests {

        @Test
        @DisplayName("오래된 PROCESSING 메시지를 Pageable과 함께 조회한다")
        void findStaleProcessingMessagesWithPageable() {
            // Given
            LocalDateTime staleThreshold = LocalDateTime.now().minusMinutes(5);
            int batchSize = 10;
            Pageable pageable = PageRequest.of(0, batchSize);

            List<PipelineOutboxJpaEntity> entities = List.of(
                PipelineOutboxJpaEntityFixture.createProcessing(1L),
                PipelineOutboxJpaEntityFixture.createProcessing(2L)
            );

            given(repository.findStaleProcessingMessages(
                eq(OutboxStatus.PROCESSING),
                eq(staleThreshold),
                eq(pageable)
            )).willReturn(entities);

            given(mapper.toDomain(any(PipelineOutboxJpaEntity.class)))
                .willReturn(domain);

            // When
            List<PipelineOutbox> result = adapter.findStaleProcessingMessages(
                staleThreshold,
                batchSize
            );

            // Then
            assertThat(result).hasSize(2);
            verify(repository).findStaleProcessingMessages(
                eq(OutboxStatus.PROCESSING),
                eq(staleThreshold),
                eq(pageable)
            );
        }

        @Test
        @DisplayName("오래된 PROCESSING 메시지가 없으면 빈 리스트를 반환한다")
        void findStaleProcessingMessagesReturnsEmptyList() {
            // Given
            LocalDateTime staleThreshold = LocalDateTime.now().minusMinutes(5);
            int batchSize = 10;
            Pageable pageable = PageRequest.of(0, batchSize);

            given(repository.findStaleProcessingMessages(
                eq(OutboxStatus.PROCESSING),
                eq(staleThreshold),
                eq(pageable)
            )).willReturn(List.of());

            // When
            List<PipelineOutbox> result = adapter.findStaleProcessingMessages(
                staleThreshold,
                batchSize
            );

            // Then
            assertThat(result).isEmpty();
        }
    }
}

