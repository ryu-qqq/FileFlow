package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity.FileVariantJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.repository.FileVariantJpaRepository;
import com.ryuqq.fileflow.domain.file.asset.FileAssetId;
import com.ryuqq.fileflow.domain.file.variant.FileVariant;
import com.ryuqq.fileflow.domain.file.variant.FileVariantCreatedEvent;
import com.ryuqq.fileflow.domain.file.variant.VariantType;
import com.ryuqq.fileflow.domain.upload.FileSize;
import com.ryuqq.fileflow.domain.upload.MimeType;
import com.ryuqq.fileflow.domain.upload.StorageKey;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/**
 * FileVariant Command Adapter 통합 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>save 메서드 (Domain → Entity 변환 및 저장)</li>
 *   <li>Domain Event 발행</li>
 *   <li>ID 자동 할당</li>
 * </ul>
 *
 * <p><strong>검증 항목:</strong></p>
 * <ul>
 *   <li>Domain → Entity 변환 정확성</li>
 *   <li>JPA save() 동작</li>
 *   <li>Entity → Domain 변환 정확성</li>
 *   <li>ID 할당 확인</li>
 *   <li>Domain Event 발행 확인</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@DataJpaTest
@Import(FileVariantCommandAdapter.class)
@DisplayName("FileVariant Command Adapter 통합 테스트")
class FileVariantCommandAdapterTest {

    @Autowired
    private FileVariantCommandAdapter fileVariantCommandAdapter;

    @Autowired
    private FileVariantJpaRepository fileVariantJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @MockBean
    private ApplicationEventPublisher eventPublisher;

    @Nested
    @DisplayName("save 메서드 테스트")
    class SaveTests {

        @Test
        @DisplayName("save_WithNewFileVariant_ShouldGenerateId - 신규 FileVariant 저장 시 ID 할당")
        void save_WithNewFileVariant_ShouldGenerateId() {
            // Given
            FileVariant variant = FileVariant.create(
                FileAssetId.of(1L),
                VariantType.THUMBNAIL,
                StorageKey.of("tenant-1/org-2/thumbnail/image.jpg"),
                FileSize.of(51200L),
                new MimeType("image/webp")
            );

            // When
            FileVariant savedVariant = fileVariantCommandAdapter.save(variant);

            // Then
            assertThat(savedVariant.getIdValue()).isNotNull();
            assertThat(savedVariant.getIdValue()).isGreaterThan(0L);

            // DB 검증
            Optional<FileVariantJpaEntity> entity = fileVariantJpaRepository
                .findById(savedVariant.getIdValue());
            assertThat(entity).isPresent();
            assertThat(entity.get().getParentFileAssetId())
                .isEqualTo(savedVariant.getParentFileAssetId());
            assertThat(entity.get().getVariantType())
                .isEqualTo(savedVariant.getVariantType());
        }

        @Test
        @DisplayName("save_ShouldPublishDomainEvent - Domain Event 발행 확인")
        void save_ShouldPublishDomainEvent() {
            // Given
            FileVariant variant = FileVariant.create(
                FileAssetId.of(1L),
                VariantType.THUMBNAIL,
                StorageKey.of("tenant-1/org-2/thumbnail/image.jpg"),
                FileSize.of(51200L),
                new MimeType("image/webp")
            );

            // When
            FileVariant savedVariant = fileVariantCommandAdapter.save(variant);

            // Then - Event 발행 확인
            verify(eventPublisher).publishEvent(any(FileVariantCreatedEvent.class));

            // Event 초기화 확인
            assertThat(savedVariant.getDomainEvents()).isEmpty();
        }

        @Test
        @DisplayName("save_WithDuplicateParentAndType_ShouldThrowException - 중복 (parentFileAssetId, variantType) 조합으로 예외 발생")
        void save_WithDuplicateParentAndType_ShouldThrowException() {
            // Given - 첫 번째 Variant 저장
            FileVariant variant1 = FileVariant.create(
                FileAssetId.of(1L),
                VariantType.THUMBNAIL,
                StorageKey.of("tenant-1/org-2/thumbnail/image1.jpg"),
                FileSize.of(51200L),
                new MimeType("image/webp")
            );
            fileVariantCommandAdapter.save(variant1);
            entityManager.flush();

            // When & Then - 동일한 (parentFileAssetId, variantType) 조합으로 저장 시 예외 발생
            FileVariant variant2 = FileVariant.create(
                FileAssetId.of(1L),
                VariantType.THUMBNAIL, // 동일한 VariantType
                StorageKey.of("tenant-1/org-2/thumbnail/image2.jpg"),
                FileSize.of(51200L),
                new MimeType("image/webp")
            );

            // Unique Constraint 위반 예외 발생 (DataIntegrityViolationException)
            org.springframework.dao.DataIntegrityViolationException exception = null;
            try {
                fileVariantCommandAdapter.save(variant2);
                entityManager.flush();
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                exception = e;
            }

            assertThat(exception).isNotNull();
        }

        @Test
        @DisplayName("save_WithDifferentVariantTypes_ShouldSaveBoth - 다른 VariantType으로 여러 Variant 저장")
        void save_WithDifferentVariantTypes_ShouldSaveBoth() {
            // Given
            FileVariant thumbnail = FileVariant.create(
                FileAssetId.of(1L),
                VariantType.THUMBNAIL,
                StorageKey.of("tenant-1/org-2/thumbnail/image.jpg"),
                FileSize.of(51200L),
                new MimeType("image/webp")
            );

            FileVariant preview = FileVariant.create(
                FileAssetId.of(1L),
                VariantType.PREVIEW,
                StorageKey.of("tenant-1/org-2/preview/image.jpg"),
                FileSize.of(102400L),
                new MimeType("image/webp")
            );

            // When
            FileVariant savedThumbnail = fileVariantCommandAdapter.save(thumbnail);
            FileVariant savedPreview = fileVariantCommandAdapter.save(preview);

            // Then
            assertThat(savedThumbnail.getIdValue()).isNotNull();
            assertThat(savedPreview.getIdValue()).isNotNull();
            assertThat(savedThumbnail.getIdValue()).isNotEqualTo(savedPreview.getIdValue());

            // DB 검증
            assertThat(fileVariantJpaRepository.findAll()).hasSize(2);
        }
    }
}


