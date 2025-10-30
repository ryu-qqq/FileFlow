package com.ryuqq.fileflow.domain.iam.organization.fixture;

import com.ryuqq.fileflow.domain.iam.organization.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Organization Test Fixture
 *
 * <p>테스트에서 Organization 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // 신규 Organization (ID 없음)
 * Organization org = OrganizationFixture.createNew();
 *
 * // ID가 있는 Organization
 * Organization org = OrganizationFixture.createWithId(1L);
 *
 * // 커스텀 Organization
 * Organization org = OrganizationFixture.builder()
 *     .tenantId(100L)
 *     .orgCode("SALES-TEAM")
 *     .name("Sales Team")
 *     .build();
 *
 * // 여러 Organization 생성
 * List<Organization> orgs = OrganizationFixture.createMultiple(5);
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
public class OrganizationFixture {

    private static final Long DEFAULT_TENANT_ID = 1L;
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private OrganizationFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final String DEFAULT_ORG_CODE = "ORG-001";
    private static final String DEFAULT_NAME = "Test Organization";

    /**
     * 신규 Organization을 생성합니다 (ID 없음).
     *
     * <p>기본값:</p>
     * <ul>
     *   <li>tenantId: 1L</li>
     *   <li>orgCode: "ORG-001"</li>
     *   <li>name: "Test Organization"</li>
     *   <li>status: ACTIVE</li>
     *   <li>deleted: false</li>
     * </ul>
     *
     * @return Organization 인스턴스 (ID = null)
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static Organization createNew() {
        return Organization.forNew(
            DEFAULT_TENANT_ID,
            OrgCode.of(DEFAULT_ORG_CODE),
            DEFAULT_NAME
        );
    }

    /**
     * 신규 Organization을 생성합니다 (커스텀 값).
     *
     * @param tenantId Tenant ID
     * @param orgCode 조직 코드
     * @param name 조직 이름
     * @return Organization 인스턴스 (ID = null)
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static Organization createNew(Long tenantId, String orgCode, String name) {
        return Organization.forNew(
            tenantId,
            OrgCode.of(orgCode),
            name
        );
    }

    /**
     * ID가 있는 Organization을 생성합니다.
     *
     * <p>기본값:</p>
     * <ul>
     *   <li>tenantId: 1L</li>
     *   <li>orgCode: "ORG-001"</li>
     *   <li>name: "Test Organization"</li>
     *   <li>status: ACTIVE</li>
     *   <li>deleted: false</li>
     * </ul>
     *
     * @param id Organization ID
     * @return Organization 인스턴스
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static Organization createWithId(Long id) {
        return Organization.of(
            OrganizationId.of(id),
            DEFAULT_TENANT_ID,
            OrgCode.of(DEFAULT_ORG_CODE),
            DEFAULT_NAME
        );
    }

    /**
     * ID가 있는 Organization을 생성합니다 (커스텀 값).
     *
     * @param id Organization ID
     * @param tenantId Tenant ID
     * @param orgCode 조직 코드
     * @param name 조직 이름
     * @return Organization 인스턴스
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static Organization createWithId(Long id, Long tenantId, String orgCode, String name) {
        return Organization.of(
            OrganizationId.of(id),
            tenantId,
            OrgCode.of(orgCode),
            name
        );
    }

    /**
     * 여러 개의 Organization을 생성합니다.
     *
     * <p>ID는 1부터 시작하는 연속된 값을 사용합니다.</p>
     * <p>조직 코드는 "ORG-001", "ORG-002", ... 형식입니다.</p>
     * <p>조직 이름은 "Test Organization 1", "Test Organization 2", ... 형식입니다.</p>
     *
     * @param count 생성할 Organization 개수
     * @return Organization 리스트
     * @throws IllegalArgumentException count가 0 이하인 경우
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static java.util.List<Organization> createMultiple(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> Organization.of(
                OrganizationId.of((long) i),
                DEFAULT_TENANT_ID,
                OrgCode.of("ORG-" + String.format("%03d", i)),
                "Test Organization " + i
            ))
            .toList();
    }

    /**
     * 특정 Tenant에 속한 여러 개의 Organization을 생성합니다.
     *
     * @param tenantId Tenant ID
     * @param count 생성할 Organization 개수
     * @return Organization 리스트
     * @throws IllegalArgumentException count가 0 이하인 경우
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static java.util.List<Organization> createMultiple(Long tenantId, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> Organization.of(
                OrganizationId.of((long) i),
                tenantId,
                OrgCode.of("ORG-" + String.format("%03d", i)),
                "Test Organization " + i
            ))
            .toList();
    }

    /**
     * DB에서 조회된 Organization을 재구성합니다 (reconstitute).
     *
     * <p>모든 상태를 커스터마이징할 수 있습니다.</p>
     *
     * @param id Organization ID
     * @param tenantId Tenant ID
     * @param orgCode 조직 코드
     * @param name 조직 이름
     * @param status Organization 상태
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @param deleted 삭제 여부
     * @return 재구성된 Organization
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static Organization reconstitute(
        Long id,
        Long tenantId,
        String orgCode,
        String name,
        OrganizationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        return Organization.reconstitute(
            OrganizationId.of(id),
            tenantId,
            OrgCode.of(orgCode),
            name,
            status,
            createdAt,
            updatedAt,
            deleted
        );
    }

    /**
     * 비활성화된 Organization을 생성합니다.
     *
     * @param id Organization ID
     * @return 비활성화된 Organization
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static Organization createInactive(Long id) {
        LocalDateTime now = LocalDateTime.now();
        return Organization.reconstitute(
            OrganizationId.of(id),
            DEFAULT_TENANT_ID,
            OrgCode.of(DEFAULT_ORG_CODE),
            DEFAULT_NAME,
            OrganizationStatus.INACTIVE,
            now,
            now,
            false
        );
    }

    /**
     * 삭제된 Organization을 생성합니다.
     *
     * @param id Organization ID
     * @return 삭제된 Organization
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static Organization createDeleted(Long id) {
        LocalDateTime now = LocalDateTime.now();
        return Organization.reconstitute(
            OrganizationId.of(id),
            DEFAULT_TENANT_ID,
            OrgCode.of(DEFAULT_ORG_CODE),
            DEFAULT_NAME,
            OrganizationStatus.INACTIVE,
            now,
            now,
            true
        );
    }

    /**
     * Organization Builder를 생성합니다.
     *
     * <p>복잡한 테스트 시나리오를 위한 Builder 패턴을 제공합니다.</p>
     *
     * @return OrganizationBuilder 인스턴스
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static OrganizationBuilder builder() {
        return new OrganizationBuilder();
    }

    /**
     * Organization Builder 클래스
     *
     * <p>테스트에서 Organization을 유연하게 생성하기 위한 Builder입니다.</p>
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static class OrganizationBuilder {
        private Long id;
        private Long tenantId = DEFAULT_TENANT_ID;
        private String orgCode = DEFAULT_ORG_CODE;
        private String name = DEFAULT_NAME;
        private OrganizationStatus status = OrganizationStatus.ACTIVE;
        private Clock clock = Clock.systemDefaultZone();
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private boolean deleted = false;

        /**
         * Organization ID를 설정합니다.
         *
         * @param id Organization ID
         * @return OrganizationBuilder
         * @author ryu-qqq
         * @since 2025-10-30
         */
        public OrganizationBuilder id(Long id) {
            this.id = id;
            return this;
        }

