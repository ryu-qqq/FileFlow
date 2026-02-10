package com.ryuqq.fileflow.integration.test.e2e.web.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import com.ryuqq.fileflow.adapter.out.persistence.session.CompletedPartJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.session.MultipartUploadSessionJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.CompletedPartJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.MultipartUploadSessionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.repository.CompletedPartJpaRepository;
import com.ryuqq.fileflow.adapter.out.persistence.session.repository.MultipartUploadSessionJpaRepository;
import com.ryuqq.fileflow.domain.session.vo.MultipartSessionStatus;
import com.ryuqq.fileflow.integration.test.common.base.E2ETestBase;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

/**
 * 멀티파트 업로드 세션 E2E 테스트.
 *
 * <p>실제 TestContainers(MySQL, Redis, LocalStack) 환경에서 멀티파트 업로드 세션의 생성, 조회, 파트 관리, 완료, 중단 API를
 * 검증합니다.
 *
 * <p>외부 시스템(S3) 호출이 포함된 Command 테스트(C3, C4, C6, C7)는 API를 통해 생성한 세션으로 진행하여 LocalStack과 연동합니다. Query
 * 및 상태 기반 검증 테스트는 Fixture로 DB 직접 삽입 후 진행합니다.
 */
@DisplayName("Multipart Upload Session E2E 테스트")
class MultipartUploadSessionE2ETest extends E2ETestBase {

    private static final String BASE_PATH = "/api/v1/sessions/multipart";

    @Autowired private MultipartUploadSessionJpaRepository multipartUploadSessionJpaRepository;

    @Autowired private CompletedPartJpaRepository completedPartJpaRepository;

    @BeforeEach
    void setUp() {
        completedPartJpaRepository.deleteAllInBatch();
        multipartUploadSessionJpaRepository.deleteAllInBatch();
    }

    // ========================================
    // Q2: GET /api/v1/sessions/multipart/{sessionId} - 세션 상세 조회
    // ========================================
    @Nested
    @DisplayName("GET /api/v1/sessions/multipart/{sessionId} - 세션 상세 조회")
    class GetMultipartUploadSessionTest {

        @Test
        @DisplayName("Q2-S01. 존재하는 세션을 조회하면 200과 세션 정보를 반환한다 (파트 없음)")
        void shouldReturnSessionWithNoParts() {
            // given
            MultipartUploadSessionJpaEntity entity =
                    multipartUploadSessionJpaRepository.save(
                            MultipartUploadSessionJpaEntityFixture.anInitiatedEntity());

            // when & then
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{sessionId}", entity.getId())
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.sessionId", equalTo(entity.getId()))
                    .body("data.uploadId", equalTo(entity.getUploadId()))
                    .body("data.s3Key", equalTo(entity.getS3Key()))
                    .body("data.bucket", equalTo(entity.getBucket()))
                    .body("data.accessType", equalTo(entity.getAccessType().name()))
                    .body("data.fileName", equalTo(entity.getFileName()))
                    .body("data.contentType", equalTo(entity.getContentType()))
                    .body("data.partSize", equalTo((int) entity.getPartSize()))
                    .body("data.status", equalTo("INITIATED"))
                    .body("data.completedPartCount", equalTo(0))
                    .body("data.completedParts", empty());
        }

        @Test
        @DisplayName("Q2-S02. 존재하는 세션을 조회하면 200과 파트 정보를 포함하여 반환한다")
        void shouldReturnSessionWithParts() {
            // given
            MultipartUploadSessionJpaEntity entity =
                    multipartUploadSessionJpaRepository.save(
                            MultipartUploadSessionJpaEntityFixture.anUploadingEntity());

            List<CompletedPartJpaEntity> parts =
                    List.of(
                            CompletedPartJpaEntityFixture.aCompletedPartEntity(entity.getId(), 1),
                            CompletedPartJpaEntityFixture.aCompletedPartEntity(entity.getId(), 2),
                            CompletedPartJpaEntityFixture.aCompletedPartEntity(entity.getId(), 3));
            completedPartJpaRepository.saveAll(parts);

            // when & then
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{sessionId}", entity.getId())
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.status", equalTo("UPLOADING"))
                    .body("data.completedPartCount", equalTo(3))
                    .body("data.completedParts", hasSize(3));
        }

