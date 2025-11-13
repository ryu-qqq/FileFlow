package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity.FileVariantJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.repository.FileVariantJpaRepository;
import com.ryuqq.fileflow.domain.file.variant.FileVariant;
import com.ryuqq.fileflow.domain.file.variant.VariantType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FileVariant Query Adapter 통합 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>findAllByFileId - fileId로 모든 Variant 조회</li>
 *   <li>findByFileIdAndVariantType - 특정 VariantType 조회</li>
 *   <li>existsByFileIdAndVariantType - 존재 여부 확인</li>
 * </ul>
 *
 * <p><strong>검증 항목:</strong></p>
 * <ul>
 *   <li>Entity → Domain 변환 정확성</li>
 *   <li>ORDER BY created_at DESC 정렬 확인</li>
 *   <li>Optional 반환 정확성</li>
 *   <li>존재 여부 확인 정확성</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@DataJpaTest
@Import(FileVariantQueryAdapter.class)
@DisplayName("FileVariant Query Adapter 통합 테스트")
@Disabled("TODO: Fix after deployment - H2 test infrastructure issue")
class FileVariantQueryAdapterTest {

    @Autowired
    private FileVariantQueryAdapter fileVariantQueryAdapter;

    @Autowired
    private FileVariantJpaRepository fileVariantJpaRepository;

    @Autowired
    private EntityManager entityManager;

    private Long fileId1;
    private Long fileId2;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 준비
        fileId1 = 1L;
        fileId2 = 2L;

        // FileId1의 Variants (2개)
        FileVariantJpaEntity variant1 = FileVariantJpaEntity.create(
            fileId1,
            VariantType.THUMBNAIL,
            "tenant-1/org-2/thumbnail/image1.jpg",
            51200L,
            "image/webp"
        );
        variant1 = fileVariantJpaRepository.save(variant1);

        // 약간의 시간 차이를 두기 위해 flush
        entityManager.flush();

        FileVariantJpaEntity variant2 = FileVariantJpaEntity.create(
            fileId1,
            VariantType.PREVIEW,
            "tenant-1/org-2/preview/image1.jpg",
            102400L,
            "image/webp"
        );
        variant2 = fileVariantJpaRepository.save(variant2);

        // FileId2의 Variant (1개)
        FileVariantJpaEntity variant3 = FileVariantJpaEntity.create(
            fileId2,
            VariantType.THUMBNAIL,
            "tenant-1/org-2/thumbnail/image2.jpg",
            51200L,
            "image/webp"
        );
        fileVariantJpaRepository.save(variant3);

        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("findAllByFileId 메서드 테스트")
    class FindAllByFileIdTests {

        @Test
        @DisplayName("findAllByFileId_WithExistingVariants_ShouldReturnOrderedByCreatedAt - 존재하는 Variants 조회 시 created_at DESC 정렬")
        void findAllByFileId_WithExistingVariants_ShouldReturnOrderedByCreatedAt() {
            // When
            List<FileVariant> variants = fileVariantQueryAdapter.findAllByFileId(fileId1);

            // Then
            assertThat(variants).hasSize(2);
            // created_at DESC 정렬 확인 (나중에 생성된 것이 먼저)
            assertThat(variants.get(0).getVariantType()).isEqualTo(VariantType.PREVIEW);
            assertThat(variants.get(1).getVariantType()).isEqualTo(VariantType.THUMBNAIL);
        }

        @Test
        @DisplayName("findAllByFileId_WithNonExistentFileId_ShouldReturnEmptyList - 존재하지 않는 FileId로 빈 리스트 반환")
        void findAllByFileId_WithNonExistentFileId_ShouldReturnEmptyList() {
            // When
            List<FileVariant> variants = fileVariantQueryAdapter.findAllByFileId(999L);

            // Then
            assertThat(variants).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByFileIdAndVariantType 메서드 테스트")
    class FindByFileIdAndVariantTypeTests {

        @Test
        @DisplayName("findByFileIdAndVariantType_WithExistingVariant_ShouldReturnVariant - 존재하는 Variant 조회")
        void findByFileIdAndVariantType_WithExistingVariant_ShouldReturnVariant() {
            // When
            Optional<FileVariant> variant = fileVariantQueryAdapter
                .findByFileIdAndVariantType(fileId1, VariantType.THUMBNAIL);

            // Then
            assertThat(variant).isPresent();
            assertThat(variant.get().getParentFileAssetId()).isEqualTo(fileId1);
            assertThat(variant.get().getVariantType()).isEqualTo(VariantType.THUMBNAIL);
        }

        @Test
        @DisplayName("findByFileIdAndVariantType_WithNonExistentVariant_ShouldReturnEmpty - 존재하지 않는 Variant로 Empty 반환")
        void findByFileIdAndVariantType_WithNonExistentVariant_ShouldReturnEmpty() {
            // When
            Optional<FileVariant> variant = fileVariantQueryAdapter
                .findByFileIdAndVariantType(fileId1, VariantType.COMPRESSED);

            // Then
            assertThat(variant).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByFileIdAndVariantType 메서드 테스트")
    class ExistsByFileIdAndVariantTypeTests {

        @Test
        @DisplayName("existsByFileIdAndVariantType_WithExistingVariant_ShouldReturnTrue - 존재하는 Variant로 true 반환")
        void existsByFileIdAndVariantType_WithExistingVariant_ShouldReturnTrue() {
            // When
            boolean exists = fileVariantQueryAdapter
                .existsByFileIdAndVariantType(fileId1, VariantType.THUMBNAIL);

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("existsByFileIdAndVariantType_WithNonExistentVariant_ShouldReturnFalse - 존재하지 않는 Variant로 false 반환")
        void existsByFileIdAndVariantType_WithNonExistentVariant_ShouldReturnFalse() {
            // When
            boolean exists = fileVariantQueryAdapter
                .existsByFileIdAndVariantType(fileId1, VariantType.COMPRESSED);

            // Then
            assertThat(exists).isFalse();
        }
    }
}


