package com.ryuqq.fileflow.application.session.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.common.dto.command.StatusChangeContext;
import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.session.factory.command.SingleSessionCommandFactory;
import com.ryuqq.fileflow.application.session.manager.command.SessionCommandManager;
import com.ryuqq.fileflow.application.session.manager.query.SessionReadManager;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSessionFixture;
import com.ryuqq.fileflow.domain.session.id.SingleUploadSessionId;
import com.ryuqq.fileflow.domain.session.vo.UploadTargetFixture;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
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
@DisplayName("RecoverExpiredSingleSessionService 단위 테스트")
class RecoverExpiredSingleSessionServiceTest {

    @InjectMocks private RecoverExpiredSingleSessionService sut;
    @Mock private SessionReadManager sessionReadManager;
    @Mock private SingleSessionCommandFactory singleSessionCommandFactory;
    @Mock private SessionCommandManager sessionCommandManager;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("만료된 세션이 없으면 total 0을 반환한다")
        void execute_NoExpired_ReturnsZeroTotal() {
            given(sessionReadManager.findExpiredSingleSessions(any(Instant.class), anyInt()))
                    .willReturn(Collections.emptyList());

            SchedulerBatchProcessingResult result = sut.execute(100);

            assertThat(result.total()).isZero();
            assertThat(result.success()).isZero();
            assertThat(result.failed()).isZero();
        }

        @Test
        @DisplayName("만료된 세션을 성공적으로 expire 처리한다")
        void execute_ExpiredSession_ExpiresSuccessfully() {
            SingleUploadSession session = SingleUploadSessionFixture.aCreatedSession();
            given(sessionReadManager.findExpiredSingleSessions(any(Instant.class), anyInt()))
                    .willReturn(List.of(session));
            given(singleSessionCommandFactory.createExpireContext(session.idValue()))
                    .willReturn(new StatusChangeContext<>(session.idValue(), NOW));

            SchedulerBatchProcessingResult result = sut.execute(100);

            assertThat(result.total()).isEqualTo(1);
            assertThat(result.success()).isEqualTo(1);
            assertThat(result.failed()).isZero();
            then(sessionCommandManager).should().persist(session);
        }

        @Test
        @DisplayName("expire 처리 중 예외 발생 시 failedCount를 증가시킨다")
        void execute_ExpireThrows_IncrementsFailedCount() {
            SingleUploadSession session = SingleUploadSessionFixture.aCreatedSession();
            given(sessionReadManager.findExpiredSingleSessions(any(Instant.class), anyInt()))
                    .willReturn(List.of(session));
            given(singleSessionCommandFactory.createExpireContext(session.idValue()))
                    .willThrow(new RuntimeException("expire 실패"));

            SchedulerBatchProcessingResult result = sut.execute(100);

            assertThat(result.total()).isEqualTo(1);
            assertThat(result.success()).isZero();
            assertThat(result.failed()).isEqualTo(1);
            then(sessionCommandManager).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("복수 세션 중 일부 성공, 일부 실패 시 각각 카운트한다")
        void execute_MixedResults_CountsCorrectly() {
            SingleUploadSession session1 = SingleUploadSessionFixture.aCreatedSession();
            SingleUploadSession session2 =
                    SingleUploadSession.forNew(
                            SingleUploadSessionId.of("single-session-002"),
                            UploadTargetFixture.anUploadTarget(),
                            "https://s3.presigned-url.com/test-2",
                            "product-image",
                            "commerce-service",
                            SingleUploadSessionFixture.defaultExpiresAt(),
                            SingleUploadSessionFixture.defaultNow());
            given(sessionReadManager.findExpiredSingleSessions(any(Instant.class), anyInt()))
                    .willReturn(List.of(session1, session2));
            given(singleSessionCommandFactory.createExpireContext(session1.idValue()))
                    .willReturn(new StatusChangeContext<>(session1.idValue(), NOW));
            given(singleSessionCommandFactory.createExpireContext(session2.idValue()))
                    .willThrow(new RuntimeException("expire 실패"));

            SchedulerBatchProcessingResult result = sut.execute(100);

            assertThat(result.total()).isEqualTo(2);
            assertThat(result.success()).isEqualTo(1);
            assertThat(result.failed()).isEqualTo(1);
        }
    }
}
