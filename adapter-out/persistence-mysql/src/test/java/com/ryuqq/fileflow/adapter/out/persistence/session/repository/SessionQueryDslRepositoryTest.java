package com.ryuqq.fileflow.adapter.out.persistence.session.repository;

import static com.ryuqq.fileflow.adapter.out.persistence.session.entity.QMultipartUploadSessionJpaEntity.multipartUploadSessionJpaEntity;
import static com.ryuqq.fileflow.adapter.out.persistence.session.entity.QSingleUploadSessionJpaEntity.singleUploadSessionJpaEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.MultipartUploadSessionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.SingleUploadSessionJpaEntity;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.iam.vo.UserId;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("SessionQueryDslRepository 단위 테스트")
@ExtendWith(MockitoExtension.class)
class SessionQueryDslRepositoryTest {
    // 테스트용 UUIDv7 값 (실제 UUIDv7 형식)
    private static final String TEST_TENANT_ID = TenantId.generate().value();
    private static final String TEST_ORG_ID = OrganizationId.generate().value();
    private static final String TEST_USER_ID = UserId.generate().value();

    @Mock private JPAQueryFactory queryFactory;

    @Mock private JPAQuery<SingleUploadSessionJpaEntity> singleQuery;

    @Mock private JPAQuery<MultipartUploadSessionJpaEntity> multipartQuery;

    private SessionQueryDslRepository repository;

    @BeforeEach
    void setUp() {
        repository = new SessionQueryDslRepository(queryFactory);
    }

    @Nested
    @DisplayName("findSingleUploadById 테스트")
    class FindSingleUploadByIdTest {

