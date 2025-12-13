package com.ryuqq.fileflow.application.asset.port.out.query;

import com.ryuqq.fileflow.domain.asset.aggregate.FileAssetStatusHistory;
import java.util.List;
import java.util.Optional;

/**
 * FileAssetStatusHistory Query Port.
 *
 * <p>파일 자산 상태 변경 이력 조회를 위한 출력 포트이다.
 */
public interface FileAssetStatusHistoryQueryPort {

    /**
     * FileAsset ID로 상태 변경 이력 목록을 조회한다.
     *
     * <p>시간순으로 정렬되어 반환된다.
     *
     * @param fileAssetId 파일 자산 ID (문자열)
     * @return 상태 변경 이력 목록
     */
    List<FileAssetStatusHistory> findByFileAssetId(String fileAssetId);

    /**
     * FileAsset ID로 최신 상태 변경 이력을 조회한다.
     *
     * @param fileAssetId 파일 자산 ID (문자열)
     * @return 최신 상태 변경 이력 (없으면 empty)
     */
    Optional<FileAssetStatusHistory> findLatestByFileAssetId(String fileAssetId);

    /**
     * SLA를 초과한 상태 변경 이력 목록을 조회한다.
     *
     * <p>모니터링 및 알림 용도로 사용된다.
     *
     * @param slaMillis SLA 기준 시간 (밀리초)
     * @param limit 최대 조회 개수
     * @return SLA 초과 상태 변경 이력 목록
     */
    List<FileAssetStatusHistory> findExceedingSla(long slaMillis, int limit);
}
