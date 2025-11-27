package com.ryuqq.fileflow.adapter.in.rest.asset.controller;

import com.ryuqq.fileflow.adapter.in.rest.asset.dto.query.FileAssetSearchApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.FileAssetApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.asset.mapper.FileAssetApiMapper;
import com.ryuqq.fileflow.adapter.in.rest.common.dto.ApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.common.dto.PageApiResponse;
import com.ryuqq.fileflow.application.asset.dto.query.GetFileAssetQuery;
import com.ryuqq.fileflow.application.asset.dto.query.ListFileAssetsQuery;
import com.ryuqq.fileflow.application.asset.dto.response.FileAssetResponse;
import com.ryuqq.fileflow.application.asset.port.in.query.GetFileAssetUseCase;
import com.ryuqq.fileflow.application.asset.port.in.query.GetFileAssetsUseCase;
import com.ryuqq.fileflow.application.common.context.UserContextHolder;
import com.ryuqq.fileflow.application.common.dto.response.PageResponse;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
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
 *   <li>GET /api/v1/file-assets/{id} - 파일 자산 단건 조회
 *   <li>GET /api/v1/file-assets - 파일 자산 목록 조회
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@RestController
@RequestMapping("${api.endpoints.base-v1}/file-assets")
@Validated
public class FileAssetQueryController {

    private final GetFileAssetUseCase getFileAssetUseCase;
    private final GetFileAssetsUseCase getFileAssetsUseCase;
    private final FileAssetApiMapper fileAssetApiMapper;

    /**
     * FileAssetQueryController 생성자
     *
     * @param getFileAssetUseCase 파일 자산 단건 조회 UseCase
     * @param getFileAssetsUseCase 파일 자산 목록 조회 UseCase
     * @param fileAssetApiMapper FileAsset API Mapper
     */
    public FileAssetQueryController(
            GetFileAssetUseCase getFileAssetUseCase,
            GetFileAssetsUseCase getFileAssetsUseCase,
            FileAssetApiMapper fileAssetApiMapper) {
        this.getFileAssetUseCase = getFileAssetUseCase;
        this.getFileAssetsUseCase = getFileAssetsUseCase;
        this.fileAssetApiMapper = fileAssetApiMapper;
    }

    /**
     * 파일 자산 단건 조회
     *
     * @param id 파일 자산 ID
     * @return 파일 자산 상세 정보 (200 OK)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FileAssetApiResponse>> getFileAsset(
            @PathVariable @NotBlank String id) {

        UserContext userContext = UserContextHolder.getRequired();
        long organizationId = userContext.getOrganizationId();
        long tenantId = userContext.tenant().id();

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
    @GetMapping
    public ResponseEntity<ApiResponse<PageApiResponse<FileAssetApiResponse>>> getFileAssets(
            @Valid @ModelAttribute FileAssetSearchApiRequest request) {

        UserContext userContext = UserContextHolder.getRequired();
        long organizationId = userContext.getOrganizationId();
        long tenantId = userContext.tenant().id();

        ListFileAssetsQuery query =
                fileAssetApiMapper.toListFileAssetsQuery(request, organizationId, tenantId);

        PageResponse<FileAssetResponse> useCaseResponse = getFileAssetsUseCase.execute(query);

        PageApiResponse<FileAssetApiResponse> apiResponse =
                PageApiResponse.from(useCaseResponse, fileAssetApiMapper::toApiResponse);

        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
