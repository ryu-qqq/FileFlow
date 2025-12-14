package com.ryuqq.fileflow.adapter.out.persistence.redis.common;

import com.ryuqq.fileflow.domain.common.vo.CacheKey;

/**
 * 테스트용 CacheKey 구현체
 *
 * <p>테스트에서 사용할 간단한 캐시 키입니다.
 */
public record TestCacheKey(String id) implements CacheKey {

    private static final String PREFIX = "test::cache::";

    public TestCacheKey {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
    }

    @Override
    public String value() {
        return PREFIX + id;
    }
}
