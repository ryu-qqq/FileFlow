package com.ryuqq.fileflow.adapter.in.rest.asset.controller;

import static com.ryuqq.fileflow.adapter.in.rest.asset.AssetEndpoints.BASE;
import static com.ryuqq.fileflow.adapter.in.rest.asset.AssetEndpoints.DETAIL;
import static com.ryuqq.fileflow.adapter.in.rest.asset.AssetEndpoints.METADATA;

import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.AssetApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.AssetMetadataApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.asset.mapper.AssetQueryApiMapper;
import com.ryuqq.fileflow.adapter.in.rest.common.dto.ApiResponse;
import com.ryuqq.fileflow.application.asset.dto.response.AssetMetadataResponse;
import com.ryuqq.fileflow.application.asset.dto.response.AssetResponse;
import com.ryuqq.fileflow.application.asset.port.in.query.GetAssetMetadataUseCase;
import com.ryuqq.fileflow.application.asset.port.in.query.GetAssetUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AssetQueryController - Asset Query Controller.
 *
 * <p>API-CTL-001: Controller는 @RestController로 등록.
 *
 * <p>API-CTR-010: CQRS Controller 분리.
 *
 * <p>API-CTL-003: UseCase만 의존 (Service 직접 의존 금지).
 */
@Tag(name = "Asset 조회", description = "Asset 조회 API")
@RestController
@RequestMapping(BASE)
public class AssetQueryController {

    private final GetAssetUseCase getAssetUseCase;
    private final GetAssetMetadataUseCase getAssetMetadataUseCase;
    private final AssetQueryApiMapper queryMapper;

    public AssetQueryController(
            GetAssetUseCase getAssetUseCase,
            GetAssetMetadataUseCase getAssetMetadataUseCase,
            AssetQueryApiMapper queryMapper) {
        this.getAssetUseCase = getAssetUseCase;
        this.getAssetMetadataUseCase = getAssetMetadataUseCase;
        this.queryMapper = queryMapper;
    }

    /**
     * Asset 상세 조회.
     *
     * @param assetId Asset ID
     * @return Asset 상세 정보
     */
    @Operation(summary = "Asset 조회", description = "Asset의 상세 정보를 조회합니다.")
    @GetMapping(DETAIL)
    public ApiResponse<AssetApiResponse> get(
            @Parameter(description = "Asset ID", required = true) @PathVariable String assetId) {

        AssetResponse response = getAssetUseCase.execute(assetId);

        return ApiResponse.of(queryMapper.toResponse(response));
    }

    /**
     * Asset 메타데이터 조회.
     *
     * @param assetId Asset ID
     * @return Asset 메타데이터 정보
     */
    @Operation(summary = "Asset 메타데이터 조회", description = "Asset의 이미지 메타데이터(너비, 높이 등)를 조회합니다.")
    @GetMapping(METADATA)
    public ApiResponse<AssetMetadataApiResponse> getMetadata(
            @Parameter(description = "Asset ID", required = true) @PathVariable String assetId) {

        AssetMetadataResponse response = getAssetMetadataUseCase.execute(assetId);

        return ApiResponse.of(queryMapper.toResponse(response));
    }
}
