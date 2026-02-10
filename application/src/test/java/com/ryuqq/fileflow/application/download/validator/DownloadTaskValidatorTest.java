package com.ryuqq.fileflow.application.download.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.download.manager.query.DownloadReadManager;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTaskFixture;
import com.ryuqq.fileflow.domain.download.exception.DownloadTaskNotFoundException;
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
@DisplayName("DownloadTaskValidator 단위 테스트")
class DownloadTaskValidatorTest {

    @InjectMocks private DownloadTaskValidator sut;
    @Mock private DownloadReadManager downloadReadManager;

    @Nested
    @DisplayName("getExistingTask 메서드")
    class GetExistingTaskTest {

        @Test
        @DisplayName("존재하는 태스크 ID로 DownloadTask를 반환한다")
        void getExistingTask_ExistingId_ReturnsTask() {
            // given
            String downloadTaskId = "download-001";
            DownloadTask expectedTask = DownloadTaskFixture.aQueuedTask();

            given(downloadReadManager.getDownloadTask(downloadTaskId)).willReturn(expectedTask);

            // when
            DownloadTask result = sut.getExistingTask(downloadTaskId);

            // then
            assertThat(result).isEqualTo(expectedTask);
            then(downloadReadManager).should().getDownloadTask(downloadTaskId);
        }

        @Test
        @DisplayName("존재하지 않는 태스크 ID로 DownloadTaskNotFoundException을 던진다")
        void getExistingTask_NonExistingId_ThrowsNotFoundException() {
            // given
            String downloadTaskId = "non-existing-task";

            given(downloadReadManager.getDownloadTask(downloadTaskId))
                    .willThrow(new DownloadTaskNotFoundException(downloadTaskId));

            // when & then
            assertThatThrownBy(() -> sut.getExistingTask(downloadTaskId))
                    .isInstanceOf(DownloadTaskNotFoundException.class);
        }
    }
}
