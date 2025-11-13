package com.ryuqq.fileflow.application.iam.usercontext.dto.response;

import java.time.LocalDateTime;

/**
 * UserContext 응답 DTO
 *
 * <p>UserContext의 상태를 외부로 전달하기 위한 불변 객체입니다.</p>
 *
 * @param userContextId UserContext ID
 * @param externalUserId 외부 IDP 사용자 ID
 * @param email 사용자 이메일
 * @param deleted 삭제 여부
 * @param createdAt 생성 일시
 * @param updatedAt 수정 일시
 * @author ryu-qqq
 * @since 2025-10-27
 */
public record UserContextResponse(
    Long userContextId,
    String externalUserId,
    String email,
    boolean deleted,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
