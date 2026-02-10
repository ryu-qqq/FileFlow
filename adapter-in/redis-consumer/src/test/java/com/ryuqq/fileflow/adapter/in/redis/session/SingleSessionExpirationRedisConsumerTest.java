package com.ryuqq.fileflow.adapter.in.redis.session;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.ryuqq.fileflow.adapter.in.redis.config.RedisConsumerProperties;
import com.ryuqq.fileflow.application.session.port.in.command.LockedExpireSingleUploadSessionUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.data.redis.connection.Message;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("SingleSessionExpirationRedisConsumer 단위 테스트")
class SingleSessionExpirationRedisConsumerTest {

    @Mock private LockedExpireSingleUploadSessionUseCase expireSingleUploadSessionUseCase;

    private SingleSessionExpirationRedisConsumer sut;

    private static final String SESSION_EXPIRATION_KEY_PREFIX = "session:expiration:";

    @BeforeEach
    void setUp() {
        RedisConsumerProperties properties =
                new RedisConsumerProperties(SESSION_EXPIRATION_KEY_PREFIX);
        sut =
                new SingleSessionExpirationRedisConsumer(
                        properties, expireSingleUploadSessionUseCase);
    }

    @Nested
    @DisplayName("onMessage 메서드")
    class OnMessage {

        @Test
        @DisplayName("성공: prefix가 일치하는 SINGLE 키이면 UseCase.execute를 호출한다")
        void shouldCallUseCaseWhenPrefixMatches() {
            // given
            String sessionId = "session-001";
            String expiredKey = SESSION_EXPIRATION_KEY_PREFIX + "SINGLE:" + sessionId;
            Message message =
                    new DefaultMessage("__keyevent@0__:expired".getBytes(), expiredKey.getBytes());

            // when
            sut.onMessage(message, null);

            // then
            then(expireSingleUploadSessionUseCase).should().execute(sessionId);
        }

        @Test
        @DisplayName("무시: prefix가 일치하지 않는 키이면 UseCase를 호출하지 않는다")
        void shouldNotCallUseCaseWhenPrefixDoesNotMatch() {
            // given
            String expiredKey = "other:key:prefix:session-001";
            Message message =
                    new DefaultMessage("__keyevent@0__:expired".getBytes(), expiredKey.getBytes());

            // when
            sut.onMessage(message, null);

            // then
            then(expireSingleUploadSessionUseCase).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("무시: MULTIPART 타입의 키이면 UseCase를 호출하지 않는다")
        void shouldNotCallUseCaseWhenSessionTypeIsMultipart() {
            // given
            String expiredKey = SESSION_EXPIRATION_KEY_PREFIX + "MULTIPART:session-002";
            Message message =
                    new DefaultMessage("__keyevent@0__:expired".getBytes(), expiredKey.getBytes());

            // when
            sut.onMessage(message, null);

            // then
            then(expireSingleUploadSessionUseCase).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("예외 안전: UseCase에서 예외가 발생해도 예외를 전파하지 않는다")
        void shouldNotPropagateExceptionWhenUseCaseThrows() {
            // given
            String sessionId = "session-fail";
            String expiredKey = SESSION_EXPIRATION_KEY_PREFIX + "SINGLE:" + sessionId;
            Message message =
                    new DefaultMessage("__keyevent@0__:expired".getBytes(), expiredKey.getBytes());

            willThrow(new RuntimeException("세션 만료 처리 실패"))
                    .given(expireSingleUploadSessionUseCase)
                    .execute(sessionId);

            // when & then
            assertDoesNotThrow(() -> sut.onMessage(message, null));
            then(expireSingleUploadSessionUseCase).should().execute(sessionId);
        }
    }
}
