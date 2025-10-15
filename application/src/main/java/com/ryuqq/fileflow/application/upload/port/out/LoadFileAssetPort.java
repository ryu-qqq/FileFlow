package com.ryuqq.fileflow.application.upload.port.out;

import com.ryuqq.fileflow.domain.upload.vo.FileAsset;
import com.ryuqq.fileflow.domain.upload.vo.FileId;

import java.util.Optional;

/**
 * FileAsset 로드 Port
 *
 * Hexagonal Architecture의 Outbound Port로서,
 * FileAsset을 영구 저장소에서 조회하는 역할을 정의합니다.
 *
 * @author sangwon-ryu
 */
public interface LoadFileAssetPort {

    /**
     * FileId로 FileAsset을 조회합니다.
     *
     * @param fileId 조회할 파일 ID
     * @return FileAsset (Optional)
     */
    Optional<FileAsset> findById(FileId fileId);

    /**
     * FileId로 FileAsset을 조회하며, 존재하지 않으면 예외를 발생시킵니다.
     *
     * @param fileId 조회할 파일 ID
     * @return FileAsset
     * @throws com.ryuqq.fileflow.domain.upload.exception.FileAssetNotFoundException 파일이 존재하지 않을 경우
     */
    FileAsset getById(FileId fileId);
}
