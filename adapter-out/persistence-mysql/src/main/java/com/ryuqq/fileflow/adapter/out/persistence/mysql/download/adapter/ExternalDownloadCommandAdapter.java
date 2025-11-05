package com.ryuqq.fileflow.adapter.out.persistence.mysql.download.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.download.entity.ExternalDownloadJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.download.mapper.ExternalDownloadEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.download.repository.ExternalDownloadJpaRepository;
import com.ryuqq.fileflow.application.download.port.out.ExternalDownloadCommandPort;
import com.ryuqq.fileflow.domain.download.ExternalDownload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * External Download Command Adapter (CQRS - Command Side)
 *
 * <p>Application Layer의 {@link ExternalDownloadCommandPort}를 구현하는 Persistence Adapter입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>ExternalDownload Domain Aggregate의 영속화 (쓰기 전용)</li>
 *   <li>JPA Repository를 통한 DB 접근</li>
 *   <li>Mapper를 통한 Domain ↔ Entity 변환</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Port & Adapter 패턴 준수</li>
 *   <li>✅ @Transactional 경계 명확히 설정</li>
 *   <li>✅ Mapper를 통한 명시적 변환</li>
 *   <li>✅ Long FK Strategy (JPA 관계 없음)</li>
 *   <li>✅ CQRS - Command 전용 (쓰기만)</li>
 *   <li>❌ 비즈니스 로직 포함 금지</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class ExternalDownloadCommandAdapter implements ExternalDownloadCommandPort {

    private final ExternalDownloadJpaRepository repository;

    /**
     * 생성자
     *
     * @param repository External Download JPA Repository
     */
    public ExternalDownloadCommandAdapter(ExternalDownloadJpaRepository repository) {
        this.repository = repository;
    }

    /**
     * External Download 저장
     *
     * @param download External Download Domain Aggregate
     * @return 저장된 External Download (ID 포함)
     */
    @Override
    @Transactional
    public ExternalDownload save(ExternalDownload download) {
        // 1. Domain → Entity 변환
        ExternalDownloadJpaEntity entity = ExternalDownloadEntityMapper.toEntity(download);

        // 2. 저장
        ExternalDownloadJpaEntity saved = repository.save(entity);

        // 3. Entity → Domain 변환
        return ExternalDownloadEntityMapper.toDomain(saved);
    }

    /**
     * External Download 삭제
     *
     * @param id External Download ID
     */
    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}

