package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity.ExtractedDataJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.mapper.ExtractedDataEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.repository.ExtractedDataQueryDslRepository;
import com.ryuqq.fileflow.application.file.port.out.ExtractedDataQueryPort;
import com.ryuqq.fileflow.domain.file.extraction.ExtractedData;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ExtractedDataQueryAdapter - ExtractedData Query 전용 Persistence Adapter
 *
 * <p>CQRS 패턴의 Query 전용 Adapter입니다.
 * QueryDSL Repository를 위임하여 성능 최적화된 조회 쿼리를 실행합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>ExtractedData 조회 작업만 담당 (CUD 작업 없음)</li>
 *   <li>QueryDSL Repository로 쿼리 위임</li>
 *   <li>JpaEntity를 Domain Entity로 변환</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ {@code @Component} 사용 (Spring Bean 등록)</li>
 *   <li>✅ {@code ExtractedDataQueryPort} 구현</li>
 *   <li>✅ QueryDSL Repository 위임</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>❌ {@code @Transactional} 사용 금지 (Application Layer에서만)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class ExtractedDataQueryAdapter implements ExtractedDataQueryPort {

    private final ExtractedDataQueryDslRepository repository;

    /**
     * Constructor - 의존성 주입
     *
     * @param repository QueryDSL Repository
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public ExtractedDataQueryAdapter(ExtractedDataQueryDslRepository repository) {
        this.repository = repository;
    }

    /**
     * fileId로 모든 ExtractedData 조회
     *
     * <p>특정 FileAsset에서 추출된 모든 메타데이터를 조회합니다.
     * Soft Delete된 데이터는 자동으로 필터링됩니다.</p>
     *
     * <p>QueryDSL 쿼리 예시:</p>
     * <pre>{@code
     * SELECT *
     * FROM extracted_data
     * WHERE file_id = ?
     *   AND deleted_at IS NULL
     * ORDER BY extracted_at ASC
     * }</pre>
     *
     * @param fileId FileAsset의 ID (Long FK)
     * @return ExtractedData 목록 (빈 리스트 가능)
     * @throws IllegalArgumentException fileId가 null이거나 0 이하인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    @Override
    public List<ExtractedData> findAllByFileId(Long fileId) {
        List<ExtractedDataJpaEntity> entities = repository.findAllByFileId(fileId);

        return entities.stream()
            .map(ExtractedDataEntityMapper::toDomain)
            .toList();
    }
}
