package com.ryuqq.fileflow.adapter.rest.file.controller;

import com.ryuqq.fileflow.adapter.rest.common.dto.ApiResponse;
import com.ryuqq.fileflow.adapter.rest.common.dto.PageApiResponse;
import com.ryuqq.fileflow.adapter.rest.file.dto.request.FilesSearchApiRequest;
import com.ryuqq.fileflow.adapter.rest.file.dto.request.GenerateDownloadUrlApiRequest;
import com.ryuqq.fileflow.adapter.rest.file.dto.response.DownloadUrlApiResponse;
import com.ryuqq.fileflow.adapter.rest.file.dto.response.FileDetailApiResponse;
import com.ryuqq.fileflow.adapter.rest.file.dto.response.FileMetadataApiResponse;
import com.ryuqq.fileflow.adapter.rest.file.mapper.FileApiMapper;
import com.ryuqq.fileflow.application.file.dto.command.DeleteFileCommand;
import com.ryuqq.fileflow.application.file.dto.command.GenerateDownloadUrlCommand;
import com.ryuqq.fileflow.application.file.dto.query.FileMetadataQuery;
import com.ryuqq.fileflow.application.file.dto.query.ListFilesQuery;
import com.ryuqq.fileflow.application.file.dto.response.DownloadUrlResponse;
import com.ryuqq.fileflow.application.file.dto.response.FileListResponse;
import com.ryuqq.fileflow.application.file.dto.response.FileMetadataResponse;
import com.ryuqq.fileflow.application.file.port.in.DeleteFileUseCase;
import com.ryuqq.fileflow.application.file.port.in.GenerateDownloadUrlUseCase;
import com.ryuqq.fileflow.application.file.port.in.GetFileMetadataUseCase;
import com.ryuqq.fileflow.application.file.port.in.GetFilesUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * File Controller
 *
 * <p>File Management REST API 엔드포인트를 제공합니다.</p>
 *
 * <p><strong>제공 기능 (CQRS 분리):</strong></p>
 * <ul>
 *   <li><strong>Command (쓰기)</strong>:
 *     <ul>
 *       <li>POST /api/v1/files/{fileId}/download-url - 다운로드 URL 생성</li>
 *       <li>DELETE /api/v1/files/{fileId} - 파일 삭제 (Soft Delete)</li>
 *     </ul>
 *   </li>
 *   <li><strong>Query (읽기)</strong>:
 *     <ul>
 *       <li>GET /api/v1/files/{fileId} - 파일 메타데이터 조회</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p><strong>공통 헤더:</strong></p>
 * <ul>
 *   <li>{@code X-Tenant-Id}: 테넌트 ID (필수)</li>
 *   <li>{@code X-Organization-Id}: 조직 ID (선택)</li>
 *   <li>{@code X-User-Id}: 사용자 ID (삭제 시 필수, 권한 검증용)</li>
 * </ul>
 *
 * <p><strong>REST API Controller 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Thin Controller - 비즈니스 로직 없음</li>
 *   <li>✅ Use Case 호출만 담당</li>
 *   <li>✅ {@code @Valid} 검증 적용</li>
 *   <li>✅ 적절한 HTTP 상태 코드 반환</li>
 *   <li>✅ DTO Mapper를 통한 변환</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ Long FK 전략 적용</li>
 *   <li>✅ CQRS 분리 (Command/Query UseCase 분리)</li>
 * </ul>
 *
 * <p><strong>Error Handling:</strong></p>
 * <ul>
 *   <li>400 Bad Request: Validation 실패</li>
 *   <li>401 Unauthorized: 인증 실패</li>
 *   <li>403 Forbidden: 권한 없음 (파일 소유자 아님)</li>
 *   <li>404 Not Found: 파일이 존재하지 않음</li>
 *   <li>410 Gone: 파일이 삭제됨</li>
 *   <li>500 Internal Server Error: 서버 내부 오류</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@RestController
@RequestMapping("${api.endpoints.base-v1}${api.endpoints.file.base}")
@Validated
public class FileController {

    private final GenerateDownloadUrlUseCase generateDownloadUrlUseCase;
    private final DeleteFileUseCase deleteFileUseCase;
    private final GetFileMetadataUseCase getFileMetadataUseCase;
    private final GetFilesUseCase getFilesUseCase;

