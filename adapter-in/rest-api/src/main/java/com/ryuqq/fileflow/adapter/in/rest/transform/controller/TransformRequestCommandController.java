package com.ryuqq.fileflow.adapter.in.rest.transform.controller;

import static com.ryuqq.fileflow.adapter.in.rest.transform.TransformRequestEndpoints.BASE;
import static com.ryuqq.fileflow.adapter.in.rest.transform.TransformRequestEndpoints.CREATE;

import com.ryuqq.fileflow.adapter.in.rest.common.dto.ApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.transform.dto.command.CreateTransformRequestApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.transform.dto.response.TransformRequestApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.transform.mapper.TransformRequestCommandApiMapper;
import com.ryuqq.fileflow.adapter.in.rest.transform.mapper.TransformRequestQueryApiMapper;
import com.ryuqq.fileflow.application.transform.dto.command.CreateTransformRequestCommand;
import com.ryuqq.fileflow.application.transform.dto.response.TransformRequestResponse;
import com.ryuqq.fileflow.application.transform.port.in.command.CreateTransformRequestUseCase;
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
 * TransformRequestCommandController - 이미지 변환 요청 Command Controller.
 *
 * <p>API-CTL-001: Controller는 @RestController로 등록.
 *
 * <p>API-CTL-004: Command Controller는 생성 엔드포인트만 처리.
 *
 * <p>API-CTL-003: UseCase만 의존 (Service 직접 의존 금지).
 */
@Tag(name = "이미지 변환 요청 관리", description = "이미지 변환 요청 생성 API")
@RestController
@RequestMapping(BASE)
public class TransformRequestCommandController {

    private final CreateTransformRequestUseCase createUseCase;
    private final TransformRequestCommandApiMapper commandMapper;
    private final TransformRequestQueryApiMapper queryMapper;

    public TransformRequestCommandController(
            CreateTransformRequestUseCase createUseCase,
            TransformRequestCommandApiMapper commandMapper,
            TransformRequestQueryApiMapper queryMapper) {
        this.createUseCase = createUseCase;
        this.commandMapper = commandMapper;
        this.queryMapper = queryMapper;
    }

    /**
     * 이미지 변환 요청 생성.
     *
     * @param request 생성 요청
     * @return 생성된 변환 요청 정보
     */
    @Operation(
            summary = "이미지 변환 요청 생성",
            description = "원본 이미지 Asset에 대한 변환(리사이즈, 포맷 변환 등) 요청을 생성합니다.")
    @PostMapping(CREATE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TransformRequestApiResponse> create(
            @Valid @RequestBody CreateTransformRequestApiRequest request) {

        CreateTransformRequestCommand command = commandMapper.toCommand(request);
        TransformRequestResponse response = createUseCase.execute(command);

        return ApiResponse.of(queryMapper.toResponse(response));
    }
}
