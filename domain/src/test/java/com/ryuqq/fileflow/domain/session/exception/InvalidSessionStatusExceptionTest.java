package com.ryuqq.fileflow.domain.session.exception;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("InvalidSessionStatusException 단위 테스트")
class InvalidSessionStatusExceptionTest {

    @Test
    @DisplayName("현재 상태와 요청 상태로 예외를 생성할 수 있다")
    void constructor_WithCurrentAndRequestedStatus_ShouldCreateException() {
        // given
        SessionStatus currentStatus = SessionStatus.COMPLETED;
        SessionStatus requestedStatus = SessionStatus.ACTIVE;

        // when
        InvalidSessionStatusException exception =
                new InvalidSessionStatusException(currentStatus, requestedStatus);

        // then
        assertThat(exception.getMessage())
                .contains("세션 상태 전환이 불가능합니다")
                .contains("현재: COMPLETED")
                .contains("요청: ACTIVE");
        assertThat(exception.code()).isEqualTo("INVALID-SESSION-STATUS");
    }

    @Test
    @DisplayName("예외 메시지에 상태 정보가 포함된다")
    void getMessage_ShouldContainStatusInformation() {
        // given
        SessionStatus currentStatus = SessionStatus.PREPARING;
        SessionStatus requestedStatus = SessionStatus.COMPLETED;

        // when
        InvalidSessionStatusException exception =
                new InvalidSessionStatusException(currentStatus, requestedStatus);

        // then
        assertThat(exception.getMessage()).contains("PREPARING").contains("COMPLETED");
    }

    @Test
    @DisplayName("DomainException을 상속한다")
    void shouldExtendDomainException() {
        // given
        InvalidSessionStatusException exception =
                new InvalidSessionStatusException(SessionStatus.ACTIVE, SessionStatus.PREPARING);

        // when & then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
