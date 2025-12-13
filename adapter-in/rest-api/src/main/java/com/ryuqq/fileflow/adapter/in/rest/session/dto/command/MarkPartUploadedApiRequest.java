package com.ryuqq.fileflow.adapter.in.rest.session.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Part 업로드 완료 표시 API Request.
 *
 * <p>Multipart 업로드의 각 Part 업로드 완료를 세션에 기록합니다.
 *
 * @param partNumber Part 번호 (1-based)
 * @param etag S3가 반환한 Part ETag
 * @param size Part 크기 (바이트)
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "Part 업로드 완료 요청")
public record MarkPartUploadedApiRequest(
        @Schema(
                        description = "Part 번호 (1-based)",
                        example = "1",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @Positive(message = "Part 번호는 양수여야 합니다")
                int partNumber,
        @Schema(
                        description = "S3가 반환한 Part ETag",
                        example = "\"d41d8cd98f00b204e9800998ecf8427e\"",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "ETag는 필수입니다")
                String etag,
        @Schema(
                        description = "Part 크기 (bytes)",
                        example = "5242880",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @Positive(message = "Part 크기는 양수여야 합니다")
                long size) {}
