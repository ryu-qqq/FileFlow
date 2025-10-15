package com.ryuqq.fileflow.application.file.port.out;

import com.ryuqq.fileflow.domain.upload.vo.FileId;

/**
 * FileRelationship 삭제 Port
 *
 * Hexagonal Architecture의 Outbound Port로서,
 * FileRelationship을 영구 저장소에서 삭제하는 역할을 정의합니다.
 *
 * 사용 사례:
 * - 원본 파일 삭제 시 관련 관계 Cascade 삭제
 * - 썸네일 파일 삭제 시 관계 정리
 * - 파일 관계 정리 작업
 *
 * @author sangwon-ryu
 */
public interface DeleteFileRelationshipPort {

    /**
     * 파일 ID와 관련된 모든 관계를 삭제합니다.
     * 원본 또는 대상 파일로 포함된 모든 관계가 삭제됩니다.
     *
     * Cascade 삭제 규칙:
     * - 원본 파일 삭제: 모든 파생 파일 관계 삭제
     * - 대상 파일 삭제: 해당 관계만 삭제
     *
     * @param fileId 삭제할 파일 ID
     * @return 삭제된 관계 개수
     */
    int deleteByFileId(FileId fileId);

    /**
     * 원본 파일 ID로 모든 관계를 삭제합니다.
     * 원본 파일이 삭제될 때 모든 파생 관계를 정리합니다.
     *
     * @param sourceFileId 원본 파일 ID
     * @return 삭제된 관계 개수
     */
    int deleteBySourceFileId(FileId sourceFileId);

    /**
     * 대상 파일 ID로 관계를 삭제합니다.
     * 썸네일이나 변환 파일이 삭제될 때 해당 관계를 정리합니다.
     *
     * @param targetFileId 대상 파일 ID
     * @return 삭제된 관계 개수
     */
    int deleteByTargetFileId(FileId targetFileId);
}
