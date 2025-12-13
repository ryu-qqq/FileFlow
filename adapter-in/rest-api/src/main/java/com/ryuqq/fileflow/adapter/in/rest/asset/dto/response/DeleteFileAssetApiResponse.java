package com.ryuqq.fileflow.adapter.in.rest.asset.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * 파일 자산 삭제 API Response.
 *
 * <p>Soft Delete 처리 결과를 반환합니다.
 *
 * @param id 삭제된 파일 자산 ID
 * @param deletedAt 삭제 시각
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "파일 자산 삭제 응답")
public record DeleteFileAssetApiResponse(
        @Schema(description = "삭제된 파일 자산 ID", example = "asset-123") String id,
        @Schema(description = "삭제 시각") Instant deletedAt) {

    /**
     * 값 기반 생성.
     *
     * @param id 파일 자산 ID
     * @param deletedAt 삭제 시각
     * @return DeleteFileAssetApiResponse
     */
    public static DeleteFileAssetApiResponse of(String id, Instant deletedAt) {
        return new DeleteFileAssetApiResponse(id, deletedAt);
    }
}
