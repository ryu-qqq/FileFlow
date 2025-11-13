package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.adapter.query;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.UploadPartJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.mapper.MultipartUploadEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.repository.MultipartUploadQueryDslRepository;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.repository.UploadPartQueryDslRepository;
import com.ryuqq.fileflow.application.upload.port.out.query.LoadMultipartUploadPort;
import com.ryuqq.fileflow.domain.upload.MultipartUpload;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * Multipart Upload Query Adapter (CQRS - Query Side)
 *
 * <p>Application Layer의 {@link LoadMultipartUploadPort}를 구현하는 Query Adapter입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>MultipartUpload Domain Aggregate 조회 (읽기 전용)</li>
 *   <li>QueryDslRepository로 조회 위임</li>
 *   <li>UploadPart 연관 데이터 함께 조회</li>
 *   <li>Mapper를 통한 Entity → Domain 변환</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Port & Adapter 패턴 준수</li>
 *   <li>✅ QueryDSL Repository 위임</li>
 *   <li>✅ Mapper를 통한 명시적 변환</li>
 *   <li>✅ CQRS Query Side 전용 (읽기만)</li>
 *   <li>❌ @Transactional 사용 금지 (Application Layer에서만)</li>
 *   <li>❌ 비즈니스 로직 포함 금지</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class MultipartUploadQueryAdapter implements LoadMultipartUploadPort {

    private final MultipartUploadQueryDslRepository multipartRepository;
    private final UploadPartQueryDslRepository partRepository;

    /**
     * 생성자
     *
     * @param multipartRepository Multipart Upload QueryDSL Repository
     * @param partRepository Upload Part QueryDSL Repository
     */
    public MultipartUploadQueryAdapter(
        MultipartUploadQueryDslRepository multipartRepository,
        UploadPartQueryDslRepository partRepository
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
     * <p>N+1 쿼리 문제를 방지하기 위해 배치 조회를 사용합니다.</p>
     *
     * @param status Multipart 상태
     * @return Multipart Upload 목록
     */
    @Override
    public List<MultipartUpload> findByStatus(MultipartUpload.MultipartStatus status) {
        List<com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.MultipartUploadJpaEntity> multipartEntities =
            multipartRepository.findByStatus(status);

        if (multipartEntities.isEmpty()) {
            return Collections.emptyList();
        }

        // 모든 MultipartUpload ID 수집
        List<Long> multipartIds = multipartEntities.stream()
            .map(com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.MultipartUploadJpaEntity::getId)
            .collect(Collectors.toList());

        // 한 번의 쿼리로 모든 관련 Parts 조회
        List<UploadPartJpaEntity> allParts = partRepository.findByMultipartUploadIds(multipartIds);

        // MultipartUpload ID별로 그룹핑
        Map<Long, List<UploadPartJpaEntity>> partsByMultipartId = allParts.stream()
            .collect(Collectors.groupingBy(
                com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.UploadPartJpaEntity::getMultipartUploadId
            ));

        // Domain 객체로 변환
        return multipartEntities.stream()
            .map(entity -> {
                List<UploadPartJpaEntity> parts = partsByMultipartId.getOrDefault(
                    entity.getId(),
                    Collections.emptyList()
                );
                return MultipartUploadEntityMapper.toDomain(entity, parts);
            })
            .collect(Collectors.toList());
    }
}

