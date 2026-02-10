package com.ryuqq.fileflow.adapter.in.redis.session;

import com.ryuqq.fileflow.adapter.in.redis.config.RedisConsumerProperties;
import com.ryuqq.fileflow.application.session.port.in.command.LockedExpireSingleUploadSessionUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/**
 * Redis keyspace notification을 통한 단건 업로드 세션 만료 처리.
 *
 * <p>키 포맷: {@code session:expiration:SINGLE:{sessionId}}
 */
@Component
public class SingleSessionExpirationRedisConsumer implements MessageListener {

    private static final Logger log =
            LoggerFactory.getLogger(SingleSessionExpirationRedisConsumer.class);

    private static final String SESSION_TYPE = "SINGLE";

    private final RedisConsumerProperties properties;
    private final LockedExpireSingleUploadSessionUseCase expireSingleUploadSessionUseCase;

    public SingleSessionExpirationRedisConsumer(
            RedisConsumerProperties properties,
            LockedExpireSingleUploadSessionUseCase expireSingleUploadSessionUseCase) {
        this.properties = properties;
        this.expireSingleUploadSessionUseCase = expireSingleUploadSessionUseCase;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        String expectedPrefix = properties.sessionExpirationKeyPrefix() + SESSION_TYPE + ":";

        if (!expiredKey.startsWith(expectedPrefix)) {
            return;
        }

        String sessionId = expiredKey.substring(expectedPrefix.length());

        log.info("단건 세션 만료 감지: sessionId={}", sessionId);

        try {
            expireSingleUploadSessionUseCase.execute(sessionId);
        } catch (Exception e) {
            log.error("단건 세션 만료 처리 실패: sessionId={}", sessionId, e);
        }
    }
}
