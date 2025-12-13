package com.ryuqq.fileflow.domain.asset.service;

import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAssetStatusHistory;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;

/**
 * FileAsset 상태 변경 결과.
 *
 * <p>FileAssetUpdateService에서 생성한 상태 변경 결과를 담는 객체입니다.
 *
 * <p><strong>DDD 원칙</strong>:
 *
 * <ul>
 *   <li>상태 변경과 StatusHistory는 항상 함께 (도메인 불변식)
 *   <li>Application Layer(Facade)는 결과를 받아 영속화만 담당
 * </ul>
 *
 * @param fileAsset 상태 변경된 FileAsset
 * @param statusHistory 상태 변경 이력
 */
public record FileAssetUpdateResult(FileAsset fileAsset, FileAssetStatusHistory statusHistory) {

    public FileAssetUpdateResult {
        if (fileAsset == null) {
            throw new IllegalArgumentException("FileAsset은 null일 수 없습니다.");
        }
        if (statusHistory == null) {
            throw new IllegalArgumentException("StatusHistory는 null일 수 없습니다.");
        }
    }

    /**
     * FileAsset ID를 반환합니다.
     *
     * @return FileAssetId
     */
    public FileAssetId fileAssetId() {
        return fileAsset.getId();
    }
}
