package com.ryuqq.fileflow.adapter.out.persistence.redis.common;

import com.ryuqq.fileflow.domain.common.vo.LockKey;

/**
 * 테스트용 LockKey 구현체
 *
 * <p>테스트에서 사용할 간단한 락 키입니다.
 */
public record TestLockKey(String id) implements LockKey {

    private static final String PREFIX = "test::lock::";

    public TestLockKey {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
    }

    @Override
    public String value() {
        return PREFIX + id;
    }
}
