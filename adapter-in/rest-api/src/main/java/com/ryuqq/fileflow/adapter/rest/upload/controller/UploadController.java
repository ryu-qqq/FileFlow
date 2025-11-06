package com.ryuqq.fileflow.adapter.rest.upload.controller;

import com.ryuqq.fileflow.adapter.rest.common.dto.ApiResponse;
import com.ryuqq.fileflow.adapter.rest.upload.dto.request.InitMultipartApiRequest;
import com.ryuqq.fileflow.adapter.rest.upload.dto.request.MarkPartUploadedApiRequest;
import com.ryuqq.fileflow.adapter.rest.upload.dto.request.SingleUploadApiRequest;
import com.ryuqq.fileflow.adapter.rest.upload.dto.response.CompleteMultipartApiResponse;
import com.ryuqq.fileflow.adapter.rest.upload.dto.response.CompleteSingleUploadApiResponse;
import com.ryuqq.fileflow.adapter.rest.upload.dto.response.InitMultipartApiResponse;
import com.ryuqq.fileflow.adapter.rest.upload.dto.response.PartPresignedUrlApiResponse;
import com.ryuqq.fileflow.adapter.rest.upload.dto.response.SingleUploadApiResponse;
import com.ryuqq.fileflow.adapter.rest.upload.mapper.UploadApiMapper;
import com.ryuqq.fileflow.application.upload.dto.command.CompleteMultipartCommand;
import com.ryuqq.fileflow.application.upload.dto.command.CompleteSingleUploadCommand;
import com.ryuqq.fileflow.application.upload.dto.command.GeneratePartUrlCommand;
import com.ryuqq.fileflow.application.upload.dto.command.InitMultipartCommand;
import com.ryuqq.fileflow.application.upload.dto.command.InitSingleUploadCommand;
import com.ryuqq.fileflow.application.upload.dto.command.MarkPartUploadedCommand;
import com.ryuqq.fileflow.application.upload.dto.response.CompleteMultipartResponse;
import com.ryuqq.fileflow.application.upload.dto.response.CompleteSingleUploadResponse;
import com.ryuqq.fileflow.application.upload.dto.response.InitMultipartResponse;
import com.ryuqq.fileflow.application.upload.dto.response.PartPresignedUrlResponse;
import com.ryuqq.fileflow.application.upload.dto.response.SingleUploadResponse;
import com.ryuqq.fileflow.application.upload.port.in.CompleteMultipartUploadUseCase;
import com.ryuqq.fileflow.application.upload.port.in.CompleteSingleUploadUseCase;
import com.ryuqq.fileflow.application.upload.port.in.GeneratePartPresignedUrlUseCase;
import com.ryuqq.fileflow.application.upload.port.in.InitMultipartUploadUseCase;
import com.ryuqq.fileflow.application.upload.port.in.InitSingleUploadUseCase;
import com.ryuqq.fileflow.application.upload.port.in.MarkPartUploadedUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Upload Controller
 *
 * <p>File Upload REST API 엔드포인트를 제공합니다.</p>
 *
 * <p><strong>제공 기능:</strong></p>
 * <ul>
 *   <li>단일 업로드 (100MB 미만)</li>
 *   <li>Multipart 업로드 초기화</li>
 *   <li>파트 업로드 URL 생성</li>
 *   <li>파트 업로드 완료 통보</li>
 *   <li>Multipart 업로드 완료</li>
 * </ul>
 *
 * <p><strong>공통 헤더:</strong></p>
 * <ul>
 *   <li>{@code X-Tenant-Id}: 테넌트 ID (필수)</li>
 *   <li>{@code X-Idempotency-Key}: 멱등성 키 (선택, init/complete 시 권장)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@RestController
@RequestMapping("${api.endpoints.base-v1}${api.endpoints.upload.base}")
@Validated
public class UploadController {

