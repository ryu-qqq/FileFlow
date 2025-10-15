package com.ryuqq.fileflow.application.file.port.in;

import com.ryuqq.fileflow.domain.file.FileRelationship;
import com.ryuqq.fileflow.domain.file.FileRelationshipType;
import com.ryuqq.fileflow.domain.upload.vo.FileId;

import java.util.Map;

/**
 * 파일 관계 생성 UseCase
 *
 * 비즈니스 규칙:
 * - 원본 파일과 대상 파일 간의 관계를 생성합니다
 * - 관계 유형과 메타데이터를 함께 저장합니다
 * - 썸네일, 최적화, 변환 등 다양한 관계 타입을 지원합니다
 *
 * @author sangwon-ryu
 */
public interface CreateFileRelationshipUseCase {

    /**
     * 파일 관계를 생성합니다.
     *
     * @param command 파일 관계 생성 커맨드
     * @return 생성된 FileRelationship (ID 포함)
     */
    FileRelationship createRelationship(CreateRelationshipCommand command);

    /**
     * 파일 관계 생성 커맨드
     */
    record CreateRelationshipCommand(
            FileId sourceFileId,
            FileId targetFileId,
            FileRelationshipType relationshipType,
            Map<String, Object> metadata
    ) {
        public CreateRelationshipCommand {
            if (sourceFileId == null) {
                throw new IllegalArgumentException("Source file ID cannot be null");
            }
            if (targetFileId == null) {
                throw new IllegalArgumentException("Target file ID cannot be null");
            }
            if (relationshipType == null) {
                throw new IllegalArgumentException("Relationship type cannot be null");
            }
            if (metadata == null) {
                throw new IllegalArgumentException("Metadata cannot be null");
            }
        }
    }
}
