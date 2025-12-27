package com.ryuqq.fileflow.adapter.in.rest.download.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 외부 다운로드 요청 API Request.
 *
 * @param idempotencyKey 멱등성 키 (UUID 형식)
 * @param sourceUrl 다운로드할 외부 이미지 URL
 * @param webhookUrl 콜백 웹훅 URL (선택)
 */
@Schema(description = "외부 다운로드 요청")
public record RequestExternalDownloadApiRequest(
        @Schema(
                        description = "멱등성 키 (UUID 형식, 중복 요청 방지용)",
                        example = "550e8400-e29b-41d4-a716-446655440000",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "idempotencyKey는 필수입니다")
                @Pattern(
                        regexp =
                                "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                        message = "idempotencyKey는 UUID 형식이어야 합니다")
                String idempotencyKey,
        @Schema(
                        description = "다운로드할 외부 이미지 URL",
                        example = "https://example.com/image.jpg",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "sourceUrl은 필수입니다")
                @Size(max = 2048, message = "sourceUrl은 2048자를 초과할 수 없습니다")
                String sourceUrl,
        @Schema(
                        description = "콜백 웹훅 URL",
                        example = "https://myservice.com/webhook",
                        nullable = true)
                @Size(max = 2048, message = "webhookUrl은 2048자를 초과할 수 없습니다")
                String webhookUrl) {}
