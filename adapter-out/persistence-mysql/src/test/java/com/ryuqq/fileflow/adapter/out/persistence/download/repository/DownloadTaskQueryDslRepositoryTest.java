package com.ryuqq.fileflow.adapter.out.persistence.download.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.out.persistence.common.AbstractRepositoryIntegrationTest;
import com.ryuqq.fileflow.adapter.out.persistence.download.DownloadTaskJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.download.condition.DownloadConditionBuilder;
import com.ryuqq.fileflow.adapter.out.persistence.download.entity.DownloadTaskJpaEntity;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.domain.download.vo.DownloadTaskStatus;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({DownloadConditionBuilder.class, DownloadTaskQueryDslRepository.class})
@DisplayName("DownloadTaskQueryDslRepository 통합 테스트")
class DownloadTaskQueryDslRepositoryTest extends AbstractRepositoryIntegrationTest {

    @Autowired private DownloadTaskQueryDslRepository queryDslRepository;

    @Autowired private DownloadTaskJpaRepository jpaRepository;

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("존재하는 ID로 조회하면 엔티티를 반환한다")
        void returnsEntityWhenExists() {
            var entity = DownloadTaskJpaEntityFixture.aQueuedEntity();
            jpaRepository.save(entity);
            flushAndClear();

            var result = queryDslRepository.findById("download-001");

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo("download-001");
            assertThat(result.get().getStatus()).isEqualTo(DownloadTaskStatus.QUEUED);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 빈 Optional을 반환한다")
        void returnsEmptyWhenNotExists() {
            var result = queryDslRepository.findById("non-existent-id");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByStatusAndCreatedBefore")
    class FindByStatusAndCreatedBefore {

        private static final Instant BASE_TIME = Instant.parse("2026-01-01T00:00:00Z");
        private static final Instant ONE_HOUR_LATER = BASE_TIME.plusSeconds(3600);
        private static final Instant TWO_HOURS_LATER = BASE_TIME.plusSeconds(7200);
        private static final Instant FAR_FUTURE = Instant.parse("2027-01-01T00:00:00Z");

        @Test
        @DisplayName("상태와 생성시간 조건에 맞는 엔티티를 반환한다")
        void returnsMatchingEntities() {
            var entity = DownloadTaskJpaEntityFixture.aQueuedEntity();
            jpaRepository.save(entity);
            flushAndClear();

            var result =
                    queryDslRepository.findByStatusAndCreatedBefore(
                            DownloadTaskStatus.QUEUED, FAR_FUTURE, 10);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo("download-001");
        }

        @Test
        @DisplayName("상태가 다른 엔티티는 포함하지 않는다")
        void excludesDifferentStatus() {
            var entity = DownloadTaskJpaEntityFixture.aCompletedEntity();
            jpaRepository.save(entity);
            flushAndClear();

            var result =
                    queryDslRepository.findByStatusAndCreatedBefore(
                            DownloadTaskStatus.QUEUED, FAR_FUTURE, 10);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("createdBefore 이후에 생성된 엔티티는 포함하지 않는다")
        void excludesEntitiesCreatedAfterCutoff() {
            var entity = DownloadTaskJpaEntityFixture.aQueuedEntity();
            jpaRepository.save(entity);
            flushAndClear();

            var result =
                    queryDslRepository.findByStatusAndCreatedBefore(
                            DownloadTaskStatus.QUEUED, Instant.parse("2025-01-01T00:00:00Z"), 10);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("limit 개수만큼만 반환한다")
        void respectsLimit() {
            jpaRepository.save(DownloadTaskJpaEntityFixture.anEntityWithId("dl-limit-1"));
            jpaRepository.save(DownloadTaskJpaEntityFixture.anEntityWithId("dl-limit-2"));
            jpaRepository.save(DownloadTaskJpaEntityFixture.anEntityWithId("dl-limit-3"));
            flushAndClear();

            var result =
                    queryDslRepository.findByStatusAndCreatedBefore(
                            DownloadTaskStatus.QUEUED, FAR_FUTURE, 2);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("createdAt 오름차순으로 정렬된다")
        void orderedByCreatedAtAsc() {
            var later =
                    DownloadTaskJpaEntity.create(
                            "dl-later",
                            "https://example.com/image.jpg",
                            "test-bucket",
                            "public/later.jpg",
                            AccessType.PUBLIC,
                            "product-image",
                            "commerce-service",
                            DownloadTaskStatus.QUEUED,
                            0,
                            3,
                            null,
                            null,
                            TWO_HOURS_LATER,
                            TWO_HOURS_LATER,
                            null,
                            null);

            var earlier =
                    DownloadTaskJpaEntity.create(
                            "dl-earlier",
                            "https://example.com/image.jpg",
                            "test-bucket",
                            "public/earlier.jpg",
                            AccessType.PUBLIC,
                            "product-image",
                            "commerce-service",
                            DownloadTaskStatus.QUEUED,
                            0,
                            3,
                            null,
                            null,
                            ONE_HOUR_LATER,
                            ONE_HOUR_LATER,
                            null,
                            null);

            jpaRepository.save(later);
            jpaRepository.save(earlier);
            flushAndClear();

            var result =
                    queryDslRepository.findByStatusAndCreatedBefore(
                            DownloadTaskStatus.QUEUED, FAR_FUTURE, 10);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo("dl-earlier");
            assertThat(result.get(1).getId()).isEqualTo("dl-later");
        }
    }
}
