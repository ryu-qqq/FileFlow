package com.ryuqq.fileflow.adapter.redis.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.policy.UploadPolicy;
import com.ryuqq.fileflow.domain.policy.vo.FileTypePolicies;
import com.ryuqq.fileflow.domain.policy.vo.RateLimiting;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Redis 캐싱을 위한 UploadPolicy DTO
 *
 * 목적:
 * - UploadPolicy Domain 객체의 Redis 직렬화/역직렬화
 * - Jackson을 통한 JSON 변환 지원
 * - Lombok 미사용 (Acceptance Criteria)
 *
 * 직렬화 전략:
 * - PolicyKey: String으로 변환
 * - FileTypePolicies: JSON 객체로 직렬화
 * - RateLimiting: JSON 객체로 직렬화
 * - LocalDateTime: ISO-8601 형식
 *
 * @author sangwon-ryu
 */
public final class UploadPolicyDto {

    private final String policyKey;
    private final FileTypePolicies fileTypePolicies;
    private final RateLimiting rateLimiting;
    private final int version;
    private final boolean isActive;
    private final LocalDateTime effectiveFrom;
    private final LocalDateTime effectiveUntil;

    @JsonCreator
    public UploadPolicyDto(
            @JsonProperty("policyKey") String policyKey,
            @JsonProperty("fileTypePolicies") FileTypePolicies fileTypePolicies,
            @JsonProperty("rateLimiting") RateLimiting rateLimiting,
            @JsonProperty("version") int version,
            @JsonProperty("active") boolean isActive,
            @JsonProperty("effectiveFrom") LocalDateTime effectiveFrom,
            @JsonProperty("effectiveUntil") LocalDateTime effectiveUntil
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
     * Domain 객체로부터 DTO 생성
     *
     * @param uploadPolicy Domain 객체
     * @return UploadPolicyDto
     */
    public static UploadPolicyDto from(UploadPolicy uploadPolicy) {
        if (uploadPolicy == null) {
            throw new IllegalArgumentException("UploadPolicy cannot be null");
        }

        return new UploadPolicyDto(
                uploadPolicy.getPolicyKey().getValue(),
                uploadPolicy.getFileTypePolicies(),
                uploadPolicy.getRateLimiting(),
                uploadPolicy.getVersion(),
                uploadPolicy.isActive(),
                uploadPolicy.getEffectiveFrom(),
                uploadPolicy.getEffectiveUntil()
        );
    }

    /**
     * DTO를 Domain 객체로 변환
     *
     * @return UploadPolicy Domain 객체
     */
    public UploadPolicy toDomain() {
        // policyKey format: "tenantId:userType:serviceType"
        String[] parts = policyKey.split(":");
        if (parts.length != 3) {
            throw new IllegalStateException("Invalid policyKey format: " + policyKey);
        }

        return UploadPolicy.reconstitute(
                PolicyKey.of(parts[0], parts[1], parts[2]),
                fileTypePolicies,
                rateLimiting,
                version,
                isActive,
                effectiveFrom,
                effectiveUntil
        );
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

    // ========== Object Methods ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UploadPolicyDto that = (UploadPolicyDto) o;
        return version == that.version &&
                isActive == that.isActive &&
                Objects.equals(policyKey, that.policyKey) &&
                Objects.equals(fileTypePolicies, that.fileTypePolicies) &&
                Objects.equals(rateLimiting, that.rateLimiting) &&
                Objects.equals(effectiveFrom, that.effectiveFrom) &&
                Objects.equals(effectiveUntil, that.effectiveUntil);
    }

    @Override
    public int hashCode() {
        return Objects.hash(policyKey, fileTypePolicies, rateLimiting, version,
                isActive, effectiveFrom, effectiveUntil);
    }

    @Override
    public String toString() {
        return "UploadPolicyDto{" +
                "policyKey='" + policyKey + '\'' +
                ", version=" + version +
                ", isActive=" + isActive +
                ", effectiveFrom=" + effectiveFrom +
                ", effectiveUntil=" + effectiveUntil +
                '}';
    }
}
