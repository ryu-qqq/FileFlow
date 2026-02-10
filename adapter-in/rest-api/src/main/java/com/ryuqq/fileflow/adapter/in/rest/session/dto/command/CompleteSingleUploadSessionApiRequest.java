package com.ryuqq.fileflow.adapter.in.rest.session.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Schema(description = "단건 업로드 세션 완료 요청")
public record CompleteSingleUploadSessionApiRequest(
        @Schema(description = "파일 크기 (bytes)", example = "1048576") @Positive long fileSize,
        @Schema(description = "S3 ETag", example = "\"d41d8cd98f00b204e9800998ecf8427e\"") @NotBlank
                String etag) {}
