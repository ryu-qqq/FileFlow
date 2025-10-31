package com.ryuqq.fileflow.domain.upload;

import com.ryuqq.fileflow.domain.upload.fixture.MultipartUploadFixture;
import com.ryuqq.fileflow.domain.upload.fixture.UploadPartFixture;
import com.ryuqq.fileflow.domain.upload.fixture.UploadSessionFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * UploadSession Domain 단위 테스트
 *
 * @author Sangwon Ryu
 * @since 2025-10-31
 */
@DisplayName("UploadSession Domain 단위 테스트")
class UploadSessionTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTests {

        @Test
        @DisplayName("단일 업로드 세션 생성 성공")
        void createSingle_Success() {
            // When
            UploadSession session = UploadSessionFixture.createSingle();

            // Then
            assertThat(session.getUploadType()).isEqualTo(UploadSession.UploadType.SINGLE);
            assertThat(session.getStatus()).isEqualTo(UploadSession.SessionStatus.PENDING);
            assertThat(session.isMultipart()).isFalse();
        }

        @Test
        @DisplayName("Multipart 업로드 세션 생성 성공")
        void createMultipart_Success() {
            // When
            UploadSession session = UploadSessionFixture.createMultipart();

            // Then
            assertThat(session.getUploadType()).isEqualTo(UploadSession.UploadType.MULTIPART);
            assertThat(session.getStatus()).isEqualTo(UploadSession.SessionStatus.PENDING);
            assertThat(session.isMultipart()).isTrue();
        }
    }

    @Nested
    @DisplayName("상태 전환 테스트")
    class StateTransitionTests {

        @Test
        @DisplayName("PENDING → IN_PROGRESS: start() 성공")
        void start_Success() {
            // Given
            UploadSession session = UploadSessionFixture.createSingle();

            // When
            session.start();

            // Then
            assertThat(session.getStatus()).isEqualTo(UploadSession.SessionStatus.IN_PROGRESS);
            assertThat(session.isInProgress()).isTrue();
        }

        @Test
        @DisplayName("IN_PROGRESS → COMPLETED: complete() 성공")
        void complete_Success() {
            // Given
            UploadSession session = UploadSessionFixture.createSingle();
            session.start();
            Long fileId = 100L;

            // When
            session.complete(fileId);

            // Then
            assertThat(session.getStatus()).isEqualTo(UploadSession.SessionStatus.COMPLETED);
            assertThat(session.isCompleted()).isTrue();
            assertThat(session.getFileId()).isEqualTo(fileId);
        }

        @Test
        @DisplayName("* → FAILED: fail() 성공")
        void fail_Success() {
            // Given
            UploadSession session = UploadSessionFixture.createSingle();
            session.start();
            String reason = "Network error";

            // When
            session.fail(reason);

            // Then
            assertThat(session.getStatus()).isEqualTo(UploadSession.SessionStatus.FAILED);
            assertThat(session.isFailed()).isTrue();
            assertThat(session.getFailureReason()).isEqualTo(reason);
        }
    }

    @Nested
    @DisplayName("예외 처리 테스트")
    class ExceptionTests {

        @Test
        @DisplayName("start() - PENDING 상태가 아니면 예외 발생")
        void start_ThrowsException_WhenNotPending() {
            // Given
            UploadSession session = UploadSessionFixture.createSingleInProgress();

            // When & Then
            assertThatThrownBy(() -> session.start())
                .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("complete() - IN_PROGRESS 상태가 아니면 예외 발생")
        void complete_ThrowsException_WhenNotInProgress() {
            // Given
            UploadSession session = UploadSessionFixture.createSingle();

            // When & Then
            assertThatThrownBy(() -> session.complete(100L))
                .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("fail() - COMPLETED 상태에서 fail 시 예외 발생")
        void fail_ThrowsException_WhenCompleted() {
            // Given
            UploadSession session = UploadSessionFixture.createSingleCompleted(100L);

            // When & Then
            assertThatThrownBy(() -> session.fail("reason"))
                .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Tell, Don't Ask 패턴 테스트")
    class TellDontAskTests {

        @Test
        @DisplayName("isMultipart() - Multipart 타입 확인")
        void isMultipart_Success() {
            // Given
            UploadSession session = UploadSessionFixture.createMultipart();

            // When
            boolean isMultipart = session.isMultipart();

            // Then
            assertThat(isMultipart).isTrue();
        }

        @Test
        @DisplayName("isInProgress() - 진행 중 상태 확인")
        void isInProgress_Success() {
            // Given
            UploadSession session = UploadSessionFixture.createSingleInProgress();

            // When
            boolean isInProgress = session.isInProgress();

            // Then
            assertThat(isInProgress).isTrue();
        }
    }
}
