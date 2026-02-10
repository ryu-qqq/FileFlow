package com.ryuqq.fileflow.adapter.in.rest.session.controller;

import static com.ryuqq.fileflow.adapter.in.rest.session.MultipartUploadSessionEndpoints.BASE;
import static com.ryuqq.fileflow.adapter.in.rest.session.MultipartUploadSessionEndpoints.DETAIL;

import com.ryuqq.fileflow.adapter.in.rest.common.dto.ApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.MultipartUploadSessionApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.mapper.SessionQueryApiMapper;
import com.ryuqq.fileflow.application.session.dto.response.MultipartUploadSessionResponse;
import com.ryuqq.fileflow.application.session.port.in.query.GetMultipartUploadSessionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MultipartUploadSessionQueryController - 멀티파트 업로드 세션 Query Controller.
 *
 * <p>API-CTL-001: Controller는 @RestController로 등록.
 *
 * <p>API-CTR-010: CQRS Controller 분리.
 *
 * <p>API-CTL-003: UseCase만 의존 (Service 직접 의존 금지).
 */
@Tag(name = "멀티파트 업로드 세션 조회", description = "멀티파트 업로드 세션 조회 API")
@RestController
@RequestMapping(BASE)
public class MultipartUploadSessionQueryController {

    private final GetMultipartUploadSessionUseCase getUseCase;
    private final SessionQueryApiMapper queryMapper;

    public MultipartUploadSessionQueryController(
            GetMultipartUploadSessionUseCase getUseCase, SessionQueryApiMapper queryMapper) {
        this.getUseCase = getUseCase;
        this.queryMapper = queryMapper;
    }

    /**
     * 멀티파트 업로드 세션 상세 조회.
     *
     * @param sessionId 세션 ID
     * @return 세션 상세 정보
     */
    @Operation(summary = "멀티파트 업로드 세션 조회", description = "멀티파트 업로드 세션의 상세 정보를 조회합니다.")
    @GetMapping(DETAIL)
    public ApiResponse<MultipartUploadSessionApiResponse> get(
            @Parameter(description = "세션 ID", required = true) @PathVariable String sessionId) {

        MultipartUploadSessionResponse response = getUseCase.execute(sessionId);

        return ApiResponse.of(queryMapper.toResponse(response));
    }
}
