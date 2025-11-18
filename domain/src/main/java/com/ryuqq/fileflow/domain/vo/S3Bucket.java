package com.ryuqq.fileflow.domain.vo;

import com.ryuqq.fileflow.domain.iam.vo.TenantId;

/**
 * S3 Bucket Value Object
 * <p>
 * 테넌트별 S3 버킷 네이밍을 담당합니다.
 * </p>
 *
 * <p>
 * 네이밍 규칙:
 * - 패턴: fileflow-uploads-{tenantId}
 * - 예시: TenantId(1) → fileflow-uploads-1
 * - 향후 확장: 테넌트별 버킷 분리 가능
 * </p>
 */
public record S3Bucket(String value) {

    /**
     * Compact Constructor (Record 검증 패턴)
     * <p>
     * S3 버킷 이름은 null이거나 빈 값일 수 없습니다.
     * </p>
     */
    public S3Bucket {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("S3 버킷 이름은 null이거나 빈 값일 수 없습니다");
        }
    }

    /**
     * 테넌트별 S3 버킷 생성
     * <p>
     * 네이밍: fileflow-uploads-{tenantId}
     * </p>
     *
     * @param tenantId 테넌트 ID
     * @return S3Bucket VO
     */
    public static S3Bucket forTenant(TenantId tenantId) {
        return new S3Bucket("fileflow-uploads-" + tenantId.value());
    }

    /**
     * S3 버킷 이름 조회
     *
     * @return S3 버킷 이름 문자열
     */
    public String getValue() {
        return value;
    }
}
