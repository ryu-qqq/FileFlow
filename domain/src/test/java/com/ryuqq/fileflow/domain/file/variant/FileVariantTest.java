package com.ryuqq.fileflow.domain.file.variant;

import com.ryuqq.fileflow.domain.file.asset.FileAssetId;
import com.ryuqq.fileflow.domain.upload.FileSize;
import com.ryuqq.fileflow.domain.upload.MimeType;
import com.ryuqq.fileflow.domain.upload.StorageKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * FileVariant 단위 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>create() - FileVariant 생성</li>
 *   <li>reconstitute() - DB 데이터로 재구성</li>
 *   <li>Domain Event 발행</li>
 *   <li>Getter 메서드</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@DisplayName("FileVariant 단위 테스트")
class FileVariantTest {

    @Nested
    @DisplayName("create 메서드 테스트")
    class CreateTests {

        @Test
        @DisplayName("create_WithValidInputs_ShouldCreateFileVariant - 정상 입력으로 FileVariant 생성")
        void create_WithValidInputs_ShouldCreateFileVariant() {
            // Given
            FileAssetId parentId = FileAssetId.of(1L);
            VariantType type = VariantType.THUMBNAIL;
            StorageKey key = StorageKey.of("tenant-1/org-2/thumbnail/image.jpg");
            FileSize size = FileSize.of(51200L);
            MimeType mimeType = new MimeType("image/webp");

            // When
            FileVariant variant = FileVariant.create(parentId, type, key, size, mimeType);

            // Then
            assertThat(variant).isNotNull();
            assertThat(variant.getId()).isNull(); // ID는 Persistence Layer에서 생성
            assertThat(variant.getParentFileAssetId()).isEqualTo(1L);
            assertThat(variant.getVariantType()).isEqualTo(type);
            assertThat(variant.getStorageKey()).isEqualTo(key);
            assertThat(variant.getFileSize()).isEqualTo(size);
            assertThat(variant.getMimeType()).isEqualTo(mimeType);
            assertThat(variant.getCreatedAt()).isNotNull();

            // Domain Event 검증
            assertThat(variant.getDomainEvents()).hasSize(1);
            assertThat(variant.getDomainEvents().get(0)).isInstanceOf(FileVariantCreatedEvent.class);

            FileVariantCreatedEvent event = (FileVariantCreatedEvent) variant.getDomainEvents().get(0);
            assertThat(event.fileVariantId()).isNull(); // 생성 시점에는 ID가 null
            assertThat(event.fileAssetId()).isEqualTo(parentId);
            assertThat(event.variantType()).isEqualTo("THUMBNAIL");
        }

