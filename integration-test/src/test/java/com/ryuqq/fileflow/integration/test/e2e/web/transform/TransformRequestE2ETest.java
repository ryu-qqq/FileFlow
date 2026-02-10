package com.ryuqq.fileflow.integration.test.e2e.web.transform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.ryuqq.fileflow.adapter.out.persistence.asset.AssetJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.AssetJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.asset.repository.AssetJpaRepository;
import com.ryuqq.fileflow.adapter.out.persistence.transform.TransformRequestJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.transform.entity.TransformRequestJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.transform.repository.TransformRequestJpaRepository;
import com.ryuqq.fileflow.domain.asset.vo.AssetOrigin;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.domain.transform.vo.TransformStatus;
import com.ryuqq.fileflow.domain.transform.vo.TransformType;
import com.ryuqq.fileflow.integration.test.common.base.E2ETestBase;
import io.restassured.response.Response;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

/**
 * Transform Request E2E 테스트.
 *
 * <p>실제 TestContainers(MySQL, Redis, LocalStack) 환경에서 이미지 변환 요청의 생성, 조회 API를 검증합니다.
 */
@DisplayName("Transform Request E2E 테스트")
class TransformRequestE2ETest extends E2ETestBase {

    private static final String BASE_PATH = "/api/v1/transform-requests";
    private static final Instant DEFAULT_NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Autowired private TransformRequestJpaRepository transformRequestJpaRepository;

    @Autowired private AssetJpaRepository assetJpaRepository;

    @BeforeEach
    void setUp() {
        transformRequestJpaRepository.deleteAllInBatch();
        assetJpaRepository.deleteAllInBatch();
    }

    // ========================================
    // Q4: GET /api/v1/transform-requests/{transformRequestId} - 변환 요청 상세 조회
    // ========================================
    @Nested
    @DisplayName("GET /api/v1/transform-requests/{transformRequestId} - 변환 요청 상세 조회")
    class GetTransformRequestTest {

        @Test
        @DisplayName("Q4-S01. 존재하는 QUEUED 상태 변환 요청을 조회하면 200과 요청 정보를 반환한다")
        void shouldReturnQueuedTransformRequestWhenExists() {
            // given
            TransformRequestJpaEntity entity =
                    transformRequestJpaRepository.save(
                            TransformRequestJpaEntityFixture.aQueuedResizeEntity());

            // when & then
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{transformRequestId}", entity.getId())
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.transformRequestId", equalTo(entity.getId()))
                    .body("data.sourceAssetId", equalTo(entity.getSourceAssetId()))
                    .body("data.sourceContentType", equalTo(entity.getSourceContentType()))
                    .body("data.transformType", equalTo(entity.getType().name()))
                    .body("data.status", equalTo("QUEUED"))
                    .body("data.width", equalTo(entity.getWidth()))
                    .body("data.height", equalTo(entity.getHeight()))
                    .body("data.resultAssetId", nullValue())
                    .body("data.lastError", nullValue())
                    .body("data.createdAt", notNullValue());
        }

        @Test
        @DisplayName("Q4-S02. 존재하지 않는 변환 요청을 조회하면 404를 반환한다")
        void shouldReturn404WhenTransformRequestNotFound() {
            // when & then
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{transformRequestId}", "non-existent-id")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("code", equalTo("TRANSFORM-001"));
        }

        @Test
        @DisplayName("Q4-S03. COMPLETED 상태 변환 요청을 조회하면 200과 완료 정보를 반환한다")
        void shouldReturnCompletedTransformRequestWithResultAssetId() {
            // given
            TransformRequestJpaEntity entity =
                    transformRequestJpaRepository.save(
                            TransformRequestJpaEntityFixture.aCompletedEntity());

            // when & then
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{transformRequestId}", entity.getId())
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.status", equalTo("COMPLETED"))
                    .body("data.resultAssetId", notNullValue())
                    .body("data.completedAt", notNullValue());
        }

        @Test
        @DisplayName("Q4-S04. FAILED 상태 변환 요청을 조회하면 200과 에러 정보를 반환한다")
        void shouldReturnFailedTransformRequestWithLastError() {
            // given
            TransformRequestJpaEntity entity =
                    transformRequestJpaRepository.save(createFailedEntity("Out of memory"));

            // when & then
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{transformRequestId}", entity.getId())
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.status", equalTo("FAILED"))
                    .body("data.lastError", equalTo("Out of memory"));
        }
    }

