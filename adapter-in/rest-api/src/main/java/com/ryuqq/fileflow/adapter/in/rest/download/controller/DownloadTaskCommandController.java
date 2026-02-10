package com.ryuqq.fileflow.adapter.in.rest.download.controller;

import static com.ryuqq.fileflow.adapter.in.rest.download.DownloadTaskEndpoints.BASE;
import static com.ryuqq.fileflow.adapter.in.rest.download.DownloadTaskEndpoints.CREATE;

import com.ryuqq.fileflow.adapter.in.rest.common.dto.ApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.download.dto.command.CreateDownloadTaskApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.download.dto.response.DownloadTaskApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.download.mapper.DownloadTaskCommandApiMapper;
import com.ryuqq.fileflow.adapter.in.rest.download.mapper.DownloadTaskQueryApiMapper;
import com.ryuqq.fileflow.application.download.dto.command.CreateDownloadTaskCommand;
import com.ryuqq.fileflow.application.download.dto.response.DownloadTaskResponse;
import com.ryuqq.fileflow.application.download.port.in.command.CreateDownloadTaskUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * DownloadTaskCommandController - 다운로드 작업 Command Controller.
 *
 * <p>API-CTL-001: Controller는 @RestController로 등록.
 *
 * <p>API-CTL-004: Command Controller는 생성 엔드포인트만 처리.
 *
 * <p>API-CTL-003: UseCase만 의존 (Service 직접 의존 금지).
 */
@Tag(name = "다운로드 작업 관리", description = "다운로드 작업 생성 API")
@RestController
@RequestMapping(BASE)
public class DownloadTaskCommandController {

    private final CreateDownloadTaskUseCase createUseCase;
    private final DownloadTaskCommandApiMapper commandMapper;
    private final DownloadTaskQueryApiMapper queryMapper;

    public DownloadTaskCommandController(
            CreateDownloadTaskUseCase createUseCase,
            DownloadTaskCommandApiMapper commandMapper,
            DownloadTaskQueryApiMapper queryMapper) {
        this.createUseCase = createUseCase;
        this.commandMapper = commandMapper;
        this.queryMapper = queryMapper;
    }

    /**
     * 다운로드 작업 생성.
     *
     * @param request 생성 요청
     * @return 생성된 다운로드 작업 정보
     */
    @Operation(summary = "다운로드 작업 생성", description = "외부 URL의 파일을 S3로 다운로드하는 작업을 생성합니다.")
    @PostMapping(CREATE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DownloadTaskApiResponse> create(
            @Valid @RequestBody CreateDownloadTaskApiRequest request) {

        CreateDownloadTaskCommand command = commandMapper.toCommand(request);
        DownloadTaskResponse response = createUseCase.execute(command);

        return ApiResponse.of(queryMapper.toResponse(response));
    }
}
