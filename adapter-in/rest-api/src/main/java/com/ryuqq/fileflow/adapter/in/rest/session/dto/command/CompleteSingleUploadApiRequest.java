package com.ryuqq.fileflow.adapter.in.rest.session.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 단일 파일 업로드 완료 API Request.
 *
 * <p>S3에 업로드 완료 후 세션 완료 처리를 요청합니다.
 *
 * @param etag S3가 반환한 ETag
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "단일 업로드 완료 요청")
public record CompleteSingleUploadApiRequest(
        @Schema(description = "S3가 반환한 ETag", example = "\"d41d8cd98f00b204e9800998ecf8427e\"", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "ETag는 필수입니다") String etag) {}