    // ========================================
    // C9: POST /api/v1/transform-requests - 변환 요청 생성
    // ========================================
    @Nested
    @DisplayName("POST /api/v1/transform-requests - 변환 요청 생성")
    class CreateTransformRequestTest {

        @Test
        @DisplayName("C9-S01. RESIZE 요청을 width + height로 생성하면 201과 QUEUED 상태를 반환한다")
        void shouldCreateResizeRequestWithWidthAndHeight() {
            // given
            AssetJpaEntity asset = assetJpaRepository.save(createImageAsset());

            // when
            Response response =
                    givenServiceAuth()
                            .body(createResizeRequest(asset.getId(), 800, 600))
                            .when()
                            .post(BASE_PATH);

            // then
            response.then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("data.transformRequestId", notNullValue())
                    .body("data.status", equalTo("QUEUED"))
                    .body("data.sourceAssetId", equalTo(asset.getId()));

            // DB 검증
            String transformRequestId = response.jsonPath().getString("data.transformRequestId");
            assertThat(transformRequestJpaRepository.findById(transformRequestId)).isPresent();
        }

        @Test
        @DisplayName("C9-S02. RESIZE 요청을 width만으로 생성하면 201을 반환한다")
        void shouldCreateResizeRequestWithWidthOnly() {
            // given
            AssetJpaEntity asset = assetJpaRepository.save(createImageAsset());

            // when & then
            givenServiceAuth()
                    .body(createResizeRequest(asset.getId(), 800, null))
                    .when()
                    .post(BASE_PATH)
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("data.transformRequestId", notNullValue());
        }

        @Test
        @DisplayName("C9-S03. CONVERT 요청을 생성하면 201을 반환한다")
        void shouldCreateConvertRequest() {
            // given
            AssetJpaEntity asset = assetJpaRepository.save(createImageAsset());

            // when & then
            givenServiceAuth()
                    .body(createConvertRequest(asset.getId(), "webp"))
                    .when()
                    .post(BASE_PATH)
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("data.transformRequestId", notNullValue());
        }

        @Test
        @DisplayName("C9-S04. COMPRESS 요청을 생성하면 201을 반환한다")
        void shouldCreateCompressRequest() {
            // given
            AssetJpaEntity asset = assetJpaRepository.save(createImageAsset());

            // when & then
            givenServiceAuth()
                    .body(createCompressRequest(asset.getId(), 85))
                    .when()
                    .post(BASE_PATH)
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("data.transformRequestId", notNullValue());
        }

        @Test
        @DisplayName("C9-S05. THUMBNAIL 요청을 생성하면 201을 반환한다")
        void shouldCreateThumbnailRequest() {
            // given
            AssetJpaEntity asset = assetJpaRepository.save(createImageAsset());

            // when & then
            givenServiceAuth()
                    .body(createThumbnailRequest(asset.getId(), 150, 150))
                    .when()
                    .post(BASE_PATH)
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("data.transformRequestId", notNullValue());
        }

