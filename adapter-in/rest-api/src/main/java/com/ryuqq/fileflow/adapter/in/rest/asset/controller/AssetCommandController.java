package com.ryuqq.fileflow.adapter.in.rest.asset.controller;

import static com.ryuqq.fileflow.adapter.in.rest.asset.AssetEndpoints.BASE;
import static com.ryuqq.fileflow.adapter.in.rest.asset.AssetEndpoints.DELETE;

import com.ryuqq.fileflow.adapter.in.rest.asset.mapper.AssetCommandApiMapper;
import com.ryuqq.fileflow.adapter.in.rest.common.dto.ApiResponse;
import com.ryuqq.fileflow.application.asset.dto.command.DeleteAssetCommand;
import com.ryuqq.fileflow.application.asset.port.in.command.DeleteAssetUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * AssetCommandController - Asset Command Controller.
 *
 * <p>API-CTL-001: Controller는 @RestController로 등록.
 *
 * <p>API-CTL-004: Command Controller는 삭제 엔드포인트 처리.
 *
 * <p>API-CTL-003: UseCase만 의존 (Service 직접 의존 금지).
 */
@Tag(name = "Asset 관리", description = "Asset 삭제 API")
@RestController
@RequestMapping(BASE)
public class AssetCommandController {

    private final DeleteAssetUseCase deleteUseCase;
    private final AssetCommandApiMapper commandMapper;

    public AssetCommandController(
            DeleteAssetUseCase deleteUseCase, AssetCommandApiMapper commandMapper) {
        this.deleteUseCase = deleteUseCase;
        this.commandMapper = commandMapper;
    }

    /**
     * Asset 삭제.
     *
     * @param assetId Asset ID
     * @param source 요청 서비스명
     * @return 빈 응답
     */
    @Operation(summary = "Asset 삭제", description = "Asset을 논리 삭제합니다.")
    @DeleteMapping(DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> delete(
            @Parameter(description = "Asset ID", required = true) @PathVariable String assetId,
            @Parameter(description = "요청 서비스명", required = true, example = "commerce-api")
                    @RequestParam
                    @NotBlank
                    String source) {

        DeleteAssetCommand command = commandMapper.toDeleteCommand(assetId, source);
        deleteUseCase.execute(command);

        return ApiResponse.of();
    }
}
