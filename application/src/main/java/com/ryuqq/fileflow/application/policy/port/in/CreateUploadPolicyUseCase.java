package com.ryuqq.fileflow.application.policy.port.in;

import com.ryuqq.fileflow.application.policy.dto.CreateUploadPolicyCommand;
import com.ryuqq.fileflow.application.policy.dto.UploadPolicyResponse;

/**
 * UploadPolicy 생성 UseCase
 *
 * Hexagonal Architecture의 Inbound Port로서,
 * 새로운 UploadPolicy를 생성하는 비즈니스 로직을 정의합니다.
 *
 * 비즈니스 규칙:
 * 1. 동일한 PolicyKey를 가진 정책이 이미 존재하면 실패
 * 2. 생성된 정책은 기본적으로 비활성화 상태
 * 3. 버전은 1로 초기화
 *
 * @author sangwon-ryu
 */
public interface CreateUploadPolicyUseCase {

    /**
     * 새로운 UploadPolicy를 생성합니다.
     *
     * @param command 정책 생성 Command
     * @return 생성된 정책 Response
     * @throws IllegalArgumentException command가 null이거나 유효하지 않은 경우
     * @throws IllegalStateException 동일한 PolicyKey를 가진 정책이 이미 존재하는 경우
     */
    UploadPolicyResponse createPolicy(CreateUploadPolicyCommand command);
}
