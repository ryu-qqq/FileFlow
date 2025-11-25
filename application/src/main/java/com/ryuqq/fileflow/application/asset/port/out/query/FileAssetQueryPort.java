package com.ryuqq.fileflow.application.asset.port.out.query;

import com.ryuqq.fileflow.application.asset.dto.query.FileAssetSearchCriteria;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import java.util.List;
import java.util.Optional;

/**
 * FileAsset Query Port.
 *
 * <p>FileAsset 조회를 위한 출력 포트입니다.
 */
public interface FileAssetQueryPort {

    /**
     * ID로 FileAsset 조회.
     *
     * @param id 파일 자산 ID (Value Object)
     * @param organizationId 조직 ID
     * @param tenantId 테넌트 ID
     * @return FileAsset Domain (없으면 empty)
     */
    Optional<FileAsset> findById(FileAssetId id, Long organizationId, Long tenantId);

    /**
     * 검색 조건에 맞는 FileAsset 목록 조회.
     *
     * @param criteria 검색 조건
     * @return FileAsset Domain 목록
     */
    List<FileAsset> findByCriteria(FileAssetSearchCriteria criteria);

    /**
     * 검색 조건에 맞는 FileAsset 개수 조회.
     *
     * @param criteria 검색 조건
     * @return 전체 개수
     */
    long countByCriteria(FileAssetSearchCriteria criteria);
}
