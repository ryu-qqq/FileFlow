package com.ryuqq.fileflow.domain.iam.usercontext.exception;

/**
 * UserContext Not Found Exception
 *
 * <p>요청한 UserContext를 찾을 수 없을 때 발생하는 Domain Exception입니다.</p>
 *
 * <p><strong>발생 시점:</strong></p>
 * <ul>
 *   <li>존재하지 않는 userId로 UserContext 조회 시</li>
 *   <li>삭제된 UserContext 접근 시</li>
 * </ul>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Domain Exception - 비즈니스 규칙 위반 표현</li>
 *   <li>✅ 명확한 예외 이름 - 용도가 명확함</li>
 *   <li>✅ RuntimeException 상속 - Unchecked Exception</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
public class UserContextNotFoundException extends RuntimeException {

    /**
     * 메시지로 예외를 생성합니다.
     *
     * @param message 예외 메시지
     * @author ryu-qqq
     * @since 2025-10-26
     */
    public UserContextNotFoundException(String message) {
        super(message);
    }

    /**
     * 메시지와 원인으로 예외를 생성합니다.
     *
     * @param message 예외 메시지
     * @param cause 원인 예외
     * @author ryu-qqq
     * @since 2025-10-26
     */
    public UserContextNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * userId로 UserContext를 찾을 수 없을 때 사용하는 Factory Method.
     *
     * @param userId 사용자 ID
     * @return UserContextNotFoundException
     * @author ryu-qqq
     * @since 2025-10-26
     */
    public static UserContextNotFoundException withUserId(Long userId) {
        return new UserContextNotFoundException(
            String.format("사용자를 찾을 수 없습니다: userId=%d", userId)
        );
    }
}
