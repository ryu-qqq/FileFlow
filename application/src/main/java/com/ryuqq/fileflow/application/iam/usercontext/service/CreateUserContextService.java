package com.ryuqq.fileflow.application.iam.usercontext.service;

import com.ryuqq.fileflow.application.iam.usercontext.assembler.UserContextAssembler;
import com.ryuqq.fileflow.application.iam.usercontext.dto.command.CreateUserContextCommand;
import com.ryuqq.fileflow.application.iam.usercontext.dto.response.UserContextResponse;
import com.ryuqq.fileflow.application.iam.usercontext.port.in.CreateUserContextUseCase;
import com.ryuqq.fileflow.application.iam.usercontext.port.out.UserContextRepositoryPort;
import com.ryuqq.fileflow.domain.iam.usercontext.Email;
import com.ryuqq.fileflow.domain.iam.usercontext.ExternalUserId;
import com.ryuqq.fileflow.domain.iam.usercontext.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserContext 생성 Service
 *
 * <p><strong>트랜잭션 경계</strong>: 이 클래스의 public 메서드</p>
 * <p><strong>비즈니스 규칙</strong>:</p>
 * <ul>
 *   <li>중복 externalUserId 검증</li>
 *   <li>Email 형식 검증 (Email VO에서 자동 처리)</li>
 *   <li>UserContext Aggregate 생성 (deleted=false, memberships=[])</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-27
 */
@Service
public class CreateUserContextService implements CreateUserContextUseCase {

    private final UserContextRepositoryPort userContextRepository;
    private final UserContextAssembler assembler;

    public CreateUserContextService(
        UserContextRepositoryPort userContextRepository,
        UserContextAssembler assembler
    ) {
        this.userContextRepository = userContextRepository;
        this.assembler = assembler;
    }

    /**
     * UserContext 생성
     *
     * <p><strong>트랜잭션</strong>: READ_COMMITTED, REQUIRED</p>
     *
     * @param command UserContext 생성 Command
     * @return 생성된 UserContext 응답
     * @throws IllegalArgumentException command가 null이거나 유효하지 않은 경우
     * @throws IllegalStateException 중복 externalUserId가 존재하는 경우
     */
    @Override
    @Transactional
    public UserContextResponse createUserContext(CreateUserContextCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("CreateUserContextCommand는 필수입니다");
        }

        // 1. 중복 검증
        ExternalUserId externalUserId = ExternalUserId.of(command.externalUserId());
        if (userContextRepository.existsByExternalUserId(externalUserId)) {
            throw new IllegalStateException(
                "이미 동일한 외부 사용자 ID를 가진 UserContext가 존재합니다: " + command.externalUserId()
            );
        }

        // 2. Domain Aggregate 생성 (Email VO에서 형식 검증)
        Email email = Email.of(command.email());
        UserContext userContext = UserContext.of(null, externalUserId, email);

        // 3. 저장
        UserContext savedUserContext = userContextRepository.save(userContext);

        // 4. Response 변환
        return assembler.toResponse(savedUserContext);
    }
}
