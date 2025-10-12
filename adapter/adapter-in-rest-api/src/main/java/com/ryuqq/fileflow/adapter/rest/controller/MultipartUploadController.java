package com.ryuqq.fileflow.adapter.rest.controller;

import com.ryuqq.fileflow.application.upload.port.in.CompletePartUploadUseCase;
import com.ryuqq.fileflow.application.upload.port.in.CompletePartUploadUseCase.CompletePartCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * Multipart Upload REST Controller
 *
 * 멀티파트 업로드 진행 상태 추적 REST API를 제공합니다.
 * Hexagonal Architecture의 Inbound Adapter로서 동작합니다.
 *
 * 제약사항:
 * - NO Lombok
 * - UseCase만 의존
 * - NO Inner Class
 *
 * @author sangwon-ryu
 */
@Tag(name = "Multipart Upload", description = "멀티파트 업로드 관리 API")
@RestController
@RequestMapping("/api/v1/upload/sessions")
public class MultipartUploadController {

    private final CompletePartUploadUseCase completePartUploadUseCase;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param completePartUploadUseCase 파트 완료 UseCase
     */
    public MultipartUploadController(CompletePartUploadUseCase completePartUploadUseCase) {
        this.completePartUploadUseCase = Objects.requireNonNull(
                completePartUploadUseCase,
                "CompletePartUploadUseCase must not be null"
        );
    }

    /**
     * POST /api/v1/upload/sessions/{sessionId}/parts/{partNumber}/complete
     * 멀티파트 업로드의 개별 파트 완료를 기록합니다.
     *
     * 비즈니스 플로우:
     * 1. 세션 조회 및 검증
     * 2. 멀티파트 업로드 여부 확인
     * 3. 파트 번호 유효성 검증
     * 4. Redis에 파트 완료 상태 기록
     *
     * @param sessionId 세션 ID
     * @param partNumber 완료된 파트 번호 (1-based)
     * @return 204 NO CONTENT
     * @throws IllegalArgumentException sessionId가 null이거나 partNumber가 유효하지 않은 경우
     * @throws com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException 세션을 찾을 수 없는 경우
     * @throws IllegalStateException 멀티파트 업로드가 아니거나 유효하지 않은 파트 번호인 경우
     */
    @Operation(
            summary = "멀티파트 파트 완료 기록",
            description = """
                    멀티파트 업로드의 개별 파트 완료를 Redis에 기록합니다.

                    **비즈니스 플로우:**
                    1. 세션 조회 및 검증
                    2. 멀티파트 업로드 여부 확인
                    3. 파트 번호 유효성 검증 (1 ~ totalParts)
                    4. Redis에 파트 완료 상태 기록

                    **사용 시나리오:**
                    - 클라이언트가 S3에 파트 업로드 완료 후 호출
                    - 진행률 추적을 위해 사용
                    - 파트 번호는 1부터 시작 (1-based index)
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "파트 완료 기록 성공 (응답 본문 없음)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "세션을 찾을 수 없음",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (멀티파트 업로드가 아니거나 유효하지 않은 파트 번호)",
                    content = @Content
            )
    })
    @PostMapping("/{sessionId}/parts/{partNumber}/complete")
    public ResponseEntity<Void> completePartUpload(
            @Parameter(description = "업로드 세션 ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String sessionId,
            @Parameter(description = "완료된 파트 번호 (1부터 시작)", example = "1")
            @PathVariable int partNumber
    ) {
        CompletePartCommand command = new CompletePartCommand(sessionId, partNumber);
        completePartUploadUseCase.completePart(command);

        return ResponseEntity.noContent().build();
    }
}
