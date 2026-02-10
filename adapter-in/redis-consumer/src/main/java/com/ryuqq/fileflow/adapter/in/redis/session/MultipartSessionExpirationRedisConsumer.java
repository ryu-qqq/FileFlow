package com.ryuqq.fileflow.adapter.in.redis.session;

import com.ryuqq.fileflow.adapter.in.redis.config.RedisConsumerProperties;
import com.ryuqq.fileflow.application.session.port.in.command.LockedExpireMultipartUploadSessionUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/**
 * Redis keyspace notification을 통한 멀티파트 업로드 세션 만료 처리.
 *
 * <p>키 포맷: {@code session:expiration:MULTIPART:{sessionId}}
 */
@Component
public class MultipartSessionExpirationRedisConsumer implements MessageListener {

    private static final Logger log =
            LoggerFactory.getLogger(MultipartSessionExpirationRedisConsumer.class);

    private static final String SESSION_TYPE = "MULTIPART";

    private final RedisConsumerProperties properties;
    private final LockedExpireMultipartUploadSessionUseCase expireMultipartUploadSessionUseCase;

    public MultipartSessionExpirationRedisConsumer(
            RedisConsumerProperties properties,
            LockedExpireMultipartUploadSessionUseCase expireMultipartUploadSessionUseCase) {
        this.properties = properties;
        this.expireMultipartUploadSessionUseCase = expireMultipartUploadSessionUseCase;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        String expectedPrefix = properties.sessionExpirationKeyPrefix() + SESSION_TYPE + ":";

        if (!expiredKey.startsWith(expectedPrefix)) {
            return;
        }

        String sessionId = expiredKey.substring(expectedPrefix.length());

        log.info("멀티파트 세션 만료 감지: sessionId={}", sessionId);

        try {
            expireMultipartUploadSessionUseCase.execute(sessionId);
        } catch (Exception e) {
            log.error("멀티파트 세션 만료 처리 실패: sessionId={}", sessionId, e);
        }
    }
}
