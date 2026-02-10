package com.ryuqq.fileflow.adapter.out.persistence.download.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.adapter.out.persistence.download.DownloadTaskJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.download.entity.DownloadTaskJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.download.mapper.DownloadTaskJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.DownloadTaskQueryDslRepository;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTaskFixture;
import com.ryuqq.fileflow.domain.download.id.DownloadTaskId;
import com.ryuqq.fileflow.domain.download.vo.DownloadTaskStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
@DisplayName("DownloadTaskQueryAdapter 단위 테스트")
class DownloadTaskQueryAdapterTest {

    @InjectMocks private DownloadTaskQueryAdapter queryAdapter;
    @Mock private DownloadTaskQueryDslRepository queryDslRepository;
    @Mock private DownloadTaskJpaMapper mapper;

    @Nested
    @DisplayName("findById 메서드 테스트")
    class FindByIdTest {

        @Test
        @DisplayName("존재하는 ID로 조회하면 도메인 객체를 반환합니다")
        void findById_existingId_shouldReturnDomain() {
            // given
            DownloadTaskId id = DownloadTaskId.of("download-001");
            DownloadTaskJpaEntity entity = DownloadTaskJpaEntityFixture.aQueuedEntity();
            DownloadTask domain = DownloadTaskFixture.aQueuedTask();

            given(queryDslRepository.findById(id.value())).willReturn(Optional.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // when
            Optional<DownloadTask> result = queryAdapter.findById(id);

            // then
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 빈 Optional을 반환합니다")
        void findById_nonExistingId_shouldReturnEmpty() {
            // given
            DownloadTaskId id = DownloadTaskId.of("non-existing-id");
            given(queryDslRepository.findById(id.value())).willReturn(Optional.empty());

            // when
            Optional<DownloadTask> result = queryAdapter.findById(id);

            // then
            assertThat(result).isEmpty();
            then(mapper).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("findByStatusAndCreatedBefore 메서드 테스트")
    class FindByStatusAndCreatedBeforeTest {

        @Test
        @DisplayName("조건에 맞는 태스크 목록을 반환합니다")
        void findByStatusAndCreatedBefore_shouldReturnList() {
            // given
            DownloadTaskStatus status = DownloadTaskStatus.QUEUED;
            Instant createdBefore = Instant.parse("2026-01-02T00:00:00Z");
            int limit = 10;
            DownloadTaskJpaEntity entity = DownloadTaskJpaEntityFixture.aQueuedEntity();
            DownloadTask domain = DownloadTaskFixture.aQueuedTask();

            given(queryDslRepository.findByStatusAndCreatedBefore(status, createdBefore, limit))
                    .willReturn(List.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // when
            List<DownloadTask> result =
                    queryAdapter.findByStatusAndCreatedBefore(status, createdBefore, limit);

            // then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("조건에 맞는 태스크가 없으면 빈 목록을 반환합니다")
        void findByStatusAndCreatedBefore_noMatch_shouldReturnEmptyList() {
            // given
            DownloadTaskStatus status = DownloadTaskStatus.QUEUED;
            Instant createdBefore = Instant.parse("2025-12-31T00:00:00Z");
            int limit = 10;

            given(queryDslRepository.findByStatusAndCreatedBefore(status, createdBefore, limit))
                    .willReturn(List.of());

            // when
            List<DownloadTask> result =
                    queryAdapter.findByStatusAndCreatedBefore(status, createdBefore, limit);

            // then
            assertThat(result).isEmpty();
        }
    }
}
