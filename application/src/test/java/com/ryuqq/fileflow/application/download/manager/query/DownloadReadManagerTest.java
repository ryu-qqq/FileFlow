package com.ryuqq.fileflow.application.download.manager.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.ryuqq.fileflow.application.download.port.out.query.DownloadTaskQueryPort;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTaskFixture;
import com.ryuqq.fileflow.domain.download.exception.DownloadTaskNotFoundException;
import com.ryuqq.fileflow.domain.download.id.DownloadTaskId;
import com.ryuqq.fileflow.domain.download.vo.DownloadTaskStatus;
import java.time.Instant;
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
@DisplayName("DownloadReadManager 단위 테스트")
class DownloadReadManagerTest {

    @InjectMocks private DownloadReadManager sut;
    @Mock private DownloadTaskQueryPort downloadTaskQueryPort;

    @Nested
    @DisplayName("getDownloadTask 메서드")
    class GetDownloadTaskTest {

        @Test
        @DisplayName("존재하는 태스크 ID로 DownloadTask를 반환한다")
        void getDownloadTask_ExistingId_ReturnsTask() {
            // given
            String downloadTaskId = "download-001";
            DownloadTask expectedTask = DownloadTaskFixture.aQueuedTask();

            given(downloadTaskQueryPort.findById(DownloadTaskId.of(downloadTaskId)))
                    .willReturn(Optional.of(expectedTask));

            // when
            DownloadTask result = sut.getDownloadTask(downloadTaskId);

            // then
            assertThat(result).isEqualTo(expectedTask);
        }

        @Test
        @DisplayName("존재하지 않는 태스크 ID로 DownloadTaskNotFoundException을 던진다")
        void getDownloadTask_NonExistingId_ThrowsNotFoundException() {
            // given
            String downloadTaskId = "non-existing-task";

            given(downloadTaskQueryPort.findById(DownloadTaskId.of(downloadTaskId)))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sut.getDownloadTask(downloadTaskId))
                    .isInstanceOf(DownloadTaskNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getStaleQueuedTasks 메서드")
    class GetStaleQueuedTasksTest {

        @Test
        @DisplayName("QUEUED 상태의 오래된 태스크 목록을 반환한다")
        void getStaleQueuedTasks_ReturnsStaleTaskList() {
            // given
            Instant createdBefore = Instant.parse("2026-01-01T00:05:00Z");
            int limit = 100;

            DownloadTask staleTask = DownloadTaskFixture.aQueuedTask();
            List<DownloadTask> expectedTasks = List.of(staleTask);

            given(
                            downloadTaskQueryPort.findByStatusAndCreatedBefore(
                                    DownloadTaskStatus.QUEUED, createdBefore, limit))
                    .willReturn(expectedTasks);

            // when
            List<DownloadTask> result = sut.getStaleQueuedTasks(createdBefore, limit);

            // then
            assertThat(result).hasSize(1);
            assertThat(result).isEqualTo(expectedTasks);
        }

        @Test
        @DisplayName("오래된 태스크가 없으면 빈 목록을 반환한다")
        void getStaleQueuedTasks_NoStaleTasks_ReturnsEmptyList() {
            // given
            Instant createdBefore = Instant.parse("2026-01-01T00:05:00Z");
            int limit = 100;

            given(
                            downloadTaskQueryPort.findByStatusAndCreatedBefore(
                                    DownloadTaskStatus.QUEUED, createdBefore, limit))
                    .willReturn(List.of());

            // when
            List<DownloadTask> result = sut.getStaleQueuedTasks(createdBefore, limit);

            // then
            assertThat(result).isEmpty();
        }
    }
}
