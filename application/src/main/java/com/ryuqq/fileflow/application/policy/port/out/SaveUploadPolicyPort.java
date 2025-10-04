package com.ryuqq.fileflow.application.policy.port.out;

import com.ryuqq.fileflow.domain.policy.UploadPolicy;

/**
 * UploadPolicy 저장을 위한 Outbound Port
 *
 * Hexagonal Architecture의 Outbound Port로서,
 * Application Layer가 UploadPolicy를 외부 저장소에
 * 저장하기 위한 인터페이스입니다.
 *
 * @author sangwon-ryu
 */
public interface SaveUploadPolicyPort {

    /**
     * UploadPolicy를 저장합니다.
     *
     * 비즈니스 규칙:
     * - 동일한 PolicyKey를 가진 정책이 이미 존재하는 경우 예외를 발생시킵니다
     * - 저장 성공 시 저장된 정책을 반환합니다
     *
     * @param uploadPolicy 저장할 UploadPolicy
     * @return 저장된 UploadPolicy
     * @throws IllegalArgumentException uploadPolicy가 null인 경우
     * @throws IllegalStateException 동일한 PolicyKey를 가진 정책이 이미 존재하는 경우
     */
    UploadPolicy save(UploadPolicy uploadPolicy);
}
