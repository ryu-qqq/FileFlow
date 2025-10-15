package com.ryuqq.fileflow.adapter.rest.dto.response;

import com.ryuqq.fileflow.application.file.port.in.GetFileThumbnailsUseCase.ThumbnailResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 썸네일 목록 API 응답 DTO
 *
 * @author sangwon-ryu
 */
@Schema(description = "썸네일 목록 응답")
public record ThumbnailListApiResponse(
        @Schema(description = "썸네일 목록", example = "[...]")
        List<ThumbnailApiResponse> thumbnails,

        @Schema(description = "썸네일 개수", example = "2")
        int totalCount
) {
    /**
     * UseCase 응답으로부터 API 응답을 생성합니다.
     *
     * @param thumbnails UseCase 썸네일 응답 목록
     * @return API 응답
     */
    public static ThumbnailListApiResponse of(List<ThumbnailResponse> thumbnails) {
        List<ThumbnailApiResponse> apiResponses = thumbnails.stream()
                .map(ThumbnailApiResponse::from)
                .collect(Collectors.toList());

        return new ThumbnailListApiResponse(apiResponses, apiResponses.size());
    }

    /**
     * 개별 썸네일 API 응답
     */
    @Schema(description = "썸네일 정보")
    public record ThumbnailApiResponse(
            @Schema(description = "관계 ID", example = "1")
            Long relationshipId,

            @Schema(description = "썸네일 파일 ID", example = "file_thumbnail_123")
            String targetFileId,

            @Schema(description = "썸네일 타입", example = "thumbnail_small")
            String relationshipType,

            @Schema(description = "너비", example = "300")
            int width,

            @Schema(description = "높이", example = "300")
            int height,

            @Schema(description = "리샘플링 알고리즘", example = "Lanczos3")
            String algorithm,

            @Schema(description = "생성 시간", example = "2025-10-14T10:30:00")
            String createdAt
    ) {
        /**
         * UseCase 응답으로부터 API 응답을 생성합니다.
         *
         * @param response UseCase 썸네일 응답
         * @return API 응답
         */
        public static ThumbnailApiResponse from(ThumbnailResponse response) {
            return new ThumbnailApiResponse(
                    response.relationshipId(),
                    response.targetFileId().value(),
                    response.relationshipType(),
                    response.width(),
                    response.height(),
                    response.algorithm(),
                    response.createdAt()
            );
        }
    }
}