        @Test
        @DisplayName("C9-S06. sourceAssetId 누락 시 400을 반환한다")
        void shouldReturn400WhenSourceAssetIdMissing() {
            // given
            Map<String, Object> request = new HashMap<>();
            request.put("transformType", "RESIZE");
            request.put("width", 800);

            // when & then
            givenServiceAuth()
                    .body(request)
                    .when()
                    .post(BASE_PATH)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("C9-S07. transformType 누락 시 400을 반환한다")
        void shouldReturn400WhenTransformTypeMissing() {
            // given
            Map<String, Object> request = new HashMap<>();
            request.put("sourceAssetId", "some-id");
            request.put("width", 800);

            // when & then
            givenServiceAuth()
                    .body(request)
                    .when()
                    .post(BASE_PATH)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("C9-S08. 존재하지 않는 Asset으로 요청하면 404를 반환한다")
        void shouldReturn404WhenAssetNotFound() {
            // when & then
            givenServiceAuth()
                    .body(createResizeRequest("non-existent", 800, null))
                    .when()
                    .post(BASE_PATH)
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("code", equalTo("ASSET-001"));
        }

        @Test
        @DisplayName("C9-S09. 이미지가 아닌 Asset으로 요청하면 400을 반환한다")
        void shouldReturn400WhenAssetIsNotImage() {
            // given
            AssetJpaEntity pdfAsset = assetJpaRepository.save(createNonImageAsset());

            // when & then
            givenServiceAuth()
                    .body(createResizeRequest(pdfAsset.getId(), 800, null))
                    .when()
                    .post(BASE_PATH)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .body("code", equalTo("TRANSFORM-002"));
        }

        @Test
        @DisplayName("C9-S10. RESIZE에 width/height 모두 누락 시 400을 반환한다")
        void shouldReturn400WhenResizeWithoutWidthAndHeight() {
            // given
            AssetJpaEntity asset = assetJpaRepository.save(createImageAsset());
            Map<String, Object> request = new HashMap<>();
            request.put("sourceAssetId", asset.getId());
            request.put("transformType", "RESIZE");

            // when & then
            givenServiceAuth()
                    .body(request)
                    .when()
                    .post(BASE_PATH)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .body("code", equalTo("TRANSFORM-003"));
        }

        @Test
        @DisplayName("C9-S11. CONVERT에 targetFormat 누락 시 400을 반환한다")
        void shouldReturn400WhenConvertWithoutTargetFormat() {
            // given
            AssetJpaEntity asset = assetJpaRepository.save(createImageAsset());
            Map<String, Object> request = new HashMap<>();
            request.put("sourceAssetId", asset.getId());
            request.put("transformType", "CONVERT");

            // when & then
            givenServiceAuth()
                    .body(request)
                    .when()
                    .post(BASE_PATH)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .body("code", equalTo("TRANSFORM-003"));
        }

        @Test
        @DisplayName("C9-S12. COMPRESS에 quality 누락 시 400을 반환한다")
        void shouldReturn400WhenCompressWithoutQuality() {
            // given
            AssetJpaEntity asset = assetJpaRepository.save(createImageAsset());
            Map<String, Object> request = new HashMap<>();
            request.put("sourceAssetId", asset.getId());
            request.put("transformType", "COMPRESS");

            // when & then
            givenServiceAuth()
                    .body(request)
                    .when()
                    .post(BASE_PATH)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .body("code", equalTo("TRANSFORM-003"));
        }

        @Test
        @DisplayName("C9-S13. THUMBNAIL에 width 누락 시 400을 반환한다")
        void shouldReturn400WhenThumbnailWithoutWidth() {
            // given
            AssetJpaEntity asset = assetJpaRepository.save(createImageAsset());
            Map<String, Object> request = new HashMap<>();
            request.put("sourceAssetId", asset.getId());
            request.put("transformType", "THUMBNAIL");
            request.put("height", 150);

            // when & then
            givenServiceAuth()
                    .body(request)
                    .when()
                    .post(BASE_PATH)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .body("code", equalTo("TRANSFORM-003"));
        }

        @Test
        @DisplayName("C9-S14. THUMBNAIL에 height 누락 시 400을 반환한다")
        void shouldReturn400WhenThumbnailWithoutHeight() {
            // given
            AssetJpaEntity asset = assetJpaRepository.save(createImageAsset());
            Map<String, Object> request = new HashMap<>();
            request.put("sourceAssetId", asset.getId());
            request.put("transformType", "THUMBNAIL");
            request.put("width", 150);

            // when & then
            givenServiceAuth()
                    .body(request)
                    .when()
                    .post(BASE_PATH)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .body("code", equalTo("TRANSFORM-003"));
        }

        @Test
        @DisplayName("C9-S15. 삭제된 Asset으로 요청하면 404를 반환한다")
        void shouldReturn404WhenAssetIsDeleted() {
            // given
            AssetJpaEntity deletedAsset =
                    assetJpaRepository.save(AssetJpaEntityFixture.aDeletedAssetEntity());

            // when & then
            givenServiceAuth()
                    .body(createResizeRequest(deletedAsset.getId(), 800, null))
                    .when()
                    .post(BASE_PATH)
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("code", equalTo("ASSET-001"));
        }
    }

    // ========================================
    // 전체 플로우 시나리오
    // ========================================
    @Nested
    @DisplayName("전체 플로우 시나리오")
    class FullFlowTest {

        @Test
        @DisplayName("FLOW-TR01. 생성 -> 조회 확인 플로우")
        void shouldCreateAndRetrieveTransformRequest() {
            // given
            AssetJpaEntity asset = assetJpaRepository.save(createImageAsset());

            // Step 1: 변환 요청 생성
            Response createResponse =
                    givenServiceAuth()
                            .body(createResizeRequest(asset.getId(), 800, 600))
                            .when()
                            .post(BASE_PATH);

            createResponse.then().statusCode(HttpStatus.CREATED.value());
            String transformRequestId =
                    createResponse.jsonPath().getString("data.transformRequestId");
            assertThat(transformRequestId).isNotBlank();

            // Step 2: 생성된 요청 조회 - QUEUED 상태
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{transformRequestId}", transformRequestId)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.transformRequestId", equalTo(transformRequestId))
                    .body("data.status", equalTo("QUEUED"))
                    .body("data.sourceAssetId", equalTo(asset.getId()))
                    .body("data.transformType", equalTo("RESIZE"))
                    .body("data.width", equalTo(800))
                    .body("data.height", equalTo(600));
        }
    }

