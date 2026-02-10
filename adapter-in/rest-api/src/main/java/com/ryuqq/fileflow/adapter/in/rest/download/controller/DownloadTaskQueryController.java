package com.ryuqq.fileflow.adapter.in.rest.download.controller;

import static com.ryuqq.fileflow.adapter.in.rest.download.DownloadTaskEndpoints.BASE;
import static com.ryuqq.fileflow.adapter.in.rest.download.DownloadTaskEndpoints.DETAIL;

import com.ryuqq.fileflow.adapter.in.rest.common.dto.ApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.download.dto.response.DownloadTaskApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.download.mapper.DownloadTaskQueryApiMapper;
import com.ryuqq.fileflow.application.download.dto.response.DownloadTaskResponse;
import com.ryuqq.fileflow.application.download.port.in.query.GetDownloadTaskUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * DownloadTaskQueryController - 다운로드 작업 Query Controller.
 *
 * <p>API-CTL-001: Controller는 @RestController로 등록.
 *
 * <p>API-CTR-010: CQRS Controller 분리.
 *
 * <p>API-CTL-003: UseCase만 의존 (Service 직접 의존 금지).
 */
@Tag(name = "다운로드 작업 조회", description = "다운로드 작업 조회 API")
@RestController
@RequestMapping(BASE)
public class DownloadTaskQueryController {

    private final GetDownloadTaskUseCase getUseCase;
    private final DownloadTaskQueryApiMapper queryMapper;

    public DownloadTaskQueryController(
            GetDownloadTaskUseCase getUseCase, DownloadTaskQueryApiMapper queryMapper) {
        this.getUseCase = getUseCase;
        this.queryMapper = queryMapper;
    }

    /**
     * 다운로드 작업 상세 조회.
     *
     * @param downloadTaskId 다운로드 작업 ID
     * @return 작업 상세 정보
     */
    @Operation(summary = "다운로드 작업 조회", description = "다운로드 작업의 상세 정보와 진행 상태를 조회합니다.")
    @GetMapping(DETAIL)
    public ApiResponse<DownloadTaskApiResponse> get(
            @Parameter(description = "다운로드 작업 ID", required = true) @PathVariable
                    String downloadTaskId) {

        DownloadTaskResponse response = getUseCase.execute(downloadTaskId);

        return ApiResponse.of(queryMapper.toResponse(response));
    }
}
