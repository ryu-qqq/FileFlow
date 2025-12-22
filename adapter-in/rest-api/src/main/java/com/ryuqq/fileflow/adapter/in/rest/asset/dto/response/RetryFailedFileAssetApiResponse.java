package com.ryuqq.fileflow.adapter.in.rest.asset.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 실패한 FileAsset 재처리 API Response.
 *
 * @param fileAssetId 파일 자산 ID
 * @param status 새로운 상태
 * @param message 결과 메시지
 */
@Schema(description = "실패한 파일 재처리 응답")
public record RetryFailedFileAssetApiResponse(
        @Schema(description = "파일 자산 ID", example = "019412d5-7c3b-7e1a-b2c4-5d6e7f8a9b0c")
                String fileAssetId,
        @Schema(description = "새로운 상태", example = "PENDING") String status,
        @Schema(description = "결과 메시지", example = "파일 재처리가 요청되었습니다. 잠시 후 처리가 시작됩니다.")
                String message) {

    public static RetryFailedFileAssetApiResponse of(
            String fileAssetId, String status, String message) {
        return new RetryFailedFileAssetApiResponse(fileAssetId, status, message);
    }
}
