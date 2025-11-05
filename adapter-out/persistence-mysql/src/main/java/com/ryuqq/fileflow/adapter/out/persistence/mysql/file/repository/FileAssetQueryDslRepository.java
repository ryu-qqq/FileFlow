package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.querydsl;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity.FileAssetJpaEntity;
import com.ryuqq.fileflow.application.file.dto.query.ListFilesQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * FileAsset QueryDSL Custom Repository
 *
 * <p>QueryDSL을 사용한 동적 쿼리 지원</p>
 *
 * <p><strong>책임</strong>:</p>
 * <ul>
 *   <li>복잡한 동적 쿼리 조립 (BooleanBuilder)</li>
 *   <li>다중 필터 조합 지원</li>
 *   <li>효율적인 페이징 처리</li>
 *   <li>최적화된 COUNT 쿼리</li>
 * </ul>
 *
 * <p><strong>성능 최적화</strong>:</p>
 * <ul>
 *   <li>BooleanBuilder: 조건별 동적 쿼리 조립</li>
 *   <li>offset/limit: DB 레벨 페이징 (Stream skip/limit 대체)</li>
 *   <li>COUNT 쿼리: 동일한 필터 조건 적용</li>
 *   <li>Soft Delete 필터: 모든 쿼리에 자동 적용</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Repository
public interface FileAssetQueryDslRepository {

    /**
     * 동적 쿼리를 사용한 파일 목록 조회
     *
     * <p><strong>지원하는 필터</strong>:</p>
     * <ul>
     *   <li>tenantId (필수)</li>
     *   <li>organizationId (선택)</li>
     *   <li>ownerUserId (선택)</li>
     *   <li>status (선택)</li>
     *   <li>visibility (선택)</li>
     *   <li>uploadedAfter (선택)</li>
     *   <li>uploadedBefore (선택)</li>
     * </ul>
     *
     * <p><strong>페이징</strong>: offset/limit 기반</p>
     * <p><strong>정렬</strong>: uploaded_at DESC (최근 업로드 순)</p>
     *
     * @param query 파일 목록 조회 Query
     * @return FileAssetJpaEntity 목록
     */
    List<FileAssetJpaEntity> findAllByDynamicQuery(ListFilesQuery query);

    /**
     * 동적 쿼리를 사용한 파일 개수 조회
     *
     * <p>findAllByDynamicQuery()와 동일한 필터 조건 적용</p>
     *
     * @param query 파일 목록 조회 Query
     * @return 전체 개수
     */
    long countByDynamicQuery(ListFilesQuery query);
}
