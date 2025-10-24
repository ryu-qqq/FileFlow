package com.ryuqq.fileflow.domain.iam.usercontext;

/**
 * 외부 Identity Provider의 사용자 식별자
 *
 * <p>IDP (Identity Provider)에서 제공하는 사용자 고유 식별자(sub claim)를 나타내는 Value Object입니다.</p>
 * <p>OAuth 2.0 / OIDC의 sub claim 값을 저장하며, 외부 IDP와의 연동을 위해 사용됩니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Java 21 Record 사용</li>
 *   <li>✅ 불변 객체 (Immutable)</li>
 *   <li>✅ null 및 빈 문자열 검증</li>
 *   <li>❌ Lombok 사용 안함</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>
 * // IDP에서 받은 sub claim: "auth0|507f1f77bcf86cd799439011"
 * ExternalUserId externalUserId = ExternalUserId.of("auth0|507f1f77bcf86cd799439011");
 * </pre>
 *
 * @param value IDP의 sub claim 값
 * @author ryu-qqq
 * @since 2025-10-24
 */
public record ExternalUserId(String value) {

    /**
     * ExternalUserId의 Compact Constructor
     *
     * <p>null 및 빈 문자열을 검증합니다.</p>
     *
     * @throws IllegalArgumentException value가 null이거나 빈 문자열인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public ExternalUserId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("External User ID는 필수입니다");
        }
    }

    /**
     * ExternalUserId 생성 - Static Factory Method
     *
     * @param value IDP의 sub claim 값
     * @return ExternalUserId 인스턴스
     * @throws IllegalArgumentException value가 null이거나 빈 문자열인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static ExternalUserId of(String value) {
        return new ExternalUserId(value);
    }
}
