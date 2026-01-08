package com.ryuqq.fileflow.adapter.in.rest.asset.dto.command;

import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * FileAsset 일괄 삭제 API Request.
 *
 * @param fileAssetIds 삭제할 파일 자산 ID 목록 (최대 100개)
 * @param reason 삭제 사유 (선택적)
 */
@Schema(description = "파일 자산 일괄 삭제 요청")
public record BatchDeleteFileAssetApiRequest(
        @Schema(description = "삭제할 파일 자산 ID 목록 (최대 100개)",
                example = "[\"asset-123\", \"asset-456\"]") @NotEmpty(
                        message = "삭제할 파일 ID 목록이 필요합니다") @Size(max = 100,
                                message = "최대 100개까지 삭제할 수 있습니다") List<String> fileAssetIds,
        @Schema(description = "삭제 사유", example = "더 이상 필요하지 않음") String reason) {
    /**
     * 생성자에서 방어적 복사 수행.
     */
    public BatchDeleteFileAssetApiRequest {
        if (fileAssetIds != null) {
            fileAssetIds = new ArrayList<>(fileAssetIds);
        }
    }
}
