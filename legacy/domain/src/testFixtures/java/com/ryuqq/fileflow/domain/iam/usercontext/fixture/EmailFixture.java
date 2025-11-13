package com.ryuqq.fileflow.domain.iam.usercontext.fixture;

import com.ryuqq.fileflow.domain.iam.usercontext.*;

/**
 * Email Test Fixture
 *
 * <p>테스트에서 Email 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // 기본 이메일 ("test@example.com")
 * Email email = EmailFixture.create();
 *
 * // 특정 이메일
 * Email email = EmailFixture.create("user@company.com");
 *
 * // 여러 이메일 생성
 * List<Email> emails = EmailFixture.createMultiple(5);
 * }</pre>
 *
     * @author ryu-qqq
 * @since 2025-10-30
 */
public class EmailFixture {

    private static final String DEFAULT_EMAIL = "test@example.com";
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private EmailFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final String DEFAULT_DOMAIN = "example.com";

    /**
     * 기본 Email을 생성합니다.
     *
     * <p>기본값: "test@example.com"</p>
     *
     * @return Email 인스턴스
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static Email create() {
        return Email.of(DEFAULT_EMAIL);
    }

    /**
     * 특정 값으로 Email을 생성합니다.
     *
     * @param value 이메일 주소
     * @return Email 인스턴스
     * @throws IllegalArgumentException value가 null, 빈 문자열이거나 이메일 형식이 아닌 경우
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static Email create(String value) {
        return Email.of(value);
    }

    /**
     * 특정 로컬 부분과 도메인으로 Email을 생성합니다.
     *
     * <p>형식: "{localPart}@{domain}"</p>
     *
     * @param localPart 이메일 로컬 부분 (@ 앞부분)
     * @param domain 이메일 도메인 (@ 뒷부분)
     * @return Email 인스턴스
     * @throws IllegalArgumentException localPart 또는 domain이 null이거나 빈 문자열인 경우
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static Email create(String localPart, String domain) {
        if (localPart == null || localPart.isBlank()) {
            throw new IllegalArgumentException("localPart는 필수입니다");
        }
        if (domain == null || domain.isBlank()) {
            throw new IllegalArgumentException("domain은 필수입니다");
        }
        return Email.of(localPart + "@" + domain);
    }

    /**
     * 순차적인 번호가 포함된 여러 개의 Email을 생성합니다.
     *
     * <p>형식: "test1@example.com", "test2@example.com", ...</p>
     *
     * @param count 생성할 이메일 개수
     * @return Email 리스트
     * @throws IllegalArgumentException count가 0 이하인 경우
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static java.util.List<Email> createMultiple(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> Email.of("test" + i + "@" + DEFAULT_DOMAIN))
            .toList();
    }

    /**
     * 특정 도메인으로 여러 개의 Email을 생성합니다.
     *
     * <p>형식: "test1@{domain}", "test2@{domain}", ...</p>
     *
     * @param domain 이메일 도메인
     * @param count 생성할 이메일 개수
     * @return Email 리스트
     * @throws IllegalArgumentException domain이 null이거나 count가 0 이하인 경우
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static java.util.List<Email> createMultiple(String domain, int count) {
        if (domain == null || domain.isBlank()) {
            throw new IllegalArgumentException("domain은 필수입니다");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> Email.of("test" + i + "@" + domain))
            .toList();
    }

    /**
     * 유효하지 않은 Email 값을 반환합니다 (검증 테스트용).
     *
     * <p>이 메서드는 Email 생성자의 유효성 검증을 테스트하기 위한 용도입니다.</p>
     *
     * @return 유효하지 않은 이메일 값 (@ 없음)
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static String invalidEmailNoAt() {
        return "invalid-email.com";
    }

    /**
     * 유효하지 않은 Email 값을 반환합니다 - 도메인 없음 (검증 테스트용).
     *
     * @return 유효하지 않은 이메일 값 (도메인 없음)
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static String invalidEmailNoDomain() {
        return "user@";
    }

    /**
     * 유효하지 않은 Email 값을 반환합니다 - 로컬 부분 없음 (검증 테스트용).
     *
     * @return 유효하지 않은 이메일 값 (로컬 부분 없음)
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static String invalidEmailNoLocal() {
        return "@example.com";
    }
}
