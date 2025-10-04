package com.ryuqq.fileflow.domain.policy.event;

import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain Event: 정책 활성화 발생 시 발행
 * <p>
 * 발행 시점:
 * - UploadPolicy가 INACTIVE → ACTIVE 상태로 전환될 때
 * - 새로운 정책이 시스템에 적용될 때
 * <p>
 * 사용 시나리오:
 * - 정책 활성화 알림 (사용자 및 관리자)
 * - 정책 적용 시작 로깅
 * - 외부 시스템과의 정책 동기화
 * - 활성화 이력 추적
 */
public record PolicyActivatedEvent(String policyKey, int version, String activatedBy, LocalDateTime activatedAt) implements DomainEvent {

    public PolicyActivatedEvent {
        Objects.requireNonNull(policyKey, "policyKey must not be null");
        Objects.requireNonNull(activatedBy, "activatedBy must not be null");
        Objects.requireNonNull(activatedAt, "activatedAt must not be null");

        if (version < 0) {
            throw new IllegalArgumentException("version must not be negative: " + version);
        }
    }

    @Override
    public LocalDateTime occurredOn() {
        return activatedAt;
    }

    @Override
    public String eventType() {
        return getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return "PolicyActivatedEvent{"
            +
            "policyKey='"
            + policyKey
            + '\''
            +
            ", version="
            + version
            +
            ", activatedBy='"
            + activatedBy
            + '\''
            +
            ", activatedAt="
            + activatedAt
            +
            '}';
    }
}
