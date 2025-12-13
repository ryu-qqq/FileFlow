package com.ryuqq.fileflow.application.download.manager.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.fileflow.application.download.port.out.command.ExternalDownloadPersistencePort;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.fixture.ExternalDownloadFixture;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExternalDownloadTransactionManager 테스트")
class ExternalDownloadTransactionManagerTest {

    @Mock private ExternalDownloadPersistencePort persistencePort;

    @InjectMocks private ExternalDownloadTransactionManager manager;

    @Test
    @DisplayName("ExternalDownload를 저장하고 ID를 반환한다")
    void shouldPersistAndReturnId() {
        // given
        ExternalDownload download = ExternalDownloadFixture.pendingExternalDownload();
        ExternalDownloadId expectedId =
                ExternalDownloadId.of("00000000-0000-0000-0000-000000000001");

        given(persistencePort.persist(download)).willReturn(expectedId);

        // when
        ExternalDownloadId result = manager.persist(download);

        // then
        assertThat(result).isEqualTo(expectedId);
        verify(persistencePort).persist(download);
    }

    @Test
    @DisplayName("신규 ExternalDownload를 저장할 수 있다")
    void shouldPersistNewExternalDownload() {
        // given
        ExternalDownload newDownload = ExternalDownloadFixture.pendingDownload();
        ExternalDownloadId generatedId =
                ExternalDownloadId.of("00000000-0000-0000-0000-0000000003e7");

        given(persistencePort.persist(any(ExternalDownload.class))).willReturn(generatedId);

        // when
        ExternalDownloadId result = manager.persist(newDownload);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getValue()).isEqualTo("00000000-0000-0000-0000-0000000003e7");
        verify(persistencePort).persist(newDownload);
    }

    @Test
    @DisplayName("PROCESSING 상태의 ExternalDownload를 저장할 수 있다")
    void shouldPersistProcessingExternalDownload() {
        // given
        ExternalDownload processingDownload = ExternalDownloadFixture.processingExternalDownload();
        ExternalDownloadId expectedId =
                ExternalDownloadId.of("00000000-0000-0000-0000-000000000002");

        given(persistencePort.persist(processingDownload)).willReturn(expectedId);

        // when
        ExternalDownloadId result = manager.persist(processingDownload);

        // then
        assertThat(result).isEqualTo(expectedId);
        verify(persistencePort).persist(processingDownload);
    }
}