    /**
     * 생성자
     *
     * @param generateDownloadUrlUseCase 다운로드 URL 생성 UseCase
     * @param deleteFileUseCase 파일 삭제 UseCase
     * @param getFileMetadataUseCase 파일 메타데이터 조회 UseCase
     * @param getFilesUseCase 파일 목록 조회 UseCase
     */
    public FileController(
        GenerateDownloadUrlUseCase generateDownloadUrlUseCase,
        DeleteFileUseCase deleteFileUseCase,
        GetFileMetadataUseCase getFileMetadataUseCase,
        GetFilesUseCase getFilesUseCase
    ) {
        this.generateDownloadUrlUseCase = generateDownloadUrlUseCase;
        this.deleteFileUseCase = deleteFileUseCase;
        this.getFileMetadataUseCase = getFileMetadataUseCase;
        this.getFilesUseCase = getFilesUseCase;
    }

    /**
     * 파일 다운로드 URL 생성 (Command)
     *
     * <p><strong>엔드포인트:</strong> {@code POST /api/v1/files/{fileId}/download-url}</p>
     *
     * <p><strong>요청 예시:</strong></p>
     * <pre>{@code
     * POST /api/v1/files/123/download-url
     * Headers:
     *   X-Tenant-Id: 1
     *   X-Organization-Id: 2 (optional)
     * Body:
     * {
     *   "fileId": 123,
     *   "expirationHours": 2
     * }
     * }</pre>
     *
     * <p><strong>응답 예시:</strong></p>
     * <pre>{@code
     * {
     *   "success": true,
     *   "data": {
     *     "fileId": 123,
     *     "fileName": "document.pdf",
     *     "downloadUrl": "https://bucket.s3.region.amazonaws.com/key?X-Amz-Signature=...",
     *     "expiresAt": "2024-10-31T14:00:00"
     *   },
     *   "error": null,
     *   "timestamp": "2024-10-31T12:00:00"
     * }
     * }</pre>
     *
     * @param fileId File ID (Path Variable)
     * @param request API Request
     * @param tenantId Tenant ID (헤더)
     * @param organizationId Organization ID (헤더, 선택)
     * @return DownloadUrlApiResponse (200 OK)
     * @throws IllegalArgumentException 파일이 존재하지 않는 경우
     * @throws IllegalStateException 파일 상태가 AVAILABLE이 아닌 경우
     */
    @PostMapping("/{fileId}/download-url")
    public ResponseEntity<ApiResponse<DownloadUrlApiResponse>> generateDownloadUrl(
        @PathVariable @NotNull Long fileId,
        @RequestBody @Valid GenerateDownloadUrlApiRequest request,
        @RequestHeader("X-Tenant-Id") @NotNull Long tenantId,
        @RequestHeader(value = "X-Organization-Id", required = false) Long organizationId
    ) {
        // Path Variable과 Request Body의 fileId 일치 검증
        if (!fileId.equals(request.getFileId())) {
            throw new IllegalArgumentException(
                String.format(
                    "Path variable fileId (%d) does not match request body fileId (%d)",
                    fileId,
                    request.getFileId()
                )
            );
        }

        // 1. API Request → Application Command 변환
        GenerateDownloadUrlCommand command = FileApiMapper.toGenerateDownloadUrlCommand(
            request,
            tenantId,
            organizationId
        );

        // 2. UseCase 실행
        DownloadUrlResponse response = generateDownloadUrlUseCase.execute(command);

        // 3. Application Response → API Response 변환
        DownloadUrlApiResponse apiResponse = FileApiMapper.toDownloadUrlApiResponse(response);

        // 4. API Response 반환
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * 파일 삭제 (Command - Soft Delete)
     *
     * <p><strong>엔드포인트:</strong> {@code DELETE /api/v1/files/{fileId}}</p>
     *
     * <p><strong>요청 예시:</strong></p>
     * <pre>{@code
     * DELETE /api/v1/files/123
     * Headers:
     *   X-Tenant-Id: 1
     *   X-Organization-Id: 2 (optional)
     *   X-User-Id: 100 (required, 권한 검증용)
     * }</pre>
     *
     * <p><strong>응답 예시:</strong></p>
     * <pre>{@code
     * 204 No Content
     * }</pre>
     *
     * <p><strong>삭제 규칙:</strong></p>
     * <ul>
     *   <li>Soft Delete: deleted_at 타임스탬프 설정</li>
     *   <li>물리 삭제는 별도 Batch Job에서 처리</li>
     *   <li>삭제된 파일은 조회 불가</li>
     *   <li>SELF 권한 검증: requesterId == ownerUserId</li>
     * </ul>
     *
     * @param fileId File ID (Path Variable)
     * @param tenantId Tenant ID (헤더)
     * @param organizationId Organization ID (헤더, 선택)
     * @param requesterId Requester User ID (헤더, 권한 검증용)
     * @return 204 No Content
     * @throws IllegalArgumentException 파일이 존재하지 않는 경우
     * @throws IllegalStateException 삭제 권한 없음 또는 이미 삭제된 파일
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
        @PathVariable @NotNull Long fileId,
        @RequestHeader("X-Tenant-Id") @NotNull Long tenantId,
        @RequestHeader(value = "X-Organization-Id", required = false) Long organizationId,
        @RequestHeader("X-User-Id") @NotNull Long requesterId
    ) {
        // 1. DeleteFileCommand 생성
        DeleteFileCommand command = FileApiMapper.toDeleteFileCommand(
            fileId,
            tenantId,
            organizationId,
            requesterId
        );

        // 2. UseCase 실행
        deleteFileUseCase.execute(command);

        // 3. 204 No Content 반환
        return ResponseEntity.noContent().build();
    }

