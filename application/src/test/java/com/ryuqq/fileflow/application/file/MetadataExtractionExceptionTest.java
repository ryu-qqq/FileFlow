package com.ryuqq.fileflow.application.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MetadataExtractionException 단위 테스트
 *
 * @author sangwon-ryu
 */
class MetadataExtractionExceptionTest {

    @Test
    @DisplayName("메시지만으로 예외 생성")
    void createException_WithMessage_Success() {
        // Given
        String message = "Failed to extract metadata from file";

        // When
        MetadataExtractionException exception = new MetadataExtractionException(message);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("메시지와 원인으로 예외 생성")
    void createException_WithMessageAndCause_Success() {
        // Given
        String message = "Failed to extract metadata from file";
        Throwable cause = new IllegalArgumentException("Invalid file format");

        // When
        MetadataExtractionException exception = new MetadataExtractionException(message, cause);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCause().getMessage()).isEqualTo("Invalid file format");
    }

    @Test
    @DisplayName("예외를 던지고 캐치할 수 있음")
    void throwException_CanBeCaught() {
        // When & Then
        assertThatThrownBy(() -> {
            throw new MetadataExtractionException("Test exception");
        })
                .isInstanceOf(MetadataExtractionException.class)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Test exception")
                .hasNoCause();
    }

    @Test
    @DisplayName("원인과 함께 예외를 던지고 캐치할 수 있음")
    void throwException_WithCause_CanBeCaught() {
        // Given
        Throwable cause = new IllegalStateException("Invalid state");

        // When & Then
        assertThatThrownBy(() -> {
            throw new MetadataExtractionException("Metadata extraction failed", cause);
        })
                .isInstanceOf(MetadataExtractionException.class)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Metadata extraction failed")
                .hasCause(cause);
    }

    @Test
    @DisplayName("예외 체인이 올바르게 유지됨")
    void exceptionChain_PreservedCorrectly() {
        // Given
        Throwable rootCause = new NullPointerException("Null file");
        Throwable intermediateCause = new IllegalArgumentException("Invalid argument", rootCause);
        MetadataExtractionException exception = new MetadataExtractionException("Extraction failed", intermediateCause);

        // When & Then
        assertThat(exception.getCause()).isEqualTo(intermediateCause);
        assertThat(exception.getCause().getCause()).isEqualTo(rootCause);
        assertThat(exception.getCause().getCause().getCause()).isNull();
    }

    @Test
    @DisplayName("스택 트레이스가 올바르게 생성됨")
    void stackTrace_GeneratedCorrectly() {
        // Given & When
        MetadataExtractionException exception = new MetadataExtractionException("Test message");

        // Then
        assertThat(exception.getStackTrace()).isNotEmpty();
        assertThat(exception.getStackTrace()[0].getClassName())
                .contains("MetadataExtractionExceptionTest");
    }
}
