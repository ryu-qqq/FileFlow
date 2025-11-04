package com.ryuqq.fileflow.application.file.port.out;

import com.ryuqq.fileflow.application.file.dto.query.FileMetadataQuery;
import com.ryuqq.fileflow.application.file.dto.query.ListFilesQuery;
import com.ryuqq.fileflow.domain.file.asset.FileAsset;

import java.util.List;
import java.util.Optional;

/**
 * File Query Port (CQRS Query Side)
 *
 * <p>파일 조회 전용 Port - Persistence Layer 구현</p>
 *
 * <p><strong>CQRS 설계 원칙:</strong></p>
 * <ul>
 *   <li>읽기 전용 - 상태 변경 없음</li>
 *   <li>최적화된 쿼리 - QueryDSL, Native Query 활용</li>
 *   <li>DTO 프로젝션 - 필요한 필드만 조회</li>
 *   <li>N+1 문제 방지 - Fetch Join 활용</li>
 * </ul>
 *
 * <p><strong>구현 가이드:</strong></p>
 * <ul>
 *   <li>인덱스 활용: (tenant_id, organization_id, uploaded_at)</li>
 *   <li>Soft Delete 필터: deleted_at IS NULL</li>
 *   <li>보안 스코프 강제: 항상 tenant_id 조건 포함</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface FileQueryPort {

    /**
     * 파일 메타데이터 단건 조회
     *
     * <p><strong>조회 조건:</strong></p>
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
    Optional<FileAsset> findByQuery(FileMetadataQuery query);

    /**
     * 파일 목록 조회 (페이징 & 필터링)
     *
     * <p><strong>조회 조건:</strong></p>
     * <ul>
     *   <li>tenantId (필수)</li>
     *   <li>organizationId (선택)</li>
     *   <li>ownerUserId (선택)</li>
     *   <li>status (선택)</li>
     *   <li>visibility (선택)</li>
     *   <li>uploadedAfter/uploadedBefore (선택)</li>
     * </ul>
     *
     * <p><strong>정렬:</strong></p>
     * <ul>
     *   <li>기본: uploaded_at DESC (최근 업로드 순)</li>
     * </ul>
     *
     * @param query 파일 목록 조회 Query
     * @return FileAsset 목록
     */
    List<FileAsset> findAllByQuery(ListFilesQuery query);

    /**
     * 파일 목록 전체 개수 조회
     *
     * <p>페이징 처리를 위한 전체 개수 조회</p>
     *
     * @param query 파일 목록 조회 Query
     * @return 전체 개수
     */
    long countByQuery(ListFilesQuery query);

    /**
     * ID로 FileAsset 조회 (단순 조회)
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <ul>
     *   <li>파일 상세 정보 조회</li>
     *   <li>Pipeline 처리 시 FileAsset 조회</li>
     *   <li>보안 검증 없는 내부 조회</li>
     * </ul>
     *
     * @param id FileAsset ID
     * @return FileAsset (Optional)
     */
    Optional<FileAsset> findById(Long id);

    /**
     * Upload Session ID로 FileAsset 조회 (단순 조회)
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <ul>
     *   <li>업로드 완료 후 FileAsset 조회</li>
     *   <li>MultipartUpload 완료 → FileAsset 매핑</li>
     * </ul>
     *
     * @param uploadSessionId Upload Session ID
     * @return FileAsset (Optional)
     */
    Optional<FileAsset> findByUploadSessionId(Long uploadSessionId);
}
