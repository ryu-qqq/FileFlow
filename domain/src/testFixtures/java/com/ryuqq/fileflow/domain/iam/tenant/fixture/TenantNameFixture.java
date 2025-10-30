package com.ryuqq.fileflow.domain.iam.tenant.fixture;

import com.ryuqq.fileflow.domain.iam.tenant.*;

/**
 * TenantName Test Fixture
 *
 * <p>테스트에서 TenantName 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // 기본 Tenant 이름 ("Test Tenant")
 * TenantName name = TenantNameFixture.create();
 *
 * // 특정 Tenant 이름
 * TenantName name = TenantNameFixture.create("Acme Corporation");
 *
 * // 순차적인 Tenant 이름 생성
 * List<TenantName> names = TenantNameFixture.createMultiple(5);
 * // → ["Test Tenant 1", "Test Tenant 2", "Test Tenant 3", "Test Tenant 4", "Test Tenant 5"]
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
public class TenantNameFixture {

    private static final String DEFAULT_NAME = "Test Tenant";
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private TenantNameFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }


    /**
     * 기본 TenantName을 생성합니다.
     *
     * <p>기본값: "Test Tenant"</p>
     *
     * @return TenantName 인스턴스
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static TenantName create() {
        return TenantName.of(DEFAULT_NAME);
    }

    /**
     * 특정 값으로 TenantName을 생성합니다.
     *
     * @param value Tenant 이름
     * @return TenantName 인스턴스
     * @throws IllegalArgumentException value가 null, 빈 문자열이거나 비즈니스 규칙을 위반하는 경우
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static TenantName create(String value) {
        return TenantName.of(value);
    }

    /**
     * 순차적인 번호가 포함된 여러 개의 TenantName을 생성합니다.
     *
     * <p>형식: "Test Tenant 1", "Test Tenant 2", "Test Tenant 3", ...</p>
     *
     * @param count 생성할 Tenant 이름 개수
     * @return TenantName 리스트
     * @throws IllegalArgumentException count가 0 이하인 경우
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static java.util.List<TenantName> createMultiple(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> TenantName.of(DEFAULT_NAME + " " + i))
            .toList();
    }

    /**
     * 특정 접두사로 여러 개의 TenantName을 생성합니다.
     *
     * <p>형식: "{prefix} 1", "{prefix} 2", "{prefix} 3", ...</p>
     *
     * @param prefix Tenant 이름 접두사
     * @param count 생성할 Tenant 이름 개수
     * @return TenantName 리스트
     * @throws IllegalArgumentException prefix가 null이거나 count가 0 이하인 경우
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static java.util.List<TenantName> createMultiple(String prefix, int count) {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("prefix는 필수입니다");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> TenantName.of(prefix + " " + i))
            .toList();
    }

    /**
     * 유효하지 않은 TenantName 값을 반환합니다 (검증 테스트용).
     *
     * <p>이 메서드는 TenantName 생성자의 유효성 검증을 테스트하기 위한 용도입니다.</p>
     *
     * @return 유효하지 않은 Tenant 이름 값 (최소 길이 미달)
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static String invalidNameTooShort() {
        return "A"; // 최소 길이 미달 (2자 미만)
    }

    /**
     * 유효하지 않은 TenantName 값을 반환합니다 - 최대 길이 초과 (검증 테스트용).
     *
     * @return 유효하지 않은 Tenant 이름 값 (최대 길이 초과)
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static String invalidNameTooLong() {
        return "A".repeat(51); // 최대 길이 초과 (50자 초과)
    }

    /**
     * 유효하지 않은 TenantName 값을 반환합니다 - 빈 문자열 (검증 테스트용).
     *
     * @return 유효하지 않은 Tenant 이름 값 (빈 문자열)
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static String invalidNameBlank() {
        return "   "; // 공백만 포함
    }
}
