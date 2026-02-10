package com.ryuqq.fileflow.adapter.out.persistence.session.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "completed_part")
public class CompletedPartJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "session_id", length = 36, nullable = false)
    private String sessionId;

    @Column(name = "part_number", nullable = false)
    private int partNumber;

    @Column(name = "etag", length = 255, nullable = false)
    private String etag;

    @Column(name = "size", nullable = false)
    private long size;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected CompletedPartJpaEntity() {}

    private CompletedPartJpaEntity(
            String sessionId, int partNumber, String etag, long size, Instant createdAt) {
        this.sessionId = sessionId;
        this.partNumber = partNumber;
        this.etag = etag;
        this.size = size;
        this.createdAt = createdAt;
    }

    public static CompletedPartJpaEntity create(
            String sessionId, int partNumber, String etag, long size, Instant createdAt) {
        return new CompletedPartJpaEntity(sessionId, partNumber, etag, size, createdAt);
    }

    public Long getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public String getEtag() {
        return etag;
    }

    public long getSize() {
        return size;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
