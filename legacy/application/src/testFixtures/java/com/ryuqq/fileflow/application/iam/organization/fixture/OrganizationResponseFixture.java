package com.ryuqq.fileflow.application.iam.organization.fixture;

import com.ryuqq.fileflow.application.iam.organization.dto.response.OrganizationResponse;

import java.time.LocalDateTime;

/**
 * Organization Response Test Fixture
 *
 * <p>테스트에서 OrganizationResponse 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
public class OrganizationResponseFixture {

    private static final Long DEFAULT_ORG_ID = 1L;
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private OrganizationResponseFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final Long DEFAULT_TENANT_ID = 1L;
    private static final String DEFAULT_ORG_CODE = "ORG001";
    private static final String DEFAULT_NAME = "Engineering Department";
    private static final String DEFAULT_STATUS = "ACTIVE";

    public static OrganizationResponse create() {
        LocalDateTime now = LocalDateTime.now();
        return new OrganizationResponse(
            DEFAULT_ORG_ID,
            DEFAULT_TENANT_ID,
            DEFAULT_ORG_CODE,
            DEFAULT_NAME,
            DEFAULT_STATUS,
            false,
            now,
            now
        );
    }

    public static OrganizationResponse create(
        Long organizationId,
        Long tenantId,
        String orgCode,
        String name,
        String status,
        boolean deleted
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new OrganizationResponse(
            organizationId,
            tenantId,
            orgCode,
            name,
            status,
            deleted,
            now,
            now
        );
    }

    public static OrganizationResponse createActive() {
        LocalDateTime now = LocalDateTime.now();
        return new OrganizationResponse(
            1L,
            1L,
            "ENG001",
            "Engineering Department",
            "ACTIVE",
            false,
            now,
            now
        );
    }

    public static OrganizationResponse createInactive() {
        LocalDateTime now = LocalDateTime.now();
        return new OrganizationResponse(
            1L,
            1L,
            "ENG001",
            "Engineering Department",
            "INACTIVE",
            false,
            now,
            now
        );
    }

    public static OrganizationResponse createDeleted() {
        LocalDateTime now = LocalDateTime.now();
        return new OrganizationResponse(
            1L,
            1L,
            "ENG001",
            "Engineering Department",
            "INACTIVE",
            true,
            now,
            now
        );
    }

    public static OrganizationResponse createEngineeringDept() {
        LocalDateTime now = LocalDateTime.now();
        return new OrganizationResponse(
            1L,
            1L,
            "ENG001",
            "Engineering Department",
            "ACTIVE",
            false,
            now,
            now
        );
    }

    public static OrganizationResponse createSalesDept() {
        LocalDateTime now = LocalDateTime.now();
        return new OrganizationResponse(
            2L,
            1L,
            "SALES001",
            "Sales Department",
            "ACTIVE",
            false,
            now,
            now
        );
    }

    public static OrganizationResponse createHRDept() {
        LocalDateTime now = LocalDateTime.now();
        return new OrganizationResponse(
            3L,
            1L,
            "HR001",
            "Human Resources",
            "ACTIVE",
            false,
            now,
            now
        );
    }

    public static java.util.List<OrganizationResponse> createMultiple(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        LocalDateTime now = LocalDateTime.now();
        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> new OrganizationResponse(
                (long) i,
                DEFAULT_TENANT_ID,
                "ORG" + String.format("%03d", i),
                "Department " + i,
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
        private Long organizationId = DEFAULT_ORG_ID;
        private Long tenantId = DEFAULT_TENANT_ID;
        private String orgCode = DEFAULT_ORG_CODE;
        private String name = DEFAULT_NAME;
        private String status = DEFAULT_STATUS;
        private boolean deleted = false;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public ResponseBuilder organizationId(Long organizationId) {
            this.organizationId = organizationId;
            return this;
        }

        public ResponseBuilder tenantId(Long tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public ResponseBuilder orgCode(String orgCode) {
            this.orgCode = orgCode;
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

        public OrganizationResponse build() {
            return new OrganizationResponse(
                organizationId,
                tenantId,
                orgCode,
                name,
                status,
                deleted,
                createdAt,
                updatedAt
            );
        }
    }
}
