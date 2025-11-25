package com.ryuqq.fileflow.application.asset.port.out.command;

import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;

/** FileAsset 영속화 포트. */
public interface PersistFileAssetPort {

    /**
     * FileAsset을 저장합니다.
     *
     * @param fileAsset 저장할 FileAsset
     */
    FileAssetId persist(FileAsset fileAsset);
}
