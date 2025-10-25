package com.ryuqq.fileflow.domain.settings;

/**
 * Invalid Setting Exception
 *
 * <p>Setting 데이터가 유효하지 않을 때 발생하는 Domain Exception입니다.</p>
 * <p>JSON 스키마 검증 실패, 타입 불일치 등의 경우에 사용합니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Domain Exception - 비즈니스 규칙 위반 표현</li>
 *   <li>✅ 명확한 예외 이름 - 용도가 명확함</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
public class InvalidSettingException extends RuntimeException {

    /**
     * 메시지로 예외를 생성합니다.
     *
     * @param message 예외 메시지
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public InvalidSettingException(String message) {
        super(message);
    }

    /**
     * 메시지와 원인으로 예외를 생성합니다.
     *
     * @param message 예외 메시지
     * @param cause 원인 예외
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public InvalidSettingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 타입 불일치 시 사용하는 Factory Method.
     *
     * @param key 설정 키
     * @param expectedType 예상 타입
     * @param actualValue 실제 값
     * @return InvalidSettingException
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static InvalidSettingException typeMismatch(
        SettingKey key,
        SettingType expectedType,
        String actualValue
    ) {
        return new InvalidSettingException(
            String.format(
                "Setting 값이 타입과 호환되지 않습니다. 키: %s, 예상 타입: %s, 실제 값: %s",
                key.getValue(), expectedType, actualValue
            )
        );
    }

    /**
     * JSON 스키마 검증 실패 시 사용하는 Factory Method.
     *
     * @param key 설정 키
     * @param validationError 검증 오류 메시지
     * @return InvalidSettingException
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static InvalidSettingException schemaValidationFailed(
        SettingKey key,
        String validationError
    ) {
        return new InvalidSettingException(
            String.format(
                "Setting JSON 스키마 검증 실패. 키: %s, 오류: %s",
                key.getValue(), validationError
            )
        );
    }
}
