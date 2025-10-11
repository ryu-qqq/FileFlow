package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.upload.port.out.UploadSessionCachePort;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * UploadSession 영속성 전용 Service
 *
 * 트랜잭션 경계를 명확히 하기 위해 DB 작업만 분리한 서비스입니다.
 * DB 저장과 함께 Redis에도 TTL과 함께 저장하여 만료 감지를 지원합니다.
 *
 * 설계 원칙:
 * - 외부 API 호출 없음 (S3, SQS 등)
 * - 명시적 @Transactional 선언으로 트랜잭션 경계 명확화
 * - 빠른 트랜잭션 커밋으로 DB 락 최소화
 * - Redis 저장은 Best Effort (실패해도 DB 저장은 성공)
 *
 * @author sangwon-ryu
 */
@Service
public class UploadSessionPersistenceService {

    private final UploadSessionPort uploadSessionPort;
    private final UploadSessionCachePort uploadSessionCachePort;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param uploadSessionPort 세션 저장소
     * @param uploadSessionCachePort 세션 캐시 저장소
     * @throws NullPointerException 파라미터가 null인 경우
     */
    public UploadSessionPersistenceService(
            UploadSessionPort uploadSessionPort,
            UploadSessionCachePort uploadSessionCachePort
    ) {
        this.uploadSessionPort = Objects.requireNonNull(
                uploadSessionPort,
                "UploadSessionPort must not be null"
        );
        this.uploadSessionCachePort = Objects.requireNonNull(
                uploadSessionCachePort,
                "UploadSessionCachePort must not be null"
        );
    }

    /**
     * 업로드 세션을 저장합니다.
     *
     * 저장 전략:
     * 1. DB에 저장 (트랜잭션 내)
     * 2. Redis에 TTL과 함께 저장 (Best Effort)
     *
     * Redis 저장이 실패해도 DB 저장은 성공합니다.
     * Redis는 만료 감지 용도이므로 Best Effort로 처리합니다.
     *
     * 트랜잭션 경계: 이 메서드 내부에서만 트랜잭션이 시작되고 종료됩니다.
     * 외부 API 호출이 없으므로 빠르게 커밋됩니다.
     *
     * @param session 저장할 업로드 세션
     * @return 저장된 업로드 세션
     * @throws IllegalArgumentException session이 null인 경우
     */
    @Transactional
    public UploadSession saveSession(UploadSession session) {
        if (session == null) {
            throw new IllegalArgumentException("UploadSession must not be null");
        }

        // 1. DB에 저장 (트랜잭션 내)
        UploadSession savedSession = uploadSessionPort.save(session);

        // 2. Redis에 TTL과 함께 저장 (Best Effort - 트랜잭션 커밋 후)
        uploadSessionCachePort.saveWithTtl(savedSession);

        return savedSession;
    }

    /**
     * 업로드 세션을 실패 처리합니다.
     *
     * 보상 트랜잭션으로 사용됩니다.
     * S3 Presigned URL 생성은 성공했지만 DB 저장이 실패한 경우,
     * 해당 세션을 FAILED 상태로 변경하여 정리합니다.
     *
     * @param sessionId 세션 ID
     * @param reason 실패 사유
     * @return 실패 처리된 세션
     * @throws IllegalArgumentException sessionId 또는 reason이 null이거나 비어있는 경우
     * @throws UploadSessionNotFoundException 세션을 찾을 수 없는 경우
     */
    @Transactional
    public UploadSession failSession(String sessionId, String reason) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("SessionId must not be null or empty");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Reason must not be null or empty");
        }

        UploadSession session = uploadSessionPort.findById(sessionId)
                .orElseThrow(() -> new UploadSessionNotFoundException(sessionId));

        UploadSession failedSession = session.fail();
        return uploadSessionPort.save(failedSession);
    }
}
