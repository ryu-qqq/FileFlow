package com.ryuqq.fileflow.adapter.out.persistence.session.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.adapter.out.persistence.session.SingleUploadSessionJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.SingleUploadSessionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.mapper.SingleUploadSessionJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.session.repository.SingleUploadSessionQueryDslRepository;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSessionFixture;
import com.ryuqq.fileflow.domain.session.id.SingleUploadSessionId;
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
@DisplayName("SingleUploadSessionQueryAdapter 단위 테스트")
class SingleUploadSessionQueryAdapterTest {

    @InjectMocks private SingleUploadSessionQueryAdapter queryAdapter;
    @Mock private SingleUploadSessionQueryDslRepository queryDslRepository;
    @Mock private SingleUploadSessionJpaMapper mapper;

    @Nested
    @DisplayName("findById 메서드 테스트")
    class FindByIdTest {

        @Test
        @DisplayName("존재하는 ID로 조회하면 도메인 객체를 반환합니다")
        void findById_existingId_shouldReturnDomain() {
            // given
            SingleUploadSessionId id = SingleUploadSessionId.of("single-session-001");
            SingleUploadSessionJpaEntity entity =
                    SingleUploadSessionJpaEntityFixture.aCreatedEntity();
            SingleUploadSession domain = SingleUploadSessionFixture.aCreatedSession();

            given(queryDslRepository.findById(id.value())).willReturn(Optional.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // when
            Optional<SingleUploadSession> result = queryAdapter.findById(id);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().idValue()).isEqualTo(id.value());
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 빈 Optional을 반환합니다")
        void findById_nonExistingId_shouldReturnEmpty() {
            // given
            SingleUploadSessionId id = SingleUploadSessionId.of("non-existing-id");
            given(queryDslRepository.findById(id.value())).willReturn(Optional.empty());

            // when
            Optional<SingleUploadSession> result = queryAdapter.findById(id);

            // then
            assertThat(result).isEmpty();
            then(mapper).shouldHaveNoInteractions();
        }
    }
}
