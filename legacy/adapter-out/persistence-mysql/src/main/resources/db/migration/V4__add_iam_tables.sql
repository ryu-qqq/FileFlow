-- =====================================================
-- Migration: V4__add_iam_tables.sql
-- Description: Add IAM tables for user-organization memberships and user-role mappings
-- =====================================================
-- Add user_org_memberships table to track User-Organization membership
-- Add user_role_mappings table to map Users to Roles within Tenant/Organization context

-- =====================================================
-- Table: user_org_memberships
-- Purpose: User-Organization membership tracking with membership types
-- =====================================================
CREATE TABLE user_org_memberships (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_context_id BIGINT NOT NULL COMMENT 'Foreign key to user_contexts.id',
    tenant_id BIGINT NOT NULL COMMENT 'Foreign key to tenants.id',
    organization_id BIGINT NOT NULL COMMENT 'Foreign key to organizations.id',
    membership_type VARCHAR(20) NOT NULL COMMENT 'Membership type: OWNER, ADMIN, MEMBER',
    PRIMARY KEY (id),
    INDEX idx_user_org_membership (user_context_id, tenant_id, organization_id),
    INDEX idx_organization_id (organization_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='User-Organization membership table';

-- =====================================================
-- Table: user_role_mappings
-- Purpose: Join table mapping Users to Roles in Tenant/Organization context (RBAC)
-- =====================================================
CREATE TABLE user_role_mappings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_context_id BIGINT NOT NULL COMMENT 'Foreign key to user_contexts.id',
    role_code VARCHAR(100) NOT NULL COMMENT 'Foreign key to roles.role_code',
    tenant_id BIGINT NOT NULL COMMENT 'Foreign key to tenants.id',
    organization_id BIGINT NOT NULL COMMENT 'Foreign key to organizations.id',
    PRIMARY KEY (id),
    INDEX idx_user_role_mapping (user_context_id, tenant_id, organization_id),
    INDEX idx_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='User-Role mapping table for RBAC';
