package com.ryuqq.fileflow.adapter.out.persistence.asset.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.FileAssetJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.asset.mapper.FileAssetJpaEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.asset.repository.FileAssetQueryDslRepository;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetCriteria;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.iam.vo.UserId;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.time.Clock;
import java.time.Instant;
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

@DisplayName("FileAssetQueryAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class FileAssetQueryAdapterTest {

    @Mock private FileAssetQueryDslRepository fileAssetQueryDslRepository;

    @Mock private FileAssetJpaEntityMapper fileAssetJpaEntityMapper;

    private FileAssetQueryAdapter adapter;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        adapter = new FileAssetQueryAdapter(fileAssetQueryDslRepository, fileAssetJpaEntityMapper);
        fixedClock = Clock.fixed(Instant.parse("2025-11-26T10:00:00Z"), ZoneId.of("UTC"));
    }

    @Nested
    @DisplayName("findById 테스트")
    class FindByIdTest {

        @Test
        @DisplayName("ID, organizationId, tenantId로 FileAsset을 조회할 수 있다")
        void findById_WithValidParams_ShouldReturnFileAsset() {
            // given
            String id = UUID.randomUUID().toString();
            String organizationId = "01912345-6789-7abc-def0-123456789100";
            String tenantId = "01912345-6789-7abc-def0-123456789001";
            FileAssetJpaEntity entity = createEntity(id, organizationId, tenantId);
            FileAsset domain = createFileAsset(id, organizationId, tenantId);

            when(fileAssetQueryDslRepository.findById(id, organizationId, tenantId))
                    .thenReturn(Optional.of(entity));
            when(fileAssetJpaEntityMapper.toDomain(entity)).thenReturn(domain);

            // when
            Optional<FileAsset> result =
                    adapter.findById(FileAssetId.of(id), organizationId, tenantId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId().getValue()).isEqualTo(id);
        }

        @Test
        @DisplayName("조회 결과가 없으면 empty Optional을 반환한다")
        void findById_WhenNotFound_ShouldReturnEmpty() {
            // given
            String id = UUID.randomUUID().toString();
            String organizationId = "01912345-6789-7abc-def0-123456789100";
            String tenantId = "01912345-6789-7abc-def0-123456789001";
            when(fileAssetQueryDslRepository.findById(id, organizationId, tenantId)).thenReturn(Optional.empty());

            // when
            Optional<FileAsset> result = adapter.findById(FileAssetId.of(id), organizationId, tenantId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByCriteria 테스트")
    class FindByCriteriaTest {

        @Test
        @DisplayName("조건에 맞는 FileAsset 목록을 조회할 수 있다")
        void findByCriteria_WithValidCriteria_ShouldReturnFileAssets() {
            // given
            String organizationId = "01912345-6789-7abc-def0-123456789100";
            String tenantId = "01912345-6789-7abc-def0-123456789001";
            FileAssetStatus status = FileAssetStatus.COMPLETED;
            FileCategory category = FileCategory.IMAGE;

            FileAssetCriteria criteria =
                    FileAssetCriteria.of(organizationId, tenantId, status, category, 0, 10);

            String id1 = UUID.randomUUID().toString();
            String id2 = UUID.randomUUID().toString();
            FileAssetJpaEntity entity1 = createEntity(id1, organizationId, tenantId);
            FileAssetJpaEntity entity2 = createEntity(id2, organizationId, tenantId);
            FileAsset domain1 = createFileAsset(id1, organizationId, tenantId);
            FileAsset domain2 = createFileAsset(id2, organizationId, tenantId);

            when(fileAssetQueryDslRepository.findByCriteria(
                            organizationId, tenantId, status, category, 0, 10))
                    .thenReturn(List.of(entity1, entity2));
            when(fileAssetJpaEntityMapper.toDomain(entity1)).thenReturn(domain1);
            when(fileAssetJpaEntityMapper.toDomain(entity2)).thenReturn(domain2);

            // when
            List<FileAsset> result = adapter.findByCriteria(criteria);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId().getValue()).isEqualTo(id1);
            assertThat(result.get(1).getId().getValue()).isEqualTo(id2);
        }

        @Test
        @DisplayName("조건에 맞는 FileAsset이 없으면 빈 목록을 반환한다")
        void findByCriteria_WhenNoMatch_ShouldReturnEmptyList() {
            // given
            String organizationId = "01912345-6789-7abc-def0-123456789100";
            String tenantId = "01912345-6789-7abc-def0-123456789001";
            FileAssetCriteria criteria = FileAssetCriteria.of(organizationId, tenantId, null, null, 0, 10);

            when(fileAssetQueryDslRepository.findByCriteria(organizationId, tenantId, null, null, 0, 10))
                    .thenReturn(List.of());

            // when
            List<FileAsset> result = adapter.findByCriteria(criteria);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("countByCriteria 테스트")
    class CountByCriteriaTest {

        @Test
        @DisplayName("조건에 맞는 FileAsset 개수를 반환한다")
        void countByCriteria_WithValidCriteria_ShouldReturnCount() {
            // given
            String organizationId = "01912345-6789-7abc-def0-123456789100";
            String tenantId = "01912345-6789-7abc-def0-123456789001";
            FileAssetCriteria criteria =
                    FileAssetCriteria.of(
                            organizationId, tenantId, FileAssetStatus.COMPLETED, FileCategory.IMAGE, 0, 10);

            when(fileAssetQueryDslRepository.countByCriteria(
                            organizationId, tenantId, FileAssetStatus.COMPLETED, FileCategory.IMAGE))
                    .thenReturn(5L);

            // when
            long result = adapter.countByCriteria(criteria);

            // then
            assertThat(result).isEqualTo(5L);
        }

        @Test
        @DisplayName("조건에 맞는 FileAsset이 없으면 0을 반환한다")
        void countByCriteria_WhenNoMatch_ShouldReturnZero() {
            // given
            String organizationId = "01912345-6789-7abc-def0-123456789100";
            String tenantId = "01912345-6789-7abc-def0-123456789001";
            FileAssetCriteria criteria = FileAssetCriteria.of(organizationId, tenantId, null, null, 0, 10);

            when(fileAssetQueryDslRepository.countByCriteria(organizationId, tenantId, null, null)).thenReturn(0L);

            // when
            long result = adapter.countByCriteria(criteria);

            // then
            assertThat(result).isEqualTo(0L);
        }
    }

    // ==================== Helper Methods ====================

    private FileAsset createFileAsset(String id, String organizationId, String tenantId) {
        return FileAsset.reconstitute(
                FileAssetId.of(id),
                UploadSessionId.of(UUID.fromString("11111111-1111-1111-1111-111111111111")),
                FileName.of("test-file.jpg"),
                FileSize.of(1024 * 1024L),
                ContentType.of("image/jpeg"),
                FileCategory.IMAGE,
                null, // ImageDimension
                S3Bucket.of("test-bucket"),
                S3Key.of("assets/test-file.jpg"),
                ETag.of("\"etag-123\""),
                UserId.of("01912345-6789-7abc-def0-123456789200"),
                OrganizationId.of(organizationId),
                TenantId.of(tenantId),
                FileAssetStatus.COMPLETED,
                Instant.now(fixedClock),
                null,
                null);
    }

    private FileAssetJpaEntity createEntity(String id, String organizationId, String tenantId) {
        Instant now = Instant.now();
        return FileAssetJpaEntity.of(
                id,
                "11111111-1111-1111-1111-111111111111",
                "test-file.jpg",
                1024 * 1024L,
                "image/jpeg",
                FileCategory.IMAGE,
                null, // imageWidth
                null, // imageHeight
                "test-bucket",
                "assets/test-file.jpg",
                "\"etag-123\"",
                "01912345-6789-7abc-def0-123456789200",
                organizationId,
                tenantId,
                FileAssetStatus.COMPLETED,
                null,
                null,
                now,
                now);
    }
}
