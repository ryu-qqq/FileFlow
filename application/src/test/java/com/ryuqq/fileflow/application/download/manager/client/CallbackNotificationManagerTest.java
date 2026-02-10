package com.ryuqq.fileflow.application.download.manager.client;

import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.download.port.out.client.CallbackNotificationClient;
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
@DisplayName("CallbackNotificationManager 단위 테스트")
class CallbackNotificationManagerTest {

    @InjectMocks private CallbackNotificationManager sut;
    @Mock private CallbackNotificationClient callbackNotificationClient;

    @Nested
    @DisplayName("notify 메서드")
    class NotifyTest {

        @Test
        @DisplayName("콜백 알림을 클라이언트에 위임한다")
        void notify_DelegatesToClient() {
            // given
            String callbackUrl = "https://callback.example.com/done";
            String downloadTaskId = "download-001";
            String status = "COMPLETED";

            // when
            sut.notify(callbackUrl, downloadTaskId, status);

            // then
            then(callbackNotificationClient).should().notify(callbackUrl, downloadTaskId, status);
        }
    }
}
