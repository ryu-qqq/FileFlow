package com.ryuqq.fileflow.domain.session.exception;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("IncompletePartsException 단위 테스트")
class IncompletePartsExceptionTest {

    @Test
    @DisplayName("완료된 Part 개수와 전체 Part 개수로 예외를 생성할 수 있다")
    void constructor_WithCompletedAndTotalParts_ShouldCreateException() {
        // given
        int completedParts = 3;
        int totalParts = 5;

        // when
        IncompletePartsException exception =
                new IncompletePartsException(completedParts, totalParts);

        // then
        assertThat(exception.getMessage()).contains("모든 Part가 완료되지 않았습니다").contains("완료: 3/5");
        assertThat(exception.code()).isEqualTo("INCOMPLETE-PARTS");
    }

    @Test
    @DisplayName("0개 완료된 경우에도 예외를 생성할 수 있다")
    void constructor_WithZeroCompletedParts_ShouldCreateException() {
        // given
        int completedParts = 0;
        int totalParts = 10;

        // when
        IncompletePartsException exception =
                new IncompletePartsException(completedParts, totalParts);

        // then
        assertThat(exception.getMessage()).contains("완료: 0/10");
    }

    @Test
    @DisplayName("완료된 Part가 전체보다 많은 경우에도 예외를 생성할 수 있다")
    void constructor_WithMoreCompletedThanTotal_ShouldCreateException() {
        // given
        int completedParts = 7;
        int totalParts = 5;

        // when
        IncompletePartsException exception =
                new IncompletePartsException(completedParts, totalParts);

        // then
        assertThat(exception.getMessage()).contains("완료: 7/5");
    }

    @Test
    @DisplayName("DomainException을 상속한다")
    void shouldExtendDomainException() {
        // given
        IncompletePartsException exception = new IncompletePartsException(2, 5);

        // when & then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
