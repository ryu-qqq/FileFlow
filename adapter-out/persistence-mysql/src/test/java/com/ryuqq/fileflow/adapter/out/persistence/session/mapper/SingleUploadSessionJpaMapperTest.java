package com.ryuqq.fileflow.adapter.out.persistence.session.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.out.persistence.session.entity.SingleUploadSessionJpaEntity;
import com.ryuqq.fileflow.domain.iam.vo.Organization;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.Tenant;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import com.ryuqq.fileflow.domain.iam.vo.UserRole;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.ExpirationTime;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.session.vo.PresignedUrl;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SingleUploadSessionJpaMapper 단위 테스트")
class SingleUploadSessionJpaMapperTest {

    private SingleUploadSessionJpaMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SingleUploadSessionJpaMapper();
    }

    @Nested
    @DisplayName("toEntity 테스트")
    class ToEntityTest {

        @Test
        @DisplayName("Domain을 Entity로 변환할 수 있다")
        void toEntity_WithValidDomain_ShouldConvertToEntity() {
            // given
            SingleUploadSession domain = createDomain(SessionStatus.ACTIVE);

            // when
            SingleUploadSessionJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getId()).isEqualTo(domain.getId().getValue());
            assertThat(entity.getIdempotencyKey()).isEqualTo(domain.getIdempotencyKey().getValue());
            assertThat(entity.getUserId()).isNull();
            assertThat(entity.getOrganizationId()).isEqualTo("01912345-6789-7abc-def0-123456789100");
            assertThat(entity.getOrganizationName()).isEqualTo("Test Org");
            assertThat(entity.getOrganizationNamespace()).isEqualTo("setof");
            assertThat(entity.getTenantId()).isEqualTo("01912345-6789-7abc-def0-123456789001");
            assertThat(entity.getTenantName()).isEqualTo("Connectly");
            assertThat(entity.getUserRole()).isEqualTo("SELLER");
            assertThat(entity.getEmail()).isEqualTo("seller@test.com");
            assertThat(entity.getFileName()).isEqualTo("document.pdf");
            assertThat(entity.getFileSize()).isEqualTo(1024 * 1024L);
            assertThat(entity.getContentType()).isEqualTo("application/pdf");
            assertThat(entity.getBucket()).isEqualTo("test-bucket");
            assertThat(entity.getS3Key()).isEqualTo("uploads/document.pdf");
            assertThat(entity.getStatus()).isEqualTo(SessionStatus.ACTIVE);
        }

        @Test
        @DisplayName("Presigned URL이 null인 경우도 변환할 수 있다")
        void toEntity_WithNullPresignedUrl_ShouldConvertWithNull() {
            // given
            SingleUploadSession domain = createDomainWithoutPresignedUrl();

            // when
            SingleUploadSessionJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getPresignedUrl()).isNull();
        }

        @Test
        @DisplayName("완료된 세션은 ETag와 completedAt을 포함한다")
        void toEntity_WithCompletedSession_ShouldIncludeEtagAndCompletedAt() {
            // given
            SingleUploadSession domain = createCompletedDomain();

            // when
            SingleUploadSessionJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getStatus()).isEqualTo(SessionStatus.COMPLETED);
            assertThat(entity.getEtag()).isEqualTo("\"abc123\"");
            assertThat(entity.getCompletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("toDomain 테스트")
    class ToDomainTest {

        @Test
        @DisplayName("Entity를 Domain으로 변환할 수 있다")
        void toDomain_WithValidEntity_ShouldConvertToDomain() {
            // given
            SingleUploadSessionJpaEntity entity = createEntity(SessionStatus.ACTIVE);

            // when
            SingleUploadSession domain = mapper.toDomain(entity);

            // then
            assertThat(domain.getId().getValue()).isEqualTo(entity.getId());
            assertThat(domain.getIdempotencyKey().getValue()).isEqualTo(entity.getIdempotencyKey());
            assertThat(domain.getFileNameValue()).isEqualTo(entity.getFileName());
            assertThat(domain.getFileSizeValue()).isEqualTo(entity.getFileSize());
            assertThat(domain.getContentTypeValue()).isEqualTo(entity.getContentType());
            assertThat(domain.getBucketValue()).isEqualTo(entity.getBucket());
            assertThat(domain.getS3KeyValue()).isEqualTo(entity.getS3Key());
            assertThat(domain.getStatus()).isEqualTo(entity.getStatus());
        }

        @Test
        @DisplayName("UserContext가 올바르게 복원된다")
        void toDomain_ShouldReconstructUserContext() {
            // given
            SingleUploadSessionJpaEntity entity = createEntity(SessionStatus.ACTIVE);

            // when
            SingleUploadSession domain = mapper.toDomain(entity);

            // then
            UserContext userContext = domain.getUserContext();
            assertThat(userContext.userId()).isEqualTo(entity.getUserId());
            assertThat(userContext.organization().id()).isEqualTo(entity.getOrganizationId());
            assertThat(userContext.organization().name()).isEqualTo(entity.getOrganizationName());
            assertThat(userContext.tenant().id()).isEqualTo(entity.getTenantId());
            assertThat(userContext.tenant().name()).isEqualTo(entity.getTenantName());
            assertThat(userContext.getRole().name()).isEqualTo(entity.getUserRole());
            assertThat(userContext.email()).isEqualTo(entity.getEmail());
        }

        @Test
        @DisplayName("Presigned URL이 null인 Entity도 변환할 수 있다")
        void toDomain_WithNullPresignedUrl_ShouldConvertWithNull() {
            // given
            SingleUploadSessionJpaEntity entity = createEntityWithNullPresignedUrl();

            // when
            SingleUploadSession domain = mapper.toDomain(entity);

            // then
            assertThat(domain.getStatus()).isEqualTo(SessionStatus.PREPARING);
        }

        @Test
        @DisplayName("ETag가 null인 Entity도 변환할 수 있다")
        void toDomain_WithNullEtag_ShouldConvertWithNull() {
            // given
            SingleUploadSessionJpaEntity entity = createEntity(SessionStatus.ACTIVE);

            // when
            SingleUploadSession domain = mapper.toDomain(entity);

            // then
            assertThat(domain.getEtag()).isNull();
        }

        @Test
        @DisplayName("완료된 Entity는 ETag와 completedAt이 복원된다")
        void toDomain_WithCompletedEntity_ShouldReconstructEtagAndCompletedAt() {
            // given
            SingleUploadSessionJpaEntity entity = createCompletedEntity();

            // when
            SingleUploadSession domain = mapper.toDomain(entity);

            // then
            assertThat(domain.getStatus()).isEqualTo(SessionStatus.COMPLETED);
            assertThat(domain.getEtag()).isNotNull();
            assertThat(domain.getEtag().value()).isEqualTo("\"abc123\"");
            assertThat(domain.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("버전 정보가 복원된다")
        void toDomain_ShouldReconstructVersion() {
            // given
            SingleUploadSessionJpaEntity entity = createEntityWithVersion(5L);

            // when
            SingleUploadSession domain = mapper.toDomain(entity);

            // then
            assertThat(domain.getVersion()).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("양방향 변환 테스트")
    class RoundTripTest {

        @Test
        @DisplayName("Domain → Entity → Domain 변환 시 데이터가 보존된다")
        void roundTrip_ShouldPreserveData() {
            // given
            SingleUploadSession original = createDomain(SessionStatus.ACTIVE);

            // when
            SingleUploadSessionJpaEntity entity = mapper.toEntity(original);
            SingleUploadSession restored = mapper.toDomain(entity);

            // then
            assertThat(restored.getId().getValue()).isEqualTo(original.getId().getValue());
            assertThat(restored.getIdempotencyKey().getValue())
                    .isEqualTo(original.getIdempotencyKey().getValue());
            assertThat(restored.getFileNameValue()).isEqualTo(original.getFileNameValue());
            assertThat(restored.getFileSizeValue()).isEqualTo(original.getFileSizeValue());
            assertThat(restored.getContentTypeValue()).isEqualTo(original.getContentTypeValue());
            assertThat(restored.getBucketValue()).isEqualTo(original.getBucketValue());
            assertThat(restored.getS3KeyValue()).isEqualTo(original.getS3KeyValue());
            assertThat(restored.getStatus()).isEqualTo(original.getStatus());
        }
    }

    // ==================== Helper Methods ====================

    private SingleUploadSession createDomain(SessionStatus status) {
        Tenant tenant = Tenant.of(TenantId.of("01912345-6789-7abc-def0-123456789001"), "Connectly");
        Organization organization = Organization.of(OrganizationId.of("01912345-6789-7abc-def0-123456789100"), "Test Org", "setof", UserRole.SELLER);
        UserContext userContext = UserContext.of(tenant, organization, "seller@test.com", null);

        Instant now = Instant.now();
        return SingleUploadSession.of(
                UploadSessionId.of(UUID.randomUUID()),
                IdempotencyKey.of(UUID.randomUUID()),
                userContext,
                FileName.of("document.pdf"),
                FileSize.of(1024 * 1024L),
                ContentType.of("application/pdf"),
                S3Bucket.of("test-bucket"),
                S3Key.of("uploads/document.pdf"),
                ExpirationTime.of(Instant.now().plus(java.time.Duration.ofMinutes(15))),
                now,
                status,
                PresignedUrl.of("https://presigned-url.s3.amazonaws.com/..."),
                null,
                null,
                now,
                0L);
    }

    private SingleUploadSession createDomainWithoutPresignedUrl() {
        Tenant tenant = Tenant.of(TenantId.of("01912345-6789-7abc-def0-123456789001"), "Connectly");
        Organization organization = Organization.of(OrganizationId.of("01912345-6789-7abc-def0-123456789100"), "Test Org", "setof", UserRole.SELLER);
        UserContext userContext = UserContext.of(tenant, organization, "seller@test.com", null);

        Instant now = Instant.now();
        return SingleUploadSession.of(
                UploadSessionId.of(UUID.randomUUID()),
                IdempotencyKey.of(UUID.randomUUID()),
                userContext,
                FileName.of("document.pdf"),
                FileSize.of(1024 * 1024L),
                ContentType.of("application/pdf"),
                S3Bucket.of("test-bucket"),
                S3Key.of("uploads/document.pdf"),
                ExpirationTime.of(Instant.now().plus(java.time.Duration.ofMinutes(15))),
                now,
                SessionStatus.PREPARING,
                null,
                null,
                null,
                now,
                null);
    }

    private SingleUploadSession createCompletedDomain() {
        Tenant tenant = Tenant.of(TenantId.of("01912345-6789-7abc-def0-123456789001"), "Connectly");
        Organization organization = Organization.of(OrganizationId.of("01912345-6789-7abc-def0-123456789100"), "Test Org", "setof", UserRole.SELLER);
        UserContext userContext = UserContext.of(tenant, organization, "seller@test.com", null);

        Instant now = Instant.now();
        return SingleUploadSession.of(
                UploadSessionId.of(UUID.randomUUID()),
                IdempotencyKey.of(UUID.randomUUID()),
                userContext,
                FileName.of("document.pdf"),
                FileSize.of(1024 * 1024L),
                ContentType.of("application/pdf"),
                S3Bucket.of("test-bucket"),
                S3Key.of("uploads/document.pdf"),
                ExpirationTime.of(Instant.now().plus(java.time.Duration.ofMinutes(15))),
                now,
                SessionStatus.COMPLETED,
                PresignedUrl.of("https://presigned-url.s3.amazonaws.com/..."),
                ETag.of("\"abc123\""),
                now,
                now,
                1L);
    }

    private SingleUploadSessionJpaEntity createEntity(SessionStatus status) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(java.time.Duration.ofMinutes(15));

        return SingleUploadSessionJpaEntity.of(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                null,
                "01912345-6789-7abc-def0-123456789100",
                "Test Org",
                "setof",
                "01912345-6789-7abc-def0-123456789001",
                "Connectly",
                "SELLER",
                "seller@test.com",
                "document.pdf",
                1024 * 1024L,
                "application/pdf",
                "test-bucket",
                "uploads/document.pdf",
                expiresAt,
                status,
                "https://presigned-url.s3.amazonaws.com/...",
                null,
                null,
                0L,
                now,
                now);
    }

    private SingleUploadSessionJpaEntity createEntityWithNullPresignedUrl() {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(java.time.Duration.ofMinutes(15));

        return SingleUploadSessionJpaEntity.of(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                null,
                "01912345-6789-7abc-def0-123456789100",
                "Test Org",
                "setof",
                "01912345-6789-7abc-def0-123456789001",
                "Connectly",
                "SELLER",
                "seller@test.com",
                "document.pdf",
                1024 * 1024L,
                "application/pdf",
                "test-bucket",
                "uploads/document.pdf",
                expiresAt,
                SessionStatus.PREPARING,
                null,
                null,
                null,
                0L,
                now,
                now);
    }

    private SingleUploadSessionJpaEntity createCompletedEntity() {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(java.time.Duration.ofMinutes(15));
        Instant completedAt = now.plus(java.time.Duration.ofMinutes(10));

        return SingleUploadSessionJpaEntity.of(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                null,
                "01912345-6789-7abc-def0-123456789100",
                "Test Org",
                "setof",
                "01912345-6789-7abc-def0-123456789001",
                "Connectly",
                "SELLER",
                "seller@test.com",
                "document.pdf",
                1024 * 1024L,
                "application/pdf",
                "test-bucket",
                "uploads/document.pdf",
                expiresAt,
                SessionStatus.COMPLETED,
                "https://presigned-url.s3.amazonaws.com/...",
                "\"abc123\"",
                completedAt,
                1L,
                now,
                now);
    }

    private SingleUploadSessionJpaEntity createEntityWithVersion(Long version) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(java.time.Duration.ofMinutes(15));

        return SingleUploadSessionJpaEntity.of(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                null,
                "01912345-6789-7abc-def0-123456789100",
                "Test Org",
                "setof",
                "01912345-6789-7abc-def0-123456789001",
                "Connectly",
                "SELLER",
                "seller@test.com",
                "document.pdf",
                1024 * 1024L,
                "application/pdf",
                "test-bucket",
                "uploads/document.pdf",
                expiresAt,
                SessionStatus.ACTIVE,
                "https://presigned-url.s3.amazonaws.com/...",
                null,
                null,
                version,
                now,
                now);
    }
}
