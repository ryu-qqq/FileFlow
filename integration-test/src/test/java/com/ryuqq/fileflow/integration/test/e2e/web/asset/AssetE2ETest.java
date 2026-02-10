package com.ryuqq.fileflow.integration.test.e2e.web.asset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.ryuqq.fileflow.adapter.out.persistence.asset.AssetJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.asset.AssetMetadataJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.AssetJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.AssetMetadataJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.asset.repository.AssetJpaRepository;
import com.ryuqq.fileflow.adapter.out.persistence.asset.repository.AssetMetadataJpaRepository;
import com.ryuqq.fileflow.domain.asset.vo.AssetOrigin;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.integration.test.common.base.E2ETestBase;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

/**
 * Asset E2E 테스트.
 *
 * <p>실제 TestContainers(MySQL, Redis, LocalStack) 환경에서 Asset 조회, 메타데이터 조회, 논리 삭제 API를 검증합니다.
 */
@DisplayName("Asset E2E 테스트")
class AssetE2ETest extends E2ETestBase {

    private static final String BASE_PATH = "/api/v1/assets";
    private static final Instant DEFAULT_NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant DELETE_TIME = Instant.parse("2026-01-02T00:00:00Z");

    @Autowired private AssetJpaRepository assetJpaRepository;

    @Autowired private AssetMetadataJpaRepository assetMetadataJpaRepository;

    @BeforeEach
    void setUp() {
        assetMetadataJpaRepository.deleteAllInBatch();
        assetJpaRepository.deleteAllInBatch();
    }

    // ========================================
    // Q5: GET /api/v1/assets/{assetId} - Asset 상세 조회
    // ========================================
    @Nested
    @DisplayName("GET /api/v1/assets/{assetId} - Asset 상세 조회")
    class GetAssetTest {

        @Test
        @DisplayName("Q5-S01. 존재하는 Asset을 조회하면 200과 상세 정보를 반환한다")
        void shouldReturnAssetWhenExists() {
            // given
            AssetJpaEntity entity = assetJpaRepository.save(AssetJpaEntityFixture.anAssetEntity());

            // when & then
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{assetId}", entity.getId())
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.assetId", equalTo(entity.getId()))
                    .body("data.s3Key", equalTo(entity.getS3Key()))
                    .body("data.bucket", equalTo(entity.getBucket()))
                    .body("data.accessType", equalTo(entity.getAccessType().name()))
                    .body("data.fileName", equalTo(entity.getFileName()))
                    .body("data.fileSize", equalTo((int) entity.getFileSize()))
                    .body("data.contentType", equalTo(entity.getContentType()))
                    .body("data.etag", equalTo(entity.getEtag()))
                    .body("data.extension", equalTo(entity.getExtension()))
                    .body("data.origin", equalTo(entity.getOrigin().name()))
                    .body("data.originId", equalTo(entity.getOriginId()))
                    .body("data.purpose", equalTo(entity.getPurpose()))
                    .body("data.source", equalTo(entity.getSource()))
                    .body("data.createdAt", notNullValue());
        }

        @Test
        @DisplayName("Q5-S02. 존재하지 않는 Asset을 조회하면 404를 반환한다")
        void shouldReturn404WhenAssetNotFound() {
            // when & then
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{assetId}", "non-existent-id")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("code", equalTo("ASSET-001"));
        }

        @Test
        @DisplayName("Q5-S03. 삭제된 Asset을 조회하면 404를 반환한다 (deleted_at IS NULL 조건)")
        void shouldReturn404WhenAssetIsDeleted() {
            // given
            assetJpaRepository.save(AssetJpaEntityFixture.aDeletedAssetEntity());

            // when & then
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{assetId}", "asset-del-001")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("code", equalTo("ASSET-001"));
        }

        @Test
        @DisplayName("Q5-S04. origin이 SINGLE_UPLOAD인 Asset을 조회하면 origin 정보가 포함된다")
        void shouldReturnAssetWithSingleUploadOrigin() {
            // given - anAssetEntity()의 기본 origin은 SINGLE_UPLOAD
            AssetJpaEntity entity = assetJpaRepository.save(AssetJpaEntityFixture.anAssetEntity());

            // when & then
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{assetId}", entity.getId())
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.origin", equalTo("SINGLE_UPLOAD"))
                    .body("data.originId", notNullValue());
        }

        @Test
        @DisplayName("Q5-S05. origin이 MULTIPART_UPLOAD인 Asset을 조회하면 해당 origin이 반환된다")
        void shouldReturnAssetWithMultipartUploadOrigin() {
            // given
            AssetJpaEntity entity =
                    assetJpaRepository.save(
                            createAssetWithOrigin(
                                    "asset-mp-001",
                                    AssetOrigin.MULTIPART_UPLOAD,
                                    "mp-session-001"));

            // when & then
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{assetId}", entity.getId())
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.origin", equalTo("MULTIPART_UPLOAD"));
        }

