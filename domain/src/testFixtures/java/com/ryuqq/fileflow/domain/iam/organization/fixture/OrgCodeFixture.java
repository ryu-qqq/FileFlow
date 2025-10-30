package com.ryuqq.fileflow.domain.iam.organization.fixture;

import com.ryuqq.fileflow.domain.iam.organization.*;

/**
 * OrgCode Test Fixture
 *
 * <p>테스트에서 OrgCode 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // 기본 조직 코드 ("ORG-001")
 * OrgCode code = OrgCodeFixture.create();
 *
 * // 특정 조직 코드
 * OrgCode code = OrgCodeFixture.create("SALES-TEAM");
 *
 * // 순차적인 조직 코드 생성
 * List<OrgCode> codes = OrgCodeFixture.createMultiple(5);
 * // → ["ORG-001", "ORG-002", "ORG-003", "ORG-004", "ORG-005"]
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
public class OrgCodeFixture {

    private static final String DEFAULT_CODE = "ORG-001";
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private OrgCodeFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final String CODE_PREFIX = "ORG-";

    /**
     * 기본 OrgCode를 생성합니다.
     *
     * <p>기본값: "ORG-001"</p>
     *
     * @return OrgCode 인스턴스
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static OrgCode create() {
        return OrgCode.of(DEFAULT_CODE);
    }

    /**
     * 특정 값으로 OrgCode를 생성합니다.
     *
     * <p>입력값은 자동으로 대문자로 변환됩니다.</p>
     *
     * @param value 조직 코드 값
     * @return OrgCode 인스턴스
     * @throws IllegalArgumentException value가 null, 빈 문자열이거나 비즈니스 규칙을 위반하는 경우
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static OrgCode create(String value) {
        return OrgCode.of(value);
    }

    /**
     * 순차적인 번호가 포함된 여러 개의 OrgCode를 생성합니다.
     *
     * <p>형식: "ORG-001", "ORG-002", "ORG-003", ...</p>
     *
     * @param count 생성할 조직 코드 개수
     * @return OrgCode 리스트
     * @throws IllegalArgumentException count가 0 이하인 경우
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static java.util.List<OrgCode> createMultiple(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> OrgCode.of(CODE_PREFIX + String.format("%03d", i)))
            .toList();
    }

    /**
     * 특정 접두사로 여러 개의 OrgCode를 생성합니다.
     *
     * <p>형식: "{prefix}-001", "{prefix}-002", "{prefix}-003", ...</p>
     *
     * @param prefix 조직 코드 접두사
     * @param count 생성할 조직 코드 개수
     * @return OrgCode 리스트
     * @throws IllegalArgumentException prefix가 null이거나 count가 0 이하인 경우
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static java.util.List<OrgCode> createMultiple(String prefix, int count) {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("prefix는 필수입니다");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> OrgCode.of(prefix + "-" + String.format("%03d", i)))
            .toList();
    }

    /**
     * 유효하지 않은 OrgCode 값을 반환합니다 (검증 테스트용).
     *
     * <p>이 메서드는 OrgCode 생성자의 유효성 검증을 테스트하기 위한 용도입니다.</p>
     *
     * @return 유효하지 않은 조직 코드 값
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static String invalidCode() {
        return "a"; // 최소 길이 미달 (2자 미만)
    }

    /**
     * 유효하지 않은 OrgCode 값을 반환합니다 - 특수문자 포함 (검증 테스트용).
     *
     * @return 유효하지 않은 조직 코드 값
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static String invalidCodeWithSpecialChars() {
        return "ORG@123"; // 허용되지 않는 특수문자 포함
    }

    /**
     * 유효하지 않은 OrgCode 값을 반환합니다 - 최대 길이 초과 (검증 테스트용).
     *
     * @return 유효하지 않은 조직 코드 값
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static String invalidCodeTooLong() {
        return "A".repeat(21); // 최대 길이 초과 (20자 초과)
    }
}
