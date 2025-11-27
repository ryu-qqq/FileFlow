package com.ryuqq.fileflow.adapter.in.rest.asset.mapper;

import com.ryuqq.fileflow.adapter.in.rest.asset.dto.command.BatchGenerateDownloadUrlApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.command.DeleteFileAssetApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.command.GenerateDownloadUrlApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.query.FileAssetSearchApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.BatchDownloadUrlApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.DeleteFileAssetApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.DownloadUrlApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.FileAssetApiResponse;
import com.ryuqq.fileflow.application.asset.dto.command.BatchGenerateDownloadUrlCommand;
import com.ryuqq.fileflow.application.asset.dto.command.DeleteFileAssetCommand;
import com.ryuqq.fileflow.application.asset.dto.command.GenerateDownloadUrlCommand;
import com.ryuqq.fileflow.application.asset.dto.query.GetFileAssetQuery;
import com.ryuqq.fileflow.application.asset.dto.query.ListFileAssetsQuery;
import com.ryuqq.fileflow.application.asset.dto.response.BatchDownloadUrlResponse;
import com.ryuqq.fileflow.application.asset.dto.response.DeleteFileAssetResponse;
import com.ryuqq.fileflow.application.asset.dto.response.DownloadUrlResponse;
import com.ryuqq.fileflow.application.asset.dto.response.FileAssetResponse;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * FileAsset API Mapper.
 *
 * <p>API DTO ↔ UseCase DTO 변환을 담당합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class FileAssetApiMapper {

    /**
     * 단건 조회 Query 변환.
     *
     * @param id 파일 자산 ID
     * @param organizationId 조직 ID
     * @param tenantId 테넌트 ID
     * @return GetFileAssetQuery
     */
    public GetFileAssetQuery toGetFileAssetQuery(String id, long organizationId, long tenantId) {
        return GetFileAssetQuery.of(id, organizationId, tenantId);
    }

    /**
     * 목록 조회 Query 변환.
     *
     * @param request 검색 API Request
     * @param organizationId 조직 ID
     * @param tenantId 테넌트 ID
     * @return ListFileAssetsQuery
     */
    public ListFileAssetsQuery toListFileAssetsQuery(
            FileAssetSearchApiRequest request, long organizationId, long tenantId) {
        return ListFileAssetsQuery.of(
                organizationId,
                tenantId,
                request.status(),
                request.category(),
                request.page(),
                request.size());
    }

    /**
     * UseCase Response → API Response 변환.
     *
     * @param response UseCase Response
     * @return FileAssetApiResponse
     */
    public FileAssetApiResponse toApiResponse(FileAssetResponse response) {
        return new FileAssetApiResponse(
                response.id(),
                response.sessionId(),
                response.fileName(),
                response.fileSize(),
                response.contentType(),
                response.category(),
                response.bucket(),
                response.s3Key(),
                response.etag(),
                response.status(),
                response.createdAt(),
                response.processedAt());
    }

    /**
     * 삭제 Command 변환.
     *
     * @param fileAssetId 파일 자산 ID
     * @param request 삭제 API Request
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @return DeleteFileAssetCommand
     */
    public DeleteFileAssetCommand toDeleteFileAssetCommand(
            String fileAssetId,
            DeleteFileAssetApiRequest request,
            long tenantId,
            long organizationId) {
        return DeleteFileAssetCommand.of(
                fileAssetId, tenantId, organizationId, request != null ? request.reason() : null);
    }

    /**
     * 삭제 UseCase Response → API Response 변환.
     *
     * @param response UseCase Response
     * @return DeleteFileAssetApiResponse
     */
    public DeleteFileAssetApiResponse toDeleteApiResponse(DeleteFileAssetResponse response) {
        return DeleteFileAssetApiResponse.of(response.id(), response.processedAt());
    }

    /**
     * Download URL 생성 Command 변환.
     *
     * @param fileAssetId 파일 자산 ID
     * @param request API Request
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @return GenerateDownloadUrlCommand
     */
    public GenerateDownloadUrlCommand toGenerateDownloadUrlCommand(
            String fileAssetId,
            GenerateDownloadUrlApiRequest request,
            long tenantId,
            long organizationId) {
        int expirationMinutes = request != null ? request.expirationMinutes() : 60;
        return GenerateDownloadUrlCommand.of(
                fileAssetId, tenantId, organizationId, expirationMinutes);
    }

    /**
     * Batch Download URL 생성 Command 변환.
     *
     * @param request API Request
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @return BatchGenerateDownloadUrlCommand
     */
    public BatchGenerateDownloadUrlCommand toBatchGenerateDownloadUrlCommand(
            BatchGenerateDownloadUrlApiRequest request, long tenantId, long organizationId) {
        return BatchGenerateDownloadUrlCommand.of(
                request.fileAssetIds(), tenantId, organizationId, request.expirationMinutes());
    }

    /**
     * Download URL UseCase Response → API Response 변환.
     *
     * @param response UseCase Response
     * @return DownloadUrlApiResponse
     */
    public DownloadUrlApiResponse toDownloadUrlApiResponse(DownloadUrlResponse response) {
        return DownloadUrlApiResponse.of(
                response.fileAssetId(),
                response.downloadUrl(),
                response.fileName(),
                response.contentType(),
                response.fileSize(),
                response.expiresAt());
    }

    /**
     * Batch Download URL UseCase Response → API Response 변환.
     *
     * @param response UseCase Response
     * @return BatchDownloadUrlApiResponse
     */
    public BatchDownloadUrlApiResponse toBatchDownloadUrlApiResponse(
            BatchDownloadUrlResponse response) {
        List<DownloadUrlApiResponse> downloadUrls =
                response.downloadUrls().stream().map(this::toDownloadUrlApiResponse).toList();

        List<BatchDownloadUrlApiResponse.FailedDownloadUrl> failures =
                response.failures().stream()
                        .map(
                                f ->
                                        BatchDownloadUrlApiResponse.FailedDownloadUrl.of(
                                                f.fileAssetId(), f.errorCode(), f.errorMessage()))
                        .toList();

        return BatchDownloadUrlApiResponse.of(downloadUrls, failures);
    }
}
