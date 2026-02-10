package com.ryuqq.fileflow.integration.test.e2e.web.download;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.ryuqq.fileflow.adapter.out.persistence.download.DownloadTaskJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.download.entity.DownloadTaskJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.CallbackOutboxJpaRepository;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.DownloadTaskJpaRepository;
import com.ryuqq.fileflow.integration.test.common.base.E2ETestBase;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

/**
 * Download Task E2E 테스트.
 *
 * <p>실제 TestContainers(MySQL, Redis, LocalStack) 환경에서 다운로드 작업의 생성, 조회 API를 검증합니다.
 */
@DisplayName("Download Task E2E 테스트")
class DownloadTaskE2ETest extends E2ETestBase {

    private static final String BASE_PATH = "/api/v1/download-tasks";

    @Autowired private DownloadTaskJpaRepository downloadTaskJpaRepository;

    @Autowired private CallbackOutboxJpaRepository callbackOutboxJpaRepository;

    @BeforeEach
    void setUp() {
        callbackOutboxJpaRepository.deleteAllInBatch();
        downloadTaskJpaRepository.deleteAllInBatch();
    }

    // ========================================
    // Q3: GET /api/v1/download-tasks/{downloadTaskId} - 다운로드 작업 상세 조회
    // ========================================
    @Nested
    @DisplayName("GET /api/v1/download-tasks/{downloadTaskId} - 다운로드 작업 상세 조회")
    class GetDownloadTaskTest {

        @Test
        @DisplayName("Q3-S01. 존재하는 QUEUED 상태 다운로드 작업을 조회하면 200과 작업 정보를 반환한다")
        void shouldReturnQueuedDownloadTaskWhenExists() {
            // given
            DownloadTaskJpaEntity entity =
                    downloadTaskJpaRepository.save(DownloadTaskJpaEntityFixture.aQueuedEntity());

            // when & then
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{downloadTaskId}", entity.getId())
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.downloadTaskId", equalTo(entity.getId()))
                    .body("data.sourceUrl", equalTo(entity.getSourceUrl()))
                    .body("data.s3Key", equalTo(entity.getS3Key()))
                    .body("data.bucket", equalTo(entity.getBucket()))
                    .body("data.accessType", equalTo(entity.getAccessType().name()))
                    .body("data.purpose", equalTo(entity.getPurpose()))
                    .body("data.source", equalTo(entity.getSource()))
                    .body("data.status", equalTo("QUEUED"))
                    .body("data.retryCount", equalTo(0))
                    .body("data.maxRetries", equalTo(3))
                    .body("data.startedAt", nullValue())
                    .body("data.completedAt", nullValue())
                    .body("data.createdAt", notNullValue());
        }

        @Test
        @DisplayName("Q3-S02. 존재하지 않는 작업을 조회하면 404를 반환한다")
        void shouldReturn404WhenDownloadTaskNotFound() {
            // when & then
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{downloadTaskId}", "non-existent-id")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("code", equalTo("DOWNLOAD-001"));
        }

        @Test
        @DisplayName("Q3-S03. COMPLETED 상태 작업을 조회하면 200과 완료 정보를 반환한다")
        void shouldReturnCompletedDownloadTask() {
            // given
            DownloadTaskJpaEntity entity =
                    downloadTaskJpaRepository.save(DownloadTaskJpaEntityFixture.aCompletedEntity());

            // when & then
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{downloadTaskId}", entity.getId())
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.status", equalTo("COMPLETED"))
                    .body("data.startedAt", notNullValue())
                    .body("data.completedAt", notNullValue());
        }

        @Test
        @DisplayName("Q3-S04. FAILED 상태 작업을 조회하면 200과 에러 정보를 반환한다")
        void shouldReturnFailedDownloadTaskWithLastError() {
            // given
            DownloadTaskJpaEntity entity =
                    downloadTaskJpaRepository.save(
                            DownloadTaskJpaEntityFixture.aFailedEntity("Connection timeout"));

            // when & then
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{downloadTaskId}", entity.getId())
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.status", equalTo("FAILED"))
                    .body("data.lastError", equalTo("Connection timeout"))
                    .body("data.retryCount", equalTo(3));
        }

