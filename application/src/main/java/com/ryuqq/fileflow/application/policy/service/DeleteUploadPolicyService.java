package com.ryuqq.fileflow.application.policy.service;

import com.ryuqq.fileflow.application.policy.dto.PolicyKeyDto;
import com.ryuqq.fileflow.application.policy.port.in.DeleteUploadPolicyUseCase;
import com.ryuqq.fileflow.application.policy.port.out.DeleteUploadPolicyPort;
import com.ryuqq.fileflow.application.policy.port.out.LoadUploadPolicyPort;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.policy.UploadPolicy;
import com.ryuqq.fileflow.domain.policy.exception.PolicyNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * UploadPolicy 삭제 Service
 *
 * Hexagonal Architecture의 UseCase 구현체로서,
 * UploadPolicy 삭제 비즈니스 로직을 처리합니다.
 *
 * @author sangwon-ryu
 */
@Service
public class DeleteUploadPolicyService implements DeleteUploadPolicyUseCase {

    private final LoadUploadPolicyPort loadUploadPolicyPort;
    private final DeleteUploadPolicyPort deleteUploadPolicyPort;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param loadUploadPolicyPort 정책 조회 Port
     * @param deleteUploadPolicyPort 정책 삭제 Port
     */
    public DeleteUploadPolicyService(
            LoadUploadPolicyPort loadUploadPolicyPort,
            DeleteUploadPolicyPort deleteUploadPolicyPort
    ) {
        this.loadUploadPolicyPort = Objects.requireNonNull(loadUploadPolicyPort, "LoadUploadPolicyPort must not be null");
        this.deleteUploadPolicyPort = Objects.requireNonNull(deleteUploadPolicyPort, "DeleteUploadPolicyPort must not be null");
    }

    /**
     * UploadPolicy를 삭제합니다.
     *
     * 비즈니스 로직:
     * 1. 기존 정책 조회
     * 2. 활성 상태 검증 (활성화된 정책은 삭제 불가)
     * 3. 정책 삭제
     *
     * @param policyKeyDto 삭제할 정책의 키
     * @throws IllegalArgumentException policyKeyDto가 null인 경우
     * @throws PolicyNotFoundException 정책이 존재하지 않는 경우
     * @throws IllegalStateException 정책이 활성화 상태인 경우
     */
    @Override
    public void deletePolicy(PolicyKeyDto policyKeyDto) {
        Objects.requireNonNull(policyKeyDto, "PolicyKeyDto must not be null");

        PolicyKey policyKey = policyKeyDto.toDomain();

        // 1. 기존 정책 조회
        UploadPolicy existingPolicy = loadUploadPolicyPort.loadByKey(policyKey)
                .orElseThrow(() -> new PolicyNotFoundException(policyKey.getValue()));

        // 2. 활성 상태 검증
        if (existingPolicy.isActive()) {
            throw new IllegalStateException("Cannot delete active policy. Deactivate it first: " + policyKey.getValue());
        }

        // 3. 정책 삭제
        deleteUploadPolicyPort.delete(policyKey);
    }
}
