package com.ryuqq.fileflow.application.download.service.query;

import com.ryuqq.fileflow.application.common.dto.response.PageResponse;
import com.ryuqq.fileflow.application.download.dto.query.ListExternalDownloadsQuery;
import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadDetailResponse;
import com.ryuqq.fileflow.application.download.manager.query.ExternalDownloadReadManager;
import com.ryuqq.fileflow.application.download.port.in.query.GetExternalDownloadsUseCase;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadStatus;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 외부 다운로드 목록 조회 서비스.
 *
 * <p>CQRS Query Side - 외부 다운로드 목록 조회 (페이징, 필터링)
 */
@Service
public class GetExternalDownloadsService implements GetExternalDownloadsUseCase {

    private final ExternalDownloadReadManager externalDownloadReadManager;

    public GetExternalDownloadsService(ExternalDownloadReadManager externalDownloadReadManager) {
        this.externalDownloadReadManager = externalDownloadReadManager;
    }

    @Override
    public PageResponse<ExternalDownloadDetailResponse> execute(ListExternalDownloadsQuery query) {
        ExternalDownloadStatus status = parseStatus(query.status());

        List<ExternalDownload> downloads =
                externalDownloadReadManager.findByCriteria(
                        query.organizationId(),
                        query.tenantId(),
                        status,
                        query.offset(),
                        query.size());

        long totalElements =
                externalDownloadReadManager.countByCriteria(
                        query.organizationId(), query.tenantId(), status);

        List<ExternalDownloadDetailResponse> content =
                downloads.stream().map(this::toDetailResponse).toList();

        int totalPages = (int) Math.ceil((double) totalElements / query.size());
        boolean isFirst = query.page() == 0;
        boolean isLast = query.page() >= totalPages - 1 || totalPages == 0;

        return PageResponse.of(
                content, query.page(), query.size(), totalElements, totalPages, isFirst, isLast);
    }

    private ExternalDownloadStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return ExternalDownloadStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 상태값입니다: " + status);
        }
    }

    private ExternalDownloadDetailResponse toDetailResponse(ExternalDownload externalDownload) {
        return new ExternalDownloadDetailResponse(
                externalDownload.getId().value().toString(),
                externalDownload.getSourceUrl().value(),
                externalDownload.getStatus().name(),
                externalDownload.getFileAssetId() != null
                        ? externalDownload.getFileAssetId().getValue()
                        : null,
                externalDownload.getErrorMessage(),
                externalDownload.getRetryCountValue(),
                externalDownload.hasWebhook() ? externalDownload.getWebhookUrl().value() : null,
                externalDownload.getCreatedAt(),
                externalDownload.getUpdatedAt());
    }
}
