package com.ryuqq.fileflow.adapter.rest.controller;

import com.ryuqq.fileflow.adapter.rest.dto.request.UpdatePolicyRequest;
import com.ryuqq.fileflow.adapter.rest.dto.response.PolicyResponse;
import com.ryuqq.fileflow.application.policy.dto.PolicyKeyDto;
import com.ryuqq.fileflow.application.policy.dto.UpdateUploadPolicyCommand;
import com.ryuqq.fileflow.application.policy.dto.UploadPolicyResponse;
import com.ryuqq.fileflow.application.policy.port.in.ActivateUploadPolicyUseCase;
import com.ryuqq.fileflow.application.policy.port.in.GetUploadPolicyUseCase;
import com.ryuqq.fileflow.application.policy.port.in.UpdateUploadPolicyUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * Policy REST Controller
 *
 * 정책 관리 REST API를 제공합니다.
 * Hexagonal Architecture의 Inbound Adapter로서 동작합니다.
 *
 * 제약사항:
 * - NO Lombok
 * - UseCase만 의존
 * - NO Inner Class
 *
 * @author sangwon-ryu
 */
@RestController
@RequestMapping("/api/v1/policies")
public class PolicyController {

    private static final int POLICY_KEY_PARTS_COUNT = 3;

    private final GetUploadPolicyUseCase getUploadPolicyUseCase;
    private final UpdateUploadPolicyUseCase updateUploadPolicyUseCase;
    private final ActivateUploadPolicyUseCase activateUploadPolicyUseCase;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param getUploadPolicyUseCase 정책 조회 UseCase
     * @param updateUploadPolicyUseCase 정책 업데이트 UseCase
     * @param activateUploadPolicyUseCase 정책 활성화 UseCase
     */
    public PolicyController(
            GetUploadPolicyUseCase getUploadPolicyUseCase,
            UpdateUploadPolicyUseCase updateUploadPolicyUseCase,
            ActivateUploadPolicyUseCase activateUploadPolicyUseCase
    ) {
        this.getUploadPolicyUseCase = Objects.requireNonNull(
                getUploadPolicyUseCase,
                "GetUploadPolicyUseCase must not be null"
        );
        this.updateUploadPolicyUseCase = Objects.requireNonNull(
                updateUploadPolicyUseCase,
                "UpdateUploadPolicyUseCase must not be null"
        );
        this.activateUploadPolicyUseCase = Objects.requireNonNull(
                activateUploadPolicyUseCase,
                "ActivateUploadPolicyUseCase must not be null"
        );
    }

    /**
     * GET /api/v1/policies/{policyKey}
     * 특정 정책을 조회합니다.
     *
     * @param policyKey 정책 키
     * @return 200 OK with PolicyResponse
     */
    @GetMapping("/{policyKey}")
    public ResponseEntity<PolicyResponse> getPolicy(@PathVariable String policyKey) {
        Objects.requireNonNull(policyKey, "policyKey must not be null");

        PolicyKeyDto policyKeyDto = parsePolicyKey(policyKey);
        UploadPolicyResponse uploadPolicyResponse = getUploadPolicyUseCase.getPolicy(policyKeyDto);

        return ResponseEntity.ok(PolicyResponse.from(uploadPolicyResponse));
    }

    /**
     * GET /api/v1/policies
     * 활성화된 정책을 조회합니다.
     * (현재 구현에서는 policyKey를 쿼리 파라미터로 받아 활성화된 정책을 조회)
     *
     * Note: 향후 전체 정책 목록 조회 기능으로 확장 가능
     *
     * @param policyKey 정책 키 (쿼리 파라미터로 전달 예정, 현재는 기본값 사용)
     * @return 501 NOT_IMPLEMENTED
     */
    @GetMapping
    public ResponseEntity<PolicyResponse> getPolicies() {
        // TODO: 향후 전체 정책 목록 조회 기능 구현 필요
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * PUT /api/v1/policies/{policyKey}
     * 정책을 업데이트합니다.
     *
     * @param policyKey 정책 키
     * @param request 업데이트 요청
     * @return 200 OK with PolicyResponse
     */
    @PutMapping("/{policyKey}")
    public ResponseEntity<PolicyResponse> updatePolicy(
            @PathVariable String policyKey,
            @Valid @RequestBody UpdatePolicyRequest request
    ) {
        Objects.requireNonNull(policyKey, "policyKey must not be null");
        Objects.requireNonNull(request, "request must not be null");

        PolicyKeyDto policyKeyDto = parsePolicyKey(policyKey);
        UpdateUploadPolicyCommand command = request.toCommand(policyKeyDto);
        UploadPolicyResponse uploadPolicyResponse = updateUploadPolicyUseCase.updatePolicy(command);

        return ResponseEntity.ok(PolicyResponse.from(uploadPolicyResponse));
    }

    /**
     * POST /api/v1/policies/{policyKey}/activate
     * 정책을 활성화합니다.
     *
     * @param policyKey 정책 키
     * @return 200 OK with PolicyResponse
     */
    @PostMapping("/{policyKey}/activate")
    public ResponseEntity<PolicyResponse> activatePolicy(@PathVariable String policyKey) {
        Objects.requireNonNull(policyKey, "policyKey must not be null");

        PolicyKeyDto policyKeyDto = parsePolicyKey(policyKey);
        UploadPolicyResponse uploadPolicyResponse = activateUploadPolicyUseCase.activatePolicy(policyKeyDto);

        return ResponseEntity.ok(PolicyResponse.from(uploadPolicyResponse));
    }

    /**
     * PolicyKey 문자열을 파싱하여 PolicyKeyDto로 변환합니다.
     * 형식: {tenantId}:{userType}:{serviceType}
     *
     * @param policyKey 정책 키 문자열
     * @return PolicyKeyDto
     * @throws IllegalArgumentException 형식이 올바르지 않은 경우
     */
    private PolicyKeyDto parsePolicyKey(String policyKey) {
        String[] parts = policyKey.split(":");
        if (parts.length != POLICY_KEY_PARTS_COUNT) {
            throw new IllegalArgumentException(
                    String.format("Invalid policyKey format. Expected format: {tenantId}:{userType}:{serviceType}, but got: %s", policyKey)
            );
        }
        return new PolicyKeyDto(parts[0], parts[1], parts[2]);
    }
}
