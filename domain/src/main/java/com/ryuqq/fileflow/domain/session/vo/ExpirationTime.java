package com.ryuqq.fileflow.domain.session.vo;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * 세션 만료 시각 Value Object.
 *
 * <p>업로드 세션의 만료 시각을 나타냅니다.
 *
 * <p>만료 시각이 지나면 세션은 더 이상 사용할 수 없습니다.
 *
 * <p><strong>도메인 규칙</strong>: 만료 시각은 null일 수 없다.
 *
 * @param value 만료 시각
 */
public record ExpirationTime(LocalDateTime value) {

    /** Compact Constructor (검증 로직). */
    public ExpirationTime {
        if (value == null) {
            throw new IllegalArgumentException("만료 시각은 null일 수 없습니다.");
        }
    }

    /**
     * 값 기반 생성.
     *
     * @param value 만료 시각 (null 불가)
     * @return ExpirationTime
     * @throws IllegalArgumentException value가 null인 경우
     */
    public static ExpirationTime of(LocalDateTime value) {
        return new ExpirationTime(value);
    }

    /**
     * 현재 시각 기준으로 만료 시각 생성.
     *
     * @param clock 시간 소스
     * @param minutesFromNow 현재 시각으로부터 몇 분 후
     * @return ExpirationTime
     */
    public static ExpirationTime fromNow(Clock clock, long minutesFromNow) {
        LocalDateTime expiresAt = LocalDateTime.now(clock).plusMinutes(minutesFromNow);
        return new ExpirationTime(expiresAt);
    }

    /**
     * 만료되었는지 확인한다.
     *
     * @param clock 현재 시간 소스
     * @return 만료되었으면 true
     */
    public boolean isExpired(Clock clock) {
        return LocalDateTime.now(clock).isAfter(value);
    }

    /**
     * 만료되지 않았는지 확인한다.
     *
     * @param clock 현재 시간 소스
     * @return 만료되지 않았으면 true
     */
    public boolean isNotExpired(Clock clock) {
        return !isExpired(clock);
    }
}