    // ========================================
    // Helper 메서드 - 사전 데이터 생성
    // ========================================

    /** 이미지 Asset 생성 (contentType: image/jpeg). */
    private AssetJpaEntity createImageAsset() {
        return AssetJpaEntityFixture.anAssetEntity();
    }

    /** 비이미지 Asset 생성 (contentType: application/pdf). */
    private AssetJpaEntity createNonImageAsset() {
        return AssetJpaEntity.create(
                "asset-pdf-001",
                "test-bucket",
                "public/2026/02/document.pdf",
                AccessType.PUBLIC,
                "document.pdf",
                2048L,
                "application/pdf",
                "etag-pdf",
                "pdf",
                AssetOrigin.SINGLE_UPLOAD,
                "origin-pdf-001",
                "document",
                "commerce-service",
                DEFAULT_NOW,
                DEFAULT_NOW,
                null);
    }

    /** FAILED 상태 TransformRequest Entity 생성. */
    private TransformRequestJpaEntity createFailedEntity(String lastError) {
        return TransformRequestJpaEntity.create(
                "transform-failed-001",
                "asset-001",
                "image/jpeg",
                TransformType.RESIZE,
                TransformStatus.FAILED,
                null,
                lastError,
                800,
                600,
                true,
                null,
                null,
                DEFAULT_NOW,
                DEFAULT_NOW,
                null);
    }

    // ========================================
    // Helper 메서드 - Request Body 생성
    // ========================================

    /** RESIZE 요청 Body 생성. */
    private Map<String, Object> createResizeRequest(
            String sourceAssetId, Integer width, Integer height) {
        Map<String, Object> request = new HashMap<>();
        request.put("sourceAssetId", sourceAssetId);
        request.put("transformType", "RESIZE");
        request.put("width", width);
        request.put("height", height);
        return request;
    }

    /** CONVERT 요청 Body 생성. */
    private Map<String, Object> createConvertRequest(String sourceAssetId, String targetFormat) {
        Map<String, Object> request = new HashMap<>();
        request.put("sourceAssetId", sourceAssetId);
        request.put("transformType", "CONVERT");
        request.put("targetFormat", targetFormat);
        return request;
    }

    /** COMPRESS 요청 Body 생성. */
    private Map<String, Object> createCompressRequest(String sourceAssetId, int quality) {
        Map<String, Object> request = new HashMap<>();
        request.put("sourceAssetId", sourceAssetId);
        request.put("transformType", "COMPRESS");
        request.put("quality", quality);
        return request;
    }

    /** THUMBNAIL 요청 Body 생성. */
    private Map<String, Object> createThumbnailRequest(
            String sourceAssetId, int width, int height) {
        Map<String, Object> request = new HashMap<>();
        request.put("sourceAssetId", sourceAssetId);
        request.put("transformType", "THUMBNAIL");
        request.put("width", width);
        request.put("height", height);
        return request;
    }
}
