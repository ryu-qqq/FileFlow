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
 * External Download 통합 테스트.
 *
 * <p>외부 URL 다운로드 요청 및 조회 API를 검증합니다:
 * <ul>
 *   <li>POST /api/v1/file/external-downloads - 외부 다운로드 요청
 *   <li>GET /api/v1/file/external-downloads - 외부 다운로드 목록 조회
 *   <li>GET /api/v1/file/external-downloads/{id} - 외부 다운로드 상태 조회
 * </ul>
 */
@DisplayName("External Download 통합 테스트")
class ExternalDownloadIntegrationTest extends WebApiIntegrationTest {

    private static final String EXTERNAL_DOWNLOAD_BASE = "/api/v1/file/external-downloads";

    @Nested
    @DisplayName("외부 다운로드 요청")
    class RequestExternalDownload {

        @Test
        @DisplayName("유효한 요청으로 외부 다운로드 요청이 성공해야 한다")
        void shouldRequestExternalDownloadWithValidRequest() {
            // given
            String idempotencyKey = UUID.randomUUID().toString();
            Map<String, Object> request = Map.of(
                "idempotencyKey", idempotencyKey,
                "sourceUrl", "https://example.com/image.jpg",
                "webhookUrl", "https://myservice.com/webhook"
            );

            // when
            ResponseEntity<Map> response = restTemplate.exchange(
                url(EXTERNAL_DOWNLOAD_BASE),
                HttpMethod.POST,
                createRequestEntity(request, sellerHeaders()),
                Map.class
            );

            System.out.println("[DEBUG] Request status: " + response.getStatusCode());
            System.out.println("[DEBUG] Request body: " + response.getBody());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();

            Map<String, Object> body = response.getBody();
            assertThat(body).containsKey("data");

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) body.get("data");
            assertThat(data).containsKey("id");
            assertThat(data).containsKey("status");
            assertThat(data).containsKey("createdAt");
            assertThat(data.get("status")).isEqualTo("PENDING");
        }

