package com.ryuqq.fileflow.adapter.out.persistence.session.adapter;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.adapter.out.persistence.session.entity.SingleUploadSessionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.mapper.SingleUploadSessionJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.session.repository.SingleUploadSessionJpaRepository;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSessionFixture;
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
@DisplayName("SingleUploadSessionCommandAdapter 단위 테스트")
class SingleUploadSessionCommandAdapterTest {

    @InjectMocks private SingleUploadSessionCommandAdapter commandAdapter;
    @Mock private SingleUploadSessionJpaRepository jpaRepository;
    @Mock private SingleUploadSessionJpaMapper mapper;

    @Nested
    @DisplayName("persist 메서드 테스트")
    class PersistTest {

        @Test
        @DisplayName("도메인 객체를 엔티티로 변환하여 저장합니다")
        void persist_shouldMapAndSave() {
            // given
            SingleUploadSession session = SingleUploadSessionFixture.aCreatedSession();
            SingleUploadSessionJpaEntity entity =
                    SingleUploadSessionJpaEntity.create(
                            session.idValue(),
                            session.s3Key(),
                            session.bucket(),
                            session.accessType(),
                            session.fileName(),
                            session.contentType(),
                            session.presignedUrlValue(),
                            session.purposeValue(),
                            session.sourceValue(),
                            session.status(),
                            session.expiresAt(),
                            session.createdAt(),
                            session.updatedAt());
            given(mapper.toEntity(session)).willReturn(entity);

            // when
            commandAdapter.persist(session);

            // then
            then(mapper).should().toEntity(session);
            then(jpaRepository).should().save(entity);
        }
    }
}
