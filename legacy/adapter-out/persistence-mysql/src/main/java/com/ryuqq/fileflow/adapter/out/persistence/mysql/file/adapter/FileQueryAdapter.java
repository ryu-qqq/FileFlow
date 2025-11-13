package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.mapper.FileAssetEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.repository.FileQueryDslRepository;
import com.ryuqq.fileflow.application.file.dto.query.FileMetadataQuery;
import com.ryuqq.fileflow.application.file.dto.query.ListFilesQuery;
import com.ryuqq.fileflow.application.file.port.out.FileQueryPort;
import com.ryuqq.fileflow.domain.file.asset.FileAsset;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * FileQueryPort Adapter (CQRS Query Side)
 *
 * <p>CQRS 패턴의 Query 전용 Adapter입니다.
 * FileQueryDslRepository를 통해 성능 최적화된 조회 쿼리를 실행합니다.</p>
 *
 * <p><strong>책임</strong>:</p>
 * <ul>
 *   <li>FileAsset 조회 작업만 담당 (CUD 작업 없음)</li>
 *   <li>Repository 계층에 조회 요청 위임</li>
 *   <li>JPA Entity를 Domain Entity로 변환</li>
 * </ul>
 *
 * <p><strong>설계 원칙</strong>:</p>
 * <ul>
 *   <li>✅ {@code @Component} 사용 (Spring Bean 등록)</li>
 *   <li>✅ {@code FileQueryPort} 구현</li>
 *   <li>✅ Repository에 조회 로직 위임</li>
 *   <li>✅ Mapper를 통한 Entity 변환</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>❌ {@code @Transactional} 사용 금지 (Application Layer에서만)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class FileQueryAdapter implements FileQueryPort {

    private final FileQueryDslRepository repository;

    /**
     * Constructor - 의존성 주입
     *
     * @param repository FileQueryDslRepository
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public FileQueryAdapter(FileQueryDslRepository repository) {
        this.repository = repository;
    }

    /**
     * 파일 메타데이터 단건 조회
     *
     * <p><strong>쿼리 조건</strong>:</p>
     * <ul>
     *   <li>fileId (필수)</li>
     *   <li>tenantId (보안 스코프 - 필수)</li>
     *   <li>organizationId (선택)</li>
     *   <li>deleted_at IS NULL (자동 필터)</li>
     * </ul>
     *
     * @param query 파일 메타데이터 조회 Query
     * @return FileAsset (Optional)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    @Override
    public Optional<FileAsset> findByQuery(FileMetadataQuery query) {
        return repository.findByQuery(query)
            .map(FileAssetEntityMapper::toDomain);
    }

    /**
     * 파일 목록 조회 (페이징 & 필터링)
     *
     * <p><strong>조회 조건</strong>:</p>
     * <ul>
     *   <li>tenantId (필수)</li>
     *   <li>organizationId (선택)</li>
     *   <li>ownerUserId (선택)</li>
     *   <li>status (선택)</li>
     *   <li>visibility (선택)</li>
     *   <li>uploadedAfter/uploadedBefore (선택)</li>
     * </ul>
     *
     * <p><strong>정렬</strong>: uploaded_at DESC (최근 업로드 순)</p>
     *
     * @param query 파일 목록 조회 Query
     * @return FileAsset 목록
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    @Override
    public List<FileAsset> findAllByQuery(ListFilesQuery query) {
        return repository.findAllByQuery(query).stream()
            .map(FileAssetEntityMapper::toDomain)
            .toList();
    }

    /**
     * 파일 목록 전체 개수 조회
     *
     * <p>페이징 처리를 위한 전체 개수 조회</p>
     *
     * @param query 파일 목록 조회 Query
     * @return 전체 개수
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    @Override
    public long countByQuery(ListFilesQuery query) {
        return repository.countByQuery(query);
    }

    /**
     * ID로 FileAsset 조회 (단순 조회)
     *
     * <p><strong>사용 시나리오</strong>:</p>
     * <ul>
     *   <li>파일 상세 정보 조회</li>
     *   <li>Pipeline 처리 시 FileAsset 조회</li>
     *   <li>보안 검증 없는 내부 조회</li>
     * </ul>
     *
     * @param id FileAsset ID
     * @return FileAsset (Optional)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    @Override
    public Optional<FileAsset> findById(Long id) {
        return repository.findById(id)
            .map(FileAssetEntityMapper::toDomain);
    }

    /**
     * Upload Session ID로 파일 조회
     *
     * @param uploadSessionId Upload Session ID
     * @return FileAsset (Optional)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    @Override
    public Optional<FileAsset> findByUploadSessionId(Long uploadSessionId) {
        return repository.findByUploadSessionId(uploadSessionId)
            .map(FileAssetEntityMapper::toDomain);
    }
}
