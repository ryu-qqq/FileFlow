package com.ryuqq.fileflow.adapter.in.rest.session.controller;

import static com.ryuqq.fileflow.adapter.in.rest.session.MultipartUploadSessionEndpoints.ABORT;
import static com.ryuqq.fileflow.adapter.in.rest.session.MultipartUploadSessionEndpoints.BASE;
import static com.ryuqq.fileflow.adapter.in.rest.session.MultipartUploadSessionEndpoints.COMPLETE;
import static com.ryuqq.fileflow.adapter.in.rest.session.MultipartUploadSessionEndpoints.CREATE;
import static com.ryuqq.fileflow.adapter.in.rest.session.MultipartUploadSessionEndpoints.PARTS;
import static com.ryuqq.fileflow.adapter.in.rest.session.MultipartUploadSessionEndpoints.PRESIGNED_PART_URL;

import com.ryuqq.fileflow.adapter.in.rest.common.dto.ApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.AddCompletedPartApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.CompleteMultipartUploadSessionApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.CreateMultipartUploadSessionApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.MultipartUploadSessionApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.PresignedPartUrlApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.mapper.SessionCommandApiMapper;
import com.ryuqq.fileflow.adapter.in.rest.session.mapper.SessionQueryApiMapper;
import com.ryuqq.fileflow.application.session.dto.command.AbortMultipartUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.command.AddCompletedPartCommand;
import com.ryuqq.fileflow.application.session.dto.command.CompleteMultipartUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.command.CreateMultipartUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.command.GeneratePresignedPartUrlCommand;
import com.ryuqq.fileflow.application.session.dto.response.MultipartUploadSessionResponse;
import com.ryuqq.fileflow.application.session.dto.response.PresignedPartUrlResponse;
import com.ryuqq.fileflow.application.session.port.in.command.AbortMultipartUploadSessionUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.AddCompletedPartUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.CompleteMultipartUploadSessionUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.CreateMultipartUploadSessionUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.GeneratePresignedPartUrlUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * MultipartUploadSessionCommandController - 멀티파트 업로드 세션 Command Controller.
 *
 * <p>API-CTL-001: Controller는 @RestController로 등록.
 *
 * <p>API-CTL-004: Command Controller는 생성/완료/중단 엔드포인트 처리.
 *
 * <p>API-CTL-003: UseCase만 의존 (Service 직접 의존 금지).
 */
@Tag(name = "멀티파트 업로드 세션 관리", description = "멀티파트 업로드 세션 생성/파트관리/완료/중단 API")
@RestController
@RequestMapping(BASE)
public class MultipartUploadSessionCommandController {

    private final CreateMultipartUploadSessionUseCase createUseCase;
    private final GeneratePresignedPartUrlUseCase generatePresignedPartUrlUseCase;
    private final AddCompletedPartUseCase addCompletedPartUseCase;
    private final CompleteMultipartUploadSessionUseCase completeUseCase;
    private final AbortMultipartUploadSessionUseCase abortUseCase;
    private final SessionCommandApiMapper commandMapper;
    private final SessionQueryApiMapper queryMapper;

    public MultipartUploadSessionCommandController(
            CreateMultipartUploadSessionUseCase createUseCase,
            GeneratePresignedPartUrlUseCase generatePresignedPartUrlUseCase,
            AddCompletedPartUseCase addCompletedPartUseCase,
            CompleteMultipartUploadSessionUseCase completeUseCase,
            AbortMultipartUploadSessionUseCase abortUseCase,
            SessionCommandApiMapper commandMapper,
            SessionQueryApiMapper queryMapper) {
        this.createUseCase = createUseCase;
        this.generatePresignedPartUrlUseCase = generatePresignedPartUrlUseCase;
        this.addCompletedPartUseCase = addCompletedPartUseCase;
        this.completeUseCase = completeUseCase;
        this.abortUseCase = abortUseCase;
        this.commandMapper = commandMapper;
        this.queryMapper = queryMapper;
    }

    /**
     * 멀티파트 업로드 세션 생성.
     *
     * @param request 생성 요청
     * @return 생성된 세션 정보
     */
    @Operation(summary = "멀티파트 업로드 세션 생성", description = "S3 멀티파트 업로드 세션을 생성합니다.")
    @PostMapping(CREATE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MultipartUploadSessionApiResponse> create(
            @Valid @RequestBody CreateMultipartUploadSessionApiRequest request) {

        CreateMultipartUploadSessionCommand command = commandMapper.toCommand(request);
        MultipartUploadSessionResponse response = createUseCase.execute(command);

        return ApiResponse.of(queryMapper.toResponse(response));
    }

    /**
     * 파트별 Presigned URL 발급.
     *
     * @param sessionId 세션 ID
     * @param partNumber 파트 번호
     * @return Presigned URL 정보
     */
    @Operation(
            summary = "파트별 Presigned URL 발급",
            description = "멀티파트 업로드의 개별 파트에 대한 Presigned URL을 발급합니다.")
    @GetMapping(PRESIGNED_PART_URL)
    public ApiResponse<PresignedPartUrlApiResponse> generatePresignedPartUrl(
            @Parameter(description = "세션 ID", required = true) @PathVariable String sessionId,
            @Parameter(description = "파트 번호", required = true) @PathVariable int partNumber) {

        GeneratePresignedPartUrlCommand command = commandMapper.toCommand(sessionId, partNumber);
        PresignedPartUrlResponse response = generatePresignedPartUrlUseCase.execute(command);

        return ApiResponse.of(queryMapper.toResponse(response));
    }

    /**
     * 파트 업로드 완료 기록.
     *
     * @param sessionId 세션 ID
     * @param request 파트 완료 기록 요청
     * @return 빈 응답
     */
    @Operation(summary = "파트 업로드 완료 기록", description = "업로드가 완료된 파트 정보를 기록합니다.")
    @PostMapping(PARTS)
    public ApiResponse<Void> addCompletedPart(
            @Parameter(description = "세션 ID", required = true) @PathVariable String sessionId,
            @Valid @RequestBody AddCompletedPartApiRequest request) {

        AddCompletedPartCommand command = commandMapper.toCommand(sessionId, request);
        addCompletedPartUseCase.execute(command);

        return ApiResponse.of();
    }

    /**
     * 멀티파트 업로드 세션 완료.
     *
     * @param sessionId 세션 ID
     * @param request 완료 요청
     * @return 빈 응답
     */
    @Operation(summary = "멀티파트 업로드 세션 완료", description = "멀티파트 업로드를 완료 처리합니다.")
    @PostMapping(COMPLETE)
    public ApiResponse<Void> complete(
            @Parameter(description = "세션 ID", required = true) @PathVariable String sessionId,
            @Valid @RequestBody CompleteMultipartUploadSessionApiRequest request) {

        CompleteMultipartUploadSessionCommand command = commandMapper.toCommand(sessionId, request);
        completeUseCase.execute(command);

        return ApiResponse.of();
    }

    /**
     * 멀티파트 업로드 세션 중단.
     *
     * @param sessionId 세션 ID
     * @return 빈 응답
     */
    @Operation(summary = "멀티파트 업로드 세션 중단", description = "멀티파트 업로드를 중단하고 S3 리소스를 정리합니다.")
    @PostMapping(ABORT)
    public ApiResponse<Void> abort(
            @Parameter(description = "세션 ID", required = true) @PathVariable String sessionId) {

        AbortMultipartUploadSessionCommand command = commandMapper.toAbortCommand(sessionId);
        abortUseCase.execute(command);

        return ApiResponse.of();
    }
}
