package com.ryuqq.fileflow.domain.session.fixture;

import com.ryuqq.fileflow.domain.session.vo.TotalParts;

/**
 * TotalParts Value Object Test Fixture
 *
 * @author development-team
 * @since 1.0.0
 */
public class TotalPartsFixture {

    private TotalPartsFixture() {
        throw new AssertionError("Utility class");
    }

    /** 기본 TotalParts Fixture (5개) */
    public static TotalParts defaultTotalParts() {
        return TotalParts.of(5);
    }

    /** 작은 TotalParts Fixture (2개) */
    public static TotalParts smallTotalParts() {
        return TotalParts.of(2);
    }

    /** 큰 TotalParts Fixture (100개) */
    public static TotalParts largeTotalParts() {
        return TotalParts.of(100);
    }

    /** Custom TotalParts Fixture */
    public static TotalParts customTotalParts(int value) {
        return TotalParts.of(value);
    }
}
