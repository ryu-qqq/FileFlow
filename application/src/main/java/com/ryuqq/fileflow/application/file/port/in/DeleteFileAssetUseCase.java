package com.ryuqq.fileflow.application.file.port.in;

import com.ryuqq.fileflow.domain.upload.vo.FileId;

/**
 * FileAsset 삭제 UseCase
 *
 * 책임:
 * - FileAsset 삭제
 * - Cascade 삭제: 관련된 FileRelationship (썸네일 등) 자동 삭제
 *
 * @author sangwon-ryu
 */
public interface DeleteFileAssetUseCase {

    /**
     * FileAsset을 삭제하고 관련된 모든 FileRelationship을 cascade 삭제합니다.
     *
     * 삭제 흐름:
     * 1. FileAsset 존재 확인
     * 2. 관련된 모든 FileRelationship 삭제 (원본/대상 모두)
     * 3. FileAsset 삭제
     *
     * @param fileId 삭제할 파일 ID
     * @throws com.ryuqq.fileflow.domain.upload.exception.FileAssetNotFoundException 파일이 존재하지 않을 경우
     */
    void deleteFileAsset(FileId fileId);

    /**
     * 여러 FileAsset을 일괄 삭제합니다.
     *
     * @param fileIds 삭제할 파일 ID 목록
     * @return 성공적으로 삭제된 파일 개수
     */
    int deleteFileAssets(Iterable<FileId> fileIds);

    /**
     * 삭제 결과 DTO
     *
     * @param fileId 삭제된 파일 ID
     * @param deletedRelationshipsCount 삭제된 관계 개수
     */
    record DeleteResult(
            FileId fileId,
            int deletedRelationshipsCount
    ) {
        public static DeleteResult of(FileId fileId, int deletedRelationshipsCount) {
            return new DeleteResult(fileId, deletedRelationshipsCount);
        }
    }
}
