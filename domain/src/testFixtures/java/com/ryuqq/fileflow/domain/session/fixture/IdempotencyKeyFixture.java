package com.ryuqq.fileflow.domain.session.fixture;

import com.ryuqq.fileflow.domain.session.vo.IdempotencyKey;
import java.util.UUID;

/**
 * IdempotencyKey Value Object Test Fixture
 *
 * @author development-team
 * @since 1.0.0
 */
public class IdempotencyKeyFixture {

    private IdempotencyKeyFixture() {
        throw new AssertionError("Utility class");
    }

    /** 기본 IdempotencyKey Fixture (신규 생성) */
    public static IdempotencyKey defaultIdempotencyKey() {
        return IdempotencyKey.forNew();
    }

    /** 고정된 UUID를 가진 IdempotencyKey Fixture (테스트 검증용) */
    public static IdempotencyKey fixedIdempotencyKey() {
        return IdempotencyKey.of(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
    }

    /** Custom IdempotencyKey Fixture */
    public static IdempotencyKey customIdempotencyKey(UUID uuid) {
        return IdempotencyKey.of(uuid);
    }
}
