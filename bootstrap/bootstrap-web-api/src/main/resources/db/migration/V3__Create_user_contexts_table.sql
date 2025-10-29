-- ===============================================
-- V3: Create user_contexts table
-- ===============================================
-- Description: UserContext 테이블 생성
-- Author: windsurf
-- Date: 2025-10-29
-- ===============================================

CREATE TABLE user_contexts (
    -- Primary Key
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'UserContext 고유 식별자',
    
    -- Business Fields
    external_user_id VARCHAR(255) NOT NULL COMMENT '외부 IDP 사용자 ID (예: auth0|abc123)',
    email VARCHAR(255) NOT NULL COMMENT '사용자 이메일',
    
    -- Audit Fields (BaseAuditEntity)
    created_at DATETIME(6) NOT NULL COMMENT '생성 일시',
    updated_at DATETIME(6) NOT NULL COMMENT '최종 수정 일시',
    
    -- Soft Delete
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '소프트 삭제 플래그',
    
    -- Constraints
    PRIMARY KEY (id),
    
    -- Unique Constraints
    UNIQUE KEY uk_user_contexts_external_user_id (external_user_id),
    
    -- Indexes
    INDEX idx_user_contexts_email (email),
    INDEX idx_user_contexts_deleted (deleted),
    INDEX idx_user_contexts_created_at (created_at)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='UserContext 테이블';
