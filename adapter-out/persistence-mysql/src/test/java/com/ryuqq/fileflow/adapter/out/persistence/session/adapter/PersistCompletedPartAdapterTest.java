package com.ryuqq.fileflow.adapter.out.persistence.session.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ryuqq.fileflow.adapter.out.persistence.session.entity.CompletedPartJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.mapper.MultipartUploadSessionJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.session.repository.CompletedPartJpaRepository;
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
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("PersistCompletedPartAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class PersistCompletedPartAdapterTest {

    @Mock private CompletedPartJpaRepository repository;

    @Mock private MultipartUploadSessionJpaMapper mapper;

    private PersistCompletedPartAdapter adapter;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        adapter = new PersistCompletedPartAdapter(repository, mapper);
        fixedClock = Clock.fixed(Instant.parse("2025-11-26T10:00:00Z"), ZoneId.of("UTC"));
    }

    @Nested
    @DisplayName("persist 테스트")
    class PersistTest {

        @Test
        @DisplayName("CompletedPart를 영속화하고 반환한다")
        void persist_WithValidPart_ShouldReturnPersistedPart() {
            // given
            String sessionId = UUID.randomUUID().toString();
            UploadSessionId uploadSessionId = UploadSessionId.of(UUID.fromString(sessionId));
            CompletedPart completedPart = createCompletedPart(sessionId, 1);
            CompletedPartJpaEntity entity = createEntity(sessionId, 1);

            when(mapper.toPartEntity(anyString(), any(CompletedPart.class))).thenReturn(entity);
            when(repository.save(entity)).thenReturn(entity);
            when(mapper.toCompletedPart(entity)).thenReturn(completedPart);

            // when
            CompletedPart result = adapter.persist(uploadSessionId, completedPart);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getPartNumberValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("Mapper를 호출하여 Entity로 변환한다")
        void persist_ShouldCallMapperToPartEntity() {
            // given
            String sessionId = UUID.randomUUID().toString();
            UploadSessionId uploadSessionId = UploadSessionId.of(UUID.fromString(sessionId));
            CompletedPart completedPart = createCompletedPart(sessionId, 1);
            CompletedPartJpaEntity entity = createEntity(sessionId, 1);

            when(mapper.toPartEntity(anyString(), any(CompletedPart.class))).thenReturn(entity);
            when(repository.save(entity)).thenReturn(entity);
            when(mapper.toCompletedPart(entity)).thenReturn(completedPart);

            // when
            adapter.persist(uploadSessionId, completedPart);

            // then
            verify(mapper).toPartEntity(sessionId, completedPart);
        }

        @Test
        @DisplayName("Repository를 호출하여 Entity를 저장한다")
        void persist_ShouldCallRepositorySave() {
            // given
            String sessionId = UUID.randomUUID().toString();
            UploadSessionId uploadSessionId = UploadSessionId.of(UUID.fromString(sessionId));
            CompletedPart completedPart = createCompletedPart(sessionId, 1);
            CompletedPartJpaEntity entity = createEntity(sessionId, 1);

            when(mapper.toPartEntity(anyString(), any(CompletedPart.class))).thenReturn(entity);
            when(repository.save(entity)).thenReturn(entity);
            when(mapper.toCompletedPart(entity)).thenReturn(completedPart);

            // when
            adapter.persist(uploadSessionId, completedPart);

            // then
            verify(repository).save(entity);
        }

        @Test
        @DisplayName("저장된 Entity를 Domain으로 변환하여 반환한다")
        void persist_ShouldCallMapperToCompletedPart() {
            // given
            String sessionId = UUID.randomUUID().toString();
            UploadSessionId uploadSessionId = UploadSessionId.of(UUID.fromString(sessionId));
            CompletedPart completedPart = createCompletedPart(sessionId, 1);
            CompletedPartJpaEntity entity = createEntity(sessionId, 1);

            when(mapper.toPartEntity(anyString(), any(CompletedPart.class))).thenReturn(entity);
            when(repository.save(entity)).thenReturn(entity);
            when(mapper.toCompletedPart(entity)).thenReturn(completedPart);

            // when
            adapter.persist(uploadSessionId, completedPart);

            // then
            verify(mapper).toCompletedPart(entity);
        }
    }

    @Nested
    @DisplayName("persistAll 테스트")
    class PersistAllTest {

        @Test
        @DisplayName("여러 CompletedPart를 일괄 저장한다")
        void persistAll_WithValidParts_ShouldSaveAll() {
            // given
            String sessionId = UUID.randomUUID().toString();
            CompletedPart part1 = createCompletedPart(sessionId, 1);
            CompletedPart part2 = createCompletedPart(sessionId, 2);
            CompletedPartJpaEntity entity1 = createEntity(sessionId, 1);
            CompletedPartJpaEntity entity2 = createEntity(sessionId, 2);

            when(mapper.toPartEntity(anyString(), any(CompletedPart.class)))
                    .thenReturn(entity1)
                    .thenReturn(entity2);

            // when
            adapter.persistAll(List.of(part1, part2));

            // then
            verify(repository).saveAll(any());
        }

        @Test
        @DisplayName("빈 목록이면 saveAll을 빈 목록으로 호출한다")
        void persistAll_WithEmptyList_ShouldSaveEmptyList() {
            // when
            adapter.persistAll(List.of());

            // then
            verify(repository).saveAll(List.of());
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
