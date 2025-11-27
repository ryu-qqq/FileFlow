package com.ryuqq.fileflow.domain.common.fixture;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Clock Test Fixture
 *
 * @author development-team
 * @since 1.0.0
 */
public class ClockFixture {

    private ClockFixture() {
        throw new AssertionError("Utility class");
    }

    /** 기본 Clock Fixture (시스템 기본 시간대) */
    public static Clock defaultClock() {
        return Clock.systemDefaultZone();
    }

    /** UTC Clock Fixture */
    public static Clock utcClock() {
        return Clock.systemUTC();
    }

    /** 고정된 시간의 Clock Fixture (2025-01-25 12:00:00 UTC) */
    public static Clock fixedClock() {
        return Clock.fixed(Instant.parse("2025-01-25T12:00:00Z"), ZoneId.of("UTC"));
    }

    /** Custom 고정 시간 Clock Fixture */
    public static Clock customFixedClock(Instant instant, ZoneId zoneId) {
        return Clock.fixed(instant, zoneId);
    }
}
