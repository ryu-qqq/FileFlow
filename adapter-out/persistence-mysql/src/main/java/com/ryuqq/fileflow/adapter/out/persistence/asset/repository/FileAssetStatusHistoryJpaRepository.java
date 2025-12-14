package com.ryuqq.fileflow.adapter.out.persistence.asset.repository;

import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.FileAssetStatusHistoryJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * FileAssetStatusHistory JPA Repository.
 */
public interface FileAssetStatusHistoryJpaRepository
        extends JpaRepository<FileAssetStatusHistoryJpaEntity, UUID> {

    /**
     * FileAsset ID로 상태 변경 이력 목록 조회 (시간순 정렬).
     *
     * @param fileAssetId 파일 에셋 ID
     * @return 상태 변경 이력 목록
     */
    List<FileAssetStatusHistoryJpaEntity> findByFileAssetIdOrderByChangedAtAsc(String fileAssetId);

    /**
     * FileAsset ID로 최신 상태 변경 이력 조회.
     *
     * @param fileAssetId 파일 에셋 ID
     * @return 최신 상태 변경 이력
     */
    Optional<FileAssetStatusHistoryJpaEntity> findFirstByFileAssetIdOrderByChangedAtDesc(
            String fileAssetId);

    /**
     * SLA 초과 상태 변경 이력 조회.
     *
     * @param slaMillis SLA 기준 시간 (밀리초)
     * @param limit 최대 조회 개수
     * @return SLA 초과 이력 목록
     */
    @Query(
            value =
                    "SELECT h FROM FileAssetStatusHistoryJpaEntity h "
                            + "WHERE h.durationMillis > :slaMillis "
                            + "ORDER BY h.changedAt DESC "
                            + "LIMIT :limit")
    List<FileAssetStatusHistoryJpaEntity> findExceedingSla(
            @Param("slaMillis") long slaMillis, @Param("limit") int limit);
}
