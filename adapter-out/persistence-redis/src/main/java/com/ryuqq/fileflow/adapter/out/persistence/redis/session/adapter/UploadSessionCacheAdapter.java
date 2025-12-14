package com.ryuqq.fileflow.adapter.out.persistence.redis.session.adapter;

import com.ryuqq.fileflow.application.session.port.out.command.UploadSessionCachePersistencePort;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Upload Session Cache Adapter.
 *
 * <p>Redis를 통한 업로드 세션 캐싱을 담당합니다.
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>SingleUploadSession 캐시 저장
 *   <li>MultipartUploadSession 캐시 저장
 *   <li>TTL 기반 자동 만료
 * </ul>
 *
 * <p><strong>Key Naming Convention</strong>:
 *
 * <ul>
 *   <li>단일 업로드: cache::single-upload::{sessionId}
 *   <li>멀티파트 업로드: cache::multipart-upload::{sessionId}
 * </ul>
 *
 * <p><strong>활성화 조건</strong>: {@code redisson.enabled=true}
 */
@Component
@ConditionalOnProperty(
        name = "spring.data.redis.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class UploadSessionCacheAdapter implements UploadSessionCachePersistencePort {

    private static final String SINGLE_UPLOAD_KEY_PREFIX = "cache::single-upload::";
    private static final String MULTIPART_UPLOAD_KEY_PREFIX = "cache::multipart-upload::";

    private final RedisTemplate<String, Object> redisTemplate;

    public UploadSessionCacheAdapter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 단일 업로드 세션을 캐시에 저장합니다.
     *
     * @param session 저장할 세션
     * @param ttl Time-To-Live (만료 시간)
     */
    @Override
    public void persist(SingleUploadSession session, Duration ttl) {
        String key = generateSingleUploadKey(session.getIdValue());
        redisTemplate.opsForValue().set(key, session, ttl);
    }

    /**
     * 멀티파트 업로드 세션을 캐시에 저장합니다.
     *
     * @param session 저장할 세션
     * @param ttl Time-To-Live (만료 시간)
     */
    @Override
    public void persist(MultipartUploadSession session, Duration ttl) {
        String key = generateMultipartUploadKey(session.getId().value().toString());
        redisTemplate.opsForValue().set(key, session, ttl);
    }

    /**
     * 단일 업로드 세션을 캐시에서 삭제합니다.
     *
     * <p>완료/취소된 세션은 즉시 삭제하여 불필요한 TTL 만료 이벤트를 방지합니다.
     *
     * @param sessionId 삭제할 세션 ID
     */
    @Override
    public void deleteSingleUploadSession(UploadSessionId sessionId) {
        String key = generateSingleUploadKey(sessionId.getValue());
        redisTemplate.delete(key);
    }

    /**
     * 멀티파트 업로드 세션을 캐시에서 삭제합니다.
     *
     * <p>완료/취소된 세션은 즉시 삭제하여 불필요한 TTL 만료 이벤트를 방지합니다.
     *
     * @param sessionId 삭제할 세션 ID
     */
    @Override
    public void deleteMultipartUploadSession(UploadSessionId sessionId) {
        String key = generateMultipartUploadKey(sessionId.getValue());
        redisTemplate.delete(key);
    }

    private String generateSingleUploadKey(String sessionId) {
        return SINGLE_UPLOAD_KEY_PREFIX + sessionId;
    }

    private String generateMultipartUploadKey(String sessionId) {
        return MULTIPART_UPLOAD_KEY_PREFIX + sessionId;
    }
}
