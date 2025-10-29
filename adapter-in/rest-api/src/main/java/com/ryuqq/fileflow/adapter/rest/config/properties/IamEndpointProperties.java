package com.ryuqq.fileflow.adapter.rest.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * IAM 바운디드 컨텍스트 엔드포인트 설정 Properties
 *
 * <p>IAM (Identity and Access Management) 도메인의 REST API 엔드포인트 경로를 관리합니다.</p>
 *
 * <p><strong>설정 예시 (application.yml):</strong></p>
 * <pre>{@code
 * api:
 *   endpoints:
 *     iam:
 *       organization:
 *         base: /organizations
 *         by-id: /{organizationId}
 *       tenant:
 *         base: /tenants
 *         by-id: /{tenantId}
 * }</pre>
 *
 * <p><strong>사용 방법:</strong></p>
 * <pre>{@code
 * @RestController
 * @RequestMapping("${api.endpoints.base-v1}${api.endpoints.iam.organization.base}")
 * public class OrganizationController { ... }
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-29
 */
@Component
@ConfigurationProperties(prefix = "api.endpoints.iam")
public class IamEndpointProperties {

    /**
     * Organization 도메인 엔드포인트 설정
     */
    private OrganizationEndpoints organization = new OrganizationEndpoints();

    /**
     * Tenant 도메인 엔드포인트 설정
     */
    private TenantEndpoints tenant = new TenantEndpoints();

    /**
     * Permission 도메인 엔드포인트 설정
     */
    private PermissionEndpoints permission = new PermissionEndpoints();

    /**
     * UserContext 도메인 엔드포인트 설정
     */
    private UserContextEndpoints userContext = new UserContextEndpoints();

    /**
     * Organization 도메인 엔드포인트 경로
     */
    public static class OrganizationEndpoints {
        private String base = "/organizations";
        private String byId = "/{organizationId}";
        private String status = "/{organizationId}/status";

        public String getBase() {
            return base;
        }

        public void setBase(String base) {
            this.base = base;
        }

        public String getById() {
            return byId;
        }

        public void setById(String byId) {
            this.byId = byId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    /**
     * Tenant 도메인 엔드포인트 경로
     */
    public static class TenantEndpoints {
        private String base = "/tenants";
        private String byId = "/{tenantId}";
        private String status = "/{tenantId}/status";
        private String tree = "/{tenantId}/tree";

        public String getBase() {
            return base;
        }

        public void setBase(String base) {
            this.base = base;
        }

        public String getById() {
            return byId;
        }

        public void setById(String byId) {
            this.byId = byId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getTree() {
            return tree;
        }

        public void setTree(String tree) {
            this.tree = tree;
        }
    }

    /**
     * Permission 도메인 엔드포인트 경로
     */
    public static class PermissionEndpoints {
        private String base = "/permissions";
        private String evaluate = "/evaluate";

        public String getBase() {
            return base;
        }

        public void setBase(String base) {
            this.base = base;
        }

        public String getEvaluate() {
            return evaluate;
        }

        public void setEvaluate(String evaluate) {
            this.evaluate = evaluate;
        }
    }

    /**
     * UserContext 도메인 엔드포인트 경로
     */
    public static class UserContextEndpoints {
        private String base = "/user-contexts";

        public String getBase() {
            return base;
        }

        public void setBase(String base) {
            this.base = base;
        }
    }

    public OrganizationEndpoints getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationEndpoints organization) {
        this.organization = organization;
    }

    public TenantEndpoints getTenant() {
        return tenant;
    }

    public void setTenant(TenantEndpoints tenant) {
        this.tenant = tenant;
    }

    public PermissionEndpoints getPermission() {
        return permission;
    }

    public void setPermission(PermissionEndpoints permission) {
        this.permission = permission;
    }

    public UserContextEndpoints getUserContext() {
        return userContext;
    }

    public void setUserContext(UserContextEndpoints userContext) {
        this.userContext = userContext;
    }
}