    /**
     * 파일 메타데이터 조회 (Query)
     *
     * <p><strong>엔드포인트:</strong> {@code GET /api/v1/files/{fileId}}</p>
     *
     * <p><strong>요청 예시:</strong></p>
     * <pre>{@code
     * GET /api/v1/files/123
     * Headers:
     *   X-Tenant-Id: 1
     *   X-Organization-Id: 2 (optional)
     * }</pre>
     *
     * <p><strong>응답 예시:</strong></p>
     * <pre>{@code
     * {
     *   "success": true,
     *   "data": {
     *     "fileId": 123,
     *     "fileName": "document.pdf",
     *     "fileSize": 1024000,
     *     "contentType": "application/pdf",
     *     "status": "AVAILABLE",
     *     "visibility": "PRIVATE",
     *     "storageKey": "tenant-1/org-2/file-123.pdf",
     *     "uploadedAt": "2024-10-31T12:00:00",
     *     "expiresAt": "2024-11-30T12:00:00"
     *   },
     *   "error": null,
     *   "timestamp": "2024-10-31T12:00:00"
     * }
     * }</pre>
     *
     * @param fileId File ID (Path Variable)
     * @param tenantId Tenant ID (헤더)
     * @param organizationId Organization ID (헤더, 선택)
     * @return FileMetadataApiResponse (200 OK)
     * @throws IllegalArgumentException 파일이 존재하지 않는 경우
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<ApiResponse<FileMetadataApiResponse>> getFileMetadata(
        @PathVariable @NotNull Long fileId,
        @RequestHeader("X-Tenant-Id") @NotNull Long tenantId,
        @RequestHeader(value = "X-Organization-Id", required = false) Long organizationId
    ) {
        // 1. FileMetadataQuery 생성
        FileMetadataQuery query = FileApiMapper.toFileMetadataQuery(
            fileId,
            tenantId,
            organizationId
        );

        // 2. UseCase 실행
        FileMetadataResponse response = getFileMetadataUseCase.execute(query);

        // 3. Application Response → API Response 변환
        FileMetadataApiResponse apiResponse = FileApiMapper.toFileMetadataApiResponse(response);

        // 4. API Response 반환
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * 파일 상세 조회 (Query - Variant 정보 포함)
     *
     * <p><strong>엔드포인트:</strong> {@code GET /api/v1/files/{fileId}/details}</p>
     *
     * <p><strong>요청 예시:</strong></p>
     * <pre>{@code
     * GET /api/v1/files/123/details
     * Headers:
     *   X-Tenant-Id: 1
     *   X-Organization-Id: 2 (optional)
     * }</pre>
     *
     * <p><strong>응답 예시:</strong></p>
     * <pre>{@code
     * {
     *   "success": true,
     *   "data": {
     *     "fileId": 123,
     *     "fileName": "profile.jpg",
     *     "fileSize": 5242880,
     *     "contentType": "image/jpeg",
     *     "status": "AVAILABLE",
     *     "visibility": "PUBLIC",
     *     "ownerUserId": 100,
     *     "tenantId": "1",
     *     "organizationId": 2,
     *     "uploadSessionKey": null,
     *     "checksum": "d41d8cd98f00b204e9800998ecf8427e",
     *     "original": {
     *       "type": "ORIGINAL",
     *       "storageKey": "tenant-1/org-2/file-123.jpg",
     *       "url": null,
     *       "width": null,
     *       "height": null,
     *       "fileSize": 5242880
     *     },
     *     "variants": [],
     *     "metadata": {},
     *     "uploadedAt": "2024-10-31T12:00:00",
     *     "processedAt": "2024-10-31T12:01:00",
     *     "expiresAt": "2024-11-30T12:00:00",
     *     "retentionDays": 30
     *   },
     *   "error": null,
     *   "timestamp": "2024-10-31T12:00:00"
     * }
     * }</pre>
     *
     * @param fileId File ID (Path Variable)
     * @param tenantId Tenant ID (헤더)
     * @param organizationId Organization ID (헤더, 선택)
     * @return FileDetailApiResponse (200 OK)
     * @throws IllegalArgumentException 파일이 존재하지 않는 경우
     */
    @GetMapping("/{fileId}/details")
    public ResponseEntity<ApiResponse<FileDetailApiResponse>> getFileDetails(
        @PathVariable @NotNull Long fileId,
        @RequestHeader("X-Tenant-Id") @NotNull Long tenantId,
        @RequestHeader(value = "X-Organization-Id", required = false) Long organizationId
    ) {
        // 1. FileMetadataQuery 생성 (기존 UseCase 재사용)
        FileMetadataQuery query = FileApiMapper.toFileMetadataQuery(
            fileId,
            tenantId,
            organizationId
        );

        // 2. UseCase 실행
        FileMetadataResponse response = getFileMetadataUseCase.execute(query);

        // 3. Application Response → API Response 변환 (상세 정보 포함)
        FileDetailApiResponse apiResponse = FileApiMapper.toFileDetailApiResponse(response);

        // 4. API Response 반환
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * 파일 목록 조회 (Query - Pagination)
     *
     * <p><strong>엔드포인트:</strong> {@code GET /api/v1/files}</p>
     *
     * <p><strong>요청 예시:</strong></p>
     * <pre>{@code
     * GET /api/v1/files?ownerUserId=100&status=AVAILABLE&page=0&size=20
     * Headers:
     *   X-Tenant-Id: 1
     *   X-Organization-Id: 2 (optional)
     * }</pre>
     *
     * <p><strong>응답 예시:</strong></p>
     * <pre>{@code
     * {
     *   "success": true,
     *   "data": {
     *     "content": [
     *       {
     *         "fileId": 123,
     *         "fileName": "document.pdf",
     *         "fileSize": 1024000,
     *         "contentType": "application/pdf",
     *         "status": "AVAILABLE",
     *         "visibility": "PRIVATE",
     *         "storageKey": "tenant-1/org-2/file-123.pdf",
     *         "uploadedAt": "2024-10-31T12:00:00",
     *         "expiresAt": "2024-11-30T12:00:00"
     *       }
     *     ],
     *     "page": 0,
     *     "size": 20,
     *     "totalElements": 100,
     *     "totalPages": 5,
     *     "first": true,
     *     "last": false
     *   },
     *   "error": null,
     *   "timestamp": "2024-10-31T12:00:00"
     * }
     * }</pre>
     *
     * <p><strong>필터링 조건:</strong></p>
     * <ul>
     *   <li>ownerUserId: 파일 소유자 ID (선택)</li>
     *   <li>status: 파일 상태 (AVAILABLE, PROCESSING, DELETED 등, 선택)</li>
     *   <li>visibility: 가시성 (PRIVATE, INTERNAL, PUBLIC, 선택)</li>
     *   <li>page: 페이지 번호 (기본값: 0, min: 0)</li>
     *   <li>size: 페이지 크기 (기본값: 20, min: 1, max: 100)</li>
     * </ul>
     *
     * @param request 파일 목록 조회 요청 (쿼리 파라미터)
     * @param tenantId Tenant ID (헤더)
     * @param organizationId Organization ID (헤더, 선택)
     * @return PageApiResponse&lt;FileMetadataApiResponse&gt; (200 OK)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageApiResponse<FileMetadataApiResponse>>> getFiles(
        @Valid FilesSearchApiRequest request,
        @RequestHeader("X-Tenant-Id") @NotNull Long tenantId,
        @RequestHeader(value = "X-Organization-Id", required = false) Long organizationId
    ) {
        // 1. API Request → Application Query 변환
        ListFilesQuery query = FileApiMapper.toListFilesQuery(
            request,
            tenantId,
            organizationId
        );

        // 2. UseCase 실행
        FileListResponse response = getFilesUseCase.execute(query);

        // 3. Application Response → API Response 변환 (PageApiResponse 래핑)
        PageApiResponse<FileMetadataApiResponse> apiResponse = FileApiMapper.toFileListApiResponse(response);

        // 4. API Response 반환
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
