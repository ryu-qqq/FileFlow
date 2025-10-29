package com.ryuqq.fileflow.adapter.rest.iam.usercontext.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * UserContext 생성 요청 DTO
 *
 * <p>외부 IDP로부터 인증된 사용자의 UserContext 생성 요청을 받습니다.</p>
 *
 * <p><strong>사용 시점</strong>: OAuth 로그인 성공 후 최초 사용자 등록 시</p>
 *
 * @param externalUserId 외부 IDP 사용자 식별자 (필수)
 * @param email 사용자 이메일 (필수, 이메일 형식 검증)
 * @author ryu-qqq
 * @since 2025-10-27
 */
public record CreateUserContextApiRequest(
    @NotBlank(message = "외부 사용자 ID는 필수입니다")
    String externalUserId,

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    String email
) {
}
