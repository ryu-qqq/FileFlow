package com.ryuqq.fileflow.adapter.rest.iam.permission.controller;

import com.ryuqq.fileflow.adapter.rest.common.dto.ApiResponse;
import com.ryuqq.fileflow.adapter.rest.iam.permission.dto.request.EvaluatePermissionApiRequest;
import com.ryuqq.fileflow.adapter.rest.iam.permission.dto.response.PermissionEvaluationApiResponse;
import com.ryuqq.fileflow.adapter.rest.iam.permission.mapper.PermissionApiMapper;
import com.ryuqq.fileflow.application.iam.permission.dto.command.EvaluatePermissionCommand;
import com.ryuqq.fileflow.application.iam.permission.dto.response.EvaluatePermissionResponse;
import com.ryuqq.fileflow.application.iam.permission.port.in.EvaluatePermissionUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Permission REST API Controller
 *
 * <p>Permission 평가 API를 제공합니다.</p>
 *
 * <p><strong>Endpoint Base Path</strong>: {@code /api/v1/permissions}</p>
 *
 * <p><strong>제공 API</strong>:</p>
 * <ul>
 *   <li>GET /api/v1/permissions/evaluate - Permission 평가 (ABAC 엔진)</li>
 * </ul>
 *
 * <p><strong>헥사고날 아키텍처</strong>:</p>
 * <ul>
 *   <li><strong>Adapter-In (REST API)</strong>: Request → Command 변환</li>
 *   <li><strong>Port-In (UseCase)</strong>: EvaluatePermissionUseCase</li>
 *   <li><strong>Application</strong>: EvaluatePermissionService</li>
 *   <li><strong>Port-Out</strong>: GrantRepositoryPort, AbacEvaluatorPort</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-27
 */
@RestController
@RequestMapping("${api.endpoints.base-v1}${api.endpoints.iam.permission.base}")
public class PermissionController {

    private final EvaluatePermissionUseCase evaluatePermissionUseCase;
    private final PermissionApiMapper mapper;

    /**
     * Constructor - Spring이 의존성 자동 주입
     *
     * @param evaluatePermissionUseCase Permission 평가 UseCase
     * @param mapper DTO 변환 Mapper
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public PermissionController(
        EvaluatePermissionUseCase evaluatePermissionUseCase,
        PermissionApiMapper mapper
    ) {
        this.evaluatePermissionUseCase = evaluatePermissionUseCase;
        this.mapper = mapper;
    }

    /**
     * Permission 평가 API (ABAC 엔진)
     *
     * <p><strong>HTTP Method</strong>: GET</p>
     * <p><strong>Path</strong>: /api/v1/permissions/evaluate</p>
     * <p><strong>Query Parameters</strong>:</p>
     * <ul>
     *   <li>userId - 사용자 ID (필수)</li>
     *   <li>tenantId - 테넌트 ID (필수)</li>
     *   <li>organizationId - 조직 ID (필수)</li>
     *   <li>permissionCode - 권한 코드 (필수, 예: "file.upload")</li>
     *   <li>scope - Scope 코드 (필수, 예: "SELF", "ORGANIZATION", "TENANT")</li>
     *   <li>roleCode - Role 코드 (선택, 예: "UPLOADER")</li>
     *   <li>resourceAttributes - 리소스 속성 (선택, JSON 형식)</li>
     * </ul>
     * <p><strong>Response</strong>: 200 OK + {@link PermissionEvaluationApiResponse}</p>
     *
     * <p><strong>사용 예시:</strong></p>
     * <pre>
     * GET /api/v1/permissions/evaluate
     *   ?userId=1001
     *   &tenantId=10
     *   &organizationId=100
     *   &permissionCode=file.upload
     *   &scope=ORGANIZATION
     *   &resourceAttributes={"size_mb":15.5,"ownerId":1001}
     * </pre>
     *
     * <p><strong>권한 평가 파이프라인 (4단계)</strong>:</p>
     * <ol>
     *   <li>Cache Lookup - user:tenant:org 키로 Grants 조회</li>
     *   <li>Permission 필터링 - 요청된 permissionCode와 일치하는 Grant 추출</li>
     *   <li>Scope 매칭 - Grant의 Scope가 요청 Scope를 포함하는지 검증</li>
     *   <li>ABAC 평가 - CEL 조건식 평가 (조건이 있는 경우)</li>
     * </ol>
     *
     * <p><strong>보수적 거부 (Deny by Default)</strong>:</p>
     * <ul>
     *   <li>Grant가 없으면 즉시 거부 (NO_GRANT)</li>
     *   <li>Scope 불일치 시 즉시 거부 (SCOPE_MISMATCH)</li>
     *   <li>ABAC 조건 불충족 또는 평가 실패 시 거부 (CONDITION_NOT_MET, CONDITION_EVALUATION_FAILED)</li>
     * </ul>
     *
     * @param request Permission 평가 요청 (Query Parameters를 @ModelAttribute로 바인딩)
     * @return 200 OK + Permission 평가 결과
     * @author ryu-qqq
     * @since 2025-10-27
     */
    @GetMapping("/evaluate")
    public ResponseEntity<ApiResponse<PermissionEvaluationApiResponse>> evaluatePermission(
        @Valid @ModelAttribute EvaluatePermissionApiRequest request
    ) {
        // 1. Request → Command 변환
        EvaluatePermissionCommand command = mapper.toCommand(request);

        // 2. UseCase 실행 (4단계 평가 파이프라인)
        EvaluatePermissionResponse response = evaluatePermissionUseCase.execute(command);

        // 3. Response 변환
        PermissionEvaluationApiResponse apiResponse = mapper.toApiResponse(response);

        // 4. 200 OK 응답
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
