package com.ryuqq.fileflow.adapter.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 처리 정책 엔티티 (향후 확장용)
 *
 * 비즈니스 규칙:
 * - 파일 처리 관련 정책을 저장합니다
 * - policyKey는 UploadPolicy와 동일한 키 구조를 사용합니다
 * - 현재는 기본 구조만 정의하고, 향후 확장 가능하도록 설계
 * - Lombok을 사용하지 않으므로 모든 메서드를 수동으로 구현
 *
 * @author sangwon-ryu
 */
@Entity
@Table(name = "processing_policy")
public class ProcessingPolicyEntity {

    @Id
    @Column(name = "policy_key", nullable = false, length = 200)
    private String policyKey;

    @Column(name = "processing_config", columnDefinition = "TEXT")
    private String processingConfig;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * JPA 기본 생성자 (protected)
     */
    protected ProcessingPolicyEntity() {
    }

    /**
     * 처리 정책 엔티티 생성자 (protected - factory method 사용 권장)
     *
     * @param policyKey 정책 키
     * @param processingConfig 처리 설정 (JSON 문자열)
     * @param isActive 활성 상태
     */
    protected ProcessingPolicyEntity(String policyKey, String processingConfig, boolean isActive) {
        this.policyKey = policyKey;
        this.processingConfig = processingConfig;
        this.isActive = isActive;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 새로운 처리 정책 엔티티를 생성하는 factory method
     *
     * @param policyKey 정책 키
     * @param processingConfig 처리 설정 (JSON 문자열)
     * @param isActive 활성 상태
     * @return 생성된 ProcessingPolicyEntity
     */
    public static ProcessingPolicyEntity of(String policyKey, String processingConfig, boolean isActive) {
        return new ProcessingPolicyEntity(policyKey, processingConfig, isActive);
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ========== Getters ==========

    public String getPolicyKey() {
        return policyKey;
    }

    public String getProcessingConfig() {
        return processingConfig;
    }

    public boolean isActive() {
        return isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // ========== Object Methods ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessingPolicyEntity that = (ProcessingPolicyEntity) o;
        return Objects.equals(policyKey, that.policyKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(policyKey);
    }

    @Override
    public String toString() {
        return "ProcessingPolicyEntity{" +
                "policyKey='" + policyKey + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