        @Test
        @DisplayName("Q3-S05. callbackUrl이 있는 작업을 조회하면 200과 콜백 정보를 반환한다")
        void shouldReturnDownloadTaskWithCallbackUrl() {
            // given - aQueuedEntity()는 기본적으로 callbackUrl이 설정되어 있음
            DownloadTaskJpaEntity entity =
                    downloadTaskJpaRepository.save(DownloadTaskJpaEntityFixture.aQueuedEntity());

            // when & then
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{downloadTaskId}", entity.getId())
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.callbackUrl", notNullValue());
        }
    }

    // ========================================
    // C8: POST /api/v1/download-tasks - 다운로드 작업 생성
    // ========================================
    @Nested
    @DisplayName("POST /api/v1/download-tasks - 다운로드 작업 생성")
    class CreateDownloadTaskTest {

        @Test
        @DisplayName("C8-S01. 유효한 요청으로 작업을 생성하면 201과 작업 정보를 반환한다 (callbackUrl 포함)")
        void shouldCreateDownloadTaskWithCallback() {
            // when
            Response response =
                    givenServiceAuth().body(createRequestWithCallback()).when().post(BASE_PATH);

            // then
            response.then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("data.downloadTaskId", notNullValue())
                    .body("data.status", equalTo("QUEUED"))
                    .body("data.retryCount", equalTo(0))
                    .body("data.callbackUrl", notNullValue());

            // DB 검증
            String downloadTaskId = response.jsonPath().getString("data.downloadTaskId");
            assertThat(downloadTaskJpaRepository.findById(downloadTaskId)).isPresent();
            var saved = downloadTaskJpaRepository.findById(downloadTaskId).orElseThrow();
            assertThat(saved.getStatus().name()).isEqualTo("QUEUED");
        }

        @Test
        @DisplayName("C8-S02. callbackUrl 없이 작업을 생성하면 201과 callbackUrl=null을 반환한다")
        void shouldCreateDownloadTaskWithoutCallback() {
            // when
            Response response =
                    givenServiceAuth().body(createRequestWithoutCallback()).when().post(BASE_PATH);

            // then
            response.then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("data.downloadTaskId", notNullValue())
                    .body("data.callbackUrl", nullValue());
        }

