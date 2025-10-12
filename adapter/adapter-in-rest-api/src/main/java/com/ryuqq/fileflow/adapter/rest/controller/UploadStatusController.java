package com.ryuqq.fileflow.adapter.rest.controller;

import com.ryuqq.fileflow.adapter.rest.dto.response.UploadStatusApiResponse;
import com.ryuqq.fileflow.application.upload.dto.UploadStatusResponse;
import com.ryuqq.fileflow.application.upload.port.in.GetUploadStatusUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * Upload Status REST Controller
 *
 * 업로드 진행률 조회 REST API를 제공합니다.
 * Hexagonal Architecture의 Inbound Adapter로서 동작합니다.
 *
 * 제약사항:
 * - NO Lombok
 * - UseCase만 의존
 * - NO Inner Class
 *
 * @author sangwon-ryu
 */
@Tag(name = "Upload Status", description = "업로드 상태 및 진행률 조회 API")
@RestController
@RequestMapping("/api/v1/upload/sessions")
public class UploadStatusController {

    private final GetUploadStatusUseCase getUploadStatusUseCase;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param getUploadStatusUseCase 업로드 진행률 조회 UseCase
     */
    public UploadStatusController(GetUploadStatusUseCase getUploadStatusUseCase) {
        this.getUploadStatusUseCase = Objects.requireNonNull(
                getUploadStatusUseCase,
                "GetUploadStatusUseCase must not be null"
        );
    }

    /**
     * GET /api/v1/upload/sessions/{sessionId}/status
     * 업로드 세션의 진행 상태를 조회합니다.
     *
     * @param sessionId 세션 ID
     * @return 200 OK with UploadStatusApiResponse
     * @throws IllegalArgumentException sessionId가 null이거나 빈 문자열인 경우
     * @throws com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException 세션을 찾을 수 없는 경우
     */
    @Operation(
            summary = "업로드 상태 조회",
            description = """
                    업로드 세션의 현재 진행 상태를 조회합니다.

                    **반환 정보:**
                    - 업로드 상태 (PENDING, IN_PROGRESS, COMPLETED, FAILED)
                    - 진행률 (%)
                    - 완료된 파트 수 / 전체 파트 수 (멀티파트 업로드인 경우)
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "업로드 상태 조회 성공",
                    content = @Content(schema = @Schema(implementation = UploadStatusApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "세션을 찾을 수 없음",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 세션 ID",
                    content = @Content
            )
    })
    @GetMapping("/{sessionId}/status")
    public ResponseEntity<UploadStatusApiResponse> getUploadStatus(
            @Parameter(description = "업로드 세션 ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String sessionId
    ) {
        UploadStatusResponse response = getUploadStatusUseCase.getUploadStatus(sessionId);
        return ResponseEntity.ok(UploadStatusApiResponse.from(response));
    }
}
