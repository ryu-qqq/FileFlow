-- ========================================
-- FileFlow Phase 1 Initial Schema
-- ========================================
-- Author: FileFlow Team
-- Date: 2025-10-27
-- Description: 초기 데이터베이스 스키마 생성 (Tenant, Organization, User, IAM)
-- ========================================

-- ========================================
-- 1. Tenant 테이블
-- ========================================
CREATE TABLE IF NOT EXISTS tenants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Tenant 고유 식별자',
    name VARCHAR(200) NOT NULL COMMENT 'Tenant 이름',
    status VARCHAR(20) NOT NULL COMMENT 'Tenant 상태 (ACTIVE, SUSPENDED)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 수정 일시',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '소프트 삭제 플래그',
    INDEX idx_tenants_status (status),
    INDEX idx_tenants_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tenant 정보';

-- ========================================
-- 2. Organization 테이블
-- ========================================
CREATE TABLE IF NOT EXISTS organizations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Organization 고유 식별자',
    tenant_id BIGINT NOT NULL COMMENT '소속 Tenant ID',
    org_code VARCHAR(50) NOT NULL COMMENT '조직 코드 (Tenant 내 유니크)',
    name VARCHAR(200) NOT NULL COMMENT '조직 이름',
    status VARCHAR(20) NOT NULL COMMENT 'Organization 상태 (ACTIVE, INACTIVE)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 수정 일시',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '소프트 삭제 플래그',
    UNIQUE KEY uk_organizations_tenant_org_code (tenant_id, org_code),
    INDEX idx_organizations_tenant_id (tenant_id),
    INDEX idx_organizations_status (status),
    INDEX idx_organizations_deleted (deleted),
    CONSTRAINT fk_organizations_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Organization 정보';

-- ========================================
-- 3. UserContext 테이블
-- ========================================
CREATE TABLE IF NOT EXISTS user_contexts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'UserContext 고유 식별자',
    external_user_id VARCHAR(255) NOT NULL COMMENT '외부 IDP 사용자 ID (예: auth0|abc123)',
    email VARCHAR(255) NOT NULL COMMENT '사용자 이메일',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 수정 일시',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '소프트 삭제 플래그',
    UNIQUE KEY uk_user_contexts_external_user_id (external_user_id),
    INDEX idx_user_contexts_email (email),
    INDEX idx_user_contexts_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 컨텍스트';

