package com.ryuqq.fileflow.adapter.rest.controller;

import com.ryuqq.fileflow.adapter.rest.dto.response.ThumbnailListApiResponse;
import com.ryuqq.fileflow.application.file.port.in.GetFileThumbnailsUseCase;
import com.ryuqq.fileflow.application.file.port.in.GetFileThumbnailsUseCase.ThumbnailResponse;
import com.ryuqq.fileflow.domain.upload.vo.FileId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * File Thumbnail REST API Controller
 *
 * 책임:
 * - 파일 썸네일 조회 API 제공
 * - HTTP 요청/응답 변환
 * - API 문서화 (Swagger/OpenAPI)
 *
 * @author sangwon-ryu
 */
@Tag(name = "File Thumbnails", description = "파일 썸네일 관리 API")
@RestController
@RequestMapping("/api/v1/files")
public class FileThumbnailController {

    private static final Logger log = LoggerFactory.getLogger(FileThumbnailController.class);

    private final GetFileThumbnailsUseCase getFileThumbnailsUseCase;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param getFileThumbnailsUseCase 썸네일 조회 UseCase
     */
    public FileThumbnailController(GetFileThumbnailsUseCase getFileThumbnailsUseCase) {
        this.getFileThumbnailsUseCase = Objects.requireNonNull(
                getFileThumbnailsUseCase,
                "GetFileThumbnailsUseCase must not be null"
        );
    }

    /**
     * 파일의 모든 썸네일을 조회합니다.
     *
     * @param fileId 원본 파일 ID
     * @return 썸네일 목록 응답
     */
    @Operation(
            summary = "파일 썸네일 조회",
            description = "원본 파일의 모든 썸네일(SMALL, MEDIUM)을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "썸네일 조회 성공",
                    content = @Content(schema = @Schema(implementation = ThumbnailListApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 파일 ID"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "파일을 찾을 수 없음"
            )
    })
    @GetMapping("/{fileId}/thumbnails")
    public ResponseEntity<ThumbnailListApiResponse> getThumbnails(
            @Parameter(description = "파일 ID", required = true, example = "file_123456789")
            @PathVariable String fileId
    ) {
        log.info("GET /api/v1/files/{}/thumbnails", fileId);

        FileId sourceFileId = FileId.of(fileId);
        List<ThumbnailResponse> thumbnails = getFileThumbnailsUseCase.getThumbnails(sourceFileId);

        ThumbnailListApiResponse response = ThumbnailListApiResponse.of(thumbnails);

        log.info("Successfully retrieved {} thumbnails for file: {}", thumbnails.size(), fileId);

        return ResponseEntity.ok(response);
    }
}
