package com.ryuqq.fileflow.adapter.rest.iam.usercontext.mapper;

import com.ryuqq.fileflow.adapter.rest.iam.usercontext.dto.request.CreateUserContextApiRequest;
import com.ryuqq.fileflow.adapter.rest.iam.usercontext.dto.response.UserContextApiResponse;
import com.ryuqq.fileflow.application.iam.usercontext.dto.command.CreateUserContextCommand;
import com.ryuqq.fileflow.application.iam.usercontext.dto.response.UserContextResponse;
import org.springframework.stereotype.Component;

/**
 * UserContext DTO Mapper
 *
 * <p>REST API DTO ↔ Application DTO 변환을 담당합니다.</p>
 *
 * <p><strong>규칙 준수</strong>:</p>
 * <ul>
 *   <li>❌ Lombok 사용 안함</li>
 *   <li>✅ Pure Java 구현</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-27
 */
@Component
public class UserContextApiMapper {

    /**
     * REST API Request → Application Command 변환
     *
     * @param request UserContext 생성 요청
     * @return CreateUserContextCommand
     * @throws IllegalArgumentException request가 null인 경우
     */
    public CreateUserContextCommand toCommand(CreateUserContextApiRequest request) {
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
    public UserContextApiResponse toApiResponse(UserContextResponse response) {
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
}
