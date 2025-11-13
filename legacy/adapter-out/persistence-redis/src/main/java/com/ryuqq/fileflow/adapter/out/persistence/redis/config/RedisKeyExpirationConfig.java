package com.ryuqq.fileflow.adapter.out.persistence.redis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Redis Key Expiration Config
 *
 * <p>Redis의 Key Expiration Event를 구독하기 위한 설정입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Redis Keyspace Notification 활성화</li>
 *   <li>RedisMessageListenerContainer 설정</li>
 *   <li>Key Expiration Event 구독 준비</li>
 * </ul>
 *
 * <p><strong>Redis 설정 요구사항:</strong></p>
 * <pre>
 * # redis.conf 또는 application.yml
 * spring:
 *   redis:
 *     host: localhost
 *     port: 6379
 *     listener-config:
 *       notify-keyspace-events: Ex  # E=Keyevent, x=Expired
 * </pre>
 *
 * <p><strong>Keyspace Notification 설정:</strong></p>
 * <ul>
 *   <li>E: Keyevent events (key-based notification)</li>
 *   <li>x: Expired events (TTL 만료 이벤트)</li>
 *   <li>Ex: Expired event를 Keyevent로 발행</li>
 * </ul>
 *
 * <p><strong>보안 주의사항:</strong></p>
 * <ul>
 *   <li>⚠️ notify-keyspace-events는 성능 오버헤드 발생 (Redis CPU 사용)</li>
 *   <li>⚠️ 필요한 이벤트만 활성화 (Ex만 사용, A(All) 사용 금지)</li>
 *   <li>⚠️ Production 환경에서는 모니터링 필수</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Configuration
public class RedisKeyExpirationConfig {

    /**
     * RedisMessageListenerContainer 생성
     *
     * <p>Redis Pub/Sub 메시지를 수신하기 위한 컨테이너입니다.</p>
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>Redis 서버에서 Key Expiration 이벤트 발생</li>
     *   <li>Redis Pub/Sub 채널로 이벤트 발행</li>
     *   <li>RedisMessageListenerContainer가 이벤트 수신</li>
     *   <li>등록된 MessageListener에게 이벤트 전달</li>
     *   <li>UploadSessionExpirationListener가 처리</li>
     * </ol>
     *
     * <p><strong>설정:</strong></p>
     * <ul>
     *   <li>Connection Factory: Redis 연결</li>
     *   <li>Thread Pool: 비동기 메시지 처리</li>
     *   <li>Error Handler: 예외 발생 시 로깅</li>
     * </ul>
     *
     * @param connectionFactory Redis 연결 팩토리
     * @return RedisMessageListenerContainer
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
        RedisConnectionFactory connectionFactory
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }
}
