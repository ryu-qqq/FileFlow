package com.ryuqq.fileflow.domain.download.fixture;

import com.ryuqq.fileflow.domain.download.IdempotencyKey;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * IdempotencyKey Test Fixture
 *
 * <p>테스트에서 IdempotencyKey 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // 기본 Idempotency Key (UUID)
 * IdempotencyKey key = IdempotencyKeyFixture.create();
 *
 * // 특정 값으로 생성
 * IdempotencyKey key = IdempotencyKeyFixture.create("custom-key-123");
 *
 * // 여러 Idempotency Key 생성
 * List<IdempotencyKey> keys = IdempotencyKeyFixture.createMultiple(5);
 * }</pre>
 *
 * @author Sangwon Ryu
 * @since 2025-11-02
 */
public class IdempotencyKeyFixture {

    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    private IdempotencyKeyFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 기본 IdempotencyKey를 생성합니다 (UUID 기반).
     *
     * @return IdempotencyKey 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static IdempotencyKey create() {
        return IdempotencyKey.of(UUID.randomUUID().toString());
    }

    /**
     * 특정 값으로 IdempotencyKey를 생성합니다.
     *
     * @param value Idempotency Key 값
     * @return IdempotencyKey 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static IdempotencyKey create(String value) {
        return IdempotencyKey.of(value);
    }

    /**
     * 순차적인 Idempotency Key를 생성합니다 (테스트용).
     *
     * <p>형식: "idempotency-key-1", "idempotency-key-2", ...</p>
     *
     * @param sequence 순서 번호
     * @return IdempotencyKey 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static IdempotencyKey createSequential(int sequence) {
        return IdempotencyKey.of("idempotency-key-" + sequence);
    }

    /**
     * 여러 개의 IdempotencyKey를 생성합니다 (UUID 기반).
     *
     * @param count 생성할 Key 개수
     * @return IdempotencyKey 리스트
     * @throws IllegalArgumentException count가 0 이하인 경우
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static List<IdempotencyKey> createMultiple(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return IntStream.rangeClosed(1, count)
            .mapToObj(i -> IdempotencyKey.of(UUID.randomUUID().toString()))
            .toList();
    }

    /**
     * 여러 개의 순차적인 IdempotencyKey를 생성합니다 (테스트용).
     *
     * @param count 생성할 Key 개수
     * @return IdempotencyKey 리스트
     * @throws IllegalArgumentException count가 0 이하인 경우
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static List<IdempotencyKey> createMultipleSequential(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return IntStream.rangeClosed(1, count)
            .mapToObj(IdempotencyKeyFixture::createSequential)
            .toList();
    }
}