    private final InitSingleUploadUseCase initSingleUploadUseCase;
    private final CompleteSingleUploadUseCase completeSingleUploadUseCase;
    private final InitMultipartUploadUseCase initMultipartUseCase;
    private final GeneratePartPresignedUrlUseCase generatePartUrlUseCase;
    private final MarkPartUploadedUseCase markPartUploadedUseCase;
    private final CompleteMultipartUploadUseCase completeMultipartUseCase;

    /**
     * 생성자
     *
     * @param initSingleUploadUseCase 단일 업로드 초기화 UseCase
     * @param completeSingleUploadUseCase 단일 업로드 완료 UseCase
     * @param initMultipartUseCase Multipart 초기화 UseCase
     * @param generatePartUrlUseCase 파트 URL 생성 UseCase
     * @param markPartUploadedUseCase 파트 업로드 완료 UseCase
     * @param completeMultipartUseCase Multipart 완료 UseCase
     */
    public UploadController(
        InitSingleUploadUseCase initSingleUploadUseCase,
        CompleteSingleUploadUseCase completeSingleUploadUseCase,
        InitMultipartUploadUseCase initMultipartUseCase,
        GeneratePartPresignedUrlUseCase generatePartUrlUseCase,
        MarkPartUploadedUseCase markPartUploadedUseCase,
        CompleteMultipartUploadUseCase completeMultipartUseCase
    ) {
        this.initSingleUploadUseCase = initSingleUploadUseCase;
        this.completeSingleUploadUseCase = completeSingleUploadUseCase;
        this.initMultipartUseCase = initMultipartUseCase;
        this.generatePartUrlUseCase = generatePartUrlUseCase;
        this.markPartUploadedUseCase = markPartUploadedUseCase;
        this.completeMultipartUseCase = completeMultipartUseCase;
    }

