package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.mapper.FileVariantEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.repository.FileVariantQueryDslRepository;
import com.ryuqq.fileflow.application.file.port.out.FileVariantQueryPort;
import com.ryuqq.fileflow.domain.file.variant.FileVariant;
import com.ryuqq.fileflow.domain.file.variant.VariantType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * FileVariantQueryAdapter - FileVariant Query 전용 Persistence Adapter
 *
 * <p>CQRS 패턴의 Query 전용 Adapter입니다.
 * QueryDSL을 사용하여 성능 최적화된 조회 쿼리를 실행합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>FileVariant 조회 작업만 담당 (CUD 작업 없음)</li>
 *   <li>QueryDSL Repository를 통한 조회</li>
 *   <li>JPA Entity를 Domain Entity로 변환</li>
 *   <li>Parent FileAsset ID 기반 조회 (Long FK 전략)</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ {@code @Component} 사용 (Spring Bean 등록)</li>
 *   <li>✅ {@code FileVariantQueryPort} 구현</li>
 *   <li>✅ QueryDSL Repository 위임</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>❌ {@code @Transactional} 사용 금지 (Application Layer에서만)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see FileVariantQueryPort
 * @see FileVariantQueryDslRepository
 */
@Component
public class FileVariantQueryAdapter implements FileVariantQueryPort {

    private final FileVariantQueryDslRepository repository;

    /**
     * Constructor - 의존성 주입
     *
     * @param repository FileVariant QueryDSL Repository
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public FileVariantQueryAdapter(FileVariantQueryDslRepository repository) {
        this.repository = repository;
    }

    /**
     * fileId로 모든 FileVariant 조회
     *
     * <p>QueryDSL을 사용하여 Parent FileAsset ID로 모든 Variant를 조회합니다.
     * createdAt 내림차순으로 정렬하여 최신 순으로 반환합니다.</p>
     *
     * @param fileId FileAsset의 ID (Long FK)
     * @return FileVariant 목록 (빈 리스트 가능)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    @Override
    public List<FileVariant> findAllByFileId(Long fileId) {
        return repository.findAllByFileId(fileId).stream()
            .map(FileVariantEntityMapper::toDomain)
            .toList();
    }

    /**
     * fileId와 variantType으로 FileVariant 조회
     *
     * <p>QueryDSL을 사용하여 Parent FileAsset ID와 Variant Type으로 단건 조회합니다.</p>
     *
     * @param fileId FileAsset의 ID (Long FK)
     * @param variantType Variant Type
     * @return FileVariant (있으면)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    @Override
    public Optional<FileVariant> findByFileIdAndVariantType(Long fileId, VariantType variantType) {
        return repository.findByFileIdAndVariantType(fileId, variantType)
            .map(FileVariantEntityMapper::toDomain);
    }

    /**
     * fileId와 variantType으로 존재 여부 확인
     *
     * <p>QueryDSL을 사용하여 Parent FileAsset ID와 Variant Type으로 존재 여부를 확인합니다.</p>
     *
     * @param fileId FileAsset의 ID (Long FK)
     * @param variantType Variant Type
     * @return 존재 여부
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    @Override
    public boolean existsByFileIdAndVariantType(Long fileId, VariantType variantType) {
        return repository.existsByFileIdAndVariantType(fileId, variantType);
    }
}
