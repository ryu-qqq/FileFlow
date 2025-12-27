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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Single Upload E2E 통합 테스트.
 *
 * <p>단일 파일 업로드의 전체 흐름을 검증합니다:
 * <ol>
 *   <li>세션 초기화 (Presigned URL 발급)
 *   <li>S3 업로드 (LocalStack)
 *   <li>업로드 완료 처리
 *   <li>세션 상태 검증
 * </ol>
 */
@DisplayName("Single Upload E2E 통합 테스트")
class SingleUploadIntegrationTest extends WebApiIntegrationTest {

    private static final String UPLOAD_SESSION_BASE = "/api/v1/file/upload-sessions";
    private static final String SINGLE_INIT = UPLOAD_SESSION_BASE + "/single";

    @Nested
    @DisplayName("단일 업로드 세션 초기화")
    class InitSingleUpload {

        @Test
        @DisplayName("유효한 요청으로 세션 초기화가 성공해야 한다")
        void shouldInitializeSessionWithValidRequest() {
            // given
            String idempotencyKey = UUID.randomUUID().toString();
            Map<String, Object> request = Map.of(
                "idempotencyKey", idempotencyKey,
                "fileName", "test-image.jpg",
                "fileSize", 1024L,
                "contentType", "image/jpeg",
                "uploadCategory", "PRODUCT"
            );

            // when
            ResponseEntity<Map> response = restTemplate.exchange(
                url(SINGLE_INIT),
                HttpMethod.POST,
                createRequestEntity(request, sellerHeaders()),
                Map.class
            );

            System.out.println("[DEBUG] Init test status: " + response.getStatusCode());
            System.out.println("[DEBUG] Init test body: " + response.getBody());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();

            Map<String, Object> body = response.getBody();
            assertThat(body).containsKey("data");

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) body.get("data");
            assertThat(data).containsKey("sessionId");
            assertThat(data).containsKey("presignedUrl");
            assertThat(data).containsKey("bucket");
            assertThat(data).containsKey("key");
            // LocalStack은 127.0.0.1 또는 localhost를 사용할 수 있음
            String presignedUrl = data.get("presignedUrl").toString();
            assertThat(presignedUrl.contains("localhost") || presignedUrl.contains("127.0.0.1"))
                .as("presignedUrl should contain localhost or 127.0.0.1: " + presignedUrl)
                .isTrue();
        }

        @Test
        @DisplayName("동일한 멱등성 키로 중복 요청 시 동일한 세션을 반환해야 한다")
        void shouldReturnSameSessionForDuplicateIdempotencyKey() {
            // given
            String idempotencyKey = UUID.randomUUID().toString();
            Map<String, Object> request = Map.of(
                "idempotencyKey", idempotencyKey,
                "fileName", "test-image.jpg",
                "fileSize", 1024L,
                "contentType", "image/jpeg",
                "uploadCategory", "PRODUCT"
            );

            HttpHeaders headers = sellerHeaders();

            // when - 첫 번째 요청
            ResponseEntity<Map> response1 = restTemplate.exchange(
                url(SINGLE_INIT),
                HttpMethod.POST,
                createRequestEntity(request, headers),
                Map.class
            );

            System.out.println("[DEBUG] First request status: " + response1.getStatusCode());
            System.out.println("[DEBUG] First request body: " + response1.getBody());

            // when - 두 번째 요청 (동일 멱등성 키)
            ResponseEntity<Map> response2 = restTemplate.exchange(
                url(SINGLE_INIT),
                HttpMethod.POST,
                createRequestEntity(request, headers),
                Map.class
            );

            System.out.println("[DEBUG] Second request status: " + response2.getStatusCode());
            System.out.println("[DEBUG] Second request body: " + response2.getBody());

            // then
            assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CREATED);

            @SuppressWarnings("unchecked")
            Map<String, Object> data1 = (Map<String, Object>) response1.getBody().get("data");
            @SuppressWarnings("unchecked")
            Map<String, Object> data2 = (Map<String, Object>) response2.getBody().get("data");

