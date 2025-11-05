package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.adapter.command;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.MultipartUploadJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.UploadPartJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.mapper.MultipartUploadEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.repository.MultipartUploadJpaRepository;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.repository.UploadPartJpaRepository;
import com.ryuqq.fileflow.application.upload.port.out.command.DeleteMultipartUploadPort;
import com.ryuqq.fileflow.application.upload.port.out.command.SaveMultipartUploadPort;
import com.ryuqq.fileflow.domain.upload.MultipartUpload;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Multipart Upload Command Adapter
 *
 * <p>Application Layer의 Command Port를 구현하는 Persistence Adapter입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>MultipartUpload Domain Aggregate의 영속화 (Write 전담)</li>
 *   <li>CQRS Command Adapter 패턴 구현</li>
 *   <li>UploadPart 연관 데이터 함께 관리</li>
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
public class MultipartUploadCommandAdapter implements SaveMultipartUploadPort, DeleteMultipartUploadPort {

    private final MultipartUploadJpaRepository multipartRepository;
    private final UploadPartJpaRepository partRepository;

    /**
     * 생성자
     *
     * @param multipartRepository Multipart Upload JPA Repository
     * @param partRepository Upload Part JPA Repository
     */
    public MultipartUploadCommandAdapter(
        MultipartUploadJpaRepository multipartRepository,
        UploadPartJpaRepository partRepository
    ) {
        this.multipartRepository = multipartRepository;
        this.partRepository = partRepository;
    }

    /**
     * Multipart Upload 저장
     *
     * <p><strong>저장 처리:</strong></p>
     * <ol>
     *   <li>Multipart Upload Entity 저장</li>
     *   <li>연관된 Upload Part 목록 저장 (기존 삭제 후 재저장)</li>
     *   <li>저장된 데이터로 Domain Aggregate 재구성</li>
     * </ol>
     *
     * <p><strong>주의</strong>: 트랜잭션은 Application Layer에서 관리됨</p>
     *
     * @param multipart Multipart Upload Domain Aggregate
     * @return 저장된 Multipart Upload (ID 포함)
     */
    @Override
    public MultipartUpload save(MultipartUpload multipart) {
        // 1. Domain → Entity 변환
        MultipartUploadJpaEntity entity = MultipartUploadEntityMapper.toEntity(multipart);

        // 2. Multipart Upload 저장
        MultipartUploadJpaEntity saved = multipartRepository.save(entity);

        // 3. Upload Parts 저장 (있는 경우)
        if (multipart.getUploadedParts() != null && !multipart.getUploadedParts().isEmpty()) {
            saveUploadParts(saved.getId(), multipart.getUploadedParts());
        }

        // 4. 저장된 데이터로 Domain 재구성
        List<UploadPartJpaEntity> parts = partRepository.findByMultipartUploadId(saved.getId());
        return MultipartUploadEntityMapper.toDomain(saved, parts);
    }

    /**
     * Multipart Upload 삭제
     *
     * <p>연관된 Upload Part도 함께 삭제됩니다 (Cascade).</p>
     *
     * <p><strong>주의</strong>: 트랜잭션은 Application Layer에서 관리됨</p>
     *
     * @param id Multipart Upload ID
     */
    @Override
    public void delete(Long id) {
        // 1. Upload Parts 먼저 삭제
        partRepository.deleteByMultipartUploadId(id);

        // 2. Multipart Upload 삭제
        multipartRepository.deleteById(id);
    }

    /**
     * Upload Parts 저장 (Private Helper)
     *
     * <p>기존 Parts를 삭제하고 새로운 Parts를 저장합니다 (교체 전략).</p>
     *
     * @param multipartUploadId Multipart Upload ID
     * @param parts Upload Part 목록
     */
    private void saveUploadParts(Long multipartUploadId, List<com.ryuqq.fileflow.domain.upload.UploadPart> parts) {
        // 기존 Parts 삭제
        partRepository.deleteByMultipartUploadId(multipartUploadId);

        // 새로운 Parts 저장
        List<UploadPartJpaEntity> entities = MultipartUploadEntityMapper.partsToEntities(parts, multipartUploadId);
        partRepository.saveAll(entities);
    }
}

