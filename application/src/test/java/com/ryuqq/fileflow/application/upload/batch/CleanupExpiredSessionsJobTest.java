package com.ryuqq.fileflow.application.upload.batch;

import com.ryuqq.fileflow.application.upload.manager.UploadSessionStateManager;
import com.ryuqq.fileflow.application.upload.port.out.query.LoadUploadSessionPort;
import com.ryuqq.fileflow.domain.upload.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.withSettings;

/**
 * CleanupExpiredSessionsJob 단위 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>cleanupExpiredSessions() - 전체 정리 작업</li>
 *   <li>cleanupPendingSessions() - PENDING 세션 정리</li>
 *   <li>cleanupInProgressSessions() - IN_PROGRESS 세션 정리</li>
 *   <li>failExpiredSession() - 개별 세션 FAILED 전환</li>
 *   <li>cleanupMultipleStatuses() - 복수 상태 동시 정리</li>
 * </ul>
 *
 * <p><strong>검증 항목:</strong></p>
 * <ul>
 *   <li>Batch 로직 정확성</li>
 *   <li>만료 기준 시간 계산</li>
 *   <li>Port 위임 정확성</li>
 *   <li>예외 처리</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CleanupExpiredSessionsJob 단위 테스트")
class CleanupExpiredSessionsJobTest {

    @Mock
    private LoadUploadSessionPort loadUploadSessionPort;

    @Mock
    private UploadSessionStateManager uploadSessionStateManager;

    private CleanupExpiredSessionsJob job;

    private static final int PENDING_EXPIRATION_MINUTES = 30;
    private static final int IN_PROGRESS_EXPIRATION_HOURS = 24;

    @BeforeEach
    void setUp() {
        // 수동으로 인스턴스 생성 (생성자가 @Value 어노테이션을 사용하므로 @InjectMocks 사용 불가)
        job = new CleanupExpiredSessionsJob(
            loadUploadSessionPort,
            uploadSessionStateManager,
            PENDING_EXPIRATION_MINUTES,
            IN_PROGRESS_EXPIRATION_HOURS
        );
    }

    @Nested
    @DisplayName("cleanupExpiredSessions 메서드 테스트")
    class CleanupExpiredSessionsTests {

        @Test
        @DisplayName("cleanupExpiredSessions_WithExpiredSessions_ShouldCleanupSuccessfully - 만료된 세션 정리 성공")
        void cleanupExpiredSessions_WithExpiredSessions_ShouldCleanupSuccessfully() {
            // Given - PENDING 2개, IN_PROGRESS 1개 만료
            UploadSession pendingSession1 = createMockSession(1L, SessionStatus.PENDING);
            UploadSession pendingSession2 = createMockSession(2L, SessionStatus.PENDING);
            UploadSession inProgressSession = createMockSession(3L, SessionStatus.IN_PROGRESS);

            given(loadUploadSessionPort.findByStatusAndCreatedBefore(
                eq(SessionStatus.PENDING),
                any(LocalDateTime.class)
            )).willReturn(Arrays.asList(pendingSession1, pendingSession2));

            given(loadUploadSessionPort.findByStatusAndCreatedBefore(
                eq(SessionStatus.IN_PROGRESS),
                any(LocalDateTime.class)
            )).willReturn(Collections.singletonList(inProgressSession));

            // When
            job.cleanupExpiredSessions();

            // Then
            verify(loadUploadSessionPort).findByStatusAndCreatedBefore(
                eq(SessionStatus.PENDING),
                any(LocalDateTime.class)
            );
            verify(loadUploadSessionPort).findByStatusAndCreatedBefore(
                eq(SessionStatus.IN_PROGRESS),
                any(LocalDateTime.class)
            );
            verify(uploadSessionStateManager, times(3)).save(any(UploadSession.class));
        }

        @Test
        @DisplayName("cleanupExpiredSessions_WithNoExpiredSessions_ShouldNotCleanup - 만료된 세션 없을 시 정리 안함")
        void cleanupExpiredSessions_WithNoExpiredSessions_ShouldNotCleanup() {
            // Given - 만료된 세션 없음
            given(loadUploadSessionPort.findByStatusAndCreatedBefore(
                eq(SessionStatus.PENDING),
                any(LocalDateTime.class)
            )).willReturn(Collections.emptyList());

            given(loadUploadSessionPort.findByStatusAndCreatedBefore(
                eq(SessionStatus.IN_PROGRESS),
                any(LocalDateTime.class)
            )).willReturn(Collections.emptyList());

            // When
            job.cleanupExpiredSessions();

            // Then - save 호출 안됨
            verify(uploadSessionStateManager, never()).save(any(UploadSession.class));
        }

        @Test
        @DisplayName("cleanupExpiredSessions_WithException_ShouldHandleGracefully - 예외 발생 시 정상 처리")
        void cleanupExpiredSessions_WithException_ShouldHandleGracefully() {
            // Given - 조회 시 예외 발생
            given(loadUploadSessionPort.findByStatusAndCreatedBefore(
                eq(SessionStatus.PENDING),
                any(LocalDateTime.class)
            )).willThrow(new RuntimeException("Database error"));

            // When - 예외가 메서드 밖으로 전파되지 않음
            job.cleanupExpiredSessions();

            // Then - 정상 처리 (로그 기록)
            verify(loadUploadSessionPort).findByStatusAndCreatedBefore(
                eq(SessionStatus.PENDING),
                any(LocalDateTime.class)
            );
        }
    }

    @Nested
    @DisplayName("cleanupPendingSessions 메서드 테스트")
    class CleanupPendingSessionsTests {

        @Test
        @DisplayName("cleanupPendingSessions_WithExpiredSessions_ShouldReturnCount - 만료된 PENDING 세션 정리")
        void cleanupPendingSessions_WithExpiredSessions_ShouldReturnCount() {
            // Given - 30분 전 threshold
            LocalDateTime threshold = LocalDateTime.now().minusMinutes(PENDING_EXPIRATION_MINUTES);
            UploadSession session1 = createMockSession(1L, SessionStatus.PENDING);
            UploadSession session2 = createMockSession(2L, SessionStatus.PENDING);

            given(loadUploadSessionPort.findByStatusAndCreatedBefore(eq(SessionStatus.PENDING), eq(threshold)))
                .willReturn(Arrays.asList(session1, session2));

            // When
            int count = job.cleanupPendingSessions(threshold);

            // Then
            assertThat(count).isEqualTo(2);
            verify(uploadSessionStateManager, times(2)).save(any(UploadSession.class));
        }

        @Test
        @DisplayName("cleanupPendingSessions_WithPartialFailure_ShouldContinueProcessing - 일부 실패 시에도 계속 처리")
        void cleanupPendingSessions_WithPartialFailure_ShouldContinueProcessing() {
            // Given - 3개 세션 중 2번째 실패
            LocalDateTime threshold = LocalDateTime.now().minusMinutes(PENDING_EXPIRATION_MINUTES);
            UploadSession session1 = createMockSession(1L, SessionStatus.PENDING);
            UploadSession session2 = createMockSession(2L, SessionStatus.PENDING);
            UploadSession session3 = createMockSession(3L, SessionStatus.PENDING);

            given(loadUploadSessionPort.findByStatusAndCreatedBefore(eq(SessionStatus.PENDING), eq(threshold)))
                .willReturn(Arrays.asList(session1, session2, session3));

            // willAnswer를 사용하여 세션별 다른 동작 정의
            willAnswer(invocation -> {
                UploadSession session = invocation.getArgument(0);
                if (session.getIdValue().equals(2L)) {
                    throw new RuntimeException("Save failed");
                }
                return null; // void method
            }).given(uploadSessionStateManager).save(any(UploadSession.class));

            // When - 예외 발생해도 계속 진행
            int count = job.cleanupPendingSessions(threshold);

            // Then - 실패한 것 제외하고 2개 처리
            assertThat(count).isEqualTo(2);
            verify(uploadSessionStateManager, times(3)).save(any(UploadSession.class));
        }
    }

    @Nested
    @DisplayName("cleanupInProgressSessions 메서드 테스트")
    class CleanupInProgressSessionsTests {

        @Test
        @DisplayName("cleanupInProgressSessions_WithExpiredSessions_ShouldReturnCount - 만료된 IN_PROGRESS 세션 정리")
        void cleanupInProgressSessions_WithExpiredSessions_ShouldReturnCount() {
            // Given - 24시간 전 threshold
            LocalDateTime threshold = LocalDateTime.now().minusHours(IN_PROGRESS_EXPIRATION_HOURS);
            UploadSession session = createMockSession(1L, SessionStatus.IN_PROGRESS);

            given(loadUploadSessionPort.findByStatusAndCreatedBefore(eq(SessionStatus.IN_PROGRESS), eq(threshold)))
                .willReturn(Collections.singletonList(session));

            // When
            int count = job.cleanupInProgressSessions(threshold);

            // Then
            assertThat(count).isEqualTo(1);
            verify(uploadSessionStateManager).save(eq(session));
        }
    }

    @Nested
    @DisplayName("failExpiredSession 메서드 테스트")
    class FailExpiredSessionTests {

        @Test
        @DisplayName("failExpiredSession_WithValidSession_ShouldMarkAsFailed - 정상 세션 FAILED로 전환")
        void failExpiredSession_WithValidSession_ShouldMarkAsFailed() {
            // Given
            UploadSession session = createMockSession(1L, SessionStatus.PENDING);
            String reason = "Session expired (PENDING > 30 minutes)";

            // When
            job.failExpiredSession(session, reason);

            // Then
            verify(session).fail(any(com.ryuqq.fileflow.domain.upload.FailureReason.class));
            verify(uploadSessionStateManager).save(eq(session));
        }
    }

    @Nested
    @DisplayName("cleanupMultipleStatuses 메서드 테스트")
    class CleanupMultipleStatusesTests {

        @Test
        @DisplayName("cleanupMultipleStatuses_WithMultipleStatuses_ShouldCleanupAll - 여러 상태 동시 정리")
        void cleanupMultipleStatuses_WithMultipleStatuses_ShouldCleanupAll() {
            // Given - PENDING 1개, IN_PROGRESS 2개
            LocalDateTime threshold = LocalDateTime.now().minusHours(1);
            UploadSession pendingSession = createMockSession(1L, SessionStatus.PENDING);
            UploadSession inProgressSession1 = createMockSession(2L, SessionStatus.IN_PROGRESS);
            UploadSession inProgressSession2 = createMockSession(3L, SessionStatus.IN_PROGRESS);

            List<SessionStatus> statuses = Arrays.asList(SessionStatus.PENDING, SessionStatus.IN_PROGRESS);
            given(loadUploadSessionPort.findByStatusInAndCreatedBefore(eq(statuses), eq(threshold)))
                .willReturn(Arrays.asList(pendingSession, inProgressSession1, inProgressSession2));

            // When
            int count = job.cleanupMultipleStatuses(threshold);

            // Then
            assertThat(count).isEqualTo(3);
            verify(uploadSessionStateManager, times(3)).save(any(UploadSession.class));
        }
    }

    // ===== Helper Methods =====

    /**
     * Mock UploadSession 생성
     *
     * @param id 세션 ID
     * @param status 세션 상태
     * @return Mock UploadSession
     */
    private UploadSession createMockSession(Long id, SessionStatus status) {
        UploadSession session = mock(UploadSession.class, withSettings().lenient());
        when(session.getIdValue()).thenReturn(id);
        when(session.getStatus()).thenReturn(status);
        doNothing().when(session).fail(any(com.ryuqq.fileflow.domain.upload.FailureReason.class));
        return session;
    }
}
