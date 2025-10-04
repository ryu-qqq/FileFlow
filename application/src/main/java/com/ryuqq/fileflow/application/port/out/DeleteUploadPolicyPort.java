package com.ryuqq.fileflow.application.port.out;

import com.ryuqq.fileflow.domain.policy.PolicyKey;

/**
 * UploadPolicy 삭제를 위한 Outbound Port
 *
 * Hexagonal Architecture의 Outbound Port로서,
 * Application Layer가 UploadPolicy를 삭제하기 위한 인터페이스입니다.
 *
 * @author sangwon-ryu
 */
public interface DeleteUploadPolicyPort {

    /**
     * PolicyKey에 해당하는 UploadPolicy를 삭제합니다.
     *
     * 비즈니스 규칙:
     * - 활성화된 정책은 삭제할 수 없습니다 (먼저 비활성화 필요)
     * - PolicyKey에 해당하는 정책이 존재하지 않으면 예외를 발생시킵니다
     *
     * @param policyKey 삭제할 정책의 키
     * @throws IllegalArgumentException policyKey가 null인 경우
     * @throws IllegalStateException 정책이 활성화 상태인 경우 또는 존재하지 않는 경우
     */
    void delete(PolicyKey policyKey);
}
