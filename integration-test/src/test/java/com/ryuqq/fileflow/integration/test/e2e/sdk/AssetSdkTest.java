package com.ryuqq.fileflow.integration.test.e2e.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.adapter.out.persistence.asset.AssetJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.asset.AssetMetadataJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.AssetJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.AssetMetadataJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.asset.repository.AssetJpaRepository;
import com.ryuqq.fileflow.adapter.out.persistence.asset.repository.AssetMetadataJpaRepository;
import com.ryuqq.fileflow.domain.asset.vo.AssetOrigin;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.sdk.exception.FileFlowForbiddenException;
import com.ryuqq.fileflow.sdk.exception.FileFlowNotFoundException;
import com.ryuqq.fileflow.sdk.model.asset.AssetMetadataResponse;
import com.ryuqq.fileflow.sdk.model.asset.AssetResponse;
import com.ryuqq.fileflow.sdk.model.common.ApiResponse;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * SDK를 통한 Asset 통합 테스트.
 *
 * <p>FileFlowClient SDK를 사용하여 Asset 조회, 메타데이터 조회, 삭제를 검증합니다.
 */
@DisplayName("SDK - Asset 통합 테스트")
class AssetSdkTest extends SdkTestBase {

    private static final Instant DEFAULT_NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Autowired private AssetJpaRepository assetJpaRepository;

    @Autowired private AssetMetadataJpaRepository assetMetadataJpaRepository;

