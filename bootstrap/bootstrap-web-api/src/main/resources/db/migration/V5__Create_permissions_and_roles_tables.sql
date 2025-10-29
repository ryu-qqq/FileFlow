-- ===============================================
-- V5: Create permissions and roles tables
-- ===============================================
-- Description: Permission, Role, RolePermission, UserRoleMapping 테이블 생성
-- Author: windsurf
-- Date: 2025-10-29
-- ===============================================

-- ===============================================
-- 1. permissions 테이블
-- ===============================================
CREATE TABLE permissions (
    -- Primary Key (String PK)
    code VARCHAR(100) NOT NULL COMMENT 'Permission 코드 (PK, 예: file.upload, user.read)',
    
    -- Business Fields
    description VARCHAR(500) NOT NULL COMMENT 'Permission 설명',
    default_scope VARCHAR(20) NOT NULL COMMENT '기본 적용 범위 (SYSTEM, TENANT, ORGANIZATION, SELF)',
    
    -- Audit Fields (SoftDeletableEntity)
    created_at DATETIME(6) NOT NULL COMMENT '생성 일시',
    updated_at DATETIME(6) NOT NULL COMMENT '최종 수정 일시',
    deleted_at DATETIME(6) NULL COMMENT '삭제 일시 (NULL이면 활성)',
    
    -- Constraints
    PRIMARY KEY (code),
    
    -- Indexes
    INDEX idx_permissions_default_scope (default_scope),
    INDEX idx_permissions_deleted_at (deleted_at),
    INDEX idx_permissions_created_at (created_at)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Permission 테이블';

-- ===============================================
-- 2. roles 테이블
-- ===============================================
CREATE TABLE roles (
    -- Primary Key (String PK)
    code VARCHAR(100) NOT NULL COMMENT 'Role 코드 (PK, 예: org.uploader, tenant.admin)',
    
    -- Business Fields
    description VARCHAR(500) NOT NULL COMMENT 'Role 설명',
    
    -- Audit Fields (SoftDeletableEntity)
    created_at DATETIME(6) NOT NULL COMMENT '생성 일시',
    updated_at DATETIME(6) NOT NULL COMMENT '최종 수정 일시',
    deleted_at DATETIME(6) NULL COMMENT '삭제 일시 (NULL이면 활성)',
    
    -- Constraints
    PRIMARY KEY (code),
    
    -- Indexes
    INDEX idx_roles_deleted_at (deleted_at),
    INDEX idx_roles_created_at (created_at)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Role 테이블';

-- ===============================================
-- 3. role_permissions 테이블 (Join Table)
-- ===============================================
CREATE TABLE role_permissions (
    -- Primary Key
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'RolePermission 고유 식별자',
    
    -- Foreign Keys (String FK 전략)
    role_code VARCHAR(100) NOT NULL COMMENT 'Role 코드 (FK)',
    permission_code VARCHAR(100) NOT NULL COMMENT 'Permission 코드 (FK)',
    
    -- Constraints
    PRIMARY KEY (id),
    
    -- Unique Constraints (한 Role에 같은 Permission 중복 불가)
    UNIQUE KEY uk_role_permissions_role_permission (role_code, permission_code),
    
    -- Foreign Key Constraints
    CONSTRAINT fk_role_permissions_role_code 
        FOREIGN KEY (role_code) 
        REFERENCES roles(code)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    
    CONSTRAINT fk_role_permissions_permission_code 
        FOREIGN KEY (permission_code) 
        REFERENCES permissions(code)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    
    -- Indexes
    INDEX idx_role_permissions_role_code (role_code),
    INDEX idx_role_permissions_permission_code (permission_code)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Role-Permission 연결 테이블';

-- ===============================================
-- 4. user_role_mappings 테이블 (Join Table)
-- ===============================================
CREATE TABLE user_role_mappings (
    -- Primary Key
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'UserRoleMapping 고유 식별자',
    
    -- Foreign Keys
    user_context_id BIGINT NOT NULL COMMENT 'UserContext ID (FK)',
    role_code VARCHAR(100) NOT NULL COMMENT 'Role 코드 (FK)',
    tenant_id BIGINT NOT NULL COMMENT 'Tenant ID (FK)',
    organization_id BIGINT NOT NULL COMMENT 'Organization ID (FK)',
    
    -- Constraints
    PRIMARY KEY (id),
    
    -- Unique Constraints (한 사용자는 특정 조직에서 같은 Role 중복 불가)
    UNIQUE KEY uk_user_role_mappings_user_role_org (user_context_id, role_code, organization_id),
    
    -- Foreign Key Constraints
    CONSTRAINT fk_user_role_mappings_user_context_id 
        FOREIGN KEY (user_context_id) 
        REFERENCES user_contexts(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    
    CONSTRAINT fk_user_role_mappings_role_code 
        FOREIGN KEY (role_code) 
        REFERENCES roles(code)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    
    CONSTRAINT fk_user_role_mappings_tenant_id 
        FOREIGN KEY (tenant_id) 
        REFERENCES tenants(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    
    CONSTRAINT fk_user_role_mappings_organization_id 
        FOREIGN KEY (organization_id) 
        REFERENCES organizations(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    
    -- Indexes
    -- Composite Index for buildEffectiveGrants() query
    INDEX idx_user_role_mappings_context (user_context_id, tenant_id, organization_id),
    INDEX idx_user_role_mappings_role_code (role_code),
    INDEX idx_user_role_mappings_tenant_id (tenant_id),
    INDEX idx_user_role_mappings_organization_id (organization_id)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User-Role Mapping 테이블 (Tenant/Organization 컨텍스트)';
