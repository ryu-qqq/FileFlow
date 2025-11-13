package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.adapter.command;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.UploadSessionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.mapper.UploadSessionEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.repository.UploadSessionJpaRepository;
import com.ryuqq.fileflow.application.upload.port.out.command.DeleteUploadSessionPort;
import com.ryuqq.fileflow.application.upload.port.out.command.SaveUploadSessionPort;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import org.springframework.stereotype.Component;

/**
 * Upload Session Command Adapter
 *
 * <p>Application Layer의 Command Port를 구현하는 Persistence Adapter입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>UploadSession Domain Aggregate의 영속화 (Write 전담)</li>
 *   <li>CQRS Command Adapter 패턴 구현</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ CQRS Command Adapter (Write 전담)</li>
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
public class UploadSessionCommandAdapter implements SaveUploadSessionPort, DeleteUploadSessionPort {

    private final UploadSessionJpaRepository repository;

    /**
     * 생성자
     *
     * @param repository Upload Session JPA Repository
     */
    public UploadSessionCommandAdapter(UploadSessionJpaRepository repository) {
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

