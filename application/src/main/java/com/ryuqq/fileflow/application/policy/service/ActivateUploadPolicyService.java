package com.ryuqq.fileflow.application.policy.service;

import com.ryuqq.fileflow.application.policy.dto.PolicyKeyDto;
import org.springframework.stereotype.Service;
import com.ryuqq.fileflow.application.policy.dto.UploadPolicyResponse;
import com.ryuqq.fileflow.application.policy.port.in.ActivateUploadPolicyUseCase;
import com.ryuqq.fileflow.application.policy.port.out.LoadUploadPolicyPort;
import com.ryuqq.fileflow.application.policy.port.out.UpdateUploadPolicyPort;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import org.springframework.stereotype.Service;
import com.ryuqq.fileflow.domain.policy.UploadPolicy;
import com.ryuqq.fileflow.domain.policy.exception.PolicyNotFoundException;

import java.util.Objects;

/**
 * UploadPolicy 활성화 Service
 *
 * Hexagonal Architecture의 UseCase 구현체로서,
 * UploadPolicy 활성화 비즈니스 로직을 처리합니다.
 *
 * @author sangwon-ryu
 */
@Service
public class ActivateUploadPolicyService implements ActivateUploadPolicyUseCase {

    private final LoadUploadPolicyPort loadUploadPolicyPort;
    private final UpdateUploadPolicyPort updateUploadPolicyPort;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param loadUploadPolicyPort 정책 조회 Port
     * @param updateUploadPolicyPort 정책 업데이트 Port
     */
    public ActivateUploadPolicyService(
            LoadUploadPolicyPort loadUploadPolicyPort,
            UpdateUploadPolicyPort updateUploadPolicyPort
    ) {
        this.loadUploadPolicyPort = Objects.requireNonNull(loadUploadPolicyPort, "LoadUploadPolicyPort must not be null");
        this.updateUploadPolicyPort = Objects.requireNonNull(updateUploadPolicyPort, "UpdateUploadPolicyPort must not be null");
    }

    /**
     * UploadPolicy를 활성화합니다.
     *
     * 비즈니스 로직:
     * 1. 기존 정책 조회
     * 2. 도메인 정책 활성화 (이벤트 발행)
     * 3. 활성화된 정책 저장
     *
     * @param policyKeyDto 활성화할 정책의 키
     * @return 활성화된 정책 Response
     * @throws IllegalArgumentException policyKeyDto가 null인 경우
     * @throws PolicyNotFoundException 정책이 존재하지 않는 경우
     * @throws IllegalStateException 이미 활성화된 경우
     */
    @Override
    public UploadPolicyResponse activatePolicy(PolicyKeyDto policyKeyDto) {
        Objects.requireNonNull(policyKeyDto, "PolicyKeyDto must not be null");

        PolicyKey policyKey = policyKeyDto.toDomain();

        // 1. 기존 정책 조회
        UploadPolicy existingPolicy = loadUploadPolicyPort.loadByKey(policyKey)
                .orElseThrow(() -> new PolicyNotFoundException(policyKey.getValue()));

        // 2. 도메인 정책 활성화
        UploadPolicy activatedPolicy = existingPolicy.activate();

        // 3. 활성화된 정책 저장
        UploadPolicy savedPolicy = updateUploadPolicyPort.update(activatedPolicy);

        return UploadPolicyResponse.from(savedPolicy);
    }
}
