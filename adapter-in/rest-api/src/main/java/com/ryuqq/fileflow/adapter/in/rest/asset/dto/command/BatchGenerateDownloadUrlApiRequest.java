package com.ryuqq.fileflow.adapter.in.rest.asset.dto.command;

import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Presigned Download URL 일괄 생성 요청 DTO.
 *
 * <p>
 * 여러 파일에 대한 S3 Presigned URL을 일괄 생성하기 위한 요청 정보를 담습니다.
 *
 * @param fileAssetIds 파일 자산 ID 목록 (최대 100개)
 * @param expirationMinutes URL 유효 기간 (분 단위, 기본값 60분, 최대 1440분/24시간)
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "다운로드 URL 일괄 생성 요청")
public record BatchGenerateDownloadUrlApiRequest(
        @Schema(description = "파일 자산 ID 목록 (최대 100개)", example = "[\"asset-1\", \"asset-2\"]",
                requiredMode = Schema.RequiredMode.REQUIRED) @NotEmpty(
                        message = "파일 자산 ID 목록은 필수입니다") @Size(max = 100,
                                message = "한 번에 최대 100개까지 요청 가능합니다") List<String> fileAssetIds,
        @Schema(description = "URL 유효 기간 (분 단위, 기본값 60분, 최대 1440분)", example = "60") @Min(value = 1,
                message = "유효 기간은 최소 1분 이상이어야 합니다") @Max(value = 1440,
                        message = "유효 기간은 최대 1440분(24시간)까지 설정 가능합니다") Integer expirationMinutes) {

    /**
     * 기본값 적용 생성자.
     *
     * @param fileAssetIds 파일 자산 ID 목록
     * @param expirationMinutes URL 유효 기간 (null인 경우 60분 기본값)
     */
    public BatchGenerateDownloadUrlApiRequest {
        if (fileAssetIds != null) {
            fileAssetIds = new ArrayList<>(fileAssetIds);
        }
        expirationMinutes = expirationMinutes == null ? 60 : expirationMinutes;
    }
}
