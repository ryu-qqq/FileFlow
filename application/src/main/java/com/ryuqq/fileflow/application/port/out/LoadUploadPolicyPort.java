package com.ryuqq.fileflow.application.port.out;

import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.policy.UploadPolicy;

import java.util.Optional;

/**
 * UploadPolicy 조회를 위한 Outbound Port
 *
 * Hexagonal Architecture의 Outbound Port로서,
 * Application Layer가 외부 저장소(Database, Cache 등)로부터
 * UploadPolicy를 조회하기 위한 인터페이스입니다.
 *
 * @author sangwon-ryu
 */
public interface LoadUploadPolicyPort {

    /**
     * PolicyKey로 UploadPolicy를 조회합니다.
     *
     * @param policyKey 조회할 정책의 키
     * @return 조회된 UploadPolicy (존재하지 않으면 Optional.empty())
     * @throws IllegalArgumentException policyKey가 null인 경우
     */
    Optional<UploadPolicy> loadByKey(PolicyKey policyKey);

    /**
     * PolicyKey로 활성화된 UploadPolicy를 조회합니다.
     *
     * @param policyKey 조회할 정책의 키
     * @return 활성화된 UploadPolicy (존재하지 않거나 비활성화 상태면 Optional.empty())
     * @throws IllegalArgumentException policyKey가 null인 경우
     */
    Optional<UploadPolicy> loadActiveByKey(PolicyKey policyKey);
}
