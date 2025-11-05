package com.ryuqq.fileflow.domain.download.exception;

import com.ryuqq.fileflow.domain.common.DomainException;
import com.ryuqq.fileflow.domain.download.ExternalDownloadStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Download Domain Exception 테스트
 *
 * <p>Download 바운디드 컨텍스트의 Domain Exception 계층을 검증합니다.</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@DisplayName("Download Domain Exception 테스트")
class DownloadExceptionTest {

    @Nested
    @DisplayName("DownloadNotFoundException 테스트")
    class DownloadNotFoundExceptionTests {

        @Test
        @DisplayName("Download ID로 예외 생성 성공")
        void constructor_WithDownloadId_Success() {
            // When
            DownloadNotFoundException exception = new DownloadNotFoundException(123L);

            // Then
            assertThat(exception).isInstanceOf(DomainException.class);
            assertThat(exception.code()).isEqualTo("DOWNLOAD-001");
            assertThat(exception.getMessage()).contains("123");
        }

        @Test
        @DisplayName("기본 생성자로 예외 생성 성공")
        void constructor_Default_Success() {
            // When
            DownloadNotFoundException exception = new DownloadNotFoundException();

            // Then
            assertThat(exception).isInstanceOf(DomainException.class);
            assertThat(exception.code()).isEqualTo("DOWNLOAD-001");
        }
    }

    @Nested
    @DisplayName("InvalidDownloadStateException 테스트")
    class InvalidDownloadStateExceptionTests {

        @Test
        @DisplayName("상태와 상세 메시지로 예외 생성 성공")
        void constructor_WithStatusAndMessage_Success() {
            // When
            InvalidDownloadStateException exception = new InvalidDownloadStateException(
                ExternalDownloadStatus.COMPLETED,
                "Can only start from INIT state"
            );

            // Then
            assertThat(exception).isInstanceOf(DomainException.class);
            assertThat(exception.code()).isEqualTo("DOWNLOAD-002");
            assertThat(exception.getMessage()).contains("COMPLETED");
            assertThat(exception.getMessage()).contains("Can only start from INIT state");
        }

        @Test
        @DisplayName("상태만으로 예외 생성 성공")
        void constructor_WithStatus_Success() {
            // When
            InvalidDownloadStateException exception = new InvalidDownloadStateException(
                ExternalDownloadStatus.FAILED
            );

            // Then
            assertThat(exception).isInstanceOf(DomainException.class);
            assertThat(exception.code()).isEqualTo("DOWNLOAD-002");
            assertThat(exception.getMessage()).contains("FAILED");
        }

        @Test
        @DisplayName("기본 생성자로 예외 생성 성공")
        void constructor_Default_Success() {
            // When
            InvalidDownloadStateException exception = new InvalidDownloadStateException();

            // Then
            assertThat(exception).isInstanceOf(DomainException.class);
            assertThat(exception.code()).isEqualTo("DOWNLOAD-002");
        }
    }

    @Nested
    @DisplayName("InvalidUrlException 테스트")
    class InvalidUrlExceptionTests {

        @Test
        @DisplayName("상세 메시지로 예외 생성 성공")
        void constructor_WithMessage_Success() {
            // When
            InvalidUrlException exception = new InvalidUrlException("URL은 필수입니다");

            // Then
            assertThat(exception).isInstanceOf(DomainException.class);
            assertThat(exception.code()).isEqualTo("DOWNLOAD-003");
            assertThat(exception.getMessage()).contains("URL은 필수입니다");
        }

        @Test
        @DisplayName("상세 메시지와 원인으로 예외 생성 성공")
        void constructor_WithMessageAndCause_Success() {
            // Given
            Exception cause = new java.net.MalformedURLException("Invalid URL format");

            // When
            InvalidUrlException exception = new InvalidUrlException("URL 형식이 올바르지 않습니다", cause);

            // Then
            assertThat(exception).isInstanceOf(DomainException.class);
            assertThat(exception.code()).isEqualTo("DOWNLOAD-003");
            assertThat(exception.getMessage()).contains("URL 형식이 올바르지 않습니다");
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("기본 생성자로 예외 생성 성공")
        void constructor_Default_Success() {
            // When
            InvalidUrlException exception = new InvalidUrlException();

            // Then
            assertThat(exception).isInstanceOf(DomainException.class);
            assertThat(exception.code()).isEqualTo("DOWNLOAD-003");
        }
    }
}

