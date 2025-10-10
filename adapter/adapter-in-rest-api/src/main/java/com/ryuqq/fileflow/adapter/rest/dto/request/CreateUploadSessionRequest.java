package com.ryuqq.fileflow.adapter.rest.dto.request;

import com.ryuqq.fileflow.application.upload.dto.CreateUploadSessionCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Upload Session 생성 요청 DTO
 *
 * Presigned URL 발급을 위한 업로드 세션 생성 요청을 전달하는 DTO입니다.
 *
 * @param policyKey 정책 키 (형식: {tenantId}:{userType}:{serviceType})
 * @param fileName 업로드할 파일명
 * @param fileSize 파일 크기 (bytes)
 * @param contentType 파일 Content-Type
 * @param uploaderId 업로더 ID
 * @param expirationMinutes 세션 만료 시간 (분, 기본값: 30)
 * @param idempotencyKey 멱등성 키 (선택적)
 * @author sangwon-ryu
 */
public record CreateUploadSessionRequest(
        @NotBlank(message = "PolicyKey cannot be blank")
        String policyKey,

        @NotBlank(message = "FileName cannot be blank")
        String fileName,

        @Positive(message = "FileSize must be positive")
        long fileSize,

        @NotBlank(message = "ContentType cannot be blank")
        String contentType,

        @NotBlank(message = "UploaderId cannot be blank")
        String uploaderId,

        @Min(value = 1, message = "ExpirationMinutes must be at least 1")
        Integer expirationMinutes,

        String idempotencyKey
) {
    private static final int DEFAULT_EXPIRATION_MINUTES = 30;

    /**
     * Compact constructor로 기본값 처리
     */
    public CreateUploadSessionRequest {
        if (expirationMinutes == null) {
            expirationMinutes = DEFAULT_EXPIRATION_MINUTES;
        }
    }

    /**
     * REST Request를 Application Command로 변환합니다.
     *
     * @return CreateUploadSessionCommand
     */
    public CreateUploadSessionCommand toCommand() {
        return new CreateUploadSessionCommand(
                policyKey,
                fileName,
                fileSize,
                contentType,
                uploaderId,
                expirationMinutes,
                idempotencyKey
        );
    }
}