    /**
     * 단일 업로드 초기화
     *
     * <p><strong>엔드포인트:</strong> {@code POST /api/v1/uploads/single}</p>
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <ul>
     *   <li>파일 크기가 100MB 미만인 경우</li>
     *   <li>단일 HTTP PUT으로 업로드 가능</li>
     *   <li>Multipart보다 간단하고 효율적</li>
     * </ul>
     *
     * <p><strong>Request Body 예시:</strong></p>
     * <pre>{@code
     * {
     *   "fileName": "document.pdf",
     *   "fileSize": 5242880,
     *   "contentType": "application/pdf",
     *   "checksum": "d41d8cd98f00b204e9800998ecf8427e"
     * }
     * }</pre>
     *
     * <p><strong>Response 예시:</strong></p>
     * <pre>{@code
     * {
     *   "success": true,
     *   "data": {
     *     "sessionKey": "spu_abc123def456",
     *     "uploadUrl": "https://s3.amazonaws.com/...",
     *     "storageKey": "uploads/2024/10/31/document.pdf"
     *   }
     * }
     * }</pre>
     *
     * <p><strong>클라이언트 흐름:</strong></p>
     * <ol>
     *   <li>이 API 호출하여 Presigned URL 획득</li>
     *   <li>uploadUrl로 HTTP PUT 요청 (파일 데이터를 Body에 포함)</li>
     *   <li>업로드 완료 (별도 complete API 불필요)</li>
     * </ol>
     *
     * @param request 단일 업로드 초기화 요청
     * @param tenantId 테넌트 ID (헤더)
     * @return 단일 업로드 초기화 응답 (201 Created)
     * @throws IllegalArgumentException 파일 크기가 100MB 이상인 경우
     */
    @PostMapping("/single")
    public ResponseEntity<ApiResponse<SingleUploadApiResponse>> initSingleUpload(
        @Valid @RequestBody SingleUploadApiRequest request,
        @RequestHeader("X-Tenant-Id") Long tenantId
    ) {
        InitSingleUploadCommand command = UploadApiMapper.toCommand(request, tenantId);
        SingleUploadResponse response = initSingleUploadUseCase.execute(command);
        SingleUploadApiResponse apiResponse = UploadApiMapper.toApiResponse(response);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * 단일 업로드 완료
     *
     * <p><strong>엔드포인트:</strong> {@code POST /api/v1/uploads/single/{sessionKey}/complete}</p>
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <ul>
     *   <li>Client가 Presigned URL로 S3에 직접 업로드 완료 후 호출</li>
     *   <li>S3에 파일 존재 여부 확인</li>
     *   <li>FileAsset Aggregate 생성</li>
     *   <li>UploadSession 완료 처리</li>
     * </ul>
     *
     * <p><strong>클라이언트 흐름:</strong></p>
     * <ol>
     *   <li>{@code POST /api/v1/uploads/single} 호출하여 Presigned URL 획득</li>
     *   <li>uploadUrl로 HTTP PUT 요청 (파일 업로드)</li>
     *   <li><strong>이 API 호출하여 업로드 완료 처리</strong> ⭐</li>
     * </ol>
     *
     * <p><strong>Response 예시:</strong></p>
     * <pre>{@code
     * {
     *   "success": true,
     *   "data": {
     *     "fileId": 12345,
     *     "etag": "\"d41d8cd98f00b204e9800998ecf8427e\"",
     *     "fileSize": 5242880
     *   }
     * }
     * }</pre>
     *
     * @param sessionKey 세션 키 (initSingleUpload에서 반환받은 값)
     * @return 생성된 FileAsset 정보 (200 OK)
     * @throws IllegalStateException UploadSession이 완료 가능한 상태가 아닌 경우
     * @throws com.ryuqq.fileflow.adapter.out.aws.s3.exception.S3StorageException S3에 파일이 존재하지 않는 경우
     */
    @PostMapping("/single/{sessionKey}/complete")
    public ResponseEntity<ApiResponse<CompleteSingleUploadApiResponse>> completeSingleUpload(
        @PathVariable String sessionKey
    ) {
        CompleteSingleUploadCommand command =
            UploadApiMapper.toCompleteSingleCommand(sessionKey);
        CompleteSingleUploadResponse response = completeSingleUploadUseCase.execute(command);
        CompleteSingleUploadApiResponse apiResponse = UploadApiMapper.toApiResponse(response);

        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * Multipart 업로드 초기화
     *
     * <p><strong>엔드포인트:</strong> {@code POST /api/v1/uploads/multipart/init}</p>
     *
     * <p><strong>Request Body 예시:</strong></p>
     * <pre>{@code
     * {
     *   "fileName": "large-video.mp4",
     *   "fileSize": 524288000,
     *   "contentType": "video/mp4",
     *   "checksum": "d41d8cd98f00b204e9800998ecf8427e"
     * }
     * }</pre>
     *
     * <p><strong>Response 예시:</strong></p>
     * <pre>{@code
     * {
     *   "success": true,
     *   "data": {
     *     "sessionKey": "mpu_abc123def456",
     *     "uploadId": "upload-xyz789",
     *     "totalParts": 10,
     *     "storageKey": "uploads/2024/10/31/large-video.mp4"
     *   }
     * }
     * }</pre>
     *
     * @param request 초기화 요청
     * @param tenantId 테넌트 ID (헤더)
     * @param idempotencyKey 멱등성 키 (헤더, 선택)
     * @return 초기화 응답
     */
    @PostMapping("/multipart/init")
    public ResponseEntity<ApiResponse<InitMultipartApiResponse>> initMultipart(
        @Valid @RequestBody InitMultipartApiRequest request,
        @RequestHeader("X-Tenant-Id") Long tenantId,
        @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey
    ) {
        InitMultipartCommand command = UploadApiMapper.toCommand(request, tenantId);
        InitMultipartResponse response = initMultipartUseCase.execute(command);
        InitMultipartApiResponse apiResponse = UploadApiMapper.toApiResponse(response);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * 파트 업로드 URL 생성
     *
     * <p><strong>엔드포인트:</strong> {@code POST /api/v1/uploads/multipart/{sessionKey}/parts/{partNumber}/url}</p>
     *
     * <p>클라이언트가 직접 S3에 파트를 업로드할 수 있도록 Presigned URL을 제공합니다.</p>
     *
     * <p><strong>Response 예시:</strong></p>
     * <pre>{@code
     * {
     *   "success": true,
     *   "data": {
     *     "partNumber": 1,
     *     "presignedUrl": "https://s3.amazonaws.com/...",
     *     "expiresInSeconds": 3600
     *   }
     * }
     * }</pre>
     *
     * @param sessionKey 세션 키
     * @param partNumber 파트 번호 (1~10000)
     * @return Presigned URL 응답
     */
    @PostMapping("/multipart/{sessionKey}/parts/{partNumber}/url")
    public ResponseEntity<ApiResponse<PartPresignedUrlApiResponse>> generatePartUrl(
        @PathVariable String sessionKey,
        @PathVariable @Min(1) @Max(10000) Integer partNumber
    ) {
        GeneratePartUrlCommand command = UploadApiMapper.toCommand(sessionKey, partNumber);
        PartPresignedUrlResponse response = generatePartUrlUseCase.execute(command);
        PartPresignedUrlApiResponse apiResponse = UploadApiMapper.toApiResponse(response);

        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * 파트 업로드 완료 통보
     *
     * <p><strong>엔드포인트:</strong> {@code PUT /api/v1/uploads/multipart/{sessionKey}/parts/{partNumber}}</p>
     *
     * <p>클라이언트가 S3에 파트 업로드 완료 후 호출하여 업로드된 파트 정보를 기록합니다.</p>
     *
     * <p><strong>Request Body 예시:</strong></p>
     * <pre>{@code
     * {
     *   "etag": "\"d41d8cd98f00b204e9800998ecf8427e\"",
     *   "partSize": 5242880
     * }
     * }</pre>
     *
     * @param sessionKey 세션 키
     * @param partNumber 파트 번호 (1~10000)
     * @param request 파트 업로드 완료 요청
     * @return 204 No Content
     */
    @PutMapping("/multipart/{sessionKey}/parts/{partNumber}")
    public ResponseEntity<Void> markPartUploaded(
        @PathVariable String sessionKey,
        @PathVariable @Min(1) @Max(10000) Integer partNumber,
        @Valid @RequestBody MarkPartUploadedApiRequest request
    ) {
        MarkPartUploadedCommand command = UploadApiMapper.toCommand(sessionKey, partNumber, request);
        markPartUploadedUseCase.execute(command);

        return ResponseEntity.noContent().build();
    }

    /**
     * Multipart 업로드 완료
     *
     * <p><strong>엔드포인트:</strong> {@code POST /api/v1/uploads/multipart/{sessionKey}/complete}</p>
     *
     * <p>모든 파트 업로드 완료 후 S3에서 최종 파일을 조립합니다.</p>
     *
     * <p><strong>Response 예시:</strong></p>
     * <pre>{@code
     * {
     *   "success": true,
     *   "data": {
     *     "fileId": 12345,
     *     "etag": "\"abc123def456\"",
     *     "location": "https://s3.amazonaws.com/bucket/uploads/2024/10/31/large-video.mp4"
     *   }
     * }
     * }</pre>
     *
     * @param sessionKey 세션 키
     * @param idempotencyKey 멱등성 키 (헤더, 선택)
     * @return 완료 응답
     */
    @PostMapping("/multipart/{sessionKey}/complete")
    public ResponseEntity<ApiResponse<CompleteMultipartApiResponse>> completeMultipart(
        @PathVariable String sessionKey,
        @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey
    ) {
        CompleteMultipartCommand command = UploadApiMapper.toCommand(sessionKey);
        CompleteMultipartResponse response = completeMultipartUseCase.execute(command);
        CompleteMultipartApiResponse apiResponse = UploadApiMapper.toApiResponse(response);

        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
