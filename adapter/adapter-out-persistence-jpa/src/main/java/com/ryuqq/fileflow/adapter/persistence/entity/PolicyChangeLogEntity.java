package com.ryuqq.fileflow.adapter.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 정책 변경 로그 엔티티
 *
 * 비즈니스 규칙:
 * - 정책의 모든 변경 이력을 추적합니다
 * - 변경 전/후 내용을 JSON으로 저장합니다
 * - 감사(Audit) 목적으로 사용됩니다
 * - Lombok을 사용하지 않으므로 모든 메서드를 수동으로 구현
 *
 * @author sangwon-ryu
 */
@Entity
@Table(name = "policy_change_log", indexes = {
        @Index(name = "idx_policy_change_log_policy_key", columnList = "policy_key"),
        @Index(name = "idx_policy_change_log_changed_at", columnList = "changed_at")
})
public class PolicyChangeLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "policy_key", nullable = false, length = 200)
    private String policyKey;

    @Column(name = "change_type", nullable = false, length = 50)
    private String changeType;

    @Column(name = "old_version")
    private Integer oldVersion;

    @Column(name = "new_version")
    private Integer newVersion;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "changed_by", length = 100)
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    /**
     * JPA 기본 생성자 (protected)
     */
    protected PolicyChangeLogEntity() {
    }

    /**
     * 정책 변경 로그 엔티티 생성자 (protected - factory method 사용 권장)
     *
     * @param policyKey 정책 키
     * @param changeType 변경 유형 (CREATE, UPDATE, DELETE, ACTIVATE, DEACTIVATE)
     * @param oldVersion 이전 버전
     * @param newVersion 새 버전
     * @param oldValue 이전 값 (JSON)
     * @param newValue 새 값 (JSON)
     * @param changedBy 변경자
     */
    protected PolicyChangeLogEntity(
            String policyKey,
            String changeType,
            Integer oldVersion,
            Integer newVersion,
            String oldValue,
            String newValue,
            String changedBy
    ) {
        this.policyKey = policyKey;
        this.changeType = changeType;
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedBy = changedBy;
    }

    /**
     * 새로운 정책 변경 로그 엔티티를 생성하는 factory method
     *
     * @param policyKey 정책 키
     * @param changeType 변경 유형
     * @param oldVersion 이전 버전
     * @param newVersion 새 버전
     * @param oldValue 이전 값 (JSON)
     * @param newValue 새 값 (JSON)
     * @param changedBy 변경자
     * @return 생성된 PolicyChangeLogEntity
     */
    public static PolicyChangeLogEntity of(
            String policyKey,
            String changeType,
            Integer oldVersion,
            Integer newVersion,
            String oldValue,
            String newValue,
            String changedBy
    ) {
        return new PolicyChangeLogEntity(
                policyKey,
                changeType,
                oldVersion,
                newVersion,
                oldValue,
                newValue,
                changedBy
        );
    }

    @PrePersist
    protected void onCreate() {
        if (this.changedAt == null) {
            this.changedAt = LocalDateTime.now();
        }
    }

    // ========== Getters ==========

    public Long getId() {
        return id;
    }

    public String getPolicyKey() {
        return policyKey;
    }

    public String getChangeType() {
        return changeType;
    }

    public Integer getOldVersion() {
        return oldVersion;
    }

    public Integer getNewVersion() {
        return newVersion;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    // ========== Object Methods ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PolicyChangeLogEntity that = (PolicyChangeLogEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PolicyChangeLogEntity{" +
                "id=" + id +
                ", policyKey='" + policyKey + '\'' +
                ", changeType='" + changeType + '\'' +
                ", oldVersion=" + oldVersion +
                ", newVersion=" + newVersion +
                ", changedBy='" + changedBy + '\'' +
                ", changedAt=" + changedAt +
                '}';
    }
}
