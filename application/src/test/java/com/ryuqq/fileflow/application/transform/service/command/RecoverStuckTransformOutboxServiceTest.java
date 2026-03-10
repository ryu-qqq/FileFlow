package com.ryuqq.fileflow.application.transform.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.ryuqq.fileflow.application.transform.manager.command.TransformCallbackOutboxCommandManager;
import com.ryuqq.fileflow.application.transform.manager.command.TransformQueueOutboxCommandManager;
import java.time.Instant;
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
@DisplayName("RecoverStuckTransformOutboxService 단위 테스트")
class RecoverStuckTransformOutboxServiceTest {

    @InjectMocks private RecoverStuckTransformOutboxService sut;
    @Mock private TransformQueueOutboxCommandManager transformQueueOutboxCommandManager;
    @Mock private TransformCallbackOutboxCommandManager transformCallbackOutboxCommandManager;

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("복구 대상이 없으면 0을 반환한다")
        void execute_NoStuck_ReturnsZero() {
            given(transformQueueOutboxCommandManager.recoverStuckProcessing(any(Instant.class)))
                    .willReturn(0);
            given(transformCallbackOutboxCommandManager.recoverStuckProcessing(any(Instant.class)))
                    .willReturn(0);

            int result = sut.execute(5);

            assertThat(result).isZero();
        }

        @Test
        @DisplayName("복구 대상이 있으면 합산 결과를 반환한다")
        void execute_HasStuck_ReturnsTotalRecovered() {
            given(transformQueueOutboxCommandManager.recoverStuckProcessing(any(Instant.class)))
                    .willReturn(3);
            given(transformCallbackOutboxCommandManager.recoverStuckProcessing(any(Instant.class)))
                    .willReturn(5);

            int result = sut.execute(5);

            assertThat(result).isEqualTo(8);
        }
    }
}
