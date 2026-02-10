package com.ryuqq.fileflow.adapter.in.rest.session.dto.command;

import com.ryuqq.fileflow.domain.common.vo.AccessType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "단건 업로드 세션 생성 요청")
public record CreateSingleUploadSessionApiRequest(
        @Schema(description = "원본 파일명", example = "product-image.jpg") @NotBlank String fileName,
        @Schema(description = "MIME 타입", example = "image/jpeg") @NotBlank String contentType,
        @Schema(description = "접근 유형", example = "PUBLIC") @NotNull AccessType accessType,
        @Schema(description = "파일 용도", example = "PRODUCT_IMAGE") @NotBlank String purpose,
        @Schema(description = "요청 서비스명", example = "commerce-api") @NotBlank String source) {}
