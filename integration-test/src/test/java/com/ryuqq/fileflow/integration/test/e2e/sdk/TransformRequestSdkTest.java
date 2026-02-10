package com.ryuqq.fileflow.integration.test.e2e.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.adapter.out.persistence.asset.AssetJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.AssetJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.asset.repository.AssetJpaRepository;
import com.ryuqq.fileflow.adapter.out.persistence.transform.TransformRequestJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.transform.entity.TransformRequestJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.transform.repository.TransformRequestJpaRepository;
import com.ryuqq.fileflow.domain.asset.vo.AssetOrigin;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.sdk.exception.FileFlowBadRequestException;
import com.ryuqq.fileflow.sdk.exception.FileFlowNotFoundException;
import com.ryuqq.fileflow.sdk.model.common.ApiResponse;
import com.ryuqq.fileflow.sdk.model.transform.CreateTransformRequestRequest;
import com.ryuqq.fileflow.sdk.model.transform.TransformRequestResponse;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * SDK를 통한 이미지 변환 요청 통합 테스트.
 *
 * <p>FileFlowClient SDK를 사용하여 변환 요청 생성/조회를 검증합니다.
 */
@DisplayName("SDK - Transform Request 통합 테스트")
class TransformRequestSdkTest extends SdkTestBase {

    private static final Instant DEFAULT_NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Autowired private TransformRequestJpaRepository transformRequestJpaRepository;

    @Autowired private AssetJpaRepository assetJpaRepository;

