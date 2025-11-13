package com.ryuqq.fileflow.adapter.out.persistence.mysql.download.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.download.mapper.ExternalDownloadEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.download.repository.ExternalDownloadQueryDslRepository;
import com.ryuqq.fileflow.application.download.port.out.ExternalDownloadQueryPort;
import com.ryuqq.fileflow.domain.download.ExternalDownload;
import com.ryuqq.fileflow.domain.download.ExternalDownloadStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * External Download Query Adapter (CQRS - Query Side)
 *
 * <p>Application Layer의 {@link ExternalDownloadQueryPort}를 구현하는 Persistence Adapter입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>ExternalDownload Domain Aggregate의 조회 (읽기 전용)</li>
 *   <li>QueryDSL을 통한 DB 접근</li>
 *   <li>Mapper를 통한 Entity → Domain 변환</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Port & Adapter 패턴 준수</li>
 *   <li>✅ @Transactional(readOnly = true) 적용</li>
 *   <li>✅ QueryDSL 사용 (성능 최적화)</li>
 *   <li>✅ Mapper를 통한 명시적 변환</li>
 *   <li>✅ Long FK Strategy (JPA 관계 없음)</li>
 *   <li>✅ CQRS - Query 전용 (읽기만)</li>
 *   <li>❌ 비즈니스 로직 포함 금지</li>
 * </ul>
 *
 * <p><strong>Type D 위반 수정:</strong></p>
 * <ul>
 *   <li>✅ QueryDslRepository로 쿼리 로직 분리</li>
 *   <li>✅ Adapter는 Port 구현 및 Mapper 호출만 담당</li>
 *   <li>✅ 복잡한 QueryDSL 로직은 Repository로 위임</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class ExternalDownloadQueryAdapter implements ExternalDownloadQueryPort {

    private final ExternalDownloadQueryDslRepository repository;

    /**
     * 생성자
     *
     * @param repository External Download QueryDSL Repository
     */
    public ExternalDownloadQueryAdapter(ExternalDownloadQueryDslRepository repository) {
        this.repository = repository;
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
        return repository.findByStatus(status).stream()
            .map(ExternalDownloadEntityMapper::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * 재시도 가능한 다운로드 목록 조회
     *
     * <p>Repository에 위임하여 다음 조건을 만족하는 다운로드를 조회합니다:</p>
     * <ul>
     *   <li>DOWNLOADING 상태</li>
     *   <li>재시도 횟수가 최대값 미만</li>
     *   <li>마지막 재시도 시간이 retryAfter 이전</li>
     * </ul>
     *
     * @param maxRetry 최대 재시도 횟수
     * @param retryAfter 재시도 가능 시간 (이 시간 이전에 마지막 재시도한 것만)
     * @return 재시도 가능한 External Download 목록
     */
    @Override
    public List<ExternalDownload> findRetryableDownloads(Integer maxRetry, LocalDateTime retryAfter) {
        return repository.findRetryableDownloads(maxRetry, retryAfter).stream()
            .map(ExternalDownloadEntityMapper::toDomain)
            .collect(Collectors.toList());
    }
}

