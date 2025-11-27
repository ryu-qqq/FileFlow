package com.ryuqq.fileflow.adapter.out.persistence.session.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ryuqq.fileflow.adapter.out.persistence.session.entity.CompletedPartJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.mapper.MultipartUploadSessionJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.session.repository.CompletedPartQueryDslRepository;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.PartNumber;
import com.ryuqq.fileflow.domain.session.vo.PresignedUrl;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("FindCompletedPartQueryAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class FindCompletedPartQueryAdapterTest {

    @Mock private CompletedPartQueryDslRepository repository;

    @Mock private MultipartUploadSessionJpaMapper mapper;

    private FindCompletedPartQueryAdapter adapter;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        adapter = new FindCompletedPartQueryAdapter(repository, mapper);
        fixedClock = Clock.fixed(Instant.parse("2025-11-26T10:00:00Z"), ZoneId.of("UTC"));
    }

    @Nested
    @DisplayName("findBySessionIdAndPartNumber 테스트")
    class FindBySessionIdAndPartNumberTest {

        @Test
        @DisplayName("세션 ID와 파트 번호로 CompletedPart를 조회할 수 있다")
        void findBySessionIdAndPartNumber_WithValidParams_ShouldReturnPart() {
            // given
            String sessionId = UUID.randomUUID().toString();
            int partNumber = 1;
            UploadSessionId uploadSessionId = UploadSessionId.of(UUID.fromString(sessionId));
            CompletedPartJpaEntity entity = createEntity(sessionId, partNumber);
            CompletedPart domain = createCompletedPart(sessionId, partNumber);

            when(repository.findBySessionIdAndPartNumber(sessionId, partNumber))
                    .thenReturn(Optional.of(entity));
            when(mapper.toCompletedPart(entity)).thenReturn(domain);

            // when
            Optional<CompletedPart> result =
                    adapter.findBySessionIdAndPartNumber(uploadSessionId, partNumber);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getPartNumberValue()).isEqualTo(partNumber);
        }

        @Test
        @DisplayName("조회 결과가 없으면 empty Optional을 반환한다")
        void findBySessionIdAndPartNumber_WhenNotFound_ShouldReturnEmpty() {
            // given
            String sessionId = UUID.randomUUID().toString();
            UploadSessionId uploadSessionId = UploadSessionId.of(UUID.fromString(sessionId));

            when(repository.findBySessionIdAndPartNumber(anyString(), anyInt()))
                    .thenReturn(Optional.empty());

            // when
            Optional<CompletedPart> result =
                    adapter.findBySessionIdAndPartNumber(uploadSessionId, 1);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Repository를 올바른 파라미터로 호출한다")
        void findBySessionIdAndPartNumber_ShouldCallRepositoryWithCorrectParams() {
            // given
            String sessionId = UUID.randomUUID().toString();
            int partNumber = 3;
            UploadSessionId uploadSessionId = UploadSessionId.of(UUID.fromString(sessionId));

            when(repository.findBySessionIdAndPartNumber(sessionId, partNumber))
                    .thenReturn(Optional.empty());

            // when
            adapter.findBySessionIdAndPartNumber(uploadSessionId, partNumber);

            // then
            verify(repository).findBySessionIdAndPartNumber(sessionId, partNumber);
        }
    }

    @Nested
    @DisplayName("findAllBySessionId 테스트")
    class FindAllBySessionIdTest {

        @Test
        @DisplayName("세션 ID로 모든 CompletedPart를 조회할 수 있다")
        void findAllBySessionId_WithValidSessionId_ShouldReturnParts() {
            // given
            String sessionId = UUID.randomUUID().toString();
            UploadSessionId uploadSessionId = UploadSessionId.of(UUID.fromString(sessionId));
            CompletedPartJpaEntity entity1 = createEntity(sessionId, 1);
            CompletedPartJpaEntity entity2 = createEntity(sessionId, 2);
            CompletedPart domain1 = createCompletedPart(sessionId, 1);
            CompletedPart domain2 = createCompletedPart(sessionId, 2);

            when(repository.findAllBySessionId(sessionId)).thenReturn(List.of(entity1, entity2));
            when(mapper.toCompletedPart(entity1)).thenReturn(domain1);
            when(mapper.toCompletedPart(entity2)).thenReturn(domain2);

            // when
            List<CompletedPart> result = adapter.findAllBySessionId(uploadSessionId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getPartNumberValue()).isEqualTo(1);
            assertThat(result.get(1).getPartNumberValue()).isEqualTo(2);
        }

        @Test
        @DisplayName("조회 결과가 없으면 빈 목록을 반환한다")
        void findAllBySessionId_WhenNoResults_ShouldReturnEmptyList() {
            // given
            String sessionId = UUID.randomUUID().toString();
            UploadSessionId uploadSessionId = UploadSessionId.of(UUID.fromString(sessionId));

            when(repository.findAllBySessionId(sessionId)).thenReturn(List.of());

            // when
            List<CompletedPart> result = adapter.findAllBySessionId(uploadSessionId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Repository를 올바른 파라미터로 호출한다")
        void findAllBySessionId_ShouldCallRepositoryWithCorrectParams() {
            // given
            String sessionId = UUID.randomUUID().toString();
            UploadSessionId uploadSessionId = UploadSessionId.of(UUID.fromString(sessionId));

            when(repository.findAllBySessionId(sessionId)).thenReturn(List.of());

            // when
            adapter.findAllBySessionId(uploadSessionId);

            // then
            verify(repository).findAllBySessionId(sessionId);
        }
    }

    // ==================== Helper Methods ====================

    private CompletedPart createCompletedPart(String sessionId, int partNumber) {
        return CompletedPart.of(
                null,
                UploadSessionId.of(UUID.fromString(sessionId)),
                PartNumber.of(partNumber),
                PresignedUrl.of("https://presigned-url.s3.amazonaws.com/part-" + partNumber),
                ETag.of("\"etag-" + partNumber + "\""),
                5 * 1024 * 1024L,
                LocalDateTime.now(fixedClock),
                fixedClock);
    }

    private CompletedPartJpaEntity createEntity(String sessionId, int partNumber) {
        LocalDateTime now = LocalDateTime.now();
        return CompletedPartJpaEntity.of(
                sessionId,
                partNumber,
                "https://presigned-url.s3.amazonaws.com/part-" + partNumber,
                "\"etag-" + partNumber + "\"",
                5 * 1024 * 1024L,
                now,
                now,
                now);
    }
}
