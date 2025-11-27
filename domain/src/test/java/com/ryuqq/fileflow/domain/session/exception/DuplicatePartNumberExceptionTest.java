package com.ryuqq.fileflow.domain.session.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DuplicatePartNumberException 단위 테스트")
class DuplicatePartNumberExceptionTest {

    @Test
    @DisplayName("중복된 Part 번호로 예외를 생성할 수 있다")
    void constructor_WithDuplicatePartNumber_ShouldCreateException() {
        // given
        int partNumber = 3;

        // when
        DuplicatePartNumberException exception = new DuplicatePartNumberException(partNumber);

        // then
        assertThat(exception.code()).isEqualTo("DUPLICATE-PART-NUMBER");
        assertThat(exception.getMessage()).contains("Part 3는 이미 완료되었습니다");
    }

    @Test
    @DisplayName("DomainException을 상속한다")
    void shouldExtendDomainException() {
        DuplicatePartNumberException exception = new DuplicatePartNumberException(1);

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
