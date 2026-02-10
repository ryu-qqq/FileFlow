package com.ryuqq.fileflow.application.session.manager.client;

import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.session.port.out.client.SessionExpirationClient;
import com.ryuqq.fileflow.domain.session.vo.SessionExpiration;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("SessionExpirationManager 단위 테스트")
class SessionExpirationManagerTest {

    @InjectMocks private SessionExpirationManager sut;
    @Mock private SessionExpirationClient sessionExpirationClient;

    @Nested
    @DisplayName("registerExpiration 메서드")
    class RegisterExpirationTest {

        @Test
        @DisplayName("클라이언트에 위임하여 만료를 등록한다")
        void registerExpiration_DelegatesToClient() {
            // given
            SessionExpiration expiration =
                    SessionExpiration.of("session-001", "SINGLE", Duration.ofHours(1));

            // when
            sut.registerExpiration(expiration);

            // then
            then(sessionExpirationClient).should().registerExpiration(expiration);
        }
    }

    @Nested
    @DisplayName("removeExpiration 메서드")
    class RemoveExpirationTest {

        @Test
        @DisplayName("클라이언트에 위임하여 만료를 제거한다")
        void removeExpiration_DelegatesToClient() {
            // given
            String sessionType = "SINGLE";
            String sessionId = "session-001";

            // when
            sut.removeExpiration(sessionType, sessionId);

            // then
            then(sessionExpirationClient).should().removeExpiration(sessionType, sessionId);
        }
    }
}
