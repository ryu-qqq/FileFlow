package com.ryuqq.fileflow.adapter.out.persistence.transform.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.adapter.out.persistence.transform.TransformRequestJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.transform.entity.TransformRequestJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.transform.mapper.TransformRequestJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.transform.repository.TransformRequestQueryDslRepository;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequestFixture;
import com.ryuqq.fileflow.domain.transform.id.TransformRequestId;
import com.ryuqq.fileflow.domain.transform.vo.TransformStatus;
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
@DisplayName("TransformRequestQueryAdapter 단위 테스트")
class TransformRequestQueryAdapterTest {

    @InjectMocks private TransformRequestQueryAdapter queryAdapter;
    @Mock private TransformRequestQueryDslRepository queryDslRepository;
    @Mock private TransformRequestJpaMapper mapper;

    @Nested
    @DisplayName("findById 메서드 테스트")
    class FindByIdTest {

        @Test
        @DisplayName("존재하는 ID로 조회하면 도메인 객체를 반환합니다")
        void findById_existingId_shouldReturnDomain() {
            // given
            TransformRequestId id = TransformRequestId.of("transform-001");
            TransformRequestJpaEntity entity =
                    TransformRequestJpaEntityFixture.aQueuedResizeEntity();
            TransformRequest domain = TransformRequestFixture.aResizeRequest();

            given(queryDslRepository.findById(id.value())).willReturn(Optional.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // when
            Optional<TransformRequest> result = queryAdapter.findById(id);

            // then
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 빈 Optional을 반환합니다")
        void findById_nonExistingId_shouldReturnEmpty() {
            // given
            TransformRequestId id = TransformRequestId.of("non-existing-id");
            given(queryDslRepository.findById(id.value())).willReturn(Optional.empty());

            // when
            Optional<TransformRequest> result = queryAdapter.findById(id);

            // then
            assertThat(result).isEmpty();
            then(mapper).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("findByStatusAndCreatedBefore 메서드 테스트")
    class FindByStatusAndCreatedBeforeTest {

        @Test
        @DisplayName("조건에 맞는 요청 목록을 반환합니다")
        void findByStatusAndCreatedBefore_shouldReturnList() {
            // given
            TransformStatus status = TransformStatus.QUEUED;
            Instant createdBefore = Instant.parse("2026-01-02T00:00:00Z");
            int limit = 10;
            TransformRequestJpaEntity entity =
                    TransformRequestJpaEntityFixture.aQueuedResizeEntity();
            TransformRequest domain = TransformRequestFixture.aResizeRequest();

            given(queryDslRepository.findByStatusAndCreatedBefore(status, createdBefore, limit))
                    .willReturn(List.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // when
            List<TransformRequest> result =
                    queryAdapter.findByStatusAndCreatedBefore(status, createdBefore, limit);

            // then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("조건에 맞는 요청이 없으면 빈 목록을 반환합니다")
        void findByStatusAndCreatedBefore_noMatch_shouldReturnEmptyList() {
            // given
            TransformStatus status = TransformStatus.QUEUED;
            Instant createdBefore = Instant.parse("2025-12-31T00:00:00Z");
            int limit = 10;

            given(queryDslRepository.findByStatusAndCreatedBefore(status, createdBefore, limit))
                    .willReturn(List.of());

            // when
            List<TransformRequest> result =
                    queryAdapter.findByStatusAndCreatedBefore(status, createdBefore, limit);

            // then
            assertThat(result).isEmpty();
        }
    }
}
