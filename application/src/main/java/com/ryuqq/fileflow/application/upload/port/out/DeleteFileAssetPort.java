package com.ryuqq.fileflow.application.upload.port.out;

import com.ryuqq.fileflow.domain.upload.vo.FileId;

/**
 * FileAsset 삭제 Port
 *
 * Hexagonal Architecture의 Outbound Port로서,
 * FileAsset을 영구 저장소에서 삭제하는 역할을 정의합니다.
 *
 * @author sangwon-ryu
 */
public interface DeleteFileAssetPort {

    /**
     * FileId로 FileAsset을 삭제합니다.
     *
     * @param fileId 삭제할 파일 ID
     * @return 삭제 성공 여부
     */
    boolean deleteById(FileId fileId);

    /**
     * 여러 FileId로 FileAsset을 일괄 삭제합니다.
     *
     * @param fileIds 삭제할 파일 ID 목록
     * @return 삭제된 파일 개수
     */
    int deleteByIds(Iterable<FileId> fileIds);
}
