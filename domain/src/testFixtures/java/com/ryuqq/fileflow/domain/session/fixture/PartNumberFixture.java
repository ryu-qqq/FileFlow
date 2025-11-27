package com.ryuqq.fileflow.domain.session.fixture;

import com.ryuqq.fileflow.domain.session.vo.PartNumber;

/**
 * PartNumber Value Object Test Fixture
 *
 * @author development-team
 * @since 1.0.0
 */
public class PartNumberFixture {

    private PartNumberFixture() {
        throw new AssertionError("Utility class");
    }

    /** 기본 PartNumber Fixture (1번) */
    public static PartNumber defaultPartNumber() {
        return PartNumber.of(1);
    }

    /** 첫 번째 PartNumber Fixture */
    public static PartNumber firstPartNumber() {
        return PartNumber.of(1);
    }

    /** 마지막 PartNumber Fixture (5번) */
    public static PartNumber lastPartNumber() {
        return PartNumber.of(5);
    }

    /** Custom PartNumber Fixture */
    public static PartNumber customPartNumber(int number) {
        return PartNumber.of(number);
    }
}