-- ========================================
-- 4. UserOrgMembership 테이블 (User-Organization 연결)
-- ========================================
CREATE TABLE IF NOT EXISTS user_org_memberships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Membership 고유 식별자',
    user_context_id BIGINT NOT NULL COMMENT 'UserContext ID',
    tenant_id BIGINT NOT NULL COMMENT 'Tenant ID',
    organization_id BIGINT NOT NULL COMMENT 'Organization ID',
    membership_type VARCHAR(20) NOT NULL COMMENT 'Membership 타입 (OWNER, ADMIN, MEMBER)',
    UNIQUE KEY uk_user_org_memberships_user_org (user_context_id, organization_id),
    INDEX idx_user_org_memberships_user (user_context_id),
    INDEX idx_user_org_memberships_tenant (tenant_id),
    INDEX idx_user_org_memberships_org (organization_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자-조직 멤버십';

-- Add foreign keys after all tables are created
ALTER TABLE user_org_memberships
    ADD CONSTRAINT fk_user_org_memberships_user FOREIGN KEY (user_context_id) REFERENCES user_contexts(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_user_org_memberships_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_user_org_memberships_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE;

-- ========================================
-- 5. Permission 테이블
-- ========================================
CREATE TABLE IF NOT EXISTS permissions (
    code VARCHAR(100) PRIMARY KEY COMMENT 'Permission 코드 (예: file.upload, user.read)',
    description VARCHAR(500) NOT NULL COMMENT 'Permission 설명',
    default_scope VARCHAR(20) NOT NULL COMMENT '기본 적용 범위 (SYSTEM, TENANT, ORGANIZATION, SELF)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 수정 일시',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '소프트 삭제 플래그',
    INDEX idx_permissions_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='권한 정의';

-- ========================================
-- 6. Role 테이블
-- ========================================
CREATE TABLE IF NOT EXISTS roles (
    code VARCHAR(100) PRIMARY KEY COMMENT 'Role 코드 (예: org.uploader, tenant.admin)',
    description VARCHAR(500) NOT NULL COMMENT 'Role 설명',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 수정 일시',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '소프트 삭제 플래그',
    INDEX idx_roles_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='역할 정의';

-- ========================================
-- 7. RolePermission 테이블 (Role-Permission 연결)
-- ========================================
CREATE TABLE IF NOT EXISTS role_permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '연결 테이블 ID',
    role_code VARCHAR(100) NOT NULL COMMENT 'Role 코드',
    permission_code VARCHAR(100) NOT NULL COMMENT 'Permission 코드',
    UNIQUE KEY uk_role_permissions_role_perm (role_code, permission_code),
    INDEX idx_role_permissions_role (role_code),
    INDEX idx_role_permissions_perm (permission_code),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_code) REFERENCES roles(code) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_perm FOREIGN KEY (permission_code) REFERENCES permissions(code) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='역할-권한 연결';

-- ========================================
-- 8. UserRoleMapping 테이블 (User-Role 연결)
-- ========================================
CREATE TABLE IF NOT EXISTS user_role_mappings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '연결 테이블 ID',
    user_context_id BIGINT NOT NULL COMMENT 'UserContext ID',
    role_code VARCHAR(100) NOT NULL COMMENT 'Role 코드',
    tenant_id BIGINT NOT NULL COMMENT 'Tenant ID',
    organization_id BIGINT NOT NULL COMMENT 'Organization ID',
    UNIQUE KEY uk_user_role_mappings_user_role_ctx (user_context_id, role_code, tenant_id, organization_id),
    INDEX idx_user_role_mappings_user (user_context_id),
    INDEX idx_user_role_mappings_role (role_code),
    INDEX idx_user_role_mappings_tenant (tenant_id),
    INDEX idx_user_role_mappings_org (organization_id),
    CONSTRAINT fk_user_role_mappings_user FOREIGN KEY (user_context_id) REFERENCES user_contexts(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_mappings_role FOREIGN KEY (role_code) REFERENCES roles(code) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_mappings_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE RESTRICT,
    CONSTRAINT fk_user_role_mappings_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자-역할 연결';

-- ========================================
-- 9. Settings 테이블 (EAV 패턴)
-- ========================================
CREATE TABLE IF NOT EXISTS settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Setting 고유 식별자',
    setting_key VARCHAR(100) NOT NULL COMMENT 'Setting Key (예: app.max_upload_size)',
    setting_value TEXT NOT NULL COMMENT 'Setting Value (JSON 또는 단순 문자열)',
    setting_type VARCHAR(20) NOT NULL COMMENT 'Setting 타입 (STRING, NUMBER, BOOLEAN, JSON_OBJECT, JSON_ARRAY)',
    level VARCHAR(20) NOT NULL COMMENT 'Setting 레벨 (DEFAULT, TENANT, ORG)',
    context_id BIGINT COMMENT 'Context ID (Tenant/Organization ID, DEFAULT일 경우 NULL)',
    is_secret BOOLEAN NOT NULL DEFAULT FALSE COMMENT '비밀 키 여부',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 수정 일시',
    UNIQUE KEY uk_setting_key_level_context (setting_key, level, context_id),
    INDEX idx_settings_key (setting_key),
    INDEX idx_settings_level_context (level, context_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='설정 정보 (EAV)';

-- ========================================
-- 초기 데이터 INSERT (기본 권한 및 역할)
-- ========================================

-- 기본 Permission 생성
INSERT INTO permissions (code, description, default_scope) VALUES
('file.upload', 'Upload files to the system', 'ORGANIZATION'),
('file.read', 'Read file metadata', 'ORGANIZATION'),
('file.delete', 'Delete files', 'ORGANIZATION'),
('tenant.admin', 'Full administrative access to tenant resources', 'TENANT'),
('organization.admin', 'Full administrative access to organization resources', 'ORGANIZATION'),
('user.read', 'Read user information', 'SELF'),
('user.write', 'Modify user information', 'SELF'),
('system.admin', 'System-wide administrative access', 'SYSTEM');

-- 기본 Role 생성
INSERT INTO roles (code, description) VALUES
('org.uploader', 'Organization file uploader'),
('org.admin', 'Organization administrator'),
('tenant.admin', 'Tenant administrator'),
('system.admin', 'System administrator');

-- RolePermission 연결
INSERT INTO role_permissions (role_code, permission_code) VALUES
-- org.uploader: 파일 업로드 및 읽기 권한
('org.uploader', 'file.upload'),
('org.uploader', 'file.read'),

-- org.admin: Organization 관리자
('org.admin', 'file.upload'),
('org.admin', 'file.read'),
('org.admin', 'file.delete'),
('org.admin', 'organization.admin'),
('org.admin', 'user.read'),

-- tenant.admin: Tenant 관리자
('tenant.admin', 'file.upload'),
('tenant.admin', 'file.read'),
('tenant.admin', 'file.delete'),
('tenant.admin', 'tenant.admin'),
('tenant.admin', 'organization.admin'),
('tenant.admin', 'user.read'),
('tenant.admin', 'user.write'),

-- system.admin: 시스템 관리자
('system.admin', 'system.admin'),
('system.admin', 'tenant.admin'),
('system.admin', 'organization.admin'),
('system.admin', 'user.read'),
('system.admin', 'user.write');

-- 기본 Settings (DEFAULT 레벨)
INSERT INTO settings (setting_key, setting_value, setting_type, level, context_id, is_secret) VALUES
('app.max_upload_size', '10485760', 'NUMBER', 'DEFAULT', NULL, FALSE),
('app.allowed_file_types', '["jpg","jpeg","png","gif","pdf","docx","xlsx"]', 'JSON_ARRAY', 'DEFAULT', NULL, FALSE),
('feature.enable_notifications', 'true', 'BOOLEAN', 'DEFAULT', NULL, FALSE),
('api.rate_limit', '100', 'NUMBER', 'DEFAULT', NULL, FALSE);
