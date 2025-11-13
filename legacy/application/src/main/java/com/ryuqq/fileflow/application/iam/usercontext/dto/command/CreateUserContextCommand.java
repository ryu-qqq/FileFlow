package com.ryuqq.fileflow.application.iam.usercontext.dto.command;

/**
 * UserContext 생성 Command
 *
 * <p>외부 IDP(Identity Provider)로부터 인증된 사용자의 UserContext를 생성합니다.</p>
 *
 * <p><strong>사용 시점</strong>: OAuth 로그인 성공 후 최초 사용자 등록</p>
 *
 * @param externalUserId 외부 IDP 사용자 식별자 (필수)
 * @param email 사용자 이메일 (필수)
 * @author ryu-qqq
 * @since 2025-10-27
 */
public record CreateUserContextCommand(
    String externalUserId,
    String email
) {
    /**
     * Compact Constructor - 유효성 검증
     *
     * @throws IllegalArgumentException externalUserId 또는 email이 null이거나 빈 문자열인 경우
     */
    public CreateUserContextCommand {
        if (externalUserId == null || externalUserId.isBlank()) {
            throw new IllegalArgumentException("외부 사용자 ID는 필수입니다");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다");
        }
    }
}
