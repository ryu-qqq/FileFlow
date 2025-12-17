package com.ryuqq.fileflow.application.download.manager.query;

import com.ryuqq.fileflow.application.download.port.out.query.WebhookOutboxQueryPort;
import com.ryuqq.fileflow.domain.download.aggregate.WebhookOutbox;
import com.ryuqq.fileflow.domain.download.vo.WebhookOutboxId;
import com.ryuqq.fileflow.domain.download.vo.WebhookOutboxStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * WebhookOutbox 조회 전용 Manager.
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
public class WebhookOutboxReadManager {

    private final WebhookOutboxQueryPort webhookOutboxQueryPort;

    public WebhookOutboxReadManager(WebhookOutboxQueryPort webhookOutboxQueryPort) {
        this.webhookOutboxQueryPort = webhookOutboxQueryPort;
    }

    /**
     * ID로 WebhookOutbox 조회.
     *
     * @param id WebhookOutbox ID
     * @return WebhookOutbox (없으면 empty)
     */
    @Transactional(readOnly = true)
    public Optional<WebhookOutbox> findById(WebhookOutboxId id) {
        return webhookOutboxQueryPort.findById(id);
    }

    /**
     * 재시도 대상 PENDING Outbox 목록 조회.
     *
     * @param limit 조회 제한 수
     * @return 재시도 대상 WebhookOutbox 목록
     */
    @Transactional(readOnly = true)
    public List<WebhookOutbox> findPendingForRetry(int limit) {
        return webhookOutboxQueryPort.findByStatusForRetry(WebhookOutboxStatus.PENDING, limit);
    }
}
