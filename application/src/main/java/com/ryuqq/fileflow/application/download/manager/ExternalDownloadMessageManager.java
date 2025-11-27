package com.ryuqq.fileflow.application.download.manager;

import com.ryuqq.fileflow.application.download.dto.ExternalDownloadMessage;
import com.ryuqq.fileflow.application.download.port.out.client.SqsPublishPort;
import com.ryuqq.fileflow.domain.download.event.ExternalDownloadRegisteredEvent;
import org.springframework.stereotype.Component;

/**
 * 외부 다운로드 메시지 발행 Manager.
 *
 * <p>SQS 메시지 발행을 담당합니다.
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>도메인 이벤트로부터 SQS 메시지 생성
 *   <li>SQS 발행 포트를 통한 메시지 발행
 * </ul>
 */
@Component
public class ExternalDownloadMessageManager {

    private final SqsPublishPort sqsPublishPort;

    public ExternalDownloadMessageManager(SqsPublishPort sqsPublishPort) {
        this.sqsPublishPort = sqsPublishPort;
    }

    /**
     * 도메인 이벤트로부터 SQS 메시지를 발행합니다.
     *
     * @param event ExternalDownloadRegisteredEvent
     * @return 발행 성공 여부
     */
    public boolean publishFromEvent(ExternalDownloadRegisteredEvent event) {
        ExternalDownloadMessage message = toMessage(event);
        return sqsPublishPort.publish(message);
    }

    private ExternalDownloadMessage toMessage(ExternalDownloadRegisteredEvent event) {
        return new ExternalDownloadMessage(
                event.downloadId().value(),
                event.sourceUrl().value(),
                event.tenantId(),
                event.organizationId());
    }
}
