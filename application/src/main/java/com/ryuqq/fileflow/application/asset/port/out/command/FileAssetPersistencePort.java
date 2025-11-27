package com.ryuqq.fileflow.application.asset.port.out.command;

import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;

/**
 * FileAsset 영속화 포트.
 *
 * <p>persist()는 신규/수정을 JPA merge로 통합 처리합니다.
 */
public interface FileAssetPersistencePort {

    /**
     * FileAsset을 저장합니다.
     *
     * <p>신규 저장 및 업데이트(상태 변경, Soft Delete 포함)를 통합 처리합니다. JPA merge 활용.
     *
     * @param fileAsset 저장할 FileAsset
     * @return 저장된 FileAsset ID
     */
    FileAssetId persist(FileAsset fileAsset);
}
