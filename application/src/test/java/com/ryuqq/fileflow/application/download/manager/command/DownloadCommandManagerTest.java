package com.ryuqq.fileflow.application.download.manager.command;

import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.download.port.out.command.DownloadTaskPersistencePort;
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
@DisplayName("DownloadCommandManager 단위 테스트")
class DownloadCommandManagerTest {

    @InjectMocks private DownloadCommandManager sut;
    @Mock private DownloadTaskPersistencePort downloadTaskPersistencePort;

    @Nested
    @DisplayName("persist 메서드")
    class PersistTest {

        @Test
        @DisplayName("DownloadTask를 영속화 포트에 위임한다")
        void persist_DownloadTask_DelegatesToPort() {
            // given
            DownloadTask downloadTask = DownloadTaskFixture.aQueuedTask();

            // when
            sut.persist(downloadTask);

            // then
            then(downloadTaskPersistencePort).should().persist(downloadTask);
        }
    }
}