        @Test
        @DisplayName("create_WithNullParentId_ShouldThrowException - null parentFileAssetId로 예외 발생")
        void create_WithNullParentId_ShouldThrowException() {
            // Given
            VariantType type = VariantType.THUMBNAIL;
            StorageKey key = StorageKey.of("tenant-1/org-2/thumbnail/image.jpg");
            FileSize size = FileSize.of(51200L);
            MimeType mimeType = new MimeType("image/webp");

            // When & Then
            assertThatThrownBy(() -> FileVariant.create(null, type, key, size, mimeType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Parent FileAsset ID는 필수입니다");
        }

        @Test
        @DisplayName("create_WithDifferentVariantTypes_ShouldCreateDifferentVariants - 다른 VariantType으로 생성")
        void create_WithDifferentVariantTypes_ShouldCreateDifferentVariants() {
            // Given
            FileAssetId parentId = FileAssetId.of(1L);
            StorageKey key = StorageKey.of("tenant-1/org-2/thumbnail/image.jpg");
            FileSize size = FileSize.of(51200L);
            MimeType mimeType = new MimeType("image/webp");

            // When
            FileVariant thumbnail = FileVariant.create(parentId, VariantType.THUMBNAIL, key, size, mimeType);
            FileVariant preview = FileVariant.create(parentId, VariantType.PREVIEW, key, size, mimeType);

            // Then
            assertThat(thumbnail.getVariantType()).isEqualTo(VariantType.THUMBNAIL);
            assertThat(preview.getVariantType()).isEqualTo(VariantType.PREVIEW);
        }
    }

    @Nested
    @DisplayName("reconstitute 메서드 테스트")
    class ReconstituteTests {

        @Test
        @DisplayName("reconstitute_WithValidInputs_ShouldCreateFileVariant - 정상 입력으로 재구성")
        void reconstitute_WithValidInputs_ShouldCreateFileVariant() {
            // Given
            FileVariantId id = FileVariantId.of(1L);
            Long parentFileAssetId = 100L;
            VariantType type = VariantType.THUMBNAIL;
            StorageKey key = StorageKey.of("tenant-1/org-2/thumbnail/image.jpg");
            FileSize size = FileSize.of(51200L);
            MimeType mimeType = new MimeType("image/webp");
            LocalDateTime createdAt = LocalDateTime.now();

            // When
            FileVariant variant = FileVariant.reconstitute(
                id, parentFileAssetId, type, key, size, mimeType, createdAt
            );

            // Then
            assertThat(variant).isNotNull();
            assertThat(variant.getId()).isEqualTo(id);
            assertThat(variant.getParentFileAssetId()).isEqualTo(parentFileAssetId);
            assertThat(variant.getVariantType()).isEqualTo(type);
            assertThat(variant.getStorageKey()).isEqualTo(key);
            assertThat(variant.getFileSize()).isEqualTo(size);
            assertThat(variant.getMimeType()).isEqualTo(mimeType);
            assertThat(variant.getCreatedAt()).isEqualTo(createdAt);

            // reconstitute는 Domain Event를 발행하지 않음
            assertThat(variant.getDomainEvents()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Domain Event 테스트")
    class DomainEventTests {

        @Test
        @DisplayName("getDomainEvents_AfterCreate_ShouldReturnEvent - create 후 이벤트 조회")
        void getDomainEvents_AfterCreate_ShouldReturnEvent() {
            // Given
            FileVariant variant = FileVariant.create(
                FileAssetId.of(1L),
                VariantType.THUMBNAIL,
                StorageKey.of("tenant-1/org-2/thumbnail/image.jpg"),
                FileSize.of(51200L),
                new MimeType("image/webp")
            );

            // When
            var events = variant.getDomainEvents();

            // Then
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isInstanceOf(FileVariantCreatedEvent.class);
        }

        @Test
        @DisplayName("clearDomainEvents_ShouldRemoveEvents - 이벤트 초기화")
        void clearDomainEvents_ShouldRemoveEvents() {
            // Given
            FileVariant variant = FileVariant.create(
                FileAssetId.of(1L),
                VariantType.THUMBNAIL,
                StorageKey.of("tenant-1/org-2/thumbnail/image.jpg"),
                FileSize.of(51200L),
                new MimeType("image/webp")
            );

            assertThat(variant.getDomainEvents()).hasSize(1);

            // When
            variant.clearDomainEvents();

            // Then
            assertThat(variant.getDomainEvents()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Getter 메서드 테스트")
    class GetterTests {

        @Test
        @DisplayName("getIdValue_WithNullId_ShouldReturnNull - ID가 null일 때 null 반환")
        void getIdValue_WithNullId_ShouldReturnNull() {
            // Given
            FileVariant variant = FileVariant.create(
                FileAssetId.of(1L),
                VariantType.THUMBNAIL,
                StorageKey.of("tenant-1/org-2/thumbnail/image.jpg"),
                FileSize.of(51200L),
                new MimeType("image/webp")
            );

            // When
            Long idValue = variant.getIdValue();

            // Then
            assertThat(idValue).isNull();
        }

        @Test
        @DisplayName("getIdValue_WithId_ShouldReturnValue - ID가 있을 때 값 반환")
        void getIdValue_WithId_ShouldReturnValue() {
            // Given
            FileVariantId id = FileVariantId.of(1L);
            FileVariant variant = FileVariant.reconstitute(
                id,
                100L,
                VariantType.THUMBNAIL,
                StorageKey.of("tenant-1/org-2/thumbnail/image.jpg"),
                FileSize.of(51200L),
                new MimeType("image/webp"),
                LocalDateTime.now()
            );

            // When
            Long idValue = variant.getIdValue();

            // Then
            assertThat(idValue).isEqualTo(1L);
        }
    }
}


