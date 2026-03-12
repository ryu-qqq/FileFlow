package com.ryuqq.fileflow.adapter.out.persistence.download.adapter;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.DownloadQueueOutboxJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.download.mapper.DownloadQueueOutboxJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.DownloadQueueOutboxJpaRepository;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadQueueOutbox;
import com.ryuqq.fileflow.domain.download.id.DownloadQueueOutboxId;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("DownloadQueueOutboxCommandAdapter 단위 테스트")
class DownloadQueueOutboxCommandAdapterTest {

    @InjectMocks private DownloadQueueOutboxCommandAdapter sut;
    @Mock private DownloadQueueOutboxJpaRepository jpaRepository;
    @Mock private DownloadQueueOutboxJpaMapper mapper;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Nested
    @DisplayName("persist 메서드")
    class PersistTest {

        @Test
        @DisplayName("도메인 객체를 엔티티로 변환하여 저장한다")
        void persist_MapsAndSaves() {
            DownloadQueueOutbox domain =
                    DownloadQueueOutbox.forNew(
                            DownloadQueueOutboxId.of("outbox-001"), "download-001", NOW);
            DownloadQueueOutboxJpaEntity entity =
                    DownloadQueueOutboxJpaEntity.create(
                            "outbox-001", "download-001", OutboxStatus.PENDING, 0, null, NOW, null);
            given(mapper.toEntity(domain)).willReturn(entity);

            sut.persist(domain);

            then(mapper).should().toEntity(domain);
            then(jpaRepository).should().save(entity);
        }
    }

    @Nested
    @DisplayName("persist 메서드 -- 업데이트 시나리오")
    class PersistUpdateTest {

        @Test
        @DisplayName("상태 변경된 도메인 객체를 엔티티로 변환하여 저장한다")
        void persist_UpdatedDomain_MapsAndSaves() {
            DownloadQueueOutbox domain =
                    DownloadQueueOutbox.forNew(
                            DownloadQueueOutboxId.of("outbox-001"), "download-001", NOW);
            domain.markSent(NOW.plusSeconds(5));

            DownloadQueueOutboxJpaEntity entity =
                    DownloadQueueOutboxJpaEntity.create(
                            "outbox-001",
                            "download-001",
                            OutboxStatus.SENT,
                            0,
                            null,
                            NOW,
                            NOW.plusSeconds(5));
            given(mapper.toEntity(domain)).willReturn(entity);

            sut.persist(domain);

            then(mapper).should().toEntity(domain);
            then(jpaRepository).should().save(entity);
        }
    }

    @Nested
    @DisplayName("bulkMarkSent 메서드")
    class BulkMarkSentTest {

        @Test
        @DisplayName("ids가 비어있지 않으면 JPA Repository에 위임한다")
        void bulkMarkSent_NonEmpty_DelegatesToRepository() {
            List<String> ids = List.of("id-1", "id-2");
            sut.bulkMarkSent(ids, NOW);

            then(jpaRepository).should().bulkMarkSent(ids, NOW);
        }

        @Test
        @DisplayName("ids가 비어있으면 Repository를 호출하지 않는다")
        void bulkMarkSent_Empty_DoesNotCallRepository() {
            sut.bulkMarkSent(List.of(), NOW);

            then(jpaRepository).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("bulkMarkFailed 메서드")
    class BulkMarkFailedTest {

        @Test
        @DisplayName("ids가 비어있지 않으면 JPA Repository에 위임한다")
        void bulkMarkFailed_NonEmpty_DelegatesToRepository() {
            List<String> ids = List.of("id-1", "id-2");
            sut.bulkMarkFailed(ids, NOW, "SQS error");

            then(jpaRepository).should().bulkMarkFailed(ids, NOW, "SQS error");
        }

        @Test
        @DisplayName("ids가 비어있으면 Repository를 호출하지 않는다")
        void bulkMarkFailed_Empty_DoesNotCallRepository() {
            sut.bulkMarkFailed(List.of(), NOW, "error");

            then(jpaRepository).shouldHaveNoInteractions();
        }
    }
}