    @BeforeEach
    void setUp() {
        transformRequestJpaRepository.deleteAllInBatch();
        assetJpaRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("변환 요청 생성")
    class CreateTest {

        @Test
        @DisplayName("RESIZE 요청을 생성하면 QUEUED 상태 요청이 반환된다")
        void shouldCreateResizeRequest() {
            // given
            AssetJpaEntity asset = assetJpaRepository.save(AssetJpaEntityFixture.anAssetEntity());

            var request =
                    new CreateTransformRequestRequest(
                            asset.getId(), "RESIZE", 800, 600, null, null);

            // when
            ApiResponse<TransformRequestResponse> response =
                    client.transformRequest().create(request);

            // then
            TransformRequestResponse transform = response.data();
            assertThat(transform.transformRequestId()).isNotBlank();
            assertThat(transform.sourceAssetId()).isEqualTo(asset.getId());
            assertThat(transform.transformType()).isEqualTo("RESIZE");
            assertThat(transform.status()).isEqualTo("QUEUED");
            assertThat(transform.width()).isEqualTo(800);
            assertThat(transform.height()).isEqualTo(600);
            assertThat(transform.createdAt()).isNotBlank();
        }

        @Test
        @DisplayName("CONVERT 요청을 생성하면 QUEUED 상태 요청이 반환된다")
        void shouldCreateConvertRequest() {
            // given
            AssetJpaEntity asset = assetJpaRepository.save(AssetJpaEntityFixture.anAssetEntity());

            var request =
                    new CreateTransformRequestRequest(
                            asset.getId(), "CONVERT", null, null, null, "webp");

            // when
            ApiResponse<TransformRequestResponse> response =
                    client.transformRequest().create(request);

            // then
            assertThat(response.data().transformType()).isEqualTo("CONVERT");
            assertThat(response.data().status()).isEqualTo("QUEUED");
        }

        @Test
        @DisplayName("COMPRESS 요청을 생성하면 QUEUED 상태 요청이 반환된다")
        void shouldCreateCompressRequest() {
            // given
            AssetJpaEntity asset = assetJpaRepository.save(AssetJpaEntityFixture.anAssetEntity());

            var request =
                    new CreateTransformRequestRequest(
                            asset.getId(), "COMPRESS", null, null, 85, null);

            // when
            ApiResponse<TransformRequestResponse> response =
                    client.transformRequest().create(request);

            // then
            assertThat(response.data().transformType()).isEqualTo("COMPRESS");
            assertThat(response.data().status()).isEqualTo("QUEUED");
        }

        @Test
        @DisplayName("존재하지 않는 Asset으로 요청하면 FileFlowNotFoundException이 발생한다")
        void shouldThrowNotFoundWhenAssetNotExists() {
            var request =
                    new CreateTransformRequestRequest(
                            "non-existent", "RESIZE", 800, null, null, null);

            assertThatThrownBy(() -> client.transformRequest().create(request))
                    .isInstanceOf(FileFlowNotFoundException.class)
                    .satisfies(
                            ex -> {
                                FileFlowNotFoundException e = (FileFlowNotFoundException) ex;
                                assertThat(e.getErrorCode()).isEqualTo("ASSET-001");
                            });
        }

        @Test
        @DisplayName("이미지가 아닌 Asset으로 요청하면 FileFlowBadRequestException이 발생한다")
        void shouldThrowBadRequestWhenNotImage() {
            // given
            AssetJpaEntity pdfAsset =
                    assetJpaRepository.save(
                            AssetJpaEntity.create(
                                    "asset-pdf-sdk",
                                    "test-bucket",
                                    "public/document.pdf",
                                    AccessType.PUBLIC,
                                    "document.pdf",
                                    2048L,
                                    "application/pdf",
                                    "etag-pdf",
                                    "pdf",
                                    AssetOrigin.SINGLE_UPLOAD,
                                    "origin-pdf",
                                    "document",
                                    "commerce-service",
                                    DEFAULT_NOW,
                                    DEFAULT_NOW,
                                    null));

            var request =
                    new CreateTransformRequestRequest(
                            pdfAsset.getId(), "RESIZE", 800, null, null, null);

            // when & then
            assertThatThrownBy(() -> client.transformRequest().create(request))
                    .isInstanceOf(FileFlowBadRequestException.class)
                    .satisfies(
                            ex -> {
                                FileFlowBadRequestException e = (FileFlowBadRequestException) ex;
                                assertThat(e.getErrorCode()).isEqualTo("TRANSFORM-002");
                            });
        }
    }

    @Nested
    @DisplayName("변환 요청 조회")
    class GetTest {

        @Test
        @DisplayName("존재하는 변환 요청을 조회하면 요청 정보가 반환된다")
        void shouldGetTransformRequest() {
            // given
            TransformRequestJpaEntity entity =
                    transformRequestJpaRepository.save(
                            TransformRequestJpaEntityFixture.aQueuedResizeEntity());

            // when
            ApiResponse<TransformRequestResponse> response =
                    client.transformRequest().get(entity.getId());

            // then
            TransformRequestResponse transform = response.data();
            assertThat(transform.transformRequestId()).isEqualTo(entity.getId());
            assertThat(transform.sourceAssetId()).isEqualTo(entity.getSourceAssetId());
            assertThat(transform.status()).isEqualTo("QUEUED");
            assertThat(transform.width()).isEqualTo(entity.getWidth());
            assertThat(transform.height()).isEqualTo(entity.getHeight());
        }

        @Test
        @DisplayName("COMPLETED 상태 요청을 조회하면 결과 정보가 포함된다")
        void shouldGetCompletedRequest() {
            // given
            TransformRequestJpaEntity entity =
                    transformRequestJpaRepository.save(
                            TransformRequestJpaEntityFixture.aCompletedEntity());

            // when
            ApiResponse<TransformRequestResponse> response =
                    client.transformRequest().get(entity.getId());

            // then
            assertThat(response.data().status()).isEqualTo("COMPLETED");
            assertThat(response.data().resultAssetId()).isNotNull();
            assertThat(response.data().completedAt()).isNotNull();
        }

        @Test
        @DisplayName("존재하지 않는 변환 요청을 조회하면 FileFlowNotFoundException이 발생한다")
        void shouldThrowNotFoundWhenRequestNotExists() {
            assertThatThrownBy(() -> client.transformRequest().get("non-existent-id"))
                    .isInstanceOf(FileFlowNotFoundException.class)
                    .satisfies(
                            ex -> {
                                FileFlowNotFoundException e = (FileFlowNotFoundException) ex;
                                assertThat(e.getErrorCode()).isEqualTo("TRANSFORM-001");
                            });
        }
    }

    @Nested
    @DisplayName("전체 플로우")
    class FullFlowTest {

        @Test
        @DisplayName("생성 -> 조회 확인 플로우")
        void shouldCreateAndRetrieve() {
            // given
            AssetJpaEntity asset = assetJpaRepository.save(AssetJpaEntityFixture.anAssetEntity());

            // Step 1: 변환 요청 생성
            var request =
                    new CreateTransformRequestRequest(
                            asset.getId(), "RESIZE", 800, 600, null, null);
            ApiResponse<TransformRequestResponse> createResponse =
                    client.transformRequest().create(request);
            String transformRequestId = createResponse.data().transformRequestId();
            assertThat(transformRequestId).isNotBlank();

            // Step 2: 조회 - QUEUED 상태
            ApiResponse<TransformRequestResponse> getResponse =
                    client.transformRequest().get(transformRequestId);
            assertThat(getResponse.data().transformRequestId()).isEqualTo(transformRequestId);
            assertThat(getResponse.data().status()).isEqualTo("QUEUED");
            assertThat(getResponse.data().sourceAssetId()).isEqualTo(asset.getId());
            assertThat(getResponse.data().transformType()).isEqualTo("RESIZE");
            assertThat(getResponse.data().width()).isEqualTo(800);
            assertThat(getResponse.data().height()).isEqualTo(600);
        }
    }
}
