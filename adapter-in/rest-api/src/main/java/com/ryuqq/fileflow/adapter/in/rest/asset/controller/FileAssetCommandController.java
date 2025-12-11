package com.ryuqq.fileflow.adapter.in.rest.asset.controller;

import com.ryuqq.fileflow.adapter.in.rest.asset.dto.command.BatchGenerateDownloadUrlApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.command.DeleteFileAssetApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.command.GenerateDownloadUrlApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.BatchDownloadUrlApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.DeleteFileAssetApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.DownloadUrlApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.asset.mapper.FileAssetApiMapper;
import com.ryuqq.fileflow.adapter.in.rest.common.dto.ApiResponse;
import com.ryuqq.fileflow.application.asset.dto.command.BatchGenerateDownloadUrlCommand;
import com.ryuqq.fileflow.application.asset.dto.command.DeleteFileAssetCommand;
import com.ryuqq.fileflow.application.asset.dto.command.GenerateDownloadUrlCommand;
import com.ryuqq.fileflow.application.asset.dto.response.BatchDownloadUrlResponse;
import com.ryuqq.fileflow.application.asset.dto.response.DeleteFileAssetResponse;
import com.ryuqq.fileflow.application.asset.dto.response.DownloadUrlResponse;
import com.ryuqq.fileflow.application.asset.port.in.command.BatchGenerateDownloadUrlUseCase;
import com.ryuqq.fileflow.application.asset.port.in.command.DeleteFileAssetUseCase;
import com.ryuqq.fileflow.application.asset.port.in.command.GenerateDownloadUrlUseCase;
import com.ryuqq.fileflow.application.common.context.UserContextHolder;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * FileAsset Command Controller.
 *
 * <p>FileAsset 도메인의 Command API를 제공합니다.
 *
 * <p>제공하는 API:
 *
 * <ul>
 *   <li>PATCH /api/v1/file-assets/{id}/delete - 파일 자산 Soft Delete
 *   <li>POST /api/v1/file-assets/{id}/download-url - Presigned Download URL 생성
 *   <li>POST /api/v1/file-assets/batch-download-url - 다중 파일 Download URL 일괄 생성
 * </ul>
 *
 * <p><strong>설계 원칙:</strong>
 *
 * <ul>
 *   <li>DELETE 메서드 대신 PATCH 사용 (Soft Delete)
 *   <li>S3 객체는 삭제하지 않음 (메타데이터만 삭제 처리)
 *   <li>테넌트/조직 스코프 검증은 UseCase에서 수행
 *   <li>Presigned URL은 시간 제한이 있는 임시 URL
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag(name = "FileAsset Command", description = "파일 자산 상태 변경 API")
@RestController
@RequestMapping("${api.endpoints.base-v1}${api.endpoints.file-asset.base}")
@Validated
public class FileAssetCommandController {

    private final DeleteFileAssetUseCase deleteFileAssetUseCase;
    private final GenerateDownloadUrlUseCase generateDownloadUrlUseCase;
    private final BatchGenerateDownloadUrlUseCase batchGenerateDownloadUrlUseCase;
    private final FileAssetApiMapper fileAssetApiMapper;

    /**
     * FileAssetCommandController 생성자.
     *
     * @param deleteFileAssetUseCase 파일 자산 삭제 UseCase
     * @param generateDownloadUrlUseCase Download URL 생성 UseCase
     * @param batchGenerateDownloadUrlUseCase Batch Download URL 생성 UseCase
     * @param fileAssetApiMapper FileAsset API Mapper
     */
    public FileAssetCommandController(
            DeleteFileAssetUseCase deleteFileAssetUseCase,
            GenerateDownloadUrlUseCase generateDownloadUrlUseCase,
            BatchGenerateDownloadUrlUseCase batchGenerateDownloadUrlUseCase,
            FileAssetApiMapper fileAssetApiMapper) {
        this.deleteFileAssetUseCase = deleteFileAssetUseCase;
        this.generateDownloadUrlUseCase = generateDownloadUrlUseCase;
        this.batchGenerateDownloadUrlUseCase = batchGenerateDownloadUrlUseCase;
        this.fileAssetApiMapper = fileAssetApiMapper;
    }

