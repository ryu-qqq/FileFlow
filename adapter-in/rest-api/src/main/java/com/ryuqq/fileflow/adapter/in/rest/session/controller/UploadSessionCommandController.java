package com.ryuqq.fileflow.adapter.in.rest.session.controller;

import com.ryuqq.fileflow.adapter.in.rest.auth.paths.ApiPaths;
import com.ryuqq.fileflow.adapter.in.rest.common.dto.ApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.CompleteSingleUploadApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.InitMultipartUploadApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.InitSingleUploadApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.MarkPartUploadedApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.CancelUploadSessionApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.CompleteMultipartUploadApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.CompleteSingleUploadApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.InitMultipartUploadApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.InitSingleUploadApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.MarkPartUploadedApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.mapper.UploadSessionApiMapper;
import com.ryuqq.fileflow.application.session.dto.command.CancelUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.command.CompleteMultipartUploadCommand;
import com.ryuqq.fileflow.application.session.dto.command.CompleteSingleUploadCommand;
import com.ryuqq.fileflow.application.session.dto.command.InitMultipartUploadCommand;
import com.ryuqq.fileflow.application.session.dto.command.InitSingleUploadCommand;
import com.ryuqq.fileflow.application.session.dto.command.MarkPartUploadedCommand;
import com.ryuqq.fileflow.application.session.dto.response.CancelUploadSessionResponse;
import com.ryuqq.fileflow.application.session.dto.response.CompleteMultipartUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.CompleteSingleUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.InitMultipartUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.InitSingleUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.MarkPartUploadedResponse;
import com.ryuqq.fileflow.application.session.port.in.command.CancelUploadSessionUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.CompleteMultipartUploadUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.CompleteSingleUploadUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.InitMultipartUploadUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.InitSingleUploadUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.MarkPartUploadedUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Upload Session Command Controller
 *
 * <p>Upload Session 도메인의 상태 변경 API를 제공합니다.
 *
 * <p>제공하는 API:
 *
 * <ul>
 *   <li>POST /api/v1/file/upload-sessions/single - 단일 파일 업로드 세션 초기화
 *   <li>POST /api/v1/file/upload-sessions/multipart - Multipart 업로드 세션 초기화
 *   <li>PATCH /api/v1/file/upload-sessions/{sessionId}/single/complete - 단일 업로드 완료
 *   <li>PATCH /api/v1/file/upload-sessions/{sessionId}/multipart/complete - Multipart 업로드 완료
 *   <li>PATCH /api/v1/file/upload-sessions/{sessionId}/parts - Part 업로드 완료 표시
 *   <li>PATCH /api/v1/file/upload-sessions/{sessionId}/cancel - 업로드 세션 취소
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag(name = "Upload Session Command", description = "업로드 세션 상태 변경 API")
@RestController
@RequestMapping(ApiPaths.UploadSession.BASE)
@Validated
public class UploadSessionCommandController {

    private final InitSingleUploadUseCase initSingleUploadUseCase;
    private final InitMultipartUploadUseCase initMultiPartUploadUseCase;
    private final CompleteSingleUploadUseCase completeSingleUploadUseCase;
    private final CompleteMultipartUploadUseCase completeMultipartUploadUseCase;
    private final MarkPartUploadedUseCase markPartUploadedUseCase;
    private final CancelUploadSessionUseCase cancelUploadSessionUseCase;
    private final UploadSessionApiMapper uploadSessionApiMapper;

