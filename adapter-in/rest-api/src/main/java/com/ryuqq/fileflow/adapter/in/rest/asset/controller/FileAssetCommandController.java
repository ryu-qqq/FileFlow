package com.ryuqq.fileflow.adapter.in.rest.asset.controller;

import com.ryuqq.fileflow.adapter.in.rest.asset.dto.command.BatchDeleteFileAssetApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.command.BatchGenerateDownloadUrlApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.command.DeleteFileAssetApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.command.GenerateDownloadUrlApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.command.RetryFailedFileAssetApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.BatchDeleteFileAssetApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.BatchDownloadUrlApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.DeleteFileAssetApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.DownloadUrlApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.RetryFailedFileAssetApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.asset.mapper.FileAssetApiMapper;
import com.ryuqq.fileflow.adapter.in.rest.auth.paths.ApiPaths;
import com.ryuqq.fileflow.adapter.in.rest.common.dto.ApiResponse;
import com.ryuqq.fileflow.application.asset.dto.command.BatchDeleteFileAssetCommand;
import com.ryuqq.fileflow.application.asset.dto.command.BatchGenerateDownloadUrlCommand;
import com.ryuqq.fileflow.application.asset.dto.command.DeleteFileAssetCommand;
import com.ryuqq.fileflow.application.asset.dto.command.GenerateDownloadUrlCommand;
import com.ryuqq.fileflow.application.asset.dto.command.RetryFailedFileAssetCommand;
import com.ryuqq.fileflow.application.asset.dto.response.BatchDeleteFileAssetResponse;
import com.ryuqq.fileflow.application.asset.dto.response.BatchDownloadUrlResponse;
import com.ryuqq.fileflow.application.asset.dto.response.DeleteFileAssetResponse;
import com.ryuqq.fileflow.application.asset.dto.response.DownloadUrlResponse;
import com.ryuqq.fileflow.application.asset.dto.response.RetryFailedFileAssetResponse;
import com.ryuqq.fileflow.application.asset.port.in.command.BatchDeleteFileAssetUseCase;
import com.ryuqq.fileflow.application.asset.port.in.command.BatchGenerateDownloadUrlUseCase;
import com.ryuqq.fileflow.application.asset.port.in.command.DeleteFileAssetUseCase;
import com.ryuqq.fileflow.application.asset.port.in.command.GenerateDownloadUrlUseCase;
import com.ryuqq.fileflow.application.asset.port.in.command.RetryFailedFileAssetUseCase;
import com.ryuqq.fileflow.application.common.context.UserContextHolder;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
 *   <li>PATCH /api/v1/file/file-assets/{id}/delete - 파일 자산 Soft Delete
 *   <li>POST /api/v1/file/file-assets/{id}/download-url - Presigned Download URL 생성
 *   <li>POST /api/v1/file/file-assets/batch-download-url - 다중 파일 Download URL 일괄 생성
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
@RequestMapping(ApiPaths.FileAsset.BASE)
@Validated
public class FileAssetCommandController {

    private final DeleteFileAssetUseCase deleteFileAssetUseCase;
    private final BatchDeleteFileAssetUseCase batchDeleteFileAssetUseCase;
    private final GenerateDownloadUrlUseCase generateDownloadUrlUseCase;
    private final BatchGenerateDownloadUrlUseCase batchGenerateDownloadUrlUseCase;
    private final RetryFailedFileAssetUseCase retryFailedFileAssetUseCase;
    private final FileAssetApiMapper fileAssetApiMapper;