    @BeforeEach
    void setUp() {
        assetMetadataJpaRepository.deleteAllInBatch();
        assetJpaRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("Asset 조회")
    class GetTest {

        @Test
        @DisplayName("존재하는 Asset을 조회하면 상세 정보가 반환된다")
        void shouldGetAsset() {
            // given
            AssetJpaEntity entity = assetJpaRepository.save(AssetJpaEntityFixture.anAssetEntity());

            // when
            ApiResponse<AssetResponse> response = client.asset().get(entity.getId());

            // then
            AssetResponse asset = response.data();
            assertThat(asset.assetId()).isEqualTo(entity.getId());
            assertThat(asset.s3Key()).isEqualTo(entity.getS3Key());
            assertThat(asset.bucket()).isEqualTo(entity.getBucket());
            assertThat(asset.accessType()).isEqualTo(entity.getAccessType().name());
            assertThat(asset.fileName()).isEqualTo(entity.getFileName());
            assertThat(asset.fileSize()).isEqualTo(entity.getFileSize());
            assertThat(asset.contentType()).isEqualTo(entity.getContentType());
            assertThat(asset.etag()).isEqualTo(entity.getEtag());
            assertThat(asset.extension()).isEqualTo(entity.getExtension());
            assertThat(asset.origin()).isEqualTo(entity.getOrigin().name());
            assertThat(asset.originId()).isEqualTo(entity.getOriginId());
            assertThat(asset.purpose()).isEqualTo(entity.getPurpose());
            assertThat(asset.source()).isEqualTo(entity.getSource());
            assertThat(asset.createdAt()).isNotBlank();
        }

        @Test
        @DisplayName("존재하지 않는 Asset을 조회하면 FileFlowNotFoundException이 발생한다")
        void shouldThrowNotFoundWhenAssetNotExists() {
            assertThatThrownBy(() -> client.asset().get("non-existent-id"))
                    .isInstanceOf(FileFlowNotFoundException.class)
                    .satisfies(
                            ex -> {
                                FileFlowNotFoundException e = (FileFlowNotFoundException) ex;
                                assertThat(e.getErrorCode()).isEqualTo("ASSET-001");
                            });
        }

        @Test
        @DisplayName("삭제된 Asset을 조회하면 FileFlowNotFoundException이 발생한다")
        void shouldThrowNotFoundWhenAssetDeleted() {
            // given
            assetJpaRepository.save(AssetJpaEntityFixture.aDeletedAssetEntity());

            // when & then
            assertThatThrownBy(() -> client.asset().get("asset-del-001"))
                    .isInstanceOf(FileFlowNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Asset 메타데이터 조회")
    class GetMetadataTest {

        @Test
        @DisplayName("메타데이터가 존재하면 메타데이터 정보가 반환된다")
        void shouldGetMetadata() {
            // given
            AssetJpaEntity asset = assetJpaRepository.save(AssetJpaEntityFixture.anAssetEntity());
            AssetMetadataJpaEntity metadata =
                    assetMetadataJpaRepository.save(
                            AssetMetadataJpaEntityFixture.aMetadataEntityWithAssetId(
                                    asset.getId()));

            // when
            ApiResponse<AssetMetadataResponse> response = client.asset().getMetadata(asset.getId());

            // then
            AssetMetadataResponse meta = response.data();
            assertThat(meta.metadataId()).isEqualTo(metadata.getId());
            assertThat(meta.assetId()).isEqualTo(asset.getId());
            assertThat(meta.width()).isEqualTo(metadata.getWidth());
            assertThat(meta.height()).isEqualTo(metadata.getHeight());
            assertThat(meta.createdAt()).isNotBlank();
        }

        @Test
        @DisplayName("메타데이터가 없으면 FileFlowNotFoundException이 발생한다")
        void shouldThrowNotFoundWhenMetadataNotExists() {
            // given
            AssetJpaEntity asset = assetJpaRepository.save(AssetJpaEntityFixture.anAssetEntity());

            // when & then
            assertThatThrownBy(() -> client.asset().getMetadata(asset.getId()))
                    .isInstanceOf(FileFlowNotFoundException.class)
                    .satisfies(
                            ex -> {
                                FileFlowNotFoundException e = (FileFlowNotFoundException) ex;
                                assertThat(e.getErrorCode()).isEqualTo("ASSET-003");
                            });
        }
    }

    @Nested
    @DisplayName("Asset 삭제")
    class DeleteTest {

        @Test
        @DisplayName("올바른 source로 삭제하면 정상 처리된다")
        void shouldDeleteAsset() {
            // given
            AssetJpaEntity entity =
                    assetJpaRepository.save(createAssetWithSource("sdk-del-001", "commerce-api"));

            // when - 예외 없이 정상 삭제
            client.asset().delete(entity.getId(), "commerce-api");

            // then - DB 검증: deletedAt 설정
            AssetJpaEntity updated = assetJpaRepository.findById(entity.getId()).orElseThrow();
            assertThat(updated.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("존재하지 않는 Asset을 삭제하면 FileFlowNotFoundException이 발생한다")
        void shouldThrowNotFoundWhenDeletingNonExistent() {
            assertThatThrownBy(() -> client.asset().delete("non-existent", "commerce-api"))
                    .isInstanceOf(FileFlowNotFoundException.class);
        }

        @Test
        @DisplayName("권한 없는 source로 삭제하면 FileFlowForbiddenException이 발생한다")
        void shouldThrowForbiddenWhenSourceMismatch() {
            // given
            AssetJpaEntity entity =
                    assetJpaRepository.save(createAssetWithSource("sdk-auth-001", "commerce-api"));

            // when & then
            assertThatThrownBy(() -> client.asset().delete(entity.getId(), "other-service"))
                    .isInstanceOf(FileFlowForbiddenException.class)
                    .satisfies(
                            ex -> {
                                FileFlowForbiddenException e = (FileFlowForbiddenException) ex;
                                assertThat(e.getErrorCode()).isEqualTo("ASSET-004");
                            });
        }
    }

    @Nested
    @DisplayName("전체 플로우")
    class FullFlowTest {

        @Test
        @DisplayName("Asset 조회 -> 메타데이터 조회 플로우")
        void shouldGetAssetThenGetMetadata() {
            // given
            AssetJpaEntity asset = assetJpaRepository.save(AssetJpaEntityFixture.anAssetEntity());
            AssetMetadataJpaEntity metadata =
                    assetMetadataJpaRepository.save(
                            AssetMetadataJpaEntityFixture.aMetadataEntityWithAssetId(
                                    asset.getId()));

            // Step 1: Asset 조회
            ApiResponse<AssetResponse> assetResponse = client.asset().get(asset.getId());
            assertThat(assetResponse.data().assetId()).isEqualTo(asset.getId());

            // Step 2: 메타데이터 조회
            ApiResponse<AssetMetadataResponse> metaResponse =
                    client.asset().getMetadata(asset.getId());
            assertThat(metaResponse.data().assetId()).isEqualTo(asset.getId());
            assertThat(metaResponse.data().width()).isEqualTo(metadata.getWidth());
        }

        @Test
        @DisplayName("Asset 조회 -> 삭제 -> 재조회 시 FileFlowNotFoundException 플로우")
        void shouldGetThenDeleteThenGetThrowsNotFound() {
            // given
            AssetJpaEntity asset =
                    assetJpaRepository.save(createAssetWithSource("sdk-flow-001", "commerce-api"));

            // Step 1: 조회 - 성공
            assertThat(client.asset().get(asset.getId()).data().assetId()).isEqualTo(asset.getId());

            // Step 2: 삭제
            client.asset().delete(asset.getId(), "commerce-api");

            // Step 3: 재조회 - 404
            assertThatThrownBy(() -> client.asset().get(asset.getId()))
                    .isInstanceOf(FileFlowNotFoundException.class);
        }
    }

    // ========================================
    // Helper 메서드
    // ========================================

    private AssetJpaEntity createAssetWithSource(String id, String source) {
        return AssetJpaEntity.create(
                id,
                "test-bucket",
                "public/2026/02/" + id + ".jpg",
                AccessType.PUBLIC,
                "test.jpg",
                1024L,
                "image/jpeg",
                "etag-" + id,
                "jpg",
                AssetOrigin.SINGLE_UPLOAD,
                "origin-001",
                "product-image",
                source,
                DEFAULT_NOW,
                DEFAULT_NOW,
                null);
    }
}