    /**
     * UploadSessionCommandController 생성자
     *
     * @param initSingleUploadUseCase 단일 업로드 초기화 UseCase
     * @param initMultiPartUploadUseCase Multipart 업로드 초기화 UseCase
     * @param completeSingleUploadUseCase 단일 업로드 완료 UseCase
     * @param completeMultipartUploadUseCase Multipart 업로드 완료 UseCase
     * @param markPartUploadedUseCase Part 업로드 완료 UseCase
     * @param cancelUploadSessionUseCase 세션 취소 UseCase
     * @param uploadSessionApiMapper Upload Session Mapper
     */
    public UploadSessionCommandController(
            InitSingleUploadUseCase initSingleUploadUseCase,
            InitMultipartUploadUseCase initMultiPartUploadUseCase,
            CompleteSingleUploadUseCase completeSingleUploadUseCase,
            CompleteMultipartUploadUseCase completeMultipartUploadUseCase,
            MarkPartUploadedUseCase markPartUploadedUseCase,
            CancelUploadSessionUseCase cancelUploadSessionUseCase,
            UploadSessionApiMapper uploadSessionApiMapper) {
        this.initSingleUploadUseCase = initSingleUploadUseCase;
        this.initMultiPartUploadUseCase = initMultiPartUploadUseCase;
        this.completeSingleUploadUseCase = completeSingleUploadUseCase;
        this.completeMultipartUploadUseCase = completeMultipartUploadUseCase;
        this.markPartUploadedUseCase = markPartUploadedUseCase;
        this.cancelUploadSessionUseCase = cancelUploadSessionUseCase;
        this.uploadSessionApiMapper = uploadSessionApiMapper;
    }

