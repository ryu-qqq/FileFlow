package com.ryuqq.fileflow.adapter.in.rest.session.controller;

import static com.ryuqq.fileflow.adapter.in.rest.session.SingleUploadSessionEndpoints.BASE;
import static com.ryuqq.fileflow.adapter.in.rest.session.SingleUploadSessionEndpoints.COMPLETE;
import static com.ryuqq.fileflow.adapter.in.rest.session.SingleUploadSessionEndpoints.CREATE;

import com.ryuqq.fileflow.adapter.in.rest.common.dto.ApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.CompleteSingleUploadSessionApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.CreateSingleUploadSessionApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.SingleUploadSessionApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.mapper.SessionCommandApiMapper;
import com.ryuqq.fileflow.adapter.in.rest.session.mapper.SessionQueryApiMapper;
import com.ryuqq.fileflow.application.session.dto.command.CompleteSingleUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.command.CreateSingleUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.response.SingleUploadSessionResponse;
import com.ryuqq.fileflow.application.session.port.in.command.CompleteSingleUploadSessionUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.CreateSingleUploadSessionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * SingleUploadSessionCommandController - 단건 업로드 세션 Command Controller.
 *
 * <p>API-CTL-001: Controller는 @RestController로 등록.
 *
 * <p>API-CTL-004: Command Controller는 생성/완료 엔드포인트만 처리.
 *
 * <p>API-CTL-003: UseCase만 의존 (Service 직접 의존 금지).
 */
@Tag(name = "단건 업로드 세션 관리", description = "단건 업로드 세션 생성/완료 API")
@RestController
@RequestMapping(BASE)
public class SingleUploadSessionCommandController {

    private final CreateSingleUploadSessionUseCase createUseCase;
    private final CompleteSingleUploadSessionUseCase completeUseCase;
    private final SessionCommandApiMapper commandMapper;
    private final SessionQueryApiMapper queryMapper;

    public SingleUploadSessionCommandController(
            CreateSingleUploadSessionUseCase createUseCase,
            CompleteSingleUploadSessionUseCase completeUseCase,
            SessionCommandApiMapper commandMapper,
            SessionQueryApiMapper queryMapper) {
        this.createUseCase = createUseCase;
        this.completeUseCase = completeUseCase;
        this.commandMapper = commandMapper;
        this.queryMapper = queryMapper;
    }

    /**
     * 단건 업로드 세션 생성.
     *
     * @param request 생성 요청
     * @return 생성된 세션 정보 (Presigned URL 포함)
     */
    @Operation(summary = "단건 업로드 세션 생성", description = "Presigned URL과 함께 단건 업로드 세션을 생성합니다.")
    @PostMapping(CREATE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SingleUploadSessionApiResponse> create(
            @Valid @RequestBody CreateSingleUploadSessionApiRequest request) {

        CreateSingleUploadSessionCommand command = commandMapper.toCommand(request);
        SingleUploadSessionResponse response = createUseCase.execute(command);

        return ApiResponse.of(queryMapper.toResponse(response));
    }

    /**
     * 단건 업로드 세션 완료.
     *
     * @param sessionId 세션 ID
     * @param request 완료 요청
     * @return 빈 응답
     */
    @Operation(summary = "단건 업로드 세션 완료", description = "단건 업로드 완료를 처리합니다.")
    @PostMapping(COMPLETE)
    public ApiResponse<Void> complete(
            @Parameter(description = "세션 ID", required = true) @PathVariable String sessionId,
            @Valid @RequestBody CompleteSingleUploadSessionApiRequest request) {

        CompleteSingleUploadSessionCommand command = commandMapper.toCommand(sessionId, request);
        completeUseCase.execute(command);

        return ApiResponse.of();
    }
}
