package com.ryuqq.fileflow.adapter.out.persistence.session.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ryuqq.fileflow.adapter.out.persistence.session.entity.SingleUploadSessionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.mapper.SingleUploadSessionJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.session.repository.SingleUploadSessionJpaRepository;
import com.ryuqq.fileflow.domain.iam.vo.Organization;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.Tenant;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import com.ryuqq.fileflow.domain.iam.vo.UserRole;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("PersistSingleUploadSessionAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class PersistSingleUploadSessionAdapterTest {

    @Mock private SingleUploadSessionJpaRepository repository;

    @Mock private SingleUploadSessionJpaMapper mapper;

    private PersistSingleUploadSessionAdapter adapter;
    private static final Instant FIXED_INSTANT = Instant.parse("2025-11-26T10:00:00Z");

    @BeforeEach
    void setUp() {
        adapter = new PersistSingleUploadSessionAdapter(repository, mapper);
    }

    @Nested
    @DisplayName("persist 테스트")
    class PersistTest {

        @Test
        @DisplayName("SingleUploadSession을 영속화하고 반환한다")
        void persist_WithValidSession_ShouldReturnPersistedSession() {
            // given
            String sessionId = UUID.randomUUID().toString();
            SingleUploadSession session = createSession(sessionId);
            SingleUploadSessionJpaEntity entity = createEntity(sessionId);

            when(mapper.toEntity(session)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(entity);
            when(mapper.toDomain(entity)).thenReturn(session);

            // when
            SingleUploadSession result = adapter.persist(session);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId().getValue()).isEqualTo(sessionId);
        }

        @Test
        @DisplayName("Mapper를 호출하여 Entity로 변환한다")
        void persist_ShouldCallMapperToEntity() {
            // given
            String sessionId = UUID.randomUUID().toString();
            SingleUploadSession session = createSession(sessionId);
            SingleUploadSessionJpaEntity entity = createEntity(sessionId);

            when(mapper.toEntity(session)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(entity);
            when(mapper.toDomain(entity)).thenReturn(session);

            // when
            adapter.persist(session);

            // then
            verify(mapper).toEntity(session);
        }

        @Test
        @DisplayName("Repository를 호출하여 Entity를 저장한다")
        void persist_ShouldCallRepositorySave() {
            // given
            String sessionId = UUID.randomUUID().toString();
            SingleUploadSession session = createSession(sessionId);
            SingleUploadSessionJpaEntity entity = createEntity(sessionId);

            when(mapper.toEntity(session)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(entity);
            when(mapper.toDomain(entity)).thenReturn(session);

            // when
            adapter.persist(session);

            // then
            verify(repository).save(entity);
        }

        @Test
        @DisplayName("저장된 Entity를 Domain으로 변환하여 반환한다")
        void persist_ShouldCallMapperToDomain() {
            // given
            String sessionId = UUID.randomUUID().toString();
            SingleUploadSession session = createSession(sessionId);
            SingleUploadSessionJpaEntity entity = createEntity(sessionId);

            when(mapper.toEntity(session)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(entity);
            when(mapper.toDomain(entity)).thenReturn(session);

            // when
            adapter.persist(session);

            // then
            verify(mapper).toDomain(entity);
        }
    }

    // ==================== Helper Methods ====================

    private SingleUploadSession createSession(String sessionId) {
        String tenantId = "01912345-6789-7abc-def0-123456789001";
        String organizationId = "01912345-6789-7abc-def0-123456789100";

        Tenant tenant = Tenant.of(TenantId.of(tenantId), "Connectly");
        Organization organization = Organization.of(OrganizationId.of(organizationId), "Test Org", "setof", UserRole.SELLER);
        UserContext userContext = UserContext.of(tenant, organization, "seller@test.com", null);

        return SingleUploadSession.of(
                UploadSessionId.of(UUID.fromString(sessionId)),
                IdempotencyKey.of(UUID.randomUUID()),
                userContext,
                FileName.of("document.pdf"),
                FileSize.of(1024 * 1024L),
                ContentType.of("application/pdf"),
                S3Bucket.of("test-bucket"),
                S3Key.of("uploads/document.pdf"),
                ExpirationTime.of(FIXED_INSTANT.plus(java.time.Duration.ofMinutes(15))),
                FIXED_INSTANT,
                SessionStatus.ACTIVE,
                PresignedUrl.of("https://presigned-url.s3.amazonaws.com/..."),
                null,
                null,
                FIXED_INSTANT,
                0L);
    }

    private SingleUploadSessionJpaEntity createEntity(String sessionId) {
        String tenantId = "01912345-6789-7abc-def0-123456789001";
        String organizationId = "01912345-6789-7abc-def0-123456789100";

        Instant now = Instant.now();
        Instant expiresAt = now.plus(java.time.Duration.ofMinutes(15));

        return SingleUploadSessionJpaEntity.of(
                sessionId,
                UUID.randomUUID().toString(),
                null,
                organizationId,
                "Test Org",
                "setof",
                tenantId,
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
                0L,
                now,
                now);
    }
}
