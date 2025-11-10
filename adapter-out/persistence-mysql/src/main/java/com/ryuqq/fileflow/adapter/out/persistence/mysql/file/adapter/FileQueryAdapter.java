package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity.FileAssetJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.mapper.FileAssetEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.repository.FileAssetJpaRepository;
import com.ryuqq.fileflow.application.file.dto.query.FileMetadataQuery;
import com.ryuqq.fileflow.application.file.dto.query.ListFilesQuery;
import com.ryuqq.fileflow.application.file.port.out.FileQueryPort;
import com.ryuqq.fileflow.domain.file.asset.FileAsset;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * FileQueryPort Adapter (CQRS Query Side)
 *
 * <p>Application Layer의 FileQueryPort를 구현하는 Persistence Adapter</p>
 *
 * <p><strong>책임</strong>:</p>
 * <ul>
 *   <li>FileQueryPort 인터페이스 구현</li>
 *   <li>JPA Repository 호출 및 Domain 변환</li>
 *   <li>QueryDSL 기반 동적 쿼리 조립</li>
 * </ul>
 *
 * <p><strong>성능 최적화</strong>:</p>
 * <ul>
 *   <li>❌ Persistence Adapter에서 @Transactional 사용 금지</li>
 *   <li>✅ Application Layer (UseCase)에서 @Transactional(readOnly = true) 관리</li>
 *   <li>인덱스 활용: idx_tenant_org_uploaded, idx_owner, idx_status</li>
 *   <li>QueryDSL 동적 쿼리: 다중 필터 조합 지원</li>
 *   <li>DB 레벨 페이징: offset/limit (Stream skip/limit 대체)</li>
 * </ul>
 *
 * <p><strong>향후 개선 사항</strong>:</p>
 * <ul>
 *   <li>DTO 프로젝션: 필요한 필드만 SELECT</li>
 *   <li>페이징 최적화: Cursor 기반 페이징</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class FileQueryAdapter implements FileQueryPort {

    private final FileAssetJpaRepository fileAssetRepository;

    /**
     * 생성자
     *
     * @param fileAssetRepository JPA Repository
     */
    public FileQueryAdapter(
        FileAssetJpaRepository fileAssetRepository
    ) {
        this.fileAssetRepository = fileAssetRepository;
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
     */
    @Override
    public Optional<FileAsset> findByQuery(FileMetadataQuery query) {
        if (query == null) {
            return Optional.empty();
        }

        Long fileId = query.fileId().value();
        Long tenantId = query.tenantId().value();
        Long organizationId = query.organizationId();

        Optional<FileAssetJpaEntity> entityOpt;

        if (organizationId != null) {
            // Organization 스코프 포함 조회
            entityOpt = fileAssetRepository.findByIdAndTenantIdAndOrganizationId(
                fileId,
                tenantId,
                organizationId
            );
        } else {
            // Tenant 스코프만 조회
            entityOpt = fileAssetRepository.findByIdAndTenantId(
                fileId,
                tenantId
            );
        }

        return entityOpt.map(FileAssetEntityMapper::toDomain);
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
     * <p><strong>QueryDSL 동적 쿼리</strong>: 다중 필터 조합 지원</p>
     *
     * @param query 파일 목록 조회 Query
     * @return FileAsset 목록
     */
    @Override
    public List<FileAsset> findAllByQuery(ListFilesQuery query) {
        if (query == null) {
            return List.of();
        }

        // QueryDSL 동적 쿼리 실행
        List<FileAssetJpaEntity> entities = fileAssetRepository.findAllByDynamicQuery(query);

        // Entity → Domain 변환
        return entities.stream()
            .map(FileAssetEntityMapper::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * 파일 목록 전체 개수 조회
     *
     * <p>페이징 처리를 위한 전체 개수 조회</p>
     *
     * <p><strong>QueryDSL 동적 COUNT 쿼리</strong>: findAllByQuery()와 동일한 필터 적용</p>
     *
     * @param query 파일 목록 조회 Query
     * @return 전체 개수
     */
    @Override
    public long countByQuery(ListFilesQuery query) {
        if (query == null) {
            return 0;
        }

        // QueryDSL 동적 COUNT 쿼리 실행
        return fileAssetRepository.countByDynamicQuery(query);
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
     */
    @Override
    public Optional<FileAsset> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        return fileAssetRepository.findById(id)
            .map(FileAssetEntityMapper::toDomain);
    }

    /**
     * Upload Session ID로 파일 조회
     *
     * @param uploadSessionId Upload Session ID
     * @return FileAsset (Optional)
     */
    @Override
    public Optional<FileAsset> findByUploadSessionId(Long uploadSessionId) {
        if (uploadSessionId == null) {
            return Optional.empty();
        }

        return fileAssetRepository.findByUploadSessionId(uploadSessionId)
            .map(FileAssetEntityMapper::toDomain);
    }
}
