package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity.FileAssetJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.mapper.FileAssetEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.repository.FileAssetJpaRepository;
import com.ryuqq.fileflow.application.file.port.out.FileCommandPort;
import com.ryuqq.fileflow.domain.file.asset.FileAsset;

import org.springframework.stereotype.Component;

/**
 * FileCommandPort Adapter (CQRS Command Side)
 *
 * <p>Application Layer의 FileCommandPort를 구현하는 Persistence Adapter</p>
 *
 * <p><strong>책임</strong>:</p>
 * <ul>
 *   <li>FileCommandPort 인터페이스 구현</li>
 *   <li>JPA Repository 호출 및 Domain 변환</li>
 *   <li>DB 데이터 저장 (비즈니스 로직 없음)</li>
 * </ul>
 *
 * <p><strong>트랜잭션 전략</strong>:</p>
 * <ul>
 *   <li>❌ Persistence Adapter에서 @Transactional 사용 금지</li>
 *   <li>✅ Application Layer (UseCase)에서 트랜잭션 관리</li>
 *   <li>✅ Domain → Entity 변환은 Mapper의 reconstitute() 사용</li>
 * </ul>
 *
 * <p><strong>저장 동작 방식</strong>:</p>
 * <ol>
 *   <li>Application Layer에서 트랜잭션 시작 (@Transactional)</li>
 *   <li>Domain 비즈니스 로직 실행</li>
 *   <li>Persistence Adapter 호출 (Domain → Entity 변환 → 저장)</li>
 *   <li>Application Layer에서 트랜잭션 커밋</li>
 * </ol>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class FileCommandAdapter implements FileCommandPort {

    private final FileAssetJpaRepository fileAssetRepository;
    private final FileAssetEntityMapper fileAssetMapper;

    /**
     * 생성자
     *
     * @param fileAssetRepository JPA Repository
     * @param fileAssetMapper Entity Mapper
     */
    public FileCommandAdapter(
        FileAssetJpaRepository fileAssetRepository,
        FileAssetEntityMapper fileAssetMapper
    ) {
        this.fileAssetRepository = fileAssetRepository;
        this.fileAssetMapper = fileAssetMapper;
    }

    /**
     * 파일 저장 (신규 또는 업데이트)
     *
     * <p><strong>신규 저장</strong>:</p>
     * <ul>
     *   <li>FileAsset.getId() == null</li>
     *   <li>JPA AUTO_INCREMENT로 ID 생성</li>
     *   <li>INSERT 쿼리 실행</li>
     * </ul>
     *
     * <p><strong>업데이트</strong>:</p>
     * <ul>
     *   <li>FileAsset.getId() != null</li>
     *   <li>Mapper.reconstitute()로 전체 Entity 재생성</li>
     *   <li>JPA save()로 UPDATE 실행</li>
     * </ul>
     *
     * <p><strong>주의</strong>: 트랜잭션은 Application Layer에서 관리됨</p>
     *
     * @param fileAsset Domain FileAsset
     * @return 저장된 FileAsset (ID 포함)
     */
    @Override
    public FileAsset save(FileAsset fileAsset) {
        if (fileAsset == null) {
            throw new IllegalArgumentException("FileAsset는 null일 수 없습니다");
        }

        if (fileAsset.getId() == null) {
            // 신규 저장
            FileAssetJpaEntity entity = fileAssetMapper.toEntity(fileAsset);
            FileAssetJpaEntity savedEntity = fileAssetRepository.save(entity);
            return fileAssetMapper.toDomain(savedEntity);
        } else {
            // 업데이트 (reconstitute 패턴)
            FileAssetJpaEntity entity = fileAssetMapper.toEntity(fileAsset);
            FileAssetJpaEntity savedEntity = fileAssetRepository.save(entity);
            return fileAssetMapper.toDomain(savedEntity);
        }
    }

    /**
     * 파일 삭제 (물리 삭제)
     *
     * <p><strong>주의</strong>: 일반적으로 Soft Delete 사용 권장</p>
     * <p>물리 삭제는 Batch Job에서만 사용</p>
     *
     * @param id FileAsset ID
     */
    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("FileAsset ID는 필수입니다");
        }

        FileAssetJpaEntity entity = fileAssetRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "FileAsset not found for delete: id=" + id
            ));

        fileAssetRepository.delete(entity);
    }
}
