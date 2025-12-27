package com.ryuqq.fileflow.adapter.in.rest.download.mapper;

import com.ryuqq.fileflow.adapter.in.rest.download.dto.command.RequestExternalDownloadApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.download.dto.query.ExternalDownloadSearchApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.download.dto.response.ExternalDownloadApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.download.dto.response.ExternalDownloadDetailApiResponse;
import com.ryuqq.fileflow.application.download.dto.command.RequestExternalDownloadCommand;
import com.ryuqq.fileflow.application.download.dto.query.GetExternalDownloadQuery;
import com.ryuqq.fileflow.application.download.dto.query.ListExternalDownloadsQuery;
import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadDetailResponse;
import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadResponse;
import org.springframework.stereotype.Component;

/**
 * ExternalDownload API Mapper.
 *
 * <p>API DTO와 Application Command/Query/Response 간 변환을 담당합니다.
 */
@Component
public class ExternalDownloadApiMapper {

    /**
     * API Request를 Application Command로 변환합니다.
     *
     * @param request API Request
     * @param tenantId 테넌트 ID (UUIDv7 문자열)
     * @param organizationId 조직 ID (UUIDv7 문자열)
     * @return RequestExternalDownloadCommand
     */
    public RequestExternalDownloadCommand toCommand(
            RequestExternalDownloadApiRequest request, String tenantId, String organizationId) {
        return new RequestExternalDownloadCommand(
                request.idempotencyKey(),
                request.sourceUrl(),
                tenantId,
                organizationId,
                request.webhookUrl());
    }

    /**
     * 조회용 Query를 생성합니다.
     *
     * @param id ExternalDownload ID (UUID 문자열)
     * @param tenantId 테넌트 ID (UUIDv7 문자열)
     * @return GetExternalDownloadQuery
     */
    public GetExternalDownloadQuery toQuery(String id, String tenantId) {
        return new GetExternalDownloadQuery(id, tenantId);
    }

    /**
     * Application Response를 API Response로 변환합니다.
     *
     * @param response Application Response
     * @return ExternalDownloadApiResponse
     */
    public ExternalDownloadApiResponse toApiResponse(ExternalDownloadResponse response) {
        return new ExternalDownloadApiResponse(
                response.id(), response.status(), response.createdAt());
    }

    /**
     * Application Detail Response를 API Response로 변환합니다.
     *
     * @param response Application Detail Response
     * @return ExternalDownloadDetailApiResponse
     */
    public ExternalDownloadDetailApiResponse toDetailApiResponse(
            ExternalDownloadDetailResponse response) {
        return new ExternalDownloadDetailApiResponse(
                response.id(),
                response.sourceUrl(),
                response.status(),
                response.fileAssetId(),
                response.errorMessage(),
                response.retryCount(),
                response.webhookUrl(),
                response.createdAt(),
                response.updatedAt());
    }

    /**
     * 목록 조회용 Query를 생성합니다.
     *
     * @param request API Request
     * @param organizationId 조직 ID (UUIDv7 문자열)
     * @param tenantId 테넌트 ID (UUIDv7 문자열)
     * @return ListExternalDownloadsQuery
     */
    public ListExternalDownloadsQuery toListQuery(
            ExternalDownloadSearchApiRequest request, String organizationId, String tenantId) {
        return ListExternalDownloadsQuery.of(
                organizationId, tenantId, request.status(), request.page(), request.size());
    }
}
