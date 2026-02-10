package com.ryuqq.fileflow.adapter.out.persistence.session.adapter;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.ryuqq.fileflow.adapter.out.persistence.session.CompletedPartJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.session.MultipartUploadSessionJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.CompletedPartJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.MultipartUploadSessionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.mapper.MultipartUploadSessionJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.session.repository.CompletedPartJpaRepository;
import com.ryuqq.fileflow.adapter.out.persistence.session.repository.MultipartUploadSessionJpaRepository;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSessionFixture;
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
@DisplayName("MultipartUploadSessionCommandAdapter 단위 테스트")
class MultipartUploadSessionCommandAdapterTest {

    @InjectMocks private MultipartUploadSessionCommandAdapter commandAdapter;
    @Mock private MultipartUploadSessionJpaRepository jpaRepository;
    @Mock private CompletedPartJpaRepository completedPartJpaRepository;
    @Mock private MultipartUploadSessionJpaMapper mapper;

    @Nested
    @DisplayName("persist 메서드 테스트")
    class PersistTest {

        @Test
        @DisplayName("세션을 저장하고 기존 파트를 삭제 후 새 파트를 저장합니다")
        void persist_withParts_shouldSaveSessionAndParts() {
            // given
            MultipartUploadSession session = MultipartUploadSessionFixture.anUploadingSession();
            MultipartUploadSessionJpaEntity entity =
                    MultipartUploadSessionJpaEntityFixture.anInitiatedEntity();
            List<CompletedPartJpaEntity> partEntities =
                    List.of(
                            CompletedPartJpaEntityFixture.aCompletedPartEntity(
                                    session.idValue(), 1));

            given(mapper.toEntity(session)).willReturn(entity);
            given(mapper.toPartEntities(eq(session.idValue()), anyList())).willReturn(partEntities);

            // when
            commandAdapter.persist(session);

            // then
            then(jpaRepository).should().save(entity);
            then(completedPartJpaRepository).should().deleteBySessionId(session.idValue());
            then(completedPartJpaRepository).should().saveAll(partEntities);
        }

        @Test
        @DisplayName("파트가 없는 세션은 파트 저장을 건너뜁니다")
        void persist_withoutParts_shouldSkipPartSave() {
            // given
            MultipartUploadSession session = MultipartUploadSessionFixture.anInitiatedSession();
            MultipartUploadSessionJpaEntity entity =
                    MultipartUploadSessionJpaEntityFixture.anInitiatedEntity();

            given(mapper.toEntity(session)).willReturn(entity);
            given(mapper.toPartEntities(eq(session.idValue()), anyList())).willReturn(List.of());

            // when
            commandAdapter.persist(session);

            // then
            then(jpaRepository).should().save(entity);
            then(completedPartJpaRepository).should().deleteBySessionId(session.idValue());
            then(completedPartJpaRepository).should(never()).saveAll(anyList());
        }
    }
}
