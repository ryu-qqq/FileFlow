package com.ryuqq.fileflow.application.common.port.out;

import com.ryuqq.fileflow.domain.common.event.DomainEvent;

/**
 * 도메인 이벤트 발행 Port
 *
 * Hexagonal Architecture의 Outbound Port로서,
 * 도메인 이벤트를 외부 시스템(이벤트 버스, 메시지 큐 등)으로 발행하는 역할을 정의합니다.
 *
 * @author sangwon-ryu
 */
public interface DomainEventPublisher {

    /**
     * 도메인 이벤트를 발행합니다.
     *
     * @param event 발행할 도메인 이벤트
     */
    void publish(DomainEvent event);

    /**
     * 여러 도메인 이벤트를 일괄 발행합니다.
     *
     * @param events 발행할 도메인 이벤트 목록
     */
    void publishAll(Iterable<? extends DomainEvent> events);
}
