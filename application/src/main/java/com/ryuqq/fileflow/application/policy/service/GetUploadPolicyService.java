package com.ryuqq.fileflow.application.policy.service;

import com.ryuqq.fileflow.application.policy.dto.PolicyKeyDto;
import com.ryuqq.fileflow.application.policy.dto.UploadPolicyResponse;
import com.ryuqq.fileflow.application.policy.port.in.GetUploadPolicyUseCase;
import com.ryuqq.fileflow.application.policy.port.out.LoadUploadPolicyPort;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.policy.UploadPolicy;
import com.ryuqq.fileflow.domain.policy.exception.PolicyNotFoundException;

import java.util.Objects;

/**
 * UploadPolicy 조회 Service
 *
 * Hexagonal Architecture의 UseCase 구현체로서,
 * UploadPolicy 조회 비즈니스 로직을 처리합니다.
 *
 * @author sangwon-ryu
 */
public class GetUploadPolicyService implements GetUploadPolicyUseCase {

    private final LoadUploadPolicyPort loadUploadPolicyPort;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param loadUploadPolicyPort 정책 조회 Port
     */
    public GetUploadPolicyService(LoadUploadPolicyPort loadUploadPolicyPort) {
        this.loadUploadPolicyPort = Objects.requireNonNull(loadUploadPolicyPort, "LoadUploadPolicyPort must not be null");
    }

    /**
     * PolicyKey로 UploadPolicy를 조회합니다.
     *
     * @param policyKeyDto 조회할 정책의 키
     * @return 조회된 정책 Response
     * @throws IllegalArgumentException policyKeyDto가 null인 경우
     * @throws PolicyNotFoundException 정책이 존재하지 않는 경우
     */
    @Override
    public UploadPolicyResponse getPolicy(PolicyKeyDto policyKeyDto) {
        Objects.requireNonNull(policyKeyDto, "PolicyKeyDto must not be null");

        PolicyKey policyKey = policyKeyDto.toDomain();

        UploadPolicy uploadPolicy = loadUploadPolicyPort.loadByKey(policyKey)
                .orElseThrow(() -> new PolicyNotFoundException(policyKey.getValue()));

        return UploadPolicyResponse.from(uploadPolicy);
    }

    /**
     * PolicyKey로 활성화된 UploadPolicy를 조회합니다.
     *
     * @param policyKeyDto 조회할 정책의 키
     * @return 활성화된 정책 Response
     * @throws IllegalArgumentException policyKeyDto가 null인 경우
     * @throws PolicyNotFoundException 활성화된 정책이 존재하지 않는 경우
     */
    @Override
    public UploadPolicyResponse getActivePolicy(PolicyKeyDto policyKeyDto) {
        Objects.requireNonNull(policyKeyDto, "PolicyKeyDto must not be null");

        PolicyKey policyKey = policyKeyDto.toDomain();

        UploadPolicy uploadPolicy = loadUploadPolicyPort.loadActiveByKey(policyKey)
                .orElseThrow(() -> new PolicyNotFoundException(policyKey.getValue()));

        return UploadPolicyResponse.from(uploadPolicy);
    }
}
