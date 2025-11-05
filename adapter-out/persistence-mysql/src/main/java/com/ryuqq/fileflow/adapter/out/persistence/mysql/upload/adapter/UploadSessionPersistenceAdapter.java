package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.UploadSessionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.mapper.UploadSessionEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.repository.UploadSessionJpaRepository;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.domain.upload.SessionKey;
import com.ryuqq.fileflow.domain.upload.SessionStatus;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Upload Session Persistence Adapter
 *
 * <p>Application Layer의 {@link UploadSessionPort}를 구현하는 Persistence Adapter입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>UploadSession Domain Aggregate의 영속화</li>
 *   <li>JPA Repository를 통한 DB 접근</li>
 *   <li>Mapper를 통한 Domain ↔ Entity 변환</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Port & Adapter 패턴 준수</li>
 *   <li>❌ Persistence Adapter에서 @Transactional 사용 금지</li>
 *   <li>✅ Application Layer (UseCase)에서 트랜잭션 관리</li>
 *   <li>✅ Mapper를 통한 명시적 변환</li>
 *   <li>✅ Long FK Strategy (JPA 관계 없음)</li>
 *   <li>❌ 비즈니스 로직 포함 금지</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class UploadSessionPersistenceAdapter implements UploadSessionPort {

    private final UploadSessionJpaRepository repository;

    /**
     * 생성자
     *
     * @param repository Upload Session JPA Repository
     */
    public UploadSessionPersistenceAdapter(UploadSessionJpaRepository repository) {
        this.repository = repository;
    }

    /**
     * Upload Session 저장
     *
     * <p><strong>저장 처리:</strong></p>
     * <ol>
     *   <li>Domain → Entity 변환</li>
     *   <li>JPA save() 호출</li>
     *   <li>저장된 Entity → Domain 변환</li>
     * </ol>
     *
     * <p><strong>주의</strong>: 트랜잭션은 Application Layer에서 관리됨</p>
     *
     * @param session Upload Session Domain Aggregate
     * @return 저장된 Upload Session (ID 포함)
     */
    @Override
    public UploadSession save(UploadSession session) {
        UploadSessionJpaEntity entity = UploadSessionEntityMapper.toEntity(session);
        UploadSessionJpaEntity saved = repository.save(entity);
        return UploadSessionEntityMapper.toDomain(saved);
    }

    /**
     * ID로 Upload Session 조회
     *
     * @param id Upload Session ID
     * @return Upload Session (Optional)
     */
    @Override
    public Optional<UploadSession> findById(Long id) {
        return repository.findById(id)
            .map(UploadSessionEntityMapper::toDomain);
    }

    /**
     * Session Key로 Upload Session 조회
     *
     * @param sessionKey Session Key
     * @return Upload Session (Optional)
     */
    @Override
    public Optional<UploadSession> findBySessionKey(SessionKey sessionKey) {
        return repository.findBySessionKey(sessionKey.value())
            .map(UploadSessionEntityMapper::toDomain);
    }

    /**
     * 상태와 생성 시간 기준으로 Upload Session 목록 조회
     *
     * <p>만료된 세션 정리 등에 사용됩니다.</p>
     *
     * @param status 세션 상태
     * @param createdBefore 이 시간 이전에 생성된 세션
     * @return Upload Session 목록
     */
    @Override
    public List<UploadSession> findByStatusAndCreatedBefore(
        SessionStatus status,
        LocalDateTime createdBefore
    ) {
        return repository.findByStatusAndCreatedAtBefore(status, createdBefore)
            .stream()
            .map(UploadSessionEntityMapper::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * Upload Session 삭제
     *
     * <p><strong>주의</strong>: 트랜잭션은 Application Layer에서 관리됨</p>
     *
     * @param id Upload Session ID
     */
    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
