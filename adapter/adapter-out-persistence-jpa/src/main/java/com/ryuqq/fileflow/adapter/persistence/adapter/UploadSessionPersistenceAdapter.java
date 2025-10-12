package com.ryuqq.fileflow.adapter.persistence.adapter;

import com.ryuqq.fileflow.adapter.persistence.entity.UploadSessionEntity;
import com.ryuqq.fileflow.adapter.persistence.mapper.UploadSessionMapper;
import com.ryuqq.fileflow.adapter.persistence.repository.UploadSessionJpaRepository;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.upload.vo.UploadRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * UploadSession Persistence Adapter
 *
 * Hexagonal Architecture의 Outbound Adapter로서,
 * UploadSessionPort 인터페이스를 구현하여 데이터베이스 영속성을 제공합니다.
 *
 * 구현 Port:
 * - UploadSessionPort: 업로드 세션 저장, 조회, 삭제
 *
 * 트랜잭션 관리:
 * - 트랜잭션은 Application Layer의 UseCase에서 관리됩니다
 * - Adapter는 순수한 데이터 접근 계층으로 동작합니다
 *
 * @author sangwon-ryu
 */
@Component
public class UploadSessionPersistenceAdapter implements UploadSessionPort {

    private final UploadSessionJpaRepository repository;
    private final UploadSessionMapper mapper;

    public UploadSessionPersistenceAdapter(
            UploadSessionJpaRepository repository,
            UploadSessionMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // ========== UploadSessionPort Implementation ==========

    @Override
    public UploadSession save(UploadSession session) {
        if (session == null) {
            throw new IllegalArgumentException("UploadSession cannot be null");
        }

        // PolicyKey에서 tenantId 추출 (tenantId:userType:serviceType)
        String tenantId = session.getPolicyKey().getValue().split(":")[0];

        // 기존 엔티티 조회 (UPDATE 시) 또는 새 엔티티 생성 (INSERT 시)
        UploadSessionEntity entity = repository.findBySessionId(session.getSessionId())
                .map(existing -> updateExistingEntity(existing, session, tenantId))
                .orElseGet(() -> mapper.toEntity(session, tenantId));

        UploadSessionEntity savedEntity = repository.save(entity);

        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<UploadSession> findById(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("SessionId cannot be null or empty");
        }

        return repository.findBySessionId(sessionId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<UploadSession> findByIdempotencyKey(IdempotencyKey idempotencyKey) {
        if (idempotencyKey == null) {
            throw new IllegalArgumentException("IdempotencyKey cannot be null");
        }

        String idempotencyKeyValue = idempotencyKey.value();

        return repository.findByIdempotencyKey(idempotencyKeyValue)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsById(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("SessionId cannot be null or empty");
        }

        return repository.existsBySessionId(sessionId);
    }

    @Override
    public void deleteById(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("SessionId cannot be null or empty");
        }

        repository.deleteBySessionId(sessionId);
    }

    @Override
    public List<UploadSession> findExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();

        return repository.findExpiredSessions(now)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    // ========== Private Helper Methods ==========

    /**
     * 기존 엔티티의 필드를 업데이트합니다.
     * ID는 유지하여 JPA가 UPDATE를 수행하도록 합니다.
     *
     * @param existing 기존 엔티티
     * @param session 업데이트할 도메인 객체
     * @param tenantId 테넌트 ID
     * @return 업데이트된 엔티티
     */
    private UploadSessionEntity updateExistingEntity(
            UploadSessionEntity existing,
            UploadSession session,
            String tenantId
    ) {
        UploadRequest request = session.getUploadRequest();

        // IdempotencyKey는 nullable이므로 null 체크
        String idempotencyKeyValue = request.idempotencyKey() != null
                ? request.idempotencyKey().value()
                : null;

        // 기존 엔티티의 필드를 업데이트 (ID는 유지)
        // NOTE: UploadSessionEntity에는 setter가 없으므로,
        // 동일한 ID를 가진 새 엔티티를 생성하는 방식 사용
        return UploadSessionEntity.reconstituteWithId(
                existing.getId(), // 기존 ID 유지
                session.getSessionId(),
                idempotencyKeyValue,
                tenantId,
                session.getPolicyKey().getValue(),
                request.fileName(),
                request.contentType(),
                request.fileSizeBytes(),
                session.getStatus(),
                null, // presignedUrl will be set separately
                session.getExpiresAt(),
                existing.getCreatedAt() // 생성 시간 유지
        );
    }
}
