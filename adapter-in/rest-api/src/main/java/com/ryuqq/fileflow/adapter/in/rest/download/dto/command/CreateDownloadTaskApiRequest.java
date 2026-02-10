package com.ryuqq.fileflow.adapter.in.rest.download.dto.command;

import com.ryuqq.fileflow.domain.common.vo.AccessType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "다운로드 작업 생성 요청")
public record CreateDownloadTaskApiRequest(
        @Schema(description = "다운로드 소스 URL", example = "https://example.com/files/image.jpg")
                @NotBlank
                String sourceUrl,
        @Schema(description = "저장할 S3 객체 키", example = "downloads/2026/02/image.jpg") @NotBlank
                String s3Key,
        @Schema(description = "S3 버킷명", example = "fileflow-bucket") @NotBlank String bucket,
        @Schema(description = "접근 유형", example = "PUBLIC") @NotNull AccessType accessType,
        @Schema(description = "파일 용도", example = "PRODUCT_IMAGE") @NotBlank String purpose,
        @Schema(description = "요청 서비스명", example = "commerce-api") @NotBlank String source,
        @Schema(
                        description = "완료 콜백 URL",
                        example = "https://commerce-api.internal/callbacks/download")
                String callbackUrl) {}
