package com.ryuqq.fileflow.application.asset.port.out.query;

import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetCriteria;
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
     * @param organizationId 조직 ID (UUIDv7 문자열)
     * @param tenantId 테넌트 ID (UUIDv7 문자열)
     * @return FileAsset Domain (없으면 empty)
     */
    Optional<FileAsset> findById(FileAssetId id, String organizationId, String tenantId);

    /**
     * ID만으로 FileAsset 조회 (DLQ 처리용).
     *
     * <p>DLQ 처리 시 organizationId/tenantId 없이 조회가 필요한 경우 사용합니다.
     *
     * @param id 파일 자산 ID (Value Object)
     * @return FileAsset Domain (없으면 empty)
     */
    Optional<FileAsset> findById(FileAssetId id);

    /**
     * 검색 조건에 맞는 FileAsset 목록 조회.
     *
     * @param criteria 검색 조건 (Domain VO)
     * @return FileAsset Domain 목록
     */
    List<FileAsset> findByCriteria(FileAssetCriteria criteria);

    /**
     * 검색 조건에 맞는 FileAsset 개수 조회.
     *
     * @param criteria 검색 조건 (Domain VO)
     * @return 전체 개수
     */
    long countByCriteria(FileAssetCriteria criteria);
}
