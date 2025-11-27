package com.ryuqq.fileflow.domain.session.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.session.fixture.UploadSessionIdFixture;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SessionNotFoundException 단위 테스트")
class SessionNotFoundExceptionTest {

    private static final String ERROR_CODE = "SESSION-NOT-FOUND";

    @Nested
    @DisplayName("UploadSessionId 기반 생성자 테스트")
    class UploadSessionIdConstructorTest {

        @Test
        @DisplayName("UploadSessionId로 예외를 생성할 수 있다")
        void constructor_WithUploadSessionId_ShouldCreateException() {
            // given
            UploadSessionId sessionId = UploadSessionIdFixture.fixedUploadSessionId();

            // when
            SessionNotFoundException exception = new SessionNotFoundException(sessionId);

            // then
            assertThat(exception.code()).isEqualTo(ERROR_CODE);
            assertThat(exception.getMessage())
                    .contains("세션을 찾을 수 없습니다")
                    .contains(sessionId.value().toString());
        }
    }

    @Nested
    @DisplayName("문자열 기반 생성자 테스트")
    class StringConstructorTest {

        @Test
        @DisplayName("세션 ID 문자열로 예외를 생성할 수 있다")
        void constructor_WithSessionIdString_ShouldCreateException() {
            // given
            String sessionId = "session-1234";

            // when
            SessionNotFoundException exception = new SessionNotFoundException(sessionId);

            // then
            assertThat(exception.code()).isEqualTo(ERROR_CODE);
            assertThat(exception.getMessage()).contains("세션을 찾을 수 없습니다").contains(sessionId);
        }
    }

    @Test
    @DisplayName("DomainException을 상속한다")
    void shouldExtendDomainException() {
        SessionNotFoundException exception = new SessionNotFoundException("session-1234");

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
