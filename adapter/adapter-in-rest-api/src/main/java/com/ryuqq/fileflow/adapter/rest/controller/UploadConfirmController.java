package com.ryuqq.fileflow.adapter.rest.controller;

import com.ryuqq.fileflow.adapter.rest.dto.request.ConfirmUploadRequest;
import com.ryuqq.fileflow.adapter.rest.dto.response.ConfirmUploadApiResponse;
import com.ryuqq.fileflow.adapter.rest.dto.response.ErrorResponse;
import com.ryuqq.fileflow.application.upload.dto.ConfirmUploadCommand;
import com.ryuqq.fileflow.application.upload.dto.ConfirmUploadResponse;
import com.ryuqq.fileflow.application.upload.port.in.ConfirmUploadUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * Upload Confirmation REST Controller
 *
 * 업로드 완료 확인 REST API를 제공합니다.
 * Hexagonal Architecture의 Inbound Adapter로서 동작합니다.
 *
 * 제약사항:
 * - NO Lombok
 * - UseCase만 의존
 * - NO Inner Class
 *
 * @author sangwon-ryu
 */
@Tag(name = "Upload Confirmation", description = "업로드 완료 확인 API")
@RestController
@RequestMapping("/api/v1/upload/sessions")
public class UploadConfirmController {

    private final ConfirmUploadUseCase confirmUploadUseCase;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param confirmUploadUseCase 업로드 완료 확인 UseCase
     */
    public UploadConfirmController(ConfirmUploadUseCase confirmUploadUseCase) {
        this.confirmUploadUseCase = Objects.requireNonNull(
                confirmUploadUseCase,
                "ConfirmUploadUseCase must not be null"
        );
    }

    /**
     * POST /api/v1/upload/sessions/{sessionId}/confirm
     * 클라이언트가 S3 업로드 완료 후 서버에 알리는 API입니다.
     *
     * 비즈니스 플로우:
     * 1. 세션 조회 및 검증
     * 2. S3에 파일 존재 확인
     * 3. ETag 검증 (제공된 경우)
     * 4. 세션 상태를 COMPLETED로 업데이트
     * 5. 멱등성 보장 (이미 완료된 경우 200 OK 반환)
     *
     * 듀얼 세이프티 넷:
     * - 클라이언트 확인 API (빠른 응답: 1-2초)
     * - S3 Event 자동 처리 (백업: 5-20초 지연)
     *
     * @param sessionId 세션 ID
     * @param request 업로드 확인 요청
     * @return 200 OK with ConfirmUploadApiResponse
     * @throws IllegalArgumentException sessionId나 request가 null이거나 유효하지 않은 경우
     * @throws com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException 세션을 찾을 수 없는 경우
     * @throws com.ryuqq.fileflow.domain.upload.exception.FileNotFoundInS3Exception S3에 파일이 없는 경우
     * @throws com.ryuqq.fileflow.domain.upload.exception.ChecksumMismatchException ETag 불일치 시
     */
    @Operation(
            summary = "업로드 완료 확인",
            description = """
                    클라이언트가 S3에 파일 업로드를 완료한 후 서버에 알립니다.

                    **비즈니스 플로우:**
                    1. 세션 조회 및 상태 검증
                    2. S3에 파일 존재 확인
                    3. ETag 검증 (제공된 경우)
                    4. 세션 상태를 COMPLETED로 업데이트

                    **듀얼 세이프티 넷:**
                    - ✅ 클라이언트 확인 API: 빠른 응답 (1-2초)
                    - 🔄 S3 Event 자동 처리: 백업 (5-20초 지연)

                    **멱등성:**
                    - 이미 완료된 세션에 재호출 시 200 OK 반환
                    - S3 Event 처리 후 호출해도 안전

                    **사용 시나리오:**
                    1. 클라이언트가 Presigned URL로 S3 업로드 완료
                    2. 즉시 이 API 호출하여 빠른 응답 확인
                    3. S3 Event는 백그라운드에서 자동 처리 (백업)
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "업로드 완료 확인 성공 (신규 확인 또는 이미 완료된 세션)",
                    content = @Content(schema = @Schema(implementation = ConfirmUploadApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (ETag 불일치 등)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "세션을 찾을 수 없음 또는 S3에 파일 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/{sessionId}/confirm")
    public ResponseEntity<ConfirmUploadApiResponse> confirmUpload(
            @Parameter(description = "업로드 세션 ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String sessionId,
            @Valid @RequestBody ConfirmUploadRequest request
    ) {
        Objects.requireNonNull(sessionId, "SessionId must not be null");
        Objects.requireNonNull(request, "ConfirmUploadRequest must not be null");

        ConfirmUploadCommand command = request.toCommand(sessionId);
        ConfirmUploadResponse response = confirmUploadUseCase.confirm(command);

        return ResponseEntity
                .ok()
                .body(ConfirmUploadApiResponse.from(response));
    }
}
