package com.ryuqq.fileflow.domain.policy.event;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain Event: 정책 업데이트 발생 시 발행
 * <p>
 * 발행 시점:
 * - UploadPolicy의 정책 설정 변경 시 (파일 크기, 개수, 형식 등)
 * - 정책 버전이 증가할 때
 * <p>
 * 사용 시나리오:
 * - 정책 변경 이력 추적 (Audit Log)
 * - 정책 변경 알림 (Notification)
 * - 캐시 무효화 (Cache Invalidation)
 */
public record PolicyUpdatedEvent(String policyKey, int oldVersion, int newVersion, String changedBy,
                                 LocalDateTime changedAt) {

    public PolicyUpdatedEvent(
        String policyKey,
        int oldVersion,
        int newVersion,
        String changedBy,
        LocalDateTime changedAt
    ) {
        this.policyKey = Objects.requireNonNull(policyKey, "policyKey must not be null");
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
        this.changedBy = Objects.requireNonNull(changedBy, "changedBy must not be null");
        this.changedAt = Objects.requireNonNull(changedAt, "changedAt must not be null");

        validateVersions();
    }

    private void validateVersions() {
        if (oldVersion
            < 0) {
            throw new IllegalArgumentException("oldVersion must not be negative: "
                + oldVersion);
        }
        if (newVersion
            <= oldVersion) {
            throw new IllegalArgumentException(
                String.format("newVersion (%d) must be greater than oldVersion (%d)", newVersion, oldVersion)
            );
        }
    }

    @Override
    public String toString() {
        return "PolicyUpdatedEvent{"
            +
            "policyKey='"
            + policyKey
            + '\''
            +
            ", oldVersion="
            + oldVersion
            +
            ", newVersion="
            + newVersion
            +
            ", changedBy='"
            + changedBy
            + '\''
            +
            ", changedAt="
            + changedAt
            +
            '}';
    }
}
