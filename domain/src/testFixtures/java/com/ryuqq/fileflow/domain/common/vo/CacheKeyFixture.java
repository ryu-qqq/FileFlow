package com.ryuqq.fileflow.domain.common.vo;

public class CacheKeyFixture {

    public static TestCacheKey aCacheKey() {
        return new TestCacheKey("test-id");
    }

    public static TestCacheKey aCacheKey(String id) {
        return new TestCacheKey(id);
    }

    public record TestCacheKey(String id) implements CacheKey {

        private static final String PREFIX = "cache:test:";

        @Override
        public String value() {
            return PREFIX + id;
        }
    }
}
