package com.ryuqq.fileflow.application.upload.batch;

import com.ryuqq.fileflow.application.upload.port.in.FailUploadUseCase;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.upload.vo.UploadRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ExpiredUploadSessionBatchService 단위 테스트
 *
 * Port 의존성을 Mock 처리하여 배치 로직 검증
 */
@DisplayName("ExpiredUploadSessionBatchService 테스트")
class ExpiredUploadSessionBatchServiceTest {

    @Mock
    private UploadSessionPort uploadSessionPort;

    @Mock
    private FailUploadUseCase failUploadUseCase;

    private ExpiredUploadSessionBatchService batchService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        batchService = new ExpiredUploadSessionBatchService(
                uploadSessionPort,
                failUploadUseCase
        );
    }

    @Test
    @DisplayName("만료된 세션이 없는 경우")
    void processExpiredSessions_NoExpiredSessions() {
        // Given
        when(uploadSessionPort.findExpiredSessions()).thenReturn(List.of());

        // When
        batchService.processExpiredSessions();

        // Then
        verify(uploadSessionPort).findExpiredSessions();
        verify(failUploadUseCase, never()).failSession(anyString(), anyString());
    }

    @Test
    @DisplayName("만료된 세션들을 정상적으로 처리 (Fallback)")
    void processExpiredSessions_Success() {
        // Given
        UploadSession session1 = createExpiredSession();
        UploadSession session2 = createExpiredSession();
        UploadSession session3 = createExpiredSession();
        List<UploadSession> expiredSessions = List.of(session1, session2, session3);

        when(uploadSessionPort.findExpiredSessions()).thenReturn(expiredSessions);
        when(failUploadUseCase.failSession(anyString(), anyString())).thenReturn(null);

        // When
        batchService.processExpiredSessions();

        // Then
        verify(uploadSessionPort).findExpiredSessions();
        verify(failUploadUseCase).failSession(eq(session1.getSessionId()), eq("Session expired (Fallback)"));
        verify(failUploadUseCase).failSession(eq(session2.getSessionId()), eq("Session expired (Fallback)"));
        verify(failUploadUseCase).failSession(eq(session3.getSessionId()), eq("Session expired (Fallback)"));
        verify(failUploadUseCase, times(3)).failSession(anyString(), eq("Session expired (Fallback)"));
    }

    @Test
    @DisplayName("일부 세션 처리 실패 시 다른 세션 계속 처리")
    void processExpiredSessions_PartialFailure_ContinuesProcessing() {
        // Given
        UploadSession session1 = createExpiredSession();
        UploadSession session2 = createExpiredSession();
        UploadSession session3 = createExpiredSession();
        List<UploadSession> expiredSessions = List.of(session1, session2, session3);

        when(uploadSessionPort.findExpiredSessions()).thenReturn(expiredSessions);
        when(failUploadUseCase.failSession(eq(session1.getSessionId()), anyString())).thenReturn(null);
        when(failUploadUseCase.failSession(eq(session2.getSessionId()), anyString()))
                .thenThrow(new RuntimeException("Test failure"));
        when(failUploadUseCase.failSession(eq(session3.getSessionId()), anyString())).thenReturn(null);

        // When
        batchService.processExpiredSessions();

        // Then
        verify(uploadSessionPort).findExpiredSessions();
        verify(failUploadUseCase).failSession(eq(session1.getSessionId()), anyString());
        verify(failUploadUseCase).failSession(eq(session2.getSessionId()), anyString());
        verify(failUploadUseCase).failSession(eq(session3.getSessionId()), anyString());
        verify(failUploadUseCase, times(3)).failSession(anyString(), anyString());
    }

    @Test
    @DisplayName("전체 배치 조회 실패 시 예외를 로그만 하고 종료")
    void processExpiredSessions_FindExpiredSessions_HandlesException() {
        // Given
        when(uploadSessionPort.findExpiredSessions())
                .thenThrow(new RuntimeException("Database error"));

        // When
        batchService.processExpiredSessions();

        // Then
        verify(uploadSessionPort).findExpiredSessions();
        verify(failUploadUseCase, never()).failSession(anyString(), anyString());
    }

    @Test
    @DisplayName("대량의 만료된 세션 처리 (Fallback)")
    void processExpiredSessions_LargeVolume() {
        // Given
        List<UploadSession> expiredSessions = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            expiredSessions.add(createExpiredSession());
        }

        when(uploadSessionPort.findExpiredSessions()).thenReturn(expiredSessions);
        when(failUploadUseCase.failSession(anyString(), anyString())).thenReturn(null);

        // When
        batchService.processExpiredSessions();

        // Then
        verify(uploadSessionPort).findExpiredSessions();
        verify(failUploadUseCase, times(100)).failSession(anyString(), eq("Session expired (Fallback)"));
    }

    // ========== Helper Methods ==========

    private UploadSession createExpiredSession() {
        PolicyKey policyKey = PolicyKey.of("tenant-1", "user", "service");
        UploadRequest uploadRequest = UploadRequest.of(
                "test.jpg",
                com.ryuqq.fileflow.domain.policy.FileType.IMAGE,
                1024L,
                "image/jpeg",
                IdempotencyKey.generate()
        );
        return UploadSession.create(policyKey, uploadRequest, "uploader-1", 60);
    }
}
