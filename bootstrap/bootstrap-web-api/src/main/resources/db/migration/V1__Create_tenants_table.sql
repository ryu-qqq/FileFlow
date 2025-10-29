-- ===============================================
-- V1: Create tenants table
-- ===============================================
-- Description: Tenant 테이블 생성
-- Author: windsurf
-- Date: 2025-10-29
-- ===============================================

CREATE TABLE tenants (
    -- Primary Key
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Tenant 고유 식별자',
    
    -- Business Fields
    name VARCHAR(200) NOT NULL COMMENT 'Tenant 이름',
    status VARCHAR(20) NOT NULL COMMENT 'Tenant 상태 (ACTIVE, SUSPENDED)',
    
    -- Audit Fields (BaseAuditEntity)
    created_at DATETIME(6) NOT NULL COMMENT '생성 일시',
    updated_at DATETIME(6) NOT NULL COMMENT '최종 수정 일시',
    
    -- Soft Delete
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '소프트 삭제 플래그',
    
    -- Constraints
    PRIMARY KEY (id),
    
    -- Indexes
    INDEX idx_tenants_status (status),
    INDEX idx_tenants_deleted (deleted),
    INDEX idx_tenants_created_at (created_at)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tenant 테이블';
