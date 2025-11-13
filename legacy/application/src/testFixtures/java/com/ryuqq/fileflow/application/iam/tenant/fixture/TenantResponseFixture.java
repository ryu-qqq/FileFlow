package com.ryuqq.fileflow.application.iam.tenant.fixture;

import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantResponse;

import java.time.LocalDateTime;

/**
 * Tenant Response Test Fixture
 *
 * <p>테스트에서 TenantResponse 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
public class TenantResponseFixture {

    private static final Long DEFAULT_TENANT_ID = 1L;
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private TenantResponseFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final String DEFAULT_NAME = "Default Tenant";
    private static final String DEFAULT_STATUS = "ACTIVE";

    public static TenantResponse create() {
        LocalDateTime now = LocalDateTime.now();
        return new TenantResponse(
            DEFAULT_TENANT_ID,
            DEFAULT_NAME,
            DEFAULT_STATUS,
            false,
            now,
            now
        );
    }

    public static TenantResponse create(Long tenantId, String name, String status, boolean deleted) {
        LocalDateTime now = LocalDateTime.now();
        return new TenantResponse(tenantId, name, status, deleted, now, now);
    }

    public static TenantResponse createActive() {
        LocalDateTime now = LocalDateTime.now();
        return new TenantResponse(1L, "Acme Corporation", "ACTIVE", false, now, now);
    }

    public static TenantResponse createSuspended() {
        LocalDateTime now = LocalDateTime.now();
        return new TenantResponse(1L, "Acme Corporation", "SUSPENDED", false, now, now);
    }

    public static TenantResponse createDeleted() {
        LocalDateTime now = LocalDateTime.now();
        return new TenantResponse(1L, "Acme Corporation", "SUSPENDED", true, now, now);
    }

    public static TenantResponse createAcmeCorp() {
        LocalDateTime now = LocalDateTime.now();
        return new TenantResponse(1L, "Acme Corporation", "ACTIVE", false, now, now);
    }

    public static TenantResponse createTechStartup() {
        LocalDateTime now = LocalDateTime.now();
        return new TenantResponse(2L, "Tech Startup Inc", "ACTIVE", false, now, now);
    }

    public static TenantResponse createEnterprise() {
        LocalDateTime now = LocalDateTime.now();
        return new TenantResponse(3L, "Enterprise Solutions", "ACTIVE", false, now, now);
    }

    public static java.util.List<TenantResponse> createMultiple(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        LocalDateTime now = LocalDateTime.now();
        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> new TenantResponse(
                (long) i,
                "Tenant " + i,
                "ACTIVE",
                false,
                now,
                now
            ))
            .toList();
    }

    public static ResponseBuilder builder() {
        return new ResponseBuilder();
    }

    public static class ResponseBuilder {
        private Long tenantId = DEFAULT_TENANT_ID;
        private String name = DEFAULT_NAME;
        private String status = DEFAULT_STATUS;
        private boolean deleted = false;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public ResponseBuilder tenantId(Long tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public ResponseBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ResponseBuilder status(String status) {
            this.status = status;
            return this;
        }

        public ResponseBuilder deleted(boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public ResponseBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ResponseBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public TenantResponse build() {
            return new TenantResponse(tenantId, name, status, deleted, createdAt, updatedAt);
        }
    }
}
