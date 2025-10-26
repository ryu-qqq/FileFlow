package com.ryuqq.fileflow.domain.settings.exception;

import com.ryuqq.fileflow.domain.settings.SettingKey;
import com.ryuqq.fileflow.domain.settings.SettingLevel;

/**
 * Setting Not Found Exception
 *
 * <p>요청한 Setting을 찾을 수 없을 때 발생하는 Domain Exception입니다.</p>
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
public class SettingNotFoundException extends RuntimeException {

    /**
     * 메시지로 예외를 생성합니다.
     *
     * @param message 예외 메시지
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public SettingNotFoundException(String message) {
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
    public SettingNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * ID로 Setting을 찾을 수 없을 때 사용하는 Factory Method.
     *
     * @param id Setting ID
     * @return SettingNotFoundException
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static SettingNotFoundException withId(Long id) {
        return new SettingNotFoundException("Setting을 찾을 수 없습니다. ID: " + id);
    }

    /**
     * 키와 레벨로 Setting을 찾을 수 없을 때 사용하는 Factory Method.
     *
     * @param key 설정 키
     * @param level 설정 레벨
     * @param contextId 컨텍스트 ID
     * @return SettingNotFoundException
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static SettingNotFoundException withKeyAndLevel(
        SettingKey key,
        SettingLevel level,
        Long contextId
    ) {
        return new SettingNotFoundException(
            String.format(
                "Setting을 찾을 수 없습니다. 키: %s, 레벨: %s, contextId: %s",
                key.getValue(), level, contextId
            )
        );
    }
}
