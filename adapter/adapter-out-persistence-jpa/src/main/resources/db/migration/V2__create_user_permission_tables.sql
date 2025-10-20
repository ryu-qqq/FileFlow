-- =====================================================
-- FileFlow Database Schema Migration
-- Version: V2 - User Context & Permission Tables
-- Description: 사용자 컨텍스트 및 권한 관리 테이블 생성
-- Author: FileFlow Team
-- Date: 2025-01-20
-- Note: 인증은 외부 서버, FileFlow는 권한 관리만 담당
-- =====================================================

-- =====================================================
-- Table: user_contexts (사용자 컨텍스트)
-- Description: FileFlow 시스템 내 사용자 컨텍스트
-- =====================================================
CREATE TABLE IF NOT EXISTS user_contexts (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT 'FileFlow 내부 ID',
    external_user_id        VARCHAR(100)    NOT NULL COMMENT '인증서버의 사용자 ID',
    tenant_id               VARCHAR(50)     NOT NULL COMMENT '소속 테넌트 ID',
    organization_id         BIGINT          NULL COMMENT '소속 조직 ID',
    user_type               ENUM('SELLER', 'COMPANY_ADMIN', 'INTERNAL_ADMIN', 'CUSTOMER', 'SYSTEM') NOT NULL COMMENT '사용자 유형',
    display_name            VARCHAR(100)    NULL COMMENT '표시명 (캐싱용)',
    email                   VARCHAR(200)    NULL COMMENT '이메일 (캐싱용)',
    preferences             JSON            NULL DEFAULT ('{}') COMMENT 'FileFlow 사용자 설정',
    last_activity_at        DATETIME        NULL COMMENT '마지막 활동 시각',
    status                  ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE' COMMENT '상태',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    deleted_at              DATETIME        NULL COMMENT '삭제 시각 (soft delete)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_external_user_id (external_user_id),
    INDEX idx_tenant_org (tenant_id, organization_id),
    INDEX idx_user_type_status (user_type, status),
    INDEX idx_last_activity (last_activity_at),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 컨텍스트';

-- =====================================================
-- Table: roles (역할)
-- Description: 권한 그룹을 정의하는 역할
-- =====================================================
CREATE TABLE IF NOT EXISTS roles (
    id                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '역할 ID',
    tenant_id           VARCHAR(50)     NULL COMMENT '테넌트 ID (NULL=시스템 역할)',
    role_code           VARCHAR(50)     NOT NULL COMMENT '역할 코드',
    role_name           VARCHAR(100)    NOT NULL COMMENT '역할명',
    description         TEXT            NULL COMMENT '역할 설명',
    role_type           ENUM('SYSTEM', 'TENANT', 'CUSTOM') NOT NULL DEFAULT 'CUSTOM' COMMENT '역할 유형',
    priority            INT             NOT NULL DEFAULT 100 COMMENT '우선순위 (낮을수록 높음)',
    is_assignable       BOOLEAN         NOT NULL DEFAULT TRUE COMMENT '할당 가능 여부',
    max_users           INT             NULL COMMENT '최대 할당 사용자 수',
    metadata            JSON            NULL DEFAULT ('{}') COMMENT '추가 메타데이터',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    created_by          VARCHAR(100)    NULL COMMENT '생성자',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code),
    UNIQUE KEY uk_tenant_role_code (tenant_id, role_code),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_role_type (role_type),
    INDEX idx_is_assignable (is_assignable)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='역할 정의';

-- =====================================================
-- Table: permissions (권한)
-- Description: 세부 권한 정의
-- =====================================================
CREATE TABLE IF NOT EXISTS permissions (
    id                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '권한 ID',
    permission_code     VARCHAR(100)    NOT NULL COMMENT '권한 코드',
    permission_name     VARCHAR(200)    NOT NULL COMMENT '권한명',
    resource_type       VARCHAR(50)     NOT NULL COMMENT '리소스 타입',
    action              VARCHAR(50)     NOT NULL COMMENT '액션',
    description         TEXT            NULL COMMENT '권한 설명',
    scope               ENUM('GLOBAL', 'TENANT', 'ORGANIZATION', 'SELF') NOT NULL DEFAULT 'SELF' COMMENT '권한 범위',
    is_system           BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '시스템 권한 여부',
    depends_on          BIGINT          NULL COMMENT '선행 필요 권한 ID',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_permission_code (permission_code),
    UNIQUE KEY uk_resource_action (resource_type, action),
    INDEX idx_resource_type (resource_type),
    INDEX idx_action (action),
    INDEX idx_scope (scope)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='권한 정의';

-- =====================================================
-- Table: user_role_mappings (사용자-역할 매핑)
-- Description: 사용자 컨텍스트와 역할 매핑
-- =====================================================
CREATE TABLE IF NOT EXISTS user_role_mappings (
    id                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '매핑 ID',
    user_context_id     BIGINT          NOT NULL COMMENT '사용자 컨텍스트 ID',
    role_id             BIGINT          NOT NULL COMMENT '역할 ID',
    assigned_by         VARCHAR(100)    NULL COMMENT '할당한 사용자 (external_user_id)',
    assigned_at         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '할당 시각',
    expires_at          DATETIME        NULL COMMENT '만료 시각',
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE COMMENT '활성화 상태',
    reason              TEXT            NULL COMMENT '할당 사유',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_context_id, role_id),
    INDEX idx_user_context (user_context_id),
    INDEX idx_role_id (role_id),
    INDEX idx_expires_at (expires_at),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자-역할 매핑';

-- =====================================================
-- Table: role_permissions (역할-권한 매핑)
-- Description: 역할과 권한 매핑
-- =====================================================
CREATE TABLE IF NOT EXISTS role_permissions (
    id                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '매핑 ID',
    role_id             BIGINT          NOT NULL COMMENT '역할 ID',
    permission_id       BIGINT          NOT NULL COMMENT '권한 ID',
    conditions          JSON            NULL COMMENT '추가 조건 (ABAC용)',
    granted_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '부여 시각',
    granted_by          VARCHAR(100)    NULL COMMENT '부여자',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='역할-권한 매핑';

-- =====================================================
-- Initial Data
-- =====================================================

-- Insert system roles
INSERT INTO roles (tenant_id, role_code, role_name, description, role_type, priority) VALUES
(NULL, 'SUPER_ADMIN', '슈퍼 관리자', '시스템 전체 관리 권한', 'SYSTEM', 1),
(NULL, 'SYSTEM_ADMIN', '시스템 관리자', '시스템 운영 권한', 'SYSTEM', 10),
('b2c_kr', 'TENANT_ADMIN', '테넌트 관리자', 'B2C 테넌트 관리 권한', 'TENANT', 20),
('b2c_kr', 'SELLER_ADMIN', '판매자 관리자', '판매자 전체 권한', 'CUSTOM', 30),
('b2c_kr', 'SELLER_OPERATOR', '판매자 운영자', '판매자 운영 권한', 'CUSTOM', 40),
('b2b_global', 'COMPANY_ADMIN', '회사 관리자', '입점회사 관리 권한', 'CUSTOM', 30);

-- Insert permissions
INSERT INTO permissions (permission_code, permission_name, resource_type, action, scope, description) VALUES
-- File permissions
('FILE_CREATE', '파일 업로드', 'file', 'create', 'ORGANIZATION', '파일 업로드 권한'),
('FILE_READ', '파일 조회', 'file', 'read', 'ORGANIZATION', '파일 조회 권한'),
('FILE_UPDATE', '파일 수정', 'file', 'update', 'ORGANIZATION', '파일 정보 수정 권한'),
('FILE_DELETE', '파일 삭제', 'file', 'delete', 'ORGANIZATION', '파일 삭제 권한'),
('FILE_DOWNLOAD', '파일 다운로드', 'file', 'download', 'ORGANIZATION', '파일 다운로드 권한'),
-- Upload session permissions
('UPLOAD_SESSION_CREATE', '업로드 세션 생성', 'upload_session', 'create', 'SELF', '업로드 세션 생성 권한'),
('UPLOAD_SESSION_MANAGE', '업로드 세션 관리', 'upload_session', 'manage', 'ORGANIZATION', '업로드 세션 관리 권한'),
-- Pipeline permissions
('PIPELINE_EXECUTE', '파이프라인 실행', 'pipeline', 'execute', 'ORGANIZATION', '파이프라인 실행 권한'),
('PIPELINE_MANAGE', '파이프라인 관리', 'pipeline', 'manage', 'TENANT', '파이프라인 설정 관리 권한'),
-- User management permissions
('USER_CREATE', '사용자 생성', 'user', 'create', 'ORGANIZATION', '사용자 생성 권한'),
('USER_READ', '사용자 조회', 'user', 'read', 'ORGANIZATION', '사용자 정보 조회 권한'),
('USER_UPDATE', '사용자 수정', 'user', 'update', 'ORGANIZATION', '사용자 정보 수정 권한'),
('USER_DELETE', '사용자 삭제', 'user', 'delete', 'ORGANIZATION', '사용자 삭제 권한'),
-- Policy management permissions
('POLICY_VIEW', '정책 조회', 'policy', 'read', 'ORGANIZATION', '정책 조회 권한'),
('POLICY_MANAGE', '정책 관리', 'policy', 'manage', 'TENANT', '정책 생성/수정/삭제 권한');

-- Sample user contexts
INSERT INTO user_contexts (external_user_id, tenant_id, organization_id, user_type, display_name, email, status) VALUES
('auth_user_001', 'b2c_kr', 1, 'SELLER', '김판매', 'seller@fashionplus.com', 'ACTIVE'),
('auth_user_002', 'b2c_kr', 2, 'INTERNAL_ADMIN', '박관리', 'admin@fileflow.com', 'ACTIVE'),
('auth_user_003', 'b2b_global', 3, 'COMPANY_ADMIN', 'John Smith', 'admin@globaltrade.com', 'ACTIVE');

-- Sample role assignments
INSERT INTO user_role_mappings (user_context_id, role_id, assigned_by, reason) VALUES
(1, 4, 'auth_user_002', '판매자 관리자 권한 부여'),
(2, 3, NULL, '시스템 관리자 초기 설정'),
(3, 6, 'auth_user_002', '입점회사 관리자 등록');

-- Sample role-permission mappings (SELLER_ADMIN)
INSERT INTO role_permissions (role_id, permission_id) VALUES
(4, 1), (4, 2), (4, 3), (4, 4), (4, 5),  -- File permissions
(4, 6), (4, 7),                            -- Upload permissions
(4, 8);                                    -- Pipeline execute