    /**
     * 단일 파일 업로드 세션 초기화
     *
     * @param request 단일 업로드 초기화 요청 DTO
     * @return 세션 정보 및 Presigned URL (201 Created)
     */
    @Operation(
            summary = "단일 업로드 세션 초기화",
            description = "단일 파일 업로드를 위한 세션을 초기화하고 Presigned URL을 발급합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201",
                description = "세션 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청")
    })
    @PreAuthorize("@access.canWrite()")
    @PostMapping(ApiPaths.UploadSession.SINGLE_INIT)
    public ResponseEntity<ApiResponse<InitSingleUploadApiResponse>> initSingleUpload(
            @RequestBody @Valid InitSingleUploadApiRequest request) {

        InitSingleUploadCommand command = uploadSessionApiMapper.toInitSingleUploadCommand(request);

        InitSingleUploadResponse useCaseResponse = initSingleUploadUseCase.execute(command);

        InitSingleUploadApiResponse apiResponse =
                uploadSessionApiMapper.toInitSingleUploadApiResponse(useCaseResponse);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * Multipart 업로드 세션 초기화
     *
     * @param request Multipart 업로드 초기화 요청 DTO
     * @return 세션 정보 및 Part별 Presigned URL (201 Created)
     */
    @Operation(
            summary = "Multipart 업로드 세션 초기화",
            description = "대용량 파일 업로드를 위한 Multipart 세션을 초기화합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201",
                description = "세션 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청")
    })
    @PreAuthorize("@access.canWrite()")
    @PostMapping(ApiPaths.UploadSession.MULTIPART_INIT)
    public ResponseEntity<ApiResponse<InitMultipartUploadApiResponse>> initMultipartUpload(
            @RequestBody @Valid InitMultipartUploadApiRequest request) {

        InitMultipartUploadCommand command =
                uploadSessionApiMapper.toInitMultipartUploadCommand(request);

        InitMultipartUploadResponse useCaseResponse = initMultiPartUploadUseCase.execute(command);

        InitMultipartUploadApiResponse apiResponse =
                uploadSessionApiMapper.toInitMultipartUploadApiResponse(useCaseResponse);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * 단일 파일 업로드 완료
     *
     * @param sessionId 세션 ID
     * @param request 단일 업로드 완료 요청 DTO
     * @return 완료된 세션 정보 (200 OK)
     */
    @Operation(summary = "단일 업로드 완료", description = "단일 파일 업로드를 완료 처리합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "완료 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "세션을 찾을 수 없음")
    })
    @PreAuthorize("@access.canWrite()")
    @PatchMapping(ApiPaths.UploadSession.SINGLE_COMPLETE)
    public ResponseEntity<ApiResponse<CompleteSingleUploadApiResponse>> completeSingleUpload(
            @Parameter(description = "업로드 세션 ID", required = true, example = "session-123")
                    @PathVariable
                    @NotBlank
                    String sessionId,
            @RequestBody @Valid CompleteSingleUploadApiRequest request) {

        CompleteSingleUploadCommand command =
                uploadSessionApiMapper.toCompleteSingleUploadCommand(sessionId, request);

        CompleteSingleUploadResponse useCaseResponse = completeSingleUploadUseCase.execute(command);

        CompleteSingleUploadApiResponse apiResponse =
                uploadSessionApiMapper.toCompleteSingleUploadApiResponse(useCaseResponse);

        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * Multipart 업로드 완료
     *
     * @param sessionId 세션 ID
     * @return 완료된 세션 정보 및 Part 목록 (200 OK)
     */
    @Operation(summary = "Multipart 업로드 완료", description = "Multipart 업로드를 완료 처리합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "완료 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "세션을 찾을 수 없음")
    })
    @PreAuthorize("@access.canWrite()")
    @PatchMapping(ApiPaths.UploadSession.MULTIPART_COMPLETE)
    public ResponseEntity<ApiResponse<CompleteMultipartUploadApiResponse>> completeMultipartUpload(
            @Parameter(description = "업로드 세션 ID", required = true, example = "session-123")
                    @PathVariable
                    @NotBlank
                    String sessionId) {

        CompleteMultipartUploadCommand command =
                uploadSessionApiMapper.toCompleteMultipartUploadCommand(sessionId);

        CompleteMultipartUploadResponse useCaseResponse =
                completeMultipartUploadUseCase.execute(command);

        CompleteMultipartUploadApiResponse apiResponse =
                uploadSessionApiMapper.toCompleteMultipartUploadApiResponse(useCaseResponse);

        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * Part 업로드 완료 표시
     *
     * @param sessionId 세션 ID
     * @param request Part 업로드 완료 요청 DTO
     * @return Part 업로드 진행 상황 (200 OK)
     */
    @Operation(summary = "Part 업로드 완료 표시", description = "Multipart 업로드의 개별 Part 업로드 완료를 표시합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "표시 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "세션을 찾을 수 없음")
    })
    @PreAuthorize("@access.canWrite()")
    @PatchMapping(ApiPaths.UploadSession.PARTS)
    public ResponseEntity<ApiResponse<MarkPartUploadedApiResponse>> markPartUploaded(
            @Parameter(description = "업로드 세션 ID", required = true, example = "session-123")
                    @PathVariable
                    @NotBlank
                    String sessionId,
            @RequestBody @Valid MarkPartUploadedApiRequest request) {

        MarkPartUploadedCommand command =
                uploadSessionApiMapper.toMarkPartUploadedCommand(sessionId, request);

        MarkPartUploadedResponse useCaseResponse = markPartUploadedUseCase.execute(command);

        MarkPartUploadedApiResponse apiResponse =
                uploadSessionApiMapper.toMarkPartUploadedApiResponse(useCaseResponse);

        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * 업로드 세션 취소
     *
     * @param sessionId 세션 ID
     * @return 취소된 세션 정보 (200 OK)
     */
    @Operation(summary = "업로드 세션 취소", description = "진행 중인 업로드 세션을 취소합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "취소 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "세션을 찾을 수 없음")
    })
    @PreAuthorize("@access.canWrite()")
    @PatchMapping(ApiPaths.UploadSession.CANCEL)
    public ResponseEntity<ApiResponse<CancelUploadSessionApiResponse>> cancelUploadSession(
            @Parameter(description = "업로드 세션 ID", required = true, example = "session-123")
                    @PathVariable
                    @NotBlank
                    String sessionId) {

        CancelUploadSessionCommand command =
                uploadSessionApiMapper.toCancelUploadSessionCommand(sessionId);

        CancelUploadSessionResponse useCaseResponse = cancelUploadSessionUseCase.execute(command);

        CancelUploadSessionApiResponse apiResponse =
                uploadSessionApiMapper.toCancelUploadSessionApiResponse(useCaseResponse);

        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
