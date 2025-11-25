package com.ryuqq.fileflow.adapter.in.redis.config;

import com.ryuqq.fileflow.adapter.in.redis.session.listener.SessionExpirationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Redis Keyspace Notification Configuration.
 *
 * <p>Redis TTL 만료 이벤트를 수신하기 위한 설정입니다.
 *
 * <p><strong>주의</strong>: Redis 서버에서 Keyspace Notification이 활성화되어 있어야 합니다.
 *
 * <pre>
 * redis-cli config set notify-keyspace-events Ex
 * </pre>
 *
 * <p>또는 redis.conf에서:
 *
 * <pre>
 * notify-keyspace-events Ex
 * </pre>
 */
@Configuration
public class RedisKeyspaceNotificationConfig {

    private static final String EXPIRED_EVENT_PATTERN = "__keyevent@*__:expired";

    /**
     * Redis Message Listener Container.
     *
     * <p>Keyspace Notification을 수신하기 위한 컨테이너입니다.
     *
     * @param connectionFactory Redis 연결 팩토리
     * @param sessionExpirationListener 세션 만료 리스너
     * @return RedisMessageListenerContainer
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            SessionExpirationListener sessionExpirationListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // 만료 이벤트 리스너 등록
        container.addMessageListener(
                sessionExpirationListener, new PatternTopic(EXPIRED_EVENT_PATTERN));

        return container;
    }
}
