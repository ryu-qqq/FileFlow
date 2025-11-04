package com.ryuqq.fileflow.adapter.out.persistence.redis.upload.adapter;

import com.ryuqq.fileflow.application.upload.port.out.UploadSessionCachePort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Upload Session Cache Adapter
 *
 * <p>Application Layer의 Upload Session Cache Port를 구현하는 Redis Adapter입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>UploadSession의 Presigned URL 만료 추적</li>
 *   <li>Redis TTL 기반 자동 만료 처리</li>
 *   <li>Key Expiration Event 발행 준비</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Port & Adapter 패턴 준수 (Hexagonal Architecture)</li>
 *   <li>✅ Infrastructure 세부사항 격리</li>
 *   <li>✅ Simple Value 저장 (복잡한 객체 불필요)</li>
 *   <li>✅ Dependency Inversion: Application → Port ← Adapter</li>
 * </ul>
 *
 * <p><strong>Redis Key 구조:</strong></p>
 * <pre>
 * upload-session:active:{sessionKey}
 *
 * 예시:
 * upload-session:active:sess_abc123xyz
 * Value: "ACTIVE"
 * TTL: 3600초 (1시간)
 * </pre>
 *
 * <p><strong>작동 흐름:</strong></p>
 * <ol>
 *   <li>Presigned URL 생성 시: trackSession() 호출</li>
 *   <li>Redis에 Key 저장 + TTL 설정</li>
 *   <li>TTL 만료 시: Redis가 Expired Event 발행</li>
 *   <li>UploadSessionExpirationListener가 이벤트 수신</li>
 *   <li>ExpireUploadSessionService가 DB 상태 변경</li>
 * </ol>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class UploadSessionCacheAdapter implements UploadSessionCachePort {

    private static final String KEY_PREFIX = "upload-session:active:";

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 생성자
     *
     * @param redisTemplate Redis Template
     */
    public UploadSessionCacheAdapter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Upload Session 추적 시작
     *
     * <p>Presigned URL 생성 시 호출하여 만료 시간을 추적합니다.</p>
     *
     * <p><strong>처리 내용:</strong></p>
     * <ul>
     *   <li>Redis Key 생성: upload-session:active:{sessionKey}</li>
     *   <li>Value: "ACTIVE" (단순 문자열, 값 자체는 중요하지 않음)</li>
     *   <li>TTL 설정: Presigned URL Duration과 동일</li>
     * </ul>
     *
     * <p><strong>TTL 만료 시:</strong></p>
     * <ul>
     *   <li>Redis가 자동으로 Key 삭제</li>
     *   <li>Keyspace Notification으로 Expired Event 발행</li>
     *   <li>UploadSessionExpirationListener가 수신</li>
     * </ul>
     *
     * <p><strong>예시:</strong></p>
     * <pre>{@code
     * // Single Upload (1시간 만료)
     * cachePort.trackSession("sess_abc123", Duration.ofHours(1));
     *
     * // Multipart Upload (24시간 만료)
     * cachePort.trackSession("sess_xyz789", Duration.ofHours(24));
     * }</pre>
     *
     * @param sessionKey UploadSession의 세션 키 (SessionKey.value())
     * @param presignedUrlDuration Presigned URL 만료 시간
     * @throws IllegalArgumentException sessionKey가 null이거나 비어있는 경우
     * @throws IllegalArgumentException presignedUrlDuration이 null이거나 음수인 경우
     */
    @Override
    public void trackSession(String sessionKey, Duration presignedUrlDuration) {
        if (sessionKey == null || sessionKey.isBlank()) {
            throw new IllegalArgumentException("Session key는 필수입니다");
        }
        if (presignedUrlDuration == null || presignedUrlDuration.isNegative()) {
            throw new IllegalArgumentException("Presigned URL duration은 양수여야 합니다");
        }

        String redisKey = KEY_PREFIX + sessionKey;

        // Simple value "ACTIVE" 저장 + TTL 설정
        // Value는 중요하지 않음, TTL과 Key Expiration Event만 필요
        redisTemplate.opsForValue().set(
            redisKey,
            "ACTIVE",
            presignedUrlDuration
        );
    }

    /**
     * Upload Session 추적 해제
     *
     * <p>업로드가 완료되거나 중단된 경우 추적을 해제합니다.</p>
     *
     * <p><strong>호출 시점:</strong></p>
     * <ul>
     *   <li>업로드 완료: CompleteUploadService</li>
     *   <li>업로드 중단: AbortUploadService</li>
     * </ul>
     *
     * <p><strong>처리 내용:</strong></p>
     * <ul>
     *   <li>Redis Key 삭제</li>
     *   <li>TTL 만료 대기 없이 즉시 제거</li>
     *   <li>Expired Event 발생 방지</li>
     * </ul>
     *
     * @param sessionKey UploadSession의 세션 키
     */
    @Override
    public void untrackSession(String sessionKey) {
        if (sessionKey == null || sessionKey.isBlank()) {
            return;  // 이미 없는 경우 무시
        }

        String redisKey = KEY_PREFIX + sessionKey;
        redisTemplate.delete(redisKey);
    }

    /**
     * Upload Session 활성 상태 확인
     *
     * <p>Redis에 Key가 존재하는지 확인합니다.</p>
     *
     * <p><strong>용도:</strong></p>
     * <ul>
     *   <li>디버깅: 세션이 아직 추적 중인지 확인</li>
     *   <li>헬스체크: Redis 연결 확인</li>
     * </ul>
     *
     * @param sessionKey UploadSession의 세션 키
     * @return true if 추적 중, false if 만료되었거나 추적되지 않음
     */
    @Override
    public boolean isTracking(String sessionKey) {
        if (sessionKey == null || sessionKey.isBlank()) {
            return false;
        }

        String redisKey = KEY_PREFIX + sessionKey;
        return redisTemplate.hasKey(redisKey);
    }
}
