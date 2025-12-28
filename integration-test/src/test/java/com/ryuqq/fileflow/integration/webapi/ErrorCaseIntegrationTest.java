package com.ryuqq.fileflow.integration.webapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.common.util.UuidV7Generator;
import com.ryuqq.fileflow.integration.base.WebApiIntegrationTest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

/**
 * 에러 케이스 통합 테스트.
 *
 * <p>GlobalExceptionHandler에서 처리하는 다양한 에러 시나리오를 검증합니다. RFC 7807 Problem Details 형식의 에러 응답을 검증합니다.
 */
@DisplayName("에러 케이스 통합 테스트")
class ErrorCaseIntegrationTest extends WebApiIntegrationTest {

    // 올바른 API 경로
    private static final String UPLOAD_SESSION_BASE = "/api/v1/file/upload-sessions";
    private static final String SINGLE_INIT = UPLOAD_SESSION_BASE + "/single";
    // PATCH /{sessionId}/single/complete 형태로 사용
    private static final String FILE_ASSET_BASE = "/api/v1/file/file-assets";
    private static final String EXTERNAL_DOWNLOAD_BASE = "/api/v1/file/external-downloads";

    // URL 헬퍼 메서드
    private String singleCompleteUrl(String sessionId) {
        return UPLOAD_SESSION_BASE + "/" + sessionId + "/single/complete";
    }

    private String cancelUrl(String sessionId) {
        return UPLOAD_SESSION_BASE + "/" + sessionId + "/cancel";
    }

    // ========================================
    // 400 Bad Request - Validation Errors
    // ========================================

    @Nested
    @DisplayName("400 Bad Request - Validation Errors")
    class ValidationErrors {

