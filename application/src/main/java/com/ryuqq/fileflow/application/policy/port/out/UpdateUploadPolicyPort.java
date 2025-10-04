package com.ryuqq.fileflow.application.policy.port.out;

import com.ryuqq.fileflow.domain.policy.UploadPolicy;

/**
 * UploadPolicy 업데이트를 위한 Outbound Port
 *
 * Hexagonal Architecture의 Outbound Port로서,
 * Application Layer가 기존 UploadPolicy를 업데이트하기 위한 인터페이스입니다.
 *
 * @author sangwon-ryu
 */
public interface UpdateUploadPolicyPort {

    /**
     * UploadPolicy를 업데이트합니다.
     *
     * 비즈니스 규칙:
     * - PolicyKey에 해당하는 정책이 존재하지 않으면 예외를 발생시킵니다
     * - 업데이트 성공 시 업데이트된 정책을 반환합니다
     *
     * @param uploadPolicy 업데이트할 UploadPolicy
     * @return 업데이트된 UploadPolicy
     * @throws IllegalArgumentException uploadPolicy가 null인 경우
     * @throws IllegalStateException PolicyKey에 해당하는 정책이 존재하지 않는 경우
     */
    UploadPolicy update(UploadPolicy uploadPolicy);
}
