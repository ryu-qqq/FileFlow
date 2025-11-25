package com.ryuqq.fileflow.application.common.context;

import com.ryuqq.fileflow.domain.iam.vo.UserContext;

/**
 * UserContext를 ThreadLocal로 관리하는 Holder.
 *
 * <p>Filter에서 JWT 토큰을 파싱하여 UserContext를 생성한 후 ThreadLocal에 저장하고, Application 레이어에서 이를 조회하여 사용합니다.
 *
 * <p><strong>사용 패턴</strong>:
 *
 * <pre>{@code
 * // Filter에서 설정
 * UserContextHolder.set(userContext);
 *
 * // Application에서 조회
 * UserContext context = UserContextHolder.get();
 *
 * // Filter finally에서 정리 (메모리 누수 방지)
 * UserContextHolder.clear();
 * }</pre>
 *
 * <p><strong>주의사항</strong>:
 *
 * <ul>
 *   <li>반드시 요청 처리 완료 후 clear()를 호출해야 합니다.
 *   <li>비동기 처리 시 Context 전파에 주의해야 합니다.
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public final class UserContextHolder {

    private static final ThreadLocal<UserContext> CONTEXT_HOLDER = new ThreadLocal<>();

    private UserContextHolder() {
        // Utility class
    }

    /**
     * 현재 스레드에 UserContext를 설정합니다.
     *
     * @param userContext 사용자 컨텍스트
     */
    public static void set(UserContext userContext) {
        if (userContext == null) {
            clear();
        } else {
            CONTEXT_HOLDER.set(userContext);
        }
    }

    /**
     * 현재 스레드의 UserContext를 반환합니다.
     *
     * @return UserContext 또는 null (설정되지 않은 경우)
     */
    public static UserContext get() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 현재 스레드의 UserContext를 반환합니다.
     *
     * @return UserContext
     * @throws IllegalStateException UserContext가 설정되지 않은 경우
     */
    public static UserContext getRequired() {
        UserContext context = CONTEXT_HOLDER.get();
        if (context == null) {
            throw new IllegalStateException(
                    "UserContext가 설정되지 않았습니다. UserContextFilter가 동작하지 않았을 수 있습니다.");
        }
        return context;
    }

    /**
     * 현재 스레드의 UserContext를 제거합니다.
     *
     * <p>메모리 누수 방지를 위해 요청 처리 완료 후 반드시 호출해야 합니다.
     */
    public static void clear() {
        CONTEXT_HOLDER.remove();
    }
}
