-- ===============================================
-- V4: Create user_org_memberships table
-- ===============================================
-- Description: User-Organization Membership 테이블 생성
-- Author: windsurf
-- Date: 2025-10-29
-- ===============================================

CREATE TABLE user_org_memberships (
    -- Primary Key
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Membership 고유 식별자',
    
    -- Foreign Keys (Long FK 전략)
    user_context_id BIGINT NOT NULL COMMENT 'UserContext ID (FK)',
    tenant_id BIGINT NOT NULL COMMENT 'Tenant ID (FK)',
    organization_id BIGINT NOT NULL COMMENT 'Organization ID (FK)',
    
    -- Business Fields
    membership_type VARCHAR(20) NOT NULL COMMENT 'Membership 타입 (OWNER, ADMIN, MEMBER)',
    
    -- Constraints
    PRIMARY KEY (id),
    
    -- Unique Constraints (한 사용자는 한 조직에 하나의 멤버십만 가질 수 있음)
    UNIQUE KEY uk_user_org_memberships_user_org (user_context_id, organization_id),
    
    -- Foreign Key Constraints
    CONSTRAINT fk_user_org_memberships_user_context_id 
        FOREIGN KEY (user_context_id) 
        REFERENCES user_contexts(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    
    CONSTRAINT fk_user_org_memberships_tenant_id 
        FOREIGN KEY (tenant_id) 
        REFERENCES tenants(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    
    CONSTRAINT fk_user_org_memberships_organization_id 
        FOREIGN KEY (organization_id) 
        REFERENCES organizations(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    
    -- Indexes
    INDEX idx_user_org_memberships_user_context_id (user_context_id),
    INDEX idx_user_org_memberships_tenant_id (tenant_id),
    INDEX idx_user_org_memberships_organization_id (organization_id),
    INDEX idx_user_org_memberships_membership_type (membership_type)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User-Organization Membership 테이블';
