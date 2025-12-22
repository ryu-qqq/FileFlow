package com.ryuqq.fileflow.application.asset.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * FileAsset 일괄 삭제 Response.
 *
 * <p>여러 파일 자산의 Soft Delete 결과를 반환합니다.
 *
 * @param deletedAssets 삭제 성공한 자산 목록
 * @param successCount 성공 건수
 * @param failureCount 실패 건수
 * @param failures 실패 정보 목록
 */
public record BatchDeleteFileAssetResponse(
        List<DeletedAsset> deletedAssets,
        int successCount,
        int failureCount,
        List<FailedDelete> failures) {

    /**
     * 모든 파일이 성공적으로 삭제된 경우.
     *
     * @param deletedAssets 삭제된 자산 목록
     * @return BatchDeleteFileAssetResponse
     */
    public static BatchDeleteFileAssetResponse ofSuccess(List<DeletedAsset> deletedAssets) {
        return new BatchDeleteFileAssetResponse(deletedAssets, deletedAssets.size(), 0, List.of());
    }

    /**
     * 성공/실패가 혼합된 경우.
     *
     * @param deletedAssets 삭제된 자산 목록
     * @param failures 실패 정보 목록
     * @return BatchDeleteFileAssetResponse
     */
    public static BatchDeleteFileAssetResponse of(
            List<DeletedAsset> deletedAssets, List<FailedDelete> failures) {
        return new BatchDeleteFileAssetResponse(
                deletedAssets, deletedAssets.size(), failures.size(), failures);
    }

    /**
     * 삭제된 자산 정보.
     *
     * @param fileAssetId 파일 자산 ID
     * @param deletedAt 삭제 시각
     */
    public record DeletedAsset(String fileAssetId, Instant deletedAt) {

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
    public record FailedDelete(String fileAssetId, String errorCode, String errorMessage) {

        public static FailedDelete of(String fileAssetId, String errorCode, String errorMessage) {
            return new FailedDelete(fileAssetId, errorCode, errorMessage);
        }
    }
}
