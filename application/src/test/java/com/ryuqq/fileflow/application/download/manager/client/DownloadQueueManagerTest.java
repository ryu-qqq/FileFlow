package com.ryuqq.fileflow.application.download.manager.client;

import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.download.port.out.client.DownloadQueueClient;
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
@DisplayName("DownloadQueueManager 단위 테스트")
class DownloadQueueManagerTest {

    @InjectMocks private DownloadQueueManager sut;
    @Mock private DownloadQueueClient downloadQueueClient;

    @Nested
    @DisplayName("enqueue 메서드")
    class EnqueueTest {

        @Test
        @DisplayName("다운로드 태스크 ID를 큐 클라이언트에 위임한다")
        void enqueue_DelegatesToClient() {
            // given
            String downloadTaskId = "download-001";

            // when
            sut.enqueue(downloadTaskId);

            // then
            then(downloadQueueClient).should().enqueue(downloadTaskId);
        }
    }
}
