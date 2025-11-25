package com.ryuqq.fileflow.domain.iam.vo;

/**
 * 조직 Value Object.
 *
 * <p><strong>조직 유형</strong>:
 *
 * <ul>
 *   <li>ADMIN 조직: Connectly 관리자 전용 (organizationId=0, namespace=connectly)
 *   <li>SELLER 조직: 입점 판매자 회사별 조직 (organizationId=입점사ID, namespace=setof)
 *   <li>CUSTOMER 조직: 커머스 손님용 조직 (organizationId=-1, namespace=setof)
 * </ul>
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>조직 ID는 -1 (손님), 0 (관리자), 1+ (판매자)이다.
 *   <li>조직명은 null이거나 빈 문자열일 수 없다.
 *   <li>Admin 조직은 organizationId=0, namespace=connectly
 *   <li>Seller 조직은 organizationId=1+, namespace=setof
 *   <li>Customer 조직은 organizationId=-1, namespace=setof
 * </ul>
 *
 * @param id 조직 ID (-1: 손님, 0: 관리자, 1+: 판매자)
 * @param name 조직명 (예: "Connectly Admin", "입점사A", "Customer")
 * @param namespace S3 버킷 네임스페이스 (connectly 또는 setof)
 * @param role 조직 역할 (ADMIN, SELLER, DEFAULT)
 */
public record Organization(long id, String name, String namespace, UserRole role) {

    // 특수 조직 ID 상수
    private static final long ADMIN_ORG_ID = 0L;
    private static final long CUSTOMER_ORG_ID = -1L;

    // 네임스페이스 상수
    private static final String CONNECTLY_NAMESPACE = "connectly";
    private static final String SETOF_NAMESPACE = "setof";

    /** Compact Constructor (검증 로직). */
    public Organization {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("조직명은 null이거나 빈 문자열일 수 없습니다.");
        }

        if (namespace == null || namespace.isBlank()) {
            throw new IllegalArgumentException("네임스페이스는 null이거나 빈 문자열일 수 없습니다.");
        }

        if (role == null) {
            throw new IllegalArgumentException("조직 역할은 null일 수 없습니다.");
        }

        // 조직 ID와 Role 일관성 검증
        validateOrganizationConsistency(id, role, namespace);
    }

    /**
     * 값 기반 생성.
     *
     * @param id 조직 ID
     * @param name 조직명
     * @param namespace 네임스페이스
     * @param role 조직 역할
     * @return Organization
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static Organization of(long id, String name, String namespace, UserRole role) {
        return new Organization(id, name, namespace, role);
    }

    /**
     * Admin 조직 생성.
     *
     * @return Connectly Admin Organization (id=0, namespace=connectly)
     */
    public static Organization admin() {
        return new Organization(
                ADMIN_ORG_ID, "Connectly Admin", CONNECTLY_NAMESPACE, UserRole.ADMIN);
    }

    /**
     * Seller 조직 생성.
     *
     * @param id 입점사 ID (1 이상)
     * @param companyName 입점사명
     * @return Seller Organization
     * @throws IllegalArgumentException id가 1 미만인 경우
     */
    public static Organization seller(long id, String companyName) {
        if (id < 1) {
            throw new IllegalArgumentException("Seller 조직 ID는 1 이상이어야 합니다: " + id);
        }
        return new Organization(id, companyName, SETOF_NAMESPACE, UserRole.SELLER);
    }

    /**
     * Customer 조직 생성 (손님용).
     *
     * @return Customer Organization (id=-1, namespace=setof)
     */
    public static Organization customer() {
        return new Organization(CUSTOMER_ORG_ID, "Customer", SETOF_NAMESPACE, UserRole.DEFAULT);
    }

    /**
     * Admin 조직인지 확인한다.
     *
     * @return Admin 조직이면 true
     */
    public boolean isAdmin() {
        return id == ADMIN_ORG_ID && role == UserRole.ADMIN;
    }

    /**
     * Seller 조직인지 확인한다.
     *
     * @return Seller 조직이면 true
     */
    public boolean isSeller() {
        return id > 0 && role == UserRole.SELLER;
    }

    /**
     * Customer 조직인지 확인한다.
     *
     * @return Customer 조직이면 true
     */
    public boolean isCustomer() {
        return id == CUSTOMER_ORG_ID && role == UserRole.DEFAULT;
    }

    /**
     * S3 버킷명을 반환한다.
     *
     * @return S3 버킷명 (예: "connectly", "setof")
     */
    public String getS3BucketName() {
        return namespace;
    }

    /**
     * S3 경로 prefix를 반환한다 (조직별 기본 경로).
     *
     * <p>경로 구조:
     *
     * <ul>
     *   <li>Admin: "admin/"
     *   <li>Seller: "seller-{organizationId}/"
     *   <li>Customer: "customer/"
     * </ul>
     *
     * @return S3 경로 prefix
     */
    public String getS3PathPrefix() {
        if (isAdmin()) {
            return "admin/";
        } else if (isSeller()) {
            return "seller-" + id + "/";
        } else {
            return "customer/";
        }
    }

    /**
     * 조직 ID와 Role, Namespace 일관성을 검증한다.
     *
     * @param id 조직 ID
     * @param role 조직 역할
     * @param namespace 네임스페이스
     * @throws IllegalArgumentException 일관성 위반 시
     */
    private static void validateOrganizationConsistency(long id, UserRole role, String namespace) {
        // Admin 조직: id=0, role=ADMIN, namespace=connectly
        if (id == ADMIN_ORG_ID) {
            if (role != UserRole.ADMIN) {
                throw new IllegalArgumentException("Admin 조직 (id=0)은 ADMIN role이어야 합니다.");
            }
            if (!CONNECTLY_NAMESPACE.equals(namespace)) {
                throw new IllegalArgumentException("Admin 조직은 connectly namespace여야 합니다.");
            }
        }

        // Seller 조직: id>0, role=SELLER, namespace=setof
        if (id > 0) {
            if (role != UserRole.SELLER) {
                throw new IllegalArgumentException("Seller 조직 (id>0)은 SELLER role이어야 합니다.");
            }
            if (!SETOF_NAMESPACE.equals(namespace)) {
                throw new IllegalArgumentException("Seller 조직은 setof namespace여야 합니다.");
            }
        }

        // Customer 조직: id=-1, role=DEFAULT, namespace=setof
        if (id == CUSTOMER_ORG_ID) {
            if (role != UserRole.DEFAULT) {
                throw new IllegalArgumentException("Customer 조직 (id=-1)은 DEFAULT role이어야 합니다.");
            }
            if (!SETOF_NAMESPACE.equals(namespace)) {
                throw new IllegalArgumentException("Customer 조직은 setof namespace여야 합니다.");
            }
        }
    }
}
