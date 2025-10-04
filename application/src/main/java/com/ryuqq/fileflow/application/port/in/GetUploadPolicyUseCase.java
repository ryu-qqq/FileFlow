package com.ryuqq.fileflow.application.port.in;

import com.ryuqq.fileflow.application.dto.PolicyKeyDto;
import com.ryuqq.fileflow.application.dto.UploadPolicyResponse;

/**
 * UploadPolicy 조회 UseCase
 *
 * Hexagonal Architecture의 Inbound Port로서,
 * PolicyKey로 UploadPolicy를 조회하는 비즈니스 로직을 정의합니다.
 *
 * @author sangwon-ryu
 */
public interface GetUploadPolicyUseCase {

    /**
     * PolicyKey로 UploadPolicy를 조회합니다.
     *
     * @param policyKeyDto 조회할 정책의 키
     * @return 조회된 정책 Response
     * @throws IllegalArgumentException policyKeyDto가 null인 경우
     * @throws com.ryuqq.fileflow.domain.policy.exception.PolicyNotFoundException 정책이 존재하지 않는 경우
     */
    UploadPolicyResponse getPolicy(PolicyKeyDto policyKeyDto);

    /**
     * PolicyKey로 활성화된 UploadPolicy를 조회합니다.
     *
     * @param policyKeyDto 조회할 정책의 키
     * @return 활성화된 정책 Response
     * @throws IllegalArgumentException policyKeyDto가 null인 경우
     * @throws com.ryuqq.fileflow.domain.policy.exception.PolicyNotFoundException 활성화된 정책이 존재하지 않는 경우
     */
    UploadPolicyResponse getActivePolicy(PolicyKeyDto policyKeyDto);
}
