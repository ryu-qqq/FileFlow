package com.ryuqq.fileflow.domain.session.vo;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static com.ryuqq.fileflow.domain.session.fixture.SessionIdFixture.INVALID_SESSION_ID;
import static com.ryuqq.fileflow.domain.session.fixture.SessionIdFixture.forNew;
import static com.ryuqq.fileflow.domain.session.fixture.SessionIdFixture.from;
import static com.ryuqq.fileflow.domain.session.fixture.SessionIdFixture.unassigned;
import static com.ryuqq.fileflow.domain.session.fixture.SessionIdFixture.validValue;

@DisplayName("SessionId VO Tests")
class SessionIdTest {

    @Test
    @DisplayName("forNew()는 유효한 UUID를 생성해야 한다")
    void shouldCreateNewSessionId() {
        // when
        SessionId sessionId = forNew();

        // then
        assertThat(sessionId.value()).isNotBlank();
        assertThatCodeIsValidUuid(sessionId.value());
        assertThat(sessionId.isNew()).isFalse();
    }

    @Test
    @DisplayName("from()은 유효한 UUID 문자열로 SessionId를 생성한다")
    void shouldCreateFromValidUUID() {
        // given
        String uuid = validValue();

        // when
        SessionId sessionId = from(uuid);

        // then
        assertThat(sessionId.value()).isEqualTo(uuid);
        assertThat(sessionId.isNew()).isFalse();
    }

    @Test
    @DisplayName("유효하지 않은 UUID로 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenInvalidUUID() {
        // given
        String invalidUuid = INVALID_SESSION_ID;

        // expect
        assertThatThrownBy(() -> SessionId.from(invalidUuid))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("value가 null이면 isNew()는 true를 반환한다")
    void shouldReturnTrueWhenIsNew() {
        // given
        SessionId sessionId = unassigned();

        // expect
        assertThat(sessionId.isNew()).isTrue();
    }

    private void assertThatCodeIsValidUuid(String value) {
        assertThat(value).satisfies(uuid -> {
            UUID parsed = UUID.fromString(uuid);
            assertThat(parsed).isNotNull();
        });
    }
}

