package com.ryuqq.fileflow.adapter.in.rest.session.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Multipart 파일 업로드 세션 초기화 API Request.
 *
 * <p>대용량 파일을 Part 단위로 나누어 업로드하기 위한 세션 생성 요청 정보를 담습니다.
 *
 * @param fileName 파일명 (확장자 포함)
 * @param fileSize 파일 크기 (바이트)
 * @param contentType Content-Type (MIME 타입)
 * @param partSize 각 Part 크기 (바이트, 기본: 5MB)
 * @param tenantId 테넌트 ID
 * @param organizationId 조직 ID
 * @param userId 사용자 ID (Customer 전용, null 가능)
 * @param userEmail 사용자 이메일 (Admin/Seller 전용, null 가능)
 * @author development-team
 * @since 1.0.0
 */
public record InitMultipartUploadApiRequest(
        @NotBlank(message = "파일명은 필수입니다") String fileName,
        @Positive(message = "파일 크기는 양수여야 합니다") long fileSize,
        @NotBlank(message = "Content-Type은 필수입니다") String contentType,
        @Positive(message = "Part 크기는 양수여야 합니다") long partSize,
        @NotNull(message = "테넌트 ID는 필수입니다") @Positive(message = "테넌트 ID는 양수여야 합니다") Long tenantId,
        @NotNull(message = "조직 ID는 필수입니다") @Positive(message = "조직 ID는 양수여야 합니다")
                Long organizationId,
        Long userId,
        String userEmail) {}
