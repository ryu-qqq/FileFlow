package com.ryuqq.fileflow.adapter.rest.file.mapper;

import com.ryuqq.fileflow.adapter.rest.common.dto.PageApiResponse;
import com.ryuqq.fileflow.adapter.rest.file.dto.request.FilesSearchApiRequest;
import com.ryuqq.fileflow.adapter.rest.file.dto.request.GenerateDownloadUrlApiRequest;
import com.ryuqq.fileflow.adapter.rest.file.dto.response.DownloadUrlApiResponse;
import com.ryuqq.fileflow.adapter.rest.file.dto.response.FileDetailApiResponse;
import com.ryuqq.fileflow.adapter.rest.file.dto.response.FileMetadataApiResponse;
import com.ryuqq.fileflow.adapter.rest.file.dto.response.FileVariantInfo;
import com.ryuqq.fileflow.application.file.dto.command.DeleteFileCommand;
import com.ryuqq.fileflow.application.file.dto.command.GenerateDownloadUrlCommand;
import com.ryuqq.fileflow.application.file.dto.query.FileMetadataQuery;
import com.ryuqq.fileflow.application.file.dto.query.ListFilesQuery;
import com.ryuqq.fileflow.application.file.dto.response.DownloadUrlResponse;
import com.ryuqq.fileflow.application.file.dto.response.FileListResponse;
import com.ryuqq.fileflow.application.file.dto.response.FileMetadataResponse;
import com.ryuqq.fileflow.domain.file.asset.FileId;
import com.ryuqq.fileflow.domain.file.asset.FileStatus;
import com.ryuqq.fileflow.domain.file.asset.Visibility;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * File API Mapper
 *
 * <p>REST API DTO와 Application DTO 간 변환을 담당하는 Mapper입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>API Request → Application Command/Query 변환</li>
 *   <li>Application Response → API Response 변환</li>
 *   <li>헤더 값 추출 및 VO 생성</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ Static Factory Method 패턴</li>
 *   <li>✅ Long FK 전략 (tenantId, organizationId는 Long)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public final class FileApiMapper {

    private FileApiMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * API Request → GenerateDownloadUrlCommand 변환
     *
     * @param request API Request
     * @param tenantId Tenant ID (헤더)
     * @param organizationId Organization ID (헤더, 선택)
     * @return GenerateDownloadUrlCommand
     */
    public static GenerateDownloadUrlCommand toGenerateDownloadUrlCommand(
        GenerateDownloadUrlApiRequest request,
        Long tenantId,
        Long organizationId
    ) {
        return GenerateDownloadUrlCommand.of(
            FileId.of(request.getFileId()),
            TenantId.of(tenantId),
            organizationId,
            Duration.ofHours(request.getExpirationHours())
        );
    }

    /**
     * DownloadUrlResponse → API Response 변환
     *
     * @param response Application Response
     * @return DownloadUrlApiResponse
     */
    public static DownloadUrlApiResponse toDownloadUrlApiResponse(DownloadUrlResponse response) {
        return DownloadUrlApiResponse.of(
            response.fileId(),
            response.fileName(),
            response.downloadUrl(),
            response.expiresAt()
        );
    }

    /**
     * DeleteFileCommand 생성
     *
     * @param fileId File ID
     * @param tenantId Tenant ID (헤더)
     * @param organizationId Organization ID (헤더, 선택)
     * @param requesterId Requester User ID (헤더)
     * @return DeleteFileCommand
     */
    public static DeleteFileCommand toDeleteFileCommand(
        Long fileId,
        Long tenantId,
        Long organizationId,
        Long requesterId
    ) {
        return DeleteFileCommand.of(
            FileId.of(fileId),
            TenantId.of(tenantId),
            organizationId,
            requesterId
        );
    }

    /**
     * FileMetadataQuery 생성
     *
     * @param fileId File ID
     * @param tenantId Tenant ID (헤더)
     * @param organizationId Organization ID (헤더, 선택)
     * @return FileMetadataQuery
     */
    public static FileMetadataQuery toFileMetadataQuery(
        Long fileId,
        Long tenantId,
        Long organizationId
    ) {
        return FileMetadataQuery.of(
            FileId.of(fileId),
            TenantId.of(tenantId),
            organizationId
        );
    }

    /**
     * FileMetadataResponse → API Response 변환
     *
     * @param response Application Response
     * @return FileMetadataApiResponse
     */
    public static FileMetadataApiResponse toFileMetadataApiResponse(FileMetadataResponse response) {
        return FileMetadataApiResponse.of(
            response.fileId(),
            response.fileName(),
            response.fileSize(),
            response.mimeType(),
            response.status().name(),
            response.visibility().name(),
            response.storageKey(),
            response.uploadedAt(),
            response.expiresAt()
        );
    }

    /**
     * API Request → ListFilesQuery 변환
     *
     * @param request API Request (쿼리 파라미터)
     * @param tenantId Tenant ID (헤더)
     * @param organizationId Organization ID (헤더, 선택)
     * @return ListFilesQuery
     */
    public static ListFilesQuery toListFilesQuery(
        FilesSearchApiRequest request,
        Long tenantId,
        Long organizationId
    ) {
        // String → Enum 변환 (null 안전)
        FileStatus status = request.getStatus() != null
            ? FileStatus.valueOf(request.getStatus().toUpperCase())
            : null;

        Visibility visibility = request.getVisibility() != null
            ? Visibility.valueOf(request.getVisibility().toUpperCase())
            : null;

        return new ListFilesQuery(
            TenantId.of(tenantId),
            organizationId,
            request.getOwnerUserId(),
            status,
            visibility,
            null, // uploadedAfter (현재 API에서 미지원)
            null, // uploadedBefore (현재 API에서 미지원)
            request.getPage(),
            request.getSize()
        );
    }

    /**
     * FileListResponse → PageApiResponse 변환
     *
     * <p>Application Layer의 FileListResponse를 PageApiResponse로 래핑하여 반환합니다.</p>
     * <p>각 FileMetadataResponse는 FileMetadataApiResponse로 변환됩니다.</p>
     *
     * @param response Application Response
     * @return PageApiResponse&lt;FileMetadataApiResponse&gt;
     */
    public static PageApiResponse<FileMetadataApiResponse> toFileListApiResponse(FileListResponse response) {
        // PageApiResponse.from() 메서드를 사용하여 매퍼 함수 적용
        return PageApiResponse.from(
            // Application PageResponse 생성 (FileListResponse를 PageResponse로 변환)
            new com.ryuqq.fileflow.application.common.dto.PageResponse<>(
                response.content(),
                response.page(),
                response.size(),
                response.totalElements(),
                response.totalPages(),
                response.page() == 0,  // first
                !response.hasNext()     // last
            ),
            // 매퍼 함수: FileMetadataResponse → FileMetadataApiResponse
            FileApiMapper::toFileMetadataApiResponse
        );
    }

    /**
     * FileMetadataResponse → FileDetailApiResponse 변환
     *
     * <p>파일 상세 조회 응답으로 변환합니다. Variant 정보는 현재 미구현 상태입니다.</p>
     *
     * @param response Application Response
     * @return FileDetailApiResponse
     */
    public static FileDetailApiResponse toFileDetailApiResponse(FileMetadataResponse response) {
        // 원본 정보 생성 (현재는 storageKey만 사용, URL은 향후 CDN 연동 시 추가)
        FileVariantInfo original = FileVariantInfo.of(
            "ORIGINAL",
            response.storageKey(),
            null,  // URL은 향후 CDN 연동 시 추가
            null,  // width는 이미지 메타데이터 추출 시 추가
            null,  // height는 이미지 메타데이터 추출 시 추가
            response.fileSize()
        );

        // Variants는 현재 빈 리스트 (향후 이미지 변형본 생성 기능 추가 시 구현)
        List<FileVariantInfo> variants = List.of();

        // Metadata는 현재 빈 맵 (향후 커스텀 메타데이터 기능 추가 시 구현)
        Map<String, String> metadata = Map.of();

        return FileDetailApiResponse.of(
            response.fileId(),
            response.fileName(),
            response.fileSize(),
            response.mimeType(),
            response.status().name(),
            response.visibility().name(),
            response.ownerUserId(),
            response.tenantId(),
            response.organizationId(),
            null,  // uploadSessionKey는 향후 추가
            response.checksum(),
            original,
            variants,
            metadata,
            response.uploadedAt(),
            response.processedAt(),
            response.expiresAt(),
            response.retentionDays()
        );
    }
}
