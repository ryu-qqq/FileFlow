package com.ryuqq.fileflow.domain.common.vo;

public class LockKeyFixture {

    public static TestLockKey aLockKey() {
        return new TestLockKey("test-id");
    }

    public static TestLockKey aLockKey(String id) {
        return new TestLockKey(id);
    }

    public record TestLockKey(String id) implements LockKey {

        private static final String PREFIX = "lock:test:";

        @Override
        public String value() {
            return PREFIX + id;
        }
    }
}
