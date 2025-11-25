package com.ryuqq.fileflow.adapter.in.rest.asset.mapper;

import com.ryuqq.fileflow.adapter.in.rest.asset.dto.query.FileAssetSearchApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.FileAssetApiResponse;
import com.ryuqq.fileflow.application.asset.dto.query.GetFileAssetQuery;
import com.ryuqq.fileflow.application.asset.dto.query.ListFileAssetsQuery;
import com.ryuqq.fileflow.application.asset.dto.response.FileAssetResponse;
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
    public GetFileAssetQuery toGetFileAssetQuery(String id, Long organizationId, Long tenantId) {
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
            FileAssetSearchApiRequest request, Long organizationId, Long tenantId) {
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
}
