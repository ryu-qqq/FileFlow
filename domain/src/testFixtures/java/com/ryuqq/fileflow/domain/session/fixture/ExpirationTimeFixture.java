package com.ryuqq.fileflow.domain.session.fixture;

import com.ryuqq.fileflow.domain.session.vo.ExpirationTime;
import java.time.LocalDateTime;

/**
 * ExpirationTime Value Object Test Fixture
 *
 * @author development-team
 * @since 1.0.0
 */
public class ExpirationTimeFixture {

    private ExpirationTimeFixture() {
        throw new AssertionError("Utility class");
    }

    /** 기본 ExpirationTime Fixture (현재 시각 + 15분) */
    public static ExpirationTime defaultExpirationTime() {
        return ExpirationTime.of(LocalDateTime.now().plusMinutes(15));
    }

    /** 24시간 후 만료 ExpirationTime Fixture */
    public static ExpirationTime multipartExpirationTime() {
        return ExpirationTime.of(LocalDateTime.now().plusHours(24));
    }

    /** 이미 만료된 ExpirationTime Fixture */
    public static ExpirationTime expiredExpirationTime() {
        return ExpirationTime.of(LocalDateTime.now().minusHours(1));
    }

    /** Custom ExpirationTime Fixture */
    public static ExpirationTime customExpirationTime(LocalDateTime dateTime) {
        return ExpirationTime.of(dateTime);
    }
}