        @Test
        @DisplayName("Q5-S06. origin이 EXTERNAL_DOWNLOAD인 Asset을 조회하면 해당 origin이 반환된다")
        void shouldReturnAssetWithExternalDownloadOrigin() {
            // given
            AssetJpaEntity entity =
                    assetJpaRepository.save(
                            createAssetWithOrigin(
                                    "asset-dl-001", AssetOrigin.EXTERNAL_DOWNLOAD, "dl-task-001"));

            // when & then
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{assetId}", entity.getId())
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.origin", equalTo("EXTERNAL_DOWNLOAD"));
        }
    }

    // ========================================
    // Q6: GET /api/v1/assets/{assetId}/metadata - Asset 메타데이터 조회
    // ========================================
    @Nested
    @DisplayName("GET /api/v1/assets/{assetId}/metadata - Asset 메타데이터 조회")
    class GetAssetMetadataTest {

        @Test
        @DisplayName("Q6-S01. 메타데이터가 존재하는 Asset의 메타데이터를 조회하면 200을 반환한다")
        void shouldReturnMetadataWhenExists() {
            // given
            AssetJpaEntity asset = assetJpaRepository.save(AssetJpaEntityFixture.anAssetEntity());
            AssetMetadataJpaEntity metadata =
                    assetMetadataJpaRepository.save(
                            AssetMetadataJpaEntityFixture.aMetadataEntityWithAssetId(
                                    asset.getId()));

            // when & then
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{assetId}/metadata", asset.getId())
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.metadataId", equalTo(metadata.getId()))
                    .body("data.assetId", equalTo(asset.getId()))
                    .body("data.width", equalTo(metadata.getWidth()))
                    .body("data.height", equalTo(metadata.getHeight()))
                    .body("data.createdAt", notNullValue());
        }

        @Test
        @DisplayName("Q6-S02. 메타데이터가 없는 Asset의 메타데이터를 조회하면 404를 반환한다")
        void shouldReturn404WhenMetadataNotFound() {
            // given - Asset만 저장, 메타데이터 없음
            AssetJpaEntity asset = assetJpaRepository.save(AssetJpaEntityFixture.anAssetEntity());

            // when & then
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{assetId}/metadata", asset.getId())
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("code", equalTo("ASSET-003"));
        }

        @Test
        @DisplayName("Q6-S03. 존재하지 않는 Asset의 메타데이터를 조회하면 404를 반환한다")
        void shouldReturn404WhenAssetNotFoundForMetadata() {
            // when & then
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{assetId}/metadata", "non-existent-id")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }
    }

    // ========================================
    // C10: DELETE /api/v1/assets/{assetId}?source={source} - Asset 논리 삭제
    // ========================================
    @Nested
    @DisplayName("DELETE /api/v1/assets/{assetId}?source={source} - Asset 논리 삭제")
    class DeleteAssetTest {

