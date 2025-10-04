package com.ryuqq.fileflow.application.service;

import com.ryuqq.fileflow.application.dto.PolicyKeyDto;
import com.ryuqq.fileflow.application.dto.UploadPolicyResponse;
import com.ryuqq.fileflow.application.port.in.DeactivateUploadPolicyUseCase;
import com.ryuqq.fileflow.application.port.out.LoadUploadPolicyPort;
import com.ryuqq.fileflow.application.port.out.UpdateUploadPolicyPort;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.policy.UploadPolicy;
import com.ryuqq.fileflow.domain.policy.exception.PolicyNotFoundException;

import java.util.Objects;

/**
 * UploadPolicy 비활성화 Service
 *
 * Hexagonal Architecture의 UseCase 구현체로서,
 * UploadPolicy 비활성화 비즈니스 로직을 처리합니다.
 *
 * @author sangwon-ryu
 */
public class DeactivateUploadPolicyService implements DeactivateUploadPolicyUseCase {

    private final LoadUploadPolicyPort loadUploadPolicyPort;
    private final UpdateUploadPolicyPort updateUploadPolicyPort;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param loadUploadPolicyPort 정책 조회 Port
     * @param updateUploadPolicyPort 정책 업데이트 Port
     */
    public DeactivateUploadPolicyService(
            LoadUploadPolicyPort loadUploadPolicyPort,
            UpdateUploadPolicyPort updateUploadPolicyPort
    ) {
        this.loadUploadPolicyPort = Objects.requireNonNull(loadUploadPolicyPort, "LoadUploadPolicyPort must not be null");
        this.updateUploadPolicyPort = Objects.requireNonNull(updateUploadPolicyPort, "UpdateUploadPolicyPort must not be null");
    }

    /**
     * UploadPolicy를 비활성화합니다.
     *
     * 비즈니스 로직:
     * 1. 기존 정책 조회
     * 2. 도메인 정책 비활성화
     * 3. 비활성화된 정책 저장
     *
     * @param policyKeyDto 비활성화할 정책의 키
     * @return 비활성화된 정책 Response
     * @throws IllegalArgumentException policyKeyDto가 null인 경우
     * @throws PolicyNotFoundException 정책이 존재하지 않는 경우
     * @throws IllegalStateException 이미 비활성화된 경우
     */
    @Override
    public UploadPolicyResponse deactivatePolicy(PolicyKeyDto policyKeyDto) {
        Objects.requireNonNull(policyKeyDto, "PolicyKeyDto must not be null");

        PolicyKey policyKey = policyKeyDto.toDomain();

        // 1. 기존 정책 조회
        UploadPolicy existingPolicy = loadUploadPolicyPort.loadByKey(policyKey)
                .orElseThrow(() -> new PolicyNotFoundException(policyKey.getValue()));

        // 2. 도메인 정책 비활성화
        UploadPolicy deactivatedPolicy = existingPolicy.deactivate();

        // 3. 비활성화된 정책 저장
        UploadPolicy savedPolicy = updateUploadPolicyPort.update(deactivatedPolicy);

        return UploadPolicyResponse.from(savedPolicy);
    }
}
