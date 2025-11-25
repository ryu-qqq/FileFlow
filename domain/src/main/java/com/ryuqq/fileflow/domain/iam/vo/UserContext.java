package com.ryuqq.fileflow.domain.iam.vo;

import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.UploadCategory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 사용자 컨텍스트 Value Object (토큰 기반).
 *
 * <p><strong>토큰 정보 구조</strong>:
 *
 * <ul>
 *   <li>ADMIN: email (이메일 주소)
 *   <li>SELLER: email (이메일 주소) + organizationId (입점사 ID)
 *   <li>CUSTOMER: userId (손님 ID)
 * </ul>
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>테넌트는 항상 존재해야 한다.
 *   <li>조직은 항상 존재해야 한다.
 *   <li>Admin/Seller는 email이 필수이다.
 *   <li>Customer는 userId가 필수이다.
 *   <li>S3 경로는 조직/역할/날짜/카테고리 기반으로 자동 생성된다.
 * </ul>
 *
 * @param tenant 테넌트 정보
 * @param organization 조직 정보
 * @param email 이메일 주소 (Admin/Seller 전용, Customer는 null)
 * @param userId 사용자 ID (Customer 전용, Admin/Seller는 null)
 */
public record UserContext(Tenant tenant, Organization organization, String email, Long userId) {

    /** Compact Constructor (검증 로직). */
    public UserContext {
        if (tenant == null) {
            throw new IllegalArgumentException("테넌트는 null일 수 없습니다.");
        }

        if (organization == null) {
            throw new IllegalArgumentException("조직은 null일 수 없습니다.");
        }

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
        return new UserContext(Tenant.connectly(), Organization.admin(), email, null);
    }

    /**
     * Seller 사용자 컨텍스트 생성.
     *
     * @param organizationId 입점사 조직 ID
     * @param companyName 입점사명
     * @param email 판매자 이메일
     * @return Seller UserContext
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static UserContext seller(long organizationId, String companyName, String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Seller 이메일은 null이거나 빈 문자열일 수 없습니다.");
        }
        return new UserContext(
                Tenant.connectly(), Organization.seller(organizationId, companyName), email, null);
    }

    /**
     * Customer 사용자 컨텍스트 생성.
     *
     * @param userId 손님 사용자 ID
     * @return Customer UserContext
     * @throws IllegalArgumentException userId가 null이거나 0 이하인 경우
     */
    public static UserContext customer(long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Customer userId는 1 이상이어야 합니다: " + userId);
        }
        return new UserContext(Tenant.connectly(), Organization.customer(), null, userId);
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
            Tenant tenant, Organization organization, String email, Long userId) {
        return new UserContext(tenant, organization, email, userId);
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
     * S3 버킷을 반환한다.
     *
     * @return S3 버킷 (조직 기반)
     */
    public S3Bucket getS3Bucket() {
        return S3Bucket.of(organization.getS3BucketName());
    }

    /**
     * S3 객체 키를 생성한다 (업로드 카테고리 포함).
     *
     * <p>경로 구조:
     *
     * <ul>
     *   <li>Admin: admin/{category}/{yyyy}/{MM}/{filename}
     *   <li>Seller: seller-{organizationId}/{category}/{yyyy}/{MM}/{filename}
     *   <li>Customer: customer/{yyyy}/{MM}/{filename} (카테고리 없음)
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

        String basePath =
                organization
                        .getS3PathPrefix(); // Admin: "admin/", Seller: "seller-{orgId}/", Customer:
        // "customer/"

        // Admin/Seller: 카테고리 필수
        if (isAdmin() || isSeller()) {
            if (uploadCategory == null) {
                throw new IllegalArgumentException("Admin/Seller는 업로드 카테고리가 필수입니다.");
            }
            return S3Key.fromSegments(basePath + uploadCategory.getPath(), year, month, fileName);
        } else {
            // Customer: 카테고리 불필요, customer/{yyyy}/{MM}/{filename}
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
     * 사용자 식별자를 반환한다 (로깅/추적용).
     *
     * @return Admin/Seller: email, Customer: "user-{userId}"
     */
    public String getUserIdentifier() {
        if (isAdmin() || isSeller()) {
            return email;
        } else {
            return "user-" + userId;
        }
    }

    /**
     * 조직 ID를 반환한다.
     *
     * @return 조직 ID
     */
    public long getOrganizationId() {
        return organization.id();
    }

    /**
     * Role별 필수 필드를 검증한다.
     *
     * @param role 사용자 역할
     * @param email 이메일 (선택적)
     * @param userId 사용자 ID (선택적)
     * @throws IllegalArgumentException 검증 실패 시
     */
    private static void validateRequiredFields(UserRole role, String email, Long userId) {
        if (role == UserRole.ADMIN || role == UserRole.SELLER) {
            // Admin/Seller는 email 필수
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException(role + " 사용자는 email이 필수입니다.");
            }
            if (userId != null) {
                throw new IllegalArgumentException(role + " 사용자는 userId를 가질 수 없습니다.");
            }
        } else if (role == UserRole.DEFAULT) {
            // Customer는 userId 필수
            if (userId == null || userId <= 0) {
                throw new IllegalArgumentException("DEFAULT 사용자는 userId가 필수이며 1 이상이어야 합니다.");
            }
            if (email != null && !email.isBlank()) {
                throw new IllegalArgumentException("DEFAULT 사용자는 email을 가질 수 없습니다.");
            }
        }
    }
}
