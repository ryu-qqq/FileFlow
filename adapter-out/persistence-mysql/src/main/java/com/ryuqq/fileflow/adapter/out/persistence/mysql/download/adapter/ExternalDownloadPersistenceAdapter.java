package com.ryuqq.fileflow.adapter.out.persistence.mysql.download.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.download.entity.ExternalDownloadJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.download.mapper.ExternalDownloadEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.download.repository.ExternalDownloadJpaRepository;
import com.ryuqq.fileflow.application.download.port.out.ExternalDownloadPort;
import com.ryuqq.fileflow.domain.download.ExternalDownload;
import com.ryuqq.fileflow.domain.download.ExternalDownloadStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * External Download Persistence Adapter
 *
 * <p>Application Layer의 {@link ExternalDownloadPort}를 구현하는 Persistence Adapter입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>ExternalDownload Domain Aggregate의 영속화</li>
 *   <li>JPA Repository를 통한 DB 접근</li>
 *   <li>Mapper를 통한 Domain ↔ Entity 변환</li>
 *   <li>재시도 가능한 다운로드 조회</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Port & Adapter 패턴 준수</li>
 *   <li>✅ @Transactional 경계 명확히 설정</li>
 *   <li>✅ Mapper를 통한 명시적 변환</li>
 *   <li>✅ Long FK Strategy (JPA 관계 없음)</li>
 *   <li>❌ 비즈니스 로직 포함 금지</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class ExternalDownloadPersistenceAdapter implements ExternalDownloadPort {

    private final ExternalDownloadJpaRepository repository;

    /**
     * 생성자
     *
     * @param repository External Download JPA Repository
     */
    public ExternalDownloadPersistenceAdapter(ExternalDownloadJpaRepository repository) {
        this.repository = repository;
    }

    /**
     * External Download 저장
     *
     * @param download External Download Domain Aggregate
     * @return 저장된 External Download (ID 포함)
     */
    @Override
    public ExternalDownload save(ExternalDownload download) {
        // 1. Domain → Entity 변환
        ExternalDownloadJpaEntity entity = ExternalDownloadEntityMapper.toEntity(download);

        // 2. 저장
        ExternalDownloadJpaEntity saved = repository.save(entity);

        // 3. Entity → Domain 변환
        return ExternalDownloadEntityMapper.toDomain(saved);
    }

    /**
     * ID로 External Download 조회
     *
     * @param id External Download ID
     * @return External Download (Optional)
     */
    @Override
    public Optional<ExternalDownload> findById(Long id) {
        return repository.findById(id)
            .map(ExternalDownloadEntityMapper::toDomain);
    }

    /**
     * Upload Session ID로 External Download 조회
     *
     * @param uploadSessionId Upload Session ID
     * @return External Download (Optional)
     */
    @Override
    public Optional<ExternalDownload> findByUploadSessionId(Long uploadSessionId) {
        return repository.findByUploadSessionId(uploadSessionId)
            .map(ExternalDownloadEntityMapper::toDomain);
    }

    /**
     * 상태별 External Download 목록 조회
     *
     * @param status External Download 상태
     * @return External Download 목록
     */
    @Override
    public List<ExternalDownload> findByStatus(ExternalDownloadStatus status) {
        return repository.findByStatus(status)
            .stream()
            .map(ExternalDownloadEntityMapper::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * 재시도 가능한 다운로드 목록 조회
     *
     * <p>DOWNLOADING 상태이면서 재시도 횟수가 최대값 미만인 다운로드를 조회합니다.</p>
     *
     * @param maxRetry 최대 재시도 횟수
     * @param retryAfter 재시도 가능 시간 (이 시간 이전에 마지막 재시도한 것만)
     * @return 재시도 가능한 External Download 목록
     */
    @Override
    public List<ExternalDownload> findRetryableDownloads(Integer maxRetry, LocalDateTime retryAfter) {
        return repository.findRetryableDownloads(maxRetry, retryAfter)
            .stream()
            .map(ExternalDownloadEntityMapper::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * External Download 삭제
     *
     * @param id External Download ID
     */
    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
