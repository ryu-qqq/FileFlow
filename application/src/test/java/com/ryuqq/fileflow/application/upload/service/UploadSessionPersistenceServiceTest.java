package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.upload.port.out.UploadSessionCachePort;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.domain.policy.FileType;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException;
import com.ryuqq.fileflow.domain.upload.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.upload.vo.UploadRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UploadSessionPersistenceService 단위 테스트
 *
 * 트랜잭션 경계를 명확히 하기 위한 영속성 전용 서비스 검증
 */
@DisplayName("UploadSessionPersistenceService 테스트")
class UploadSessionPersistenceServiceTest {

    @Mock
    private UploadSessionPort uploadSessionPort;

    @Mock
    private UploadSessionCachePort uploadSessionCachePort;

    private UploadSessionPersistenceService persistenceService;

    private static final String TENANT_ID = "tenant-1";
    private static final String USER_TYPE = "user";
    private static final String SERVICE_TYPE = "service";
    private static final String UPLOADER_ID = "uploader-1";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        persistenceService = new UploadSessionPersistenceService(uploadSessionPort, uploadSessionCachePort);
    }

    @Nested
    @DisplayName("세션 저장 테스트")
    class SaveSessionTests {

        @Test
        @DisplayName("정상적인 세션 저장")
        void saveSession_Success() {
            // Given
            UploadSession session = createPendingSession();
            when(uploadSessionPort.save(any(UploadSession.class)))
                    .thenAnswer(i -> i.getArgument(0));

            // When
            UploadSession savedSession = persistenceService.saveSession(session);

            // Then
            assertThat(savedSession).isNotNull();
            assertThat(savedSession.getSessionId()).isEqualTo(session.getSessionId());

            verify(uploadSessionPort).save(session);
            verify(uploadSessionCachePort).saveWithTtl(savedSession);
        }

        @Test
        @DisplayName("Session이 null이면 예외")
        void saveSession_NullSession_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> persistenceService.saveSession(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("UploadSession must not be null");
        }

        @Test
        @DisplayName("saveSession 메서드에 @Transactional 어노테이션 존재 검증")
        void saveSession_HasTransactionalAnnotation() throws NoSuchMethodException {
            // Given
            Method saveSessionMethod = UploadSessionPersistenceService.class
                    .getDeclaredMethod("saveSession", UploadSession.class);

            // When
            boolean hasTransactional = saveSessionMethod.isAnnotationPresent(Transactional.class);

            // Then
            assertThat(hasTransactional)
                    .as("saveSession() 메서드는 @Transactional 어노테이션이 있어야 합니다")
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("세션 실패 처리 테스트")
    class FailSessionTests {

        @Test
        @DisplayName("정상적인 세션 실패 처리")
        void failSession_Success() {
            // Given
            UploadSession session = createPendingSession();
            String sessionId = session.getSessionId();
            String reason = "S3 upload failed";

            when(uploadSessionPort.findById(sessionId)).thenReturn(Optional.of(session));
            when(uploadSessionPort.save(any(UploadSession.class)))
                    .thenAnswer(i -> i.getArgument(0));

            // When
            UploadSession failedSession = persistenceService.failSession(sessionId, reason);

            // Then
            assertThat(failedSession).isNotNull();
            assertThat(failedSession.getStatus().name()).isEqualTo("FAILED");

            verify(uploadSessionPort).findById(sessionId);
            verify(uploadSessionPort).save(any(UploadSession.class));
        }

        @Test
        @DisplayName("세션이 없으면 예외")
        void failSession_NotFound_ThrowsException() {
            // Given
            String sessionId = "non-existent-session";
            String reason = "Test reason";

            when(uploadSessionPort.findById(sessionId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> persistenceService.failSession(sessionId, reason))
                    .isInstanceOf(UploadSessionNotFoundException.class);

            verify(uploadSessionPort).findById(sessionId);
        }

        @Test
        @DisplayName("SessionId가 null이면 예외")
        void failSession_NullSessionId_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> persistenceService.failSession(null, "reason"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("SessionId must not be null or empty");
        }

        @Test
        @DisplayName("Reason이 null이면 예외")
        void failSession_NullReason_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> persistenceService.failSession("session-123", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Reason must not be null or empty");
        }

        @Test
        @DisplayName("failSession 메서드에 @Transactional 어노테이션 존재 검증")
        void failSession_HasTransactionalAnnotation() throws NoSuchMethodException {
            // Given
            Method failSessionMethod = UploadSessionPersistenceService.class
                    .getDeclaredMethod("failSession", String.class, String.class);

            // When
            boolean hasTransactional = failSessionMethod.isAnnotationPresent(Transactional.class);

            // Then
            assertThat(hasTransactional)
                    .as("failSession() 메서드는 @Transactional 어노테이션이 있어야 합니다")
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTests {

        @Test
        @DisplayName("UploadSessionPort가 null이면 예외")
        void constructor_NullPort_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> new UploadSessionPersistenceService(null, uploadSessionCachePort))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("UploadSessionPort must not be null");
        }

        @Test
        @DisplayName("UploadSessionCachePort가 null이면 예외")
        void constructor_NullCachePort_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> new UploadSessionPersistenceService(uploadSessionPort, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("UploadSessionCachePort must not be null");
        }
    }

    // ========== Helper Methods ==========

    private UploadSession createPendingSession() {
        PolicyKey policyKey = PolicyKey.of(TENANT_ID, USER_TYPE, SERVICE_TYPE);
        UploadRequest uploadRequest = UploadRequest.of(
                "test.jpg",
                FileType.IMAGE,
                1024L,
                "image/jpeg",
                IdempotencyKey.generate()
        );
        return UploadSession.create(policyKey, uploadRequest, UPLOADER_ID, 60);
    }
}
