package com.ryuqq.fileflow.application.upload.manager;

import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.domain.upload.FailureReason;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.fixture.UploadSessionFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UploadSessionManager 단위 테스트
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UploadSessionManager 단위 테스트")
class UploadSessionManagerTest {

    @Mock
    private UploadSessionPort uploadSessionPort;

    @InjectMocks
    private UploadSessionManager manager;

    @Nested
    @DisplayName("save 메서드 테스트")
    class SaveTests {

        @Test
        @DisplayName("save_Success - 세션 저장 성공")
        void save_Success() {
            // Given
            UploadSession session = UploadSessionFixture.createSingle();
            UploadSession savedSession = UploadSessionFixture.reconstituteDefault(1L);

            when(uploadSessionPort.save(session)).thenReturn(savedSession);

            // When
            UploadSession result = manager.save(session);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getIdValue()).isEqualTo(1L);
            verify(uploadSessionPort).save(session);
        }
    }

    @Nested
    @DisplayName("findById 메서드 테스트")
    class FindByIdTests {

        @Test
        @DisplayName("findById_Success - 조회 성공")
        void findById_Success() {
            // Given
            Long id = 1L;
            UploadSession session = UploadSessionFixture.reconstituteDefault(id);

            when(uploadSessionPort.findById(id)).thenReturn(Optional.of(session));

            // When
            Optional<UploadSession> result = manager.findById(id);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getIdValue()).isEqualTo(id);
            verify(uploadSessionPort).findById(id);
        }

        @Test
        @DisplayName("findById_ReturnsEmpty - 조회 실패")
        void findById_ReturnsEmpty() {
            // Given
            Long id = 999L;
            when(uploadSessionPort.findById(id)).thenReturn(Optional.empty());

            // When
            Optional<UploadSession> result = manager.findById(id);

            // Then
            assertThat(result).isEmpty();
            verify(uploadSessionPort).findById(id);
        }
    }

    @Nested
    @DisplayName("complete 메서드 테스트")
    class CompleteTests {

        @Test
        @DisplayName("complete_Success - 세션 완료 성공")
        void complete_Success() {
            // Given
            Long id = 1L;
            Long fileId = 100L;
            UploadSession session = UploadSessionFixture.createSingleInProgress();
            session = UploadSessionFixture.reconstitute(
                id,
                session.getSessionKey(),
                session.getTenantId(),
                session.getFileName(),
                session.getFileSize(),
                session.getUploadType(),
                session.getStorageKey(),
                session.getStatus(),
                null,
                null,
                session.getCreatedAt(),
                session.getUpdatedAt(),
                null,
                null
            );

            UploadSession completedSession = UploadSessionFixture.createSingleCompleted(fileId);

            when(uploadSessionPort.findById(id)).thenReturn(Optional.of(session));
            when(uploadSessionPort.save(any(UploadSession.class))).thenReturn(completedSession);

            // When
            UploadSession result = manager.complete(id, fileId);

            // Then
            assertThat(result).isNotNull();
            verify(uploadSessionPort).findById(id);
            verify(uploadSessionPort).save(any(UploadSession.class));
        }

        @Test
        @DisplayName("complete_ThrowsException_WhenNotFound - 세션 조회 실패")
        void complete_ThrowsException_WhenNotFound() {
            // Given
            Long id = 999L;
            Long fileId = 100L;
            when(uploadSessionPort.findById(id)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> manager.complete(id, fileId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Upload Session not found");

            verify(uploadSessionPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("fail 메서드 테스트")
    class FailTests {

        @Test
        @DisplayName("fail_Success - 세션 실패 처리 성공")
        void fail_Success() {
            // Given
            Long id = 1L;
            FailureReason reason = FailureReason.of("S3 upload failed");
            UploadSession session = UploadSessionFixture.createSingle();
            UploadSession failedSession = UploadSessionFixture.createFailed(reason);

            when(uploadSessionPort.findById(id)).thenReturn(Optional.of(session));
            when(uploadSessionPort.save(any(UploadSession.class))).thenReturn(failedSession);

            // When
            UploadSession result = manager.fail(id, reason);

            // Then
            assertThat(result).isNotNull();
            verify(uploadSessionPort).findById(id);
            verify(uploadSessionPort).save(any(UploadSession.class));
        }
    }
}