        @Test
        @DisplayName("ID로 SingleUploadSession을 조회할 수 있다")
        void findSingleUploadById_WithValidId_ShouldReturnEntity() {
            // given
            String sessionId = "session-123";
            SingleUploadSessionJpaEntity entity = createSingleUploadEntity(sessionId);

            when(queryFactory.selectFrom(singleUploadSessionJpaEntity)).thenReturn(singleQuery);
            when(singleQuery.where(any(Predicate.class))).thenReturn(singleQuery);
            when(singleQuery.fetchOne()).thenReturn(entity);

            // when
            Optional<SingleUploadSessionJpaEntity> result =
                    repository.findSingleUploadById(sessionId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(sessionId);
        }

        @Test
        @DisplayName("조회 결과가 없으면 empty Optional을 반환한다")
        void findSingleUploadById_WhenNotFound_ShouldReturnEmpty() {
            // given
            when(queryFactory.selectFrom(singleUploadSessionJpaEntity)).thenReturn(singleQuery);
            when(singleQuery.where(any(Predicate.class))).thenReturn(singleQuery);
            when(singleQuery.fetchOne()).thenReturn(null);

            // when
            Optional<SingleUploadSessionJpaEntity> result =
                    repository.findSingleUploadById("not-exist");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findSingleUploadByIdempotencyKey 테스트")
    class FindSingleUploadByIdempotencyKeyTest {

        @Test
        @DisplayName("IdempotencyKey로 SingleUploadSession을 조회할 수 있다")
        void findSingleUploadByIdempotencyKey_WithValidKey_ShouldReturnEntity() {
            // given
            String idempotencyKey = "idem-key-123";
            SingleUploadSessionJpaEntity entity =
                    createSingleUploadEntityWithIdempotencyKey("session-123", idempotencyKey);

            when(queryFactory.selectFrom(singleUploadSessionJpaEntity)).thenReturn(singleQuery);
            when(singleQuery.where(any(Predicate.class))).thenReturn(singleQuery);
            when(singleQuery.fetchOne()).thenReturn(entity);

            // when
            Optional<SingleUploadSessionJpaEntity> result =
                    repository.findSingleUploadByIdempotencyKey(idempotencyKey);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getIdempotencyKey()).isEqualTo(idempotencyKey);
        }

        @Test
        @DisplayName("조회 결과가 없으면 empty Optional을 반환한다")
        void findSingleUploadByIdempotencyKey_WhenNotFound_ShouldReturnEmpty() {
            // given
            when(queryFactory.selectFrom(singleUploadSessionJpaEntity)).thenReturn(singleQuery);
            when(singleQuery.where(any(Predicate.class))).thenReturn(singleQuery);
            when(singleQuery.fetchOne()).thenReturn(null);

            // when
            Optional<SingleUploadSessionJpaEntity> result =
                    repository.findSingleUploadByIdempotencyKey("not-exist");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findMultipartUploadById 테스트")
    class FindMultipartUploadByIdTest {

        @Test
        @DisplayName("ID로 MultipartUploadSession을 조회할 수 있다")
        void findMultipartUploadById_WithValidId_ShouldReturnEntity() {
            // given
            String sessionId = "mp-session-123";
            MultipartUploadSessionJpaEntity entity = createMultipartUploadEntity(sessionId);

            when(queryFactory.selectFrom(multipartUploadSessionJpaEntity))
                    .thenReturn(multipartQuery);
            when(multipartQuery.where(any(Predicate.class))).thenReturn(multipartQuery);
            when(multipartQuery.fetchOne()).thenReturn(entity);

            // when
            Optional<MultipartUploadSessionJpaEntity> result =
                    repository.findMultipartUploadById(sessionId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(sessionId);
        }

        @Test
        @DisplayName("조회 결과가 없으면 empty Optional을 반환한다")
        void findMultipartUploadById_WhenNotFound_ShouldReturnEmpty() {
            // given
            when(queryFactory.selectFrom(multipartUploadSessionJpaEntity))
                    .thenReturn(multipartQuery);
            when(multipartQuery.where(any(Predicate.class))).thenReturn(multipartQuery);
            when(multipartQuery.fetchOne()).thenReturn(null);

            // when
            Optional<MultipartUploadSessionJpaEntity> result =
                    repository.findMultipartUploadById("not-exist");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findExpiredSingleUploads 테스트")
    class FindExpiredSingleUploadsTest {

        @Test
        @DisplayName("만료된 SingleUploadSession 목록을 조회할 수 있다")
        void findExpiredSingleUploads_WithExpiredSessions_ShouldReturnEntities() {
            // given
            Instant expiredBefore = Instant.now();
            int limit = 100;
            List<SingleUploadSessionJpaEntity> entities =
                    List.of(
                            createSingleUploadEntity("session-1"),
                            createSingleUploadEntity("session-2"));

            when(queryFactory.selectFrom(singleUploadSessionJpaEntity)).thenReturn(singleQuery);
            when(singleQuery.where(any(Predicate[].class))).thenReturn(singleQuery);
            when(singleQuery.limit(limit)).thenReturn(singleQuery);
            when(singleQuery.fetch()).thenReturn(entities);

            // when
            List<SingleUploadSessionJpaEntity> result =
                    repository.findExpiredSingleUploads(expiredBefore, limit);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo("session-1");
            assertThat(result.get(1).getId()).isEqualTo("session-2");
        }

        @Test
        @DisplayName("만료된 세션이 없으면 빈 목록을 반환한다")
        void findExpiredSingleUploads_WhenNoExpired_ShouldReturnEmptyList() {
            // given
            when(queryFactory.selectFrom(singleUploadSessionJpaEntity)).thenReturn(singleQuery);
            when(singleQuery.where(any(Predicate[].class))).thenReturn(singleQuery);
            when(singleQuery.limit(100)).thenReturn(singleQuery);
            when(singleQuery.fetch()).thenReturn(List.of());

            // when
            List<SingleUploadSessionJpaEntity> result =
                    repository.findExpiredSingleUploads(Instant.now(), 100);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("limit 개수만큼 조회한다")
        void findExpiredSingleUploads_WithLimit_ShouldRespectLimit() {
            // given
            int limit = 10;
            List<SingleUploadSessionJpaEntity> entities =
                    List.of(createSingleUploadEntity("session-1"));

            when(queryFactory.selectFrom(singleUploadSessionJpaEntity)).thenReturn(singleQuery);
            when(singleQuery.where(any(Predicate[].class))).thenReturn(singleQuery);
            when(singleQuery.limit(limit)).thenReturn(singleQuery);
            when(singleQuery.fetch()).thenReturn(entities);

            // when
            List<SingleUploadSessionJpaEntity> result =
                    repository.findExpiredSingleUploads(Instant.now(), limit);

            // then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findExpiredMultipartUploads 테스트")
    class FindExpiredMultipartUploadsTest {

        @Test
        @DisplayName("만료된 MultipartUploadSession 목록을 조회할 수 있다")
        void findExpiredMultipartUploads_WithExpiredSessions_ShouldReturnEntities() {
            // given
            Instant expiredBefore = Instant.now();
            int limit = 100;
            List<MultipartUploadSessionJpaEntity> entities =
                    List.of(
                            createMultipartUploadEntity("mp-session-1"),
                            createMultipartUploadEntity("mp-session-2"));

            when(queryFactory.selectFrom(multipartUploadSessionJpaEntity))
                    .thenReturn(multipartQuery);
            when(multipartQuery.where(any(Predicate[].class))).thenReturn(multipartQuery);
            when(multipartQuery.limit(limit)).thenReturn(multipartQuery);
            when(multipartQuery.fetch()).thenReturn(entities);

            // when
            List<MultipartUploadSessionJpaEntity> result =
                    repository.findExpiredMultipartUploads(expiredBefore, limit);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo("mp-session-1");
            assertThat(result.get(1).getId()).isEqualTo("mp-session-2");
        }

        @Test
        @DisplayName("만료된 세션이 없으면 빈 목록을 반환한다")
        void findExpiredMultipartUploads_WhenNoExpired_ShouldReturnEmptyList() {
            // given
            when(queryFactory.selectFrom(multipartUploadSessionJpaEntity))
                    .thenReturn(multipartQuery);
            when(multipartQuery.where(any(Predicate[].class))).thenReturn(multipartQuery);
            when(multipartQuery.limit(100)).thenReturn(multipartQuery);
            when(multipartQuery.fetch()).thenReturn(List.of());

            // when
            List<MultipartUploadSessionJpaEntity> result =
                    repository.findExpiredMultipartUploads(Instant.now(), 100);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("limit 개수만큼 조회한다")
        void findExpiredMultipartUploads_WithLimit_ShouldRespectLimit() {
            // given
            int limit = 5;
            List<MultipartUploadSessionJpaEntity> entities =
                    List.of(createMultipartUploadEntity("mp-session-1"));

            when(queryFactory.selectFrom(multipartUploadSessionJpaEntity))
                    .thenReturn(multipartQuery);
            when(multipartQuery.where(any(Predicate[].class))).thenReturn(multipartQuery);
            when(multipartQuery.limit(limit)).thenReturn(multipartQuery);
            when(multipartQuery.fetch()).thenReturn(entities);

            // when
            List<MultipartUploadSessionJpaEntity> result =
                    repository.findExpiredMultipartUploads(Instant.now(), limit);

            // then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findSingleUploadByIdAndTenantId 테스트")
    class FindSingleUploadByIdAndTenantIdTest {

        @Test
        @DisplayName("ID와 tenantId로 SingleUploadSession을 조회할 수 있다")
        void findSingleUploadByIdAndTenantId_WithValidParams_ShouldReturnEntity() {
            // given
            String sessionId = "session-123";
            String tenantId = TEST_TENANT_ID;
            SingleUploadSessionJpaEntity entity = createSingleUploadEntity(sessionId);

            when(queryFactory.selectFrom(singleUploadSessionJpaEntity)).thenReturn(singleQuery);
            when(singleQuery.where(any(Predicate.class), any(Predicate.class)))
                    .thenReturn(singleQuery);
            when(singleQuery.fetchOne()).thenReturn(entity);

            // when
            Optional<SingleUploadSessionJpaEntity> result =
                    repository.findSingleUploadByIdAndTenantId(sessionId, tenantId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(sessionId);
        }

        @Test
        @DisplayName("조회 결과가 없으면 empty Optional을 반환한다")
        void findSingleUploadByIdAndTenantId_WhenNotFound_ShouldReturnEmpty() {
            // given
            when(queryFactory.selectFrom(singleUploadSessionJpaEntity)).thenReturn(singleQuery);
            when(singleQuery.where(any(Predicate.class), any(Predicate.class)))
                    .thenReturn(singleQuery);
            when(singleQuery.fetchOne()).thenReturn(null);

            // when
            Optional<SingleUploadSessionJpaEntity> result =
                    repository.findSingleUploadByIdAndTenantId("not-exist", TEST_TENANT_ID);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findMultipartUploadByIdAndTenantId 테스트")
    class FindMultipartUploadByIdAndTenantIdTest {

        @Test
        @DisplayName("ID와 tenantId로 MultipartUploadSession을 조회할 수 있다")
        void findMultipartUploadByIdAndTenantId_WithValidParams_ShouldReturnEntity() {
            // given
            String sessionId = "mp-session-123";
            String tenantId = TEST_TENANT_ID;
            MultipartUploadSessionJpaEntity entity = createMultipartUploadEntity(sessionId);

            when(queryFactory.selectFrom(multipartUploadSessionJpaEntity))
                    .thenReturn(multipartQuery);
            when(multipartQuery.where(any(Predicate.class), any(Predicate.class)))
                    .thenReturn(multipartQuery);
            when(multipartQuery.fetchOne()).thenReturn(entity);

            // when
            Optional<MultipartUploadSessionJpaEntity> result =
                    repository.findMultipartUploadByIdAndTenantId(sessionId, tenantId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(sessionId);
        }

        @Test
        @DisplayName("조회 결과가 없으면 empty Optional을 반환한다")
        void findMultipartUploadByIdAndTenantId_WhenNotFound_ShouldReturnEmpty() {
            // given
            when(queryFactory.selectFrom(multipartUploadSessionJpaEntity))
                    .thenReturn(multipartQuery);
            when(multipartQuery.where(any(Predicate.class), any(Predicate.class)))
                    .thenReturn(multipartQuery);
            when(multipartQuery.fetchOne()).thenReturn(null);

            // when
            Optional<MultipartUploadSessionJpaEntity> result =
                    repository.findMultipartUploadByIdAndTenantId("not-exist", TEST_TENANT_ID);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findSingleUploadsByCriteria 테스트")
    class FindSingleUploadsByCriteriaTest {

        @Test
        @DisplayName("검색 조건으로 SingleUploadSession 목록을 조회할 수 있다")
        void findSingleUploadsByCriteria_WithValidCriteria_ShouldReturnEntities() {
            // given
            String tenantId = TEST_TENANT_ID;
            String organizationId = TEST_ORG_ID;
            SessionStatus status = SessionStatus.ACTIVE;
            long offset = 0;
            int limit = 20;
            List<SingleUploadSessionJpaEntity> entities =
                    List.of(
                            createSingleUploadEntity("session-1"),
                            createSingleUploadEntity("session-2"));

            when(queryFactory.selectFrom(singleUploadSessionJpaEntity)).thenReturn(singleQuery);
            when(singleQuery.where(any(Predicate.class), any(), any())).thenReturn(singleQuery);
            when(singleQuery.orderBy(any(com.querydsl.core.types.OrderSpecifier.class)))
                    .thenReturn(singleQuery);
            when(singleQuery.offset(offset)).thenReturn(singleQuery);
            when(singleQuery.limit(limit)).thenReturn(singleQuery);
            when(singleQuery.fetch()).thenReturn(entities);

            // when
            List<SingleUploadSessionJpaEntity> result =
                    repository.findSingleUploadsByCriteria(
                            tenantId, organizationId, status, offset, limit);

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("organizationId와 status가 null이어도 조회할 수 있다")
        void findSingleUploadsByCriteria_WithNullFilters_ShouldReturnEntities() {
            // given
            String tenantId = TEST_TENANT_ID;
            long offset = 0;
            int limit = 20;
            List<SingleUploadSessionJpaEntity> entities =
                    List.of(createSingleUploadEntity("session-1"));

            when(queryFactory.selectFrom(singleUploadSessionJpaEntity)).thenReturn(singleQuery);
            when(singleQuery.where(any(Predicate.class), any(), any())).thenReturn(singleQuery);
            when(singleQuery.orderBy(any(com.querydsl.core.types.OrderSpecifier.class)))
                    .thenReturn(singleQuery);
            when(singleQuery.offset(offset)).thenReturn(singleQuery);
            when(singleQuery.limit(limit)).thenReturn(singleQuery);
            when(singleQuery.fetch()).thenReturn(entities);

            // when
            List<SingleUploadSessionJpaEntity> result =
                    repository.findSingleUploadsByCriteria(tenantId, null, null, offset, limit);

            // then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findMultipartUploadsByCriteria 테스트")
    class FindMultipartUploadsByCriteriaTest {

        @Test
        @DisplayName("검색 조건으로 MultipartUploadSession 목록을 조회할 수 있다")
        void findMultipartUploadsByCriteria_WithValidCriteria_ShouldReturnEntities() {
            // given
            String tenantId = TEST_TENANT_ID;
            String organizationId = TEST_ORG_ID;
            SessionStatus status = SessionStatus.ACTIVE;
            long offset = 0;
            int limit = 20;
            List<MultipartUploadSessionJpaEntity> entities =
                    List.of(createMultipartUploadEntity("mp-session-1"));

            when(queryFactory.selectFrom(multipartUploadSessionJpaEntity))
                    .thenReturn(multipartQuery);
            when(multipartQuery.where(any(Predicate.class), any(), any()))
                    .thenReturn(multipartQuery);
            when(multipartQuery.orderBy(any(com.querydsl.core.types.OrderSpecifier.class)))
                    .thenReturn(multipartQuery);
            when(multipartQuery.offset(offset)).thenReturn(multipartQuery);
            when(multipartQuery.limit(limit)).thenReturn(multipartQuery);
            when(multipartQuery.fetch()).thenReturn(entities);

            // when
            List<MultipartUploadSessionJpaEntity> result =
                    repository.findMultipartUploadsByCriteria(
                            tenantId, organizationId, status, offset, limit);

            // then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("countSingleUploadsByCriteria 테스트")
    class CountSingleUploadsByCriteriaTest {

        @Mock private JPAQuery<Long> countQuery;

        @Test
        @DisplayName("검색 조건으로 SingleUploadSession 개수를 조회할 수 있다")
        void countSingleUploadsByCriteria_WithValidCriteria_ShouldReturnCount() {
            // given
            String tenantId = TEST_TENANT_ID;
            String organizationId = TEST_ORG_ID;
            SessionStatus status = SessionStatus.ACTIVE;

            when(queryFactory.select(singleUploadSessionJpaEntity.count())).thenReturn(countQuery);
            when(countQuery.from(singleUploadSessionJpaEntity)).thenReturn(countQuery);
            when(countQuery.where(any(Predicate.class), any(), any())).thenReturn(countQuery);
            when(countQuery.fetchOne()).thenReturn(5L);

            // when
            long result = repository.countSingleUploadsByCriteria(tenantId, organizationId, status);

            // then
            assertThat(result).isEqualTo(5L);
        }

        @Test
        @DisplayName("결과가 null이면 0을 반환한다")
        void countSingleUploadsByCriteria_WhenNull_ShouldReturnZero() {
            // given
            String tenantId = TEST_TENANT_ID;

            when(queryFactory.select(singleUploadSessionJpaEntity.count())).thenReturn(countQuery);
            when(countQuery.from(singleUploadSessionJpaEntity)).thenReturn(countQuery);
            when(countQuery.where(any(Predicate.class), any(), any())).thenReturn(countQuery);
            when(countQuery.fetchOne()).thenReturn(null);

            // when
            long result = repository.countSingleUploadsByCriteria(tenantId, null, null);

            // then
            assertThat(result).isZero();
        }
    }

    @Nested
    @DisplayName("countMultipartUploadsByCriteria 테스트")
    class CountMultipartUploadsByCriteriaTest {

        @Mock private JPAQuery<Long> countQuery;

        @Test
        @DisplayName("검색 조건으로 MultipartUploadSession 개수를 조회할 수 있다")
        void countMultipartUploadsByCriteria_WithValidCriteria_ShouldReturnCount() {
            // given
            String tenantId = TEST_TENANT_ID;
            String organizationId = TEST_ORG_ID;
            SessionStatus status = SessionStatus.ACTIVE;

            when(queryFactory.select(multipartUploadSessionJpaEntity.count()))
                    .thenReturn(countQuery);
            when(countQuery.from(multipartUploadSessionJpaEntity)).thenReturn(countQuery);
            when(countQuery.where(any(Predicate.class), any(), any())).thenReturn(countQuery);
            when(countQuery.fetchOne()).thenReturn(3L);

            // when
            long result =
                    repository.countMultipartUploadsByCriteria(tenantId, organizationId, status);

            // then
            assertThat(result).isEqualTo(3L);
        }

        @Test
        @DisplayName("결과가 null이면 0을 반환한다")
        void countMultipartUploadsByCriteria_WhenNull_ShouldReturnZero() {
            // given
            String tenantId = TEST_TENANT_ID;

            when(queryFactory.select(multipartUploadSessionJpaEntity.count()))
                    .thenReturn(countQuery);
            when(countQuery.from(multipartUploadSessionJpaEntity)).thenReturn(countQuery);
            when(countQuery.where(any(Predicate.class), any(), any())).thenReturn(countQuery);
            when(countQuery.fetchOne()).thenReturn(null);

            // when
            long result = repository.countMultipartUploadsByCriteria(tenantId, null, null);

            // then
            assertThat(result).isZero();
        }
    }

    // ==================== Helper Methods ====================

    private SingleUploadSessionJpaEntity createSingleUploadEntity(String sessionId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(java.time.Duration.ofMinutes(15));

        return SingleUploadSessionJpaEntity.of(
                sessionId,
                "idem-key-" + sessionId,
                null,
                TEST_ORG_ID,
                "Test Org",
                "setof",
                TEST_TENANT_ID,
                "Test Tenant",
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

    private SingleUploadSessionJpaEntity createSingleUploadEntityWithIdempotencyKey(
            String sessionId, String idempotencyKey) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(java.time.Duration.ofMinutes(15));

        return SingleUploadSessionJpaEntity.of(
                sessionId,
                idempotencyKey,
                null,
                TEST_ORG_ID,
                "Test Org",
                "setof",
                TEST_TENANT_ID,
                "Test Tenant",
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

    private MultipartUploadSessionJpaEntity createMultipartUploadEntity(String sessionId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(java.time.Duration.ofDays(1));

        return MultipartUploadSessionJpaEntity.of(
                sessionId,
                TEST_USER_ID,
                TEST_ORG_ID,
                "Connectly Org",
                "connectly",
                TEST_TENANT_ID,
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
