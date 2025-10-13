package com.ryuqq.fileflow.adapter.rest.dto.response;

import com.ryuqq.fileflow.application.upload.dto.ConfirmUploadResponse;
import com.ryuqq.fileflow.domain.upload.vo.UploadStatus;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 업로드 완료 확인 응답 DTO
 *
 * 업로드 완료 확인 결과를 클라이언트에 전달하기 위한 REST API 응답 객체입니다.
 * Hexagonal Architecture의 Inbound Adapter로서 동작합니다.
 *
 * 제약사항:
 * - NO Lombok
 * - Immutable Record 사용
 *
 * @author sangwon-ryu
 */
@Schema(description = "업로드 완료 확인 응답")
public record ConfirmUploadApiResponse(

        @Schema(description = "세션 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        String sessionId,

        @Schema(description = "업로드 상태", example = "COMPLETED")
        UploadStatus status,

        @Schema(description = "응답 메시지", example = "업로드가 성공적으로 확인되었습니다.")
        String message

) {
    /**
     * Application Layer의 ConfirmUploadResponse로부터 REST API 응답을 생성합니다.
     *
     * @param response ConfirmUploadResponse
     * @return ConfirmUploadApiResponse
     */
    public static ConfirmUploadApiResponse from(ConfirmUploadResponse response) {
        return new ConfirmUploadApiResponse(
                response.sessionId(),
                response.status(),
                response.message()
        );
    }
}
