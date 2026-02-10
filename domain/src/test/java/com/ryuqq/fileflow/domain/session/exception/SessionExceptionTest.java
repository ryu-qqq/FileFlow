package com.ryuqq.fileflow.domain.session.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.common.exception.DomainException;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("SessionException 단위 테스트")
class SessionExceptionTest {

    @Test
    @DisplayName("ErrorCode만으로 SessionException을 생성할 수 있다")
    void createsWithErrorCodeOnly() {
        SessionException ex = new SessionException(SessionErrorCode.SESSION_NOT_FOUND);

        assertThat(ex).isInstanceOf(DomainException.class);
        assertThat(ex.getErrorCode()).isEqualTo(SessionErrorCode.SESSION_NOT_FOUND);
        assertThat(ex.code()).isEqualTo("SESSION-001");
        assertThat(ex.httpStatus()).isEqualTo(404);
        assertThat(ex.getMessage()).isEqualTo("세션을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("ErrorCode와 상세 메시지로 SessionException을 생성할 수 있다")
    void createsWithErrorCodeAndDetail() {
        SessionException ex =
                new SessionException(
                        SessionErrorCode.SESSION_EXPIRED, "세션이 만료되었습니다. sessionId: session-001");

        assertThat(ex.getErrorCode()).isEqualTo(SessionErrorCode.SESSION_EXPIRED);
        assertThat(ex.getMessage()).isEqualTo("세션이 만료되었습니다. sessionId: session-001");
    }

    @Test
    @DisplayName("ErrorCode, 상세 메시지, args로 SessionException을 생성할 수 있다")
    void createsWithErrorCodeDetailAndArgs() {
        SessionException ex =
                new SessionException(
                        SessionErrorCode.PART_NUMBER_DUPLICATE,
                        "중복된 파트 번호: 3",
                        Map.of("partNumber", 3));

        assertThat(ex.getErrorCode()).isEqualTo(SessionErrorCode.PART_NUMBER_DUPLICATE);
        assertThat(ex.getMessage()).isEqualTo("중복된 파트 번호: 3");
        assertThat(ex.args()).containsEntry("partNumber", 3);
    }
}
