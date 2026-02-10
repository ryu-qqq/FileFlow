package com.ryuqq.fileflow.adapter.out.persistence.redis.session.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.ryuqq.fileflow.adapter.out.persistence.redis.session.dto.SessionExpirationRedisData;
import com.ryuqq.fileflow.adapter.out.persistence.redis.session.mapper.SessionExpirationRedisMapper;
import com.ryuqq.fileflow.application.session.port.out.client.SessionExpirationClient;
import com.ryuqq.fileflow.domain.session.vo.SessionExpiration;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@Tag("unit")
@DisplayName("SessionExpirationRedisClient 단위 테스트")
class SessionExpirationRedisClientTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOps;
    private SessionExpirationRedisMapper mapper;
    private SessionExpirationRedisClient sut;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        mapper = mock(SessionExpirationRedisMapper.class);
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        sut = new SessionExpirationRedisClient(redisTemplate, mapper);
    }

    @Nested
    @DisplayName("registerExpiration 메서드")
    class RegisterExpiration {

        @Test
        @DisplayName("성공: 매퍼가 변환한 RedisData로 Redis에 등록한다")
        void shouldSetRedisKeyWithMappedData() {
            // given
            String sessionId = "session-001";
            String sessionType = "SINGLE";
            Duration ttl = Duration.ofMinutes(30);
            SessionExpiration expiration = SessionExpiration.of(sessionId, sessionType, ttl);

            String expectedKey = "session:expiration:" + sessionType + ":" + sessionId;
            SessionExpirationRedisData redisData =
                    new SessionExpirationRedisData(expectedKey, sessionType, ttl);
            given(mapper.toRedisData(expiration)).willReturn(redisData);

            // when
            sut.registerExpiration(expiration);

            // then
            verify(mapper).toRedisData(expiration);
            verify(valueOps).set(expectedKey, sessionType, ttl);
        }

        @Test
        @DisplayName("성공: MULTIPART 세션 유형도 올바르게 등록한다")
        void shouldRegisterMultipartSessionType() {
            // given
            SessionExpiration expiration =
                    SessionExpiration.of("session-002", "MULTIPART", Duration.ofHours(1));

            String expectedKey = "session:expiration:MULTIPART:session-002";
            SessionExpirationRedisData redisData =
                    new SessionExpirationRedisData(expectedKey, "MULTIPART", Duration.ofHours(1));
            given(mapper.toRedisData(expiration)).willReturn(redisData);

            // when
            sut.registerExpiration(expiration);

            // then
            verify(valueOps).set(expectedKey, "MULTIPART", Duration.ofHours(1));
        }
    }

    @Nested
    @DisplayName("removeExpiration 메서드")
    class RemoveExpiration {

        @Test
        @DisplayName("성공: 매퍼가 생성한 키로 Redis에서 삭제한다")
        void shouldDeleteRedisKeyUsingMapper() {
            // given
            String sessionType = "SINGLE";
            String sessionId = "session-001";
            String expectedKey = "session:expiration:" + sessionType + ":" + sessionId;
            given(mapper.buildKey(sessionType, sessionId)).willReturn(expectedKey);

            // when
            sut.removeExpiration(sessionType, sessionId);

            // then
            verify(mapper).buildKey(sessionType, sessionId);
            verify(redisTemplate).delete(expectedKey);
        }

        @Test
        @DisplayName("성공: SessionExpirationClient 인터페이스를 구현한다")
        void shouldImplementSessionExpirationClient() {
            assertThat(sut).isInstanceOf(SessionExpirationClient.class);
        }
    }
}