        @Test
        @DisplayName("C8-S03. sourceUrl 누락 시 400을 반환한다")
        void shouldReturn400WhenSourceUrlMissing() {
            // given
            Map<String, Object> request =
                    Map.of(
                            "s3Key", "downloads/image.jpg",
                            "bucket", "fileflow-bucket",
                            "accessType", "PUBLIC",
                            "purpose", "PRODUCT_IMAGE",
                            "source", "commerce-api");

            // when & then
            givenServiceAuth()
                    .body(request)
                    .when()
                    .post(BASE_PATH)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("C8-S04. s3Key 누락 시 400을 반환한다")
        void shouldReturn400WhenS3KeyMissing() {
            // given
            Map<String, Object> request =
                    Map.of(
                            "sourceUrl", "https://example.com/image.jpg",
                            "bucket", "fileflow-bucket",
                            "accessType", "PUBLIC",
                            "purpose", "PRODUCT_IMAGE",
                            "source", "commerce-api");

            // when & then
            givenServiceAuth()
                    .body(request)
                    .when()
                    .post(BASE_PATH)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("C8-S05. bucket 누락 시 400을 반환한다")
        void shouldReturn400WhenBucketMissing() {
            // given
            Map<String, Object> request =
                    Map.of(
                            "sourceUrl", "https://example.com/image.jpg",
                            "s3Key", "downloads/image.jpg",
                            "accessType", "PUBLIC",
                            "purpose", "PRODUCT_IMAGE",
                            "source", "commerce-api");

            // when & then
            givenServiceAuth()
                    .body(request)
                    .when()
                    .post(BASE_PATH)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("C8-S06. accessType 누락 시 400을 반환한다")
        void shouldReturn400WhenAccessTypeMissing() {
            // given
            Map<String, Object> request =
                    Map.of(
                            "sourceUrl", "https://example.com/image.jpg",
                            "s3Key", "downloads/image.jpg",
                            "bucket", "fileflow-bucket",
                            "purpose", "PRODUCT_IMAGE",
                            "source", "commerce-api");

            // when & then
            givenServiceAuth()
                    .body(request)
                    .when()
                    .post(BASE_PATH)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("C8-S07. purpose 누락 시 400을 반환한다")
        void shouldReturn400WhenPurposeMissing() {
            // given
            Map<String, Object> request =
                    Map.of(
                            "sourceUrl", "https://example.com/image.jpg",
                            "s3Key", "downloads/image.jpg",
                            "bucket", "fileflow-bucket",
                            "accessType", "PUBLIC",
                            "source", "commerce-api");

            // when & then
            givenServiceAuth()
                    .body(request)
                    .when()
                    .post(BASE_PATH)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("C8-S08. source 누락 시 400을 반환한다")
        void shouldReturn400WhenSourceMissing() {
            // given
            Map<String, Object> request =
                    Map.of(
                            "sourceUrl", "https://example.com/image.jpg",
                            "s3Key", "downloads/image.jpg",
                            "bucket", "fileflow-bucket",
                            "accessType", "PUBLIC",
                            "purpose", "PRODUCT_IMAGE");

            // when & then
            givenServiceAuth()
                    .body(request)
                    .when()
                    .post(BASE_PATH)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("C8-S09. 잘못된 sourceUrl 형식 전달 시 400을 반환한다")
        void shouldReturn400WhenInvalidSourceUrlFormat() {
            // given
            Map<String, Object> request =
                    Map.of(
                            "sourceUrl", "not-a-url",
                            "s3Key", "downloads/image.jpg",
                            "bucket", "fileflow-bucket",
                            "accessType", "PUBLIC",
                            "purpose", "PRODUCT_IMAGE",
                            "source", "commerce-api");

            // when & then
            givenServiceAuth()
                    .body(request)
                    .when()
                    .post(BASE_PATH)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .body("code", equalTo("DOWNLOAD-005"));
        }

        @Test
        @DisplayName("C8-S10. 잘못된 callbackUrl 형식 전달 시 400을 반환한다")
        void shouldReturn400WhenInvalidCallbackUrlFormat() {
            // given
            Map<String, Object> request = new HashMap<>();
            request.put("sourceUrl", "https://example.com/image.jpg");
            request.put("s3Key", "downloads/image.jpg");
            request.put("bucket", "fileflow-bucket");
            request.put("accessType", "PUBLIC");
            request.put("purpose", "PRODUCT_IMAGE");
            request.put("source", "commerce-api");
            request.put("callbackUrl", "not-a-url");

            // when & then
            givenServiceAuth()
                    .body(request)
                    .when()
                    .post(BASE_PATH)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .body("code", equalTo("DOWNLOAD-006"));
        }
    }

    // ========================================
    // 전체 플로우 시나리오
    // ========================================
    @Nested
    @DisplayName("전체 플로우 시나리오")
    class FullFlowTest {

        @Test
        @DisplayName("FLOW-DL01. 생성 -> 조회 확인 플로우")
        void shouldCreateAndRetrieveDownloadTask() {
            // Step 1: 다운로드 작업 생성
            Response createResponse =
                    givenServiceAuth().body(createRequestWithCallback()).when().post(BASE_PATH);

            createResponse.then().statusCode(HttpStatus.CREATED.value());
            String downloadTaskId = createResponse.jsonPath().getString("data.downloadTaskId");
            assertThat(downloadTaskId).isNotBlank();

            // Step 2: 생성된 작업 조회 - QUEUED 상태
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{downloadTaskId}", downloadTaskId)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.downloadTaskId", equalTo(downloadTaskId))
                    .body("data.status", equalTo("QUEUED"))
                    .body("data.retryCount", equalTo(0))
                    .body("data.maxRetries", equalTo(3));
        }
    }

    // ========================================
    // Helper 메서드
    // ========================================

    private Map<String, Object> createRequestWithCallback() {
        Map<String, Object> request = new HashMap<>();
        request.put("sourceUrl", "https://example.com/image.jpg");
        request.put("s3Key", "downloads/2026/02/image.jpg");
        request.put("bucket", "fileflow-bucket");
        request.put("accessType", "PUBLIC");
        request.put("purpose", "PRODUCT_IMAGE");
        request.put("source", "commerce-api");
        request.put("callbackUrl", "https://commerce-api.internal/callbacks/download");
        return request;
    }

    private Map<String, Object> createRequestWithoutCallback() {
        Map<String, Object> request = new HashMap<>();
        request.put("sourceUrl", "https://example.com/image.jpg");
        request.put("s3Key", "downloads/image.jpg");
        request.put("bucket", "fileflow-bucket");
        request.put("accessType", "PUBLIC");
        request.put("purpose", "PRODUCT_IMAGE");
        request.put("source", "commerce-api");
        request.put("callbackUrl", null);
        return request;
    }
}