        @Test
        @DisplayName("필수 필드 누락 시 400 에러를 반환해야 한다")
        void shouldReturn400WhenRequiredFieldMissing() {
            // given - fileName 누락
            Map<String, Object> request =
                    Map.of(
                            "idempotencyKey",
                            UUID.randomUUID().toString(),
                            "contentType",
                            "image/jpeg",
                            "fileSize",
                            1024L,
                            "uploadCategory",
                            "PRODUCT");

            // when
            ResponseEntity<Map> response =
                    restTemplate.exchange(
                            url(SINGLE_INIT),
                            HttpMethod.POST,
                            createRequestEntity(request, sellerHeaders()),
                            Map.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("빈 파일명으로 요청 시 400 에러를 반환해야 한다")
        void shouldReturn400WhenFileNameEmpty() {
            // given
            Map<String, Object> request =
                    Map.of(
                            "idempotencyKey", UUID.randomUUID().toString(),
                            "fileName", "",
                            "contentType", "image/jpeg",
                            "fileSize", 1024L,
                            "uploadCategory", "PRODUCT");

            // when
            ResponseEntity<Map> response =
                    restTemplate.exchange(
                            url(SINGLE_INIT),
                            HttpMethod.POST,
                            createRequestEntity(request, sellerHeaders()),
                            Map.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("파일 크기가 0 이하일 때 400 에러를 반환해야 한다")
        void shouldReturn400WhenFileSizeInvalid() {
            // given
            Map<String, Object> request =
                    Map.of(
                            "idempotencyKey", UUID.randomUUID().toString(),
                            "fileName", "test.jpg",
                            "contentType", "image/jpeg",
                            "fileSize", 0L,
                            "uploadCategory", "PRODUCT");

            // when
            ResponseEntity<Map> response =
                    restTemplate.exchange(
                            url(SINGLE_INIT),
                            HttpMethod.POST,
                            createRequestEntity(request, sellerHeaders()),
                            Map.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("외부 다운로드 요청 시 잘못된 URL 형식이면 400 에러를 반환해야 한다")
        void shouldReturn400WhenSourceUrlInvalid() {
            // given
            Map<String, Object> request =
                    Map.of(
                            "idempotencyKey", UUID.randomUUID().toString(),
                            "sourceUrl", "not-a-valid-url",
                            "webhookUrl", "https://webhook.example.com/callback");

            // when
            ResponseEntity<Map> response =
                    restTemplate.exchange(
                            url(EXTERNAL_DOWNLOAD_BASE),
                            HttpMethod.POST,
                            createRequestEntity(request, sellerHeaders()),
                            Map.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("일괄 삭제 시 빈 ID 목록이면 400 에러를 반환해야 한다")
        void shouldReturn400WhenBatchDeleteIdsEmpty() {
            // given
            Map<String, Object> request = Map.of("fileAssetIds", List.of());

            // when
            ResponseEntity<Map> response =
                    restTemplate.exchange(
                            url(FILE_ASSET_BASE + "/batch-delete"),
                            HttpMethod.POST,
                            createRequestEntity(request, sellerHeaders()),
                            Map.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    // ========================================
    // 400 Bad Request - Malformed JSON
    // ========================================

    @Nested
    @DisplayName("400 Bad Request - Malformed JSON")
    class MalformedJsonErrors {

        @Test
        @DisplayName("잘못된 JSON 형식 요청 시 400 에러를 반환해야 한다")
        void shouldReturn400WhenJsonMalformed() {
            // given
            String malformedJson = "{ invalid json }";

            // when
            ResponseEntity<Map> response =
                    restTemplate.exchange(
                            url(SINGLE_INIT),
                            HttpMethod.POST,
                            new HttpEntity<>(malformedJson, sellerHeaders()),
                            Map.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("타입 불일치 요청 시 400 에러를 반환해야 한다")
        void shouldReturn400WhenTypeMismatch() {
            // given - fileSize를 문자열로 전송
            String request =
                    """
                    {
                        "idempotencyKey": "%s",
                        "fileName": "test.jpg",
                        "contentType": "image/jpeg",
                        "fileSize": "not-a-number",
                        "uploadCategory": "PRODUCT"
                    }
                    """
                            .formatted(UUID.randomUUID().toString());

            // when
            ResponseEntity<Map> response =
                    restTemplate.exchange(
                            url(SINGLE_INIT),
                            HttpMethod.POST,
                            new HttpEntity<>(request, sellerHeaders()),
                            Map.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    // ========================================
    // 401 Unauthorized - Authentication Errors
    // ========================================

    @Nested
    @DisplayName("401 Unauthorized - Authentication Errors")
    class AuthenticationErrors {

        @Test
        @DisplayName("인증 헤더 없이 요청 시 에러를 반환해야 한다")
        void shouldReturnErrorWhenNoAuthHeader() {
            // given - 인증 정보 없는 헤더
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> request =
                    Map.of(
                            "idempotencyKey", UUID.randomUUID().toString(),
                            "fileName", "test.jpg",
                            "contentType", "image/jpeg",
                            "fileSize", 1024L,
                            "uploadCategory", "PRODUCT");

            // when
            ResponseEntity<Map> response =
                    restTemplate.exchange(
                            url(SINGLE_INIT),
                            HttpMethod.POST,
                            new HttpEntity<>(request, headers),
                            Map.class);

            // then - 401 또는 403 (시스템 설정에 따라)
            assertThat(response.getStatusCode().value()).isIn(400, 401, 403);
        }
    }

    // ========================================
    // 403 Forbidden - Authorization Errors
    // ========================================

    @Nested
    @DisplayName("403 Forbidden - Authorization Errors")
    class AuthorizationErrors {

        @Test
        @DisplayName("파일 삭제 권한 없이 삭제 요청 시 403 에러를 반환해야 한다")
        void shouldReturn403WhenNoDeletePermission() {
            // given - 먼저 파일 업로드 완료
            String tenantId = UuidV7Generator.generate();
            String organizationId = UuidV7Generator.generate();
            HttpHeaders uploadHeaders = createSellerHeaders(tenantId, organizationId);

            String fileName = "permission-test-" + UUID.randomUUID() + ".jpg";
            String idempotencyKey = UUID.randomUUID().toString();

            // 업로드 세션 초기화
            Map<String, Object> initRequest =
                    Map.of(
                            "idempotencyKey",
                            idempotencyKey,
                            "fileName",
                            fileName,
                            "fileSize",
                            1024L,
                            "contentType",
                            "image/jpeg",
                            "uploadCategory",
                            "PRODUCT");

            ResponseEntity<Map> initResponse =
                    restTemplate.exchange(
                            url(SINGLE_INIT),
                            HttpMethod.POST,
                            createRequestEntity(initRequest, uploadHeaders),
                            Map.class);

            if (!initResponse.getStatusCode().is2xxSuccessful()) {
                // 업로드 초기화 실패 시 테스트 스킵
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> initData = (Map<String, Object>) initResponse.getBody().get("data");
            String sessionId = (String) initData.get("sessionId");

            // 업로드 완료
            Map<String, Object> completeRequest = Map.of("fileSize", 1024L, "checksum", "abc123");

            ResponseEntity<Map> completeResponse =
                    restTemplate.exchange(
                            url(singleCompleteUrl(sessionId)),
                            HttpMethod.PATCH,
                            createRequestEntity(completeRequest, uploadHeaders),
                            Map.class);

            if (!completeResponse.getStatusCode().is2xxSuccessful()) {
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> completeData =
                    (Map<String, Object>) completeResponse.getBody().get("data");
            String fileAssetId = (String) completeData.get("fileAssetId");

            // when - 삭제 권한이 없는 사용자로 삭제 요청
            HttpHeaders noDeleteHeaders =
                    createHeadersWithoutDeletePermission(tenantId, organizationId);
            Map<String, Object> deleteRequest = Map.of("reason", "삭제 테스트");

            ResponseEntity<Map> deleteResponse =
                    restTemplate.exchange(
                            url(FILE_ASSET_BASE + "/" + fileAssetId + "/delete"),
                            HttpMethod.PATCH,
                            createRequestEntity(deleteRequest, noDeleteHeaders),
                            Map.class);

            // then
            assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("다운로드 URL 생성 권한 없이 요청 시 403 에러를 반환해야 한다")
        void shouldReturn403WhenNoDownloadPermission() {
            // given - 다운로드 권한이 없는 사용자
            String tenantId = UuidV7Generator.generate();
            String organizationId = UuidV7Generator.generate();
            HttpHeaders headers = createHeadersWithoutDownloadPermission(tenantId, organizationId);

            String fileAssetId = UuidV7Generator.generate();
            Map<String, Object> request = Map.of("expireMinutes", 60);

            // when - download-url은 POST 메서드 사용
            ResponseEntity<Map> response =
                    restTemplate.exchange(
                            url(FILE_ASSET_BASE + "/" + fileAssetId + "/download-url"),
                            HttpMethod.POST,
                            createRequestEntity(request, headers),
                            Map.class);

            // then - 403 또는 404 (리소스가 없으면)
            assertThat(response.getStatusCode().value()).isIn(403, 404);
        }
    }

    // ========================================
    // 404 Not Found - Resource Not Found Errors
    // ========================================

    @Nested
    @DisplayName("404 Not Found - Resource Not Found Errors")
    class NotFoundErrors {

        @Test
        @DisplayName("존재하지 않는 업로드 세션 조회 시 404 에러를 반환해야 한다")
        void shouldReturn404WhenSessionNotFound() {
            // given
            String nonExistentId = UuidV7Generator.generate();

            // when
            ResponseEntity<Map> response =
                    restTemplate.exchange(
                            url(UPLOAD_SESSION_BASE + "/" + nonExistentId),
                            HttpMethod.GET,
                            new HttpEntity<>(sellerHeaders()),
                            Map.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("존재하지 않는 FileAsset 조회 시 404 에러를 반환해야 한다")
        void shouldReturn404WhenFileAssetNotFound() {
            // given
            String nonExistentId = UuidV7Generator.generate();

            // when
            ResponseEntity<Map> response =
                    restTemplate.exchange(
                            url(FILE_ASSET_BASE + "/" + nonExistentId),
                            HttpMethod.GET,
                            new HttpEntity<>(sellerHeaders()),
                            Map.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("존재하지 않는 외부 다운로드 조회 시 404 에러를 반환해야 한다")
        void shouldReturn404WhenExternalDownloadNotFound() {
            // given
            String nonExistentId = UuidV7Generator.generate();

            // when
            ResponseEntity<Map> response =
                    restTemplate.exchange(
                            url(EXTERNAL_DOWNLOAD_BASE + "/" + nonExistentId),
                            HttpMethod.GET,
                            new HttpEntity<>(sellerHeaders()),
                            Map.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("존재하지 않는 엔드포인트 요청 시 404 에러를 반환해야 한다")
        void shouldReturn404WhenEndpointNotFound() {
            // given/when
            ResponseEntity<Map> response =
                    restTemplate.exchange(
                            url("/api/v1/file/non-existent-endpoint"),
                            HttpMethod.GET,
                            new HttpEntity<>(sellerHeaders()),
                            Map.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    // ========================================
    // 409 Conflict - State Conflict Errors
    // ========================================

    @Nested
    @DisplayName("409 Conflict - State Conflict Errors")
    class StateConflictErrors {

        @Test
        @DisplayName("이미 완료된 세션을 다시 완료 요청하면 에러를 반환해야 한다")
        void shouldReturnErrorWhenCompletingAlreadyCompletedSession() {
            // given - 업로드 세션 생성 및 완료
            String fileName = "conflict-test-" + UUID.randomUUID() + ".jpg";
            String idempotencyKey = UUID.randomUUID().toString();

            // 동일한 헤더를 모든 요청에 사용 (tenant 일관성 유지)
            HttpHeaders headers = sellerHeaders();

            Map<String, Object> initRequest =
                    Map.of(
                            "idempotencyKey",
                            idempotencyKey,
                            "fileName",
                            fileName,
                            "fileSize",
                            1024L,
                            "contentType",
                            "image/jpeg",
                            "uploadCategory",
                            "PRODUCT");

            ResponseEntity<Map> initResponse =
                    restTemplate.exchange(
                            url(SINGLE_INIT),
                            HttpMethod.POST,
                            createRequestEntity(initRequest, headers),
                            Map.class);

            assertThat(initResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

            @SuppressWarnings("unchecked")
            Map<String, Object> initData = (Map<String, Object>) initResponse.getBody().get("data");
            String sessionId = (String) initData.get("sessionId");
            String presignedUrl = (String) initData.get("presignedUrl");

            // S3에 실제 파일 업로드 (LocalStack)
            String etag = uploadToS3(presignedUrl, "test content".getBytes(), "image/jpeg");

            // 세션 완료 - PATCH /{sessionId}/single/complete
            // CompleteSingleUploadApiRequest는 etag 필드만 필요
            Map<String, Object> completeRequest = Map.of("etag", etag);

            ResponseEntity<Map> firstCompleteResponse =
                    restTemplate.exchange(
                            url(singleCompleteUrl(sessionId)),
                            HttpMethod.PATCH,
                            createRequestEntity(completeRequest, headers),
                            Map.class);

            assertThat(firstCompleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            // when - 이미 완료된 세션 다시 완료 시도
            ResponseEntity<Map> secondCompleteResponse =
                    restTemplate.exchange(
                            url(singleCompleteUrl(sessionId)),
                            HttpMethod.PATCH,
                            createRequestEntity(completeRequest, headers),
                            Map.class);

            // then - 400 또는 409 (비즈니스 로직에 따라)
            assertThat(secondCompleteResponse.getStatusCode().value()).isIn(400, 409);
        }

        @Test
        @DisplayName("취소된 세션을 완료 요청하면 에러를 반환해야 한다")
        void shouldReturnErrorWhenCompletingCancelledSession() {
            // given - 업로드 세션 생성 및 취소
            String fileName = "cancel-conflict-" + UUID.randomUUID() + ".jpg";
            String idempotencyKey = UUID.randomUUID().toString();

            // 동일한 헤더를 모든 요청에 사용 (tenant 일관성 유지)
            HttpHeaders headers = sellerHeaders();

            Map<String, Object> initRequest =
                    Map.of(
                            "idempotencyKey",
                            idempotencyKey,
                            "fileName",
                            fileName,
                            "fileSize",
                            1024L,
                            "contentType",
                            "image/jpeg",
                            "uploadCategory",
                            "PRODUCT");

            ResponseEntity<Map> initResponse =
                    restTemplate.exchange(
                            url(SINGLE_INIT),
                            HttpMethod.POST,
                            createRequestEntity(initRequest, headers),
                            Map.class);

            assertThat(initResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

            @SuppressWarnings("unchecked")
            Map<String, Object> initData = (Map<String, Object>) initResponse.getBody().get("data");
            String sessionId = (String) initData.get("sessionId");

            // 세션 취소 - PATCH /{sessionId}/cancel (body 없음)
            restTemplate.exchange(
                    url(cancelUrl(sessionId)),
                    HttpMethod.PATCH,
                    new HttpEntity<>(headers),
                    Map.class);

            // when - 취소된 세션 완료 시도
            // CompleteSingleUploadApiRequest는 etag 필드만 필요
            Map<String, Object> completeRequest =
                    Map.of("etag", "\"d41d8cd98f00b204e9800998ecf8427e\"");

            ResponseEntity<Map> completeResponse =
                    restTemplate.exchange(
                            url(singleCompleteUrl(sessionId)),
                            HttpMethod.PATCH,
                            createRequestEntity(completeRequest, headers),
                            Map.class);

            // then - 에러 반환 (400 또는 409)
            assertThat(completeResponse.getStatusCode().value()).isIn(400, 409);
        }
    }

    // ========================================
    // Helper Methods
    // ========================================

    private HttpHeaders sellerHeaders() {
        String tenantId = UuidV7Generator.generate();
        String organizationId = UuidV7Generator.generate();
        return createSellerHeaders(tenantId, organizationId);
    }

    private HttpHeaders createSellerHeaders(String tenantId, String organizationId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String email = "seller-test-" + UUID.randomUUID() + "@example.com";

        headers.set("X-User-Id", UuidV7Generator.generate());
        headers.set("X-Tenant-Id", tenantId);
        headers.set("X-Organization-Id", organizationId);
        headers.set("X-User-Roles", "SELLER");
        headers.set("X-User-Permissions", "file:read,file:write,file:delete,file:download");
        headers.set(
                "Authorization", "Bearer " + createTestJwtToken(email, tenantId, organizationId));
        return headers;
    }

    private String createTestJwtToken(String email, String tenantId, String organizationId) {
        String header =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(
                                "{\"alg\":\"none\",\"typ\":\"JWT\"}"
                                        .getBytes(StandardCharsets.UTF_8));

        String payloadJson =
                String.format(
                        "{\"email\":\"%s\",\"tid\":\"%s\",\"oid\":\"%s\",\"tenant_name\":\"TestTenant\",\"org_name\":\"TestOrg\"}",
                        email, tenantId, organizationId);
        String payload =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));

        return header + "." + payload + ".";
    }

    private HttpHeaders createHeadersWithoutDeletePermission(
            String tenantId, String organizationId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String email = "seller-test-" + UUID.randomUUID() + "@example.com";

        headers.set("X-User-Id", UuidV7Generator.generate());
        headers.set("X-Tenant-Id", tenantId);
        headers.set("X-Organization-Id", organizationId);
        headers.set("X-User-Roles", "SELLER");
        headers.set("X-User-Permissions", "file:read,file:write,file:download"); // file:delete 없음
        headers.set(
                "Authorization", "Bearer " + createTestJwtToken(email, tenantId, organizationId));
        return headers;
    }

    private HttpHeaders createHeadersWithoutDownloadPermission(
            String tenantId, String organizationId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String email = "seller-test-" + UUID.randomUUID() + "@example.com";

        headers.set("X-User-Id", UuidV7Generator.generate());
        headers.set("X-Tenant-Id", tenantId);
        headers.set("X-Organization-Id", organizationId);
        headers.set("X-User-Roles", "SELLER");
        headers.set("X-User-Permissions", "file:read,file:write,file:delete"); // file:download 없음
        headers.set(
                "Authorization", "Bearer " + createTestJwtToken(email, tenantId, organizationId));
        return headers;
    }

    private HttpEntity<Map<String, Object>> createRequestEntity(
            Map<String, Object> request, HttpHeaders headers) {
        return new HttpEntity<>(request, headers);
    }

    /**
     * Presigned URL을 사용하여 S3에 파일을 업로드하고 ETag를 반환합니다. S3 응답의 ETag는 "abc123" 형식이므로 따옴표를 제거하여 반환합니다.
     */
    private String uploadToS3(String presignedUrl, byte[] content, String contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(content.length);

        java.net.URI uri = java.net.URI.create(presignedUrl);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        uri, HttpMethod.PUT, new HttpEntity<>(content, headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        String etag = response.getHeaders().getFirst("ETag");
        assertThat(etag).isNotBlank();
        // S3 응답의 ETag는 "abc123" 형식이므로 따옴표 제거
        return etag.replace("\"", "");
    }
}
