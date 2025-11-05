package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.adapter.query;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.UploadPartJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.mapper.MultipartUploadEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.repository.MultipartUploadJpaRepository;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.repository.UploadPartJpaRepository;
import com.ryuqq.fileflow.application.upload.port.out.query.LoadMultipartUploadPort;
import com.ryuqq.fileflow.domain.upload.MultipartUpload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Multipart Upload Query Adapter
 *
 * <p>Application Layer의 Query Port를 구현하는 Persistence Adapter입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>MultipartUpload Domain Aggregate 조회 (Read 전담)</li>
 *   <li>CQRS Query Adapter 패턴 구현</li>
 *   <li>UploadPart 연관 데이터 함께 조회</li>
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
public class MultipartUploadQueryAdapter implements LoadMultipartUploadPort {

    private final MultipartUploadJpaRepository multipartRepository;
    private final UploadPartJpaRepository partRepository;

    /**
     * 생성자
     *
     * @param multipartRepository Multipart Upload JPA Repository
     * @param partRepository Upload Part JPA Repository
     */
    public MultipartUploadQueryAdapter(
        MultipartUploadJpaRepository multipartRepository,
        UploadPartJpaRepository partRepository
    ) {
        this.multipartRepository = multipartRepository;
        this.partRepository = partRepository;
    }

    /**
     * ID로 Multipart Upload 조회
     *
     * @param id Multipart Upload ID
     * @return Multipart Upload (Optional)
     */
    @Override
    public Optional<MultipartUpload> findById(Long id) {
        return multipartRepository.findById(id)
            .map(entity -> {
                List<UploadPartJpaEntity> parts = partRepository.findByMultipartUploadId(entity.getId());
                return MultipartUploadEntityMapper.toDomain(entity, parts);
            });
    }

    /**
     * Upload Session ID로 Multipart Upload 조회
     *
     * @param uploadSessionId Upload Session ID
     * @return Multipart Upload (Optional)
     */
    @Override
    public Optional<MultipartUpload> findByUploadSessionId(Long uploadSessionId) {
        return multipartRepository.findByUploadSessionId(uploadSessionId)
            .map(entity -> {
                List<UploadPartJpaEntity> parts = partRepository.findByMultipartUploadId(entity.getId());
                return MultipartUploadEntityMapper.toDomain(entity, parts);
            });
    }

    /**
     * 상태별 Multipart Upload 목록 조회
     *
     * @param status Multipart 상태
     * @return Multipart Upload 목록
     */
    @Override
    public List<MultipartUpload> findByStatus(MultipartUpload.MultipartStatus status) {
        return multipartRepository.findByStatus(status)
            .stream()
            .map(entity -> {
                List<UploadPartJpaEntity> parts = partRepository.findByMultipartUploadId(entity.getId());
                return MultipartUploadEntityMapper.toDomain(entity, parts);
            })
            .collect(Collectors.toList());
    }
}

