package com.ryuqq.fileflow.application.session.manager;

import com.ryuqq.fileflow.application.session.port.out.command.UploadSessionCachePersistencePort;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 업로드 세션 캐시 Manager.
 *
 * <p>Redis 캐시 저장 및 예외 처리를 담당합니다.
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>Redis 캐시 저장 (Best-Effort)
 *   <li>캐시 실패 예외 처리 (로깅 후 무시)
 *   <li>RDB 저장 실패 시 캐시 무효화 (선택적)
 * </ul>
 *
 * <p><strong>예외 처리 전략</strong>:
 *
 * <ul>
 *   <li>Redis 장애 시: 로그 남기고 계속 진행 (RDB 폴백 가능)
 *   <li>Redis 장애는 성능 저하일 뿐 기능 장애 아님
 *   <li>Circuit Breaker 패턴 적용 고려 (향후)
 * </ul>
 */
@Component
public class UploadSessionCacheManager {

    private static final Logger log = LoggerFactory.getLogger(UploadSessionCacheManager.class);

    private final UploadSessionCachePersistencePort uploadSessionCachePersistencePort;

    public UploadSessionCacheManager(
            UploadSessionCachePersistencePort uploadSessionCachePersistencePort) {
        this.uploadSessionCachePersistencePort = uploadSessionCachePersistencePort;
    }

    /**
     * 단일 업로드 세션을 캐시에 저장합니다.
     *
     * <p>Redis 장애 시 로그만 남기고 계속 진행합니다.
     *
     * @param session 저장할 세션
     * @param ttl TTL
     */
    public void cacheSingleUpload(SingleUploadSession session, Duration ttl) {
        try {
            uploadSessionCachePersistencePort.persist(session, ttl);
            log.debug(
                    "Cached SingleUploadSession: sessionId={}, ttl={}s",
                    session.getIdValue(),
                    ttl.getSeconds());
        } catch (Exception e) {
            // Redis 장애는 성능 저하일 뿐, 기능 장애 아님 (RDB 폴백 가능)
            log.warn(
                    "Failed to cache SingleUploadSession: sessionId={}, idempotencyKey={},"
                            + " error={}",
                    session.getIdValue(),
                    session.getIdempotencyKey().getValue(),
                    e.getMessage(),
                    e);
            // 예외를 먹지 않고 계속 진행
        }
    }

    /**
     * 멀티파트 업로드 세션을 캐시에 저장합니다.
     *
     * <p>Redis 장애 시 로그만 남기고 계속 진행합니다.
     *
     * @param session 저장할 세션
     * @param ttl TTL
     */
    public void cacheMultipartUpload(MultipartUploadSession session, Duration ttl) {
        try {
            uploadSessionCachePersistencePort.persist(session, ttl);
            log.debug(
                    "Cached MultipartUploadSession: sessionId={}, ttl={}s",
                    session.getId().value(),
                    ttl.getSeconds());
        } catch (Exception e) {
            log.warn(
                    "Failed to cache MultipartUploadSession: sessionId={}, error={}",
                    session.getId().value(),
                    e.getMessage(),
                    e);
        }
    }
}
