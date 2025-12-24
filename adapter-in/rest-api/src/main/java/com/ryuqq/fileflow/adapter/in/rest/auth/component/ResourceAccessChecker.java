package com.ryuqq.fileflow.adapter.in.rest.auth.component;

import com.ryuqq.fileflow.adapter.in.rest.auth.paths.SecurityPaths;
import com.ryuqq.fileflow.application.common.context.UserContextHolder;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * 리소스 접근 권한 검사기.
 *
 * <p>@PreAuthorize 어노테이션에서 SpEL 함수로 사용합니다.
 *
 * <p><strong>사용 예시</strong>:
 *
 * <pre>{@code
 * // Controller 메서드에서 사용
 * @PreAuthorize("@access.authenticated()")
 * public void listFiles() { ... }
 *
 * @PreAuthorize("@access.hasPermission('file:write')")
 * public void uploadFile() { ... }
 *
 * @PreAuthorize("@access.superAdmin() or @access.hasPermission('file:delete')")
 * public void deleteFile() { ... }
 *
 * @PreAuthorize("@access.sameTenant(#tenantId)")
 * public void getTenantFiles(@PathVariable String tenantId) { ... }
 * }</pre>
 *
 * <p><strong>권한 상수</strong> (SecurityPaths.Permissions):
 *
 * <ul>
 *   <li>file:read - 파일 조회
 *   <li>file:write - 파일 업로드/생성
 *   <li>file:delete - 파일 삭제
 *   <li>file:download - 파일 다운로드
 * </ul>
 *
 * <p><strong>리소스 격리 규칙</strong>:
 *
 * <ul>
 *   <li>SUPER_ADMIN: 모든 리소스 접근 가능
 *   <li>ADMIN: 자기 테넌트 내 리소스
 *   <li>SELLER: 자기 조직 내 리소스
 *   <li>DEFAULT: 자기 자신의 리소스만
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component("access")
@SuppressWarnings("PMD.TooManyMethods")
public class ResourceAccessChecker {

    /**
     * 인증된 사용자인지 확인합니다.
     *
     * @return 인증되었으면 true
     */
    public boolean authenticated() {
        return getUserContext() != null;
    }

    /**
     * 현재 사용자가 SUPER_ADMIN인지 확인합니다.
     *
     * @return SUPER_ADMIN이면 true
     */
    public boolean superAdmin() {
        UserContext context = getUserContext();
        return context != null && context.isSuperAdmin();
    }

    /**
     * 현재 사용자가 ADMIN인지 확인합니다.
     *
     * @return ADMIN이면 true
     */
    public boolean admin() {
        UserContext context = getUserContext();
        return context != null && context.isAdmin();
    }

    /**
     * 현재 사용자가 SELLER인지 확인합니다.
     *
     * @return SELLER이면 true
     */
    public boolean seller() {
        UserContext context = getUserContext();
        return context != null && context.isSeller();
    }

    /**
     * 현재 사용자가 DEFAULT(Customer)인지 확인합니다.
     *
     * @return DEFAULT이면 true
     */
    public boolean customer() {
        UserContext context = getUserContext();
        return context != null && context.isCustomer();
    }

    /**
     * 특정 권한 보유 여부를 확인합니다.
     *
     * <p>SUPER_ADMIN 또는 SYSTEM은 모든 권한을 가집니다.
     *
     * @param permission 확인할 권한 (예: file:read, file:write)
     * @return 권한이 있으면 true
     */
    public boolean hasPermission(String permission) {
        UserContext context = getUserContext();
        if (context == null) {
            return false;
        }
        if (context.isSuperAdmin() || context.isSystem()) {
            return true;
        }
        return context.hasPermission(permission);
    }

    /**
     * 파일 읽기 권한 보유 여부를 확인합니다.
     *
     * @return file:read 권한이 있으면 true
     */
    public boolean canRead() {
        return hasPermission(SecurityPaths.Permissions.FILE_READ);
    }

    /**
     * 파일 쓰기 권한 보유 여부를 확인합니다.
     *
     * @return file:write 권한이 있으면 true
     */
    public boolean canWrite() {
        return hasPermission(SecurityPaths.Permissions.FILE_WRITE);
    }

