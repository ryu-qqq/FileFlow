package com.ryuqq.fileflow.application.session.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;

import com.ryuqq.fileflow.application.common.dto.command.StatusChangeContext;
import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.session.factory.command.MultipartSessionCommandFactory;
import com.ryuqq.fileflow.application.session.manager.client.MultipartUploadManager;
import com.ryuqq.fileflow.application.session.manager.command.SessionCommandManager;
import com.ryuqq.fileflow.application.session.manager.query.SessionReadManager;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSessionFixture;
import com.ryuqq.fileflow.domain.session.id.MultipartUploadSessionId;
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
@DisplayName("RecoverExpiredMultipartSessionService 단위 테스트")
class RecoverExpiredMultipartSessionServiceTest {

    @InjectMocks private RecoverExpiredMultipartSessionService sut;
    @Mock private SessionReadManager sessionReadManager;
    @Mock private MultipartSessionCommandFactory multipartSessionCommandFactory;
    @Mock private SessionCommandManager sessionCommandManager;
    @Mock private MultipartUploadManager multipartUploadManager;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("만료된 세션이 없으면 total 0을 반환한다")
        void execute_NoExpired_ReturnsZeroTotal() {
            given(sessionReadManager.findExpiredMultipartSessions(any(Instant.class), anyInt()))
                    .willReturn(Collections.emptyList());

            SchedulerBatchProcessingResult result = sut.execute(100);

            assertThat(result.total()).isZero();
            assertThat(result.success()).isZero();
            assertThat(result.failed()).isZero();
        }

        @Test
        @DisplayName("만료된 세션을 S3 abort 후 expire 처리한다")
        void execute_ExpiredSession_AbortsAndExpires() {
            MultipartUploadSession session = MultipartUploadSessionFixture.anInitiatedSession();
            given(sessionReadManager.findExpiredMultipartSessions(any(Instant.class), anyInt()))
                    .willReturn(List.of(session));
            given(multipartSessionCommandFactory.createExpireContext(session.idValue()))
                    .willReturn(new StatusChangeContext<>(session.idValue(), NOW));

            SchedulerBatchProcessingResult result = sut.execute(100);

            assertThat(result.total()).isEqualTo(1);
            assertThat(result.success()).isEqualTo(1);
            assertThat(result.failed()).isZero();
            then(multipartUploadManager)
                    .should()
                    .abortMultipartUpload(session.s3Key(), session.uploadId());
            then(sessionCommandManager).should().persist(session);
        }

        @Test
        @DisplayName("S3 abort 중 예외 발생 시 failedCount를 증가시킨다")
        void execute_AbortThrows_IncrementsFailedCount() {
            MultipartUploadSession session = MultipartUploadSessionFixture.anInitiatedSession();
            given(sessionReadManager.findExpiredMultipartSessions(any(Instant.class), anyInt()))
                    .willReturn(List.of(session));
            willThrow(new RuntimeException("S3 abort 실패"))
                    .given(multipartUploadManager)
                    .abortMultipartUpload(session.s3Key(), session.uploadId());

            SchedulerBatchProcessingResult result = sut.execute(100);

            assertThat(result.total()).isEqualTo(1);
            assertThat(result.success()).isZero();
            assertThat(result.failed()).isEqualTo(1);
            then(sessionCommandManager).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("expire 처리 중 예외 발생 시 failedCount를 증가시킨다")
        void execute_ExpireThrows_IncrementsFailedCount() {
            MultipartUploadSession session = MultipartUploadSessionFixture.anInitiatedSession();
            given(sessionReadManager.findExpiredMultipartSessions(any(Instant.class), anyInt()))
                    .willReturn(List.of(session));
            given(multipartSessionCommandFactory.createExpireContext(session.idValue()))
                    .willThrow(new RuntimeException("expire context 실패"));

            SchedulerBatchProcessingResult result = sut.execute(100);

            assertThat(result.total()).isEqualTo(1);
            assertThat(result.success()).isZero();
            assertThat(result.failed()).isEqualTo(1);
        }

        @Test
        @DisplayName("복수 세션 중 일부 성공, 일부 실패 시 각각 카운트한다")
        void execute_MixedResults_CountsCorrectly() {
            MultipartUploadSession session1 = MultipartUploadSessionFixture.anInitiatedSession();
            MultipartUploadSession session2 =
                    MultipartUploadSession.forNew(
                            MultipartUploadSessionId.of("multipart-session-002"),
                            UploadTargetFixture.anInternalUploadTarget(),
                            "upload-id-002",
                            5_242_880L,
                            "product-image",
                            "commerce-service",
                            MultipartUploadSessionFixture.defaultExpiresAt(),
                            MultipartUploadSessionFixture.defaultNow());
            given(sessionReadManager.findExpiredMultipartSessions(any(Instant.class), anyInt()))
                    .willReturn(List.of(session1, session2));
            willDoNothing()
                    .given(multipartUploadManager)
                    .abortMultipartUpload(session1.s3Key(), session1.uploadId());
            given(multipartSessionCommandFactory.createExpireContext(session1.idValue()))
                    .willReturn(new StatusChangeContext<>(session1.idValue(), NOW));
            willThrow(new RuntimeException("S3 abort 실패"))
                    .given(multipartUploadManager)
                    .abortMultipartUpload(session2.s3Key(), session2.uploadId());

            SchedulerBatchProcessingResult result = sut.execute(100);

            assertThat(result.total()).isEqualTo(2);
            assertThat(result.success()).isEqualTo(1);
            assertThat(result.failed()).isEqualTo(1);
        }
    }
}
