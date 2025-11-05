package com.ryuqq.fileflow.adapter.rest.iam.usercontext.mapper;

import com.ryuqq.fileflow.adapter.rest.iam.usercontext.dto.request.AssignRoleApiRequest;
import com.ryuqq.fileflow.adapter.rest.iam.usercontext.dto.request.CreateUserContextApiRequest;
import com.ryuqq.fileflow.adapter.rest.iam.usercontext.dto.request.RevokeRoleApiRequest;
import com.ryuqq.fileflow.adapter.rest.iam.usercontext.dto.response.UserContextApiResponse;
import com.ryuqq.fileflow.application.iam.usercontext.dto.command.AssignRoleCommand;
import com.ryuqq.fileflow.application.iam.usercontext.dto.command.CreateUserContextCommand;
import com.ryuqq.fileflow.application.iam.usercontext.dto.command.RevokeRoleCommand;
import com.ryuqq.fileflow.application.iam.usercontext.dto.response.UserContextResponse;
/**
 * UserContext DTO Mapper
 *
 * <p>REST API DTO ↔ Application DTO 변환을 담당합니다.</p>
 *
 * <p><strong>규칙 준수</strong>:</p>
 * <ul>
 *   <li>❌ Lombok 사용 안함</li>
 *   <li>✅ Pure Java 구현</li>
 *   <li>✅ Static Utility Class (Stateless, 인스턴스 생성 금지)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-27
 */
public final class UserContextApiMapper {

    /**
     * Private Constructor - 인스턴스 생성 방지
     *
     * @throws UnsupportedOperationException 항상 발생
     * @author ryu-qqq
     * @since 2025-10-27
     */
    private UserContextApiMapper() {
        throw new UnsupportedOperationException("Utility 클래스는 인스턴스화할 수 없습니다");
    }

    /**
     * REST API Request → Application Command 변환
     *
     * @param request UserContext 생성 요청
     * @return CreateUserContextCommand
     * @throws IllegalArgumentException request가 null인 경우
     */
    public static CreateUserContextCommand toCommand(CreateUserContextApiRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("CreateUserContextRequest는 필수입니다");
        }

        return new CreateUserContextCommand(
            request.externalUserId(),
            request.email()
        );
    }

    /**
     * Application Response → REST API Response 변환
     *
     * @param response UserContext 응답
     * @return UserContextApiResponse
     * @throws IllegalArgumentException response가 null인 경우
     */
    public static UserContextApiResponse toApiResponse(UserContextResponse response) {
        if (response == null) {
            throw new IllegalArgumentException("UserContextResponse는 필수입니다");
        }

        return new UserContextApiResponse(
            response.userContextId(),
            response.externalUserId(),
            response.email(),
            response.deleted(),
            response.createdAt(),
            response.updatedAt()
        );
    }

    /**
     * REST API Request → Application Command 변환 (Role 할당)
     *
     * @param userId User ID
     * @param request Role 할당 요청
     * @return AssignRoleCommand
     * @throws IllegalArgumentException request가 null인 경우
     * @author ryu-qqq
     * @since 2025-11-03
     */
    public static AssignRoleCommand toAssignRoleCommand(Long userId, AssignRoleApiRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AssignRoleApiRequest는 필수입니다");
        }
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId는 양수여야 합니다");
        }

        return new AssignRoleCommand(
            userId,
            request.tenantId(),
            request.organizationId(),
            request.membershipType()
        );
    }

    /**
     * REST API Request → Application Command 변환 (Role 철회)
     *
     * @param userId User ID
     * @param request Role 철회 요청
     * @return RevokeRoleCommand
     * @throws IllegalArgumentException request가 null인 경우
     * @author ryu-qqq
     * @since 2025-11-03
     */
    public static RevokeRoleCommand toRevokeRoleCommand(Long userId, RevokeRoleApiRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("RevokeRoleApiRequest는 필수입니다");
        }
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId는 양수여야 합니다");
        }

        return new RevokeRoleCommand(
            userId,
            request.tenantId(),
            request.organizationId()
        );
    }
}
