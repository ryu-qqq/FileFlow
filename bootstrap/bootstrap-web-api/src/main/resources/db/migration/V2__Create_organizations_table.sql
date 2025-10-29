-- ===============================================
-- V2: Create organizations table
-- ===============================================
-- Description: Organization 테이블 생성
-- Author: windsurf
-- Date: 2025-10-29
-- ===============================================

CREATE TABLE organizations (
    -- Primary Key
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Organization 고유 식별자',
    
    -- Foreign Keys (Long FK 전략)
    tenant_id BIGINT NOT NULL COMMENT '소속 Tenant ID (FK)',
    
    -- Business Fields
    org_code VARCHAR(50) NOT NULL COMMENT '조직 코드 (Tenant 내 유니크)',
    name VARCHAR(200) NOT NULL COMMENT '조직 이름',
    status VARCHAR(20) NOT NULL COMMENT 'Organization 상태 (ACTIVE, INACTIVE)',
    
    -- Audit Fields (BaseAuditEntity)
    created_at DATETIME(6) NOT NULL COMMENT '생성 일시',
    updated_at DATETIME(6) NOT NULL COMMENT '최종 수정 일시',
    
    -- Soft Delete
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '소프트 삭제 플래그',
    
    -- Constraints
    PRIMARY KEY (id),
    
    -- Unique Constraints
    UNIQUE KEY uk_organizations_tenant_org_code (tenant_id, org_code),
    
    -- Foreign Key Constraints
    CONSTRAINT fk_organizations_tenant_id 
        FOREIGN KEY (tenant_id) 
        REFERENCES tenants(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    
    -- Indexes
    INDEX idx_organizations_tenant_id (tenant_id),
    INDEX idx_organizations_status (status),
    INDEX idx_organizations_deleted (deleted),
    INDEX idx_organizations_created_at (created_at)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Organization 테이블';
