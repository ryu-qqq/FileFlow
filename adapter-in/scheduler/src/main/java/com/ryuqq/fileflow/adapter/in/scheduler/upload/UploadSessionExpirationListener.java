package com.ryuqq.fileflow.adapter.in.scheduler.upload;

import com.ryuqq.fileflow.application.upload.service.ExpireUploadSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * Upload Session Expiration Listener
 *
 * <p>Redis Key Expiration Event를 수신하여 만료된 UploadSession을 처리하는 Listener입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Redis Keyspace Notification 구독</li>
 *   <li>Expired Event 수신</li>
 *   <li>ExpireUploadSessionService 호출</li>
 * </ul>
 *
 * <p><strong>Adapter-in 위치 이유:</strong></p>
 * <ul>
 *   <li>✅ Redis Event는 외부 인프라 이벤트 → Adapter-in이 적합</li>
 *   <li>✅ Scheduler와 유사한 주기적 처리 → scheduler 패키지</li>
 *   <li>✅ 헥사고날 아키텍처: 외부 이벤트 → 내부 UseCase 호출</li>
 * </ul>
 *
 * <p><strong>Redis Keyspace Notification 구조:</strong></p>
 * <pre>
 * Channel: __keyevent@0__:expired
 * Message: upload-session:active:{sessionKey}
 *
 * 예시:
 * Channel: __keyevent@0__:expired
 * Message: upload-session:active:sess_abc123xyz
 * </pre>
 *
 * <p><strong>처리 흐름:</strong></p>
 * <ol>
 *   <li>Redis TTL 만료 → Expired Event 발행</li>
 *   <li>RedisMessageListenerContainer가 이벤트 수신</li>
 *   <li>onMessage() 메서드 호출</li>
 *   <li>Session Key 추출</li>
 *   <li>ExpireUploadSessionService.expire() 호출</li>
 * </ol>
 *
 * <p><strong>예외 처리:</strong></p>
 * <ul>
 *   <li>Session Key 추출 실패: 오류 로그, 무시</li>
 *   <li>Service 호출 실패: 오류 로그, 무시 (재시도 없음)</li>
 *   <li>Listener 자체 장애는 Redis 연결 끊김으로 처리</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class UploadSessionExpirationListener implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(UploadSessionExpirationListener.class);

    private static final String KEY_PREFIX = "upload-session:active:";
    private static final String KEYEVENT_PATTERN = "__keyevent@*__:expired";

    private final ExpireUploadSessionService expireUploadSessionService;

    /**
     * 생성자 및 Listener 등록
     *
     * <p><strong>처리 내용:</strong></p>
     * <ol>
     *   <li>RedisMessageListenerContainer 주입</li>
     *   <li>Expired Event Topic 구독 등록</li>
     *   <li>이 Listener를 MessageListener로 등록</li>
     * </ol>
     *
     * <p><strong>Topic 패턴:</strong></p>
     * <ul>
     *   <li>__keyevent@*__:expired → 모든 DB의 Expired Event 구독</li>
     *   <li>DB 0번만 사용하는 경우: __keyevent@0__:expired</li>
     * </ul>
     *
     * @param expireUploadSessionService Upload Session 만료 처리 Service
     * @param redisMessageListenerContainer Redis Message Listener Container
     */
    public UploadSessionExpirationListener(
        ExpireUploadSessionService expireUploadSessionService,
        RedisMessageListenerContainer redisMessageListenerContainer
    ) {
        this.expireUploadSessionService = expireUploadSessionService;

        // Expired Event Topic 구독 등록
        redisMessageListenerContainer.addMessageListener(
            this,
            new PatternTopic(KEYEVENT_PATTERN)
        );

        log.info("UploadSessionExpirationListener registered for Redis Expired Events");
    }

    /**
     * Redis Key Expiration Event 처리
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>Expired Key 추출 (Message body)</li>
     *   <li>Key가 upload-session:active: 패턴인지 확인</li>
     *   <li>Session Key 추출 (prefix 제거)</li>
     *   <li>ExpireUploadSessionService.expire() 호출</li>
     *   <li>예외 발생 시 로그만 기록 (재시도 없음)</li>
     * </ol>
     *
     * <p><strong>예시:</strong></p>
     * <pre>
     * Expired Key: upload-session:active:sess_abc123xyz
     * → Session Key: sess_abc123xyz
     * → expireUploadSessionService.expire("sess_abc123xyz")
     * </pre>
     *
     * <p><strong>예외 처리:</strong></p>
     * <ul>
     *   <li>Key 추출 실패: 오류 로그, 무시</li>
     *   <li>Service 호출 실패: 오류 로그, 무시</li>
     *   <li>재시도 없음 (Fallback Batch가 처리)</li>
     * </ul>
     *
     * @param message Redis Pub/Sub 메시지 (Expired Key)
     * @param pattern 구독 패턴 (__keyevent@*__:expired)
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // 1. Expired Key 추출
            String expiredKey = new String(message.getBody());

            // 2. upload-session:active: 패턴 확인
            if (!expiredKey.startsWith(KEY_PREFIX)) {
                // 다른 Key의 Expired Event는 무시
                return;
            }

            // 3. Session Key 추출
            String sessionKey = expiredKey.substring(KEY_PREFIX.length());

            if (sessionKey.isBlank()) {
                log.warn("Invalid session key from expired Redis key: {}", expiredKey);
                return;
            }

            // 4. 만료 처리
            log.debug("Redis TTL expired for upload session: sessionKey={}", sessionKey);
            expireUploadSessionService.expire(sessionKey);

        } catch (Exception e) {
            // 예외 발생 시 로그만 기록 (재시도 없음)
            // Fallback Batch가 누락된 만료 세션을 처리함
            log.error(
                "Failed to process Redis key expiration event: message={}, pattern={}",
                new String(message.getBody()),
                new String(pattern),
                e
            );
        }
    }
}
