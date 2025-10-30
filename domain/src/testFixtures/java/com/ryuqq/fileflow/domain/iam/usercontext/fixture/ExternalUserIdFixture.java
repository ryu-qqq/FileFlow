package com.ryuqq.fileflow.domain.iam.usercontext.fixture;

import com.ryuqq.fileflow.domain.iam.usercontext.*;

/**
 * ExternalUserId Test Fixture
 *
 * <p>테스트에서 ExternalUserId 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // 기본 External User ID ("auth0|test-user-001")
 * ExternalUserId id = ExternalUserIdFixture.create();
 *
 * // 특정 External User ID
 * ExternalUserId id = ExternalUserIdFixture.create("google|123456789");
 *
 * // 여러 External User ID 생성
 * List<ExternalUserId> ids = ExternalUserIdFixture.createMultiple(5);
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
public class ExternalUserIdFixture {

    private static final String DEFAULT_PROVIDER = "auth0";
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private ExternalUserIdFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final String DEFAULT_ID = "auth0|test-user-001";

    /**
     * 기본 ExternalUserId를 생성합니다.
     *
     * <p>기본값: "auth0|test-user-001"</p>
     *
     * @return ExternalUserId 인스턴스
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static ExternalUserId create() {
        return ExternalUserId.of(DEFAULT_ID);
    }

    /**
     * 특정 값으로 ExternalUserId를 생성합니다.
     *
     * @param value External User ID 값
     * @return ExternalUserId 인스턴스
     * @throws IllegalArgumentException value가 null이거나 빈 문자열인 경우
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static ExternalUserId create(String value) {
        return ExternalUserId.of(value);
    }

    /**
     * 특정 Provider와 ID로 ExternalUserId를 생성합니다.
     *
     * <p>형식: "{provider}|{id}"</p>
     *
     * @param provider IDP Provider (예: "auth0", "google", "github")
     * @param id 사용자 ID
     * @return ExternalUserId 인스턴스
     * @throws IllegalArgumentException provider 또는 id가 null이거나 빈 문자열인 경우
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static ExternalUserId create(String provider, String id) {
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("provider는 필수입니다");
        }
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id는 필수입니다");
        }
        return ExternalUserId.of(provider + "|" + id);
    }

    /**
     * 순차적인 번호가 포함된 여러 개의 ExternalUserId를 생성합니다.
     *
     * <p>형식: "auth0|test-user-001", "auth0|test-user-002", ...</p>
     *
     * @param count 생성할 External User ID 개수
     * @return ExternalUserId 리스트
     * @throws IllegalArgumentException count가 0 이하인 경우
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static java.util.List<ExternalUserId> createMultiple(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> ExternalUserId.of(DEFAULT_PROVIDER + "|test-user-" + String.format("%03d", i)))
            .toList();
    }

    /**
     * 특정 Provider로 여러 개의 ExternalUserId를 생성합니다.
     *
     * <p>형식: "{provider}|test-user-001", "{provider}|test-user-002", ...</p>
     *
     * @param provider IDP Provider
     * @param count 생성할 External User ID 개수
     * @return ExternalUserId 리스트
     * @throws IllegalArgumentException provider가 null이거나 count가 0 이하인 경우
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static java.util.List<ExternalUserId> createMultiple(String provider, int count) {
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("provider는 필수입니다");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> ExternalUserId.of(provider + "|test-user-" + String.format("%03d", i)))
            .toList();
    }
}
