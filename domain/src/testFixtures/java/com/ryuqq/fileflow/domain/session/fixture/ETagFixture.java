package com.ryuqq.fileflow.domain.session.fixture;

import com.ryuqq.fileflow.domain.session.vo.ETag;

/**
 * ETag Value Object Test Fixture
 *
 * @author development-team
 * @since 1.0.0
 */
public class ETagFixture {

    private ETagFixture() {
        throw new AssertionError("Utility class");
    }

    /** 기본 ETag Fixture */
    public static ETag defaultETag() {
        return ETag.of("d41d8cd98f00b204e9800998ecf8427e");
    }

    /** 빈 ETag Fixture */
    public static ETag emptyETag() {
        return ETag.empty();
    }

    /** Multipart ETag Fixture (하이픈 포함) */
    public static ETag multipartETag() {
        return ETag.of("d41d8cd98f00b204e9800998ecf8427e-5");
    }

    /** Custom ETag Fixture */
    public static ETag customETag(String value) {
        return ETag.of(value);
    }
}
