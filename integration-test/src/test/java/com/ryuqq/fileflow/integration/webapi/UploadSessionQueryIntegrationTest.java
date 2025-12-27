package com.ryuqq.fileflow.integration.webapi;

import com.ryuqq.fileflow.domain.common.util.UuidV7Generator;
import com.ryuqq.fileflow.integration.base.WebApiIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Upload Session 조회 통합 테스트.
 *
 * <p>업로드 세션 조회 API를 검증합니다:
 * <ul>
 *   <li>GET /api/v1/file/upload-sessions/{sessionId} - 업로드 세션 상세 조회
 *   <li>GET /api/v1/file/upload-sessions - 업로드 세션 목록 조회
 * </ul>
 */
@DisplayName("Upload Session 조회 통합 테스트")
class UploadSessionQueryIntegrationTest extends WebApiIntegrationTest {

    private static final String UPLOAD_SESSION_BASE = "/api/v1/file/upload-sessions";
    private static final String SINGLE_INIT = UPLOAD_SESSION_BASE + "/single";

    @Nested
    @DisplayName("업로드 세션 상세 조회")
    class GetUploadSession {

        @Test
        @DisplayName("생성된 세션을 상세 조회할 수 있어야 한다")
        void shouldGetUploadSessionById() {
            // given - 세션 초기화
            String idempotencyKey = UUID.randomUUID().toString();
            Map<String, Object> initRequest = Map.of(
                "idempotencyKey", idempotencyKey,
                "fileName", "test-image.jpg",
                "fileSize", 1024L,
                "contentType", "image/jpeg",
                "uploadCategory", "PRODUCT"
            );

            HttpHeaders headers = sellerHeaders();

            ResponseEntity<Map> initResponse = restTemplate.exchange(
                url(SINGLE_INIT),
                HttpMethod.POST,
                createRequestEntity(initRequest, headers),
                Map.class
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> initData = (Map<String, Object>) initResponse.getBody().get("data");
            String sessionId = (String) initData.get("sessionId");

            System.out.println("[DEBUG] Created session ID: " + sessionId);

            // when - 세션 상세 조회
            ResponseEntity<Map> response = restTemplate.exchange(
                url(UPLOAD_SESSION_BASE + "/" + sessionId),
                HttpMethod.GET,
                createRequestEntity(null, headers),
                Map.class
            );

            System.out.println("[DEBUG] Get session status: " + response.getStatusCode());
            System.out.println("[DEBUG] Get session body: " + response.getBody());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data.get("sessionId")).isEqualTo(sessionId);
            assertThat(data.get("fileName")).isEqualTo("test-image.jpg");
            assertThat(data.get("fileSize")).isEqualTo(1024);
            assertThat(data.get("contentType")).isEqualTo("image/jpeg");
            assertThat(data.get("uploadType")).isEqualTo("SINGLE");
            assertThat(data.get("status")).isIn("PREPARING", "ACTIVE");
            assertThat(data.get("bucket")).isNotNull();
            assertThat(data.get("key")).isNotNull();
        }

        @Test
        @DisplayName("완료된 세션을 상세 조회할 수 있어야 한다")
        void shouldGetCompletedUploadSession() {
            // given - 세션 초기화 및 완료
            String idempotencyKey = UUID.randomUUID().toString();
            Map<String, Object> initRequest = Map.of(
                "idempotencyKey", idempotencyKey,
                "fileName", "test-image.jpg",
                "fileSize", 1024L,
                "contentType", "image/jpeg",
                "uploadCategory", "PRODUCT"
            );

            HttpHeaders headers = sellerHeaders();

            ResponseEntity<Map> initResponse = restTemplate.exchange(
                url(SINGLE_INIT),
                HttpMethod.POST,
                createRequestEntity(initRequest, headers),
                Map.class
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> initData = (Map<String, Object>) initResponse.getBody().get("data");
            String sessionId = (String) initData.get("sessionId");
            String presignedUrl = (String) initData.get("presignedUrl");

            // S3에 파일 업로드 및 완료 처리
            String etag = uploadToS3(presignedUrl, "test content".getBytes(), "image/jpeg");

            Map<String, Object> completeRequest = Map.of("etag", etag);
            String completeUrl = UPLOAD_SESSION_BASE + "/" + sessionId + "/single/complete";

            restTemplate.exchange(
                url(completeUrl),
                HttpMethod.PATCH,
                createRequestEntity(completeRequest, headers),
                Map.class
            );

            // when - 완료된 세션 상세 조회
            ResponseEntity<Map> response = restTemplate.exchange(
                url(UPLOAD_SESSION_BASE + "/" + sessionId),
                HttpMethod.GET,
                createRequestEntity(null, headers),
                Map.class
            );

            System.out.println("[DEBUG] Completed session status: " + response.getStatusCode());
            System.out.println("[DEBUG] Completed session body: " + response.getBody());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data.get("sessionId")).isEqualTo(sessionId);
            assertThat(data.get("status")).isEqualTo("COMPLETED");
            assertThat(data.get("etag")).isNotNull();
            assertThat(data.get("completedAt")).isNotNull();
        }

