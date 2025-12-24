package com.ryuqq.fileflow.adapter.in.rest.asset.controller;

import com.ryuqq.fileflow.adapter.in.rest.asset.dto.query.FileAssetSearchApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.FileAssetApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.FileAssetStatisticsApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.asset.mapper.FileAssetApiMapper;
import com.ryuqq.fileflow.adapter.in.rest.auth.paths.ApiPaths;
import com.ryuqq.fileflow.adapter.in.rest.common.dto.ApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.common.dto.PageApiResponse;
import com.ryuqq.fileflow.application.asset.dto.query.GetFileAssetQuery;
import com.ryuqq.fileflow.application.asset.dto.query.GetFileAssetStatisticsQuery;
import com.ryuqq.fileflow.application.asset.dto.query.ListFileAssetsQuery;
import com.ryuqq.fileflow.application.asset.dto.response.FileAssetResponse;
import com.ryuqq.fileflow.application.asset.dto.response.FileAssetStatisticsResponse;
import com.ryuqq.fileflow.application.asset.port.in.query.GetFileAssetStatisticsUseCase;
import com.ryuqq.fileflow.application.asset.port.in.query.GetFileAssetUseCase;
import com.ryuqq.fileflow.application.asset.port.in.query.GetFileAssetsUseCase;
import com.ryuqq.fileflow.application.common.context.UserContextHolder;
import com.ryuqq.fileflow.application.common.dto.response.PageResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * FileAsset Query Controller
 *
 * <p>FileAsset 도메인의 조회 API를 제공합니다.
 *
 * <p>제공하는 API:
 *
 * <ul>
 *   <li>GET /api/v1/file/file-assets/{id} - 파일 자산 단건 조회
 *   <li>GET /api/v1/file/file-assets - 파일 자산 목록 조회
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag(name = "FileAsset Query", description = "파일 자산 조회 API")
@RestController
@RequestMapping(ApiPaths.FileAsset.BASE)
@Validated
public class FileAssetQueryController {

    private final GetFileAssetUseCase getFileAssetUseCase;
    private final GetFileAssetsUseCase getFileAssetsUseCase;
    private final GetFileAssetStatisticsUseCase getFileAssetStatisticsUseCase;
    private final FileAssetApiMapper fileAssetApiMapper;

    /**
     * FileAssetQueryController 생성자
     *
     * @param getFileAssetUseCase 파일 자산 단건 조회 UseCase
     * @param getFileAssetsUseCase 파일 자산 목록 조회 UseCase
     * @param getFileAssetStatisticsUseCase 파일 자산 통계 조회 UseCase
     * @param fileAssetApiMapper FileAsset API Mapper
     */
    public FileAssetQueryController(
            GetFileAssetUseCase getFileAssetUseCase,
            GetFileAssetsUseCase getFileAssetsUseCase,
            GetFileAssetStatisticsUseCase getFileAssetStatisticsUseCase,
            FileAssetApiMapper fileAssetApiMapper) {
        this.getFileAssetUseCase = getFileAssetUseCase;
        this.getFileAssetsUseCase = getFileAssetsUseCase;
        this.getFileAssetStatisticsUseCase = getFileAssetStatisticsUseCase;
        this.fileAssetApiMapper = fileAssetApiMapper;
    }

    /**
     * 파일 자산 단건 조회
     *
     * @param id 파일 자산 ID
     * @return 파일 자산 상세 정보 (200 OK)
     */
    @Operation(
            summary = "파일 자산 단건 조회",
            description = "파일 자산의 상세 정보를 조회합니다.\n\n**필요 권한**: `file:read`",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "파일 자산을 찾을 수 없음")
    })
    @PreAuthorize("@access.hasPermission('file:read')")
    @GetMapping(ApiPaths.FileAsset.BY_ID)
    public ResponseEntity<ApiResponse<FileAssetApiResponse>> getFileAsset(
            @Parameter(description = "파일 자산 ID", required = true, example = "asset-123")
                    @PathVariable
                    @NotBlank
                    String id) {

        UserContext userContext = UserContextHolder.getRequired();
        String organizationId = userContext.getOrganizationId();
        String tenantId = userContext.tenant().id().value();

        GetFileAssetQuery query =
                fileAssetApiMapper.toGetFileAssetQuery(id, organizationId, tenantId);

        FileAssetResponse useCaseResponse = getFileAssetUseCase.execute(query);

        FileAssetApiResponse apiResponse = fileAssetApiMapper.toApiResponse(useCaseResponse);

        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * 파일 자산 목록 조회
     *
     * @param request 검색 조건
     * @return 파일 자산 목록 (200 OK)
     */
    @Operation(
            summary = "파일 자산 목록 조회",
            description = "파일 자산 목록을 조회합니다.\n\n**필요 권한**: `file:read`",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공")
    })
    @PreAuthorize("@access.hasPermission('file:read')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageApiResponse<FileAssetApiResponse>>> getFileAssets(
            @Valid @ModelAttribute FileAssetSearchApiRequest request) {

        UserContext userContext = UserContextHolder.getRequired();
        String organizationId = userContext.getOrganizationId();
        String tenantId = userContext.tenant().id().value();

        ListFileAssetsQuery query =
                fileAssetApiMapper.toListFileAssetsQuery(request, organizationId, tenantId);

        PageResponse<FileAssetResponse> useCaseResponse = getFileAssetsUseCase.execute(query);

        PageApiResponse<FileAssetApiResponse> apiResponse =
                PageApiResponse.from(useCaseResponse, fileAssetApiMapper::toApiResponse);

        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * 파일 자산 통계 조회
     *
     * @return 파일 자산 통계 (상태별, 카테고리별 개수)
     */
    @Operation(
            summary = "파일 자산 통계 조회",
            description = "파일 자산의 상태별/카테고리별 통계를 조회합니다.\n\n**필요 권한**: `file:read`",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공")
    })
    @PreAuthorize("@access.hasPermission('file:read')")
    @GetMapping(ApiPaths.FileAsset.STATISTICS)
    public ResponseEntity<ApiResponse<FileAssetStatisticsApiResponse>> getFileAssetStatistics() {

        UserContext userContext = UserContextHolder.getRequired();
        String organizationId = userContext.getOrganizationId();
        String tenantId = userContext.tenant().id().value();

        GetFileAssetStatisticsQuery query =
                fileAssetApiMapper.toGetStatisticsQuery(organizationId, tenantId);

        FileAssetStatisticsResponse useCaseResponse = getFileAssetStatisticsUseCase.execute(query);

        FileAssetStatisticsApiResponse apiResponse =
                fileAssetApiMapper.toStatisticsApiResponse(useCaseResponse);

        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
