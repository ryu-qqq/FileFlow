package com.ryuqq.fileflow.adapter.out.persistence.session.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ryuqq.fileflow.adapter.out.persistence.session.entity.MultipartUploadSessionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.mapper.MultipartUploadSessionJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.session.repository.MultipartUploadSessionJpaRepository;
import com.ryuqq.fileflow.domain.iam.vo.Organization;
import com.ryuqq.fileflow.domain.iam.vo.Tenant;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import com.ryuqq.fileflow.domain.iam.vo.UserRole;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ExpirationTime;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.PartSize;
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

@DisplayName("PersistMultipartUploadSessionAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class PersistMultipartUploadSessionAdapterTest {

    @Mock private MultipartUploadSessionJpaRepository sessionRepository;

    @Mock private MultipartUploadSessionJpaMapper mapper;

    private PersistMultipartUploadSessionAdapter adapter;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        adapter = new PersistMultipartUploadSessionAdapter(sessionRepository, mapper);
        fixedClock = Clock.fixed(Instant.parse("2025-11-26T10:00:00Z"), ZoneId.of("UTC"));
    }

    @Nested
    @DisplayName("persist 테스트")
    class PersistTest {

        @Test
        @DisplayName("MultipartUploadSession을 영속화하고 반환한다")
        void persist_WithValidSession_ShouldReturnPersistedSession() {
            // given
            String sessionId = UUID.randomUUID().toString();
            MultipartUploadSession session = createSession(sessionId);
            MultipartUploadSessionJpaEntity entity = createEntity(sessionId);

            when(mapper.toEntity(session)).thenReturn(entity);
            when(sessionRepository.save(entity)).thenReturn(entity);
            when(mapper.toDomain(entity)).thenReturn(session);

            // when
            MultipartUploadSession result = adapter.persist(session);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId().getValue()).isEqualTo(sessionId);
        }

        @Test
        @DisplayName("Mapper를 호출하여 Entity로 변환한다")
        void persist_ShouldCallMapperToEntity() {
            // given
            String sessionId = UUID.randomUUID().toString();
            MultipartUploadSession session = createSession(sessionId);
            MultipartUploadSessionJpaEntity entity = createEntity(sessionId);

            when(mapper.toEntity(session)).thenReturn(entity);
            when(sessionRepository.save(entity)).thenReturn(entity);
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
            MultipartUploadSession session = createSession(sessionId);
            MultipartUploadSessionJpaEntity entity = createEntity(sessionId);

            when(mapper.toEntity(session)).thenReturn(entity);
            when(sessionRepository.save(entity)).thenReturn(entity);
            when(mapper.toDomain(entity)).thenReturn(session);

            // when
            adapter.persist(session);

            // then
            verify(sessionRepository).save(entity);
        }

        @Test
        @DisplayName("저장된 Entity를 Domain으로 변환하여 반환한다")
        void persist_ShouldCallMapperToDomain() {
            // given
            String sessionId = UUID.randomUUID().toString();
            MultipartUploadSession session = createSession(sessionId);
            MultipartUploadSessionJpaEntity entity = createEntity(sessionId);

            when(mapper.toEntity(session)).thenReturn(entity);
            when(sessionRepository.save(entity)).thenReturn(entity);
            when(mapper.toDomain(entity)).thenReturn(session);

            // when
            adapter.persist(session);

            // then
            verify(mapper).toDomain(entity);
        }
    }

    // ==================== Helper Methods ====================

    private MultipartUploadSession createSession(String sessionId) {
        Tenant tenant = Tenant.of(1L, "Connectly");
        Organization organization =
                Organization.of(0L, "Connectly Org", "connectly", UserRole.ADMIN);
        UserContext userContext = UserContext.of(tenant, organization, "admin@test.com", null);

        return MultipartUploadSession.reconstitute(
                UploadSessionId.of(UUID.fromString(sessionId)),
                userContext,
                FileName.of("large-video.mp4"),
                FileSize.of(500 * 1024 * 1024L),
                ContentType.of("video/mp4"),
                S3Bucket.of("test-bucket"),
                S3Key.of("uploads/large-video.mp4"),
                S3UploadId.of("s3-upload-id-xyz"),
                TotalParts.of(10),
                PartSize.of(50 * 1024 * 1024L),
                ExpirationTime.of(LocalDateTime.now(fixedClock).plusDays(1)),
                LocalDateTime.now(fixedClock),
                SessionStatus.ACTIVE,
                null,
                0L,
                fixedClock);
    }

    private MultipartUploadSessionJpaEntity createEntity(String sessionId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(1);

        return MultipartUploadSessionJpaEntity.of(
                sessionId,
                1L,
                0L,
                "Connectly Org",
                "connectly",
                1L,
                "Connectly",
                "ADMIN",
                "admin@test.com",
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
                0L,
                now,
                now);
    }
}
