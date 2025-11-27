package com.ryuqq.fileflow.adapter.out.persistence.session.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.ryuqq.fileflow.adapter.out.persistence.session.entity.CompletedPartJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.MultipartUploadSessionJpaEntity;
import com.ryuqq.fileflow.application.common.util.ClockHolder;
import com.ryuqq.fileflow.domain.iam.vo.Organization;
import com.ryuqq.fileflow.domain.iam.vo.Tenant;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import com.ryuqq.fileflow.domain.iam.vo.UserRole;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.ExpirationTime;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.PartNumber;
import com.ryuqq.fileflow.domain.session.vo.PartSize;
import com.ryuqq.fileflow.domain.session.vo.PresignedUrl;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.S3UploadId;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import com.ryuqq.fileflow.domain.session.vo.TotalParts;
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

@DisplayName("MultipartUploadSessionJpaMapper 단위 테스트")
@ExtendWith(MockitoExtension.class)
class MultipartUploadSessionJpaMapperTest {

    @Mock private ClockHolder clockHolder;

    private MultipartUploadSessionJpaMapper mapper;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        mapper = new MultipartUploadSessionJpaMapper(clockHolder);
        fixedClock = Clock.fixed(Instant.parse("2025-11-26T10:00:00Z"), ZoneId.of("UTC"));
    }

    @Nested
    @DisplayName("toEntity 테스트")
    class ToEntityTest {

        @Test
        @DisplayName("Domain을 Entity로 변환할 수 있다")
        void toEntity_WithValidDomain_ShouldConvertToEntity() {
            // given
            MultipartUploadSession domain = createDomain(SessionStatus.ACTIVE);

            // when
            MultipartUploadSessionJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getId()).isEqualTo(domain.getId().getValue());
            assertThat(entity.getUserId()).isNull();
            assertThat(entity.getOrganizationId()).isEqualTo(100L);
            assertThat(entity.getOrganizationName()).isEqualTo("Test Org");
            assertThat(entity.getOrganizationNamespace()).isEqualTo("setof");
            assertThat(entity.getTenantId()).isEqualTo(1L);
            assertThat(entity.getTenantName()).isEqualTo("Connectly");
            assertThat(entity.getUserRole()).isEqualTo("SELLER");
            assertThat(entity.getEmail()).isEqualTo("seller@test.com");
            assertThat(entity.getFileName()).isEqualTo("large-video.mp4");
            assertThat(entity.getFileSize()).isEqualTo(500 * 1024 * 1024L);
            assertThat(entity.getContentType()).isEqualTo("video/mp4");
            assertThat(entity.getBucket()).isEqualTo("test-bucket");
            assertThat(entity.getS3Key()).isEqualTo("uploads/large-video.mp4");
            assertThat(entity.getS3UploadId()).isEqualTo("s3-upload-id-xyz");
            assertThat(entity.getTotalParts()).isEqualTo(10);
            assertThat(entity.getPartSize()).isEqualTo(50 * 1024 * 1024L);
            assertThat(entity.getStatus()).isEqualTo(SessionStatus.ACTIVE);
        }

        @Test
        @DisplayName("완료된 세션은 mergedEtag와 completedAt을 포함한다")
        void toEntity_WithCompletedSession_ShouldIncludeMergedEtagAndCompletedAt() {
            // given
            MultipartUploadSession domain = createCompletedDomain();

            // when
            MultipartUploadSessionJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getStatus()).isEqualTo(SessionStatus.COMPLETED);
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
            MultipartUploadSessionJpaEntity entity = createEntity(SessionStatus.ACTIVE);

            // when
            MultipartUploadSession domain = mapper.toDomain(entity);

            // then
            assertThat(domain.getId().getValue()).isEqualTo(entity.getId());
            assertThat(domain.getFileNameValue()).isEqualTo(entity.getFileName());
            assertThat(domain.getFileSizeValue()).isEqualTo(entity.getFileSize());
            assertThat(domain.getContentTypeValue()).isEqualTo(entity.getContentType());
            assertThat(domain.getBucketValue()).isEqualTo(entity.getBucket());
            assertThat(domain.getS3KeyValue()).isEqualTo(entity.getS3Key());
            assertThat(domain.getS3UploadIdValue()).isEqualTo(entity.getS3UploadId());
            assertThat(domain.getTotalPartsValue()).isEqualTo(entity.getTotalParts());
            assertThat(domain.getPartSizeValue()).isEqualTo(entity.getPartSize());
            assertThat(domain.getStatus()).isEqualTo(entity.getStatus());
        }

        @Test
        @DisplayName("UserContext가 올바르게 복원된다")
        void toDomain_ShouldReconstructUserContext() {
            // given
            when(clockHolder.getClock()).thenReturn(fixedClock);
            MultipartUploadSessionJpaEntity entity = createEntity(SessionStatus.ACTIVE);

            // when
            MultipartUploadSession domain = mapper.toDomain(entity);

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
        @DisplayName("버전 정보가 복원된다")
        void toDomain_ShouldReconstructVersion() {
            // given
            when(clockHolder.getClock()).thenReturn(fixedClock);
            MultipartUploadSessionJpaEntity entity = createEntityWithVersion(5L);

            // when
            MultipartUploadSession domain = mapper.toDomain(entity);

            // then
            assertThat(domain.getVersion()).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("toPartEntity 테스트")
    class ToPartEntityTest {

        @Test
        @DisplayName("신규 CompletedPart를 Entity로 변환할 수 있다 (id 없음)")
        void toPartEntity_WithNewPart_ShouldCreateEntityWithoutId() {
            // given
            String sessionId = UUID.randomUUID().toString();
            CompletedPart domain = createCompletedPartWithoutId(sessionId);

            // when
            CompletedPartJpaEntity entity = mapper.toPartEntity(sessionId, domain);

            // then
            assertThat(entity.getId()).isNull();
            assertThat(entity.getSessionId()).isEqualTo(sessionId);
            assertThat(entity.getPartNumber()).isEqualTo(1);
            assertThat(entity.getPresignedUrl())
                    .isEqualTo("https://presigned-url.s3.amazonaws.com/part1");
            assertThat(entity.getEtag()).isEqualTo("\"etag-part-1\"");
            assertThat(entity.getSize()).isEqualTo(5 * 1024 * 1024L);
        }

        @Test
        @DisplayName("기존 CompletedPart를 Entity로 변환할 수 있다 (id 있음)")
        void toPartEntity_WithExistingPart_ShouldCreateEntityWithId() {
            // given
            String sessionId = UUID.randomUUID().toString();
            CompletedPart domain = createCompletedPartWithId(sessionId, 123L);

            // when
            CompletedPartJpaEntity entity = mapper.toPartEntity(sessionId, domain);

            // then
            assertThat(entity.getId()).isEqualTo(123L);
            assertThat(entity.getSessionId()).isEqualTo(sessionId);
        }
    }

    @Nested
    @DisplayName("toCompletedPart 테스트")
    class ToCompletedPartTest {

        @Test
        @DisplayName("Entity를 CompletedPart Domain으로 변환할 수 있다")
        void toCompletedPart_WithValidEntity_ShouldConvertToDomain() {
            // given
            when(clockHolder.getClock()).thenReturn(fixedClock);
            CompletedPartJpaEntity entity = createCompletedPartEntity();

            // when
            CompletedPart domain = mapper.toCompletedPart(entity);

            // then
            assertThat(domain.getId()).isEqualTo(entity.getId());
            assertThat(domain.getSessionIdValue()).isEqualTo(entity.getSessionId());
            assertThat(domain.getPartNumberValue()).isEqualTo(entity.getPartNumber());
            assertThat(domain.getPresignedUrlValue()).isEqualTo(entity.getPresignedUrl());
            assertThat(domain.getETagValue()).isEqualTo(entity.getEtag());
            assertThat(domain.getSize()).isEqualTo(entity.getSize());
            assertThat(domain.getUploadedAt()).isEqualTo(entity.getUploadedAt());
        }

        @Test
        @DisplayName("완료된 Part의 isCompleted()는 true를 반환한다")
        void toCompletedPart_WithCompletedPart_ShouldReturnIsCompletedTrue() {
            // given
            when(clockHolder.getClock()).thenReturn(fixedClock);
            CompletedPartJpaEntity entity = createCompletedPartEntity();

            // when
            CompletedPart domain = mapper.toCompletedPart(entity);

            // then
            assertThat(domain.isCompleted()).isTrue();
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
            MultipartUploadSession original = createDomain(SessionStatus.ACTIVE);

            // when
            MultipartUploadSessionJpaEntity entity = mapper.toEntity(original);
            MultipartUploadSession restored = mapper.toDomain(entity);

            // then
            assertThat(restored.getId().getValue()).isEqualTo(original.getId().getValue());
            assertThat(restored.getFileNameValue()).isEqualTo(original.getFileNameValue());
            assertThat(restored.getFileSizeValue()).isEqualTo(original.getFileSizeValue());
            assertThat(restored.getContentTypeValue()).isEqualTo(original.getContentTypeValue());
            assertThat(restored.getBucketValue()).isEqualTo(original.getBucketValue());
            assertThat(restored.getS3KeyValue()).isEqualTo(original.getS3KeyValue());
            assertThat(restored.getS3UploadIdValue()).isEqualTo(original.getS3UploadIdValue());
            assertThat(restored.getTotalPartsValue()).isEqualTo(original.getTotalPartsValue());
            assertThat(restored.getPartSizeValue()).isEqualTo(original.getPartSizeValue());
            assertThat(restored.getStatus()).isEqualTo(original.getStatus());
        }
    }

    // ==================== Helper Methods ====================

    private MultipartUploadSession createDomain(SessionStatus status) {
        Tenant tenant = Tenant.of(1L, "Connectly");
        Organization organization = Organization.of(100L, "Test Org", "setof", UserRole.SELLER);
        UserContext userContext = UserContext.of(tenant, organization, "seller@test.com", null);

        return MultipartUploadSession.reconstitute(
                UploadSessionId.of(UUID.randomUUID()),
                userContext,
                FileName.of("large-video.mp4"),
                FileSize.of(500 * 1024 * 1024L),
                ContentType.of("video/mp4"),
                S3Bucket.of("test-bucket"),
                S3Key.of("uploads/large-video.mp4"),
                S3UploadId.of("s3-upload-id-xyz"),
                TotalParts.of(10),
                PartSize.of(50 * 1024 * 1024L),
                ExpirationTime.of(LocalDateTime.now().plusHours(24)),
                LocalDateTime.now(),
                status,
                null,
                0L,
                fixedClock);
    }

    private MultipartUploadSession createCompletedDomain() {
        Tenant tenant = Tenant.of(1L, "Connectly");
        Organization organization = Organization.of(100L, "Test Org", "setof", UserRole.SELLER);
        UserContext userContext = UserContext.of(tenant, organization, "seller@test.com", null);

        return MultipartUploadSession.reconstitute(
                UploadSessionId.of(UUID.randomUUID()),
                userContext,
                FileName.of("large-video.mp4"),
                FileSize.of(500 * 1024 * 1024L),
                ContentType.of("video/mp4"),
                S3Bucket.of("test-bucket"),
                S3Key.of("uploads/large-video.mp4"),
                S3UploadId.of("s3-upload-id-xyz"),
                TotalParts.of(10),
                PartSize.of(50 * 1024 * 1024L),
                ExpirationTime.of(LocalDateTime.now().plusHours(24)),
                LocalDateTime.now(),
                SessionStatus.COMPLETED,
                LocalDateTime.now(),
                1L,
                fixedClock);
    }

    private MultipartUploadSessionJpaEntity createEntity(SessionStatus status) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(24);

        return MultipartUploadSessionJpaEntity.of(
                UUID.randomUUID().toString(),
                null,
                100L,
                "Test Org",
                "setof",
                1L,
                "Connectly",
                "SELLER",
                "seller@test.com",
                "large-video.mp4",
                500 * 1024 * 1024L,
                "video/mp4",
                "test-bucket",
                "uploads/large-video.mp4",
                "s3-upload-id-xyz",
                10,
                50 * 1024 * 1024L,
                expiresAt,
                status,
                null,
                null,
                0L,
                now,
                now);
    }

    private MultipartUploadSessionJpaEntity createEntityWithVersion(Long version) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(24);

        return MultipartUploadSessionJpaEntity.of(
                UUID.randomUUID().toString(),
                null,
                100L,
                "Test Org",
                "setof",
                1L,
                "Connectly",
                "SELLER",
                "seller@test.com",
                "large-video.mp4",
                500 * 1024 * 1024L,
                "video/mp4",
                "test-bucket",
                "uploads/large-video.mp4",
                "s3-upload-id-xyz",
                10,
                50 * 1024 * 1024L,
                expiresAt,
                SessionStatus.ACTIVE,
                null,
                null,
                version,
                now,
                now);
    }

    private CompletedPart createCompletedPartWithoutId(String sessionId) {
        return CompletedPart.of(
                null,
                UploadSessionId.of(sessionId),
                PartNumber.of(1),
                PresignedUrl.of("https://presigned-url.s3.amazonaws.com/part1"),
                ETag.of("\"etag-part-1\""),
                5 * 1024 * 1024L,
                LocalDateTime.now(),
                fixedClock);
    }

    private CompletedPart createCompletedPartWithId(String sessionId, Long id) {
        return CompletedPart.of(
                id,
                UploadSessionId.of(sessionId),
                PartNumber.of(1),
                PresignedUrl.of("https://presigned-url.s3.amazonaws.com/part1"),
                ETag.of("\"etag-part-1\""),
                5 * 1024 * 1024L,
                LocalDateTime.now(),
                fixedClock);
    }

    private CompletedPartJpaEntity createCompletedPartEntity() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime uploadedAt = now.minusMinutes(5);

        return CompletedPartJpaEntity.reconstitute(
                123L,
                UUID.randomUUID().toString(),
                1,
                "https://presigned-url.s3.amazonaws.com/part1",
                "\"etag-part-1\"",
                5 * 1024 * 1024L,
                uploadedAt,
                now,
                now);
    }
}
