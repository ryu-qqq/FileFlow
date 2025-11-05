package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.adapter.command;

import jakarta.persistence.EntityManager;
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
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.fixture.UploadSessionFixture;

/**
 * Upload Session Command Adapter 단위 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>save 메서드 (Domain → Entity 변환 및 저장)</li>
 *   <li>delete 메서드 (영구 삭제)</li>
 * </ul>
 *
 * <p><strong>검증 항목:</strong></p>
 * <ul>
 *   <li>Domain → Entity 변환 정확성</li>
 *   <li>JPA save() 동작</li>
 *   <li>Entity → Domain 변환 정확성</li>
 *   <li>ID 할당 확인</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@DataJpaTest
@Import(UploadSessionCommandAdapter.class)
@DisplayName("Upload Session Command Adapter 단위 테스트")
class UploadSessionCommandAdapterTest {

    @Autowired
    private UploadSessionCommandAdapter commandAdapter;

    @Autowired
    private UploadSessionJpaRepository jpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Nested
    @DisplayName("save 메서드 테스트")
    class SaveTests {

        @Test
        @DisplayName("save_WithNewSession_ShouldPersistAndReturnId - 신규 세션 저장 시 ID 할당")
        void save_WithNewSession_ShouldPersistAndReturnId() {
            // Given - Domain Model (ID 없음)
            UploadSession session = UploadSessionFixture.createSingle();

            // When - Command Adapter로 저장
            UploadSession savedSession = commandAdapter.save(session);

            // Then - ID 할당 확인
            assertThat(savedSession.getIdValue()).isNotNull();

            // DB 검증
            Optional<UploadSessionJpaEntity> entity =
                jpaRepository.findById(savedSession.getIdValue());
            assertThat(entity).isPresent();
            assertThat(entity.get().getSessionKey())
                .isEqualTo(savedSession.getSessionKey().value());
            assertThat(entity.get().getTenantId())
                .isEqualTo(savedSession.getTenantId().value());
        }

        @Test
        @DisplayName("save_WithExistingSession_ShouldUpdate - 기존 세션 수정 시 업데이트")
        void save_WithExistingSession_ShouldUpdate() {
            // Given - 기존 세션 저장
            UploadSessionJpaEntity entity = UploadSessionJpaEntityFixture.create();
            entity = jpaRepository.save(entity);
            entityManager.flush();
            entityManager.clear();

            // Domain으로 변환 후 수정
            UploadSession session = UploadSessionFixture.reconstitute(
                entity.getId(),
                com.ryuqq.fileflow.domain.upload.SessionKey.of(entity.getSessionKey()),
                com.ryuqq.fileflow.domain.iam.tenant.TenantId.of(entity.getTenantId()),
                com.ryuqq.fileflow.domain.upload.FileName.of(entity.getFileName()),
                com.ryuqq.fileflow.domain.upload.FileSize.of(entity.getFileSize()),
                entity.getUploadType(),
                entity.getStorageKey() != null
                    ? com.ryuqq.fileflow.domain.upload.StorageKey.of(entity.getStorageKey())
                    : null,
                entity.getStatus(),
                entity.getFileId(),
                entity.getFailureReason() != null
                    ? com.ryuqq.fileflow.domain.upload.FailureReason.of(entity.getFailureReason())
                    : null,
                java.time.LocalDateTime.now().minusDays(1),
                java.time.LocalDateTime.now(),
                entity.getCompletedAt(),
                entity.getFailedAt()
            );

            // 상태 변경
            session.start();

            // When - 저장
            UploadSession savedSession = commandAdapter.save(session);

            // Then - 수정 확인
            entityManager.flush();
            entityManager.clear();

            UploadSessionJpaEntity updated =
                jpaRepository.findById(savedSession.getIdValue()).orElseThrow();
            assertThat(updated.getStatus())
                .isEqualTo(com.ryuqq.fileflow.domain.upload.SessionStatus.IN_PROGRESS);
        }
    }

    @Nested
    @DisplayName("delete 메서드 테스트")
    class DeleteTests {

        @Test
        @DisplayName("delete_WithExistingId_ShouldRemove - 기존 세션 삭제 성공")
        void delete_WithExistingId_ShouldRemove() {
            // Given - 기존 세션 저장
            UploadSessionJpaEntity entity = UploadSessionJpaEntityFixture.create();
            entity = jpaRepository.save(entity);
            entityManager.flush();
            Long id = entity.getId();

            // When - 삭제
            commandAdapter.delete(id);

            // Then - 삭제 확인
            entityManager.flush();
            entityManager.clear();

            Optional<UploadSessionJpaEntity> deleted = jpaRepository.findById(id);
            assertThat(deleted).isEmpty();
        }
    }
}

