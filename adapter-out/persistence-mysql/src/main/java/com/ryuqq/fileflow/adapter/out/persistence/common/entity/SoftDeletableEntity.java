package com.ryuqq.fileflow.adapter.out.persistence.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;

@MappedSuperclass
public abstract class SoftDeletableEntity extends BaseAuditEntity {

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected SoftDeletableEntity() {}

    protected SoftDeletableEntity(Instant createdAt, Instant updatedAt, Instant deletedAt) {
        super(createdAt, updatedAt);
        this.deletedAt = deletedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
