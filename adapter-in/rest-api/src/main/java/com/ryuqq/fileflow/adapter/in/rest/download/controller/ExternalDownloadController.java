package com.ryuqq.fileflow.adapter.in.rest.download.controller;

import com.ryuqq.fileflow.adapter.in.rest.auth.paths.ApiPaths;
import com.ryuqq.fileflow.adapter.in.rest.common.dto.ApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.download.dto.command.RequestExternalDownloadApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.download.dto.query.ExternalDownloadSearchApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.download.dto.response.ExternalDownloadApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.download.dto.response.ExternalDownloadDetailApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.download.mapper.ExternalDownloadApiMapper;
import com.ryuqq.fileflow.application.common.context.UserContextHolder;
import com.ryuqq.fileflow.application.common.dto.response.PageResponse;
import com.ryuqq.fileflow.application.download.dto.command.RequestExternalDownloadCommand;
import com.ryuqq.fileflow.application.download.dto.query.GetExternalDownloadQuery;
import com.ryuqq.fileflow.application.download.dto.query.ListExternalDownloadsQuery;
import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadDetailResponse;
import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadResponse;
import com.ryuqq.fileflow.application.download.port.in.command.RequestExternalDownloadUseCase;
import com.ryuqq.fileflow.application.download.port.in.query.GetExternalDownloadUseCase;
import com.ryuqq.fileflow.application.download.port.in.query.GetExternalDownloadsUseCase;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * External Download Controller
 *
 * <p>외부 URL 다운로드 요청 및 조회 API를 제공합니다.
 *
 * <p>제공하는 API:
 *
 * <ul>
 *   <li>POST /api/v1/file/external-downloads - 외부 다운로드 요청
 *   <li>GET /api/v1/file/external-downloads - 외부 다운로드 목록 조회
 *   <li>GET /api/v1/file/external-downloads/{id} - 외부 다운로드 상태 조회
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag(name = "External Download", description = "외부 URL 다운로드 API")
@RestController
@RequestMapping(ApiPaths.ExternalDownload.BASE)
@Validated
public class ExternalDownloadController {

    private final RequestExternalDownloadUseCase requestExternalDownloadUseCase;
    private final GetExternalDownloadUseCase getExternalDownloadUseCase;
    private final GetExternalDownloadsUseCase getExternalDownloadsUseCase;
    private final ExternalDownloadApiMapper externalDownloadApiMapper;

    /**
     * ExternalDownloadController 생성자
     *
     * @param requestExternalDownloadUseCase 외부 다운로드 요청 UseCase
     * @param getExternalDownloadUseCase 외부 다운로드 조회 UseCase
     * @param getExternalDownloadsUseCase 외부 다운로드 목록 조회 UseCase
     * @param externalDownloadApiMapper ExternalDownload API Mapper
     */
    public ExternalDownloadController(
            RequestExternalDownloadUseCase requestExternalDownloadUseCase,
            GetExternalDownloadUseCase getExternalDownloadUseCase,
            GetExternalDownloadsUseCase getExternalDownloadsUseCase,
            ExternalDownloadApiMapper externalDownloadApiMapper) {
        this.requestExternalDownloadUseCase = requestExternalDownloadUseCase;
        this.getExternalDownloadUseCase = getExternalDownloadUseCase;
        this.getExternalDownloadsUseCase = getExternalDownloadsUseCase;
        this.externalDownloadApiMapper = externalDownloadApiMapper;
    }

