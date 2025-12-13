package com.ryuqq.fileflow.adapter.in.rest.session.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 단일 파일 업로드 세션 초기화 API Request.
 *
 * <p>Presigned URL 발급을 위한 세션 생성 요청 정보를 담습니다.
 *
 * @param idempotencyKey 멱등성 키 (클라이언트 제공 UUID)
 * @param fileName 파일명 (확장자 포함)
 * @param fileSize 파일 크기 (바이트)
 * @param contentType Content-Type (MIME 타입)
 * @param tenantId 테넌트 ID
 * @param organizationId 조직 ID
 * @param userId 사용자 ID (Customer 전용, null 가능)
 * @param userEmail 사용자 이메일 (Admin/Seller 전용, null 가능)
 * @param uploadCategory 업로드 카테고리 (Admin/Seller 필수, Customer는 null)
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "단일 업로드 세션 초기화 요청")
public record InitSingleUploadApiRequest(
        @Schema(
                        description = "멱등성 키 (클라이언트 제공 UUID)",
                        example = "550e8400-e29b-41d4-a716-446655440000",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "멱등성 키는 필수입니다")
                String idempotencyKey,
        @Schema(
                        description = "파일명 (확장자 포함)",
                        example = "image.jpg",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "파일명은 필수입니다")
                String fileName,
        @Schema(
                        description = "파일 크기 (bytes)",
                        example = "1024000",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @Positive(message = "파일 크기는 양수여야 합니다")
                long fileSize,
        @Schema(
                        description = "Content-Type (MIME 타입)",
                        example = "image/jpeg",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "Content-Type은 필수입니다")
                String contentType,
        @Schema(description = "테넌트 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "테넌트 ID는 필수입니다")
                @Positive(message = "테넌트 ID는 양수여야 합니다")
                Long tenantId,
        @Schema(description = "조직 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "조직 ID는 필수입니다")
                @Positive(message = "조직 ID는 양수여야 합니다")
                Long organizationId,
        @Schema(description = "사용자 ID (Customer 전용)", example = "12345", nullable = true)
                Long userId,
        @Schema(
                        description = "사용자 이메일 (Admin/Seller 전용)",
                        example = "user@example.com",
                        nullable = true)
                String userEmail,
        @Schema(description = "업로드 카테고리 (Admin/Seller 필수)", example = "PRODUCT", nullable = true)
                String uploadCategory) {}
