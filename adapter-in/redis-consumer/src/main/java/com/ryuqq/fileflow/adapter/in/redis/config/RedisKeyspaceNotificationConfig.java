package com.ryuqq.fileflow.adapter.in.redis.config;

import com.ryuqq.fileflow.adapter.in.redis.session.MultipartSessionExpirationRedisConsumer;
import com.ryuqq.fileflow.adapter.in.redis.session.SingleSessionExpirationRedisConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisKeyspaceNotificationConfig {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            SingleSessionExpirationRedisConsumer singleSessionExpirationRedisConsumer,
            MultipartSessionExpirationRedisConsumer multipartSessionExpirationRedisConsumer) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        PatternTopic expiredTopic = new PatternTopic("__keyevent@*__:expired");
        container.addMessageListener(singleSessionExpirationRedisConsumer, expiredTopic);
        container.addMessageListener(multipartSessionExpirationRedisConsumer, expiredTopic);

        return container;
    }
}
