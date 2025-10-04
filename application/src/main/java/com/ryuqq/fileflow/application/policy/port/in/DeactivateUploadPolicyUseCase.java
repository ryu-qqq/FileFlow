package com.ryuqq.fileflow.application.policy.port.in;

import com.ryuqq.fileflow.application.policy.dto.PolicyKeyDto;
import com.ryuqq.fileflow.application.policy.dto.UploadPolicyResponse;

/**
 * UploadPolicy 비활성화 UseCase
 *
 * Hexagonal Architecture의 Inbound Port로서,
 * UploadPolicy를 비활성화하는 비즈니스 로직을 정의합니다.
 *
 * 비즈니스 규칙:
 * 1. 이미 비활성화된 정책은 다시 비활성화할 수 없음
 *
 * @author sangwon-ryu
 */
public interface DeactivateUploadPolicyUseCase {

    /**
     * UploadPolicy를 비활성화합니다.
     *
     * @param policyKeyDto 비활성화할 정책의 키
     * @return 비활성화된 정책 Response
     * @throws IllegalArgumentException policyKeyDto가 null인 경우
     * @throws com.ryuqq.fileflow.domain.policy.exception.PolicyNotFoundException 정책이 존재하지 않는 경우
     * @throws IllegalStateException 이미 비활성화된 경우
     */
    UploadPolicyResponse deactivatePolicy(PolicyKeyDto policyKeyDto);
}
