package com.ryuqq.fileflow.adapter.in.rest.asset.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

/**
 * FileAsset 일괄 삭제 API Response.
 *
 * @param deletedAssets 삭제 성공한 자산 목록
 * @param successCount 성공 건수
 * @param failureCount 실패 건수
 * @param failures 실패 정보 목록
 */
@Schema(description = "파일 자산 일괄 삭제 응답")
public record BatchDeleteFileAssetApiResponse(
        @Schema(description = "삭제된 자산 목록") List<DeletedAsset> deletedAssets,
        @Schema(description = "성공 건수", example = "8") int successCount,
        @Schema(description = "실패 건수", example = "2") int failureCount,
        @Schema(description = "실패 정보 목록") List<FailedDelete> failures) {

    public static BatchDeleteFileAssetApiResponse of(
            List<DeletedAsset> deletedAssets, List<FailedDelete> failures) {
        return new BatchDeleteFileAssetApiResponse(
                deletedAssets, deletedAssets.size(), failures.size(), failures);
    }

    /**
     * 삭제된 자산 정보.
     *
     * @param fileAssetId 파일 자산 ID
     * @param deletedAt 삭제 시각
     */
    @Schema(description = "삭제된 자산 정보")
    public record DeletedAsset(
            @Schema(description = "파일 자산 ID", example = "asset-123") String fileAssetId,
            @Schema(description = "삭제 시각") Instant deletedAt) {

        public static DeletedAsset of(String fileAssetId, Instant deletedAt) {
            return new DeletedAsset(fileAssetId, deletedAt);
        }
    }

    /**
     * 삭제 실패 정보.
     *
     * @param fileAssetId 파일 자산 ID
     * @param errorCode 에러 코드
     * @param errorMessage 에러 메시지
     */
    @Schema(description = "삭제 실패 정보")
    public record FailedDelete(
            @Schema(description = "파일 자산 ID", example = "asset-789") String fileAssetId,
            @Schema(description = "에러 코드", example = "NOT_FOUND") String errorCode,
            @Schema(description = "에러 메시지", example = "FileAsset을 찾을 수 없습니다") String errorMessage) {

        public static FailedDelete of(String fileAssetId, String errorCode, String errorMessage) {
            return new FailedDelete(fileAssetId, errorCode, errorMessage);
        }
    }
}
