package com.ryuqq.fileflow.adapter.out.persistence.asset.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.FileAssetJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.asset.mapper.FileAssetJpaEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.asset.repository.FileAssetJpaRepository;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
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

@DisplayName("FileAssetCommandAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class FileAssetCommandAdapterTest {

    @Mock private FileAssetJpaRepository fileAssetJpaRepository;

    @Mock private FileAssetJpaEntityMapper fileAssetJpaEntityMapper;

    private FileAssetCommandAdapter adapter;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        adapter = new FileAssetCommandAdapter(fileAssetJpaRepository, fileAssetJpaEntityMapper);
        fixedClock = Clock.fixed(Instant.parse("2025-11-26T10:00:00Z"), ZoneId.of("UTC"));
    }

    @Nested
    @DisplayName("persist 테스트")
    class PersistTest {

        @Test
        @DisplayName("FileAsset을 영속화하고 ID를 반환한다")
        void persist_WithValidFileAsset_ShouldReturnFileAssetId() {
            // given
            String assetId = UUID.randomUUID().toString();
            FileAsset fileAsset = createFileAsset(assetId);
            FileAssetJpaEntity entity = createEntity(assetId);

            when(fileAssetJpaEntityMapper.toEntity(fileAsset)).thenReturn(entity);
            when(fileAssetJpaRepository.save(entity)).thenReturn(entity);

            // when
            FileAssetId result = adapter.persist(fileAsset);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getValue()).isEqualTo(assetId);
        }

        @Test
        @DisplayName("Mapper를 호출하여 Entity로 변환한다")
        void persist_ShouldCallMapperToEntity() {
            // given
            String assetId = UUID.randomUUID().toString();
            FileAsset fileAsset = createFileAsset(assetId);
            FileAssetJpaEntity entity = createEntity(assetId);

            when(fileAssetJpaEntityMapper.toEntity(fileAsset)).thenReturn(entity);
            when(fileAssetJpaRepository.save(entity)).thenReturn(entity);

            // when
            adapter.persist(fileAsset);

            // then
            verify(fileAssetJpaEntityMapper).toEntity(fileAsset);
        }

        @Test
        @DisplayName("Repository를 호출하여 Entity를 저장한다")
        void persist_ShouldCallRepositorySave() {
            // given
            String assetId = UUID.randomUUID().toString();
            FileAsset fileAsset = createFileAsset(assetId);
            FileAssetJpaEntity entity = createEntity(assetId);

            when(fileAssetJpaEntityMapper.toEntity(fileAsset)).thenReturn(entity);
            when(fileAssetJpaRepository.save(entity)).thenReturn(entity);

            // when
            adapter.persist(fileAsset);

            // then
            verify(fileAssetJpaRepository).save(entity);
        }
    }

    // ==================== Helper Methods ====================

    private FileAsset createFileAsset(String id) {
        return FileAsset.reconstitute(
                FileAssetId.of(id),
                UploadSessionId.of(UUID.fromString("11111111-1111-1111-1111-111111111111")),
                FileName.of("test-file.jpg"),
                FileSize.of(1024 * 1024L),
                ContentType.of("image/jpeg"),
                FileCategory.IMAGE,
                S3Bucket.of("test-bucket"),
                S3Key.of("assets/test-file.jpg"),
                ETag.of("\"etag-123\""),
                1L,
                100L,
                1L,
                FileAssetStatus.COMPLETED,
                LocalDateTime.now(fixedClock),
                null,
                null,
                fixedClock);
    }

    private FileAssetJpaEntity createEntity(String id) {
        LocalDateTime now = LocalDateTime.now();
        return FileAssetJpaEntity.of(
                id,
                "11111111-1111-1111-1111-111111111111",
                "test-file.jpg",
                1024 * 1024L,
                "image/jpeg",
                FileCategory.IMAGE,
                "test-bucket",
                "assets/test-file.jpg",
                "\"etag-123\"",
                1L,
                100L,
                1L,
                FileAssetStatus.COMPLETED,
                null,
                null,
                now,
                now);
    }
}
