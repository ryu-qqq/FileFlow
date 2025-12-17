package com.ryuqq.fileflow.application.download.port.out.query;

import com.ryuqq.fileflow.domain.download.aggregate.WebhookOutbox;
import com.ryuqq.fileflow.domain.download.vo.WebhookOutboxId;
import com.ryuqq.fileflow.domain.download.vo.WebhookOutboxStatus;
import java.util.List;
import java.util.Optional;

/**
 * WebhookOutbox Query Port.
 *
 * <p>CQRS Query Side - Webhook Outbox 조회
 */
public interface WebhookOutboxQueryPort {

    /**
     * ID로 WebhookOutbox 조회.
     *
     * @param id WebhookOutbox ID
     * @return WebhookOutbox (없으면 empty)
     */
    Optional<WebhookOutbox> findById(WebhookOutboxId id);

    /**
     * 특정 상태의 재시도 대상 Outbox 목록 조회.
     *
     * <p>상태가 PENDING이고 재시도 횟수가 최대 재시도 횟수 미만인 Outbox를 조회합니다.
     *
     * @param status 조회할 상태
     * @param limit 조회 제한 수
     * @return 재시도 대상 WebhookOutbox 목록
     */
    List<WebhookOutbox> findByStatusForRetry(WebhookOutboxStatus status, int limit);
}
