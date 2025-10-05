package com.ryuqq.fileflow.adapter.persistence.entity;

import com.ryuqq.fileflow.adapter.persistence.converter.FileTypePoliciesConverter;
import com.ryuqq.fileflow.adapter.persistence.converter.RateLimitingConverter;
import com.ryuqq.fileflow.domain.policy.vo.FileTypePolicies;
import com.ryuqq.fileflow.domain.policy.vo.RateLimiting;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 업로드 정책 엔티티
 *
 * 비즈니스 규칙:
 * - policyKey는 "tenantId:userType:serviceType" 형식의 복합 키
 * - FileTypePolicies와 RateLimiting은 JSONB 컬럼에 저장
 * - version은 정책 업데이트 시 자동 증가
 * - Lombok을 사용하지 않으므로 모든 메서드를 수동으로 구현
 *
 * @author sangwon-ryu
 */
@Entity
@Table(name = "upload_policy", indexes = {
        @Index(name = "idx_upload_policy_is_active", columnList = "is_active"),
        @Index(name = "idx_upload_policy_effective_period", columnList = "effective_from,effective_until")
})
public class UploadPolicyEntity {

    @Id
    @Column(name = "policy_key", nullable = false, length = 200)
    private String policyKey;

    @Convert(converter = FileTypePoliciesConverter.class)
    @Column(name = "file_type_policies", columnDefinition = "TEXT", nullable = false)
    private FileTypePolicies fileTypePolicies;

    @Convert(converter = RateLimitingConverter.class)
    @Column(name = "rate_limiting", columnDefinition = "TEXT", nullable = false)
    private RateLimiting rateLimiting;

    @Version
    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_until", nullable = false)
    private LocalDateTime effectiveUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * JPA 기본 생성자 (protected)
     */
    protected UploadPolicyEntity() {
    }

    /**
     * 업로드 정책 엔티티 생성자 (protected - factory method 사용 권장)
     *
     * @param policyKey 정책 키 (tenantId:userType:serviceType)
     * @param fileTypePolicies 파일 타입별 정책
     * @param rateLimiting Rate Limiting 정책
     * @param version 버전
     * @param isActive 활성 상태
     * @param effectiveFrom 유효 시작 일시
     * @param effectiveUntil 유효 종료 일시
     */
    protected UploadPolicyEntity(
            String policyKey,
            FileTypePolicies fileTypePolicies,
            RateLimiting rateLimiting,
            int version,
            boolean isActive,
            LocalDateTime effectiveFrom,
            LocalDateTime effectiveUntil
    ) {
        this.policyKey = policyKey;
        this.fileTypePolicies = fileTypePolicies;
        this.rateLimiting = rateLimiting;
        this.version = version;
        this.isActive = isActive;
        this.effectiveFrom = effectiveFrom;
        this.effectiveUntil = effectiveUntil;
    }

    /**
     * 새로운 업로드 정책 엔티티를 생성하는 factory method
     *
     * @param policyKey 정책 키
     * @param fileTypePolicies 파일 타입별 정책
     * @param rateLimiting Rate Limiting 정책
     * @param version 버전
     * @param isActive 활성 상태
     * @param effectiveFrom 유효 시작 일시
     * @param effectiveUntil 유효 종료 일시
     * @return 생성된 UploadPolicyEntity
     */
    public static UploadPolicyEntity of(
            String policyKey,
            FileTypePolicies fileTypePolicies,
            RateLimiting rateLimiting,
            int version,
            boolean isActive,
            LocalDateTime effectiveFrom,
            LocalDateTime effectiveUntil
    ) {
        return new UploadPolicyEntity(
                policyKey,
                fileTypePolicies,
                rateLimiting,
                version,
                isActive,
                effectiveFrom,
                effectiveUntil
        );
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

    // ========== Business Methods ==========

    /**
     * 정책 정보를 업데이트 (JPA가 자동으로 version 증가)
     *
     * @param fileTypePolicies 파일 타입별 정책
     * @param rateLimiting Rate Limiting 정책
     * @param isActive 활성 상태
     * @param effectiveFrom 유효 시작 일시
     * @param effectiveUntil 유효 종료 일시
     */
    public void update(
            FileTypePolicies fileTypePolicies,
            RateLimiting rateLimiting,
            boolean isActive,
            LocalDateTime effectiveFrom,
            LocalDateTime effectiveUntil
    ) {
        this.fileTypePolicies = fileTypePolicies;
        this.rateLimiting = rateLimiting;
        this.isActive = isActive;
        this.effectiveFrom = effectiveFrom;
        this.effectiveUntil = effectiveUntil;
    }

    // ========== Getters ==========

    public String getPolicyKey() {
        return policyKey;
    }

    public FileTypePolicies getFileTypePolicies() {
        return fileTypePolicies;
    }

    public RateLimiting getRateLimiting() {
        return rateLimiting;
    }

    public int getVersion() {
        return version;
    }

    public boolean isActive() {
        return isActive;
    }

    public LocalDateTime getEffectiveFrom() {
        return effectiveFrom;
    }

    public LocalDateTime getEffectiveUntil() {
        return effectiveUntil;
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
        UploadPolicyEntity that = (UploadPolicyEntity) o;
        return Objects.equals(policyKey, that.policyKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(policyKey);
    }

    @Override
    public String toString() {
        return "UploadPolicyEntity{" +
                "policyKey='" + policyKey + '\'' +
                ", version=" + version +
                ", isActive=" + isActive +
                ", effectiveFrom=" + effectiveFrom +
                ", effectiveUntil=" + effectiveUntil +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