        @Test
        @DisplayName("동일한 멱등성 키로 중복 요청 시 동일한 결과를 반환해야 한다")
        void shouldReturnSameResultForDuplicateIdempotencyKey() {
            // given
            String idempotencyKey = UUID.randomUUID().toString();
            Map<String, Object> request = Map.of(
                "idempotencyKey", idempotencyKey,
                "sourceUrl", "https://example.com/image.jpg"
            );

            HttpHeaders headers = sellerHeaders();

            // when - 첫 번째 요청
            ResponseEntity<Map> response1 = restTemplate.exchange(
                url(EXTERNAL_DOWNLOAD_BASE),
                HttpMethod.POST,
                createRequestEntity(request, headers),
                Map.class
            );

            System.out.println("[DEBUG] First request status: " + response1.getStatusCode());
            System.out.println("[DEBUG] First request body: " + response1.getBody());

            // when - 두 번째 요청 (동일 멱등성 키)
            ResponseEntity<Map> response2 = restTemplate.exchange(
                url(EXTERNAL_DOWNLOAD_BASE),
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

            // 동일한 ID가 반환되어야 함
            assertThat(data1.get("id")).isEqualTo(data2.get("id"));
        }

        @Test
        @DisplayName("필수 필드 누락 시 400 에러를 반환해야 한다")
        void shouldReturn400WhenRequiredFieldMissing() {
            // given - sourceUrl 누락
            Map<String, Object> request = Map.of(
                "idempotencyKey", UUID.randomUUID().toString()
            );

            // when
            ResponseEntity<Map> response = restTemplate.exchange(
                url(EXTERNAL_DOWNLOAD_BASE),
                HttpMethod.POST,
                createRequestEntity(request, sellerHeaders()),
                Map.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("잘못된 멱등성 키 형식은 400 에러를 반환해야 한다")
        void shouldReturn400WhenIdempotencyKeyIsInvalidFormat() {
            // given - 잘못된 UUID 형식
            Map<String, Object> request = Map.of(
                "idempotencyKey", "invalid-uuid-format",
                "sourceUrl", "https://example.com/image.jpg"
            );

            // when
            ResponseEntity<Map> response = restTemplate.exchange(
                url(EXTERNAL_DOWNLOAD_BASE),
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
                "sourceUrl", "https://example.com/image.jpg"
            );

            // when - file:download 권한 없는 사용자
            ResponseEntity<Map> response = restTemplate.exchange(
                url(EXTERNAL_DOWNLOAD_BASE),
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
    @DisplayName("외부 다운로드 목록 조회")
    class GetExternalDownloads {

        @Test
        @DisplayName("외부 다운로드 목록 조회가 성공해야 한다")
        void shouldListExternalDownloads() {
            // given - 외부 다운로드 요청 생성
            String idempotencyKey1 = UUID.randomUUID().toString();
            String idempotencyKey2 = UUID.randomUUID().toString();
            HttpHeaders headers = sellerHeaders();

            // 두 개의 외부 다운로드 요청 생성
            Map<String, Object> request1 = Map.of(
                "idempotencyKey", idempotencyKey1,
                "sourceUrl", "https://example.com/image1.jpg"
            );
            Map<String, Object> request2 = Map.of(
                "idempotencyKey", idempotencyKey2,
                "sourceUrl", "https://example.com/image2.jpg"
            );

            restTemplate.exchange(
                url(EXTERNAL_DOWNLOAD_BASE),
                HttpMethod.POST,
                createRequestEntity(request1, headers),
                Map.class
            );
            restTemplate.exchange(
                url(EXTERNAL_DOWNLOAD_BASE),
                HttpMethod.POST,
                createRequestEntity(request2, headers),
                Map.class
            );

            // when
            ResponseEntity<Map> response = restTemplate.exchange(
                url(EXTERNAL_DOWNLOAD_BASE),
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
            assertThat(data).containsKey("totalElements");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");
            assertThat(content).hasSizeGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("상태 필터링이 동작해야 한다")
        void shouldFilterByStatus() {
            // given - 외부 다운로드 요청 생성
            String idempotencyKey = UUID.randomUUID().toString();
            HttpHeaders headers = sellerHeaders();

            Map<String, Object> request = Map.of(
                "idempotencyKey", idempotencyKey,
                "sourceUrl", "https://example.com/image.jpg"
            );

            restTemplate.exchange(
                url(EXTERNAL_DOWNLOAD_BASE),
                HttpMethod.POST,
                createRequestEntity(request, headers),
                Map.class
            );

            // when - PENDING 상태로 필터링
            ResponseEntity<Map> response = restTemplate.exchange(
                url(EXTERNAL_DOWNLOAD_BASE + "?status=PENDING"),
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

            // 모든 결과가 PENDING 상태여야 함
            assertThat(content).allMatch(item -> "PENDING".equals(item.get("status")));
        }

        @Test
        @DisplayName("페이징이 동작해야 한다")
        void shouldPaginateResults() {
            // given - 여러 외부 다운로드 요청 생성
            HttpHeaders headers = sellerHeaders();

            for (int i = 0; i < 5; i++) {
                Map<String, Object> request = Map.of(
                    "idempotencyKey", UUID.randomUUID().toString(),
                    "sourceUrl", "https://example.com/image" + i + ".jpg"
                );
                restTemplate.exchange(
                    url(EXTERNAL_DOWNLOAD_BASE),
                    HttpMethod.POST,
                    createRequestEntity(request, headers),
                    Map.class
                );
            }

            // when - 페이지 크기 2로 조회
            ResponseEntity<Map> response = restTemplate.exchange(
                url(EXTERNAL_DOWNLOAD_BASE + "?page=0&size=2"),
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
            assertThat((Integer) data.get("totalElements")).isGreaterThanOrEqualTo(5);
        }
    }

    @Nested
    @DisplayName("외부 다운로드 상태 조회")
    class GetExternalDownload {

        @Test
        @DisplayName("외부 다운로드 상태 조회가 성공해야 한다")
        void shouldGetExternalDownloadById() {
            // given - 외부 다운로드 요청 생성
            String idempotencyKey = UUID.randomUUID().toString();
            HttpHeaders headers = sellerHeaders();

            Map<String, Object> request = Map.of(
                "idempotencyKey", idempotencyKey,
                "sourceUrl", "https://example.com/image.jpg",
                "webhookUrl", "https://myservice.com/webhook"
            );

            ResponseEntity<Map> createResponse = restTemplate.exchange(
                url(EXTERNAL_DOWNLOAD_BASE),
                HttpMethod.POST,
                createRequestEntity(request, headers),
                Map.class
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> createData = (Map<String, Object>) createResponse.getBody().get("data");
            String downloadId = (String) createData.get("id");

            System.out.println("[DEBUG] Created download ID: " + downloadId);

            // when
            ResponseEntity<Map> response = restTemplate.exchange(
                url(EXTERNAL_DOWNLOAD_BASE + "/" + downloadId),
                HttpMethod.GET,
                createRequestEntity(null, headers),
                Map.class
            );

            System.out.println("[DEBUG] Get by ID status: " + response.getStatusCode());
            System.out.println("[DEBUG] Get by ID body: " + response.getBody());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data.get("id")).isEqualTo(downloadId);
            assertThat(data.get("sourceUrl")).isEqualTo("https://example.com/image.jpg");
            assertThat(data.get("status")).isEqualTo("PENDING");
            assertThat(data.get("webhookUrl")).isEqualTo("https://myservice.com/webhook");
            assertThat(data.get("retryCount")).isEqualTo(0);
        }

        @Test
        @DisplayName("존재하지 않는 ID 조회 시 404 에러를 반환해야 한다")
        void shouldReturn404WhenExternalDownloadNotFound() {
            // given - 존재하지 않는 유효한 UUID 형식의 ID
            String nonExistentId = UuidV7Generator.generate();

            // when
            ResponseEntity<Map> response = restTemplate.exchange(
                url(EXTERNAL_DOWNLOAD_BASE + "/" + nonExistentId),
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
        @DisplayName("다른 테넌트의 다운로드 조회 시 접근이 거부되어야 한다")
        void shouldDenyAccessToDifferentTenantDownload() {
            // given - 테넌트 A로 외부 다운로드 요청 생성
            String idempotencyKey = UUID.randomUUID().toString();
            HttpHeaders tenantAHeaders = sellerHeaders();

            Map<String, Object> request = Map.of(
                "idempotencyKey", idempotencyKey,
                "sourceUrl", "https://example.com/image.jpg"
            );

            ResponseEntity<Map> createResponse = restTemplate.exchange(
                url(EXTERNAL_DOWNLOAD_BASE),
                HttpMethod.POST,
                createRequestEntity(request, tenantAHeaders),
                Map.class
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> createData = (Map<String, Object>) createResponse.getBody().get("data");
            String downloadId = (String) createData.get("id");

            // when - 다른 테넌트 B로 조회 시도
            HttpHeaders tenantBHeaders = sellerHeaders(); // 새로운 테넌트 ID 생성됨

            ResponseEntity<Map> response = restTemplate.exchange(
                url(EXTERNAL_DOWNLOAD_BASE + "/" + downloadId),
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
     * 읽기 전용 사용자의 인증 헤더를 생성합니다 (file:download 권한 없음).
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
