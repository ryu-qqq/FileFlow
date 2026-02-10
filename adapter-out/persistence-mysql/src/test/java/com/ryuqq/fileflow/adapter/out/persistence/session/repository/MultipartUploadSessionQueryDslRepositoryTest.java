package com.ryuqq.fileflow.adapter.out.persistence.session.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.out.persistence.common.AbstractRepositoryIntegrationTest;
import com.ryuqq.fileflow.adapter.out.persistence.session.CompletedPartJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.session.MultipartUploadSessionJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.session.condition.SessionConditionBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({SessionConditionBuilder.class, MultipartUploadSessionQueryDslRepository.class})
@DisplayName("MultipartUploadSessionQueryDslRepository 통합 테스트")
class MultipartUploadSessionQueryDslRepositoryTest extends AbstractRepositoryIntegrationTest {

    @Autowired private MultipartUploadSessionQueryDslRepository queryDslRepository;

    @Autowired private MultipartUploadSessionJpaRepository sessionJpaRepository;

    @Autowired private CompletedPartJpaRepository partJpaRepository;

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("존재하는 ID로 조회하면 엔티티를 반환한다")
        void returnsEntityWhenExists() {
            var entity = MultipartUploadSessionJpaEntityFixture.anInitiatedEntity();
            sessionJpaRepository.save(entity);
            flushAndClear();

            var result = queryDslRepository.findById("multipart-session-001");

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo("multipart-session-001");
            assertThat(result.get().getUploadId()).isEqualTo(entity.getUploadId());
            assertThat(result.get().getStatus()).isEqualTo(entity.getStatus());
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 빈 Optional을 반환한다")
        void returnsEmptyWhenNotExists() {
            var result = queryDslRepository.findById("non-existent-id");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findCompletedPartsBySessionId")
    class FindCompletedPartsBySessionId {

        @Test
        @DisplayName("세션의 완료된 파트를 partNumber 오름차순으로 반환한다")
        void returnsPartsOrderedByPartNumber() {
            var session = MultipartUploadSessionJpaEntityFixture.anInitiatedEntity();
            sessionJpaRepository.save(session);

            var part3 =
                    CompletedPartJpaEntityFixture.aCompletedPartEntity("multipart-session-001", 3);
            var part1 =
                    CompletedPartJpaEntityFixture.aCompletedPartEntity("multipart-session-001", 1);
            var part2 =
                    CompletedPartJpaEntityFixture.aCompletedPartEntity("multipart-session-001", 2);
            partJpaRepository.save(part3);
            partJpaRepository.save(part1);
            partJpaRepository.save(part2);
            flushAndClear();

            var result = queryDslRepository.findCompletedPartsBySessionId("multipart-session-001");

            assertThat(result).hasSize(3);
            assertThat(result.get(0).getPartNumber()).isEqualTo(1);
            assertThat(result.get(1).getPartNumber()).isEqualTo(2);
            assertThat(result.get(2).getPartNumber()).isEqualTo(3);
        }

        @Test
        @DisplayName("파트가 없으면 빈 리스트를 반환한다")
        void returnsEmptyListWhenNoParts() {
            var result = queryDslRepository.findCompletedPartsBySessionId("no-parts-session");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("다른 세션의 파트는 포함하지 않는다")
        void excludesPartsFromOtherSessions() {
            var session1 = MultipartUploadSessionJpaEntityFixture.anEntityWithId("session-A");
            var session2 = MultipartUploadSessionJpaEntityFixture.anEntityWithId("session-B");
            sessionJpaRepository.save(session1);
            sessionJpaRepository.save(session2);

            partJpaRepository.save(
                    CompletedPartJpaEntityFixture.aCompletedPartEntity("session-A", 1));
            partJpaRepository.save(
                    CompletedPartJpaEntityFixture.aCompletedPartEntity("session-B", 1));
            partJpaRepository.save(
                    CompletedPartJpaEntityFixture.aCompletedPartEntity("session-B", 2));
            flushAndClear();

            var result = queryDslRepository.findCompletedPartsBySessionId("session-A");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSessionId()).isEqualTo("session-A");
        }
    }
}
