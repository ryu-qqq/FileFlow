package com.ryuqq.fileflow.application.transform.manager.client;

import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.transform.port.out.client.TransformQueueClient;
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
@DisplayName("TransformQueueManager 단위 테스트")
class TransformQueueManagerTest {

    @InjectMocks private TransformQueueManager sut;
    @Mock private TransformQueueClient transformQueueClient;

    @Nested
    @DisplayName("enqueue 메서드")
    class EnqueueTest {

        @Test
        @DisplayName("변환 요청 ID를 큐 클라이언트에 위임한다")
        void enqueue_ValidId_DelegatesToClient() {
            // given
            String transformRequestId = "transform-001";

            // when
            sut.enqueue(transformRequestId);

            // then
            then(transformQueueClient).should().enqueue(transformRequestId);
        }
    }
}