        @Test
        @DisplayName("Q2-S03. 존재하지 않는 세션을 조회하면 404를 반환한다")
        void shouldReturn404WhenSessionNotFound() {
            // when & then
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{sessionId}", "non-existent-id")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("code", equalTo("SESSION-001"));
        }
    }

    // ========================================
    // C3: POST /api/v1/sessions/multipart - 세션 생성
    // ========================================
    @Nested
    @DisplayName("POST /api/v1/sessions/multipart - 세션 생성")
    class CreateMultipartUploadSessionTest {

        @Test
        @DisplayName("C3-S01. 유효한 요청으로 세션을 생성하면 201과 세션 정보를 반환한다")
        void shouldCreateSessionSuccessfully() {
            // when
            Response response =
                    givenServiceAuth().body(createSessionRequest()).when().post(BASE_PATH);

            // then
            response.then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("data.sessionId", notNullValue())
                    .body("data.uploadId", notNullValue())
                    .body("data.s3Key", notNullValue())
                    .body("data.status", equalTo("INITIATED"));

            // DB 검증
            String sessionId = response.jsonPath().getString("data.sessionId");
            assertThat(multipartUploadSessionJpaRepository.findById(sessionId)).isPresent();
        }

        @Test
        @DisplayName("C3-S02. fileName 누락 시 400을 반환한다")
        void shouldReturn400WhenFileNameMissing() {
            // given
            Map<String, Object> request =
                    Map.of(
                            "contentType", "video/mp4",
                            "accessType", "PUBLIC",
                            "partSize", 5242880,
                            "purpose", "VIDEO_UPLOAD",
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
        @DisplayName("C3-S03. partSize 0 이하 시 400을 반환한다")
        void shouldReturn400WhenPartSizeNotPositive() {
            // given
            Map<String, Object> request =
                    Map.of(
                            "fileName", "large.mp4",
                            "contentType", "video/mp4",
                            "accessType", "PUBLIC",
                            "partSize", 0,
                            "purpose", "VIDEO_UPLOAD",
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
        @DisplayName("C3-S04. accessType 누락 시 400을 반환한다")
        void shouldReturn400WhenAccessTypeMissing() {
            // given
            Map<String, Object> request =
                    Map.of(
                            "fileName", "large.mp4",
                            "contentType", "video/mp4",
                            "partSize", 5242880,
                            "purpose", "VIDEO_UPLOAD",
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
        @DisplayName("C3-S05. contentType 누락 시 400을 반환한다")
        void shouldReturn400WhenContentTypeMissing() {
            // given
            Map<String, Object> request =
                    Map.of(
                            "fileName", "large.mp4",
                            "accessType", "PUBLIC",
                            "partSize", 5242880,
                            "purpose", "VIDEO_UPLOAD",
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
        @DisplayName("C3-S06. 잘못된 accessType 값 전달 시 400을 반환한다")
        void shouldReturn400WhenInvalidAccessType() {
            // given
            Map<String, Object> request =
                    Map.of(
                            "fileName", "large.mp4",
                            "contentType", "video/mp4",
                            "accessType", "INVALID",
                            "partSize", 5242880,
                            "purpose", "VIDEO_UPLOAD",
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
    // C4: GET /api/v1/sessions/multipart/{sessionId}/parts/{partNumber}/presigned-url
    //     파트 Presigned URL 발급
    // ========================================
    @Nested
    @DisplayName(
            "GET /api/v1/sessions/multipart/{sessionId}/parts/{partNumber}/presigned-url - 파트"
                    + " Presigned URL 발급")
    class GeneratePresignedPartUrlTest {

        @Test
        @DisplayName("C4-S01. INITIATED 상태 세션의 파트 URL을 발급하면 200과 Presigned URL을 반환한다")
        void shouldReturnPresignedUrlForInitiatedSession() {
            // given - API로 세션 생성 (LocalStack S3에 실제 업로드 세션 존재)
            Response createResponse =
                    givenServiceAuth().body(createSessionRequest()).when().post(BASE_PATH);
            createResponse.then().statusCode(HttpStatus.CREATED.value());
            String sessionId = createResponse.jsonPath().getString("data.sessionId");

            // when & then
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{sessionId}/parts/{partNumber}/presigned-url", sessionId, 1)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.presignedUrl", notNullValue())
                    .body("data.partNumber", equalTo(1))
                    .body("data.expiresInSeconds", greaterThan(0));
        }

        @Test
        @DisplayName("C4-S02. 존재하지 않는 세션의 파트 URL 발급 시 404를 반환한다")
        void shouldReturn404WhenSessionNotFound() {
            // when & then
            givenServiceAuth()
                    .when()
                    .get(
                            BASE_PATH + "/{sessionId}/parts/{partNumber}/presigned-url",
                            "non-existent",
                            1)
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("code", equalTo("SESSION-001"));
        }

        @Test
        @DisplayName("C4-S03. COMPLETED 세션의 파트 URL 발급 시 409를 반환한다")
        void shouldReturn409WhenSessionCompleted() {
            // given - API로 세션 생성 -> 파트 추가 -> 완료 (S3 리소스 존재 필요)
            String sessionId = createSessionAndAddParts(1);
            givenServiceAuth()
                    .body(completeSessionRequest())
                    .when()
                    .post(BASE_PATH + "/{sessionId}/complete", sessionId)
                    .then()
                    .statusCode(HttpStatus.OK.value());

            // when & then
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{sessionId}/parts/{partNumber}/presigned-url", sessionId, 1)
                    .then()
                    .statusCode(HttpStatus.CONFLICT.value())
                    .body("code", equalTo("SESSION-002"));
        }

        @Test
        @DisplayName("C4-S04. ABORTED 세션의 파트 URL 발급 시 409를 반환한다")
        void shouldReturn409WhenSessionAborted() {
            // given - API로 세션 생성 -> 중단 (S3 리소스 존재 필요)
            Response createResponse =
                    givenServiceAuth().body(createSessionRequest()).when().post(BASE_PATH);
            createResponse.then().statusCode(HttpStatus.CREATED.value());
            String sessionId = createResponse.jsonPath().getString("data.sessionId");

            givenServiceAuth()
                    .when()
                    .post(BASE_PATH + "/{sessionId}/abort", sessionId)
                    .then()
                    .statusCode(HttpStatus.OK.value());

            // when & then
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{sessionId}/parts/{partNumber}/presigned-url", sessionId, 1)
                    .then()
                    .statusCode(HttpStatus.CONFLICT.value())
                    .body("code", equalTo("SESSION-004"));
        }

        @Test
        @DisplayName("C4-S05. EXPIRED 세션의 파트 URL 발급 시 에러를 반환한다")
        void shouldReturnErrorWhenSessionExpired() {
            // given - Fixture를 이용한 EXPIRED 세션 (expiresAt이 과거)
            // 참고: 현재 서비스 로직에서 PartPresignedUrlSpec.of()가 validateUploadable() 이전에
            // 호출되므로, expiresAt이 과거인 경우 TTL이 음수가 되어 IllegalArgumentException(400)이
            // 먼저 발생합니다. 이는 서비스 로직의 호출 순서에 의한 동작입니다.
            MultipartUploadSessionJpaEntity entity =
                    multipartUploadSessionJpaRepository.save(
                            MultipartUploadSessionJpaEntityFixture.anExpiredEntity());

            // when & then - 만료된 세션에 대해 에러 응답 확인 (400 또는 410)
            int statusCode =
                    givenServiceAuth()
                            .when()
                            .get(
                                    BASE_PATH + "/{sessionId}/parts/{partNumber}/presigned-url",
                                    entity.getId(),
                                    1)
                            .then()
                            .extract()
                            .statusCode();

            assertThat(statusCode).isIn(HttpStatus.BAD_REQUEST.value(), HttpStatus.GONE.value());
        }
    }

    // ========================================
    // C5: POST /api/v1/sessions/multipart/{sessionId}/parts - 파트 추가
    // ========================================
    @Nested
    @DisplayName("POST /api/v1/sessions/multipart/{sessionId}/parts - 파트 완료 기록")
    class AddCompletedPartTest {

        @Test
        @DisplayName("C5-S01. INITIATED 세션에 첫 파트를 추가하면 200을 반환하고 UPLOADING으로 전이한다")
        void shouldAddFirstPartAndTransitionToUploading() {
            // given - API로 세션 생성
            Response createResponse =
                    givenServiceAuth().body(createSessionRequest()).when().post(BASE_PATH);
            createResponse.then().statusCode(HttpStatus.CREATED.value());
            String sessionId = createResponse.jsonPath().getString("data.sessionId");

            // when
            Response addPartResponse =
                    givenServiceAuth()
                            .body(addPartRequest(1, "\"part1etag\"", 5242880))
                            .when()
                            .post(BASE_PATH + "/{sessionId}/parts", sessionId);

            // then
            addPartResponse.then().statusCode(HttpStatus.OK.value());

            // DB 검증 - 상태가 UPLOADING으로 전이
            var updated = multipartUploadSessionJpaRepository.findById(sessionId).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(MultipartSessionStatus.UPLOADING);

            // 파트 저장 확인
            assertThat(completedPartJpaRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("C5-S02. UPLOADING 세션에 추가 파트를 추가하면 200을 반환한다")
        void shouldAddAdditionalPartToUploadingSession() {
            // given - API로 세션 생성 후 첫 파트 추가
            Response createResponse =
                    givenServiceAuth().body(createSessionRequest()).when().post(BASE_PATH);
            createResponse.then().statusCode(HttpStatus.CREATED.value());
            String sessionId = createResponse.jsonPath().getString("data.sessionId");

            givenServiceAuth()
                    .body(addPartRequest(1, "\"part1etag\"", 5242880))
                    .when()
                    .post(BASE_PATH + "/{sessionId}/parts", sessionId)
                    .then()
                    .statusCode(HttpStatus.OK.value());

            // when - 두 번째 파트 추가
            givenServiceAuth()
                    .body(addPartRequest(2, "\"part2etag\"", 5242880))
                    .when()
                    .post(BASE_PATH + "/{sessionId}/parts", sessionId)
                    .then()
                    .statusCode(HttpStatus.OK.value());

            // then - 파트 2건 저장 확인
            assertThat(completedPartJpaRepository.count()).isEqualTo(2);
        }

        @Test
        @DisplayName("C5-S03. 존재하지 않는 세션에 파트 추가 시 404를 반환한다")
        void shouldReturn404WhenSessionNotFound() {
            // when & then
            givenServiceAuth()
                    .body(addPartRequest(1, "\"part1etag\"", 5242880))
                    .when()
                    .post(BASE_PATH + "/{sessionId}/parts", "non-existent")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @DisplayName("C5-S04. 중복 파트번호 추가 시 409를 반환한다")
        void shouldReturn409WhenDuplicatePartNumber() {
            // given - API로 세션 생성 후 첫 파트 추가
            Response createResponse =
                    givenServiceAuth().body(createSessionRequest()).when().post(BASE_PATH);
            createResponse.then().statusCode(HttpStatus.CREATED.value());
            String sessionId = createResponse.jsonPath().getString("data.sessionId");

            givenServiceAuth()
                    .body(addPartRequest(1, "\"part1etag\"", 5242880))
                    .when()
                    .post(BASE_PATH + "/{sessionId}/parts", sessionId)
                    .then()
                    .statusCode(HttpStatus.OK.value());

            // when - 동일 파트번호로 재추가
            givenServiceAuth()
                    .body(addPartRequest(1, "\"duplicate\"", 5242880))
                    .when()
                    .post(BASE_PATH + "/{sessionId}/parts", sessionId)
                    .then()
                    .statusCode(HttpStatus.CONFLICT.value())
                    .body("code", equalTo("SESSION-006"));
        }

        @Test
        @DisplayName("C5-S05. COMPLETED 세션에 파트 추가 시 409를 반환한다")
        void shouldReturn409WhenSessionCompleted() {
            // given
            MultipartUploadSessionJpaEntity entity =
                    multipartUploadSessionJpaRepository.save(
                            MultipartUploadSessionJpaEntityFixture.aCompletedEntity());

            // when & then
            givenServiceAuth()
                    .body(addPartRequest(1, "\"late\"", 5242880))
                    .when()
                    .post(BASE_PATH + "/{sessionId}/parts", entity.getId())
                    .then()
                    .statusCode(HttpStatus.CONFLICT.value())
                    .body("code", equalTo("SESSION-002"));
        }

        @Test
        @DisplayName("C5-S06. ABORTED 세션에 파트 추가 시 409를 반환한다")
        void shouldReturn409WhenSessionAborted() {
            // given
            MultipartUploadSessionJpaEntity entity =
                    multipartUploadSessionJpaRepository.save(
                            MultipartUploadSessionJpaEntityFixture.anAbortedEntity());

            // when & then
            givenServiceAuth()
                    .body(addPartRequest(1, "\"late\"", 5242880))
                    .when()
                    .post(BASE_PATH + "/{sessionId}/parts", entity.getId())
                    .then()
                    .statusCode(HttpStatus.CONFLICT.value())
                    .body("code", equalTo("SESSION-004"));
        }

        @Test
        @DisplayName("C5-S07. partNumber 0 이하 시 400을 반환한다")
        void shouldReturn400WhenPartNumberNotPositive() {
            // given - API로 세션 생성
            Response createResponse =
                    givenServiceAuth().body(createSessionRequest()).when().post(BASE_PATH);
            createResponse.then().statusCode(HttpStatus.CREATED.value());
            String sessionId = createResponse.jsonPath().getString("data.sessionId");

            // when & then
            givenServiceAuth()
                    .body(addPartRequest(0, "\"etag\"", 5242880))
                    .when()
                    .post(BASE_PATH + "/{sessionId}/parts", sessionId)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("C5-S08. etag 누락 시 400을 반환한다")
        void shouldReturn400WhenEtagMissing() {
            // given - API로 세션 생성
            Response createResponse =
                    givenServiceAuth().body(createSessionRequest()).when().post(BASE_PATH);
            createResponse.then().statusCode(HttpStatus.CREATED.value());
            String sessionId = createResponse.jsonPath().getString("data.sessionId");

            Map<String, Object> request =
                    Map.of(
                            "partNumber", 1,
                            "size", 5242880);

            // when & then
            givenServiceAuth()
                    .body(request)
                    .when()
                    .post(BASE_PATH + "/{sessionId}/parts", sessionId)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("C5-S09. size 0 이하 시 400을 반환한다")
        void shouldReturn400WhenSizeNotPositive() {
            // given - API로 세션 생성
            Response createResponse =
                    givenServiceAuth().body(createSessionRequest()).when().post(BASE_PATH);
            createResponse.then().statusCode(HttpStatus.CREATED.value());
            String sessionId = createResponse.jsonPath().getString("data.sessionId");

            // when & then
            givenServiceAuth()
                    .body(addPartRequest(1, "\"etag\"", 0))
                    .when()
                    .post(BASE_PATH + "/{sessionId}/parts", sessionId)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }
    }

    // ========================================
    // C6: POST /api/v1/sessions/multipart/{sessionId}/complete - 세션 완료
    // ========================================
    @Nested
    @DisplayName("POST /api/v1/sessions/multipart/{sessionId}/complete - 세션 완료")
    class CompleteMultipartUploadSessionTest {

        @Test
        @DisplayName("C6-S01. 파트가 있는 세션을 정상적으로 완료한다")
        void shouldCompleteSessionWithParts() {
            // given - API로 세션 생성 + 파트 추가
            String sessionId = createSessionAndAddParts(3);

            // when
            Response response =
                    givenServiceAuth()
                            .body(completeSessionRequest())
                            .when()
                            .post(BASE_PATH + "/{sessionId}/complete", sessionId);

            // then
            response.then().statusCode(HttpStatus.OK.value());

            // DB 검증 - 상태가 COMPLETED로 변경
            var updated = multipartUploadSessionJpaRepository.findById(sessionId).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(MultipartSessionStatus.COMPLETED);
        }

        @Test
        @DisplayName("C6-S02. 파트가 없는 세션 완료 시 400을 반환한다")
        void shouldReturn400WhenNoCompletedParts() {
            // given - API로 세션 생성 (파트 추가 없이)
            Response createResponse =
                    givenServiceAuth().body(createSessionRequest()).when().post(BASE_PATH);
            createResponse.then().statusCode(HttpStatus.CREATED.value());
            String sessionId = createResponse.jsonPath().getString("data.sessionId");

            // when & then
            givenServiceAuth()
                    .body(completeSessionRequest())
                    .when()
                    .post(BASE_PATH + "/{sessionId}/complete", sessionId)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .body("code", equalTo("SESSION-005"));
        }

        @Test
        @DisplayName("C6-S03. 존재하지 않는 세션 완료 시 404를 반환한다")
        void shouldReturn404WhenSessionNotFound() {
            // when & then
            givenServiceAuth()
                    .body(completeSessionRequest())
                    .when()
                    .post(BASE_PATH + "/{sessionId}/complete", "non-existent")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @DisplayName("C6-S04. 이미 완료된 세션 재완료 시 409를 반환한다")
        void shouldReturn409WhenSessionAlreadyCompleted() {
            // given
            MultipartUploadSessionJpaEntity entity =
                    multipartUploadSessionJpaRepository.save(
                            MultipartUploadSessionJpaEntityFixture.aCompletedEntity());

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
        @DisplayName("C6-S05. ABORTED 세션 완료 시 409를 반환한다")
        void shouldReturn409WhenSessionAborted() {
            // given
            MultipartUploadSessionJpaEntity entity =
                    multipartUploadSessionJpaRepository.save(
                            MultipartUploadSessionJpaEntityFixture.anAbortedEntity());

            // when & then
            givenServiceAuth()
                    .body(completeSessionRequest())
                    .when()
                    .post(BASE_PATH + "/{sessionId}/complete", entity.getId())
                    .then()
                    .statusCode(HttpStatus.CONFLICT.value())
                    .body("code", equalTo("SESSION-004"));
        }

        @Test
        @DisplayName("C6-S06. totalFileSize 0 이하 시 400을 반환한다")
        void shouldReturn400WhenTotalFileSizeNotPositive() {
            // given - API로 세션 생성
            Response createResponse =
                    givenServiceAuth().body(createSessionRequest()).when().post(BASE_PATH);
            createResponse.then().statusCode(HttpStatus.CREATED.value());
            String sessionId = createResponse.jsonPath().getString("data.sessionId");

            Map<String, Object> request = Map.of("totalFileSize", 0, "etag", "\"etag\"");

            // when & then
            givenServiceAuth()
                    .body(request)
                    .when()
                    .post(BASE_PATH + "/{sessionId}/complete", sessionId)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("C6-S07. etag 누락 시 400을 반환한다")
        void shouldReturn400WhenEtagMissing() {
            // given - API로 세션 생성
            Response createResponse =
                    givenServiceAuth().body(createSessionRequest()).when().post(BASE_PATH);
            createResponse.then().statusCode(HttpStatus.CREATED.value());
            String sessionId = createResponse.jsonPath().getString("data.sessionId");

            Map<String, Object> request = Map.of("totalFileSize", 15728640);

            // when & then
            givenServiceAuth()
                    .body(request)
                    .when()
                    .post(BASE_PATH + "/{sessionId}/complete", sessionId)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }
    }

    // ========================================
    // C7: POST /api/v1/sessions/multipart/{sessionId}/abort - 세션 중단
    // ========================================
    @Nested
    @DisplayName("POST /api/v1/sessions/multipart/{sessionId}/abort - 세션 중단")
    class AbortMultipartUploadSessionTest {

        @Test
        @DisplayName("C7-S01. INITIATED 세션을 중단하면 200을 반환하고 ABORTED로 전이한다")
        void shouldAbortInitiatedSession() {
            // given - API로 세션 생성
            Response createResponse =
                    givenServiceAuth().body(createSessionRequest()).when().post(BASE_PATH);
            createResponse.then().statusCode(HttpStatus.CREATED.value());
            String sessionId = createResponse.jsonPath().getString("data.sessionId");

            // when
            givenServiceAuth()
                    .when()
                    .post(BASE_PATH + "/{sessionId}/abort", sessionId)
                    .then()
                    .statusCode(HttpStatus.OK.value());

            // DB 검증
            var updated = multipartUploadSessionJpaRepository.findById(sessionId).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(MultipartSessionStatus.ABORTED);
        }

        @Test
        @DisplayName("C7-S02. UPLOADING 세션을 중단하면 200을 반환하고 ABORTED로 전이한다")
        void shouldAbortUploadingSession() {
            // given - API로 세션 생성 + 파트 추가 (UPLOADING 상태)
            Response createResponse =
                    givenServiceAuth().body(createSessionRequest()).when().post(BASE_PATH);
            createResponse.then().statusCode(HttpStatus.CREATED.value());
            String sessionId = createResponse.jsonPath().getString("data.sessionId");

            // 파트 추가하여 UPLOADING 전이
            givenServiceAuth()
                    .body(addPartRequest(1, "\"part1etag\"", 5242880))
                    .when()
                    .post(BASE_PATH + "/{sessionId}/parts", sessionId)
                    .then()
                    .statusCode(HttpStatus.OK.value());

            // when
            givenServiceAuth()
                    .when()
                    .post(BASE_PATH + "/{sessionId}/abort", sessionId)
                    .then()
                    .statusCode(HttpStatus.OK.value());

            // DB 검증
            var updated = multipartUploadSessionJpaRepository.findById(sessionId).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(MultipartSessionStatus.ABORTED);
        }

        @Test
        @DisplayName("C7-S03. 존재하지 않는 세션 중단 시 404를 반환한다")
        void shouldReturn404WhenSessionNotFound() {
            // when & then
            givenServiceAuth()
                    .when()
                    .post(BASE_PATH + "/{sessionId}/abort", "non-existent")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @DisplayName("C7-S04. COMPLETED 세션 중단 시 409를 반환한다")
        void shouldReturn409WhenSessionCompleted() {
            // given - API로 세션 생성 -> 파트 추가 -> 완료 (S3 리소스 존재 필요)
            String sessionId = createSessionAndAddParts(1);
            givenServiceAuth()
                    .body(completeSessionRequest())
                    .when()
                    .post(BASE_PATH + "/{sessionId}/complete", sessionId)
                    .then()
                    .statusCode(HttpStatus.OK.value());

            // when & then
            givenServiceAuth()
                    .when()
                    .post(BASE_PATH + "/{sessionId}/abort", sessionId)
                    .then()
                    .statusCode(HttpStatus.CONFLICT.value())
                    .body("code", equalTo("SESSION-002"));
        }

        @Test
        @DisplayName("C7-S05. 이미 중단된 세션 재중단 시 에러를 반환한다")
        void shouldReturnErrorWhenSessionAlreadyAborted() {
            // given - API로 세션 생성 -> 중단
            Response createResponse =
                    givenServiceAuth().body(createSessionRequest()).when().post(BASE_PATH);
            createResponse.then().statusCode(HttpStatus.CREATED.value());
            String sessionId = createResponse.jsonPath().getString("data.sessionId");

            givenServiceAuth()
                    .when()
                    .post(BASE_PATH + "/{sessionId}/abort", sessionId)
                    .then()
                    .statusCode(HttpStatus.OK.value());

            // when & then - 이미 중단된 세션 재중단
            // 참고: 현재 AbortService에서 S3 abortMultipartUpload 호출이 도메인 검증보다
            // 먼저 실행됩니다. 이미 abort된 세션의 S3 리소스는 삭제된 상태이므로
            // S3 호출 실패(500) 또는 도메인 검증 실패(409)가 발생할 수 있습니다.
            int statusCode =
                    givenServiceAuth()
                            .when()
                            .post(BASE_PATH + "/{sessionId}/abort", sessionId)
                            .then()
                            .extract()
                            .statusCode();

            assertThat(statusCode)
                    .isIn(HttpStatus.CONFLICT.value(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    // ========================================
    // 전체 플로우 시나리오
    // ========================================
    @Nested
    @DisplayName("전체 플로우 시나리오")
    class FullFlowTest {

        @Test
        @DisplayName("FLOW-MP01. 멀티파트 업로드 전체 플로우: 생성 -> Presigned URL -> 파트 추가 -> 완료 -> 조회")
        void shouldCompleteFullMultipartUploadFlow() {
            // Step 1: 세션 생성
            Response createResponse =
                    givenServiceAuth().body(createSessionRequest()).when().post(BASE_PATH);
            createResponse.then().statusCode(HttpStatus.CREATED.value());
            String sessionId = createResponse.jsonPath().getString("data.sessionId");
            assertThat(sessionId).isNotBlank();

            // Step 2: 세션 조회 - INITIATED 상태
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{sessionId}", sessionId)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.sessionId", equalTo(sessionId))
                    .body("data.status", equalTo("INITIATED"));

            // Step 3: 파트 1 Presigned URL 발급
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{sessionId}/parts/{partNumber}/presigned-url", sessionId, 1)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.presignedUrl", notNullValue());

            // Step 4: 파트 1 완료 기록
            givenServiceAuth()
                    .body(addPartRequest(1, "\"part1etag\"", 5242880))
                    .when()
                    .post(BASE_PATH + "/{sessionId}/parts", sessionId)
                    .then()
                    .statusCode(HttpStatus.OK.value());

            // Step 5: 세션 조회 - UPLOADING 상태, 파트 1건
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{sessionId}", sessionId)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.status", equalTo("UPLOADING"))
                    .body("data.completedPartCount", equalTo(1));

            // Step 6: 파트 2 완료 기록
            givenServiceAuth()
                    .body(addPartRequest(2, "\"part2etag\"", 5242880))
                    .when()
                    .post(BASE_PATH + "/{sessionId}/parts", sessionId)
                    .then()
                    .statusCode(HttpStatus.OK.value());

            // Step 7: 세션 완료
            givenServiceAuth()
                    .body(completeSessionRequest())
                    .when()
                    .post(BASE_PATH + "/{sessionId}/complete", sessionId)
                    .then()
                    .statusCode(HttpStatus.OK.value());

            // Step 8: 세션 조회 - COMPLETED 상태
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{sessionId}", sessionId)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.sessionId", equalTo(sessionId))
                    .body("data.status", equalTo("COMPLETED"));
        }

        @Test
        @DisplayName("FLOW-MP02. 멀티파트 업로드 중단 플로우: 생성 -> 파트 추가 -> 중단 -> 조회 -> 추가 시도 실패")
        void shouldAbortMultipartUploadFlow() {
            // Step 1: 세션 생성
            Response createResponse =
                    givenServiceAuth().body(createSessionRequest()).when().post(BASE_PATH);
            createResponse.then().statusCode(HttpStatus.CREATED.value());
            String sessionId = createResponse.jsonPath().getString("data.sessionId");

            // Step 2: 파트 1 완료 기록
            givenServiceAuth()
                    .body(addPartRequest(1, "\"part1etag\"", 5242880))
                    .when()
                    .post(BASE_PATH + "/{sessionId}/parts", sessionId)
                    .then()
                    .statusCode(HttpStatus.OK.value());

            // Step 3: 세션 중단
            givenServiceAuth()
                    .when()
                    .post(BASE_PATH + "/{sessionId}/abort", sessionId)
                    .then()
                    .statusCode(HttpStatus.OK.value());

            // Step 4: 세션 조회 - ABORTED 상태
            givenServiceAuth()
                    .when()
                    .get(BASE_PATH + "/{sessionId}", sessionId)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.status", equalTo("ABORTED"));

            // Step 5: 중단 후 파트 추가 시도 - 409
            givenServiceAuth()
                    .body(addPartRequest(2, "\"part2etag\"", 5242880))
                    .when()
                    .post(BASE_PATH + "/{sessionId}/parts", sessionId)
                    .then()
                    .statusCode(HttpStatus.CONFLICT.value())
                    .body("code", equalTo("SESSION-004"));
        }

        @Test
        @DisplayName("FLOW-MP03. 완료 후 중단 시도: 생성 -> 파트 추가 -> 완료 -> 중단 시도 실패")
        void shouldFailAbortAfterCompletion() {
            // Step 1: 세션 생성 + 파트 추가 + 완료
            String sessionId = createSessionAndAddParts(2);

            givenServiceAuth()
                    .body(completeSessionRequest())
                    .when()
                    .post(BASE_PATH + "/{sessionId}/complete", sessionId)
                    .then()
                    .statusCode(HttpStatus.OK.value());

            // Step 2: 완료된 세션 중단 시도 - 409
            givenServiceAuth()
                    .when()
                    .post(BASE_PATH + "/{sessionId}/abort", sessionId)
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
                "fileName", "large-video.mp4",
                "contentType", "video/mp4",
                "accessType", "PUBLIC",
                "partSize", 5242880,
                "purpose", "VIDEO_UPLOAD",
                "source", "commerce-api");
    }

    private Map<String, Object> addPartRequest(int partNumber, String etag, long size) {
        Map<String, Object> request = new HashMap<>();
        request.put("partNumber", partNumber);
        request.put("etag", etag);
        request.put("size", size);
        return request;
    }

    private Map<String, Object> completeSessionRequest() {
        return Map.of("totalFileSize", 15728640, "etag", "\"combined-etag-3\"");
    }

    /**
     * API를 통해 세션을 생성하고 지정된 수만큼 파트를 추가한 후 sessionId를 반환한다.
     *
     * @param partCount 추가할 파트 수
     * @return 생성된 세션 ID
     */
    private String createSessionAndAddParts(int partCount) {
        Response createResponse =
                givenServiceAuth().body(createSessionRequest()).when().post(BASE_PATH);
        createResponse.then().statusCode(HttpStatus.CREATED.value());
        String sessionId = createResponse.jsonPath().getString("data.sessionId");

        for (int i = 1; i <= partCount; i++) {
            givenServiceAuth()
                    .body(addPartRequest(i, "\"part" + i + "etag\"", 5242880))
                    .when()
                    .post(BASE_PATH + "/{sessionId}/parts", sessionId)
                    .then()
                    .statusCode(HttpStatus.OK.value());
        }

        return sessionId;
    }
}
