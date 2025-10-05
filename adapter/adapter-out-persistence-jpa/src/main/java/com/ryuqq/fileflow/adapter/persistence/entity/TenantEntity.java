package com.ryuqq.fileflow.adapter.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 테넌트 엔티티
 *
 * 비즈니스 규칙:
 * - 테넌트는 고유한 tenantId를 가집니다
 * - 생성 시각은 자동으로 기록됩니다
 * - Lombok을 사용하지 않으므로 모든 메서드를 수동으로 구현합니다
 *
 * @author sangwon-ryu
 */
@Entity
@Table(name = "tenant")
public class TenantEntity {

    @Id
    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * JPA 기본 생성자 (protected)
     */
    protected TenantEntity() {
    }

    /**
     * 테넌트 엔티티 생성자 (protected - factory method 사용 권장)
     *
     * @param tenantId 테넌트 ID
     * @param name 테넌트 이름
     */
    protected TenantEntity(String tenantId, String name) {
        this.tenantId = tenantId;
        this.name = name;
    }

    /**
     * 새로운 테넌트 엔티티를 생성하는 factory method
     *
     * @param tenantId 테넌트 ID
     * @param name 테넌트 이름
     * @return 생성된 TenantEntity
     */
    public static TenantEntity of(String tenantId, String name) {
        return new TenantEntity(tenantId, name);
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

    public String getTenantId() {
        return tenantId;
    }

    public String getName() {
        return name;
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
        TenantEntity that = (TenantEntity) o;
        return Objects.equals(tenantId, that.tenantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId);
    }

    @Override
    public String toString() {
        return "TenantEntity{" +
                "tenantId='" + tenantId + '\'' +
                ", name='" + name + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
