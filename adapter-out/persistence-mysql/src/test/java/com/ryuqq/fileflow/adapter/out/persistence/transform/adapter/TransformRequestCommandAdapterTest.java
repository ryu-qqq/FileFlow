package com.ryuqq.fileflow.adapter.out.persistence.transform.adapter;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.adapter.out.persistence.transform.TransformRequestJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.transform.entity.TransformRequestJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.transform.mapper.TransformRequestJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.transform.repository.TransformRequestJpaRepository;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequestFixture;
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
@DisplayName("TransformRequestCommandAdapter 단위 테스트")
class TransformRequestCommandAdapterTest {

    @InjectMocks private TransformRequestCommandAdapter commandAdapter;
    @Mock private TransformRequestJpaRepository jpaRepository;
    @Mock private TransformRequestJpaMapper mapper;

    @Nested
    @DisplayName("persist 메서드 테스트")
    class PersistTest {

        @Test
        @DisplayName("도메인 객체를 엔티티로 변환하여 저장합니다")
        void persist_shouldMapAndSave() {
            // given
            TransformRequest request = TransformRequestFixture.aResizeRequest();
            TransformRequestJpaEntity entity =
                    TransformRequestJpaEntityFixture.aQueuedResizeEntity();
            given(mapper.toEntity(request)).willReturn(entity);

            // when
            commandAdapter.persist(request);

            // then
            then(mapper).should().toEntity(request);
            then(jpaRepository).should().save(entity);
        }
    }
}
