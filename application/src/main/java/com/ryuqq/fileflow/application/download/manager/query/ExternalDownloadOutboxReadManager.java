package com.ryuqq.fileflow.application.download.manager.query;

import com.ryuqq.fileflow.application.download.port.out.query.ExternalDownloadOutboxQueryPort;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * ExternalDownloadOutbox 조회 전용 Manager.
 *
 * <p>QueryPort를 래핑하여 Service/Scheduler/Listener에서 직접 Port를 호출하지 않도록 합니다.
 *
 * <p><strong>컨벤션</strong>:
 *
 * <ul>
 *   <li>단일 QueryPort 의존성
 *   <li>모든 메서드에 @Transactional(readOnly = true)
 *   <li>Service/Scheduler/Listener는 이 Manager를 통해 조회 수행
 * </ul>
 */
@Component
public class ExternalDownloadOutboxReadManager {

    private final ExternalDownloadOutboxQueryPort externalDownloadOutboxQueryPort;

    public ExternalDownloadOutboxReadManager(
            ExternalDownloadOutboxQueryPort externalDownloadOutboxQueryPort) {
        this.externalDownloadOutboxQueryPort = externalDownloadOutboxQueryPort;
    }

    @Transactional(readOnly = true)
    public Optional<ExternalDownloadOutbox> findByExternalDownloadId(
            ExternalDownloadId externalDownloadId) {
        return externalDownloadOutboxQueryPort.findByExternalDownloadId(externalDownloadId);
    }

    @Transactional(readOnly = true)
    public List<ExternalDownloadOutbox> findUnpublished(int limit) {
        return externalDownloadOutboxQueryPort.findUnpublished(limit);
    }
}
