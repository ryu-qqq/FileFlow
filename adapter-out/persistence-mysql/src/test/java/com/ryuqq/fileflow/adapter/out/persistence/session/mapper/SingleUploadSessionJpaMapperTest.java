package com.ryuqq.fileflow.adapter.out.persistence.session.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.ryuqq.fileflow.adapter.out.persistence.session.entity.SingleUploadSessionJpaEntity;
import com.ryuqq.fileflow.domain.common.util.ClockHolder;
import com.ryuqq.fileflow.domain.iam.vo.Organization;
import com.ryuqq.fileflow.domain.iam.vo.Tenant;
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
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("SingleUploadSessionJpaMapper 단위 테스트")
@ExtendWith(MockitoExtension.class)
class SingleUploadSessionJpaMapperTest {

    @Mock private ClockHolder clockHolder;

    private SingleUploadSessionJpaMapper mapper;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        mapper = new SingleUploadSessionJpaMapper(clockHolder);
        fixedClock = Clock.fixed(Instant.parse("2025-11-26T10:00:00Z"), ZoneId.of("UTC"));
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
            assertThat(entity.getOrganizationId()).isEqualTo(100L);
            assertThat(entity.getOrganizationName()).isEqualTo("Test Org");
            assertThat(entity.getOrganizationNamespace()).isEqualTo("setof");
            assertThat(entity.getTenantId()).isEqualTo(1L);
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
            when(clockHolder.getClock()).thenReturn(fixedClock);
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
            when(clockHolder.getClock()).thenReturn(fixedClock);
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
            when(clockHolder.getClock()).thenReturn(fixedClock);
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
            when(clockHolder.getClock()).thenReturn(fixedClock);
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
            when(clockHolder.getClock()).thenReturn(fixedClock);
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
            when(clockHolder.getClock()).thenReturn(fixedClock);
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
            when(clockHolder.getClock()).thenReturn(fixedClock);
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
        Tenant tenant = Tenant.of(1L, "Connectly");
        Organization organization = Organization.of(100L, "Test Org", "setof", UserRole.SELLER);
        UserContext userContext = UserContext.of(tenant, organization, "seller@test.com", null);

        LocalDateTime now = LocalDateTime.now();
        return SingleUploadSession.of(
                UploadSessionId.of(UUID.randomUUID()),
                IdempotencyKey.of(UUID.randomUUID()),
                userContext,
                FileName.of("document.pdf"),
                FileSize.of(1024 * 1024L),
                ContentType.of("application/pdf"),
                S3Bucket.of("test-bucket"),
                S3Key.of("uploads/document.pdf"),
                ExpirationTime.of(LocalDateTime.now().plusMinutes(15)),
                now,
                status,
                PresignedUrl.of("https://presigned-url.s3.amazonaws.com/..."),
                null,
                null,
                now,
                0L,
                fixedClock);
    }

    private SingleUploadSession createDomainWithoutPresignedUrl() {
        Tenant tenant = Tenant.of(1L, "Connectly");
        Organization organization = Organization.of(100L, "Test Org", "setof", UserRole.SELLER);
        UserContext userContext = UserContext.of(tenant, organization, "seller@test.com", null);

        LocalDateTime now = LocalDateTime.now();
        return SingleUploadSession.of(
                UploadSessionId.of(UUID.randomUUID()),
                IdempotencyKey.of(UUID.randomUUID()),
                userContext,
                FileName.of("document.pdf"),
                FileSize.of(1024 * 1024L),
                ContentType.of("application/pdf"),
                S3Bucket.of("test-bucket"),
                S3Key.of("uploads/document.pdf"),
                ExpirationTime.of(LocalDateTime.now().plusMinutes(15)),
                now,
                SessionStatus.PREPARING,
                null,
                null,
                null,
                now,
                null,
                fixedClock);
    }

    private SingleUploadSession createCompletedDomain() {
        Tenant tenant = Tenant.of(1L, "Connectly");
        Organization organization = Organization.of(100L, "Test Org", "setof", UserRole.SELLER);
        UserContext userContext = UserContext.of(tenant, organization, "seller@test.com", null);

        LocalDateTime now = LocalDateTime.now();
        return SingleUploadSession.of(
                UploadSessionId.of(UUID.randomUUID()),
                IdempotencyKey.of(UUID.randomUUID()),
                userContext,
                FileName.of("document.pdf"),
                FileSize.of(1024 * 1024L),
                ContentType.of("application/pdf"),
                S3Bucket.of("test-bucket"),
                S3Key.of("uploads/document.pdf"),
                ExpirationTime.of(LocalDateTime.now().plusMinutes(15)),
                now,
                SessionStatus.COMPLETED,
                PresignedUrl.of("https://presigned-url.s3.amazonaws.com/..."),
                ETag.of("\"abc123\""),
                now,
                now,
                1L,
                fixedClock);
    }

    private SingleUploadSessionJpaEntity createEntity(SessionStatus status) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(15);

        return SingleUploadSessionJpaEntity.of(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                null,
                100L,
                "Test Org",
                "setof",
                1L,
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
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(15);

        return SingleUploadSessionJpaEntity.of(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                null,
                100L,
                "Test Org",
                "setof",
                1L,
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
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(15);
        LocalDateTime completedAt = now.plusMinutes(10);

        return SingleUploadSessionJpaEntity.of(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                null,
                100L,
                "Test Org",
                "setof",
                1L,
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
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(15);

        return SingleUploadSessionJpaEntity.of(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                null,
                100L,
                "Test Org",
                "setof",
                1L,
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
