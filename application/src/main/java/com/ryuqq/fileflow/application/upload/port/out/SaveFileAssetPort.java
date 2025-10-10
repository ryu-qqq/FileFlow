package com.ryuqq.fileflow.application.upload.port.out;

import com.ryuqq.fileflow.domain.upload.vo.FileAsset;

/**
 * FileAsset 저장 Port
 *
 * Hexagonal Architecture의 Outbound Port로서,
 * FileAsset을 영구 저장소에 저장하는 역할을 정의합니다.
 *
 * @author sangwon-ryu
 */
public interface SaveFileAssetPort {

    /**
     * FileAsset을 저장합니다.
     *
     * @param fileAsset 저장할 파일 자산
     * @return 저장된 FileAsset
     */
    FileAsset save(FileAsset fileAsset);
}
