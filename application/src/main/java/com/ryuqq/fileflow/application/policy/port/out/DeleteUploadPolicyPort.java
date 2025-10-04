package com.ryuqq.fileflow.application.policy.port.out;

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
     * @param policyKey 삭제할 정책의 키
     * @throws IllegalArgumentException policyKey가 null인 경우
     */
    void delete(PolicyKey policyKey);
}
