package com.ryuqq.fileflow.adapter.in.rest.transform.controller;

import static com.ryuqq.fileflow.adapter.in.rest.transform.TransformRequestEndpoints.BASE;
import static com.ryuqq.fileflow.adapter.in.rest.transform.TransformRequestEndpoints.DETAIL;

import com.ryuqq.fileflow.adapter.in.rest.common.dto.ApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.transform.dto.response.TransformRequestApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.transform.mapper.TransformRequestQueryApiMapper;
import com.ryuqq.fileflow.application.transform.dto.response.TransformRequestResponse;
import com.ryuqq.fileflow.application.transform.port.in.query.GetTransformRequestUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TransformRequestQueryController - 이미지 변환 요청 Query Controller.
 *
 * <p>API-CTL-001: Controller는 @RestController로 등록.
 *
 * <p>API-CTR-010: CQRS Controller 분리.
 *
 * <p>API-CTL-003: UseCase만 의존 (Service 직접 의존 금지).
 */
@Tag(name = "이미지 변환 요청 조회", description = "이미지 변환 요청 조회 API")
@RestController
@RequestMapping(BASE)
public class TransformRequestQueryController {

    private final GetTransformRequestUseCase getUseCase;
    private final TransformRequestQueryApiMapper queryMapper;

    public TransformRequestQueryController(
            GetTransformRequestUseCase getUseCase, TransformRequestQueryApiMapper queryMapper) {
        this.getUseCase = getUseCase;
        this.queryMapper = queryMapper;
    }

    /**
     * 이미지 변환 요청 상세 조회.
     *
     * @param transformRequestId 변환 요청 ID
     * @return 변환 요청 상세 정보
     */
    @Operation(summary = "이미지 변환 요청 조회", description = "이미지 변환 요청의 상세 정보와 진행 상태를 조회합니다.")
    @GetMapping(DETAIL)
    public ApiResponse<TransformRequestApiResponse> get(
            @Parameter(description = "변환 요청 ID", required = true) @PathVariable
                    String transformRequestId) {

        TransformRequestResponse response = getUseCase.execute(transformRequestId);

        return ApiResponse.of(queryMapper.toResponse(response));
    }
}
