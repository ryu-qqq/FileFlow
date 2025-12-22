package com.ryuqq.fileflow.application.asset.dto.response;

/**
 * 실패한 FileAsset 재처리 응답.
 *
 * @param fileAssetId 재처리 요청된 파일 자산 ID
 * @param status 새로운 상태 (PENDING)
 * @param message 결과 메시지
 */
public record RetryFailedFileAssetResponse(String fileAssetId, String status, String message) {

    public static RetryFailedFileAssetResponse of(String fileAssetId, String status) {
        return new RetryFailedFileAssetResponse(
                fileAssetId, status, "파일 재처리가 요청되었습니다. 잠시 후 처리가 시작됩니다.");
    }
}
