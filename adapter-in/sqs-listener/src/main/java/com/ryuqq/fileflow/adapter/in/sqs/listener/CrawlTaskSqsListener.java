package com.ryuqq.crawlinghub.adapter.in.sqs.listener;

import com.ryuqq.crawlinghub.application.common.component.lock.DistributedLockExecutor;
import com.ryuqq.crawlinghub.application.common.component.lock.LockType;
import com.ryuqq.crawlinghub.application.execution.dto.command.ExecuteCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.execution.port.in.CrawlTaskExecutionUseCase;
import com.ryuqq.crawlinghub.application.task.dto.messaging.CrawlTaskPayload;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * CrawlTask SQS 리스너
 *
 * <p><strong>용도</strong>: CrawlTask SQS 큐에서 메시지를 수신하여 크롤링 작업 실행
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
 * <p><strong>처리 흐름</strong>:
 * <ol>
 *   <li>SQS 큐에서 메시지 수신</li>
 *   <li>분산 락 획득 시도 (taskId 기준)</li>
 *   <li>크롤링 작업 실행 (Application Layer 호출)</li>
 *   <li>성공 시 메시지 ACK</li>
 *   <li>실패 시 메시지 유지 → 재시도 → DLQ</li>
 * </ol>
 *
 * <p><strong>분산 락</strong>:
 * <ul>
 *   <li>락 획득 성공 → 크롤링 실행 → ACK</li>
 *   <li>락 획득 실패 → ACK (다른 워커가 처리 중이므로 skip)</li>
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(
        name = "aws.sqs.listener.crawl-task-listener-enabled",
        havingValue = "true",
        matchIfMissing = true)
public class CrawlTaskSqsListener {

    private static final Logger log = LoggerFactory.getLogger(CrawlTaskSqsListener.class);

    private final DistributedLockExecutor lockExecutor;
    private final CrawlTaskExecutionUseCase crawlTaskExecutionUseCase;

    public CrawlTaskSqsListener(
            DistributedLockExecutor lockExecutor,
            CrawlTaskExecutionUseCase crawlTaskExecutionUseCase
    ) {
        this.lockExecutor = lockExecutor;
        this.crawlTaskExecutionUseCase = crawlTaskExecutionUseCase;
    }

    /**
     * CrawlTask 메시지 수신 및 처리
     *
     * <p>Spring Cloud AWS SQS의 자동 메시지 역직렬화 사용
     * <p>MANUAL acknowledgement 모드로 명시적 ACK 처리
     *
     * @param payload         CrawlTask 페이로드
     * @param acknowledgement 메시지 ACK 핸들러
     */
    @SqsListener(
            value = "${aws.sqs.listener.crawl-task-queue-url}",
            acknowledgementMode = "MANUAL"
    )
    public void handleMessage(
            @Payload CrawlTaskPayload payload,
            Acknowledgement acknowledgement
    ) {
        Long taskId = payload.taskId();

        log.debug(
                "CrawlTask 메시지 수신: taskId={}, schedulerId={}, sellerId={}, taskType={}",
                taskId,
                payload.schedulerId(),
                payload.sellerId(),
                payload.taskType());

        // 분산 락 획득 시도 (taskId 기준, waitTime=0 즉시 반환)
        boolean lockAcquired = lockExecutor.tryExecuteWithLock(
                LockType.CRAWL_TASK,
                taskId,
                () -> executeCrawlTask(payload)
        );

        if (!lockAcquired) {
            // 락 획득 실패 = 다른 워커가 처리 중 = 내가 할 일 없음
            acknowledgement.acknowledge();
            log.info("CrawlTask 처리 skip (다른 워커 처리 중): taskId={}", taskId);
            return;
        }

        // 락 획득 성공 + 처리 완료
        acknowledgement.acknowledge();
        log.info("CrawlTask 처리 완료: taskId={}, taskType={}", taskId, payload.taskType());
    }

    /**
     * 크롤링 작업 실행
     *
     * <p>CrawlTaskExecutionUseCase를 호출하여 크롤링 수행
     *
     * @param payload 크롤링 작업 정보
     */
    private void executeCrawlTask(CrawlTaskPayload payload) {
        ExecuteCrawlTaskCommand command = new ExecuteCrawlTaskCommand(
                payload.taskId(),
                payload.schedulerId(),
                payload.sellerId(),
                payload.taskType(),
                payload.endpoint()
        );

        crawlTaskExecutionUseCase.execute(command);
    }
}
