-- =====================================================
-- FileFlow Database Schema Migration
-- Version: V1 - Tenant & Organization Tables
-- Description: 테넌트 및 조직 관련 핵심 테이블 생성
-- Author: FileFlow Team
-- Date: 2025-01-20
-- =====================================================

-- =====================================================
-- Table: tenants (테넌트)
-- Description: 멀티테넌시 핵심 테이블
-- =====================================================
CREATE TABLE IF NOT EXISTS tenants (
    tenant_id           VARCHAR(50)     NOT NULL COMMENT '테넌트 고유 식별자',
    tenant_type         ENUM('B2C', 'B2B') NOT NULL COMMENT '테넌트 유형',
    name                VARCHAR(100)    NOT NULL COMMENT '테넌트 명칭',
    description         TEXT            NULL COMMENT '테넌트 설명',
    status              ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE' COMMENT '테넌트 상태',
    settings            JSON            NULL DEFAULT ('{}') COMMENT '테넌트 전역 설정',
    api_quota_limit     INT             NULL DEFAULT 10000 COMMENT 'API 일일 호출 제한',
    storage_quota_gb    BIGINT          NULL DEFAULT 1000 COMMENT '스토리지 할당량 (GB)',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    created_by          VARCHAR(100)    NULL COMMENT '생성자',
    PRIMARY KEY (tenant_id),
    INDEX idx_tenant_type_status (tenant_type, status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='테넌트 정보';

-- =====================================================
-- Table: organizations (조직)
-- Description: 판매자, 입점회사, 내부 관리 조직 관리
-- =====================================================
CREATE TABLE IF NOT EXISTS organizations (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '조직 ID',
    tenant_id               VARCHAR(50)     NOT NULL COMMENT '소속 테넌트 ID',
    org_code                VARCHAR(100)    NOT NULL COMMENT '조직 코드 (고유값)',
    name                    VARCHAR(200)    NOT NULL COMMENT '조직명',
    org_type                ENUM('SELLER', 'COMPANY', 'INTERNAL', 'CUSTOMER') NOT NULL COMMENT '조직 유형',
    business_number         VARCHAR(50)     NULL COMMENT '사업자등록번호',
    contract_start_date     DATE            NULL COMMENT '계약 시작일',
    contract_end_date       DATE            NULL COMMENT '계약 종료일',
    status                  ENUM('PENDING', 'ACTIVE', 'INACTIVE', 'SUSPENDED', 'TERMINATED') NOT NULL DEFAULT 'PENDING' COMMENT '조직 상태',
    tier                    ENUM('BASIC', 'STANDARD', 'PREMIUM', 'ENTERPRISE') NULL DEFAULT 'BASIC' COMMENT '서비스 등급',
    metadata                JSON            NULL DEFAULT ('{}') COMMENT '추가 메타데이터',
    api_key                 VARCHAR(255)    NULL COMMENT 'API 인증키 (암호화)',
    webhook_url             VARCHAR(500)    NULL COMMENT '웹훅 URL',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    created_by              VARCHAR(100)    NULL COMMENT '생성자',
    PRIMARY KEY (id),
    UNIQUE KEY uk_org_code (org_code),
    UNIQUE KEY uk_tenant_org_code (tenant_id, org_code),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_org_type_status (org_type, status),
    INDEX idx_contract_dates (contract_start_date, contract_end_date)
    -- Note: Foreign Key constraints 제거 (애플리케이션 레벨 검증)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='조직 정보';

-- =====================================================
-- Table: tenant_settings (테넌트 설정)
-- Description: 테넌트별 세부 설정 (EAV 패턴)
-- =====================================================
CREATE TABLE IF NOT EXISTS tenant_settings (
    id                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '설정 ID',
    tenant_id           VARCHAR(50)     NOT NULL COMMENT '테넌트 ID',
    setting_key         VARCHAR(100)    NOT NULL COMMENT '설정 키',
    setting_value       TEXT            NULL COMMENT '설정 값',
    value_type          ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON', 'DATE') NOT NULL DEFAULT 'STRING' COMMENT '값 타입',
    description         TEXT            NULL COMMENT '설정 설명',
    is_encrypted        BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '암호화 여부',
    is_system           BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '시스템 설정 여부',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    updated_by          VARCHAR(100)    NULL COMMENT '수정자',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_setting_key (tenant_id, setting_key),
    INDEX idx_setting_key (setting_key),
    INDEX idx_value_type (value_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='테넌트 설정';

-- =====================================================
-- Table: organization_settings (조직 설정)
-- Description: 조직별 세부 설정
-- =====================================================
CREATE TABLE IF NOT EXISTS organization_settings (
    id                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '설정 ID',
    organization_id     BIGINT          NOT NULL COMMENT '조직 ID',
    setting_key         VARCHAR(100)    NOT NULL COMMENT '설정 키',
    setting_value       TEXT            NULL COMMENT '설정 값',
    value_type          ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON', 'DATE') NOT NULL DEFAULT 'STRING' COMMENT '값 타입',
    is_inherited        BOOLEAN         NOT NULL DEFAULT TRUE COMMENT '테넌트 설정 상속 여부',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_org_setting_key (organization_id, setting_key),
    INDEX idx_org_setting_key (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='조직 설정';

-- =====================================================
-- Initial Data
-- =====================================================

-- Insert default tenants
INSERT INTO tenants (tenant_id, tenant_type, name, description, settings) VALUES
('b2c_kr', 'B2C', 'B2C Korea', '한국 B2C 마켓플레이스', '{"default_language": "ko", "currency": "KRW"}'),
('b2b_global', 'B2B', 'B2B Global', '글로벌 B2B 플랫폼', '{"default_language": "en", "currency": "USD"}');

-- Insert sample organizations
INSERT INTO organizations (tenant_id, org_code, name, org_type, business_number, status, tier) VALUES
('b2c_kr', 'SELLER_001', '패션플러스', 'SELLER', '123-45-67890', 'ACTIVE', 'PREMIUM'),
('b2c_kr', 'INTERNAL_ADMIN', '내부 관리팀', 'INTERNAL', NULL, 'ACTIVE', 'ENTERPRISE'),
('b2b_global', 'COMPANY_001', 'Global Trade Co.', 'COMPANY', '987-65-43210', 'ACTIVE', 'ENTERPRISE');
