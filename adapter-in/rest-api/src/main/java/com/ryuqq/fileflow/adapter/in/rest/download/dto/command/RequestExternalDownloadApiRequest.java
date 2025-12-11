package com.ryuqq.fileflow.adapter.in.rest.download.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 외부 다운로드 요청 API Request.
 *
 * @param sourceUrl 다운로드할 외부 이미지 URL
 * @param webhookUrl 콜백 웹훅 URL (선택)
 */
@Schema(description = "외부 다운로드 요청")
public record RequestExternalDownloadApiRequest(
        @Schema(description = "다운로드할 외부 이미지 URL", example = "https://example.com/image.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "sourceUrl은 필수입니다")
                @Size(max = 2048, message = "sourceUrl은 2048자를 초과할 수 없습니다")
                String sourceUrl,
        @Schema(description = "콜백 웹훅 URL", example = "https://myservice.com/webhook", nullable = true)
        @Size(max = 2048, message = "webhookUrl은 2048자를 초과할 수 없습니다") String webhookUrl) {}
