package com.ryuqq.crawlinghub.adapter.in.sqs.listener;

import com.ryuqq.crawlinghub.application.task.dto.messaging.CrawlTaskPayload;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * CrawlTask DLQ 리스너
 *
 * <p><strong>용도</strong>: CrawlTask 처리 실패 메시지 수신 및 Outbox FAILED 마킹
 *
 * <p><strong>처리 흐름</strong>:
 * <pre>
 * crawl-task-queue (재시도 실패)
 *     ↓
 * crawl-task-dlq (maxReceiveCount 초과)
 *     ↓
 * CrawlTaskDlqListener (이 클래스)
 *     ↓
 * Outbox FAILED 마킹
 * </pre>
 *
 * <p><strong>메시지 페이로드</strong>:
 * <pre>{@code
 * {
 *   "taskId": 123,
 *   "schedulerId": 456,
 *   "sellerId": 789,
 *   "taskType": "PRODUCT",
 *   "endpoint": "https://example.com/api/products"
 * }
 * }</pre>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(
        name = "aws.sqs.listener.crawl-task-dlq-listener-enabled",
        havingValue = "true",
        matchIfMissing = true)
public class CrawlTaskDlqListener {

    private static final Logger log = LoggerFactory.getLogger(CrawlTaskDlqListener.class);

    private final CrawlTaskOutboxQueryPort outboxQueryPort;
    private final CrawlTaskOutboxTransactionManager outboxTransactionManager;

    public CrawlTaskDlqListener(
            CrawlTaskOutboxQueryPort outboxQueryPort,
            CrawlTaskOutboxTransactionManager outboxTransactionManager
    ) {
        this.outboxQueryPort = outboxQueryPort;
        this.outboxTransactionManager = outboxTransactionManager;
    }

    /**
     * DLQ 메시지 수신 및 처리
     *
     * <p>Spring Cloud AWS SQS의 자동 메시지 역직렬화 사용
     * <p>MANUAL acknowledgement 모드로 명시적 ACK 처리
     *
     * @param payload         CrawlTask 페이로드
     * @param acknowledgement 메시지 ACK 핸들러
     */
    @SqsListener(
            value = "${aws.sqs.listener.crawl-task-dlq-url}",
            acknowledgementMode = "MANUAL"
    )
    public void handleMessage(
            @Payload CrawlTaskPayload payload,
            Acknowledgement acknowledgement
    ) {
        Long taskId = payload.taskId();

        log.debug("CrawlTask DLQ 메시지 수신: taskId={}", taskId);

        try {
            // Outbox 조회 및 FAILED 마킹
            markOutboxAsFailed(taskId);

            // 성공 시 ACK
            acknowledgement.acknowledge();

            log.info("CrawlTask DLQ 처리 완료 (Outbox FAILED 마킹): taskId={}", taskId);

        } catch (Exception e) {
            log.error(
                    "CrawlTask DLQ 처리 실패: taskId={}, error={}",
                    taskId,
                    e.getMessage(),
                    e);
            // 처리 실패 시 ACK 하지 않음 → 다음 폴링에서 재시도
            throw e;
        }
    }

    /**
     * Outbox FAILED 마킹
     *
     * @param taskId CrawlTask ID
     */
    private void markOutboxAsFailed(Long taskId) {
        CrawlTaskId crawlTaskId = CrawlTaskId.of(taskId);

        Optional<CrawlTaskOutbox> outboxOpt = outboxQueryPort.findByCrawlTaskId(crawlTaskId);

        if (outboxOpt.isPresent()) {
            CrawlTaskOutbox outbox = outboxOpt.get();
            outboxTransactionManager.markAsFailed(outbox);
            log.info("Outbox FAILED 마킹 완료: taskId={}", taskId);
        } else {
            log.warn("Outbox를 찾을 수 없음: taskId={}", taskId);
        }
    }
}