    /**
     * FileAssetCommandController 생성자.
     *
     * @param deleteFileAssetUseCase 파일 자산 삭제 UseCase
     * @param batchDeleteFileAssetUseCase 파일 자산 일괄 삭제 UseCase
     * @param generateDownloadUrlUseCase Download URL 생성 UseCase
     * @param batchGenerateDownloadUrlUseCase Batch Download URL 생성 UseCase
     * @param retryFailedFileAssetUseCase 실패한 파일 재처리 UseCase
     * @param fileAssetApiMapper FileAsset API Mapper
     */
    public FileAssetCommandController(
            DeleteFileAssetUseCase deleteFileAssetUseCase,
            BatchDeleteFileAssetUseCase batchDeleteFileAssetUseCase,
            GenerateDownloadUrlUseCase generateDownloadUrlUseCase,
            BatchGenerateDownloadUrlUseCase batchGenerateDownloadUrlUseCase,
            RetryFailedFileAssetUseCase retryFailedFileAssetUseCase,
            FileAssetApiMapper fileAssetApiMapper) {
        this.deleteFileAssetUseCase = deleteFileAssetUseCase;
        this.batchDeleteFileAssetUseCase = batchDeleteFileAssetUseCase;
        this.generateDownloadUrlUseCase = generateDownloadUrlUseCase;
        this.batchGenerateDownloadUrlUseCase = batchGenerateDownloadUrlUseCase;
        this.retryFailedFileAssetUseCase = retryFailedFileAssetUseCase;
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
    @Operation(
            summary = "파일 자산 삭제",
            description = "파일 자산을 논리적으로 삭제합니다. S3 객체는 유지됩니다.\n\n**필요 권한**: `file:delete`",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "파일 자산을 찾을 수 없음")
    })
    @PreAuthorize("@access.canDelete()")
    @PatchMapping(ApiPaths.FileAsset.DELETE)
    public ResponseEntity<ApiResponse<DeleteFileAssetApiResponse>> deleteFileAsset(
            @Parameter(description = "파일 자산 ID", required = true, example = "asset-123")
                    @PathVariable
                    @NotBlank
                    String id,
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
     * 파일 자산 일괄 삭제.
     *
     * <p>여러 파일 자산을 한 번에 논리적으로 삭제합니다. 부분 성공을 지원합니다.
     *
     * @param request 일괄 삭제 요청 (파일 ID 목록, 삭제 사유)
     * @return 삭제 결과 (성공/실패 목록)
     */
    @Operation(
            summary = "파일 자산 일괄 삭제",
            description =
                    "여러 파일 자산을 한 번에 논리적으로 삭제합니다. 최대 100개까지 요청 가능합니다.\n\n"
                            + "**부분 성공 지원**: 일부 파일 삭제 실패 시에도 나머지 파일은 정상 처리됩니다.\n\n"
                            + "**필요 권한**: `file:delete`",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "일괄 삭제 처리 완료 (부분 성공 포함)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청")
    })
    @PreAuthorize("@access.canDelete()")
    @PostMapping(ApiPaths.FileAsset.BATCH_DELETE)
    public ResponseEntity<ApiResponse<BatchDeleteFileAssetApiResponse>> batchDeleteFileAssets(
            @Valid @RequestBody BatchDeleteFileAssetApiRequest request) {

        UserContext userContext = UserContextHolder.getRequired();
        String tenantId = userContext.tenant().id().value();
        String organizationId = userContext.getOrganizationId().value();

        BatchDeleteFileAssetCommand command =
                fileAssetApiMapper.toBatchDeleteFileAssetCommand(request, tenantId, organizationId);

        BatchDeleteFileAssetResponse useCaseResponse = batchDeleteFileAssetUseCase.execute(command);

        BatchDeleteFileAssetApiResponse apiResponse =
                fileAssetApiMapper.toBatchDeleteApiResponse(useCaseResponse);

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
    @Operation(
            summary = "다운로드 URL 생성",
            description = "파일 다운로드를 위한 Presigned URL을 생성합니다.\n\n**필요 권한**: `file:download`",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "URL 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "파일 자산을 찾을 수 없음")
    })
    @PreAuthorize("@access.canDownload()")
    @PostMapping(ApiPaths.FileAsset.DOWNLOAD_URL)
    public ResponseEntity<ApiResponse<DownloadUrlApiResponse>> generateDownloadUrl(
            @Parameter(description = "파일 자산 ID", required = true, example = "asset-123")
                    @PathVariable
                    @NotBlank
                    String id,
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
    @Operation(
            summary = "다운로드 URL 일괄 생성",
            description =
                    "여러 파일에 대한 다운로드 URL을 일괄 생성합니다. 최대 100개까지 요청 가능합니다.\n\n"
                            + "**필요 권한**: `file:download`",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "URL 일괄 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청")
    })
    @PreAuthorize("@access.canDownload()")
    @PostMapping(ApiPaths.FileAsset.BATCH_DOWNLOAD_URL)
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

    /**
     * 실패한 파일 자산 재처리.
     *
     * <p>FAILED 상태인 파일 자산을 PENDING 상태로 변경하여 재처리를 시작합니다.
     *
     * @param id 파일 자산 ID
     * @param request 재처리 요청 (사유 선택적)
     * @return 재처리 결과 (200 OK)
     */
    @Operation(
            summary = "실패한 파일 재처리",
            description =
                    "FAILED 상태인 파일 자산을 PENDING 상태로 변경하여 재처리를 시작합니다.\n\n"
                            + "**필요 권한**: `file:write`",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "재처리 요청 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "FAILED 상태가 아닌 경우"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "파일 자산을 찾을 수 없음")
    })
    @PreAuthorize("@access.canWrite()")
    @PostMapping(ApiPaths.FileAsset.RETRY)
    public ResponseEntity<ApiResponse<RetryFailedFileAssetApiResponse>> retryFailedFileAsset(
            @Parameter(description = "파일 자산 ID", required = true, example = "asset-123")
                    @PathVariable
                    @NotBlank
                    String id,
            @Valid @RequestBody(required = false) RetryFailedFileAssetApiRequest request) {

        UserContext userContext = UserContextHolder.getRequired();
        String tenantId = userContext.tenant().id().value();
        String organizationId = userContext.getOrganizationId().value();

        RetryFailedFileAssetCommand command =
                fileAssetApiMapper.toRetryFailedFileAssetCommand(id, tenantId, organizationId);

        RetryFailedFileAssetResponse useCaseResponse = retryFailedFileAssetUseCase.execute(command);

        RetryFailedFileAssetApiResponse apiResponse =
                fileAssetApiMapper.toRetryApiResponse(useCaseResponse);

        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
