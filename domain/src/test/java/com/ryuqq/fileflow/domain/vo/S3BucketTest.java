package com.ryuqq.fileflow.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * S3Bucket Value Object 테스트
 * <p>
 * 테넌트별 S3 버킷 네이밍 검증
 * </p>
 */
@DisplayName("S3Bucket Value Object 테스트")
class S3BucketTest {

    @Test
    @DisplayName("TenantId로 S3 버킷 이름을 생성해야 한다")
    void shouldCreateS3BucketForTenant() {
        // given
        TenantId tenantId = TenantId.of(1L);

        // when
        S3Bucket s3Bucket = S3Bucket.forTenant(tenantId);

        // then
        assertThat(s3Bucket).isNotNull();
        assertThat(s3Bucket.getValue()).isEqualTo("fileflow-uploads-1");
    }

    @Test
    @DisplayName("다양한 TenantId로 버킷 이름을 생성해야 한다")
    void shouldCreateS3BucketForDifferentTenants() {
        // given
        TenantId tenantId2 = TenantId.of(2L);
        TenantId tenantId100 = TenantId.of(100L);

        // when
        S3Bucket s3Bucket2 = S3Bucket.forTenant(tenantId2);
        S3Bucket s3Bucket100 = S3Bucket.forTenant(tenantId100);

        // then
        assertThat(s3Bucket2.getValue()).isEqualTo("fileflow-uploads-2");
        assertThat(s3Bucket100.getValue()).isEqualTo("fileflow-uploads-100");
    }

    @Test
    @DisplayName("getValue()는 버킷 이름을 반환해야 한다")
    void shouldReturnBucketName() {
        // given
        TenantId tenantId = TenantId.of(1L);
        S3Bucket s3Bucket = S3Bucket.forTenant(tenantId);

        // when
        String value = s3Bucket.getValue();

        // then
        assertThat(value).isNotBlank();
        assertThat(value).startsWith("fileflow-uploads-");
        assertThat(value).endsWith("1");
    }

    @Test
    @DisplayName("같은 TenantId로 생성된 S3Bucket은 동등해야 한다")
    void shouldBeEqualWhenCreatedWithSameTenantId() {
        // given
        TenantId tenantId = TenantId.of(1L);

        // when
        S3Bucket s3Bucket1 = S3Bucket.forTenant(tenantId);
        S3Bucket s3Bucket2 = S3Bucket.forTenant(tenantId);

        // then
        assertThat(s3Bucket1).isEqualTo(s3Bucket2);
    }

    @Test
    @DisplayName("같은 TenantId로 생성된 S3Bucket은 같은 해시코드를 가져야 한다")
    void shouldHaveSameHashCodeWhenCreatedWithSameTenantId() {
        // given
        TenantId tenantId = TenantId.of(5L);

        // when
        S3Bucket s3Bucket1 = S3Bucket.forTenant(tenantId);
        S3Bucket s3Bucket2 = S3Bucket.forTenant(tenantId);

        // then
        assertThat(s3Bucket1.hashCode()).isEqualTo(s3Bucket2.hashCode());
    }

    @Test
    @DisplayName("다른 TenantId로 생성된 S3Bucket은 동등하지 않아야 한다")
    void shouldNotBeEqualWhenCreatedWithDifferentTenantId() {
        // given
        TenantId tenantId1 = TenantId.of(1L);
        TenantId tenantId2 = TenantId.of(2L);

        // when
        S3Bucket s3Bucket1 = S3Bucket.forTenant(tenantId1);
        S3Bucket s3Bucket2 = S3Bucket.forTenant(tenantId2);

        // then
        assertThat(s3Bucket1).isNotEqualTo(s3Bucket2);
    }

    @Test
    @DisplayName("버킷 이름은 'fileflow-uploads-' 접두사로 시작해야 한다")
    void shouldStartWithPrefix() {
        // given
        TenantId tenantId = TenantId.of(999L);

        // when
        S3Bucket s3Bucket = S3Bucket.forTenant(tenantId);

        // then
        assertThat(s3Bucket.getValue()).startsWith("fileflow-uploads-");
    }
}
