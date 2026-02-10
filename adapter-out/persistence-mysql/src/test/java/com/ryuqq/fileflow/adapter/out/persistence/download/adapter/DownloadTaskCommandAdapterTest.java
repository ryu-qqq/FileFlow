package com.ryuqq.fileflow.adapter.out.persistence.download.adapter;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.adapter.out.persistence.download.DownloadTaskJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.download.entity.DownloadTaskJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.download.mapper.DownloadTaskJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.DownloadTaskJpaRepository;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTaskFixture;
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
@DisplayName("DownloadTaskCommandAdapter 단위 테스트")
class DownloadTaskCommandAdapterTest {

    @InjectMocks private DownloadTaskCommandAdapter commandAdapter;
    @Mock private DownloadTaskJpaRepository jpaRepository;
    @Mock private DownloadTaskJpaMapper mapper;

    @Nested
    @DisplayName("persist 메서드 테스트")
    class PersistTest {

        @Test
        @DisplayName("도메인 객체를 엔티티로 변환하여 저장합니다")
        void persist_shouldMapAndSave() {
            // given
            DownloadTask task = DownloadTaskFixture.aQueuedTask();
            DownloadTaskJpaEntity entity = DownloadTaskJpaEntityFixture.aQueuedEntity();
            given(mapper.toEntity(task)).willReturn(entity);

            // when
            commandAdapter.persist(task);

            // then
            then(mapper).should().toEntity(task);
            then(jpaRepository).should().save(entity);
        }
    }
}
