package com.ryuqq.fileflow.adapter.in.rest.asset.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Presigned Download URL 생성 요청 DTO.
 *
 * <p>S3 Presigned URL을 생성하기 위한 요청 정보를 담습니다.
 *
 * @param expirationMinutes URL 유효 기간 (분 단위, 기본값 60분, 최대 1440분/24시간)
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "다운로드 URL 생성 요청")
public record GenerateDownloadUrlApiRequest(
        @Schema(description = "URL 유효 기간 (분 단위, 기본값 60분, 최대 1440분)", example = "60")
                @Min(value = 1, message = "유효 기간은 최소 1분 이상이어야 합니다")
                @Max(value = 1440, message = "유효 기간은 최대 1440분(24시간)까지 설정 가능합니다")
                Integer expirationMinutes) {

    /**
     * 기본값 적용 생성자.
     *
     * @param expirationMinutes URL 유효 기간 (null인 경우 60분 기본값)
     */
    public GenerateDownloadUrlApiRequest {
        expirationMinutes = expirationMinutes == null ? 60 : expirationMinutes;
    }

    /**
     * 빈 요청 생성 (기본값 사용).
     *
     * @return 기본 유효 기간(60분)을 가진 요청
     */
    public static GenerateDownloadUrlApiRequest withDefaults() {
        return new GenerateDownloadUrlApiRequest(null);
    }
}
