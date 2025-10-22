-- FileFlow schema.sql (Greenfield, Production-first)
-- Engine: MySQL 8.x (FK intentionally omitted; soft-delete + indexes included)

-- 1) Tenants / Organizations --------------------------------------------------
CREATE TABLE IF NOT EXISTS tenants (
  id            VARCHAR(50) PRIMARY KEY,
  name          VARCHAR(200) NOT NULL,
  status        ENUM('ACTIVE','SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at    DATETIME NULL,
  UNIQUE KEY uk_tenant_name (name),
  INDEX idx_tenant_status (status),
  INDEX idx_tenant_deleted (deleted_at)
);

CREATE TABLE IF NOT EXISTS organizations (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id     VARCHAR(50) NOT NULL,
  org_code      VARCHAR(100) NOT NULL,
  name          VARCHAR(200) NOT NULL,
  status        ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at    DATETIME NULL,
  UNIQUE KEY uk_tenant_org_code (tenant_id, org_code),
  INDEX idx_org_tenant_status (tenant_id, status),
  INDEX idx_org_deleted (deleted_at)
);

-- 2) Settings (EAV) + Schema --------------------------------------------------
CREATE TABLE IF NOT EXISTS setting_schemas (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  key_name     VARCHAR(150) UNIQUE NOT NULL,
  value_type   ENUM('STRING','INT','BOOL','JSON') NOT NULL,
  json_schema  JSON NULL,
  is_secret    TINYINT(1) NOT NULL DEFAULT 0,
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tenant_settings (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id    VARCHAR(50) NOT NULL,
  key_name     VARCHAR(150) NOT NULL,
  value_raw    TEXT NULL,
  updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_tenant_key (tenant_id, key_name),
  INDEX idx_tenant_key (tenant_id, key_name)
);

CREATE TABLE IF NOT EXISTS organization_settings (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id       VARCHAR(50) NOT NULL,
  organization_id BIGINT NOT NULL,
  key_name        VARCHAR(150) NOT NULL,
  value_raw       TEXT NULL,
  updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_org_key (tenant_id, organization_id, key_name),
  INDEX idx_org_key (tenant_id, organization_id, key_name)
);

-- 3) Users & Memberships ------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_contexts (
  id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
  external_user_id   VARCHAR(200) NOT NULL,
  email              VARCHAR(200) NULL,
  display_name       VARCHAR(200) NULL,
  created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_external (external_user_id)
);

CREATE TABLE IF NOT EXISTS user_org_memberships (
  id               BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_context_id  BIGINT NOT NULL,
  tenant_id        VARCHAR(50) NOT NULL,
  organization_id  BIGINT NULL,
  membership_type  ENUM('EMPLOYEE','SELLER_MEMBER','GUEST','SYSTEM') NOT NULL,
  created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_tenant_org (user_context_id, tenant_id, organization_id),
  INDEX idx_um_user (user_context_id),
  INDEX idx_um_scope (tenant_id, organization_id)
);

-- 4) RBAC + ABAC --------------------------------------------------------------
CREATE TABLE IF NOT EXISTS permissions (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  code         VARCHAR(150) UNIQUE NOT NULL,
  description  VARCHAR(300) NULL,
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at   DATETIME NULL,
  INDEX idx_perm_deleted (deleted_at)
);

CREATE TABLE IF NOT EXISTS roles (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  code         VARCHAR(150) UNIQUE NOT NULL,
  description  VARCHAR(300) NULL,
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at   DATETIME NULL,
  INDEX idx_role_deleted (deleted_at)
);

CREATE TABLE IF NOT EXISTS role_permissions (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id         BIGINT NOT NULL,
  permission_id   BIGINT NOT NULL,
  scope           ENUM('SELF','ORGANIZATION','TENANT','GLOBAL') NOT NULL,
  condition_name  VARCHAR(100) NULL,
  condition_expr  TEXT NULL,   -- CEL expression (optional)
  created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_role_perm_scope (role_id, permission_id, scope),
  INDEX idx_rp_role (role_id),
  INDEX idx_rp_perm (permission_id)
);

CREATE TABLE IF NOT EXISTS condition_schemas (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  name         VARCHAR(100) UNIQUE NOT NULL,
  cel_example  TEXT NULL,
  json_schema  JSON NULL,
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_role_mappings (
  id               BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_context_id  BIGINT NOT NULL,
  role_id          BIGINT NOT NULL,
  tenant_id        VARCHAR(50) NULL,
  organization_id  BIGINT NULL,
  resource_filter  JSON NULL,
  created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_urm_user_scope (user_context_id, tenant_id, organization_id),
  INDEX idx_urm_role (role_id)
);
