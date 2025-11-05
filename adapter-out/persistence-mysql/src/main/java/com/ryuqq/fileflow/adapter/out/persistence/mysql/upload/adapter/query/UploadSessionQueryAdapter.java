package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.adapter.query;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.mapper.UploadSessionEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.repository.UploadSessionJpaRepository;
import com.ryuqq.fileflow.application.upload.port.out.query.LoadUploadSessionPort;
import com.ryuqq.fileflow.domain.upload.SessionKey;
import com.ryuqq.fileflow.domain.upload.SessionStatus;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Upload Session Query Adapter
 *
 * <p>Application Layer의 Query Port를 구현하는 Persistence Adapter입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>UploadSession Domain Aggregate 조회 (Read 전담)</li>
 *   <li>CQRS Query Adapter 패턴 구현</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ CQRS Query Adapter (Read 전담)</li>
 *   <li>❌ Persistence Adapter에서 @Transactional 사용 금지</li>
 *   <li>✅ Application Layer (UseCase)에서 트랜잭션 관리</li>
 *   <li>✅ Mapper를 통한 명시적 변환</li>
 *   <li>❌ 비즈니스 로직 포함 금지</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class UploadSessionQueryAdapter implements LoadUploadSessionPort {

    private final UploadSessionJpaRepository repository;

    /**
     * 생성자
     *
     * @param repository Upload Session JPA Repository
     */
    public UploadSessionQueryAdapter(UploadSessionJpaRepository repository) {
        this.repository = repository;
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
}