        /**
         * Tenant ID를 설정합니다.
         *
         * @param tenantId Tenant ID
         * @return OrganizationBuilder
         * @author ryu-qqq
         * @since 2025-10-30
         */
        public OrganizationBuilder tenantId(Long tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        /**
         * 조직 코드를 설정합니다.
         *
         * @param orgCode 조직 코드
         * @return OrganizationBuilder
         * @author ryu-qqq
         * @since 2025-10-30
         */
        public OrganizationBuilder orgCode(String orgCode) {
            this.orgCode = orgCode;
            return this;
        }

        /**
         * 조직 이름을 설정합니다.
         *
         * @param name 조직 이름
         * @return OrganizationBuilder
         * @author ryu-qqq
         * @since 2025-10-30
         */
        public OrganizationBuilder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Organization 상태를 설정합니다.
         *
         * @param status Organization 상태
         * @return OrganizationBuilder
         * @author ryu-qqq
         * @since 2025-10-30
         */
        public OrganizationBuilder status(OrganizationStatus status) {
            this.status = status;
            return this;
        }

        /**
         * Clock을 설정합니다 (시간 제어용).
         *
         * @param clock Clock 인스턴스
         * @return OrganizationBuilder
         * @author ryu-qqq
         * @since 2025-10-30
         */
        public OrganizationBuilder clock(Clock clock) {
            this.clock = clock;
            return this;
        }

        /**
         * 고정된 시간을 설정합니다 (테스트용).
         *
         * @param instant 고정할 시간
         * @return OrganizationBuilder
         * @author ryu-qqq
         * @since 2025-10-30
         */
        public OrganizationBuilder fixedClock(Instant instant) {
            this.clock = Clock.fixed(instant, ZoneId.systemDefault());
            return this;
        }

        /**
         * 생성 일시를 설정합니다.
         *
         * @param createdAt 생성 일시
         * @return OrganizationBuilder
         * @author ryu-qqq
         * @since 2025-10-30
         */
        public OrganizationBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * 수정 일시를 설정합니다.
         *
         * @param updatedAt 수정 일시
         * @return OrganizationBuilder
         * @author ryu-qqq
         * @since 2025-10-30
         */
        public OrganizationBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        /**
         * 삭제 여부를 설정합니다.
         *
         * @param deleted 삭제 여부
         * @return OrganizationBuilder
         * @author ryu-qqq
         * @since 2025-10-30
         */
        public OrganizationBuilder deleted(boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        /**
         * Organization을 생성합니다.
         *
         * @return Organization 인스턴스
         * @author ryu-qqq
         * @since 2025-10-30
         */
        public Organization build() {
            if (id == null) {
                // ID 없이 신규 생성
                return Organization.forNew(
                    tenantId,
                    OrgCode.of(orgCode),
                    name
                );
            }

            // reconstitute 사용 (모든 상태 복원)
            LocalDateTime now = LocalDateTime.now(clock);
            return Organization.reconstitute(
                OrganizationId.of(id),
                tenantId,
                OrgCode.of(orgCode),
                name,
                status,
                createdAt != null ? createdAt : now,
                updatedAt != null ? updatedAt : now,
                deleted
            );
        }
    }
}
