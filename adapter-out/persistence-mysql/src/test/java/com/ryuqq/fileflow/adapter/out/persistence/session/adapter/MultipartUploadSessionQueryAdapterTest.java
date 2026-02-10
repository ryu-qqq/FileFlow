package com.ryuqq.fileflow.adapter.out.persistence.session.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.adapter.out.persistence.session.CompletedPartJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.session.MultipartUploadSessionJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.CompletedPartJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.MultipartUploadSessionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.mapper.MultipartUploadSessionJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.session.repository.MultipartUploadSessionQueryDslRepository;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSessionFixture;
import com.ryuqq.fileflow.domain.session.id.MultipartUploadSessionId;
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
@DisplayName("MultipartUploadSessionQueryAdapter 단위 테스트")
class MultipartUploadSessionQueryAdapterTest {

    @InjectMocks private MultipartUploadSessionQueryAdapter queryAdapter;
    @Mock private MultipartUploadSessionQueryDslRepository queryDslRepository;
    @Mock private MultipartUploadSessionJpaMapper mapper;

    @Nested
    @DisplayName("findById 메서드 테스트")
    class FindByIdTest {

        @Test
        @DisplayName("존재하는 ID로 조회하면 파트와 함께 도메인 객체를 반환합니다")
        void findById_existingId_shouldReturnDomainWithParts() {
            // given
            MultipartUploadSessionId id = MultipartUploadSessionId.of("multipart-session-001");
            MultipartUploadSessionJpaEntity entity =
                    MultipartUploadSessionJpaEntityFixture.anInitiatedEntity();
            List<CompletedPartJpaEntity> parts =
                    List.of(CompletedPartJpaEntityFixture.aCompletedPartEntity(id.value(), 1));
            MultipartUploadSession domain = MultipartUploadSessionFixture.anUploadingSession();

            given(queryDslRepository.findById(id.value())).willReturn(Optional.of(entity));
            given(queryDslRepository.findCompletedPartsBySessionId(id.value())).willReturn(parts);
            given(mapper.toDomain(entity, parts)).willReturn(domain);

            // when
            Optional<MultipartUploadSession> result = queryAdapter.findById(id);

            // then
            assertThat(result).isPresent();
            then(queryDslRepository).should().findCompletedPartsBySessionId(id.value());
            then(mapper).should().toDomain(entity, parts);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 빈 Optional을 반환합니다")
        void findById_nonExistingId_shouldReturnEmpty() {
            // given
            MultipartUploadSessionId id = MultipartUploadSessionId.of("non-existing-id");
            given(queryDslRepository.findById(id.value())).willReturn(Optional.empty());

            // when
            Optional<MultipartUploadSession> result = queryAdapter.findById(id);

            // then
            assertThat(result).isEmpty();
            then(mapper).shouldHaveNoInteractions();
        }
    }
}
