package com.ryuqq.fileflow.adapter.in.rest.session.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Schema(description = "멀티파트 업로드 세션 완료 요청")
public record CompleteMultipartUploadSessionApiRequest(
        @Schema(description = "전체 파일 크기 (bytes)", example = "52428800") @Positive
                long totalFileSize,
        @Schema(
                        description = "S3 CompleteMultipartUpload ETag",
                        example = "\"d41d8cd98f00b204e9800998ecf8427e-10\"")
                @NotBlank
                String etag) {}
