package com.ryuqq.fileflow.domain.iam.usercontext;

import java.util.regex.Pattern;

/**
 * 이메일 주소
 *
 * <p>사용자의 이메일 주소를 나타내는 Value Object입니다.</p>
 * <p>이메일 형식 검증을 포함하며, 불변 객체로 구현됩니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Java 21 Record 사용</li>
 *   <li>✅ 불변 객체 (Immutable)</li>
 *   <li>✅ 이메일 형식 검증 (RFC 5322 간소화 버전)</li>
 *   <li>✅ null 및 빈 문자열 검증</li>
 *   <li>❌ Lombok 사용 안함</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>
 * Email email = Email.of("user@example.com");
 * String value = email.value(); // "user@example.com"
 * </pre>
 *
 * @param value 이메일 주소
 * @author ryu-qqq
 * @since 2025-10-24
 */
public record Email(String value) {

    /**
     * 이메일 형식 검증을 위한 정규표현식
     *
     * <p>RFC 5322 표준의 간소화 버전입니다.</p>
     * <p>일반적인 이메일 형식을 검증하며, 완벽한 RFC 5322 준수는 아닙니다.</p>
     * <p>연속된 점(..), 시작/끝 점(.), 도메인 시작 점(.) 등을 거부합니다.</p>
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        """
        ^                                             # 문자열 시작
        # 로컬 파트: 연속된 점(..), 시작/끝 점(.) 불허
        [A-Za-z0-9+_-]+(?:\\.[A-Za-z0-9+_-]+)*
        @                                             # @ 기호
        # 도메인 파트: 시작/끝 하이픈(-) 불허
        [A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?
        (?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)*
        # TLD: 최소 2글자
        \\.[A-Za-z]{2,}
        $                                             # 문자열 끝
        """, Pattern.COMMENTS
    );

    /**
     * Email의 Compact Constructor
     *
     * <p>null, 빈 문자열 및 이메일 형식을 검증합니다.</p>
     *
     * @throws IllegalArgumentException value가 null, 빈 문자열이거나 이메일 형식이 아닌 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public Email {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("이메일 주소는 필수입니다");
        }

        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다: " + value);
        }
    }

    /**
     * Email 생성 - Static Factory Method
     *
     * @param value 이메일 주소
     * @return Email 인스턴스
     * @throws IllegalArgumentException value가 null, 빈 문자열이거나 이메일 형식이 아닌 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static Email of(String value) {
        return new Email(value);
    }

    /**
     * 이메일 주소의 도메인 부분을 반환합니다.
     *
     * <p>Law of Demeter 준수: 내부 구조를 노출하지 않고 필요한 정보만 제공합니다.</p>
     * <p>예: "user@example.com" → "example.com"</p>
     *
     * @return 이메일 도메인
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public String getDomain() {
        int atIndex = value.indexOf('@');
        return value.substring(atIndex + 1);
    }

    /**
     * 이메일 주소의 로컬 부분을 반환합니다.
     *
     * <p>Law of Demeter 준수: 내부 구조를 노출하지 않고 필요한 정보만 제공합니다.</p>
     * <p>예: "user@example.com" → "user"</p>
     *
     * @return 이메일 로컬 부분
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public String getLocalPart() {
        int atIndex = value.indexOf('@');
        return value.substring(0, atIndex);
    }
}
