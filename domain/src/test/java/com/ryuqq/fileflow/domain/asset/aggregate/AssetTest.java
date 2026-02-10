package com.ryuqq.fileflow.domain.asset.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.domain.asset.exception.AssetErrorCode;
import com.ryuqq.fileflow.domain.asset.exception.AssetException;
import com.ryuqq.fileflow.domain.asset.id.AssetId;
import com.ryuqq.fileflow.domain.asset.vo.AssetOrigin;
import com.ryuqq.fileflow.domain.asset.vo.FileInfo;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.domain.common.vo.StorageInfo;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Asset 애그리게이트")
class AssetTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant DELETE_TIME = Instant.parse("2026-01-02T00:00:00Z");

    @Nested
    @DisplayName("forNew() - 신규 생성")
    class ForNew {

        @Test
        @DisplayName("모든 속성이 올바르게 설정된다")
        void shouldSetAllProperties() {
            // given
            AssetId id = AssetId.of("asset-001");
            StorageInfo storageInfo =
                    StorageInfo.of("test-bucket", "public/2026/02/test.jpg", AccessType.PUBLIC);
            FileInfo fileInfo = FileInfo.of("test.jpg", 1024L, "image/jpeg", "etag-123", "jpg");

            // when
            Asset asset =
                    Asset.forNew(
                            id,
                            storageInfo,
                            fileInfo,
                            AssetOrigin.SINGLE_UPLOAD,
                            "origin-001",
                            "product-image",
                            "commerce-service",
                            NOW);

            // then
            assertThat(asset.id()).isEqualTo(id);
            assertThat(asset.storageInfo()).isEqualTo(storageInfo);
            assertThat(asset.fileInfo()).isEqualTo(fileInfo);
            assertThat(asset.origin()).isEqualTo(AssetOrigin.SINGLE_UPLOAD);
            assertThat(asset.originId()).isEqualTo("origin-001");
            assertThat(asset.purpose()).isEqualTo("product-image");
            assertThat(asset.source()).isEqualTo("commerce-service");
            assertThat(asset.createdAt()).isEqualTo(NOW);
            assertThat(asset.updatedAt()).isEqualTo(NOW);
            assertThat(asset.deletedAt()).isNull();
            assertThat(asset.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("편의 메서드가 올바르게 StorageInfo, FileInfo를 위임한다")
        void shouldDelegateToStorageInfoAndFileInfo() {
            // given
            Asset asset = AssetFixture.anAsset();

            // then
            assertThat(asset.bucket()).isEqualTo("test-bucket");
            assertThat(asset.s3Key()).isEqualTo("public/2026/02/test.jpg");
            assertThat(asset.accessType()).isEqualTo(AccessType.PUBLIC);
            assertThat(asset.fileName()).isEqualTo("test.jpg");
            assertThat(asset.fileSize()).isEqualTo(1024L);
            assertThat(asset.contentType()).isEqualTo("image/jpeg");
            assertThat(asset.etag()).isEqualTo("etag-123");
            assertThat(asset.extension()).isEqualTo("jpg");
        }
    }

    @Nested
    @DisplayName("reconstitute() - DB에서 복원")
    class Reconstitute {

        @Test
        @DisplayName("모든 속성이 올바르게 복원된다")
        void shouldReconstituteWithAllProperties() {
            // when
            Asset asset =
                    Asset.reconstitute(
                            AssetId.of("asset-recon-001"),
                            StorageInfo.of(
                                    "test-bucket", "public/2026/02/test.jpg", AccessType.PUBLIC),
                            FileInfo.of("test.jpg", 1024L, "image/jpeg", "etag-123", "jpg"),
                            AssetOrigin.SINGLE_UPLOAD,
                            "origin-001",
                            "product-image",
                            "commerce-service",
                            NOW,
                            NOW,
                            null);

            // then
            assertThat(asset.idValue()).isEqualTo("asset-recon-001");
            assertThat(asset.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("삭제 상태로 복원할 수 있다")
        void shouldReconstituteDeletedAsset() {
            // when
            Asset asset = AssetFixture.aReconstitutedDeletedAsset();

            // then
            assertThat(asset.isDeleted()).isTrue();
            assertThat(asset.deletedAt()).isEqualTo(DELETE_TIME);
        }
    }

    @Nested
    @DisplayName("delete() - 소프트 삭제")
    class Delete {

        @Test
        @DisplayName("삭제 시각이 설정되고 isDeleted가 true가 된다")
        void shouldSetDeletedAtAndUpdatedAt() {
            // given
            Asset asset = AssetFixture.anAsset();

            // when
            asset.delete(DELETE_TIME);

            // then
            assertThat(asset.isDeleted()).isTrue();
            assertThat(asset.deletedAt()).isEqualTo(DELETE_TIME);
            assertThat(asset.updatedAt()).isEqualTo(DELETE_TIME);
        }

        @Test
        @DisplayName("이미 삭제된 Asset을 다시 삭제하면 AssetException이 발생한다")
        void shouldThrowWhenAlreadyDeleted() {
            // given
            Asset asset = AssetFixture.aDeletedAsset();

            // when & then
            assertThatThrownBy(() -> asset.delete(DELETE_TIME.plusSeconds(3600)))
                    .isInstanceOf(AssetException.class)
                    .satisfies(
                            ex -> {
                                AssetException assetEx = (AssetException) ex;
                                assertThat(assetEx.code())
                                        .isEqualTo(AssetErrorCode.ASSET_ALREADY_DELETED.getCode());
                                assertThat(assetEx.httpStatus()).isEqualTo(409);
                            });
        }
    }

    @Nested
    @DisplayName("isImage()")
    class IsImage {

        @Test
        @DisplayName("contentType이 image/로 시작하면 true를 반환한다")
        void shouldReturnTrueForImageContentType() {
            // given
            Asset asset = AssetFixture.anAsset();

            // then
            assertThat(asset.isImage()).isTrue();
        }

        @Test
        @DisplayName("contentType이 image/가 아니면 false를 반환한다")
        void shouldReturnFalseForNonImageContentType() {
            // given
            Asset asset = AssetFixture.aPdfAsset();

            // then
            assertThat(asset.isImage()).isFalse();
        }
    }

    @Nested
    @DisplayName("equals/hashCode - ID 기반 동등성")
    class EqualsHashCode {

        @Test
        @DisplayName("같은 ID면 동일하다")
        void shouldBeEqualWithSameId() {
            // given
            Asset asset1 = AssetFixture.anAssetWithId("same-id");
            Asset asset2 = AssetFixture.anAssetWithId("same-id");

            // then
            assertThat(asset1).isEqualTo(asset2);
            assertThat(asset1.hashCode()).isEqualTo(asset2.hashCode());
        }

        @Test
        @DisplayName("다른 ID면 다르다")
        void shouldNotBeEqualWithDifferentId() {
            // given
            Asset asset1 = AssetFixture.anAssetWithId("id-1");
            Asset asset2 = AssetFixture.anAssetWithId("id-2");

            // then
            assertThat(asset1).isNotEqualTo(asset2);
        }

        @Test
        @DisplayName("null과 비교하면 false이다")
        void shouldNotBeEqualToNull() {
            // given
            Asset asset = AssetFixture.anAsset();

            // then
            assertThat(asset).isNotEqualTo(null);
        }
    }
}
