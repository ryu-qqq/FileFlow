package com.ryuqq.fileflow.adapter.out.persistence.session.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.ryuqq.fileflow.adapter.out.persistence.session.entity.MultipartUploadSessionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.SingleUploadSessionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.mapper.MultipartUploadSessionJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.session.mapper.SingleUploadSessionJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.session.repository.SessionQueryDslRepository;
import com.ryuqq.fileflow.domain.iam.vo.Organization;
import com.ryuqq.fileflow.domain.iam.vo.Tenant;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import com.ryuqq.fileflow.domain.iam.vo.UserRole;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.UploadSession;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ExpirationTime;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.session.vo.PartSize;
import com.ryuqq.fileflow.domain.session.vo.PresignedUrl;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.S3UploadId;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import com.ryuqq.fileflow.domain.session.vo.TotalParts;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionSearchCriteria;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("FindUploadSessionQueryAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class FindUploadSessionQueryAdapterTest {

    @Mock private SessionQueryDslRepository queryRepository;

    @Mock private SingleUploadSessionJpaMapper singleMapper;

    @Mock private MultipartUploadSessionJpaMapper multipartMapper;

    private FindUploadSessionQueryAdapter adapter;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        adapter = new FindUploadSessionQueryAdapter(queryRepository, singleMapper, multipartMapper);
        fixedClock = Clock.fixed(Instant.parse("2025-11-26T10:00:00Z"), ZoneId.of("UTC"));
    }

    @Nested
    @DisplayName("findSingleUploadById 테스트")
    class FindSingleUploadByIdTest {

        @Test
        @DisplayName("ID로 SingleUploadSession을 조회할 수 있다")
        void findSingleUploadById_WithValidId_ShouldReturnSession() {
            // given
            String sessionId = UUID.randomUUID().toString();
            UploadSessionId uploadSessionId = UploadSessionId.of(UUID.fromString(sessionId));
            SingleUploadSessionJpaEntity entity = createSingleEntity(sessionId);
            SingleUploadSession domain = createSingleSession(sessionId);

            when(queryRepository.findSingleUploadById(sessionId)).thenReturn(Optional.of(entity));
            when(singleMapper.toDomain(entity)).thenReturn(domain);

            // when
            Optional<SingleUploadSession> result = adapter.findSingleUploadById(uploadSessionId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId().getValue()).isEqualTo(sessionId);
        }

        @Test
        @DisplayName("조회 결과가 없으면 empty Optional을 반환한다")
        void findSingleUploadById_WhenNotFound_ShouldReturnEmpty() {
            // given
            String sessionId = UUID.randomUUID().toString();
            UploadSessionId uploadSessionId = UploadSessionId.of(UUID.fromString(sessionId));

            when(queryRepository.findSingleUploadById(anyString())).thenReturn(Optional.empty());

            // when
            Optional<SingleUploadSession> result = adapter.findSingleUploadById(uploadSessionId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findSingleUploadByIdempotencyKey 테스트")
    class FindSingleUploadByIdempotencyKeyTest {

        @Test
        @DisplayName("IdempotencyKey로 SingleUploadSession을 조회할 수 있다")
        void findSingleUploadByIdempotencyKey_WithValidKey_ShouldReturnSession() {
            // given
            String sessionId = UUID.randomUUID().toString();
            UUID idempotencyKeyValue = UUID.randomUUID();
            IdempotencyKey idempotencyKey = IdempotencyKey.of(idempotencyKeyValue);
            SingleUploadSessionJpaEntity entity = createSingleEntity(sessionId);
            SingleUploadSession domain = createSingleSession(sessionId);

            when(queryRepository.findSingleUploadByIdempotencyKey(idempotencyKeyValue.toString()))
                    .thenReturn(Optional.of(entity));
            when(singleMapper.toDomain(entity)).thenReturn(domain);

            // when
            Optional<SingleUploadSession> result =
                    adapter.findSingleUploadByIdempotencyKey(idempotencyKey);

            // then
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("조회 결과가 없으면 empty Optional을 반환한다")
        void findSingleUploadByIdempotencyKey_WhenNotFound_ShouldReturnEmpty() {
            // given
            IdempotencyKey idempotencyKey = IdempotencyKey.of(UUID.randomUUID());

            when(queryRepository.findSingleUploadByIdempotencyKey(anyString()))
                    .thenReturn(Optional.empty());

            // when
            Optional<SingleUploadSession> result =
                    adapter.findSingleUploadByIdempotencyKey(idempotencyKey);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findMultipartUploadById 테스트")
    class FindMultipartUploadByIdTest {

        @Test
        @DisplayName("ID로 MultipartUploadSession을 조회할 수 있다")
        void findMultipartUploadById_WithValidId_ShouldReturnSession() {
            // given
            String sessionId = UUID.randomUUID().toString();
            UploadSessionId uploadSessionId = UploadSessionId.of(UUID.fromString(sessionId));
            MultipartUploadSessionJpaEntity entity = createMultipartEntity(sessionId);
            MultipartUploadSession domain = createMultipartSession(sessionId);

            when(queryRepository.findMultipartUploadById(sessionId))
                    .thenReturn(Optional.of(entity));
            when(multipartMapper.toDomain(entity)).thenReturn(domain);

            // when
            Optional<MultipartUploadSession> result =
                    adapter.findMultipartUploadById(uploadSessionId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId().getValue()).isEqualTo(sessionId);
        }

        @Test
        @DisplayName("조회 결과가 없으면 empty Optional을 반환한다")
        void findMultipartUploadById_WhenNotFound_ShouldReturnEmpty() {
            // given
            String sessionId = UUID.randomUUID().toString();
            UploadSessionId uploadSessionId = UploadSessionId.of(UUID.fromString(sessionId));

            when(queryRepository.findMultipartUploadById(anyString())).thenReturn(Optional.empty());

            // when
            Optional<MultipartUploadSession> result =
                    adapter.findMultipartUploadById(uploadSessionId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findById 테스트")
    class FindByIdTest {

        @Test
        @DisplayName("Single 세션이 존재하면 Single을 반환한다")
        void findById_WhenSingleExists_ShouldReturnSingle() {
            // given
            String sessionId = UUID.randomUUID().toString();
            UploadSessionId uploadSessionId = UploadSessionId.of(UUID.fromString(sessionId));
            SingleUploadSessionJpaEntity entity = createSingleEntity(sessionId);
            SingleUploadSession domain = createSingleSession(sessionId);

            when(queryRepository.findSingleUploadById(sessionId)).thenReturn(Optional.of(entity));
            when(singleMapper.toDomain(entity)).thenReturn(domain);

            // when
            Optional<UploadSession> result = adapter.findById(uploadSessionId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isInstanceOf(SingleUploadSession.class);
        }

        @Test
        @DisplayName("Single이 없고 Multipart가 존재하면 Multipart를 반환한다")
        void findById_WhenOnlyMultipartExists_ShouldReturnMultipart() {
            // given
            String sessionId = UUID.randomUUID().toString();
            UploadSessionId uploadSessionId = UploadSessionId.of(UUID.fromString(sessionId));
            MultipartUploadSessionJpaEntity entity = createMultipartEntity(sessionId);
            MultipartUploadSession domain = createMultipartSession(sessionId);

            when(queryRepository.findSingleUploadById(sessionId)).thenReturn(Optional.empty());
            when(queryRepository.findMultipartUploadById(sessionId))
                    .thenReturn(Optional.of(entity));
            when(multipartMapper.toDomain(entity)).thenReturn(domain);

            // when
            Optional<UploadSession> result = adapter.findById(uploadSessionId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isInstanceOf(MultipartUploadSession.class);
        }

        @Test
        @DisplayName("둘 다 없으면 empty Optional을 반환한다")
        void findById_WhenNoneExists_ShouldReturnEmpty() {
            // given
            String sessionId = UUID.randomUUID().toString();
            UploadSessionId uploadSessionId = UploadSessionId.of(UUID.fromString(sessionId));

            when(queryRepository.findSingleUploadById(sessionId)).thenReturn(Optional.empty());
            when(queryRepository.findMultipartUploadById(sessionId)).thenReturn(Optional.empty());

            // when
            Optional<UploadSession> result = adapter.findById(uploadSessionId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findExpiredSingleUploads 테스트")
    class FindExpiredSingleUploadsTest {

        @Test
        @DisplayName("만료된 Single 세션 목록을 조회할 수 있다")
        void findExpiredSingleUploads_ShouldReturnExpiredSessions() {
            // given
            Instant expiredBefore = Instant.now();
            int limit = 10;
            String sessionId = UUID.randomUUID().toString();
            SingleUploadSessionJpaEntity entity = createSingleEntity(sessionId);
            SingleUploadSession domain = createSingleSession(sessionId);

            when(queryRepository.findExpiredSingleUploads(expiredBefore, limit))
                    .thenReturn(List.of(entity));
            when(singleMapper.toDomain(entity)).thenReturn(domain);

            // when
            List<SingleUploadSession> result =
                    adapter.findExpiredSingleUploads(expiredBefore, limit);

            // then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("만료된 세션이 없으면 빈 목록을 반환한다")
        void findExpiredSingleUploads_WhenNoExpired_ShouldReturnEmptyList() {
            // given
            Instant expiredBefore = Instant.now();
            int limit = 10;

            when(queryRepository.findExpiredSingleUploads(any(Instant.class), anyInt()))
                    .thenReturn(List.of());

            // when
            List<SingleUploadSession> result =
                    adapter.findExpiredSingleUploads(expiredBefore, limit);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findExpiredMultipartUploads 테스트")
    class FindExpiredMultipartUploadsTest {

        @Test
        @DisplayName("만료된 Multipart 세션 목록을 조회할 수 있다")
        void findExpiredMultipartUploads_ShouldReturnExpiredSessions() {
            // given
            Instant expiredBefore = Instant.now();
            int limit = 10;
            String sessionId = UUID.randomUUID().toString();
            MultipartUploadSessionJpaEntity entity = createMultipartEntity(sessionId);
            MultipartUploadSession domain = createMultipartSession(sessionId);

            when(queryRepository.findExpiredMultipartUploads(expiredBefore, limit))
                    .thenReturn(List.of(entity));
            when(multipartMapper.toDomain(entity)).thenReturn(domain);

            // when
            List<MultipartUploadSession> result =
                    adapter.findExpiredMultipartUploads(expiredBefore, limit);

            // then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("만료된 세션이 없으면 빈 목록을 반환한다")
        void findExpiredMultipartUploads_WhenNoExpired_ShouldReturnEmptyList() {
            // given
            Instant expiredBefore = Instant.now();
            int limit = 10;

            when(queryRepository.findExpiredMultipartUploads(any(Instant.class), anyInt()))
                    .thenReturn(List.of());

            // when
            List<MultipartUploadSession> result =
                    adapter.findExpiredMultipartUploads(expiredBefore, limit);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByIdAndTenantId 테스트")
    class FindByIdAndTenantIdTest {

        @Test
        @DisplayName("Single 세션이 존재하면 Single을 반환한다")
        void findByIdAndTenantId_WhenSingleExists_ShouldReturnSingle() {
            // given
            String sessionId = UUID.randomUUID().toString();
            Long tenantId = 1L;
            UploadSessionId uploadSessionId = UploadSessionId.of(UUID.fromString(sessionId));
            SingleUploadSessionJpaEntity entity = createSingleEntity(sessionId);
            SingleUploadSession domain = createSingleSession(sessionId);

            when(queryRepository.findSingleUploadByIdAndTenantId(sessionId, tenantId))
                    .thenReturn(Optional.of(entity));
            when(singleMapper.toDomain(entity)).thenReturn(domain);

            // when
            Optional<UploadSession> result = adapter.findByIdAndTenantId(uploadSessionId, tenantId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isInstanceOf(SingleUploadSession.class);
        }

        @Test
        @DisplayName("Single이 없고 Multipart가 존재하면 Multipart를 반환한다")
        void findByIdAndTenantId_WhenOnlyMultipartExists_ShouldReturnMultipart() {
            // given
            String sessionId = UUID.randomUUID().toString();
            Long tenantId = 1L;
            UploadSessionId uploadSessionId = UploadSessionId.of(UUID.fromString(sessionId));
            MultipartUploadSessionJpaEntity entity = createMultipartEntity(sessionId);
            MultipartUploadSession domain = createMultipartSession(sessionId);

            when(queryRepository.findSingleUploadByIdAndTenantId(sessionId, tenantId))
                    .thenReturn(Optional.empty());
            when(queryRepository.findMultipartUploadByIdAndTenantId(sessionId, tenantId))
                    .thenReturn(Optional.of(entity));
            when(multipartMapper.toDomain(entity)).thenReturn(domain);

            // when
            Optional<UploadSession> result = adapter.findByIdAndTenantId(uploadSessionId, tenantId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isInstanceOf(MultipartUploadSession.class);
        }

        @Test
        @DisplayName("둘 다 없으면 empty Optional을 반환한다")
        void findByIdAndTenantId_WhenNoneExists_ShouldReturnEmpty() {
            // given
            String sessionId = UUID.randomUUID().toString();
            Long tenantId = 1L;
            UploadSessionId uploadSessionId = UploadSessionId.of(UUID.fromString(sessionId));

            when(queryRepository.findSingleUploadByIdAndTenantId(sessionId, tenantId))
                    .thenReturn(Optional.empty());
            when(queryRepository.findMultipartUploadByIdAndTenantId(sessionId, tenantId))
                    .thenReturn(Optional.empty());

            // when
            Optional<UploadSession> result = adapter.findByIdAndTenantId(uploadSessionId, tenantId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByCriteria 테스트")
    class FindByCriteriaTest {

        @Test
        @DisplayName("uploadType이 null이면 Single과 Multipart 모두 조회한다")
        void findByCriteria_WithNullUploadType_ShouldReturnBoth() {
            // given
            Long tenantId = 1L;
            Long organizationId = 100L;
            UploadSessionSearchCriteria criteria =
                    UploadSessionSearchCriteria.of(tenantId, organizationId, null, null, 0, 20);

            String singleId = UUID.randomUUID().toString();
            String multipartId = UUID.randomUUID().toString();
            SingleUploadSessionJpaEntity singleEntity = createSingleEntity(singleId);
            MultipartUploadSessionJpaEntity multipartEntity = createMultipartEntity(multipartId);
            SingleUploadSession singleDomain = createSingleSession(singleId);
            MultipartUploadSession multipartDomain = createMultipartSession(multipartId);

            when(queryRepository.findSingleUploadsByCriteria(tenantId, organizationId, null, 0, 20))
                    .thenReturn(List.of(singleEntity));
            when(queryRepository.findMultipartUploadsByCriteria(
                            tenantId, organizationId, null, 0, 20))
                    .thenReturn(List.of(multipartEntity));
            when(singleMapper.toDomain(singleEntity)).thenReturn(singleDomain);
            when(multipartMapper.toDomain(multipartEntity)).thenReturn(multipartDomain);

            // when
            List<UploadSession> result = adapter.findByCriteria(criteria);

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("uploadType이 SINGLE이면 Single만 조회한다")
        void findByCriteria_WithSingleUploadType_ShouldReturnOnlySingle() {
            // given
            Long tenantId = 1L;
            Long organizationId = 100L;
            UploadSessionSearchCriteria criteria =
                    UploadSessionSearchCriteria.of(tenantId, organizationId, null, "SINGLE", 0, 20);

            String singleId = UUID.randomUUID().toString();
            SingleUploadSessionJpaEntity singleEntity = createSingleEntity(singleId);
            SingleUploadSession singleDomain = createSingleSession(singleId);

            when(queryRepository.findSingleUploadsByCriteria(tenantId, organizationId, null, 0, 20))
                    .thenReturn(List.of(singleEntity));
            when(singleMapper.toDomain(singleEntity)).thenReturn(singleDomain);

            // when
            List<UploadSession> result = adapter.findByCriteria(criteria);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isInstanceOf(SingleUploadSession.class);
        }

        @Test
        @DisplayName("uploadType이 MULTIPART이면 Multipart만 조회한다")
        void findByCriteria_WithMultipartUploadType_ShouldReturnOnlyMultipart() {
            // given
            Long tenantId = 1L;
            Long organizationId = 100L;
            UploadSessionSearchCriteria criteria =
                    UploadSessionSearchCriteria.of(
                            tenantId, organizationId, null, "MULTIPART", 0, 20);

            String multipartId = UUID.randomUUID().toString();
            MultipartUploadSessionJpaEntity multipartEntity = createMultipartEntity(multipartId);
            MultipartUploadSession multipartDomain = createMultipartSession(multipartId);

            when(queryRepository.findMultipartUploadsByCriteria(
                            tenantId, organizationId, null, 0, 20))
                    .thenReturn(List.of(multipartEntity));
            when(multipartMapper.toDomain(multipartEntity)).thenReturn(multipartDomain);

            // when
            List<UploadSession> result = adapter.findByCriteria(criteria);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isInstanceOf(MultipartUploadSession.class);
        }

        @Test
        @DisplayName("status 필터로 조회할 수 있다")
        void findByCriteria_WithStatusFilter_ShouldFilterByStatus() {
            // given
            Long tenantId = 1L;
            Long organizationId = 100L;
            SessionStatus status = SessionStatus.COMPLETED;
            UploadSessionSearchCriteria criteria =
                    UploadSessionSearchCriteria.of(tenantId, organizationId, status, null, 0, 20);

            when(queryRepository.findSingleUploadsByCriteria(
                            tenantId, organizationId, status, 0, 20))
                    .thenReturn(List.of());
            when(queryRepository.findMultipartUploadsByCriteria(
                            tenantId, organizationId, status, 0, 20))
                    .thenReturn(List.of());

            // when
            List<UploadSession> result = adapter.findByCriteria(criteria);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("countByCriteria 테스트")
    class CountByCriteriaTest {

        @Test
        @DisplayName("uploadType이 null이면 Single과 Multipart 개수를 합산한다")
        void countByCriteria_WithNullUploadType_ShouldReturnTotalCount() {
            // given
            Long tenantId = 1L;
            Long organizationId = 100L;
            UploadSessionSearchCriteria criteria =
                    UploadSessionSearchCriteria.of(tenantId, organizationId, null, null, 0, 20);

            when(queryRepository.countSingleUploadsByCriteria(tenantId, organizationId, null))
                    .thenReturn(5L);
            when(queryRepository.countMultipartUploadsByCriteria(tenantId, organizationId, null))
                    .thenReturn(3L);

            // when
            long result = adapter.countByCriteria(criteria);

            // then
            assertThat(result).isEqualTo(8L);
        }

        @Test
        @DisplayName("uploadType이 SINGLE이면 Single 개수만 반환한다")
        void countByCriteria_WithSingleUploadType_ShouldReturnSingleCount() {
            // given
            Long tenantId = 1L;
            Long organizationId = 100L;
            UploadSessionSearchCriteria criteria =
                    UploadSessionSearchCriteria.of(tenantId, organizationId, null, "SINGLE", 0, 20);

            when(queryRepository.countSingleUploadsByCriteria(tenantId, organizationId, null))
                    .thenReturn(5L);

            // when
            long result = adapter.countByCriteria(criteria);

            // then
            assertThat(result).isEqualTo(5L);
        }

        @Test
        @DisplayName("uploadType이 MULTIPART이면 Multipart 개수만 반환한다")
        void countByCriteria_WithMultipartUploadType_ShouldReturnMultipartCount() {
            // given
            Long tenantId = 1L;
            Long organizationId = 100L;
            UploadSessionSearchCriteria criteria =
                    UploadSessionSearchCriteria.of(
                            tenantId, organizationId, null, "MULTIPART", 0, 20);

            when(queryRepository.countMultipartUploadsByCriteria(tenantId, organizationId, null))
                    .thenReturn(3L);

            // when
            long result = adapter.countByCriteria(criteria);

            // then
            assertThat(result).isEqualTo(3L);
        }

        @Test
        @DisplayName("status 필터로 개수를 조회할 수 있다")
        void countByCriteria_WithStatusFilter_ShouldFilterByStatus() {
            // given
            Long tenantId = 1L;
            Long organizationId = 100L;
            SessionStatus status = SessionStatus.ACTIVE;
            UploadSessionSearchCriteria criteria =
                    UploadSessionSearchCriteria.of(tenantId, organizationId, status, null, 0, 20);

            when(queryRepository.countSingleUploadsByCriteria(tenantId, organizationId, status))
                    .thenReturn(2L);
            when(queryRepository.countMultipartUploadsByCriteria(tenantId, organizationId, status))
                    .thenReturn(1L);

            // when
            long result = adapter.countByCriteria(criteria);

            // then
            assertThat(result).isEqualTo(3L);
        }
    }

    // ==================== Helper Methods ====================

    private SingleUploadSession createSingleSession(String sessionId) {
        Tenant tenant = Tenant.of(1L, "Connectly");
        Organization organization = Organization.of(100L, "Test Org", "setof", UserRole.SELLER);
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
                ExpirationTime.of(LocalDateTime.now(fixedClock).plusMinutes(15)),
                LocalDateTime.now(fixedClock),
                SessionStatus.ACTIVE,
                PresignedUrl.of("https://presigned-url.s3.amazonaws.com/..."),
                null,
                null,
                0L,
                fixedClock);
    }

    private MultipartUploadSession createMultipartSession(String sessionId) {
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

    private SingleUploadSessionJpaEntity createSingleEntity(String sessionId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(15);

        return SingleUploadSessionJpaEntity.of(
                sessionId,
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
                0L,
                now,
                now);
    }

    private MultipartUploadSessionJpaEntity createMultipartEntity(String sessionId) {
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
