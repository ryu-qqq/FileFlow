package com.ryuqq.fileflow.application.download.service.query;

import com.ryuqq.fileflow.application.download.dto.query.GetExternalDownloadQuery;
import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadDetailResponse;
import com.ryuqq.fileflow.application.download.manager.query.ExternalDownloadReadManager;
import com.ryuqq.fileflow.application.download.port.in.query.GetExternalDownloadUseCase;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.exception.ExternalDownloadNotFoundException;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import org.springframework.stereotype.Service;

/**
 * 외부 다운로드 조회 서비스.
 *
 * <p>CQRS Query Side - 외부 다운로드 상세 정보 조회
 */
@Service
public class GetExternalDownloadService implements GetExternalDownloadUseCase {

    private final ExternalDownloadReadManager externalDownloadReadManager;

    public GetExternalDownloadService(ExternalDownloadReadManager externalDownloadReadManager) {
        this.externalDownloadReadManager = externalDownloadReadManager;
    }

    @Override
    public ExternalDownloadDetailResponse execute(GetExternalDownloadQuery query) {
        ExternalDownloadId externalDownloadId = ExternalDownloadId.of(query.id());

        ExternalDownload externalDownload =
                externalDownloadReadManager
                        .findByIdAndTenantId(externalDownloadId, query.tenantId())
                        .orElseThrow(() -> new ExternalDownloadNotFoundException(query.id()));

        return toDetailResponse(externalDownload);
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
