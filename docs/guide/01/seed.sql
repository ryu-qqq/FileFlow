-- FileFlow seed.sql (Phase 1 minimal)
-- Assumes schema.sql has been applied.

START TRANSACTION;

-- 0) Optional demo tenant/org --------------------------------------------------
INSERT INTO tenants (id, name, status) VALUES
  ('tnt_demo', 'Demo Tenant', 'ACTIVE')
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO organizations (tenant_id, org_code, name, status) VALUES
  ('tnt_demo', 'brand-a', 'Brand A', 'ACTIVE')
ON DUPLICATE KEY UPDATE name=VALUES(name);

SET @org_id := (SELECT id FROM organizations WHERE tenant_id='tnt_demo' AND org_code='brand-a');

-- 1) Permission catalog --------------------------------------------------------
INSERT INTO permissions (code, description) VALUES
 ('file.upload','파일 업로드'),
 ('file.read','파일 조회'),
 ('file.delete','파일 삭제'),
 ('org.manage','조직 관리')
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- 2) Role catalog --------------------------------------------------------------
INSERT INTO roles (code, description) VALUES
 ('org.uploader','조직 업로더'),
 ('org.manager','조직 매니저'),
 ('tenant.admin','테넌트 관리자')
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- 3) ABAC condition schemas (optional examples) -------------------------------
INSERT INTO condition_schemas (name, cel_example, json_schema) VALUES
 ('file.upload.limit.v1',
  'in(res.mime, ["image/jpeg","image/png","application/pdf"]) && res.size_mb <= 20',
  NULL)
ON DUPLICATE KEY UPDATE cel_example=VALUES(cel_example);

-- 4) Role → Permission mappings (+scope/+condition) ---------------------------
-- org.uploader: upload (ORG scope, 20MB & jpeg/png/pdf), read (ORG scope)
INSERT INTO role_permissions (role_id, permission_id, scope, condition_name, condition_expr)
SELECT r.id, p.id, 'ORGANIZATION', 'file.upload.limit.v1',
       'in(res.mime, ["image/jpeg","image/png","application/pdf"]) && res.size_mb <= 20'
FROM roles r, permissions p
WHERE r.code='org.uploader' AND p.code='file.upload'
ON DUPLICATE KEY UPDATE condition_name=VALUES(condition_name), condition_expr=VALUES(condition_expr);

INSERT INTO role_permissions (role_id, permission_id, scope)
SELECT r.id, p.id, 'ORGANIZATION'
FROM roles r, permissions p
WHERE r.code='org.uploader' AND p.code='file.read'
ON DUPLICATE KEY UPDATE scope=VALUES(scope);

-- org.manager: read/delete (ORG scope)
INSERT INTO role_permissions (role_id, permission_id, scope)
SELECT r.id, p.id, 'ORGANIZATION'
FROM roles r, permissions p
WHERE r.code='org.manager' AND p.code='file.read'
ON DUPLICATE KEY UPDATE scope=VALUES(scope);

INSERT INTO role_permissions (role_id, permission_id, scope)
SELECT r.id, p.id, 'ORGANIZATION'
FROM roles r, permissions p
WHERE r.code='org.manager' AND p.code='file.delete'
ON DUPLICATE KEY UPDATE scope=VALUES(scope);

-- tenant.admin: org.manage (TENANT scope), read/delete (TENANT scope)
INSERT INTO role_permissions (role_id, permission_id, scope)
SELECT r.id, p.id, 'TENANT'
FROM roles r, permissions p
WHERE r.code='tenant.admin' AND p.code='org.manage'
ON DUPLICATE KEY UPDATE scope=VALUES(scope);

INSERT INTO role_permissions (role_id, permission_id, scope)
SELECT r.id, p.id, 'TENANT'
FROM roles r, permissions p
WHERE r.code='tenant.admin' AND p.code='file.read'
ON DUPLICATE KEY UPDATE scope=VALUES(scope);

INSERT INTO role_permissions (role_id, permission_id, scope)
SELECT r.id, p.id, 'TENANT'
FROM roles r, permissions p
WHERE r.code='tenant.admin' AND p.code='file.delete'
ON DUPLICATE KEY UPDATE scope=VALUES(scope);

-- 5) Demo users & memberships (optional) --------------------------------------
-- One demo user bound to brand-a org
INSERT INTO user_contexts (external_user_id, email, display_name)
VALUES ('ext_demo_user', 'demo@fileflow.local', 'Demo User')
ON DUPLICATE KEY UPDATE email=VALUES(email), display_name=VALUES(display_name);

SET @user_id := (SELECT id FROM user_contexts WHERE external_user_id='ext_demo_user');

INSERT INTO user_org_memberships (user_context_id, tenant_id, organization_id, membership_type)
VALUES (@user_id, 'tnt_demo', @org_id, 'EMPLOYEE')
ON DUPLICATE KEY UPDATE membership_type=VALUES(membership_type);

-- Grant org.uploader in ORG scope
INSERT INTO user_role_mappings (user_context_id, role_id, tenant_id, organization_id)
SELECT @user_id, r.id, 'tnt_demo', @org_id FROM roles r WHERE r.code='org.uploader'
ON DUPLICATE KEY UPDATE organization_id=VALUES(organization_id);

COMMIT;
