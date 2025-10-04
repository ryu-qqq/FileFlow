package com.ryuqq.fileflow.application.policy.port.in;

import com.ryuqq.fileflow.application.policy.dto.PolicyKeyDto;

/**
 * UploadPolicy 삭제 UseCase
 *
 * Hexagonal Architecture의 Inbound Port로서,
 * UploadPolicy를 삭제하는 비즈니스 로직을 정의합니다.
 *
 * 비즈니스 규칙:
 * 1. 활성화된 정책은 삭제할 수 없음 (먼저 비활성화 필요)
 *
 * @author sangwon-ryu
 */
public interface DeleteUploadPolicyUseCase {

    /**
     * UploadPolicy를 삭제합니다.
     *
     * @param policyKeyDto 삭제할 정책의 키
     * @throws IllegalArgumentException policyKeyDto가 null인 경우
     * @throws com.ryuqq.fileflow.domain.policy.exception.PolicyNotFoundException 정책이 존재하지 않는 경우
     * @throws IllegalStateException 정책이 활성화 상태인 경우
     */
    void deletePolicy(PolicyKeyDto policyKeyDto);
}
