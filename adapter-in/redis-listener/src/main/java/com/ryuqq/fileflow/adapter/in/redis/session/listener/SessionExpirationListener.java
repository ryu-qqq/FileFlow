package com.ryuqq.fileflow.adapter.in.redis.session.listener;

import com.ryuqq.fileflow.application.session.dto.command.ExpireUploadSessionCommand;
import com.ryuqq.fileflow.application.session.port.in.command.ExpireUploadSessionUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/**
 * Session Expiration Listener.
 *
 * <p>Redis TTL 만료 이벤트를 수신하여 세션 만료 처리를 수행합니다.
 *
 * <p><strong>Key Naming Convention</strong>:
 *
 * <ul>
 *   <li>단일 업로드: cache::single-upload::{sessionId}
 *   <li>멀티파트 업로드: cache::multipart-upload::{sessionId}
 * </ul>
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>Redis 만료 이벤트 수신
 *   <li>Key에서 sessionId 추출
 *   <li>ExpireUploadSessionUseCase 호출
 *   <li>세션 상태 EXPIRED로 변경
 * </ol>
 */
@Component
public class SessionExpirationListener implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(SessionExpirationListener.class);

    private static final String SINGLE_UPLOAD_KEY_PREFIX = "cache::single-upload::";
    private static final String MULTIPART_UPLOAD_KEY_PREFIX = "cache::multipart-upload::";

    private final ExpireUploadSessionUseCase expireUploadSessionUseCase;

    public SessionExpirationListener(ExpireUploadSessionUseCase expireUploadSessionUseCase) {
        this.expireUploadSessionUseCase = expireUploadSessionUseCase;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = new String(message.getBody());

        String sessionId = extractSessionId(expiredKey);
        if (sessionId == null) {
            return; // 다른 키는 무시
        }

        handleExpiration(sessionId);
    }

    private String extractSessionId(String expiredKey) {
        if (expiredKey.startsWith(SINGLE_UPLOAD_KEY_PREFIX)) {
            return expiredKey.substring(SINGLE_UPLOAD_KEY_PREFIX.length());
        } else if (expiredKey.startsWith(MULTIPART_UPLOAD_KEY_PREFIX)) {
            return expiredKey.substring(MULTIPART_UPLOAD_KEY_PREFIX.length());
        }
        return null;
    }

    private void handleExpiration(String sessionId) {
        try {
            ExpireUploadSessionCommand command = ExpireUploadSessionCommand.of(sessionId);
            expireUploadSessionUseCase.execute(command);
            log.info("Session expired successfully: {}", sessionId);
        } catch (Exception e) {
            // 이미 처리되었거나 존재하지 않는 세션인 경우 로깅 후 무시
            log.warn("Failed to expire session: {}. Reason: {}", sessionId, e.getMessage());
        }
    }
}
