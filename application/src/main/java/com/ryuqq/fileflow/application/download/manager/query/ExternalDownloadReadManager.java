package com.ryuqq.fileflow.application.download.manager.query;

import com.ryuqq.fileflow.application.download.port.out.query.ExternalDownloadQueryPort;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * ExternalDownload 조회 전용 Manager.
 *
 * <p>QueryPort를 래핑하여 Service에서 직접 Port를 호출하지 않도록 합니다.
 *
 * <p><strong>컨벤션</strong>:
 *
 * <ul>
 *   <li>단일 QueryPort 의존성
 *   <li>모든 메서드에 @Transactional(readOnly = true)
 *   <li>Service는 이 Manager를 통해 조회 수행
 * </ul>
 */
@Component
public class ExternalDownloadReadManager {

    private final ExternalDownloadQueryPort externalDownloadQueryPort;

    public ExternalDownloadReadManager(ExternalDownloadQueryPort externalDownloadQueryPort) {
        this.externalDownloadQueryPort = externalDownloadQueryPort;
    }

    @Transactional(readOnly = true)
    public Optional<ExternalDownload> findById(ExternalDownloadId id) {
        return externalDownloadQueryPort.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<ExternalDownload> findByIdAndTenantId(ExternalDownloadId id, String tenantId) {
        return externalDownloadQueryPort.findByIdAndTenantId(id, tenantId);
    }

    @Transactional(readOnly = true)
    public boolean existsById(ExternalDownloadId id) {
        return externalDownloadQueryPort.existsById(id);
    }
}
