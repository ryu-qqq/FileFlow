package com.ryuqq.fileflow.domain.settings;

import java.util.Objects;

/**
 * Setting Value Value Object
 *
 * <p>설정 값과 메타데이터(타입, 비밀 여부)를 캡슐화하는 값 객체입니다.</p>
 * <p>비밀 키(isSecret=true)인 경우 자동 마스킹을 지원합니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Value Object 불변성 - final 필드</li>
 *   <li>✅ Law of Demeter - 캡슐화된 행동</li>
 *   <li>✅ Tell, Don't Ask - 마스킹 로직 내부화</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
public class SettingValue {

    private static final String MASKED_VALUE = "********";

    private final String value;
    private final SettingType type;
    private final boolean isSecret;

    /**
     * SettingValue를 생성합니다 (Static Factory Method).
     *
     * <p>비밀 키가 아닌 일반 설정 값을 생성합니다.</p>
     *
     * @param value 설정 값
     * @param type 설정 타입
     * @return 생성된 SettingValue
     * @throws IllegalArgumentException 값이나 타입이 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static SettingValue of(String value, SettingType type) {
        return new SettingValue(value, type, false);
    }

    /**
     * 비밀 키 SettingValue를 생성합니다 (Static Factory Method).
     *
     * <p>isSecret=true로 설정되어 자동 마스킹됩니다.</p>
     *
     * @param value 설정 값
     * @param type 설정 타입
     * @return 생성된 비밀 키 SettingValue
     * @throws IllegalArgumentException 값이나 타입이 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static SettingValue secret(String value, SettingType type) {
        return new SettingValue(value, type, true);
    }

    /**
     * SettingValue 생성자.
     *
     * @param value 설정 값
     * @param type 설정 타입
     * @param isSecret 비밀 키 여부
     * @throws IllegalArgumentException 값이나 타입이 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    private SettingValue(String value, SettingType type, boolean isSecret) {
        validateValue(value, type);

        this.value = value;
        this.type = type;
        this.isSecret = isSecret;
    }

    /**
     * 설정 값의 유효성을 검증합니다.
     *
     * <p>타입과 값의 호환성을 검증합니다.</p>
     *
     * @param value 검증할 값
     * @param type 설정 타입
     * @throws IllegalArgumentException 값이 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    private static void validateValue(String value, SettingType type) {
        if (value == null) {
            throw new IllegalArgumentException("Setting 값은 null일 수 없습니다");
        }
        if (type == null) {
            throw new IllegalArgumentException("Setting 타입은 필수입니다");
        }
        if (!type.isCompatibleWith(value)) {
            throw new IllegalArgumentException(
                "설정 값이 타입과 호환되지 않습니다. 타입: " + type + ", 값: " + value
            );
        }
    }

    /**
     * 설정 값을 반환합니다.
     *
     * <p>비밀 키인 경우에도 원본 값을 반환합니다.</p>
     * <p>마스킹이 필요한 경우 {@link #getDisplayValue()}를 사용하세요.</p>
     *
     * @return 설정 값 (원본)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public String getValue() {
        return value;
    }

    /**
     * 표시용 설정 값을 반환합니다.
     *
     * <p>Law of Demeter 준수: 마스킹 로직 캡슐화</p>
     * <p>❌ Bad: value.isSecret() ? "********" : value.getValue()</p>
     * <p>✅ Good: value.getDisplayValue()</p>
     *
     * <p>비밀 키(isSecret=true)인 경우 마스킹된 값을 반환합니다.</p>
     *
     * @return 표시용 설정 값 (비밀 키는 마스킹됨)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public String getDisplayValue() {
        return isSecret ? MASKED_VALUE : value;
    }

    /**
     * 설정 타입을 반환합니다.
     *
     * @return 설정 타입
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public SettingType getType() {
        return type;
    }

    /**
     * 비밀 키 여부를 반환합니다.
     *
     * @return 비밀 키이면 true
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public boolean isSecret() {
        return isSecret;
    }

    /**
     * 새로운 값으로 SettingValue를 생성합니다.
     *
     * <p>Value Object 불변성: 기존 객체를 변경하지 않고 새 객체를 반환합니다.</p>
     *
     * @param newValue 새로운 설정 값
     * @return 새로운 SettingValue
     * @throws IllegalArgumentException 새 값이 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public SettingValue withNewValue(String newValue) {
        return new SettingValue(newValue, this.type, this.isSecret);
    }

    /**
     * 동등성을 비교합니다.
     *
     * <p>Value Object 패턴: 모든 필드가 같으면 동일한 객체</p>
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
        SettingValue that = (SettingValue) o;
        return isSecret == that.isSecret &&
            Objects.equals(value, that.value) &&
            type == that.type;
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
        return Objects.hash(value, type, isSecret);
    }

    /**
     * 문자열 표현을 반환합니다.
     *
     * <p>비밀 키는 자동으로 마스킹되어 표시됩니다.</p>
     *
     * @return 설정 값 정보 문자열
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public String toString() {
        return "SettingValue{" +
            "value='" + getDisplayValue() + '\'' +
            ", type=" + type +
            ", isSecret=" + isSecret +
            '}';
    }
}
