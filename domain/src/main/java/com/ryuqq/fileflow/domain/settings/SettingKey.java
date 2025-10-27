package com.ryuqq.fileflow.domain.settings;

import java.util.Objects;

/**
 * Setting Key Value Object
 *
 * <p>설정의 고유 키를 나타내는 값 객체입니다.</p>
 * <p>도메인 모델에서 식별자 역할을 하며 불변성을 보장합니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Value Object 불변성 - final 필드</li>
 *   <li>✅ Law of Demeter - 캡슐화된 행동</li>
 *   <li>✅ Domain 규칙 검증 - 생성 시 유효성 검증</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
public class SettingKey {

    private static final int MAX_LENGTH = 100;
    private static final String KEY_PATTERN = "^[a-zA-Z0-9._-]+$";

    private final String value;

    /**
     * SettingKey를 생성합니다 (Static Factory Method).
     *
     * <p>키는 영문자, 숫자, '.', '_', '-'만 허용하며 최대 100자입니다.</p>
     *
     * @param value 설정 키 값
     * @return 생성된 SettingKey
     * @throws IllegalArgumentException 키가 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static SettingKey of(String value) {
        return new SettingKey(value);
    }

    /**
     * SettingKey 생성자 (package-private).
     *
     * @param value 설정 키 값
     * @throws IllegalArgumentException 키가 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    SettingKey(String value) {
        validateKey(value);
        this.value = value;
    }

    /**
     * 설정 키의 유효성을 검증합니다.
     *
     * <p>검증 규칙:</p>
     * <ul>
     *   <li>null 또는 빈 문자열 불가</li>
     *   <li>최대 100자</li>
     *   <li>영문자, 숫자, '.', '_', '-'만 허용</li>
     * </ul>
     *
     * @param value 검증할 키 값
     * @throws IllegalArgumentException 키가 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    private static void validateKey(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Setting 키는 필수입니다");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                "Setting 키는 " + MAX_LENGTH + "자를 초과할 수 없습니다: " + value.length() + "자"
            );
        }
        if (!value.matches(KEY_PATTERN)) {
            throw new IllegalArgumentException(
                "Setting 키는 영문자, 숫자, '.', '_', '-'만 허용됩니다: " + value
            );
        }
    }

    /**
     * 설정 키 값을 반환합니다.
     *
     * @return 설정 키 값
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public String getValue() {
        return value;
    }

    /**
     * 다른 키와 동일한지 확인합니다.
     *
     * <p>Law of Demeter 준수: 비교 로직 캡슐화</p>
     * <p>❌ Bad: key1.getValue().equals(key2.getValue())</p>
     * <p>✅ Good: key1.isSameAs(key2)</p>
     *
     * @param other 비교 대상 키
     * @return 동일하면 true
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public boolean isSameAs(SettingKey other) {
        if (other == null) {
            return false;
        }
        return this.value.equals(other.value);
    }

    /**
     * 이 키가 비밀 설정 키인지 판단합니다.
     *
     * <p>특정 패턴의 키 이름은 자동으로 비밀 설정으로 간주됩니다:</p>
     * <ul>
     *   <li>API_KEY, API-KEY, api.key 등 (대소문자 무관)</li>
     *   <li>PASSWORD, password 등</li>
     *   <li>SECRET, secret 등</li>
     *   <li>TOKEN, token 등</li>
     *   <li>CREDENTIAL, credential 등</li>
     * </ul>
     *
     * <p><strong>도메인 비즈니스 규칙:</strong></p>
     * <p>민감한 정보를 담는 키는 자동으로 마스킹 처리되어야 합니다.</p>
     *
     * @return 비밀 설정 키이면 true
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public boolean isSecretKey() {
        String upperValue = this.value.toUpperCase();
        return upperValue.contains("API_KEY") ||
               upperValue.contains("API-KEY") ||
               upperValue.contains("API.KEY") ||
               upperValue.contains("PASSWORD") ||
               upperValue.contains("SECRET") ||
               upperValue.contains("TOKEN") ||
               upperValue.contains("CREDENTIAL");
    }

    /**
     * 동등성을 비교합니다.
     *
     * <p>Value Object 패턴: 값이 같으면 동일한 객체</p>
     *
     * @param o 비교 대상 객체
     * @return 동등 여부
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SettingKey that = (SettingKey) o;
        return Objects.equals(value, that.value);
    }

    /**
     * 해시코드를 반환합니다.
     *
     * @return 해시코드
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    /**
     * 문자열 표현을 반환합니다.
     *
     * @return 설정 키 값
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public String toString() {
        return value;
    }
}
