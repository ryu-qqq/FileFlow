package com.ryuqq.fileflow.application.download.manager;

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
@DisplayName("ExternalDownloadManager 테스트")
class ExternalDownloadManagerTest {

    @Mock private ExternalDownloadPersistencePort persistencePort;

    @InjectMocks private ExternalDownloadManager manager;

    @Test
    @DisplayName("ExternalDownload를 저장하고 ID를 반환한다")
    void shouldSaveAndReturnId() {
        // given
        ExternalDownload download = ExternalDownloadFixture.pendingExternalDownload();
        ExternalDownloadId expectedId = ExternalDownloadId.of(1L);

        given(persistencePort.persist(download)).willReturn(expectedId);

        // when
        ExternalDownloadId result = manager.save(download);

        // then
        assertThat(result).isEqualTo(expectedId);
        verify(persistencePort).persist(download);
    }

    @Test
    @DisplayName("신규 ExternalDownload를 저장할 수 있다")
    void shouldSaveNewExternalDownload() {
        // given
        ExternalDownload newDownload = ExternalDownloadFixture.pendingDownload();
        ExternalDownloadId generatedId = ExternalDownloadId.of(999L);

        given(persistencePort.persist(any(ExternalDownload.class))).willReturn(generatedId);

        // when
        ExternalDownloadId result = manager.save(newDownload);

        // then
        assertThat(result).isNotNull();
        assertThat(result.value()).isEqualTo(999L);
        verify(persistencePort).persist(newDownload);
    }

    @Test
    @DisplayName("PROCESSING 상태의 ExternalDownload를 저장할 수 있다")
    void shouldSaveProcessingExternalDownload() {
        // given
        ExternalDownload processingDownload = ExternalDownloadFixture.processingExternalDownload();
        ExternalDownloadId expectedId = ExternalDownloadId.of(2L);

        given(persistencePort.persist(processingDownload)).willReturn(expectedId);

        // when
        ExternalDownloadId result = manager.save(processingDownload);

        // then
        assertThat(result).isEqualTo(expectedId);
        verify(persistencePort).persist(processingDownload);
    }
}
