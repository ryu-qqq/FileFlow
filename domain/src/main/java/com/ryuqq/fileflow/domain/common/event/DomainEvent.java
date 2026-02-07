package com.ryuqq.fileflow.domain.common.event;

import java.time.Instant;

/**
 * 도메인 이벤트 마커 인터페이스.
 * 모든 도메인 이벤트는 이 인터페이스를 구현합니다.
 *
 * <p>구현체는 Java Record로 정의하고, occurredAt 필드를 포함해야 합니다.
 * <p>from(Aggregate, Instant occurredAt) 팩토리 메서드를 제공합니다.
 */
public interface DomainEvent {

    Instant occurredAt();
}
