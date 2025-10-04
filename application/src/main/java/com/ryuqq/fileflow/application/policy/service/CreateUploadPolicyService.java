package com.ryuqq.fileflow.application.policy.service;

import com.ryuqq.fileflow.application.policy.dto.CreateUploadPolicyCommand;
import com.ryuqq.fileflow.application.policy.dto.UploadPolicyResponse;
import com.ryuqq.fileflow.application.policy.port.in.CreateUploadPolicyUseCase;
import com.ryuqq.fileflow.application.policy.port.out.LoadUploadPolicyPort;
import com.ryuqq.fileflow.application.policy.port.out.SaveUploadPolicyPort;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.policy.UploadPolicy;

import java.util.Objects;

/**
 * UploadPolicy 생성 Service
 *
 * Hexagonal Architecture의 UseCase 구현체로서,
 * UploadPolicy 생성 비즈니스 로직을 처리합니다.
 *
 * @author sangwon-ryu
 */
public class CreateUploadPolicyService implements CreateUploadPolicyUseCase {

    private final LoadUploadPolicyPort loadUploadPolicyPort;
    private final SaveUploadPolicyPort saveUploadPolicyPort;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param loadUploadPolicyPort 정책 조회 Port
     * @param saveUploadPolicyPort 정책 저장 Port
     */
    public CreateUploadPolicyService(
            LoadUploadPolicyPort loadUploadPolicyPort,
            SaveUploadPolicyPort saveUploadPolicyPort
    ) {
        this.loadUploadPolicyPort = Objects.requireNonNull(loadUploadPolicyPort, "LoadUploadPolicyPort must not be null");
        this.saveUploadPolicyPort = Objects.requireNonNull(saveUploadPolicyPort, "SaveUploadPolicyPort must not be null");
    }

    /**
     * 새로운 UploadPolicy를 생성합니다.
     *
     * 비즈니스 로직:
     * 1. 동일한 PolicyKey를 가진 정책이 이미 존재하는지 확인
     * 2. UploadPolicy 도메인 객체 생성
     * 3. 정책 저장
     *
     * @param command 정책 생성 Command
     * @return 생성된 정책 Response
     * @throws IllegalArgumentException command가 null이거나 유효하지 않은 경우
     * @throws IllegalStateException 동일한 PolicyKey를 가진 정책이 이미 존재하는 경우
     */
    @Override
    public UploadPolicyResponse createPolicy(CreateUploadPolicyCommand command) {
        Objects.requireNonNull(command, "CreateUploadPolicyCommand must not be null");

        PolicyKey policyKey = command.getPolicyKey();

        // 1. 중복 정책 확인
        loadUploadPolicyPort.loadByKey(policyKey).ifPresent(existing -> {
            throw new IllegalStateException("Policy already exists with key: " + policyKey.getValue());
        });

        // 2. UploadPolicy 생성
        UploadPolicy uploadPolicy = UploadPolicy.create(
                policyKey,
                command.getFileTypePolicies(),
                command.getRateLimiting(),
                command.effectiveFrom(),
                command.effectiveUntil()
        );

        // 3. 정책 저장
        UploadPolicy savedPolicy = saveUploadPolicyPort.save(uploadPolicy);

        return UploadPolicyResponse.from(savedPolicy);
    }
}
