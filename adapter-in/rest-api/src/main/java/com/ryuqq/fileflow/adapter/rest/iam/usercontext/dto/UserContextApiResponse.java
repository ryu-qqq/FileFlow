package com.ryuqq.fileflow.adapter.rest.iam.usercontext.dto;

import java.time.LocalDateTime;

/**
 * UserContext API 응답 DTO
 *
 * <p>REST API 응답으로 UserContext 정보를 반환합니다.</p>
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
public record UserContextApiResponse(
    Long userContextId,
    String externalUserId,
    String email,
    boolean deleted,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
