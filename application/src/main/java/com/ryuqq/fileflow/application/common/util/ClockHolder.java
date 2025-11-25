package com.ryuqq.fileflow.application.common.util;

import java.time.Clock;
import org.springframework.stereotype.Component;

/**
 * Clock Holder
 *
 * <p>Clock을 전역적으로 제공하는 Singleton Bean
 *
 * <p>테스트 시 Clock을 교체하여 시간 제어 가능
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class ClockHolder {

    private final Clock clock;

    public ClockHolder(Clock clock) {
        this.clock = clock;
    }

    /**
     * Clock 반환
     *
     * @return Clock 인스턴스
     */
    public Clock getClock() {
        return clock;
    }
}
