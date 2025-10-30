package com.ryuqq.fileflow.domain.iam.organization.fixture;

import com.ryuqq.fileflow.domain.iam.organization.*;

/**
 * OrganizationId Test Fixture
 *
 * <p>테스트에서 OrganizationId 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // 기본 ID (1L)
 * OrganizationId id = OrganizationIdFixture.create();
 *
 * // 특정 ID
 * OrganizationId id = OrganizationIdFixture.create(100L);
 *
 * // 여러 ID 생성
 * List<OrganizationId> ids = OrganizationIdFixture.createMultiple(5);
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
public class OrganizationIdFixture {

    private static final Long DEFAULT_ID = 1L;

    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private OrganizationIdFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 기본 OrganizationId를 생성합니다.
     *
     * <p>기본값: 1L</p>
     *
     * @return OrganizationId 인스턴스
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static OrganizationId create() {
        return OrganizationId.of(DEFAULT_ID);
    }

    /**
     * 특정 값으로 OrganizationId를 생성합니다.
     *
     * @param value Organization ID 값
     * @return OrganizationId 인스턴스
     * @throws IllegalArgumentException value가 null이거나 음수인 경우
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static OrganizationId create(Long value) {
        return OrganizationId.of(value);
    }

    /**
     * 여러 개의 OrganizationId를 생성합니다.
     *
     * <p>1부터 시작하는 연속된 ID를 생성합니다.</p>
     *
     * @param count 생성할 ID 개수
     * @return OrganizationId 리스트
     * @throws IllegalArgumentException count가 0 이하인 경우
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static java.util.List<OrganizationId> createMultiple(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> OrganizationId.of((long) i))
            .toList();
    }

    /**
     * 특정 시작 값부터 여러 개의 OrganizationId를 생성합니다.
     *
     * @param startId 시작 ID 값
     * @param count 생성할 ID 개수
     * @return OrganizationId 리스트
     * @throws IllegalArgumentException startId가 음수이거나 count가 0 이하인 경우
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static java.util.List<OrganizationId> createMultiple(Long startId, int count) {
        if (startId <= 0) {
            throw new IllegalArgumentException("startId는 양수여야 합니다");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.LongStream.range(startId, startId + count)
            .mapToObj(OrganizationId::of)
            .toList();
    }
}
