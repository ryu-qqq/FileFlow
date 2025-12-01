package com.ryuqq.fileflow.application.common.util;

import java.time.Clock;
import org.springframework.stereotype.Component;

/**
 * Clock Holder 구현체.
 *
 * <p>Domain Layer의 ClockHolder 인터페이스를 구현합니다.
 *
 * <p>Clock을 전역적으로 제공하는 Singleton Bean으로, 테스트 시 Clock을 교체하여 시간 제어 가능합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class ClockHolder implements com.ryuqq.fileflow.domain.common.util.ClockHolder {

    private final Clock clock;

    public ClockHolder(Clock clock) {
        this.clock = clock;
    }

    /**
     * Clock 반환
     *
     * @return Clock 인스턴스
     */
    @Override
    public Clock getClock() {
        return clock;
    }
}