            assertThat(data1.get("sessionId")).isEqualTo(data2.get("sessionId"));
        }

        @Test
        @DisplayName("필수 필드 누락 시 400 에러를 반환해야 한다")
        void shouldReturn400WhenRequiredFieldMissing() {
            // given - fileName 누락
            Map<String, Object> request = Map.of(
                "idempotencyKey", UUID.randomUUID().toString(),
                "fileSize", 1024L,
                "contentType", "image/jpeg"
            );

            // when
            ResponseEntity<Map> response = restTemplate.exchange(
                url(SINGLE_INIT),
                HttpMethod.POST,
                createRequestEntity(request, sellerHeaders()),
                Map.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("권한이 없는 사용자는 403 에러를 반환해야 한다")
        void shouldReturn403WhenUserHasNoPermission() {
            // given
            Map<String, Object> request = Map.of(
                "idempotencyKey", UUID.randomUUID().toString(),
                "fileName", "test-image.jpg",
                "fileSize", 1024L,
                "contentType", "image/jpeg",
                "uploadCategory", "PRODUCT"
            );

            // when - file:write 권한 없는 사용자
            ResponseEntity<Map> response = restTemplate.exchange(
                url(SINGLE_INIT),
                HttpMethod.POST,
                createRequestEntity(request, readOnlyUserHeaders()),
                Map.class
            );

            System.out.println("[DEBUG] Permission test status: " + response.getStatusCode());
            System.out.println("[DEBUG] Permission test body: " + response.getBody());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("단일 업로드 완료")
    class CompleteSingleUpload {

        @Test
        @DisplayName("업로드 완료 처리가 성공해야 한다")
        void shouldCompleteSingleUpload() {
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

            System.out.println("[DEBUG] Complete test - init status: " + initResponse.getStatusCode());
            System.out.println("[DEBUG] Complete test - init body: " + initResponse.getBody());

            @SuppressWarnings("unchecked")
            Map<String, Object> initData = (Map<String, Object>) initResponse.getBody().get("data");
            String sessionId = (String) initData.get("sessionId");
            String presignedUrl = (String) initData.get("presignedUrl");

            System.out.println("[DEBUG] Complete test - sessionId: " + sessionId);
            System.out.println("[DEBUG] Complete test - presignedUrl: " + presignedUrl);

            // S3에 파일 업로드 (LocalStack) - content-type을 세션 초기화 시 지정한 것과 동일하게 사용
            String etag = uploadToS3(presignedUrl, "test content".getBytes(), "image/jpeg");
            System.out.println("[DEBUG] Complete test - etag: " + etag);

            // when - 업로드 완료 처리
            Map<String, Object> completeRequest = Map.of("etag", etag);
            String completeUrl = UPLOAD_SESSION_BASE + "/" + sessionId + "/single/complete";

            ResponseEntity<Map> completeResponse = restTemplate.exchange(
                url(completeUrl),
                HttpMethod.PATCH,
                createRequestEntity(completeRequest, headers),
                Map.class
            );

            System.out.println("[DEBUG] Complete test - complete status: " + completeResponse.getStatusCode());
            System.out.println("[DEBUG] Complete test - complete body: " + completeResponse.getBody());

            // then
            assertThat(completeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(completeResponse.getBody()).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> completeData = (Map<String, Object>) completeResponse.getBody().get("data");
            assertThat(completeData.get("sessionId")).isEqualTo(sessionId);
            assertThat(completeData.get("status")).isEqualTo("COMPLETED");
            assertThat(completeData.get("etag")).isEqualTo(etag);
        }

        @Test
        @DisplayName("존재하지 않는 세션 완료 요청 시 404 에러를 반환해야 한다")
        void shouldReturn404WhenSessionNotFound() {
            // given - 존재하지 않는 유효한 UUID 형식의 세션 ID
            String nonExistentSessionId = UuidV7Generator.generate();
            Map<String, Object> completeRequest = Map.of("etag", "\"test-etag\"");
            String completeUrl = UPLOAD_SESSION_BASE + "/" + nonExistentSessionId + "/single/complete";

            // when
            ResponseEntity<Map> response = restTemplate.exchange(
                url(completeUrl),
                HttpMethod.PATCH,
                createRequestEntity(completeRequest, sellerHeaders()),
                Map.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("업로드 세션 취소")
    class CancelUploadSession {

        @Test
        @DisplayName("진행 중인 세션 취소가 성공해야 한다")
        void shouldCancelUploadSession() {
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

            System.out.println("[DEBUG] Cancel test - init status: " + initResponse.getStatusCode());
            System.out.println("[DEBUG] Cancel test - init body: " + initResponse.getBody());

            @SuppressWarnings("unchecked")
            Map<String, Object> initData = (Map<String, Object>) initResponse.getBody().get("data");
            String sessionId = (String) initData.get("sessionId");

            System.out.println("[DEBUG] Cancel test - sessionId: " + sessionId);

            // when - 세션 취소
            String cancelUrl = UPLOAD_SESSION_BASE + "/" + sessionId + "/cancel";

            ResponseEntity<Map> cancelResponse = restTemplate.exchange(
                url(cancelUrl),
                HttpMethod.PATCH,
                createRequestEntity(null, headers),
                Map.class
            );

            System.out.println("[DEBUG] Cancel test - cancel status: " + cancelResponse.getStatusCode());
            System.out.println("[DEBUG] Cancel test - cancel body: " + cancelResponse.getBody());

            // then
            assertThat(cancelResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(cancelResponse.getBody()).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> cancelData = (Map<String, Object>) cancelResponse.getBody().get("data");
            assertThat(cancelData.get("sessionId")).isEqualTo(sessionId);
            // 취소는 FAILED 상태로 처리됨 (도메인에 CANCELLED 상태가 없음)
            assertThat(cancelData.get("status")).isEqualTo("FAILED");
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
     * 읽기 전용 사용자의 인증 헤더를 생성합니다 (file:write 권한 없음).
     */
    private HttpHeaders readOnlyUserHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String tenantId = UuidV7Generator.generate();
        String userId = UuidV7Generator.generate();

        headers.set("X-User-Id", userId);
        headers.set("X-Tenant-Id", tenantId);
        headers.set("X-Organization-Id", UuidV7Generator.generate());
        headers.set("X-User-Roles", "DEFAULT");
        headers.set("X-User-Permissions", "file:read");
        // DEFAULT(Customer) 역할은 userId가 필수이고 email은 불필요
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
     * Presigned URL 생성 시 지정된 content-type과 동일한 타입을 사용해야 합니다.
     */
    private String uploadToS3(String presignedUrl, byte[] content, String contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(content.length);

        System.out.println("[DEBUG] S3 upload - presignedUrl: " + presignedUrl);
        System.out.println("[DEBUG] S3 upload - contentType: " + contentType);

        // URI 객체를 사용하여 이미 인코딩된 URL이 다시 인코딩되지 않도록 함
        java.net.URI uri = java.net.URI.create(presignedUrl);

        // String으로 응답을 받아서 에러 바디를 확인
        ResponseEntity<String> response = restTemplate.exchange(
            uri,
            HttpMethod.PUT,
            new HttpEntity<>(content, headers),
            String.class
        );

        System.out.println("[DEBUG] S3 upload - response status: " + response.getStatusCode());
        System.out.println("[DEBUG] S3 upload - response headers: " + response.getHeaders());
        System.out.println("[DEBUG] S3 upload - response body: " + response.getBody());

        assertThat(response.getStatusCode())
            .as("S3 upload should succeed. Response body: " + response.getBody())
            .isEqualTo(HttpStatus.OK);

        // S3는 ETag를 응답 헤더에 포함 (따옴표가 포함되어 있으므로 제거)
        String etag = response.getHeaders().getFirst("ETag");
        assertThat(etag).isNotNull();

        // S3 응답의 ETag는 "abc123" 형식이므로 따옴표 제거
        return etag.replace("\"", "");
    }

    /**
     * SELLER/ADMIN 역할용 테스트 JWT 토큰을 생성합니다.
     * UserContextFilter가 JWT payload에서 email을 추출할 수 있도록 합니다.
     */
    private String createTestJwtToken(String email, String tenantId, String organizationId) {
        // JWT Header (alg: none for testing)
        String header = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

        // JWT Payload with email claim
        String payloadJson = String.format(
            "{\"email\":\"%s\",\"tid\":\"%s\",\"oid\":\"%s\",\"tenant_name\":\"TestTenant\",\"org_name\":\"TestOrg\"}",
            email, tenantId, organizationId
        );
        String payload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));

        // Signature (empty for testing)
        String signature = "";

        return header + "." + payload + "." + signature;
    }

    /**
     * DEFAULT(Customer) 역할용 테스트 JWT 토큰을 생성합니다.
     * Customer는 email이 아닌 userId(sub)가 필수입니다.
     */
    private String createTestJwtTokenForCustomer(String userId, String tenantId) {
        // JWT Header (alg: none for testing)
        String header = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

        // JWT Payload with sub claim (userId)
        String payloadJson = String.format(
            "{\"sub\":\"%s\",\"tid\":\"%s\",\"tenant_name\":\"TestTenant\"}",
            userId, tenantId
        );
        String payload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));

        // Signature (empty for testing)
        String signature = "";

        return header + "." + payload + "." + signature;
    }
}
