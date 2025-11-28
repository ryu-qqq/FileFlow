package com.ryuqq.fileflow.adapter.in.rest.session.controller;

import com.ryuqq.fileflow.adapter.in.rest.common.dto.ApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.common.dto.SliceApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.query.UploadSessionSearchApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.UploadSessionApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.UploadSessionDetailApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.mapper.UploadSessionApiMapper;
import com.ryuqq.fileflow.application.common.context.UserContextHolder;
import com.ryuqq.fileflow.application.common.dto.response.SliceResponse;
import com.ryuqq.fileflow.application.session.dto.query.GetUploadSessionQuery;
import com.ryuqq.fileflow.application.session.dto.query.ListUploadSessionsQuery;
import com.ryuqq.fileflow.application.session.dto.response.UploadSessionDetailResponse;
import com.ryuqq.fileflow.application.session.dto.response.UploadSessionResponse;
import com.ryuqq.fileflow.application.session.port.in.query.GetUploadSessionUseCase;
import com.ryuqq.fileflow.application.session.port.in.query.GetUploadSessionsUseCase;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Upload Session Query Controller
 *
 * <p>Upload Session 도메인의 조회 API를 제공합니다.
 *
 * <p>제공하는 API:
 *
 * <ul>
 *   <li>GET /api/v1/upload-sessions/{sessionId} - 업로드 세션 상세 조회
 *   <li>GET /api/v1/upload-sessions - 업로드 세션 목록 조회
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@RestController
@RequestMapping("${api.endpoints.base-v1}${api.endpoints.upload-session.base}")
@Validated
public class UploadSessionQueryController {

    private final GetUploadSessionUseCase getUploadSessionUseCase;
    private final GetUploadSessionsUseCase getUploadSessionsUseCase;
    private final UploadSessionApiMapper uploadSessionApiMapper;

    /**
     * UploadSessionQueryController 생성자
     *
     * @param getUploadSessionUseCase 단건 조회 UseCase
     * @param getUploadSessionsUseCase 목록 조회 UseCase
     * @param uploadSessionApiMapper Upload Session Mapper
     */
    public UploadSessionQueryController(
            GetUploadSessionUseCase getUploadSessionUseCase,
            GetUploadSessionsUseCase getUploadSessionsUseCase,
            UploadSessionApiMapper uploadSessionApiMapper) {
        this.getUploadSessionUseCase = getUploadSessionUseCase;
        this.getUploadSessionsUseCase = getUploadSessionsUseCase;
        this.uploadSessionApiMapper = uploadSessionApiMapper;
    }

    /**
     * 업로드 세션 상세 조회
     *
     * <p>업로드 세션의 상세 정보를 조회합니다. Multipart 세션의 경우 Part 정보를 포함합니다.
     *
     * @param sessionId 세션 ID
     * @return 업로드 세션 상세 정보 (200 OK)
     */
    @GetMapping("${api.endpoints.upload-session.by-id}")
    public ResponseEntity<ApiResponse<UploadSessionDetailApiResponse>> getUploadSession(
            @PathVariable @NotBlank String sessionId) {

        UserContext userContext = UserContextHolder.getRequired();
        long tenantId = userContext.tenant().id();

        GetUploadSessionQuery query =
                uploadSessionApiMapper.toGetUploadSessionQuery(sessionId, tenantId);

        UploadSessionDetailResponse useCaseResponse = getUploadSessionUseCase.execute(query);

        UploadSessionDetailApiResponse apiResponse =
                uploadSessionApiMapper.toUploadSessionDetailApiResponse(useCaseResponse);

        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * 업로드 세션 목록 조회
     *
     * <p>업로드 세션 목록을 조회합니다. 상태 및 업로드 타입으로 필터링할 수 있습니다.
     *
     * @param request 검색 조건
     * @return 업로드 세션 목록 (Slice 응답, 200 OK)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<SliceApiResponse<UploadSessionApiResponse>>>
            getUploadSessions(@Valid UploadSessionSearchApiRequest request) {

        UserContext userContext = UserContextHolder.getRequired();
        long tenantId = userContext.tenant().id();
        long organizationId = userContext.getOrganizationId();

        ListUploadSessionsQuery query =
                uploadSessionApiMapper.toListUploadSessionsQuery(request, tenantId, organizationId);

        SliceResponse<UploadSessionResponse> useCaseResponse =
                getUploadSessionsUseCase.execute(query);

        SliceApiResponse<UploadSessionApiResponse> apiResponse =
                SliceApiResponse.from(
                        useCaseResponse, uploadSessionApiMapper::toUploadSessionApiResponse);

        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
