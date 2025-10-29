package com.ryuqq.fileflow.adapter.rest.iam.usercontext.controller;

import com.ryuqq.fileflow.adapter.rest.common.dto.ApiResponse;
import com.ryuqq.fileflow.adapter.rest.iam.usercontext.dto.request.CreateUserContextApiRequest;
import com.ryuqq.fileflow.adapter.rest.iam.usercontext.dto.response.UserContextApiResponse;
import com.ryuqq.fileflow.adapter.rest.iam.usercontext.mapper.UserContextApiMapper;
import com.ryuqq.fileflow.application.iam.usercontext.dto.command.CreateUserContextCommand;
import com.ryuqq.fileflow.application.iam.usercontext.dto.response.UserContextResponse;
import com.ryuqq.fileflow.application.iam.usercontext.port.in.CreateUserContextUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * UserContext REST API Controller
 *
 * <p>UserContext 생성, 조회, 수정 API를 제공합니다.</p>
 *
 * <p><strong>Endpoint Base Path</strong>: {@code /api/v1/user-contexts}</p>
 *
 * <p><strong>제공 API</strong>:</p>
 * <ul>
 *   <li>POST /api/v1/user-contexts - UserContext 생성</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-27
 */
@RestController
@RequestMapping("${api.endpoints.base-v1}${api.endpoints.iam.user-context.base}")
public class UserContextController {

    private final CreateUserContextUseCase createUserContextUseCase;
    private final UserContextApiMapper mapper;

    public UserContextController(
        CreateUserContextUseCase createUserContextUseCase,
        UserContextApiMapper mapper
    ) {
        this.createUserContextUseCase = createUserContextUseCase;
        this.mapper = mapper;
    }

    /**
     * UserContext 생성 API
     *
     * <p><strong>HTTP Method</strong>: POST</p>
     * <p><strong>Path</strong>: /api/v1/user-contexts</p>
     * <p><strong>Request Body</strong>: {@link CreateUserContextApiRequest}</p>
     * <p><strong>Response</strong>: 201 Created + {@link UserContextApiResponse}</p>
     *
     * <p><strong>비즈니스 규칙</strong>:</p>
     * <ul>
     *   <li>중복 externalUserId 검증 - 409 Conflict 반환</li>
     *   <li>Email 형식 검증 - 400 Bad Request 반환</li>
     * </ul>
     *
     * @param request UserContext 생성 요청
     * @return 201 Created + UserContext 응답
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserContextApiResponse>> createUserContext(
        @Valid @RequestBody CreateUserContextApiRequest request
    ) {
        // 1. Request → Command 변환
        CreateUserContextCommand command = mapper.toCommand(request);

        // 2. UseCase 실행
        UserContextResponse response = createUserContextUseCase.createUserContext(command);

        // 3. Response 변환
        UserContextApiResponse apiResponse = mapper.toApiResponse(response);

        // 4. 201 Created 응답
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.ofSuccess(apiResponse));
    }
}
