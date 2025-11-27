package com.ryuqq.fileflow.domain.session.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("InvalidPartNumberException 단위 테스트")
class InvalidPartNumberExceptionTest {

    @Test
    @DisplayName("유효하지 않은 Part 번호와 전체 개수로 예외를 생성할 수 있다")
    void constructor_WithInvalidPartNumber_ShouldCreateException() {
        // given
        int partNumber = 0;
        int totalParts = 5;

        // when
        InvalidPartNumberException exception =
                new InvalidPartNumberException(partNumber, totalParts);

        // then
        assertThat(exception.code()).isEqualTo("INVALID-PART-NUMBER");
        assertThat(exception.getMessage())
                .contains("Part 번호가 유효하지 않습니다")
                .contains("0")
                .contains("전체: 5");
    }

    @Test
    @DisplayName("DomainException을 상속한다")
    void shouldExtendDomainException() {
        InvalidPartNumberException exception = new InvalidPartNumberException(11, 10);

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
