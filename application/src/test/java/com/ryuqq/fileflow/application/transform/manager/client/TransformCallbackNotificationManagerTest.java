package com.ryuqq.fileflow.application.transform.manager.client;

import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.transform.dto.response.TransformCallbackPayload;
import com.ryuqq.fileflow.application.transform.port.out.client.TransformCallbackNotificationClient;
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
@DisplayName("TransformCallbackNotificationManager 단위 테스트")
class TransformCallbackNotificationManagerTest {

    @InjectMocks private TransformCallbackNotificationManager sut;
    @Mock private TransformCallbackNotificationClient transformCallbackNotificationClient;

    @Nested
    @DisplayName("notify 메서드")
    class NotifyTest {

        @Test
        @DisplayName("콜백 URL과 페이로드를 클라이언트에 위임한다")
        void notify_DelegatesToClient() {
            // given
            String callbackUrl = "https://callback.example.com/transform-done";
            TransformCallbackPayload payload =
                    TransformCallbackPayload.ofCompleted(
                            "transform-001",
                            "asset-001",
                            "result-001",
                            "RESIZE",
                            800,
                            600,
                            null,
                            null);

            // when
            sut.notify(callbackUrl, payload);

            // then
            then(transformCallbackNotificationClient).should().notify(callbackUrl, payload);
        }
    }
}
