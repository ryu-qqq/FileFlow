package com.ryuqq.fileflow.integration.test.e2e.web.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.ryuqq.fileflow.adapter.out.persistence.session.SingleUploadSessionJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.SingleUploadSessionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.repository.SingleUploadSessionJpaRepository;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.domain.session.vo.SingleSessionStatus;
import com.ryuqq.fileflow.integration.test.common.base.E2ETestBase;
import io.restassured.response.Response;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

/**
 * 단건 업로드 세션 E2E 테스트.
 *
 * <p>실제 TestContainers(MySQL, Redis, LocalStack) 환경에서 단건 업로드 세션의 생성, 조회, 완료 API를 검증합니다.
 */
@DisplayName("Single Upload Session E2E 테스트")
class SingleUploadSessionE2ETest extends E2ETestBase {

    private static final String BASE_PATH = "/api/v1/sessions/single";

    @Autowired private SingleUploadSessionJpaRepository singleUploadSessionJpaRepository;

    @BeforeEach
    void setUp() {
        singleUploadSessionJpaRepository.deleteAllInBatch();
    }

    // ========================================
    // Q1: GET /api/v1/sessions/single/{sessionId} - 세션 상세 조회
    // ========================================
    @Nested
    @DisplayName("GET /api/v1/sessions/single/{sessionId} - 세션 상세 조회")
    class GetSingleUploadSessionTest {

        @Test
        @DisplayName("Q1-S01. 존재하는 세션을 조회하면 200과 세션 정보를 반환한다")
        void shouldReturnSessionWhenExists() {
            // given
            SingleUploadSessionJpaEntity entity =
                    singleUploadSessionJpaRepository.save(
                            SingleUploadSessionJpaEntityFixture.aCreatedEntity());

            // when & then
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{sessionId}", entity.getId())
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.sessionId", equalTo(entity.getId()))
                    .body("data.s3Key", equalTo(entity.getS3Key()))
                    .body("data.bucket", equalTo(entity.getBucket()))
                    .body("data.accessType", equalTo(entity.getAccessType().name()))
                    .body("data.fileName", equalTo(entity.getFileName()))
                    .body("data.contentType", equalTo(entity.getContentType()))
                    .body("data.presignedUrl", notNullValue())
                    .body("data.status", equalTo("CREATED"))
                    .body("data.expiresAt", notNullValue())
                    .body("data.createdAt", notNullValue());
        }

        @Test
        @DisplayName("Q1-S02. 존재하지 않는 세션을 조회하면 404를 반환한다")
        void shouldReturn404WhenSessionNotFound() {
            // when & then
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{sessionId}", "non-existent-id")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("code", equalTo("SESSION-001"));
        }

        @Test
        @DisplayName("Q1-S03. COMPLETED 상태 세션을 조회하면 200과 COMPLETED 상태를 반환한다")
        void shouldReturnCompletedSession() {
            // given
            SingleUploadSessionJpaEntity entity =
                    singleUploadSessionJpaRepository.save(
                            SingleUploadSessionJpaEntityFixture.aCompletedEntity());

            // when & then
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{sessionId}", entity.getId())
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.status", equalTo("COMPLETED"));
        }
    }

    // ========================================
    // C1: POST /api/v1/sessions/single - 세션 생성
    // ========================================
    @Nested
    @DisplayName("POST /api/v1/sessions/single - 세션 생성")
    class CreateSingleUploadSessionTest {

        @Test
        @DisplayName("C1-S01. 유효한 요청으로 세션을 생성하면 201과 세션 정보를 반환한다")
        void shouldCreateSessionSuccessfully() {
            // when
            Response response =
                    givenServiceAuth().body(createSessionRequest()).when().post(BASE_PATH);

            // then
            response.then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("data.sessionId", notNullValue())
                    .body("data.presignedUrl", notNullValue())
                    .body("data.s3Key", notNullValue())
                    .body("data.status", equalTo("CREATED"))
                    .body("data.expiresAt", notNullValue());

            // DB 검증
            String sessionId = response.jsonPath().getString("data.sessionId");
            assertThat(singleUploadSessionJpaRepository.findById(sessionId)).isPresent();
        }

