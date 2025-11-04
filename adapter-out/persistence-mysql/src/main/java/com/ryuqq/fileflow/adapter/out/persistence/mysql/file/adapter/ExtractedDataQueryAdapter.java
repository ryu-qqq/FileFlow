package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity.ExtractedDataJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.mapper.ExtractedDataEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.repository.ExtractedDataJpaRepository;
import com.ryuqq.fileflow.application.file.port.out.ExtractedDataQueryPort;
import com.ryuqq.fileflow.domain.file.extraction.ExtractedData;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ExtractedData Query Adapter (Persistence Layer)
 *
 * <p><strong>역할</strong>: ExtractedData 조회 Command 구현</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/file/adapter/</p>
 * <p><strong>구현</strong>: {@code ExtractedDataQueryPort} 인터페이스 구현</p>
 *
 * <h3>헥사고날 아키텍처 패턴</h3>
 * <ul>
 *   <li>✅ Application Layer의 Port 인터페이스 구현</li>
 *   <li>✅ 의존성 방향: Adapter → Application</li>
 *   <li>✅ Entity ↔ Domain 변환 (Mapper 사용)</li>
 *   <li>❌ 비즈니스 로직 없음 (단순 조회만)</li>
 * </ul>
 *
 * <h3>Transaction 관리</h3>
 * <ul>
 *   <li>✅ {@code @Transactional(readOnly = true)} - 읽기 전용 최적화</li>
 *   <li>✅ Dirty Checking 비활성화</li>
 *   <li>❌ 외부 API 호출 없음 (DB 조회만)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see ExtractedDataQueryPort
 */
@Component
public class ExtractedDataQueryAdapter implements ExtractedDataQueryPort {

    private final ExtractedDataJpaRepository extractedDataJpaRepository;
    private final ExtractedDataEntityMapper extractedDataEntityMapper;

    public ExtractedDataQueryAdapter(
        ExtractedDataJpaRepository extractedDataJpaRepository,
        ExtractedDataEntityMapper extractedDataEntityMapper
    ) {
        this.extractedDataJpaRepository = extractedDataJpaRepository;
        this.extractedDataEntityMapper = extractedDataEntityMapper;
    }

    /**
     * fileId로 모든 ExtractedData 조회
     *
     * <p>Entity → Domain 변환 후 반환</p>
     *
     * @param fileId FileAsset의 ID (Long FK)
     * @return ExtractedData 목록 (빈 리스트 가능)
     */
    @Override
    @Transactional(readOnly = true)
    public List<ExtractedData> findAllByFileId(Long fileId) {
        // 1. Entity 조회 (Soft Delete 필터링 포함)
        List<ExtractedDataJpaEntity> entities = extractedDataJpaRepository.findByFileId(fileId);

        // 2. Entity → Domain 변환
        return entities.stream()
            .map(extractedDataEntityMapper::toDomain)
            .toList();
    }
}
