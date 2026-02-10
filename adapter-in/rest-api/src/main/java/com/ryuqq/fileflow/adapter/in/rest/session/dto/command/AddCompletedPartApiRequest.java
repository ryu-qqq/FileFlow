package com.ryuqq.fileflow.adapter.in.rest.session.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Schema(description = "파트 업로드 완료 기록 요청")
public record AddCompletedPartApiRequest(
        @Schema(description = "파트 번호", example = "1") @Positive int partNumber,
        @Schema(description = "파트 ETag", example = "\"d41d8cd98f00b204e9800998ecf8427e\"") @NotBlank
                String etag,
        @Schema(description = "파트 크기 (bytes)", example = "5242880") @Positive long size) {}