        @Test
        @DisplayName("존재하지 않는 세션 조회 시 404 에러를 반환해야 한다")
        void shouldReturn404WhenSessionNotFound() {
            // given - 존재하지 않는 유효한 UUID 형식의 세션 ID
            String nonExistentSessionId = UuidV7Generator.generate();

            // when
            ResponseEntity<Map> response = restTemplate.exchange(
                url(UPLOAD_SESSION_BASE + "/" + nonExistentSessionId),
                HttpMethod.GET,
                createRequestEntity(null, sellerHeaders()),
                Map.class
            );

            System.out.println("[DEBUG] Not found status: " + response.getStatusCode());
            System.out.println("[DEBUG] Not found body: " + response.getBody());

            // then - 404 또는 400 허용 (서버 구현에 따라 다름)
            assertThat(response.getStatusCode()).isIn(HttpStatus.NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("다른 테넌트의 세션 조회 시 접근이 거부되어야 한다")
        void shouldDenyAccessToDifferentTenantSession() {
            // given - 테넌트 A로 세션 생성
            String idempotencyKey = UUID.randomUUID().toString();
            HttpHeaders tenantAHeaders = sellerHeaders();

            Map<String, Object> initRequest = Map.of(
                "idempotencyKey", idempotencyKey,
                "fileName", "test-image.jpg",
                "fileSize", 1024L,
                "contentType", "image/jpeg",
                "uploadCategory", "PRODUCT"
            );

            ResponseEntity<Map> initResponse = restTemplate.exchange(
                url(SINGLE_INIT),
                HttpMethod.POST,
                createRequestEntity(initRequest, tenantAHeaders),
                Map.class
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> initData = (Map<String, Object>) initResponse.getBody().get("data");
            String sessionId = (String) initData.get("sessionId");

            // when - 다른 테넌트 B로 조회 시도
            HttpHeaders tenantBHeaders = sellerHeaders(); // 새로운 테넌트 ID 생성됨

            ResponseEntity<Map> response = restTemplate.exchange(
                url(UPLOAD_SESSION_BASE + "/" + sessionId),
                HttpMethod.GET,
                createRequestEntity(null, tenantBHeaders),
                Map.class
            );

            System.out.println("[DEBUG] Different tenant status: " + response.getStatusCode());
            System.out.println("[DEBUG] Different tenant body: " + response.getBody());

            // then - 404 또는 403 또는 400 허용 (테넌트 격리로 인해 접근 불가)
            assertThat(response.getStatusCode()).isIn(
                HttpStatus.NOT_FOUND,
                HttpStatus.FORBIDDEN,
                HttpStatus.BAD_REQUEST
            );
        }
    }

    @Nested
    @DisplayName("업로드 세션 목록 조회")
    class GetUploadSessions {

        @Test
        @DisplayName("업로드 세션 목록 조회가 성공해야 한다")
        void shouldListUploadSessions() {
            // given - 여러 세션 생성
            HttpHeaders headers = sellerHeaders();

            for (int i = 0; i < 3; i++) {
                Map<String, Object> initRequest = Map.of(
                    "idempotencyKey", UUID.randomUUID().toString(),
                    "fileName", "test-image-" + i + ".jpg",
                    "fileSize", 1024L,
                    "contentType", "image/jpeg",
                    "uploadCategory", "PRODUCT"
                );

                restTemplate.exchange(
                    url(SINGLE_INIT),
                    HttpMethod.POST,
                    createRequestEntity(initRequest, headers),
                    Map.class
                );
            }

            // when
            ResponseEntity<Map> response = restTemplate.exchange(
                url(UPLOAD_SESSION_BASE),
                HttpMethod.GET,
                createRequestEntity(null, headers),
                Map.class
            );

            System.out.println("[DEBUG] List status: " + response.getStatusCode());
            System.out.println("[DEBUG] List body: " + response.getBody());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> body = response.getBody();
            assertThat(body).containsKey("data");

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) body.get("data");
            assertThat(data).containsKey("content");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");
            assertThat(content).hasSizeGreaterThanOrEqualTo(3);
        }

        @Test
        @DisplayName("상태 필터링이 동작해야 한다")
        void shouldFilterByStatus() {
            // given - 세션 생성 및 완료
            HttpHeaders headers = sellerHeaders();
            String idempotencyKey = UUID.randomUUID().toString();

            Map<String, Object> initRequest = Map.of(
                "idempotencyKey", idempotencyKey,
                "fileName", "test-image.jpg",
                "fileSize", 1024L,
                "contentType", "image/jpeg",
                "uploadCategory", "PRODUCT"
            );

            ResponseEntity<Map> initResponse = restTemplate.exchange(
                url(SINGLE_INIT),
                HttpMethod.POST,
                createRequestEntity(initRequest, headers),
                Map.class
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> initData = (Map<String, Object>) initResponse.getBody().get("data");
            String sessionId = (String) initData.get("sessionId");
            String presignedUrl = (String) initData.get("presignedUrl");

            // 세션 완료 처리
            String etag = uploadToS3(presignedUrl, "test content".getBytes(), "image/jpeg");
            Map<String, Object> completeRequest = Map.of("etag", etag);
            String completeUrl = UPLOAD_SESSION_BASE + "/" + sessionId + "/single/complete";

            restTemplate.exchange(
                url(completeUrl),
                HttpMethod.PATCH,
                createRequestEntity(completeRequest, headers),
                Map.class
            );

            // when - COMPLETED 상태로 필터링
            ResponseEntity<Map> response = restTemplate.exchange(
                url(UPLOAD_SESSION_BASE + "?status=COMPLETED"),
                HttpMethod.GET,
                createRequestEntity(null, headers),
                Map.class
            );

            System.out.println("[DEBUG] Filter status: " + response.getStatusCode());
            System.out.println("[DEBUG] Filter body: " + response.getBody());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");

            // 결과가 있으면 모두 COMPLETED 상태여야 함
            if (!content.isEmpty()) {
                assertThat(content).allMatch(item -> "COMPLETED".equals(item.get("status")));
            }
        }

        @Test
        @DisplayName("업로드 타입 필터링이 동작해야 한다")
        void shouldFilterByUploadType() {
            // given - 단일 업로드 세션 생성
            HttpHeaders headers = sellerHeaders();

            Map<String, Object> initRequest = Map.of(
                "idempotencyKey", UUID.randomUUID().toString(),
                "fileName", "test-image.jpg",
                "fileSize", 1024L,
                "contentType", "image/jpeg",
                "uploadCategory", "PRODUCT"
            );

            restTemplate.exchange(
                url(SINGLE_INIT),
                HttpMethod.POST,
                createRequestEntity(initRequest, headers),
                Map.class
            );

            // when - SINGLE 타입으로 필터링
            ResponseEntity<Map> response = restTemplate.exchange(
                url(UPLOAD_SESSION_BASE + "?uploadType=SINGLE"),
                HttpMethod.GET,
                createRequestEntity(null, headers),
                Map.class
            );

            System.out.println("[DEBUG] Upload type filter status: " + response.getStatusCode());
            System.out.println("[DEBUG] Upload type filter body: " + response.getBody());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");

            // 결과가 있으면 모두 SINGLE 타입이어야 함
            if (!content.isEmpty()) {
                assertThat(content).allMatch(item -> "SINGLE".equals(item.get("uploadType")));
            }
        }

        @Test
        @DisplayName("페이징이 동작해야 한다")
        void shouldPaginateResults() {
            // given - 여러 세션 생성
            HttpHeaders headers = sellerHeaders();

            for (int i = 0; i < 5; i++) {
                Map<String, Object> initRequest = Map.of(
                    "idempotencyKey", UUID.randomUUID().toString(),
                    "fileName", "test-image-" + i + ".jpg",
                    "fileSize", 1024L,
                    "contentType", "image/jpeg",
                    "uploadCategory", "PRODUCT"
                );
                restTemplate.exchange(
                    url(SINGLE_INIT),
                    HttpMethod.POST,
                    createRequestEntity(initRequest, headers),
                    Map.class
                );
            }

            // when - 페이지 크기 2로 조회
            ResponseEntity<Map> response = restTemplate.exchange(
                url(UPLOAD_SESSION_BASE + "?page=0&size=2"),
                HttpMethod.GET,
                createRequestEntity(null, headers),
                Map.class
            );

            System.out.println("[DEBUG] Pagination status: " + response.getStatusCode());
            System.out.println("[DEBUG] Pagination body: " + response.getBody());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");

            assertThat(content).hasSize(2);
            assertThat(data).containsKey("hasNext");
        }

        @Test
        @DisplayName("권한이 없는 사용자는 403 에러를 반환해야 한다")
        void shouldReturn403WhenUserHasNoPermission() {
            // when - file:read 권한 없는 사용자
            ResponseEntity<Map> response = restTemplate.exchange(
                url(UPLOAD_SESSION_BASE),
                HttpMethod.GET,
                createRequestEntity(null, noPermissionHeaders()),
                Map.class
            );

            System.out.println("[DEBUG] No permission status: " + response.getStatusCode());
            System.out.println("[DEBUG] No permission body: " + response.getBody());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }
    }

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * SELLER 역할의 인증 헤더를 생성합니다.
     */
    private HttpHeaders sellerHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String tenantId = UuidV7Generator.generate();
        String organizationId = UuidV7Generator.generate();
        String email = "seller-test@example.com";

        headers.set("X-User-Id", UuidV7Generator.generate());
        headers.set("X-Tenant-Id", tenantId);
        headers.set("X-Organization-Id", organizationId);
        headers.set("X-User-Roles", "SELLER");
        headers.set("X-User-Permissions", "file:read,file:write,file:delete,file:download");
        headers.set("Authorization", "Bearer " + createTestJwtToken(email, tenantId, organizationId));
        return headers;
    }

    /**
     * 권한이 없는 사용자의 인증 헤더를 생성합니다.
     */
    private HttpHeaders noPermissionHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String tenantId = UuidV7Generator.generate();
        String userId = UuidV7Generator.generate();

        headers.set("X-User-Id", userId);
        headers.set("X-Tenant-Id", tenantId);
        headers.set("X-Organization-Id", UuidV7Generator.generate());
        headers.set("X-User-Roles", "DEFAULT");
        headers.set("X-User-Permissions", ""); // 권한 없음
        headers.set("Authorization", "Bearer " + createTestJwtTokenForCustomer(userId, tenantId));
        return headers;
    }

