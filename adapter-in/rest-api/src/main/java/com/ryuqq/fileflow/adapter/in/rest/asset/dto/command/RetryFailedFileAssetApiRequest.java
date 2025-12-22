package com.ryuqq.fileflow.adapter.in.rest.asset.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 실패한 FileAsset 재처리 API Request.
 *
 * @param reason 재처리 사유 (선택)
 */
@Schema(description = "실패한 파일 재처리 요청")
public record RetryFailedFileAssetApiRequest(
        @Schema(description = "재처리 사유", example = "일시적 오류로 인한 재처리", required = false)
                String reason) {

    public RetryFailedFileAssetApiRequest {
        if (reason == null) {
            reason = "";
        }
    }
}