        @Test
        @DisplayName("C1-S02. fileName 누락 시 400을 반환한다")
        void shouldReturn400WhenFileNameMissing() {
            // given
            Map<String, Object> request =
                    Map.of(
                            "contentType", "image/jpeg",
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
        @DisplayName("C1-S03. contentType 누락 시 400을 반환한다")
        void shouldReturn400WhenContentTypeMissing() {
            // given
            Map<String, Object> request =
                    Map.of(
                            "fileName", "test.jpg",
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
        @DisplayName("C1-S04. accessType 누락 시 400을 반환한다")
        void shouldReturn400WhenAccessTypeMissing() {
            // given
            Map<String, Object> request =
                    Map.of(
                            "fileName", "test.jpg",
                            "contentType", "image/jpeg",
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
        @DisplayName("C1-S05. purpose 누락 시 400을 반환한다")
        void shouldReturn400WhenPurposeMissing() {
            // given
            Map<String, Object> request =
                    Map.of(
                            "fileName", "test.jpg",
                            "contentType", "image/jpeg",
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
        @DisplayName("C1-S06. source 누락 시 400을 반환한다")
        void shouldReturn400WhenSourceMissing() {
            // given
            Map<String, Object> request =
                    Map.of(
                            "fileName", "test.jpg",
                            "contentType", "image/jpeg",
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
        @DisplayName("C1-S07. 잘못된 accessType 값 전달 시 400을 반환한다")
        void shouldReturn400WhenInvalidAccessType() {
            // given
            Map<String, Object> request =
                    Map.of(
                            "fileName", "test.jpg",
                            "contentType", "image/jpeg",
                            "accessType", "INVALID",
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
    }

    // ========================================
    // C2: POST /api/v1/sessions/single/{sessionId}/complete - 세션 완료
    // ========================================
    @Nested
    @DisplayName("POST /api/v1/sessions/single/{sessionId}/complete - 세션 완료")
    class CompleteSingleUploadSessionTest {

        @Test
        @DisplayName("C2-S01. CREATED 상태 세션을 정상적으로 완료한다")
        void shouldCompleteSessionSuccessfully() {
            // given - expiresAt을 미래로 설정하여 만료되지 않은 세션 생성
            Instant now = Instant.now();
            SingleUploadSessionJpaEntity entity =
                    singleUploadSessionJpaRepository.save(
                            SingleUploadSessionJpaEntity.create(
                                    "complete-test-session",
                                    "public/2026/01/file-complete.jpg",
                                    "fileflow-test-bucket",
                                    AccessType.PUBLIC,
                                    "product-image.jpg",
                                    "image/jpeg",
                                    "https://s3.presigned-url.com/test",
                                    "product-image",
                                    "commerce-service",
                                    SingleSessionStatus.CREATED,
                                    now.plus(Duration.ofHours(1)),
                                    now,
                                    now));

            // when
            Response response =
                    givenServiceAuth()
                            .body(completeSessionRequest())
                            .when()
                            .post(BASE_PATH + "/{sessionId}/complete", entity.getId());

            // then
            response.then().statusCode(HttpStatus.OK.value());

            // DB 검증 - 상태가 COMPLETED로 변경되었는지 확인
            var updated = singleUploadSessionJpaRepository.findById(entity.getId()).orElseThrow();
            assertThat(updated.getStatus().name()).isEqualTo("COMPLETED");
        }

        @Test
        @DisplayName("C2-S02. 존재하지 않는 세션 완료 시 404를 반환한다")
        void shouldReturn404WhenSessionNotFound() {
            // when & then
            givenServiceAuth()
                    .body(completeSessionRequest())
                    .when()
                    .post(BASE_PATH + "/{sessionId}/complete", "non-existent-id")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("code", equalTo("SESSION-001"));
        }

        @Test
        @DisplayName("C2-S03. 이미 완료된 세션 재완료 시 409를 반환한다")
        void shouldReturn409WhenSessionAlreadyCompleted() {
            // given
            SingleUploadSessionJpaEntity entity =
                    singleUploadSessionJpaRepository.save(
                            SingleUploadSessionJpaEntityFixture.aCompletedEntity());

            // when & then
            givenServiceAuth()
                    .body(completeSessionRequest())
                    .when()
                    .post(BASE_PATH + "/{sessionId}/complete", entity.getId())
                    .then()
                    .statusCode(HttpStatus.CONFLICT.value())
                    .body("code", equalTo("SESSION-002"));
        }

        @Test
        @DisplayName("C2-S04. 만료된 세션 완료 시 410을 반환한다")
        void shouldReturn410WhenSessionExpired() {
            // given
            SingleUploadSessionJpaEntity entity =
                    singleUploadSessionJpaRepository.save(
                            SingleUploadSessionJpaEntityFixture.anExpiredEntity());

            // when & then
            givenServiceAuth()
                    .body(completeSessionRequest())
                    .when()
                    .post(BASE_PATH + "/{sessionId}/complete", entity.getId())
                    .then()
                    .statusCode(HttpStatus.GONE.value())
                    .body("code", equalTo("SESSION-003"));
        }

        @Test
        @DisplayName("C2-S05. fileSize가 0 이하일 때 400을 반환한다")
        void shouldReturn400WhenFileSizeNotPositive() {
            // given
            SingleUploadSessionJpaEntity entity =
                    singleUploadSessionJpaRepository.save(
                            SingleUploadSessionJpaEntityFixture.aCreatedEntity());

            Map<String, Object> request = Map.of("fileSize", 0, "etag", "\"abc123\"");

            // when & then
            givenServiceAuth()
                    .body(request)
                    .when()
                    .post(BASE_PATH + "/{sessionId}/complete", entity.getId())
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("C2-S06. etag 누락 시 400을 반환한다")
        void shouldReturn400WhenEtagMissing() {
            // given
            SingleUploadSessionJpaEntity entity =
                    singleUploadSessionJpaRepository.save(
                            SingleUploadSessionJpaEntityFixture.aCreatedEntity());

            Map<String, Object> request = Map.of("fileSize", 1048576);

            // when & then
            givenServiceAuth()
                    .body(request)
                    .when()
                    .post(BASE_PATH + "/{sessionId}/complete", entity.getId())
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
        @DisplayName("FLOW-S01. 생성 -> 조회 -> 완료 -> 재조회 전체 플로우")
        void shouldCompleteFullCreateQueryCompleteFlow() {
            // Step 1: 세션 생성
            Response createResponse =
                    givenServiceAuth().body(createSessionRequest()).when().post(BASE_PATH);

            createResponse.then().statusCode(HttpStatus.CREATED.value());
            String sessionId = createResponse.jsonPath().getString("data.sessionId");
            assertThat(sessionId).isNotBlank();

            // Step 2: 생성된 세션 조회 - CREATED 상태
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{sessionId}", sessionId)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.sessionId", equalTo(sessionId))
                    .body("data.status", equalTo("CREATED"));

            // Step 3: 세션 완료
            givenServiceAuth()
                    .body(completeSessionRequest())
                    .when()
                    .post(BASE_PATH + "/{sessionId}/complete", sessionId)
                    .then()
                    .statusCode(HttpStatus.OK.value());

            // Step 4: 재조회 - COMPLETED 상태
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{sessionId}", sessionId)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.sessionId", equalTo(sessionId))
                    .body("data.status", equalTo("COMPLETED"));
        }

        @Test
        @DisplayName("FLOW-S02. 완료된 세션 재완료 시도 시 409를 반환한다")
        void shouldReturn409WhenCompletingAlreadyCompletedSession() {
            // Step 1: 세션 생성
            Response createResponse =
                    givenServiceAuth().body(createSessionRequest()).when().post(BASE_PATH);

            createResponse.then().statusCode(HttpStatus.CREATED.value());
            String sessionId = createResponse.jsonPath().getString("data.sessionId");

            // Step 2: 첫 번째 완료 - 성공
            givenServiceAuth()
                    .body(completeSessionRequest())
                    .when()
                    .post(BASE_PATH + "/{sessionId}/complete", sessionId)
                    .then()
                    .statusCode(HttpStatus.OK.value());

            // Step 3: 두 번째 완료 - 409 충돌
            givenServiceAuth()
                    .body(completeSessionRequest())
                    .when()
                    .post(BASE_PATH + "/{sessionId}/complete", sessionId)
                    .then()
                    .statusCode(HttpStatus.CONFLICT.value())
                    .body("code", equalTo("SESSION-002"));
        }
    }

    // ========================================
    // Helper 메서드
    // ========================================

    private Map<String, Object> createSessionRequest() {
        return Map.of(
                "fileName", "test-image.jpg",
                "contentType", "image/jpeg",
                "accessType", "PUBLIC",
                "purpose", "PRODUCT_IMAGE",
                "source", "commerce-api");
    }

    private Map<String, Object> completeSessionRequest() {
        return Map.of("fileSize", 1048576, "etag", "\"abc123\"");
    }
}