    /**
     * 외부 다운로드 요청
     *
     * <p>외부 URL에서 이미지를 다운로드하여 S3에 업로드하는 비동기 요청을 생성합니다.
     *
     * @param request 외부 다운로드 요청 DTO
     * @return 생성된 ExternalDownload ID 및 상태 (201 Created)
     */
    @Operation(
            summary = "외부 다운로드 요청",
            description =
                    "외부 URL에서 이미지를 다운로드하여 S3에 업로드하는 비동기 요청을 생성합니다.\n\n**필요 권한**: `file:download`",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201",
                description = "요청 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청")
    })
    @PreAuthorize("@access.canDownload()")
    @PostMapping
    public ResponseEntity<ApiResponse<ExternalDownloadApiResponse>> requestExternalDownload(
            @RequestBody @Valid RequestExternalDownloadApiRequest request) {

        UserContext userContext = UserContextHolder.getRequired();
        String tenantId = userContext.tenant().id().value();
        String organizationId = userContext.getOrganizationId().value();

        RequestExternalDownloadCommand command =
                externalDownloadApiMapper.toCommand(request, tenantId, organizationId);

        ExternalDownloadResponse useCaseResponse = requestExternalDownloadUseCase.execute(command);

        ExternalDownloadApiResponse apiResponse =
                externalDownloadApiMapper.toApiResponse(useCaseResponse);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * 외부 다운로드 목록 조회
     *
     * <p>조건에 맞는 외부 다운로드 요청 목록을 페이징하여 조회합니다.
     *
     * @param status 상태 필터 (nullable)
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 외부 다운로드 목록 (200 OK)
     */
    @Operation(
            summary = "외부 다운로드 목록 조회",
            description = "조건에 맞는 외부 다운로드 요청 목록을 페이징하여 조회합니다.\n\n**필요 권한**: `file:read`",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공")
    })
    @PreAuthorize("@access.canRead()")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ExternalDownloadDetailApiResponse>>>
            getExternalDownloads(
                    @Parameter(
                                    description = "상태 필터 (PENDING, PROCESSING, COMPLETED, FAILED)",
                                    example = "COMPLETED")
                            String status,
                    @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") Integer page,
                    @Parameter(description = "페이지 크기", example = "20") Integer size) {

        UserContext userContext = UserContextHolder.getRequired();
        String tenantId = userContext.tenant().id().value();
        String organizationId = userContext.getOrganizationId().value();

        ExternalDownloadSearchApiRequest request =
                new ExternalDownloadSearchApiRequest(status, page, size);
        ListExternalDownloadsQuery query =
                externalDownloadApiMapper.toListQuery(request, organizationId, tenantId);

        PageResponse<ExternalDownloadDetailResponse> useCaseResponse =
                getExternalDownloadsUseCase.execute(query);

        PageResponse<ExternalDownloadDetailApiResponse> apiResponse =
                PageResponse.of(
                        useCaseResponse.content().stream()
                                .map(externalDownloadApiMapper::toDetailApiResponse)
                                .toList(),
                        useCaseResponse.page(),
                        useCaseResponse.size(),
                        useCaseResponse.totalElements(),
                        useCaseResponse.totalPages(),
                        useCaseResponse.first(),
                        useCaseResponse.last());

        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * 외부 다운로드 상태 조회
     *
     * <p>외부 다운로드 요청의 현재 상태를 조회합니다.
     *
     * @param id ExternalDownload ID
     * @return 외부 다운로드 상세 정보 (200 OK)
     */
    @Operation(
            summary = "외부 다운로드 상태 조회",
            description = "외부 다운로드 요청의 현재 상태를 조회합니다.\n\n**필요 권한**: `file:read`",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "외부 다운로드를 찾을 수 없음")
    })
    @PreAuthorize("@access.canRead()")
    @GetMapping(ApiPaths.ExternalDownload.BY_ID)
    public ResponseEntity<ApiResponse<ExternalDownloadDetailApiResponse>> getExternalDownload(
            @Parameter(description = "외부 다운로드 ID", required = true, example = "download-123")
                    @PathVariable
                    String id) {

        UserContext userContext = UserContextHolder.getRequired();
        String tenantId = userContext.tenant().id().value();

        GetExternalDownloadQuery query = externalDownloadApiMapper.toQuery(id, tenantId);

        ExternalDownloadDetailResponse useCaseResponse = getExternalDownloadUseCase.execute(query);

        ExternalDownloadDetailApiResponse apiResponse =
                externalDownloadApiMapper.toDetailApiResponse(useCaseResponse);

        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
