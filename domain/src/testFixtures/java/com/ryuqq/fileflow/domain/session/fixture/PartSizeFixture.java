package com.ryuqq.fileflow.domain.session.fixture;

import com.ryuqq.fileflow.domain.session.vo.PartSize;

/**
 * PartSize Value Object Test Fixture
 *
 * @author development-team
 * @since 1.0.0
 */
public class PartSizeFixture {

    private PartSizeFixture() {
        throw new AssertionError("Utility class");
    }

    /** 기본 PartSize Fixture (10MB) */
    public static PartSize defaultPartSize() {
        return PartSize.of(10 * 1024 * 1024L); // 10MB
    }

    /** 최소 PartSize Fixture (5MB) */
    public static PartSize minimumPartSize() {
        return PartSize.of(5 * 1024 * 1024L); // 5MB
    }

    /** 큰 PartSize Fixture (100MB) */
    public static PartSize largePartSize() {
        return PartSize.of(100 * 1024 * 1024L); // 100MB
    }

    /** Custom PartSize Fixture */
    public static PartSize customPartSize(long bytes) {
        return PartSize.of(bytes);
    }
}
