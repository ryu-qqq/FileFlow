package com.ryuqq.fileflow.adapter.rest.dto.request;

import com.ryuqq.fileflow.application.upload.dto.ConfirmUploadCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 업로드 완료 확인 요청 DTO
 *
 * 클라이언트가 S3에 파일 업로드를 완료한 후 서버에 알리기 위한 요청 객체입니다.
 * Hexagonal Architecture의 Inbound Adapter로서 동작합니다.
 *
 * 제약사항:
 * - NO Lombok
 * - Immutable Record 사용
 *
 * @author sangwon-ryu
 */
@Schema(description = "업로드 완료 확인 요청")
public record ConfirmUploadRequest(

        @Schema(
                description = "S3 ETag (선택적 - 제공 시 검증 수행)",
                example = "\"d41d8cd98f00b204e9800998ecf8427e\"",
                nullable = true
        )
        String etag

) {
    /**
     * Command 객체로 변환합니다.
     *
     * @param sessionId 세션 ID (PathVariable에서 전달됨)
     * @return ConfirmUploadCommand
     */
    public ConfirmUploadCommand toCommand(String sessionId) {
        if (etag != null && !etag.trim().isEmpty()) {
            return ConfirmUploadCommand.withEtag(sessionId, etag);
        }
        return ConfirmUploadCommand.withoutEtag(sessionId);
    }
}
