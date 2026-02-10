package com.ryuqq.fileflow.adapter.out.persistence.redis.session.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.out.persistence.redis.session.dto.SessionExpirationRedisData;
import com.ryuqq.fileflow.domain.session.vo.SessionExpiration;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("SessionExpirationRedisMapper 단위 테스트")
class SessionExpirationRedisMapperTest {

    private SessionExpirationRedisMapper sut;

    @BeforeEach
    void setUp() {
        sut = new SessionExpirationRedisMapper();
    }

    @Nested
    @DisplayName("toRedisData 메서드")
    class ToRedisData {

        @Test
        @DisplayName("성공: SessionExpiration을 RedisData로 변환한다")
        void shouldConvertToRedisData() {
            // given
            String sessionId = "session-001";
            String sessionType = "SINGLE";
            Duration ttl = Duration.ofMinutes(30);
            SessionExpiration expiration = SessionExpiration.of(sessionId, sessionType, ttl);

            // when
            SessionExpirationRedisData result = sut.toRedisData(expiration);

            // then
            assertThat(result.key()).isEqualTo("session:expiration:SINGLE:session-001");
            assertThat(result.value()).isEqualTo("SINGLE");
            assertThat(result.ttl()).isEqualTo(Duration.ofMinutes(30));
        }

        @Test
        @DisplayName("성공: MULTIPART 유형도 올바르게 변환한다")
        void shouldConvertMultipartType() {
            // given
            SessionExpiration expiration =
                    SessionExpiration.of("session-002", "MULTIPART", Duration.ofHours(1));

            // when
            SessionExpirationRedisData result = sut.toRedisData(expiration);

            // then
            assertThat(result.key()).isEqualTo("session:expiration:MULTIPART:session-002");
            assertThat(result.value()).isEqualTo("MULTIPART");
            assertThat(result.ttl()).isEqualTo(Duration.ofHours(1));
        }
    }

    @Nested
    @DisplayName("buildKey 메서드")
    class BuildKey {

        @Test
        @DisplayName("성공: sessionType과 sessionId에 prefix를 붙여 키를 생성한다")
        void shouldBuildKeyWithPrefix() {
            // when
            String result = sut.buildKey("SINGLE", "session-001");

            // then
            assertThat(result).isEqualTo("session:expiration:SINGLE:session-001");
        }
    }
}
