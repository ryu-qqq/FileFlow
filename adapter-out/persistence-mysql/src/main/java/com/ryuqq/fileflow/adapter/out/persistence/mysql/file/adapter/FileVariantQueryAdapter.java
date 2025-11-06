package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity.FileVariantJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.mapper.FileVariantEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.repository.FileVariantJpaRepository;
import com.ryuqq.fileflow.application.file.port.out.FileVariantQueryPort;
import com.ryuqq.fileflow.domain.file.variant.FileVariant;
import com.ryuqq.fileflow.domain.file.variant.VariantType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * FileVariant Query Adapter (Persistence Layer)
 *
 * <p><strong>역할</strong>: FileVariant 조회 Command 구현</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/file/adapter/</p>
 * <p><strong>구현</strong>: {@code FileVariantQueryPort} 인터페이스 구현</p>
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
 * @see FileVariantQueryPort
 */
@Component
public class FileVariantQueryAdapter implements FileVariantQueryPort {

    private final FileVariantJpaRepository fileVariantJpaRepository;
    private final FileVariantEntityMapper fileVariantEntityMapper;

    public FileVariantQueryAdapter(
        FileVariantJpaRepository fileVariantJpaRepository,
        FileVariantEntityMapper fileVariantEntityMapper
    ) {
        this.fileVariantJpaRepository = fileVariantJpaRepository;
        this.fileVariantEntityMapper = fileVariantEntityMapper;
    }

    /**
     * fileId로 모든 FileVariant 조회
     *
     * <p>Entity → Domain 변환 후 반환</p>
     *
     * @param fileId FileAsset의 ID (Long FK)
     * @return FileVariant 목록 (빈 리스트 가능)
     */
    @Override
    @Transactional(readOnly = true)
    public List<FileVariant> findAllByFileId(Long fileId) {
        // 1. Entity 조회 (Repository 메서드명: findByParentFileAssetId)
        List<FileVariantJpaEntity> entities = fileVariantJpaRepository.findByParentFileAssetId(fileId);

        // 2. Entity → Domain 변환
        return entities.stream()
            .map(fileVariantEntityMapper::toDomain)
            .toList();
    }

    /**
     * fileId와 variantType으로 FileVariant 조회
     *
     * <p>Entity → Domain 변환 후 Optional로 반환</p>
     *
     * @param fileId FileAsset의 ID (Long FK)
     * @param variantType Variant Type
     * @return FileVariant (있으면)
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<FileVariant> findByFileIdAndVariantType(Long fileId, VariantType variantType) {
        // 1. Entity 조회 (Repository 메서드명: findByParentFileAssetIdAndVariantType)
        Optional<FileVariantJpaEntity> entity = fileVariantJpaRepository
            .findByParentFileAssetIdAndVariantType(fileId, variantType);

        // 2. Entity → Domain 변환
        return entity.map(fileVariantEntityMapper::toDomain);
    }

    /**
     * fileId와 variantType으로 존재 여부 확인
     *
     * <p>Repository의 existsByParentFileAssetIdAndVariantType 메서드 사용</p>
     *
     * @param fileId FileAsset의 ID (Long FK)
     * @param variantType Variant Type
     * @return 존재 여부
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByFileIdAndVariantType(Long fileId, VariantType variantType) {
        return fileVariantJpaRepository.existsByParentFileAssetIdAndVariantType(fileId, variantType);
    }
}
