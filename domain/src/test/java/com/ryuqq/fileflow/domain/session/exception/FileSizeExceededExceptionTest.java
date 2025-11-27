package com.ryuqq.fileflow.domain.session.exception;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("FileSizeExceededException 단위 테스트")
class FileSizeExceededExceptionTest {

    @Test
    @DisplayName("실제 크기와 최대 크기로 예외를 생성할 수 있다")
    void constructor_WithActualAndMaxSize_ShouldCreateException() {
        // given
        long actualSize = 6L * 1024 * 1024 * 1024; // 6GB
        long maxSize = 5L * 1024 * 1024 * 1024; // 5GB

        // when
        FileSizeExceededException exception = new FileSizeExceededException(actualSize, maxSize);

        // then
        assertThat(exception.getMessage())
                .contains("파일 크기가 최대 허용 크기를 초과했습니다")
                .contains("실제: " + actualSize)
                .contains("최대: " + maxSize);
        assertThat(exception.code()).isEqualTo("FILE-SIZE-EXCEEDED");
    }

    @Test
    @DisplayName("바이트 단위로 크기 정보가 표시된다")
    void getMessage_ShouldShowSizeInBytes() {
        // given
        long actualSize = 1024L; // 1KB
        long maxSize = 512L; // 512B

        // when
        FileSizeExceededException exception = new FileSizeExceededException(actualSize, maxSize);

        // then
        assertThat(exception.getMessage()).contains("실제: 1024").contains("최대: 512");
    }

    @Test
    @DisplayName("0 크기로도 예외를 생성할 수 있다")
    void constructor_WithZeroSizes_ShouldCreateException() {
        // given
        long actualSize = 0L;
        long maxSize = 0L;

        // when
        FileSizeExceededException exception = new FileSizeExceededException(actualSize, maxSize);

        // then
        assertThat(exception.getMessage()).contains("실제: 0").contains("최대: 0");
    }

    @Test
    @DisplayName("DomainException을 상속한다")
    void shouldExtendDomainException() {
        // given
        FileSizeExceededException exception = new FileSizeExceededException(1000L, 500L);

        // when & then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
