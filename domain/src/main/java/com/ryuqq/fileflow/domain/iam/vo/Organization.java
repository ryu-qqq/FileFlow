package com.ryuqq.fileflow.domain.iam.vo;

/**
 * 조직 Value Object.
 *
 * <p><strong>조직 유형</strong>:
 *
 * <ul>
 *   <li>ADMIN 조직: Connectly 관리자 전용 (namespace=connectly)
 *   <li>SELLER 조직: 입점 판매자 회사별 조직 (organizationId=UUIDv7, namespace=setof)
 *   <li>CUSTOMER 조직: 커머스 손님용 조직 (namespace=setof)
 * </ul>
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>조직 유형은 UserRole로만 판단한다 (ID 비교 없음).
 *   <li>Seller 조직만 OrganizationId를 가진다 (UUIDv7).
 *   <li>Admin/Customer 조직은 시스템 정의 조직이라 OrganizationId가 null이다.
 *   <li>조직명은 null이거나 빈 문자열일 수 없다.
 *   <li>Admin 조직은 namespace=connectly.
 *   <li>Seller/Customer 조직은 namespace=setof.
 * </ul>
 *
 * @param id 조직 ID (Seller만 UUIDv7, Admin/Customer는 null)
 * @param name 조직명 (예: "Connectly Admin", "입점사A", "Customer")
 * @param namespace S3 버킷 네임스페이스 (connectly 또는 setof)
 * @param role 조직 역할 (ADMIN, SELLER, DEFAULT)
 */
public record Organization(OrganizationId id, String name, String namespace, UserRole role) {

    // 네임스페이스 상수
    private static final String CONNECTLY_NAMESPACE = "connectly";
    private static final String SETOF_NAMESPACE = "setof";

    // S3 경로 접두사 상수
    private static final String PUBLIC_PREFIX = "uploads/";
    private static final String INTERNAL_PREFIX = "internal/";

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

