package com.ryuqq.fileflow.domain.session.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ryuqq.fileflow.domain.common.exception.DomainException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FileSizeExceededException Tests")
class FileSizeExceededExceptionTest {

    @Test
    @DisplayName("actualSize와 maxSize로 예외를 생성할 수 있어야 한다")
    void shouldCreateExceptionWithCorrectMessage() {
        // given
        long actualSize = 10_000_000_000L; // 10GB
        long maxSize = 5_000_000_000L; // 5GB

        // when
        FileSizeExceededException exception = new FileSizeExceededException(actualSize, maxSize);

        // then
        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getMessage()).contains("파일 크기가 최대 허용 크기를 초과했습니다");
        assertThat(exception.getMessage()).contains("실제: 10000000000");
        assertThat(exception.getMessage()).contains("최대: 5000000000");
        assertThat(exception.errorCode()).isEqualTo(SessionErrorCode.FILE_SIZE_EXCEEDED);
    }

    @Test
    @DisplayName("HTTP 상태 코드는 400을 반환해야 한다")
    void shouldReturnHttpStatus400() {
        // given
        long actualSize = 10_000_000_000L;
        long maxSize = 5_000_000_000L;

        // when
        FileSizeExceededException exception = new FileSizeExceededException(actualSize, maxSize);

        // then
        assertThat(exception.httpStatus()).isEqualTo(400);
        assertThat(exception.code()).isEqualTo("FILE-SIZE-EXCEEDED");
    }
}

