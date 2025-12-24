package com.ryuqq.fileflow.adapter.in.rest.auth.component;

import com.ryuqq.auth.common.access.BaseAccessChecker;
import com.ryuqq.auth.common.context.SecurityContext;
import com.ryuqq.fileflow.adapter.in.rest.auth.paths.SecurityPaths;
import com.ryuqq.fileflow.application.common.context.UserContextHolder;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import java.util.Arrays;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * 리소스 접근 권한 검사기.
 *
 * <p>BaseAccessChecker를 확장하여 공통 권한 검사 로직을 재사용합니다.
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
public class ResourceAccessChecker extends BaseAccessChecker {

    // ========================================
    // SecurityContext Provider (BaseAccessChecker 오버라이드)
    // ========================================

    /**
     * 현재 스레드의 SecurityContext를 반환합니다.
     *
     * <p>fileflow 프로젝트의 UserContextHolder를 사용합니다.
     *
     * @return SecurityContext (UserContext) 또는 null
     */
    @Override
    protected SecurityContext getSecurityContext() {
        return UserContextHolder.get();
    }

    /**
     * 현재 스레드의 UserContext를 반환합니다.
     *
     * <p>fileflow 도메인 특화 메서드에서 사용합니다.
     *
     * @return UserContext 또는 null
     */
    protected UserContext getUserContext() {
        return UserContextHolder.get();
    }

    // ========================================
    // BaseAccessChecker 오버라이드 (Null-safe 처리)
    // ========================================

    /**
     * 현재 사용자가 인증되었는지 확인합니다.
     *
     * @return 인증되었으면 true
     */
    @Override
    public boolean authenticated() {
        SecurityContext ctx = getSecurityContext();
        return ctx != null && ctx.isAuthenticated();
    }

    /**
     * 현재 사용자가 SUPER_ADMIN인지 확인합니다.
     *
     * @return SUPER_ADMIN이면 true
     */
    @Override
    public boolean superAdmin() {
        SecurityContext ctx = getSecurityContext();
        return ctx != null && ctx.hasRole("SUPER_ADMIN");
    }

    /**
     * 현재 사용자가 ADMIN인지 확인합니다.
     *
     * @return ADMIN이면 true
     */
    @Override
    public boolean admin() {
        UserContext context = getUserContext();
        return context != null && context.isAdmin();
    }

    /**
     * 현재 사용자가 특정 역할을 가지고 있는지 확인합니다.
     *
     * @param role 확인할 역할
     * @return 역할이 있으면 true
     */
    @Override
    public boolean hasRole(String role) {
        SecurityContext ctx = getSecurityContext();
        return ctx != null && ctx.hasRole(role);
    }

    /**
     * 현재 사용자가 주어진 역할 중 하나라도 가지고 있는지 확인합니다.
     *
     * @param roles 확인할 역할들
     * @return 역할 중 하나라도 있으면 true
     */
    @Override
    public boolean hasAnyRole(String... roles) {
        if (roles == null || roles.length == 0) {
            return false;
        }
        SecurityContext ctx = getSecurityContext();
        if (ctx == null) {
            return false;
        }
        return Arrays.stream(roles).anyMatch(ctx::hasRole);
    }

    /**
     * 현재 사용자가 특정 권한을 가지고 있는지 확인합니다.
     *
     * @param permission 확인할 권한
     * @return 권한이 있으면 true
     */
    @Override
    public boolean hasPermission(String permission) {
        if (superAdmin()) {
            return true;
        }
        SecurityContext ctx = getSecurityContext();
        return ctx != null && ctx.hasPermission(permission);
    }

    /**
     * 현재 사용자가 주어진 권한 중 하나라도 가지고 있는지 확인합니다.
     *
     * @param permissions 확인할 권한들
     * @return 권한 중 하나라도 있으면 true
     */
    @Override
    public boolean hasAnyPermission(String... permissions) {
        if (permissions == null || permissions.length == 0) {
            return false;
        }
        if (superAdmin()) {
            return true;
        }
        SecurityContext ctx = getSecurityContext();
        if (ctx == null) {
            return false;
        }
        return Arrays.stream(permissions).anyMatch(ctx::hasPermission);
    }

    /**
     * 현재 사용자가 동일 테넌트에 속하는지 확인합니다.
     *
     * @param tenantId 확인할 테넌트 ID
     * @return 동일 테넌트면 true
     */
    @Override
    public boolean sameTenant(String tenantId) {
        if (tenantId == null) {
            return false;
        }
        if (superAdmin()) {
            return true;
        }
        SecurityContext ctx = getSecurityContext();
        return ctx != null && Objects.equals(ctx.getTenantId(), tenantId);
    }

    /**
     * 현재 사용자가 동일 조직에 속하는지 확인합니다.
     *
     * @param organizationId 확인할 조직 ID
     * @return 동일 조직이면 true
     */
    @Override
    public boolean sameOrganization(String organizationId) {
        if (organizationId == null) {
            return false;
        }
        SecurityContext ctx = getSecurityContext();
        if (ctx == null) {
            return false;
        }
        if (ctx.hasRole("SUPER_ADMIN") || ctx.hasRole("TENANT_ADMIN") || ctx.hasRole("ADMIN")) {
            return true;
        }
        return Objects.equals(ctx.getOrganizationId(), organizationId);
    }

    /**
     * 서비스 계정인지 확인합니다.
     *
     * @return 서비스 계정이면 true
     */
    @Override
    public boolean serviceAccount() {
        SecurityContext ctx = getSecurityContext();
        return ctx != null && ctx.isServiceAccount();
    }

    // ========================================
    // 역할 기반 검사 (fileflow 특화)
    // ========================================

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

    // ========================================
    // File 권한 체크 (fileflow 특화)
    // ========================================

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

    // ========================================
    // 리소스 격리 (fileflow 특화 - userId 기반)
    // ========================================

    /**
     * 현재 사용자가 요청한 userId 본인인지 확인합니다.
     *
     * @param userId 확인할 사용자 ID
     * @return 본인이면 true
     */
    @Override
    public boolean myself(String userId) {
        UserContext context = getUserContext();
        if (context == null || context.userId() == null) {
            return false;
        }
        return Objects.equals(context.userId().value(), userId);
    }
}
