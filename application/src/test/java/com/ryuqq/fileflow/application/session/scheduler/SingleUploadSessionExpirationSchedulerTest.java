package com.ryuqq.fileflow.application.session.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ryuqq.fileflow.application.session.dto.command.ExpireUploadSessionCommand;
import com.ryuqq.fileflow.application.session.port.in.command.ExpireUploadSessionUseCase;
import com.ryuqq.fileflow.application.session.port.out.query.FindUploadSessionQueryPort;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.fixture.SingleUploadSessionFixture;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("SingleUploadSessionExpirationScheduler 단위 테스트")
@ExtendWith(MockitoExtension.class)
class SingleUploadSessionExpirationSchedulerTest {

    @Mock private FindUploadSessionQueryPort findUploadSessionQueryPort;
    @Mock private ExpireUploadSessionUseCase expireUploadSessionUseCase;

    @InjectMocks private SingleUploadSessionExpirationScheduler scheduler;

    @Nested
    @DisplayName("expireStalesSingleUploadSessions")
    class ExpireStalesSingleUploadSessions {

        @Test
        @DisplayName("만료된 세션이 없으면 아무 처리도 하지 않는다")
        void expireStalesSingleUploadSessions_WhenNoExpiredSessions_ShouldDoNothing() {
            // given
            when(findUploadSessionQueryPort.findExpiredSingleUploads(any(Instant.class), eq(100)))
                    .thenReturn(Collections.emptyList());

            // when
            scheduler.expireStalesSingleUploadSessions();

            // then
            verify(findUploadSessionQueryPort)
                    .findExpiredSingleUploads(any(Instant.class), eq(100));
            verify(expireUploadSessionUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("만료된 세션이 있으면 각 세션에 대해 만료 처리를 수행한다")
        void expireStalesSingleUploadSessions_WhenExpiredSessionsExist_ShouldExpireEachSession() {
            // given
            SingleUploadSession session1 = SingleUploadSessionFixture.activeSingleUploadSession();
            SingleUploadSession session2 = SingleUploadSessionFixture.activeSingleUploadSession();
            List<SingleUploadSession> expiredSessions = List.of(session1, session2);

            when(findUploadSessionQueryPort.findExpiredSingleUploads(any(Instant.class), eq(100)))
                    .thenReturn(expiredSessions)
                    .thenReturn(Collections.emptyList());

            // when
            scheduler.expireStalesSingleUploadSessions();

            // then
            verify(expireUploadSessionUseCase, times(2))
                    .execute(any(ExpireUploadSessionCommand.class));
        }

        @Test
        @DisplayName("배치 크기만큼 세션이 있으면 다음 배치를 조회한다")
        void expireStalesSingleUploadSessions_WhenBatchIsFull_ShouldFetchNextBatch() {
            // given
            List<SingleUploadSession> firstBatch = createSessionBatch(100);
            List<SingleUploadSession> secondBatch = createSessionBatch(50);

            when(findUploadSessionQueryPort.findExpiredSingleUploads(any(Instant.class), eq(100)))
                    .thenReturn(firstBatch)
                    .thenReturn(secondBatch)
                    .thenReturn(Collections.emptyList());

            // when
            scheduler.expireStalesSingleUploadSessions();

            // then
            verify(findUploadSessionQueryPort, times(2))
                    .findExpiredSingleUploads(any(Instant.class), eq(100));
            verify(expireUploadSessionUseCase, times(150))
                    .execute(any(ExpireUploadSessionCommand.class));
        }

        @Test
        @DisplayName("개별 세션 만료 실패 시 다른 세션 처리를 계속한다")
        void expireStalesSingleUploadSessions_WhenOneSessionFails_ShouldContinueWithOthers() {
            // given
            SingleUploadSession session1 = SingleUploadSessionFixture.activeSingleUploadSession();
            SingleUploadSession session2 = SingleUploadSessionFixture.activeSingleUploadSession();
            SingleUploadSession session3 = SingleUploadSessionFixture.activeSingleUploadSession();
            List<SingleUploadSession> expiredSessions = List.of(session1, session2, session3);

            when(findUploadSessionQueryPort.findExpiredSingleUploads(any(Instant.class), eq(100)))
                    .thenReturn(expiredSessions)
                    .thenReturn(Collections.emptyList());

            doAnswer(
                            invocation -> {
                                ExpireUploadSessionCommand cmd = invocation.getArgument(0);
                                if (cmd.sessionId().equals(session2.getIdValue())) {
                                    throw new RuntimeException("만료 처리 실패");
                                }
                                return null;
                            })
                    .when(expireUploadSessionUseCase)
                    .execute(any(ExpireUploadSessionCommand.class));

            // when
            scheduler.expireStalesSingleUploadSessions();

            // then
            verify(expireUploadSessionUseCase, times(3))
                    .execute(any(ExpireUploadSessionCommand.class));
        }

        @Test
        @DisplayName("배치 크기보다 적은 세션이면 추가 조회 없이 종료한다")
        void expireStalesSingleUploadSessions_WhenLessThanBatchSize_ShouldNotFetchAgain() {
            // given
            List<SingleUploadSession> sessions = createSessionBatch(50);

            when(findUploadSessionQueryPort.findExpiredSingleUploads(any(Instant.class), eq(100)))
                    .thenReturn(sessions);

            // when
            scheduler.expireStalesSingleUploadSessions();

            // then
            verify(findUploadSessionQueryPort, times(1))
                    .findExpiredSingleUploads(any(Instant.class), eq(100));
            verify(expireUploadSessionUseCase, times(50))
                    .execute(any(ExpireUploadSessionCommand.class));
        }

        @Test
        @DisplayName("올바른 ExpireUploadSessionCommand로 UseCase를 호출한다")
        void expireStalesSingleUploadSessions_ShouldCallUseCaseWithCorrectCommand() {
            // given
            SingleUploadSession session = SingleUploadSessionFixture.activeSingleUploadSession();
            List<SingleUploadSession> expiredSessions = List.of(session);

            when(findUploadSessionQueryPort.findExpiredSingleUploads(any(Instant.class), eq(100)))
                    .thenReturn(expiredSessions)
                    .thenReturn(Collections.emptyList());

            // when
            scheduler.expireStalesSingleUploadSessions();

            // then
            verify(expireUploadSessionUseCase)
                    .execute(argThat(command -> command.sessionId().equals(session.getIdValue())));
        }

        private List<SingleUploadSession> createSessionBatch(int size) {
            return java.util.stream.IntStream.range(0, size)
                    .mapToObj(i -> SingleUploadSessionFixture.activeSingleUploadSession())
                    .toList();
        }
    }
}
