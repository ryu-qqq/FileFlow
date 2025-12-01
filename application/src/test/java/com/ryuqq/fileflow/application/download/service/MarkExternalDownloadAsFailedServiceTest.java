package com.ryuqq.fileflow.application.download.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ryuqq.fileflow.application.download.manager.ExternalDownloadManager;
import com.ryuqq.fileflow.application.download.port.out.query.ExternalDownloadQueryPort;
import com.ryuqq.fileflow.domain.common.util.ClockHolder;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.fixture.ExternalDownloadFixture;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("MarkExternalDownloadAsFailedService 테스트")
class MarkExternalDownloadAsFailedServiceTest {

    @Mock private ExternalDownloadQueryPort queryPort;

    @Mock private ExternalDownloadManager externalDownloadManager;

    @Mock private ClockHolder clockHolder;

    @InjectMocks private MarkExternalDownloadAsFailedService service;

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2025-11-26T12:00:00Z"), ZoneId.of("UTC"));

    @BeforeEach
    void setUp() {
        given(clockHolder.getClock()).willReturn(FIXED_CLOCK);
    }

    @Nested
    @DisplayName("markAsFailed 메서드")
    class MarkAsFailedTest {

        @Test
        @DisplayName("PROCESSING 상태의 다운로드를 FAILED로 변경하고 저장한다")
        void shouldMarkProcessingDownloadAsFailed() {
            // given
            String downloadId = "00000000-0000-0000-0000-000000000001";
            String errorMessage = "Connection timeout";

            ExternalDownload download =
                    ExternalDownloadFixture.withId(downloadId)
                            .status(ExternalDownloadStatus.PROCESSING)
                            .build();

            given(queryPort.findById(ExternalDownloadId.of(downloadId)))
                    .willReturn(Optional.of(download));

            // when
            service.markAsFailed(downloadId, errorMessage);

            // then
            verify(queryPort).findById(ExternalDownloadId.of(downloadId));
            verify(externalDownloadManager).save(download);
        }

        @Test
        @DisplayName("존재하지 않는 다운로드 ID인 경우 저장하지 않는다")
        void shouldNotSaveWhenDownloadNotFound() {
            // given
            String downloadId = "00000000-0000-0000-0000-0000000003e7";
            String errorMessage = "Some error";

            given(queryPort.findById(ExternalDownloadId.of(downloadId)))
                    .willReturn(Optional.empty());

            // when
            service.markAsFailed(downloadId, errorMessage);

            // then
            verify(queryPort).findById(ExternalDownloadId.of(downloadId));
            verify(externalDownloadManager, never()).save(any());
        }

        @Test
        @DisplayName("이미 COMPLETED 상태인 경우 저장하지 않는다")
        void shouldNotSaveWhenAlreadyCompleted() {
            // given
            String downloadId = "00000000-0000-0000-0000-000000000001";
            String errorMessage = "Error after completion";

            ExternalDownload download =
                    ExternalDownloadFixture.withId(downloadId)
                            .status(ExternalDownloadStatus.COMPLETED)
                            .build();

            given(queryPort.findById(ExternalDownloadId.of(downloadId)))
                    .willReturn(Optional.of(download));

            // when
            service.markAsFailed(downloadId, errorMessage);

            // then
            verify(queryPort).findById(ExternalDownloadId.of(downloadId));
            verify(externalDownloadManager, never()).save(any());
        }

        @Test
        @DisplayName("이미 FAILED 상태인 경우 저장하지 않는다")
        void shouldNotSaveWhenAlreadyFailed() {
            // given
            String downloadId = "00000000-0000-0000-0000-000000000001";
            String errorMessage = "Second failure";

            ExternalDownload download =
                    ExternalDownloadFixture.withId(downloadId)
                            .status(ExternalDownloadStatus.FAILED)
                            .failed("First failure")
                            .build();

            given(queryPort.findById(ExternalDownloadId.of(downloadId)))
                    .willReturn(Optional.of(download));

            // when
            service.markAsFailed(downloadId, errorMessage);

            // then
            verify(queryPort).findById(ExternalDownloadId.of(downloadId));
            verify(externalDownloadManager, never()).save(any());
        }

        @Test
        @DisplayName("PENDING 상태에서도 FAILED로 변경할 수 있다")
        void shouldMarkPendingDownloadAsFailed() {
            // given
            String downloadId = "00000000-0000-0000-0000-000000000001";
            String errorMessage = "Validation failed";

            ExternalDownload download =
                    ExternalDownloadFixture.withId(downloadId)
                            .status(ExternalDownloadStatus.PENDING)
                            .build();

            given(queryPort.findById(ExternalDownloadId.of(downloadId)))
                    .willReturn(Optional.of(download));

            // when
            service.markAsFailed(downloadId, errorMessage);

            // then
            verify(queryPort).findById(ExternalDownloadId.of(downloadId));
            verify(externalDownloadManager).save(download);
        }

        @Test
        @DisplayName("에러 메시지와 함께 실패 처리된다")
        void shouldSaveWithErrorMessage() {
            // given
            String downloadId = "00000000-0000-0000-0000-000000000001";
            String errorMessage = "Network error: Connection refused";

            ExternalDownload download =
                    ExternalDownloadFixture.withId(downloadId)
                            .status(ExternalDownloadStatus.PROCESSING)
                            .build();

            given(queryPort.findById(ExternalDownloadId.of(downloadId)))
                    .willReturn(Optional.of(download));

            // when
            service.markAsFailed(downloadId, errorMessage);

            // then
            verify(externalDownloadManager).save(download);
        }

        @Test
        @DisplayName("Clock의 현재 시간을 사용하여 실패 처리한다")
        void shouldUseClockForFailedTime() {
            // given
            String downloadId = "00000000-0000-0000-0000-000000000001";
            String errorMessage = "Timeout error";

            ExternalDownload download =
                    ExternalDownloadFixture.withId(downloadId)
                            .status(ExternalDownloadStatus.PROCESSING)
                            .build();

            given(queryPort.findById(ExternalDownloadId.of(downloadId)))
                    .willReturn(Optional.of(download));

            // when
            service.markAsFailed(downloadId, errorMessage);

            // then
            verify(clockHolder).getClock();
            verify(externalDownloadManager).save(download);
        }
    }
}