        @Test
        @DisplayName("C10-S01. 올바른 source로 삭제하면 204를 반환하고 deletedAt이 설정된다")
        void shouldDeleteAssetWithCorrectSource() {
            // given
            AssetJpaEntity entity =
                    assetJpaRepository.save(
                            createAssetWithSource("asset-del-test-001", "commerce-api"));

            // when
            givenServiceAuth()
                    .queryParam("source", "commerce-api")
                    .when()
                    .delete(BASE_PATH + "/{assetId}", entity.getId())
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            // then - DB 검증: deletedAt이 설정되어야 함
            AssetJpaEntity updated = assetJpaRepository.findById(entity.getId()).orElseThrow();
            assertThat(updated.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("C10-S02. 존재하지 않는 Asset을 삭제하면 404를 반환한다")
        void shouldReturn404WhenDeletingNonExistentAsset() {
            // when & then
            givenServiceAuth()
                    .queryParam("source", "commerce-api")
                    .when()
                    .delete(BASE_PATH + "/{assetId}", "non-existent-id")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("code", equalTo("ASSET-001"));
        }

        @Test
        @DisplayName("C10-S03. 권한 없는 source로 삭제하면 403을 반환한다")
        void shouldReturn403WhenSourceMismatch() {
            // given
            AssetJpaEntity entity =
                    assetJpaRepository.save(
                            createAssetWithSource("asset-auth-001", "commerce-api"));

            // when & then
            givenServiceAuth()
                    .queryParam("source", "other-service")
                    .when()
                    .delete(BASE_PATH + "/{assetId}", entity.getId())
                    .then()
                    .statusCode(HttpStatus.FORBIDDEN.value())
                    .body("code", equalTo("ASSET-004"));
        }

        @Test
        @DisplayName("C10-S04. 이미 삭제된 Asset을 재삭제하면 404를 반환한다 (deleted_at IS NULL 조건)")
        void shouldReturn404WhenDeletingAlreadyDeletedAsset() {
            // given - deleted_at이 설정된 Asset
            assetJpaRepository.save(AssetJpaEntityFixture.aDeletedAssetEntity());

            // when & then - notDeleted() 조건으로 조회 자체가 실패하여 404
            givenServiceAuth()
                    .queryParam("source", "commerce-service")
                    .when()
                    .delete(BASE_PATH + "/{assetId}", "asset-del-001")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("code", equalTo("ASSET-001"));
        }

        @Test
        @DisplayName("C10-S05. source 파라미터가 누락되면 400을 반환한다")
        void shouldReturn400WhenSourceMissing() {
            // given
            AssetJpaEntity entity = assetJpaRepository.save(AssetJpaEntityFixture.anAssetEntity());

            // when & then
            givenServiceAuth()
                    .when()
                    .delete(BASE_PATH + "/{assetId}", entity.getId())
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("C10-S06. source가 빈 문자열이면 Validation 에러를 반환한다")
        void shouldReturnErrorWhenSourceIsBlank() {
            // given
            AssetJpaEntity entity = assetJpaRepository.save(AssetJpaEntityFixture.anAssetEntity());

            // when & then
            givenServiceAuth()
                    .queryParam("source", "")
                    .when()
                    .delete(BASE_PATH + "/{assetId}", entity.getId())
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }
    }

    // ========================================
    // 전체 플로우 시나리오
    // ========================================
    @Nested
    @DisplayName("전체 플로우 시나리오")
    class FullFlowTest {

        @Test
        @DisplayName("FLOW-AS01. Asset 조회 -> 메타데이터 조회 플로우")
        void shouldGetAssetThenGetMetadata() {
            // given
            AssetJpaEntity asset = assetJpaRepository.save(AssetJpaEntityFixture.anAssetEntity());
            AssetMetadataJpaEntity metadata =
                    assetMetadataJpaRepository.save(
                            AssetMetadataJpaEntityFixture.aMetadataEntityWithAssetId(
                                    asset.getId()));

            // Step 1: Asset 상세 조회
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{assetId}", asset.getId())
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.assetId", equalTo(asset.getId()));

            // Step 2: 동일 Asset의 메타데이터 조회
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{assetId}/metadata", asset.getId())
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.assetId", equalTo(asset.getId()))
                    .body("data.width", equalTo(metadata.getWidth()))
                    .body("data.height", equalTo(metadata.getHeight()));
        }

        @Test
        @DisplayName("FLOW-AS02. Asset 조회 -> 삭제 -> 재조회 시 404 플로우")
        void shouldGetAssetThenDeleteThenGetReturns404() {
            // given
            AssetJpaEntity asset =
                    assetJpaRepository.save(
                            createAssetWithSource("asset-flow-001", "commerce-api"));

            // Step 1: Asset 조회 - 200 OK
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{assetId}", asset.getId())
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.assetId", equalTo(asset.getId()));

            // Step 2: Asset 삭제 - 204 NO_CONTENT
            givenServiceAuth()
                    .queryParam("source", "commerce-api")
                    .when()
                    .delete(BASE_PATH + "/{assetId}", asset.getId())
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            // Step 3: 삭제된 Asset 재조회 - 404 NOT_FOUND (논리 삭제로 조회 불가)
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{assetId}", asset.getId())
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("code", equalTo("ASSET-001"));
        }

        @Test
        @DisplayName("FLOW-AS03. 삭제 -> 재삭제 시도 시 404 플로우")
        void shouldDeleteThenReDeleteReturns404() {
            // given
            AssetJpaEntity asset =
                    assetJpaRepository.save(
                            createAssetWithSource("asset-redel-001", "commerce-api"));

            // Step 1: 첫 번째 삭제 - 204 NO_CONTENT
            givenServiceAuth()
                    .queryParam("source", "commerce-api")
                    .when()
                    .delete(BASE_PATH + "/{assetId}", asset.getId())
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            // Step 2: 재삭제 시도 - 404 (deleted_at IS NULL 조건으로 조회 실패)
            givenServiceAuth()
                    .queryParam("source", "commerce-api")
                    .when()
                    .delete(BASE_PATH + "/{assetId}", asset.getId())
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("code", equalTo("ASSET-001"));
        }
    }

    // ========================================
    // Helper 메서드
    // ========================================

    private AssetJpaEntity createAssetWithOrigin(String id, AssetOrigin origin, String originId) {
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
                origin,
                originId,
                "product-image",
                "commerce-service",
                DEFAULT_NOW,
                DEFAULT_NOW,
                null);
    }

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
