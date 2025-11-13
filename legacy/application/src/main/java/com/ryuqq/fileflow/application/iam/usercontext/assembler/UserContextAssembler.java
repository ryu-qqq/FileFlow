package com.ryuqq.fileflow.application.iam.usercontext.assembler;

import com.ryuqq.fileflow.application.iam.usercontext.dto.response.UserContextResponse;
import com.ryuqq.fileflow.domain.iam.usercontext.UserContext;
import org.springframework.stereotype.Component;

/**
 * UserContext Assembler
 *
 * <p>Domain Model과 Application DTO 간 변환을 담당합니다.</p>
 *
 * <p><strong>패턴</strong>: Assembler Pattern (DDD)</p>
 * <p><strong>규칙 준수</strong>:</p>
 * <ul>
 *   <li>❌ Lombok 사용 안함</li>
 *   <li>✅ Pure Java 구현</li>
 *   <li>✅ Law of Demeter - Getter 체이닝 방지</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-27
 */
@Component
public class UserContextAssembler {

    /**
     * UserContext Domain을 UserContextResponse로 변환
     *
     * @param userContext UserContext Aggregate
     * @return UserContextResponse
     * @throws IllegalArgumentException userContext가 null인 경우
     */
    public UserContextResponse toResponse(UserContext userContext) {
        if (userContext == null) {
            throw new IllegalArgumentException("UserContext는 필수입니다");
        }

        return new UserContextResponse(
            userContext.getIdValue(),
            userContext.getExternalUserIdValue(),
            userContext.getEmailValue(),
            userContext.isDeleted(),
            userContext.getCreatedAt(),
            userContext.getUpdatedAt()
        );
    }
}
