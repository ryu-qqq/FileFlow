package com.ryuqq.fileflow.adapter.rest.iam.permission.mapper;

import com.ryuqq.fileflow.adapter.rest.iam.permission.dto.request.EvaluatePermissionApiRequest;
import com.ryuqq.fileflow.adapter.rest.iam.permission.dto.response.PermissionEvaluationApiResponse;
import com.ryuqq.fileflow.application.iam.permission.dto.command.EvaluatePermissionCommand;
import com.ryuqq.fileflow.application.iam.permission.dto.context.EvaluationContext;
import com.ryuqq.fileflow.application.iam.permission.dto.context.ResourceAttributes;
import com.ryuqq.fileflow.application.iam.permission.dto.response.EvaluatePermissionResponse;
import com.ryuqq.fileflow.domain.iam.permission.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Permission DTO Mapper
 *
 * <p>Permission REST API의 Request/Response DTO를 Application Layer Command/Response로 변환합니다.</p>
 *
 * <p><strong>매핑 패턴</strong>:</p>
 * <ul>
 *   <li>Request → Command (Inbound Adapter → Application)</li>
 *   <li>Response → ApiResponse (Application → Outbound Adapter)</li>
 * </ul>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Static Utility Class 또는 Component 선택 (Component 선택 - DI 활용)</li>
 *   <li>✅ Null-safe 변환</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-27
 */
@Component
public class PermissionApiMapper {

    /**
     * Default Constructor (Spring Component Scan용)
     *
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public PermissionApiMapper() {
    }

    /**
     * EvaluatePermissionRequest → EvaluatePermissionCommand 변환
     *
     * <p>REST API Request DTO를 Application Layer Command로 변환합니다.</p>
     *
     * @param request Permission 평가 요청 DTO
     * @return EvaluatePermissionCommand
     * @throws IllegalArgumentException request가 null이거나 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public EvaluatePermissionCommand toCommand(EvaluatePermissionApiRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("EvaluatePermissionRequest는 null일 수 없습니다");
        }

        // 1. EvaluationContext 생성
        EvaluationContext userContext;
        if (request.hasRoleCode()) {
            userContext = EvaluationContext.withRole(
                request.getUserId(),
                request.getTenantId(),
                request.getOrganizationId(),
                request.getRoleCode()
            );
        } else {
            userContext = EvaluationContext.withoutRole(
                request.getUserId(),
                request.getTenantId(),
                request.getOrganizationId()
            );
        }

        // 2. Scope 변환
        Scope scope = parseScope(request.getScope());

        // 3. ResourceAttributes 생성
        ResourceAttributes resourceAttributes = buildResourceAttributes(request.getResourceAttributes());

        // 4. Command 생성
        return EvaluatePermissionCommand.builder()
            .userContext(userContext)
            .permissionCode(request.getPermissionCode())
            .scope(scope)
            .resourceAttributes(resourceAttributes)
            .build();
    }

    /**
     * EvaluatePermissionResponse → PermissionEvaluationApiResponse 변환
     *
     * <p>Application Layer Response를 REST API Response DTO로 변환합니다.</p>
     *
     * @param response Permission 평가 결과 Response
     * @return PermissionEvaluationApiResponse
     * @throws IllegalArgumentException response가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public PermissionEvaluationApiResponse toApiResponse(EvaluatePermissionResponse response) {
        if (response == null) {
            throw new IllegalArgumentException("EvaluatePermissionResponse는 null일 수 없습니다");
        }

        return new PermissionEvaluationApiResponse(
            response.allowed(),
            response.denialReason() != null ? response.denialReason().name() : null,
            response.message(),
            response.evaluatedCondition()
        );
    }

    /**
     * Scope 문자열을 Scope Enum으로 변환합니다
     *
     * <p>대소문자 구분 없이 변환하며, 유효하지 않은 Scope는 예외를 발생시킵니다.</p>
     *
     * @param scopeCode Scope 코드 문자열 (예: "SELF", "ORGANIZATION", "TENANT")
     * @return Scope Enum
     * @throws IllegalArgumentException scopeCode가 null, 빈 문자열, 또는 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-27
     */
    private Scope parseScope(String scopeCode) {
        if (scopeCode == null || scopeCode.isBlank()) {
            throw new IllegalArgumentException("Scope 코드는 필수입니다");
        }

        try {
            return Scope.valueOf(scopeCode.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                String.format("유효하지 않은 Scope 코드입니다: %s (허용: SELF, ORGANIZATION, TENANT)", scopeCode),
                e
            );
        }
    }

    /**
     * Request의 resourceAttributes Map을 ResourceAttributes 객체로 변환합니다
     *
     * <p>Map이 null이거나 비어있으면 빈 ResourceAttributes를 반환합니다.</p>
     *
     * @param attributesMap 리소스 속성 Map (Nullable)
     * @return ResourceAttributes 객체 (빈 객체 가능)
     * @author ryu-qqq
     * @since 2025-10-27
     */
    private ResourceAttributes buildResourceAttributes(Map<String, Object> attributesMap) {
        if (attributesMap == null || attributesMap.isEmpty()) {
            return ResourceAttributes.empty();
        }

        // Builder 패턴으로 ResourceAttributes 생성
        ResourceAttributes.ResourceAttributesBuilder builder = ResourceAttributes.builder();
        attributesMap.forEach(builder::attribute);
        return builder.build();
    }
}
