package com.ryuqq.fileflow.application.policy.port.in;

import com.ryuqq.fileflow.application.policy.dto.UpdateUploadPolicyCommand;
import com.ryuqq.fileflow.application.policy.dto.UploadPolicyResponse;

/**
 * UploadPolicy 업데이트 UseCase
 *
 * Hexagonal Architecture의 Inbound Port로서,
 * 기존 UploadPolicy를 업데이트하는 비즈니스 로직을 정의합니다.
 *
 * 비즈니스 규칙:
 * 1. 버전이 자동으로 1 증가
 * 2. PolicyUpdatedEvent 발행
 * 3. 정책이 존재하지 않으면 실패
 *
 * @author sangwon-ryu
 */
public interface UpdateUploadPolicyUseCase {

    /**
     * UploadPolicy를 업데이트합니다.
     *
     * @param command 정책 업데이트 Command
     * @return 업데이트된 정책 Response
     * @throws IllegalArgumentException command가 null이거나 유효하지 않은 경우
     * @throws com.ryuqq.fileflow.domain.policy.exception.PolicyNotFoundException 정책이 존재하지 않는 경우
     */
    UploadPolicyResponse updatePolicy(UpdateUploadPolicyCommand command);
}
