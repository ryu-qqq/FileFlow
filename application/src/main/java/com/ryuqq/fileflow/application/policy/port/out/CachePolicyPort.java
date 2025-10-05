package com.ryuqq.fileflow.application.policy.port.out;

import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.policy.UploadPolicy;

import java.util.Optional;

/**
 * UploadPolicy 캐싱을 위한 Outbound Port
 *
 * Hexagonal Architecture의 Outbound Port로서,
 * Application Layer가 캐시 저장소(Redis 등)와 상호작용하기 위한 인터페이스입니다.
 *
 * 캐시 정책:
 * - TTL: 1시간
 * - 저장/업데이트 시 캐시 갱신
 * - 삭제/비활성화 시 캐시 무효화
 *
 * @author sangwon-ryu
 */
public interface CachePolicyPort {

    /**
     * PolicyKey로 캐시된 UploadPolicy를 조회합니다.
     *
     * @param policyKey 조회할 정책의 키
     * @return 캐시된 UploadPolicy (캐시 미스 시 Optional.empty())
     * @throws IllegalArgumentException policyKey가 null인 경우
     */
    Optional<UploadPolicy> get(PolicyKey policyKey);

    /**
     * UploadPolicy를 캐시에 저장합니다.
     * TTL은 1시간으로 설정됩니다.
     *
     * @param uploadPolicy 캐시할 정책
     * @throws IllegalArgumentException uploadPolicy가 null인 경우
     */
    void put(UploadPolicy uploadPolicy);

    /**
     * PolicyKey에 해당하는 캐시를 무효화(삭제)합니다.
     *
     * @param policyKey 무효화할 정책의 키
     * @throws IllegalArgumentException policyKey가 null인 경우
     */
    void evict(PolicyKey policyKey);

    /**
     * 모든 정책 캐시를 무효화합니다.
     * 주의: 전체 캐시 삭제는 신중하게 사용해야 합니다.
     */
    void evictAll();
}
