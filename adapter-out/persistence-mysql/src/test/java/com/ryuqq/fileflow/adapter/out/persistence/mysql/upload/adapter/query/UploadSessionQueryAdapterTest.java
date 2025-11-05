package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.adapter.query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
}

