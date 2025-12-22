package com.ryuqq.fileflow.domain.iam.vo;

import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.UploadCategory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * 사용자 컨텍스트 Value Object (Gateway 헤더 기반).
 *
 * <p><strong>Gateway 헤더 정보 구조</strong>:
 *
 * <ul>
 *   <li>X-Tenant-Id: 테넌트 ID (UUIDv7)
 *   <li>X-Organization-Id: 조직 ID (UUIDv7)
 *   <li>X-User-Id: 사용자 ID (UUIDv7)
 *   <li>X-Roles: 역할 목록 (JSON 배열, 예: ["SUPER_ADMIN"])
 *   <li>X-Permissions: 권한 목록 (콤마 구분, 예: "file:read,file:write")
 * </ul>
 *
 * <p><strong>서비스 간 호출 헤더</strong>:
 *
 * <ul>
 *   <li>X-Service-Token: 서비스 인증 토큰
 *   <li>X-Service-Name: 호출 서비스 식별자 (예: "setof-server", "partner-admin")
 * </ul>
 *
 * <p><strong>JWT Payload에서 추가 추출</strong>:
 *
 * <ul>
 *   <li>tenant_name: 테넌트명
 *   <li>org_name: 조직명
 *   <li>email: 이메일 주소
 * </ul>
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>테넌트는 항상 존재해야 한다.
 *   <li>조직은 항상 존재해야 한다.
 *   <li>Admin/Seller는 email이 필수이다.
 *   <li>Customer는 userId가 필수이다 (UUIDv7).
 *   <li>roles는 불변 리스트로 저장된다.
 *   <li>permissions는 불변 리스트로 저장된다.
 *   <li>S3 경로는 조직/역할/날짜/카테고리 기반으로 자동 생성된다.
 *   <li>serviceName은 서비스 간 호출 시에만 설정된다 (일반 사용자 요청은 null).
 * </ul>
 *
 * @param tenant 테넌트 정보
 * @param organization 조직 정보
 * @param email 이메일 주소 (Admin/Seller 전용, Customer는 null)
 * @param userId 사용자 ID (UUIDv7 기반)
 * @param roles 역할 목록 (불변, 예: ["SUPER_ADMIN", "ADMIN"])
 * @param permissions 권한 목록 (불변, 예: ["file:read", "file:write"])
 * @param serviceName 호출 서비스 이름 (서비스 간 호출 시에만 설정, 일반 사용자 요청은 null)
 */
