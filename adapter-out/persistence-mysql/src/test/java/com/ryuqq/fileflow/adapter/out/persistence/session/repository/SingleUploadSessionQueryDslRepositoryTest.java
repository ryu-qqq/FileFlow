package com.ryuqq.fileflow.adapter.out.persistence.session.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.out.persistence.common.AbstractRepositoryIntegrationTest;
import com.ryuqq.fileflow.adapter.out.persistence.session.SingleUploadSessionJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.session.condition.SessionConditionBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({SessionConditionBuilder.class, SingleUploadSessionQueryDslRepository.class})
@DisplayName("SingleUploadSessionQueryDslRepository 통합 테스트")
class SingleUploadSessionQueryDslRepositoryTest extends AbstractRepositoryIntegrationTest {

    @Autowired private SingleUploadSessionQueryDslRepository queryDslRepository;

    @Autowired private SingleUploadSessionJpaRepository jpaRepository;

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("존재하는 ID로 조회하면 엔티티를 반환한다")
        void returnsEntityWhenExists() {
            var entity = SingleUploadSessionJpaEntityFixture.aCreatedEntity();
            jpaRepository.save(entity);
            flushAndClear();

            var result = queryDslRepository.findById("single-session-001");

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo("single-session-001");
            assertThat(result.get().getStatus()).isEqualTo(entity.getStatus());
            assertThat(result.get().getFileName()).isEqualTo(entity.getFileName());
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 빈 Optional을 반환한다")
        void returnsEmptyWhenNotExists() {
            var result = queryDslRepository.findById("non-existent-id");

            assertThat(result).isEmpty();
        }
    }
}
