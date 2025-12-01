package com.ryuqq.fileflow.application.download.service;

import com.ryuqq.fileflow.application.download.dto.query.GetExternalDownloadQuery;
import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadDetailResponse;
import com.ryuqq.fileflow.application.download.port.in.query.GetExternalDownloadUseCase;
import com.ryuqq.fileflow.application.download.port.out.query.ExternalDownloadQueryPort;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 외부 다운로드 조회 서비스.
 *
 * <p>CQRS Query Side - 외부 다운로드 상세 정보 조회
 */
@Service
@Transactional(readOnly = true)
public class GetExternalDownloadService implements GetExternalDownloadUseCase {

    private final ExternalDownloadQueryPort externalDownloadQueryPort;

    public GetExternalDownloadService(ExternalDownloadQueryPort externalDownloadQueryPort) {
        this.externalDownloadQueryPort = externalDownloadQueryPort;
    }

    @Override
    public ExternalDownloadDetailResponse execute(GetExternalDownloadQuery query) {
        ExternalDownloadId externalDownloadId = ExternalDownloadId.of(query.id());

        ExternalDownload externalDownload =
                externalDownloadQueryPort
                        .findByIdAndTenantId(externalDownloadId, query.tenantId())
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "ExternalDownload를 찾을 수 없습니다: id=" + query.id()));

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
