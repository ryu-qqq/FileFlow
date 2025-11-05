package com.ryuqq.fileflow.adapter.rest.download.mapper;

import com.ryuqq.fileflow.adapter.rest.download.dto.request.StartDownloadApiRequest;
import com.ryuqq.fileflow.adapter.rest.download.dto.response.DownloadStatusApiResponse;
import com.ryuqq.fileflow.adapter.rest.download.dto.response.StartDownloadApiResponse;
import com.ryuqq.fileflow.application.download.dto.command.StartExternalDownloadCommand;
import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadResponse;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;

/**
 * Download API Mapper
 *
 * <p>REST API Request/Response와 Application Layer Command/Response 간 변환을 담당합니다.</p>
 *
 * <p><strong>변환 규칙:</strong></p>
 * <ul>
 *   <li>API Request → Application Command</li>
 *   <li>Application Response → API Response</li>
 *   <li>헤더 정보(tenantId) 추가</li>
 *   <li>fileName이 없으면 URL에서 추출</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public final class DownloadApiMapper {

    private DownloadApiMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * StartDownloadApiRequest → StartExternalDownloadCommand 변환
     *
     * @param request API Request
     * @param tenantId Tenant ID
     * @param idempotencyKey 멱등키 (중복 요청 방지)
     * @return StartExternalDownloadCommand
     */
    public static StartExternalDownloadCommand toCommand(
        StartDownloadApiRequest request,
        Long tenantId,
        String idempotencyKey
    ) {
        String fileName = request.fileName();

        // fileName이 없으면 URL에서 추출
        if (fileName == null || fileName.isBlank()) {
            fileName = extractFileNameFromUrl(request.sourceUrl());
        }

        return StartExternalDownloadCommand.of(
            idempotencyKey,
            new TenantId(tenantId),
            request.sourceUrl(),
            com.ryuqq.fileflow.domain.upload.FileName.of(fileName)
        );
    }

    /**
     * ExternalDownloadResponse → StartDownloadApiResponse 변환
     *
     * @param response Application Response
     * @return StartDownloadApiResponse
     */
    public static StartDownloadApiResponse toApiResponse(ExternalDownloadResponse response) {
        return StartDownloadApiResponse.of(
            response.downloadId(),
            response.uploadSessionId(),
            response.status()
        );
    }

    /**
     * ExternalDownloadResponse → DownloadStatusApiResponse 변환
     *
     * @param response Application Response
     * @return DownloadStatusApiResponse
     */
    public static DownloadStatusApiResponse toStatusApiResponse(ExternalDownloadResponse response) {
        return DownloadStatusApiResponse.of(
            response.downloadId(),
            response.status(),
            response.sourceUrl(),
            response.uploadSessionId()
        );
    }

    /**
     * URL에서 파일명 추출
     *
     * @param url 소스 URL
     * @return 파일명
     */
    private static String extractFileNameFromUrl(String url) {
        try {
            String path = url.substring(url.lastIndexOf('/') + 1);
            int queryIndex = path.indexOf('?');
            if (queryIndex > 0) {
                path = path.substring(0, queryIndex);
            }
            return path.isBlank() ? "downloaded-file" : path;
        } catch (Exception e) {
            return "downloaded-file";
        }
    }
}
