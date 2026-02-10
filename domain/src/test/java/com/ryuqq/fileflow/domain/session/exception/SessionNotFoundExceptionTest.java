package com.ryuqq.fileflow.domain.session.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("SessionNotFoundException 단위 테스트")
class SessionNotFoundExceptionTest {

    @Test
    @DisplayName("sessionId로 SessionNotFoundException을 생성할 수 있다")
    void createsWithSessionId() {
        SessionNotFoundException ex = new SessionNotFoundException("session-001");

        assertThat(ex).isInstanceOf(SessionException.class);
        assertThat(ex.getErrorCode()).isEqualTo(SessionErrorCode.SESSION_NOT_FOUND);
        assertThat(ex.getMessage()).contains("session-001");
        assertThat(ex.args()).containsEntry("sessionId", "session-001");
    }

    @Test
    @DisplayName("null sessionId로 생성해도 NullPointerException이 발생하지 않는다")
    void handlesNullSessionId() {
        SessionNotFoundException ex = new SessionNotFoundException(null);

        assertThat(ex.getErrorCode()).isEqualTo(SessionErrorCode.SESSION_NOT_FOUND);
        assertThat(ex.args()).containsEntry("sessionId", "null");
    }
}
