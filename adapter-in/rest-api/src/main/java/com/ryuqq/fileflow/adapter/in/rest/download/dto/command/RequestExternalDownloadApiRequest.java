package com.ryuqq.fileflow.adapter.in.rest.download.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 외부 다운로드 요청 API Request.
 *
 * @param sourceUrl 다운로드할 외부 이미지 URL
 * @param webhookUrl 콜백 웹훅 URL (선택)
 */
public record RequestExternalDownloadApiRequest(
        @NotBlank(message = "sourceUrl은 필수입니다")
                @Size(max = 2048, message = "sourceUrl은 2048자를 초과할 수 없습니다")
                String sourceUrl,
        @Size(max = 2048, message = "webhookUrl은 2048자를 초과할 수 없습니다") String webhookUrl) {}