    /**
     * 파일 삭제 권한 보유 여부를 확인합니다.
     *
     * @return file:delete 권한이 있으면 true
     */
    public boolean canDelete() {
        return hasPermission(SecurityPaths.Permissions.FILE_DELETE);
    }

    /**
     * 파일 다운로드 권한 보유 여부를 확인합니다.
     *
     * @return file:download 권한이 있으면 true
     */
    public boolean canDownload() {
        return hasPermission(SecurityPaths.Permissions.FILE_DOWNLOAD);
    }

    /**
     * 여러 권한 중 하나라도 보유 여부를 확인합니다.
     *
     * @param permissions 확인할 권한들
     * @return 하나라도 있으면 true
     */
    public boolean hasAnyPermission(String... permissions) {
        UserContext context = getUserContext();
        if (context == null) {
            return false;
        }
        if (context.isSuperAdmin()) {
            return true;
        }
        return context.hasAnyPermission(permissions);
    }

    /**
     * 특정 역할 보유 여부를 확인합니다.
     *
     * @param role 확인할 역할
     * @return 역할이 있으면 true
     */
    public boolean hasRole(String role) {
        UserContext context = getUserContext();
        return context != null && context.hasRole(role);
    }

    /**
     * 여러 역할 중 하나라도 보유 여부를 확인합니다.
     *
     * @param roles 확인할 역할들
     * @return 하나라도 있으면 true
     */
    public boolean hasAnyRole(String... roles) {
        UserContext context = getUserContext();
        return context != null && context.hasAnyRole(roles);
    }

    /**
     * 현재 사용자가 해당 테넌트 소속인지 확인합니다.
     *
     * <p>SUPER_ADMIN은 모든 테넌트에 접근 가능합니다.
     *
     * @param tenantId 확인할 테넌트 ID
     * @return 해당 테넌트 소속이거나 SUPER_ADMIN이면 true
     */
    public boolean sameTenant(String tenantId) {
        UserContext context = getUserContext();
        if (context == null) {
            return false;
        }
        if (context.isSuperAdmin()) {
            return true;
        }
        return Objects.equals(context.getTenantId(), tenantId);
    }

    /**
     * 현재 사용자가 해당 조직 소속인지 확인합니다.
     *
     * <p>SUPER_ADMIN과 ADMIN은 자기 테넌트 내 모든 조직에 접근 가능합니다.
     *
     * @param organizationId 확인할 조직 ID
     * @return 해당 조직 소속이거나 상위 권한이면 true
     */
    public boolean sameOrganization(String organizationId) {
        UserContext context = getUserContext();
        if (context == null) {
            return false;
        }
        if (context.isSuperAdmin() || context.isAdmin()) {
            return true;
        }
        return Objects.equals(context.getOrganizationId(), organizationId);
    }

    /**
     * 현재 사용자가 요청한 userId 본인인지 확인합니다.
     *
     * @param userId 확인할 사용자 ID
     * @return 본인이면 true
     */
    public boolean myself(String userId) {
        UserContext context = getUserContext();
        if (context == null || context.userId() == null) {
            return false;
        }
        return Objects.equals(context.userId().value(), userId);
    }

    /**
     * 본인이거나 특정 권한이 있는지 확인합니다.
     *
     * @param userId 확인할 사용자 ID
     * @param permission 필요한 권한
     * @return 본인이거나 권한이 있으면 true
     */
    public boolean myselfOr(String userId, String permission) {
        return myself(userId) || hasPermission(permission);
    }

    /**
     * 파일 리소스 접근 권한을 확인합니다.
     *
     * <p>자기 테넌트/조직의 파일에 대해 해당 action 권한이 있는지 확인합니다.
     *
     * @param action 필요한 액션 (read, write, delete, download)
     * @return 접근 가능하면 true
     */
    public boolean file(String action) {
        return hasPermission("file:" + action);
    }

    /**
     * 현재 스레드의 UserContext를 반환합니다.
     *
     * @return UserContext 또는 null
     */
    private UserContext getUserContext() {
        return UserContextHolder.get();
    }
}
