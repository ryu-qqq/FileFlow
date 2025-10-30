package com.ryuqq.application.{domain_lower}.command;

import java.util.Objects;

/**
 * {Domain} 작업 실행 Command
 *
 * <p>Orchestration Pattern의 Command 캡슐화 패턴을 구현합니다.
 * IdemKey를 통해 멱등성을 보장하며, Compact Constructor로 불변성을 검증합니다.</p>
 *
 * @author {author_name}
 * @since {version}
 */
public record {Domain}Command(
    String businessKey,
    String idempotencyKey,
    // TODO: 비즈니스 필드 추가 (예: BigDecimal amount, String description)
) {
    /**
     * Compact Constructor - 불변성 검증
     *
     * <p>Record 패턴의 Compact Constructor를 사용하여
     * 모든 필수 필드의 null 체크 및 비즈니스 규칙을 검증합니다.</p>
     */
    public {Domain}Command {
        Objects.requireNonNull(businessKey, "businessKey must not be null");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey must not be null");

        // TODO: 비즈니스 규칙 검증 추가
        // 예시:
        // if (amount.compareTo(BigDecimal.ZERO) <= 0) {
        //     throw new IllegalArgumentException("amount must be positive");
        // }
    }
}
