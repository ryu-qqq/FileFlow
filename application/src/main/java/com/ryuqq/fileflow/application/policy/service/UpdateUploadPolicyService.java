package com.ryuqq.fileflow.application.policy.service;

import com.ryuqq.fileflow.application.policy.dto.UpdateUploadPolicyCommand;
import com.ryuqq.fileflow.application.policy.dto.UploadPolicyResponse;
import com.ryuqq.fileflow.application.policy.port.in.UpdateUploadPolicyUseCase;
import com.ryuqq.fileflow.application.policy.port.out.LoadUploadPolicyPort;
import com.ryuqq.fileflow.application.policy.port.out.UpdateUploadPolicyPort;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import org.springframework.stereotype.Service;
import com.ryuqq.fileflow.domain.policy.UploadPolicy;
import com.ryuqq.fileflow.domain.policy.exception.PolicyNotFoundException;

import java.util.Objects;

/**
 * UploadPolicy 업데이트 Service
 *
 * Hexagonal Architecture의 UseCase 구현체로서,
 * UploadPolicy 업데이트 비즈니스 로직을 처리합니다.
 *
 * @author sangwon-ryu
 */
@Service
public class UpdateUploadPolicyService implements UpdateUploadPolicyUseCase {

    private final LoadUploadPolicyPort loadUploadPolicyPort;
    private final UpdateUploadPolicyPort updateUploadPolicyPort;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param loadUploadPolicyPort 정책 조회 Port
     * @param updateUploadPolicyPort 정책 업데이트 Port
     */
    public UpdateUploadPolicyService(
            LoadUploadPolicyPort loadUploadPolicyPort,
            UpdateUploadPolicyPort updateUploadPolicyPort
    ) {
        this.loadUploadPolicyPort = Objects.requireNonNull(loadUploadPolicyPort, "LoadUploadPolicyPort must not be null");
        this.updateUploadPolicyPort = Objects.requireNonNull(updateUploadPolicyPort, "UpdateUploadPolicyPort must not be null");
    }

    /**
     * UploadPolicy를 업데이트합니다.
     *
     * 비즈니스 로직:
     * 1. 기존 정책 조회
     * 2. 도메인 정책 업데이트 (버전 자동 증가 + 이벤트 발행)
     * 3. 업데이트된 정책 저장
     *
     * @param command 정책 업데이트 Command
     * @return 업데이트된 정책 Response
     * @throws IllegalArgumentException command가 null이거나 유효하지 않은 경우
     * @throws PolicyNotFoundException 정책이 존재하지 않는 경우
     */
    @Override
    public UploadPolicyResponse updatePolicy(UpdateUploadPolicyCommand command) {
        Objects.requireNonNull(command, "UpdateUploadPolicyCommand must not be null");

        PolicyKey policyKey = command.getPolicyKey();

        // 1. 기존 정책 조회
        UploadPolicy existingPolicy = loadUploadPolicyPort.loadByKey(policyKey)
                .orElseThrow(() -> new PolicyNotFoundException(policyKey.getValue()));

        // 2. 도메인 정책 업데이트 (버전 증가 + 이벤트 발행)
        UploadPolicy updatedPolicy = existingPolicy.updatePolicy(
                command.getFileTypePolicies(),
                command.changedBy()
        );

        // 3. 업데이트된 정책 저장
        UploadPolicy savedPolicy = updateUploadPolicyPort.update(updatedPolicy);

        return UploadPolicyResponse.from(savedPolicy);
    }
}