        // Role과 Namespace 일관성 검증
        validateRoleNamespaceConsistency(role, namespace, id);
    }

    /**
     * 값 기반 생성.
     *
     * @param id 조직 ID (nullable)
     * @param name 조직명
     * @param namespace 네임스페이스
     * @param role 조직 역할
     * @return Organization
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static Organization of(OrganizationId id, String name, String namespace, UserRole role) {
        return new Organization(id, name, namespace, role);
    }

    /**
     * Admin 조직 생성.
     *
     * <p>Admin은 시스템 정의 조직이므로 OrganizationId가 없다.
     *
     * @return Connectly Admin Organization (id=null, namespace=connectly)
     */
    public static Organization admin() {
        return new Organization(null, "Connectly Admin", CONNECTLY_NAMESPACE, UserRole.ADMIN);
    }

    /**
     * Seller 조직 생성 (기존 OrganizationId 사용).
     *
     * @param id 입점사 조직 ID (UUIDv7)
     * @param companyName 입점사명
     * @return Seller Organization
     * @throws IllegalArgumentException id가 null인 경우
     */
    public static Organization seller(OrganizationId id, String companyName) {
        if (id == null) {
            throw new IllegalArgumentException("Seller 조직 ID는 null일 수 없습니다.");
        }
        return new Organization(id, companyName, SETOF_NAMESPACE, UserRole.SELLER);
    }

    /**
     * Seller 조직 생성 (새로운 OrganizationId 자동 생성).
     *
     * @param companyName 입점사명
     * @return Seller Organization (새로운 UUIDv7 기반 ID)
     */
    public static Organization newSeller(String companyName) {
        return new Organization(
                OrganizationId.generate(), companyName, SETOF_NAMESPACE, UserRole.SELLER);
    }

    /**
     * Customer 조직 생성 (손님용).
     *
     * <p>Customer는 시스템 정의 조직이므로 OrganizationId가 없다.
     *
     * @return Customer Organization (id=null, namespace=setof)
     */
    public static Organization customer() {
        return new Organization(null, "Customer", SETOF_NAMESPACE, UserRole.DEFAULT);
    }

    /**
     * Admin 조직인지 확인한다 (SUPER_ADMIN 포함).
     *
     * <p><strong>주의</strong>: UserRole로만 판단한다 (ID 비교 없음).
     *
     * @return SUPER_ADMIN 또는 ADMIN 조직이면 true
     */
    public boolean isAdmin() {
        return role == UserRole.SUPER_ADMIN || role == UserRole.ADMIN;
    }

    /**
     * SuperAdmin 조직인지 확인한다.
     *
     * @return SUPER_ADMIN 조직이면 true
     */
    public boolean isSuperAdmin() {
        return role == UserRole.SUPER_ADMIN;
    }

    /**
     * Seller 조직인지 확인한다.
     *
     * <p><strong>주의</strong>: UserRole로만 판단한다 (ID 비교 없음).
     *
     * @return Seller 조직이면 true
     */
    public boolean isSeller() {
        return role == UserRole.SELLER;
    }

    /**
     * Customer 조직인지 확인한다.
     *
     * <p><strong>주의</strong>: UserRole로만 판단한다 (ID 비교 없음).
     *
     * @return Customer 조직이면 true
     */
    public boolean isCustomer() {
        return role == UserRole.DEFAULT;
    }

    /**
     * S3 버킷명을 반환한다.
     *
     * <p>모든 조직이 동일한 버킷을 사용하며, namespace는 경로의 첫 번째 세그먼트로 사용됩니다.
     *
     * @return S3 버킷명 (환경 변수로 주입 필요, 기본값: "fileflow-uploads-prod")
     * @deprecated 환경 변수에서 버킷명을 가져와야 함. 이 메서드는 하드코딩된 값을 반환하므로 사용하지 말 것.
     */
    @Deprecated
    public String getS3BucketName() {
        // TODO: 환경 변수에서 주입받도록 수정 필요
        return "fileflow-uploads-prod";
    }

    /**
     * S3 경로 prefix를 반환한다 (조직별 기본 경로).
     *
     * <p>경로 구조:
     *
     * <ul>
     *   <li>Admin: "connectly/"
     *   <li>Seller: "setof/seller-{organizationId}/" (전체 UUID 사용)
     *   <li>Customer: "setof/customer/"
     * </ul>
     *
     * @return S3 경로 prefix (namespace 포함)
     */
    public String getS3PathPrefix() {
        if (isAdmin()) {
            return namespace + "/"; // "connectly/"
        } else if (isSeller()) {
            // Option A 확정: 전체 UUID 사용
            return namespace
                    + "/seller-"
                    + id.value()
                    + "/"; // "setof/seller-01912345-6789-7abc.../
        } else {
            return namespace + "/customer/"; // "setof/customer/"
        }
    }

    /**
     * CDN 접근용 Public S3 경로 prefix를 반환한다.
     *
     * <p>CloudFront CDN(cdn.set-of.com)을 통해 공개 접근이 가능한 경로입니다. {@code uploads/} prefix 하위에 저장되며,
     * CDN을 통해 직접 접근 가능합니다.
     *
     * <p><strong>사용 대상 카테고리</strong>: BANNER, PRODUCT_IMAGE, HTML
     *
     * <p><strong>경로 구조</strong>:
     *
     * <ul>
     *   <li>Admin: "uploads/connectly/"
     *   <li>Seller: "uploads/setof/seller-{organizationId}/"
     *   <li>Customer: "uploads/setof/customer/"
     * </ul>
     *
     * @return CDN 접근용 S3 경로 prefix
     */
    public String getPublicS3PathPrefix() {
        return PUBLIC_PREFIX + getS3PathPrefix();
    }

    /**
     * 내부 전용 S3 경로 prefix를 반환한다.
     *
     * <p>CDN을 통해 접근할 수 없는 내부 전용 파일 경로입니다. {@code internal/} prefix 하위에 저장되며, 서명된 URL 또는 내부 서비스를
     * 통해서만 접근 가능합니다.
     *
     * <p><strong>사용 대상 카테고리</strong>: EXCEL, SALES_MATERIAL, DOCUMENT
     *
     * <p><strong>사용 대상 변형</strong>: ORIGINAL (원본 파일)
     *
     * <p><strong>경로 구조</strong>:
     *
     * <ul>
     *   <li>Admin: "internal/connectly/"
     *   <li>Seller: "internal/setof/seller-{organizationId}/"
     *   <li>Customer: "internal/setof/customer/"
     * </ul>
     *
     * @return 내부 전용 S3 경로 prefix
     */
    public String getInternalS3PathPrefix() {
        return INTERNAL_PREFIX + getS3PathPrefix();
    }

    /**
     * Role과 Namespace, ID 일관성을 검증한다.
     *
     * @param role 조직 역할
     * @param namespace 네임스페이스
     * @param id 조직 ID (nullable)
     * @throws IllegalArgumentException 일관성 위반 시
     */
    private static void validateRoleNamespaceConsistency(
            UserRole role, String namespace, OrganizationId id) {
        // SuperAdmin/Admin 조직: role=SUPER_ADMIN/ADMIN, namespace=connectly, id=null
        if (role == UserRole.SUPER_ADMIN || role == UserRole.ADMIN) {
            if (!CONNECTLY_NAMESPACE.equals(namespace)) {
                throw new IllegalArgumentException("Admin 조직은 connectly namespace여야 합니다.");
            }
            if (id != null) {
                throw new IllegalArgumentException("Admin 조직은 OrganizationId를 가질 수 없습니다.");
            }
        }

        // Seller 조직: role=SELLER, namespace=setof, id=필수
        if (role == UserRole.SELLER) {
            if (!SETOF_NAMESPACE.equals(namespace)) {
                throw new IllegalArgumentException("Seller 조직은 setof namespace여야 합니다.");
            }
            if (id == null) {
                throw new IllegalArgumentException("Seller 조직은 OrganizationId가 필수입니다.");
            }
        }

        // Customer 조직: role=DEFAULT, namespace=setof, id=null
        if (role == UserRole.DEFAULT) {
            if (!SETOF_NAMESPACE.equals(namespace)) {
                throw new IllegalArgumentException("Customer 조직은 setof namespace여야 합니다.");
            }
            if (id != null) {
                throw new IllegalArgumentException("Customer 조직은 OrganizationId를 가질 수 없습니다.");
            }
        }
    }
}
