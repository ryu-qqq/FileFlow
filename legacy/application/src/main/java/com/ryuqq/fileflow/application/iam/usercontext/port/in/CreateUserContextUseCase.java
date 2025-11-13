package com.ryuqq.fileflow.application.iam.usercontext.port.in;

import com.ryuqq.fileflow.application.iam.usercontext.dto.command.CreateUserContextCommand;
import com.ryuqq.fileflow.application.iam.usercontext.dto.response.UserContextResponse;

/**
 * UserContext 생성 UseCase (Inbound Port)
 *
 * <p>외부 IDP로부터 인증된 사용자의 UserContext를 생성합니다.</p>
 *
 * <p><strong>Hexagonal Architecture</strong>: Adapter → UseCase → Service → Repository</p>
 *
 * <p><strong>구현 위치</strong>: {@code application/iam/usercontext/service/CreateUserContextService.java}</p>
 *
 * @author ryu-qqq
 * @since 2025-10-27
 */
public interface CreateUserContextUseCase {

    /**
     * UserContext를 생성합니다.
     *
     * <p><strong>비즈니스 규칙</strong>:</p>
     * <ul>
     *   <li>중복 externalUserId 검증 - 이미 존재하면 예외 발생</li>
     *   <li>Email 형식 검증 - {@link com.ryuqq.fileflow.domain.iam.usercontext.Email} VO에서 검증</li>
     *   <li>UserContext 생성 시 deleted=false, memberships=[] 상태로 생성</li>
     * </ul>
     *
     * @param command UserContext 생성 Command
     * @return 생성된 UserContext 응답 DTO
     * @throws IllegalArgumentException command가 null이거나 유효하지 않은 경우
     * @throws IllegalStateException 이미 동일한 externalUserId를 가진 UserContext가 존재하는 경우
     */
    UserContextResponse createUserContext(CreateUserContextCommand command);
}
