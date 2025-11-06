package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.adapter.query;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.UploadSessionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.fixture.UploadSessionJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.repository.UploadSessionJpaRepository;
import com.ryuqq.fileflow.domain.upload.SessionKey;
import com.ryuqq.fileflow.domain.upload.SessionStatus;
import com.ryuqq.fileflow.domain.upload.UploadSession;

/**
 * Upload Session Query Adapter 단위 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>findById 메서드 (ID로 조회)</li>
 *   <li>findBySessionKey 메서드 (Session Key로 조회)</li>
 *   <li>findByStatusAndCreatedBefore 메서드 (상태 및 생성 시간 기준 조회)</li>
 * </ul>
 *
 * <p><strong>검증 항목:</strong></p>
 * <ul>
 *   <li>Entity → Domain 변환 정확성</li>
 *   <li>Query 결과 정확성</li>
 *   <li>Optional 반환 정확성</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@DataJpaTest
@Import(UploadSessionQueryAdapter.class)
@DisplayName("Upload Session Query Adapter 단위 테스트")
class UploadSessionQueryAdapterTest {

    @Autowired
    private UploadSessionQueryAdapter queryAdapter;

    @Autowired
    private UploadSessionJpaRepository jpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Nested
    @DisplayName("findById 메서드 테스트")
    class FindByIdTests {

        @Test
        @DisplayName("findById_WithExistingId_ShouldReturnSession - 기존 ID로 조회 성공")
        void findById_WithExistingId_ShouldReturnSession() {
            // Given - 기존 세션 저장
            UploadSessionJpaEntity entity = UploadSessionJpaEntityFixture.create();
            entity = jpaRepository.save(entity);
            entityManager.flush();
            Long id = entity.getId();

            // When - 조회
            Optional<UploadSession> result = queryAdapter.findById(id);

            // Then - 조회 확인
            assertThat(result).isPresent();
            UploadSession session = result.get();
            assertThat(session.getIdValue()).isEqualTo(id);
            assertThat(session.getSessionKey().value()).isEqualTo(entity.getSessionKey());
            assertThat(session.getTenantId().value()).isEqualTo(entity.getTenantId());
        }

        @Test
        @DisplayName("findById_WithNonExistentId_ShouldReturnEmpty - 존재하지 않는 ID 조회 시 Empty")
        void findById_WithNonExistentId_ShouldReturnEmpty() {
            // Given
            Long nonExistentId = 999L;

            // When
            Optional<UploadSession> result = queryAdapter.findById(nonExistentId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findBySessionKey 메서드 테스트")
    class FindBySessionKeyTests {

        @Test
        @DisplayName("findBySessionKey_WithExistingKey_ShouldReturnSession - 기존 Session Key로 조회 성공")
        void findBySessionKey_WithExistingKey_ShouldReturnSession() {
            // Given - 기존 세션 저장
            UploadSessionJpaEntity entity = UploadSessionJpaEntityFixture.create();
            entity = jpaRepository.save(entity);
            entityManager.flush();
            String sessionKeyValue = entity.getSessionKey();

            // When - 조회
            Optional<UploadSession> result = queryAdapter.findBySessionKey(SessionKey.of(sessionKeyValue));

            // Then - 조회 확인
            assertThat(result).isPresent();
            UploadSession session = result.get();
            assertThat(session.getSessionKey().value()).isEqualTo(sessionKeyValue);
        }

        @Test
        @DisplayName("findBySessionKey_WithNonExistentKey_ShouldReturnEmpty - 존재하지 않는 Session Key 조회 시 Empty")
        void findBySessionKey_WithNonExistentKey_ShouldReturnEmpty() {
            // Given
            SessionKey nonExistentKey = SessionKey.of("non-existent-key");

            // When
            Optional<UploadSession> result = queryAdapter.findBySessionKey(nonExistentKey);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByStatusAndCreatedBefore 메서드 테스트")
    class FindByStatusAndCreatedBeforeTests {

        @Test
        @DisplayName("findByStatusAndCreatedBefore_WithMatchingCriteria_ShouldReturnSessions - 조건에 맞는 세션 조회")
        void findByStatusAndCreatedBefore_WithMatchingCriteria_ShouldReturnSessions() {
            // Given - PENDING 상태 세션 2개 저장
            LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
            LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);

            UploadSessionJpaEntity entity1 = UploadSessionJpaEntityFixture.createWithId(1L);
            entity1 = UploadSessionJpaEntity.reconstitute(
                entity1.getId(),
                entity1.getSessionKey(),
                entity1.getTenantId(),
                entity1.getFileName(),
                entity1.getFileSize(),
                entity1.getUploadType(),
                entity1.getStorageKey(),
                SessionStatus.PENDING,
                entity1.getFileId(),
                entity1.getFailureReason(),
                entity1.getCompletedAt(),
                entity1.getFailedAt(),
                twoDaysAgo,
                twoDaysAgo
            );
            entity1 = jpaRepository.save(entity1);

            UploadSessionJpaEntity entity2 = UploadSessionJpaEntityFixture.createWithId(2L);
            entity2 = UploadSessionJpaEntity.reconstitute(
                entity2.getId(),
                entity2.getSessionKey(),
                entity2.getTenantId(),
                entity2.getFileName(),
                entity2.getFileSize(),
                entity2.getUploadType(),
                entity2.getStorageKey(),
                SessionStatus.PENDING,
                entity2.getFileId(),
                entity2.getFailureReason(),
                entity2.getCompletedAt(),
                entity2.getFailedAt(),
                oneDayAgo,
                oneDayAgo
            );
            entity2 = jpaRepository.save(entity2);

            // IN_PROGRESS 상태 세션 1개 저장 (제외되어야 함)
            UploadSessionJpaEntity entity3 = UploadSessionJpaEntityFixture.createWithId(3L);
            entity3 = UploadSessionJpaEntity.reconstitute(
                entity3.getId(),
                entity3.getSessionKey(),
                entity3.getTenantId(),
                entity3.getFileName(),
                entity3.getFileSize(),
                entity3.getUploadType(),
                entity3.getStorageKey(),
                SessionStatus.IN_PROGRESS,
                entity3.getFileId(),
                entity3.getFailureReason(),
                entity3.getCompletedAt(),
                entity3.getFailedAt(),
                twoDaysAgo,
                twoDaysAgo
            );
            jpaRepository.save(entity3);

            entityManager.flush();
            entityManager.clear();

            // When - PENDING 상태이고 1일 전 이전에 생성된 세션 조회
            LocalDateTime createdBefore = LocalDateTime.now().minusDays(1).minusHours(1);
            List<UploadSession> result = queryAdapter.findByStatusAndCreatedBefore(
                SessionStatus.PENDING,
                createdBefore
            );

            // Then - 조건에 맞는 세션만 조회
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getIdValue()).isEqualTo(entity1.getId());
            assertThat(result.get(0).getStatus()).isEqualTo(SessionStatus.PENDING);
        }

        @Test
        @DisplayName("findByStatusAndCreatedBefore_WithNoMatchingCriteria_ShouldReturnEmpty - 조건에 맞는 세션 없을 시 Empty")
        void findByStatusAndCreatedBefore_WithNoMatchingCriteria_ShouldReturnEmpty() {
            // Given - 최근에 생성된 세션만 존재
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            UploadSessionJpaEntity entity = UploadSessionJpaEntityFixture.createWithId(1L);
            entity = UploadSessionJpaEntity.reconstitute(
                entity.getId(),
                entity.getSessionKey(),
                entity.getTenantId(),
                entity.getFileName(),
                entity.getFileSize(),
                entity.getUploadType(),
                entity.getStorageKey(),
                SessionStatus.PENDING,
                entity.getFileId(),
                entity.getFailureReason(),
                entity.getCompletedAt(),
                entity.getFailedAt(),
                oneHourAgo,
                oneHourAgo
            );
            jpaRepository.save(entity);
            entityManager.flush();
            entityManager.clear();

            // When - 1일 전 이전에 생성된 세션 조회
            LocalDateTime createdBefore = LocalDateTime.now().minusDays(1);
            List<UploadSession> result = queryAdapter.findByStatusAndCreatedBefore(
                SessionStatus.PENDING,
                createdBefore
            );

            // Then - Empty
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByStatusInAndCreatedBefore 메서드 테스트")
    class FindByStatusInAndCreatedBeforeTests {

        @Test
        @DisplayName("findByStatusInAndCreatedBefore_WithMultipleStatuses_ShouldReturnMatchingSessions - 여러 상태 조회")
        void findByStatusInAndCreatedBefore_WithMultipleStatuses_ShouldReturnMatchingSessions() {
            // Given - 여러 상태의 세션 저장
            LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
            LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);

            // PENDING 상태 (2일 전)
            UploadSessionJpaEntity pendingEntity = UploadSessionJpaEntityFixture.createWithId(1L);
            pendingEntity = UploadSessionJpaEntity.reconstitute(
                pendingEntity.getId(),
                pendingEntity.getSessionKey(),
                pendingEntity.getTenantId(),
                pendingEntity.getFileName(),
                pendingEntity.getFileSize(),
                pendingEntity.getUploadType(),
                pendingEntity.getStorageKey(),
                SessionStatus.PENDING,
                pendingEntity.getFileId(),
                pendingEntity.getFailureReason(),
                pendingEntity.getCompletedAt(),
                pendingEntity.getFailedAt(),
                twoDaysAgo,
                twoDaysAgo
            );
            pendingEntity = jpaRepository.save(pendingEntity);

            // IN_PROGRESS 상태 (2일 전)
            UploadSessionJpaEntity inProgressEntity = UploadSessionJpaEntityFixture.createWithId(2L);
            inProgressEntity = UploadSessionJpaEntity.reconstitute(
                inProgressEntity.getId(),
                inProgressEntity.getSessionKey(),
                inProgressEntity.getTenantId(),
                inProgressEntity.getFileName(),
                inProgressEntity.getFileSize(),
                inProgressEntity.getUploadType(),
                inProgressEntity.getStorageKey(),
                SessionStatus.IN_PROGRESS,
                inProgressEntity.getFileId(),
                inProgressEntity.getFailureReason(),
                inProgressEntity.getCompletedAt(),
                inProgressEntity.getFailedAt(),
                twoDaysAgo,
                twoDaysAgo
            );
            inProgressEntity = jpaRepository.save(inProgressEntity);

            // COMPLETED 상태 (2일 전) - 제외되어야 함
            UploadSessionJpaEntity completedEntity = UploadSessionJpaEntityFixture.createWithId(3L);
            completedEntity = UploadSessionJpaEntity.reconstitute(
                completedEntity.getId(),
                completedEntity.getSessionKey(),
                completedEntity.getTenantId(),
                completedEntity.getFileName(),
                completedEntity.getFileSize(),
                completedEntity.getUploadType(),
                completedEntity.getStorageKey(),
                SessionStatus.COMPLETED,
                completedEntity.getFileId(),
                completedEntity.getFailureReason(),
                completedEntity.getCompletedAt(),
                completedEntity.getFailedAt(),
                twoDaysAgo,
                twoDaysAgo
            );
            jpaRepository.save(completedEntity);

            // PENDING 상태 (최근) - 제외되어야 함
            UploadSessionJpaEntity recentEntity = UploadSessionJpaEntityFixture.createWithId(4L);
            recentEntity = UploadSessionJpaEntity.reconstitute(
                recentEntity.getId(),
                recentEntity.getSessionKey(),
                recentEntity.getTenantId(),
                recentEntity.getFileName(),
                recentEntity.getFileSize(),
                recentEntity.getUploadType(),
                recentEntity.getStorageKey(),
                SessionStatus.PENDING,
                recentEntity.getFileId(),
                recentEntity.getFailureReason(),
                recentEntity.getCompletedAt(),
                recentEntity.getFailedAt(),
                oneDayAgo,
                oneDayAgo
            );
            jpaRepository.save(recentEntity);

            entityManager.flush();
            entityManager.clear();

            // When - PENDING 또는 IN_PROGRESS 상태이고 1일 전 이전에 생성된 세션 조회
            LocalDateTime createdBefore = LocalDateTime.now().minusDays(1).minusHours(1);
            List<SessionStatus> statuses = List.of(SessionStatus.PENDING, SessionStatus.IN_PROGRESS);
            List<UploadSession> result = queryAdapter.findByStatusInAndCreatedBefore(
                statuses,
                createdBefore
            );

            // Then - PENDING과 IN_PROGRESS 상태이고 오래된 세션만 조회
            assertThat(result).hasSize(2);
            assertThat(result).extracting(UploadSession::getIdValue)
                .containsExactlyInAnyOrder(pendingEntity.getId(), inProgressEntity.getId());
            assertThat(result).extracting(UploadSession::getStatus)
                .containsExactlyInAnyOrder(SessionStatus.PENDING, SessionStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("findByStatusInAndCreatedBefore_WithEmptyStatuses_ShouldThrowException - 빈 상태 목록 시 예외")
        void findByStatusInAndCreatedBefore_WithEmptyStatuses_ShouldThrowException() {
            // Given
            List<SessionStatus> emptyStatuses = List.of();
            LocalDateTime createdBefore = LocalDateTime.now().minusDays(1);

            // When & Then
            assertThat(
                org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> queryAdapter.findByStatusInAndCreatedBefore(emptyStatuses, createdBefore)
                )
            ).hasMessageContaining("statuses must not be null or empty");
        }

        @Test
        @DisplayName("findByStatusInAndCreatedBefore_WithNullStatuses_ShouldThrowException - null 상태 목록 시 예외")
        void findByStatusInAndCreatedBefore_WithNullStatuses_ShouldThrowException() {
            // Given
            LocalDateTime createdBefore = LocalDateTime.now().minusDays(1);

            // When & Then
            assertThat(
                org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> queryAdapter.findByStatusInAndCreatedBefore(null, createdBefore)
                )
            ).hasMessageContaining("statuses must not be null or empty");
        }
    }

    @Nested
    @DisplayName("countByTenantIdAndStatus 메서드 테스트")
    class CountByTenantIdAndStatusTests {

        @Test
        @DisplayName("countByTenantIdAndStatus_WithMatchingCriteria_ShouldReturnCount - 조건에 맞는 세션 개수 반환")
        void countByTenantIdAndStatus_WithMatchingCriteria_ShouldReturnCount() {
            // Given - Tenant 1의 IN_PROGRESS 세션 3개, PENDING 세션 2개
            Long tenantId1 = 100L;
            Long tenantId2 = 200L;

            // Tenant 1 - IN_PROGRESS (3개)
            for (int i = 1; i <= 3; i++) {
                UploadSessionJpaEntity entity = UploadSessionJpaEntityFixture.createWithId((long) i);
                entity = UploadSessionJpaEntity.reconstitute(
                    entity.getId(),
                    entity.getSessionKey(),
                    tenantId1,
                    entity.getFileName(),
                    entity.getFileSize(),
                    entity.getUploadType(),
                    entity.getStorageKey(),
                    SessionStatus.IN_PROGRESS,
                    entity.getFileId(),
                    entity.getFailureReason(),
                    entity.getCompletedAt(),
                    entity.getFailedAt(),
                    entity.getCreatedAt(),
                    entity.getUpdatedAt()
                );
                jpaRepository.save(entity);
            }

            // Tenant 1 - PENDING (2개)
            for (int i = 4; i <= 5; i++) {
                UploadSessionJpaEntity entity = UploadSessionJpaEntityFixture.createWithId((long) i);
                entity = UploadSessionJpaEntity.reconstitute(
                    entity.getId(),
                    entity.getSessionKey(),
                    tenantId1,
                    entity.getFileName(),
                    entity.getFileSize(),
                    entity.getUploadType(),
                    entity.getStorageKey(),
                    SessionStatus.PENDING,
                    entity.getFileId(),
                    entity.getFailureReason(),
                    entity.getCompletedAt(),
                    entity.getFailedAt(),
                    entity.getCreatedAt(),
                    entity.getUpdatedAt()
                );
                jpaRepository.save(entity);
            }

            // Tenant 2 - IN_PROGRESS (1개) - 제외되어야 함
            UploadSessionJpaEntity entity6 = UploadSessionJpaEntityFixture.createWithId(6L);
            entity6 = UploadSessionJpaEntity.reconstitute(
                entity6.getId(),
                entity6.getSessionKey(),
                tenantId2,
                entity6.getFileName(),
                entity6.getFileSize(),
                entity6.getUploadType(),
                entity6.getStorageKey(),
                SessionStatus.IN_PROGRESS,
                entity6.getFileId(),
                entity6.getFailureReason(),
                entity6.getCompletedAt(),
                entity6.getFailedAt(),
                entity6.getCreatedAt(),
                entity6.getUpdatedAt()
            );
            jpaRepository.save(entity6);

            entityManager.flush();
            entityManager.clear();

            // When - Tenant 1의 IN_PROGRESS 세션 개수 조회
            long count = queryAdapter.countByTenantIdAndStatus(tenantId1, SessionStatus.IN_PROGRESS);

            // Then - 3개 반환
            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("countByTenantIdAndStatus_WithNoMatchingCriteria_ShouldReturnZero - 조건에 맞는 세션 없을 시 0 반환")
        void countByTenantIdAndStatus_WithNoMatchingCriteria_ShouldReturnZero() {
            // Given - Tenant 1의 PENDING 세션만 존재
            Long tenantId = 100L;
            UploadSessionJpaEntity entity = UploadSessionJpaEntityFixture.createWithId(1L);
            entity = UploadSessionJpaEntity.reconstitute(
                entity.getId(),
                entity.getSessionKey(),
                tenantId,
                entity.getFileName(),
                entity.getFileSize(),
                entity.getUploadType(),
                entity.getStorageKey(),
                SessionStatus.PENDING,
                entity.getFileId(),
                entity.getFailureReason(),
                entity.getCompletedAt(),
                entity.getFailedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
            );
            jpaRepository.save(entity);
            entityManager.flush();
            entityManager.clear();

            // When - IN_PROGRESS 세션 개수 조회
            long count = queryAdapter.countByTenantIdAndStatus(tenantId, SessionStatus.IN_PROGRESS);

            // Then - 0 반환
            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("countByTenantIdAndStatus_WithNullTenantId_ShouldThrowException - null tenantId 시 예외")
        void countByTenantIdAndStatus_WithNullTenantId_ShouldThrowException() {
            // Given
            SessionStatus status = SessionStatus.IN_PROGRESS;

            // When & Then
            assertThat(
                org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> queryAdapter.countByTenantIdAndStatus(null, status)
                )
            ).hasMessageContaining("tenantId must not be null");
        }

        @Test
        @DisplayName("countByTenantIdAndStatus_WithNullStatus_ShouldThrowException - null status 시 예외")
        void countByTenantIdAndStatus_WithNullStatus_ShouldThrowException() {
            // Given
            Long tenantId = 100L;

            // When & Then
            assertThat(
                org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> queryAdapter.countByTenantIdAndStatus(tenantId, null)
                )
            ).hasMessageContaining("status must not be null");
        }
    }
}

