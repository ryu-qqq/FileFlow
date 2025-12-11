package com.ryuqq.fileflow.domain.common.event;

import java.time.Instant;

/**
 * 도메인 이벤트 마커 인터페이스
 *
 * <p>모든 도메인 이벤트가 구현해야 하는 기본 인터페이스입니다. 향후 도메인 이벤트 발행 기능 구현 시 사용됩니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface DomainEvent {

    /**
     * 이벤트 발생 시각
     *
     * <p>Aggregate의 Clock에서 획득한 시간을 사용합니다.
     *
     * @return 이벤트가 생성된 시각
     */
    Instant occurredAt();

    /**
     * 이벤트 타입 식별자
     *
     * <p>기본 구현은 클래스 단순명을 반환합니다.
     *
     * <p>이벤트 라우팅 및 로깅에 사용됩니다.
     *
     * @return 이벤트 타입 문자열
     */
    default String eventType() {
        return this.getClass().getSimpleName();
    }
}
