package com.ryuqq.fileflow.domain.session.fixture;

import java.util.UUID;

import com.ryuqq.fileflow.domain.session.vo.SessionId;

/**
 * SessionId Test Fixture (Object Mother)
 *
 * <p>
 * Domain Layer 테스트는 반드시 Fixture를 사용해야 하므로,
 * 해당 클래스에서 SessionId 생성 패턴을 표준화한다.
 * </p>
 */
public final class SessionIdFixture {

    private static final String DEFAULT_SESSION_ID = "11111111-2222-3333-4444-555555555555";

    private SessionIdFixture() {
    }

    /**
     * 신규 SessionId 생성 (UUID v4)
     *
     * @return SessionId
     */
    public static SessionId forNew() {
        return SessionId.forNew();
    }

    /**
     * 고정된 UUID 문자열로 SessionId 생성
     *
     * @return SessionId
     */
    public static SessionId fromDefault() {
        return SessionId.from(DEFAULT_SESSION_ID);
    }

    /**
     * 주어진 UUID 문자열로 SessionId 생성
     *
     * @param value UUID 문자열
     * @return SessionId
     */
    public static SessionId from(String value) {
        return SessionId.from(value);
    }

    /**
     * UUID v4 문자열을 반환 (테스트에서 재사용)
     *
     * @return UUID 문자열
     */
    public static String validValue() {
        return DEFAULT_SESSION_ID;
    }

    /**
     * 무작위 UUID 문자열 반환
     *
     * @return UUID 문자열
     */
    public static String randomValue() {
        return UUID.randomUUID().toString();
    }

    /**
     * 아직 영속화되지 않은 SessionId (value = null)
     *
     * @return SessionId
     */
    public static SessionId unassigned() {
        return new SessionId(null);
    }

    /**
     * 유효하지 않은 SessionId 문자열
     *
     * @return invalid 문자열
     */
    public static String invalidValue() {
        return "invalid-session-id";
    }
}

