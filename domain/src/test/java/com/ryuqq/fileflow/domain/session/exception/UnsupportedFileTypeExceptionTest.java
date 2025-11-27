package com.ryuqq.fileflow.domain.session.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UnsupportedFileTypeException 단위 테스트")
class UnsupportedFileTypeExceptionTest {

    @Test
    @DisplayName("지원하지 않는 MIME 타입으로 예외를 생성할 수 있다")
    void constructor_WithUnsupportedMimeType_ShouldCreateException() {
        // given
        String mimeType = "application/pdf";

        // when
        UnsupportedFileTypeException exception = new UnsupportedFileTypeException(mimeType);

        // then
        assertThat(exception.code()).isEqualTo("UNSUPPORTED-FILE-TYPE");
        assertThat(exception.getMessage())
                .contains("지원하지 않는 파일 타입입니다")
                .contains(mimeType)
                .contains("image/*")
                .contains("text/html");
    }

    @Test
    @DisplayName("DomainException을 상속한다")
    void shouldExtendDomainException() {
        UnsupportedFileTypeException exception = new UnsupportedFileTypeException("video/mp4");

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