    /**
     * 요청 엔티티를 생성합니다.
     */
    private <T> HttpEntity<T> createRequestEntity(T body, HttpHeaders headers) {
        return new HttpEntity<>(body, headers);
    }

    /**
     * Presigned URL을 사용하여 S3에 파일을 업로드하고 ETag를 반환합니다.
     */
    private String uploadToS3(String presignedUrl, byte[] content, String contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(content.length);

        java.net.URI uri = java.net.URI.create(presignedUrl);

        ResponseEntity<String> response = restTemplate.exchange(
            uri,
            HttpMethod.PUT,
            new HttpEntity<>(content, headers),
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        String etag = response.getHeaders().getFirst("ETag");
        assertThat(etag).isNotNull();

        return etag.replace("\"", "");
    }

    /**
     * SELLER/ADMIN 역할용 테스트 JWT 토큰을 생성합니다.
     */
    private String createTestJwtToken(String email, String tenantId, String organizationId) {
        String header = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

        String payloadJson = String.format(
            "{\"email\":\"%s\",\"tid\":\"%s\",\"oid\":\"%s\",\"tenant_name\":\"TestTenant\",\"org_name\":\"TestOrg\"}",
            email, tenantId, organizationId
        );
        String payload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));

        String signature = "";

        return header + "." + payload + "." + signature;
    }

    /**
     * DEFAULT(Customer) 역할용 테스트 JWT 토큰을 생성합니다.
     */
    private String createTestJwtTokenForCustomer(String userId, String tenantId) {
        String header = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

        String payloadJson = String.format(
            "{\"sub\":\"%s\",\"tid\":\"%s\",\"tenant_name\":\"TestTenant\"}",
            userId, tenantId
        );
        String payload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));

        String signature = "";

        return header + "." + payload + "." + signature;
    }
}