public record UserContext(
        Tenant tenant,
        Organization organization,
        String email,
        UserId userId,
        List<String> roles,
        List<String> permissions,
        String serviceName) {

    // Well-Known System 사용자 ID (UUIDv7)
    private static final String SYSTEM_USER_ID = "019b2b35-3979-75ba-a981-84ae15f0572a";

    // Well-Known System 사용자 이메일
    private static final String SYSTEM_USER_EMAIL = "master@connectly.co.kr";

    /** Compact Constructor (검증 로직). */
    public UserContext {
        if (tenant == null) {
            throw new IllegalArgumentException("테넌트는 null일 수 없습니다.");
        }

        if (organization == null) {
            throw new IllegalArgumentException("조직은 null일 수 없습니다.");
        }

        // roles, permissions 불변 리스트로 변환 (null 방어)
        roles = roles != null ? Collections.unmodifiableList(roles) : Collections.emptyList();
        permissions =
                permissions != null
                        ? Collections.unmodifiableList(permissions)
                        : Collections.emptyList();

        // Role별 필수 필드 검증
        validateRequiredFields(organization.role(), email, userId);
    }

    /**
     * Admin 사용자 컨텍스트 생성.
     *
     * @param email 관리자 이메일
     * @return Admin UserContext
     * @throws IllegalArgumentException email이 null이거나 빈 문자열인 경우
     */
    public static UserContext admin(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Admin 이메일은 null이거나 빈 문자열일 수 없습니다.");
        }
        return new UserContext(
                Tenant.connectly(),
                Organization.admin(),
                email,
                null,
                List.of("ADMIN"),
                Collections.emptyList(),
                null);
    }

    /**
     * Admin 사용자 컨텍스트 생성 (roles, permissions 포함).
     *
     * @param email 관리자 이메일
     * @param roles 역할 목록
     * @param permissions 권한 목록
     * @return Admin UserContext
     * @throws IllegalArgumentException email이 null이거나 빈 문자열인 경우
     */
    public static UserContext admin(String email, List<String> roles, List<String> permissions) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Admin 이메일은 null이거나 빈 문자열일 수 없습니다.");
        }
        return new UserContext(
                Tenant.connectly(), Organization.admin(), email, null, roles, permissions, null);
    }

    /**
     * System 내부 호출 컨텍스트 생성.
     *
     * <p>서버 간 통신에서 Service Token 인증 시 사용된다. 최상위 권한을 가지며, 모든 리소스에 접근 가능하다.
     *
     * <p>serviceName이 없는 레거시 호출에 사용됩니다. 신규 코드에서는 {@link #system(String)}을 사용하세요.
     *
     * @return System UserContext (serviceName=null)
     */
    public static UserContext system() {
        return new UserContext(
                Tenant.connectly(),
                Organization.system(),
                SYSTEM_USER_EMAIL,
                UserId.of(SYSTEM_USER_ID),
                List.of("SYSTEM"),
                Collections.emptyList(),
                null);
    }

    /**
     * System 내부 호출 컨텍스트 생성 (서비스명 포함).
     *
     * <p>서버 간 통신에서 Service Token 인증 시 사용된다. 최상위 권한을 가지며, 모든 리소스에 접근 가능하다.
     *
     * <p>serviceName을 통해 어떤 서비스에서 호출했는지 추적할 수 있습니다.
     *
     * @param serviceName 호출 서비스 이름 (예: "setof-server", "partner-admin")
     * @return System UserContext
     */
    public static UserContext system(String serviceName) {
        return new UserContext(
                Tenant.connectly(),
                Organization.system(),
                SYSTEM_USER_EMAIL,
                UserId.of(SYSTEM_USER_ID),
                List.of("SYSTEM"),
                Collections.emptyList(),
                serviceName);
    }

    /**
     * Seller 사용자 컨텍스트 생성.
     *
     * @param organizationId 입점사 조직 ID (UUIDv7)
     * @param companyName 입점사명
     * @param email 판매자 이메일
     * @return Seller UserContext
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static UserContext seller(
            OrganizationId organizationId, String companyName, String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Seller 이메일은 null이거나 빈 문자열일 수 없습니다.");
        }
        return new UserContext(
                Tenant.connectly(),
                Organization.seller(organizationId, companyName),
                email,
                null,
                List.of("SELLER"),
                Collections.emptyList(),
                null);
    }

    /**
     * Seller 사용자 컨텍스트 생성 (roles, permissions 포함).
     *
     * @param organizationId 입점사 조직 ID (UUIDv7)
     * @param companyName 입점사명
     * @param email 판매자 이메일
     * @param roles 역할 목록
     * @param permissions 권한 목록
     * @return Seller UserContext
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static UserContext seller(
            OrganizationId organizationId,
            String companyName,
            String email,
            List<String> roles,
            List<String> permissions) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Seller 이메일은 null이거나 빈 문자열일 수 없습니다.");
        }
        return new UserContext(
                Tenant.connectly(),
                Organization.seller(organizationId, companyName),
                email,
                null,
                roles,
                permissions,
                null);
    }

    /**
     * Customer 사용자 컨텍스트 생성.
     *
     * @param userId 손님 사용자 ID (UUIDv7)
     * @return Customer UserContext
     * @throws IllegalArgumentException userId가 null인 경우
     */
    public static UserContext customer(UserId userId) {
        if (userId == null) {
            throw new IllegalArgumentException("Customer userId는 null일 수 없습니다.");
        }
        return new UserContext(
                Tenant.connectly(),
                Organization.customer(),
                null,
                userId,
                List.of("DEFAULT"),
                Collections.emptyList(),
                null);
    }

    /**
     * Customer 사용자 컨텍스트 생성 (roles, permissions 포함).
     *
     * @param userId 손님 사용자 ID (UUIDv7)
     * @param roles 역할 목록
     * @param permissions 권한 목록
     * @return Customer UserContext
     * @throws IllegalArgumentException userId가 null인 경우
     */
    public static UserContext customer(
            UserId userId, List<String> roles, List<String> permissions) {
        if (userId == null) {
            throw new IllegalArgumentException("Customer userId는 null일 수 없습니다.");
        }
        return new UserContext(
                Tenant.connectly(),
                Organization.customer(),
                null,
                userId,
                roles,
                permissions,
                null);
    }

    /**
     * 일반 생성 메서드 (테스트/복원용).
     *
     * @param tenant 테넌트
     * @param organization 조직
     * @param email 이메일 (선택적)
     * @param userId 사용자 ID (선택적)
     * @return UserContext
     */
    public static UserContext of(
            Tenant tenant, Organization organization, String email, UserId userId) {
        return new UserContext(
                tenant,
                organization,
                email,
                userId,
                Collections.emptyList(),
                Collections.emptyList(),
                null);
    }

    /**
     * 전체 필드를 받는 일반 생성 메서드 (serviceName 제외).
     *
     * @param tenant 테넌트
     * @param organization 조직
     * @param email 이메일 (선택적)
     * @param userId 사용자 ID (선택적)
     * @param roles 역할 목록
     * @param permissions 권한 목록
     * @return UserContext
     */
    public static UserContext of(
            Tenant tenant,
            Organization organization,
            String email,
            UserId userId,
            List<String> roles,
            List<String> permissions) {
        return new UserContext(tenant, organization, email, userId, roles, permissions, null);
    }

    /**
     * 전체 필드를 받는 일반 생성 메서드 (serviceName 포함).
     *
     * @param tenant 테넌트
     * @param organization 조직
     * @param email 이메일 (선택적)
     * @param userId 사용자 ID (선택적)
     * @param roles 역할 목록
     * @param permissions 권한 목록
     * @param serviceName 호출 서비스 이름 (선택적)
     * @return UserContext
     */
    public static UserContext of(
            Tenant tenant,
            Organization organization,
            String email,
            UserId userId,
            List<String> roles,
            List<String> permissions,
            String serviceName) {
        return new UserContext(
                tenant, organization, email, userId, roles, permissions, serviceName);
    }

    /**
     * 사용자 역할을 반환한다.
     *
     * @return UserRole (ADMIN, SELLER, DEFAULT)
     */
    public UserRole getRole() {
        return organization.role();
    }

    /**
     * Admin 사용자인지 확인한다.
     *
     * @return Admin이면 true
     */
    public boolean isAdmin() {
        return organization.isAdmin();
    }

    /**
     * System 내부 호출인지 확인한다.
     *
     * @return System이면 true
     */
    public boolean isSystem() {
        return organization.isSystem();
    }

    /**
     * Seller 사용자인지 확인한다.
     *
     * @return Seller이면 true
     */
    public boolean isSeller() {
        return organization.isSeller();
    }

    /**
     * Customer 사용자인지 확인한다.
     *
     * @return Customer이면 true
     */
    public boolean isCustomer() {
        return organization.isCustomer();
    }

    /**
     * 서비스 간 호출인지 확인한다.
     *
     * <p>serviceName이 설정되어 있으면 서비스 간 호출로 판단한다.
     *
     * @return 서비스 간 호출이면 true
     */
    public boolean isServiceCall() {
        return serviceName != null && !serviceName.isBlank();
    }

    /**
     * 호출 서비스 이름을 반환한다.
     *
     * <p>서비스 간 호출이 아닌 경우 null을 반환한다.
     *
     * @return 서비스 이름 (nullable)
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * S3 버킷을 반환한다.
     *
     * @return S3 버킷 (조직 기반)
     */
    public S3Bucket getS3Bucket() {
        return S3Bucket.of(organization.getS3BucketName());
    }

    /**
     * S3 객체 키를 생성한다 (업로드 카테고리 포함, CDN 접근 여부에 따라 경로 분기).
     *
     * <p><strong>CDN 경로 분기</strong>:
     *
     * <ul>
     *   <li>CDN 접근 필요 (BANNER, PRODUCT_IMAGE, HTML): {@code uploads/} prefix 사용
     *   <li>내부 전용 (EXCEL, SALES_MATERIAL, DOCUMENT): {@code internal/} prefix 사용
     *   <li>Customer (카테고리 없음): {@code uploads/} prefix 사용 (공개 이미지)
     * </ul>
     *
     * <p><strong>경로 구조</strong>:
     *
     * <ul>
     *   <li>System: internal/connectly/system/{category}/{yyyy}/{MM}/{filename}
     *   <li>Admin CDN: uploads/connectly/{category}/{yyyy}/{MM}/{filename}
     *   <li>Admin Internal: internal/connectly/{category}/{yyyy}/{MM}/{filename}
     *   <li>Seller CDN: uploads/setof/seller-{id}/{category}/{yyyy}/{MM}/{filename}
     *   <li>Seller Internal: internal/setof/seller-{id}/{category}/{yyyy}/{MM}/{filename}
     *   <li>Customer: uploads/setof/customer/{yyyy}/{MM}/{filename} (카테고리 없음)
     * </ul>
     *
     * @param uploadCategory 업로드 카테고리 (Admin/Seller 전용, Customer는 null)
     * @param fileName 파일명
     * @param uploadDate 업로드 날짜
     * @return S3Key
     */
    public S3Key generateS3Key(
            UploadCategory uploadCategory, String fileName, LocalDate uploadDate) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("파일명은 null이거나 빈 문자열일 수 없습니다.");
        }

        if (uploadDate == null) {
            throw new IllegalArgumentException("업로드 날짜는 null일 수 없습니다.");
        }

        String year = uploadDate.format(DateTimeFormatter.ofPattern("yyyy"));
        String month = uploadDate.format(DateTimeFormatter.ofPattern("MM"));

        // System/Admin/Seller: 카테고리 필수
        if (isSystem() || isAdmin() || isSeller()) {
            if (uploadCategory == null) {
                throw new IllegalArgumentException("System/Admin/Seller는 업로드 카테고리가 필수입니다.");
            }

            // System은 항상 internal 경로 사용
            if (isSystem()) {
                String basePath = organization.getInternalS3PathPrefix();
                return S3Key.fromSegments(
                        basePath + uploadCategory.getPath(), year, month, fileName);
            }

            // CDN 접근 여부에 따라 경로 분기
            String basePath =
                    uploadCategory.requiresCdnAccess()
                            ? organization.getPublicS3PathPrefix()
                            : organization.getInternalS3PathPrefix();

            return S3Key.fromSegments(basePath + uploadCategory.getPath(), year, month, fileName);
        } else {
            // Customer: 카테고리 불필요, 공개 경로 사용
            String basePath = organization.getPublicS3PathPrefix();
            return S3Key.fromSegments(basePath + year, month, fileName);
        }
    }

    /**
     * S3 객체 키를 생성한다 (오늘 날짜 기준).
     *
     * @param uploadCategory 업로드 카테고리 (Admin/Seller 전용, Customer는 null)
     * @param fileName 파일명
     * @return S3Key
     */
    public S3Key generateS3KeyToday(UploadCategory uploadCategory, String fileName) {
        return generateS3Key(uploadCategory, fileName, LocalDate.now());
    }

    /**
     * SYSTEM 토큰 전용: customPath로 S3 경로 직접 지정.
     *
     * <p>internal/ prefix가 자동으로 추가됩니다. 날짜 경로(yyyy/MM/)는 추가되지 않습니다.
     *
     * <p><strong>경로 검증 규칙</strong>:
     *
     * <ul>
     *   <li>customPath는 null이거나 빈 문자열일 수 없습니다.
     *   <li>customPath에 '..'을 포함할 수 없습니다 (path traversal 방지).
     *   <li>customPath는 '/'로 시작할 수 없습니다 (상대 경로만 허용).
     * </ul>
     *
     * @param customPath 사용자 지정 경로 (예: "applications/seller-123/docs")
     * @param fileName 파일명
     * @return S3Key (예: "internal/applications/seller-123/docs/file.pdf")
     * @throws IllegalStateException SYSTEM이 아닌 경우
     * @throws IllegalArgumentException customPath 또는 fileName이 유효하지 않은 경우
     */
    public S3Key generateS3KeyWithCustomPath(String customPath, String fileName) {
        if (!isSystem()) {
            throw new IllegalStateException("customPath는 SYSTEM 토큰에서만 사용 가능합니다.");
        }

        validateCustomPath(customPath);
        validateFileName(fileName);

        String normalizedPath =
                customPath.endsWith("/")
                        ? customPath.substring(0, customPath.length() - 1)
                        : customPath;

        return S3Key.of("internal/" + normalizedPath + "/" + fileName);
    }

    private void validateCustomPath(String customPath) {
        if (customPath == null || customPath.isBlank()) {
            throw new IllegalArgumentException("customPath는 null이거나 빈 문자열일 수 없습니다.");
        }
        if (customPath.contains("..")) {
            throw new IllegalArgumentException("customPath에 '..'을 포함할 수 없습니다.");
        }
        if (customPath.startsWith("/")) {
            throw new IllegalArgumentException("customPath는 '/'로 시작할 수 없습니다.");
        }
    }

    private void validateFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("파일명은 null이거나 빈 문자열일 수 없습니다.");
        }
    }

    /**
     * 사용자 식별자를 반환한다 (로깅/추적용).
     *
     * @return System: "SYSTEM", Admin/Seller: email, Customer: "user-{userId}"
     */
    public String getUserIdentifier() {
        if (isSystem()) {
            return "SYSTEM";
        }
        if (isAdmin() || isSeller()) {
            return email;
        } else {
            return "user-" + userId.value();
        }
    }

    /**
     * 조직 ID를 반환한다.
     *
     * <p>Admin/Customer는 OrganizationId가 null이므로 null을 반환할 수 있다.
     *
     * @return 조직 ID (nullable)
     */
    public OrganizationId getOrganizationId() {
        return organization.id();
    }

    /**
     * 테넌트 ID를 반환한다.
     *
     * @return 테넌트 ID
     */
    public TenantId getTenantId() {
        return tenant.id();
    }

    /**
     * 특정 역할을 가지고 있는지 확인한다.
     *
     * @param role 역할 문자열 (예: "SUPER_ADMIN", "ADMIN")
     * @return 해당 역할을 가지고 있으면 true
     */
    public boolean hasRole(String role) {
        if (role == null || role.isBlank()) {
            return false;
        }
        return roles.contains(role.toUpperCase());
    }

    /**
     * 특정 권한을 가지고 있는지 확인한다.
     *
     * @param permission 권한 문자열 (예: "file:read", "file:write")
     * @return 해당 권한을 가지고 있으면 true
     */
    public boolean hasPermission(String permission) {
        if (permission == null || permission.isBlank()) {
            return false;
        }
        return permissions.contains(permission);
    }

    /**
     * 여러 역할 중 하나라도 가지고 있는지 확인한다.
     *
     * @param targetRoles 확인할 역할 목록
     * @return 하나라도 가지고 있으면 true
     */
    public boolean hasAnyRole(String... targetRoles) {
        if (targetRoles == null || targetRoles.length == 0) {
            return false;
        }
        for (String role : targetRoles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 여러 권한 중 하나라도 가지고 있는지 확인한다.
     *
     * @param targetPermissions 확인할 권한 목록
     * @return 하나라도 가지고 있으면 true
     */
    public boolean hasAnyPermission(String... targetPermissions) {
        if (targetPermissions == null || targetPermissions.length == 0) {
            return false;
        }
        for (String permission : targetPermissions) {
            if (hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 슈퍼 관리자인지 확인한다.
     *
     * @return SUPER_ADMIN 역할을 가지고 있으면 true
     */
    public boolean isSuperAdmin() {
        return hasRole("SUPER_ADMIN");
    }

    /**
     * 가장 높은 우선순위의 UserRole을 반환한다.
     *
     * <p>roles 목록에서 가장 높은 우선순위의 역할을 추출하여 UserRole enum으로 반환한다.
     *
     * @return 가장 높은 우선순위의 UserRole
     */
    public UserRole getPrimaryRole() {
        return UserRole.highestPriority(roles);
    }

    /**
     * Role별 필수 필드를 검증한다.
     *
     * @param role 사용자 역할
     * @param email 이메일 (선택적)
     * @param userId 사용자 ID (선택적)
     * @throws IllegalArgumentException 검증 실패 시
     */
    private static void validateRequiredFields(UserRole role, String email, UserId userId) {
        if (role == UserRole.SYSTEM) {
            // SYSTEM은 email/userId 검증 없음 (내부 호출용)
            return;
        }
        if (role == UserRole.SUPER_ADMIN || role == UserRole.ADMIN || role == UserRole.SELLER) {
            // SuperAdmin/Admin/Seller는 email 필수, userId는 선택적
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException(role + " 사용자는 email이 필수입니다.");
            }
            // userId는 AuthHub JWT의 sub 클레임으로 전달되므로 허용
        } else if (role == UserRole.DEFAULT) {
            // Customer는 userId 필수
            if (userId == null) {
                throw new IllegalArgumentException("DEFAULT 사용자는 userId가 필수입니다.");
            }
            if (email != null && !email.isBlank()) {
                throw new IllegalArgumentException("DEFAULT 사용자는 email을 가질 수 없습니다.");
            }
        }
    }
}
