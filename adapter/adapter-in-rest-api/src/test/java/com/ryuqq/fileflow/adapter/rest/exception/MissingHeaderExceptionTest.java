package com.ryuqq.fileflow.adapter.rest.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MissingHeaderException 테스트
 *
 * @author sangwon-ryu
 */
@DisplayName("MissingHeaderException 테스트")
class MissingHeaderExceptionTest {

    @Test
    @DisplayName("예외 생성 시 메시지와 헤더 이름이 올바르게 설정됨")
    void constructor_SetsMessageAndHeaderName() {
        // Given
        String headerName = "X-Tenant-Id";

        // When
        MissingHeaderException exception = new MissingHeaderException(headerName);

        // Then
        assertThat(exception.getMessage()).contains("Required header is missing: X-Tenant-Id");
        assertThat(exception.getHeaderName()).isEqualTo(headerName);
    }

    @Test
    @DisplayName("getHeaderName()이 올바른 헤더 이름을 반환함")
    void getHeaderName_ReturnsCorrectValue() {
        // Given
        String headerName = "X-User-Type";
        MissingHeaderException exception = new MissingHeaderException(headerName);

        // When
        String result = exception.getHeaderName();

        // Then
        assertThat(result).isEqualTo("X-User-Type");
    }
}