    /**
     * 파일 자산 Soft Delete.
     *
     * <p>파일 자산을 논리적으로 삭제합니다. S3 객체는 유지됩니다.
     *
     * @param id 파일 자산 ID
     * @param request 삭제 요청 (삭제 사유 선택적)
     * @return 삭제 결과 (200 OK)
     */
    @Operation(summary = "파일 자산 삭제", description = "파일 자산을 논리적으로 삭제합니다. S3 객체는 유지됩니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "파일 자산을 찾을 수 없음")
    })
    @PatchMapping("${api.endpoints.file-asset.delete}")
    public ResponseEntity<ApiResponse<DeleteFileAssetApiResponse>> deleteFileAsset(
            @Parameter(description = "파일 자산 ID", required = true, example = "asset-123")
            @PathVariable @NotBlank String id,
            @Valid @RequestBody(required = false) DeleteFileAssetApiRequest request) {

        UserContext userContext = UserContextHolder.getRequired();
        String tenantId = userContext.tenant().id().value();
        String organizationId = userContext.getOrganizationId().value();

        DeleteFileAssetCommand command =
                fileAssetApiMapper.toDeleteFileAssetCommand(id, request, tenantId, organizationId);

        DeleteFileAssetResponse useCaseResponse = deleteFileAssetUseCase.execute(command);

        DeleteFileAssetApiResponse apiResponse =
                fileAssetApiMapper.toDeleteApiResponse(useCaseResponse);

        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * Presigned Download URL 생성.
     *
     * <p>파일 다운로드를 위한 Presigned URL을 생성합니다.
     *
     * @param id 파일 자산 ID
     * @param request URL 생성 요청 (유효 기간 설정 선택적)
     * @return Download URL 정보 (200 OK)
     */
    @Operation(summary = "다운로드 URL 생성", description = "파일 다운로드를 위한 Presigned URL을 생성합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "URL 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "파일 자산을 찾을 수 없음")
    })
    @PostMapping("${api.endpoints.file-asset.download-url}")
    public ResponseEntity<ApiResponse<DownloadUrlApiResponse>> generateDownloadUrl(
            @Parameter(description = "파일 자산 ID", required = true, example = "asset-123")
            @PathVariable @NotBlank String id,
            @Valid @RequestBody(required = false) GenerateDownloadUrlApiRequest request) {

        UserContext userContext = UserContextHolder.getRequired();
        String tenantId = userContext.tenant().id().value();
        String organizationId = userContext.getOrganizationId().value();

        GenerateDownloadUrlCommand command =
                fileAssetApiMapper.toGenerateDownloadUrlCommand(
                        id, request, tenantId, organizationId);

        DownloadUrlResponse useCaseResponse = generateDownloadUrlUseCase.execute(command);

        DownloadUrlApiResponse apiResponse =
                fileAssetApiMapper.toDownloadUrlApiResponse(useCaseResponse);

        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * Presigned Download URL 일괄 생성.
     *
     * <p>여러 파일에 대한 다운로드 URL을 일괄 생성합니다. 최대 100개까지 요청 가능합니다.
     *
     * @param request 일괄 URL 생성 요청
     * @return Download URL 목록 및 실패 정보 (200 OK)
     */
    @Operation(summary = "다운로드 URL 일괄 생성", description = "여러 파일에 대한 다운로드 URL을 일괄 생성합니다. 최대 100개까지 요청 가능합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "URL 일괄 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("${api.endpoints.file-asset.batch-download-url}")
    public ResponseEntity<ApiResponse<BatchDownloadUrlApiResponse>> batchGenerateDownloadUrl(
            @Valid @RequestBody BatchGenerateDownloadUrlApiRequest request) {

        UserContext userContext = UserContextHolder.getRequired();
        String tenantId = userContext.tenant().id().value();
        String organizationId = userContext.getOrganizationId().value();

        BatchGenerateDownloadUrlCommand command =
                fileAssetApiMapper.toBatchGenerateDownloadUrlCommand(
                        request, tenantId, organizationId);

        BatchDownloadUrlResponse useCaseResponse = batchGenerateDownloadUrlUseCase.execute(command);

        BatchDownloadUrlApiResponse apiResponse =
                fileAssetApiMapper.toBatchDownloadUrlApiResponse(useCaseResponse);

        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
