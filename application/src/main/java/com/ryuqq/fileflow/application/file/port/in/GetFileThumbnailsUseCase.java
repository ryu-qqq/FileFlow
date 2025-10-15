package com.ryuqq.fileflow.application.file.port.in;

import com.ryuqq.fileflow.domain.upload.vo.FileId;

import java.util.List;

/**
 * 파일 썸네일 조회 UseCase
 *
 * 비즈니스 규칙:
 * - 원본 파일의 모든 썸네일 관계를 조회합니다
 * - 썸네일 크기 정보와 메타데이터를 함께 반환합니다
 *
 * @author sangwon-ryu
 */
public interface GetFileThumbnailsUseCase {

    /**
     * 원본 파일의 모든 썸네일을 조회합니다.
     *
     * @param fileId 원본 파일 ID
     * @return 썸네일 응답 목록
     */
    List<ThumbnailResponse> getThumbnails(FileId fileId);

    /**
     * 썸네일 응답 DTO
     */
    record ThumbnailResponse(
            Long relationshipId,
            FileId targetFileId,
            String relationshipType,
            int width,
            int height,
            String algorithm,
            String createdAt
    ) {}
}
