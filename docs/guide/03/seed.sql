-- FileFlow Phase 3 seed.sql (File Management Initial Data)
-- Assumes Phase 1 and Phase 2 seed.sql have been applied
-- Assumes schema.sql has been applied

START TRANSACTION;

-- 1) File Management-specific permissions -------------------------------------
INSERT INTO permissions (code, description) VALUES
  ('file.read', '파일 메타데이터 조회'),
  ('file.download', '파일 다운로드'),
  ('file.delete', '파일 삭제'),
  ('file.variant.create', '파일 변형 생성'),
  ('file.relationship.create', '파일 관계 설정'),
  ('file.visibility.update', '파일 가시성 변경')
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- 2) File Management-specific roles -------------------------------------------
-- org.uploader, org.manager에 추가 권한 매핑

-- org.uploader: file.read, file.download (자신이 업로드한 파일)
INSERT INTO role_permissions (role_id, permission_id, scope, abac_condition)
SELECT r.id, p.id, 'ORGANIZATION', 'resource.ownerId == user.id'
FROM roles r, permissions p
WHERE r.code='org.uploader' AND p.code='file.read'
ON DUPLICATE KEY UPDATE scope=VALUES(scope);

INSERT INTO role_permissions (role_id, permission_id, scope, abac_condition)
SELECT r.id, p.id, 'ORGANIZATION', 'resource.ownerId == user.id'
FROM roles r, permissions p
WHERE r.code='org.uploader' AND p.code='file.download'
ON DUPLICATE KEY UPDATE scope=VALUES(scope);

-- org.manager: 모든 파일 관리 권한 (ORG scope)
INSERT INTO role_permissions (role_id, permission_id, scope)
SELECT r.id, p.id, 'ORGANIZATION'
FROM roles r, permissions p
WHERE r.code='org.manager' AND p.code IN (
  'file.read', 'file.download', 'file.delete',
  'file.variant.create', 'file.relationship.create', 'file.visibility.update'
)
ON DUPLICATE KEY UPDATE scope=VALUES(scope);

-- 3) File Management policy settings ------------------------------------------
-- Default policy: 90일 만료, PRIVATE 기본 가시성
INSERT INTO setting_schemas (key_name, value_type, json_schema, is_secret) VALUES
  ('file.default_expiration_days', 'INT', NULL, 0),
  ('file.default_visibility', 'STRING', NULL, 0),
  ('file.enable_auto_variant', 'BOOL', NULL, 0),
  ('file.thumbnail_size', 'INT', NULL, 0),
  ('file.max_download_url_expiration', 'INT', NULL, 0)
ON DUPLICATE KEY UPDATE value_type=VALUES(value_type);

-- Demo tenant specific settings
INSERT INTO tenant_settings (tenant_id, key_name, value_raw) VALUES
  ('tnt_demo', 'file.default_expiration_days', '90'),
  ('tnt_demo', 'file.default_visibility', 'PRIVATE'),
  ('tnt_demo', 'file.enable_auto_variant', 'true'),
  ('tnt_demo', 'file.thumbnail_size', '200'),
  ('tnt_demo', 'file.max_download_url_expiration', '15')
ON DUPLICATE KEY UPDATE value_raw=VALUES(value_raw);

-- Demo organization (Brand A) specific settings (더 짧은 만료 기간)
SET @org_id := (SELECT id FROM organizations WHERE tenant_id='tnt_demo' AND org_code='brand-a');

INSERT INTO organization_settings (tenant_id, organization_id, key_name, value_raw) VALUES
  ('tnt_demo', @org_id, 'file.default_expiration_days', '30'),    -- 30일 (더 짧음)
  ('tnt_demo', @org_id, 'file.default_visibility', 'INTERNAL'),   -- INTERNAL 기본값
  ('tnt_demo', @org_id, 'file.thumbnail_size', '150')             -- 더 작은 썸네일
ON DUPLICATE KEY UPDATE value_raw=VALUES(value_raw);

-- 4) Sample file asset --------------------------------------------------------
-- Phase 2의 완료된 업로드 세션으로부터 생성된 FileAsset
SET @demo_user_id := (SELECT id FROM user_contexts WHERE external_user_id='ext_demo_user');
SET @upload_session_id := 'sess_demo_001';

INSERT INTO file_assets (
  id, upload_session_id, owner_user_context_id,
  tenant_id, organization_id,
  s3_bucket, s3_key, mime_type, file_size_bytes, checksum_sha256,
  visibility, expires_at, created_at, deleted_at
) VALUES (
  'file_demo_001',
  @upload_session_id,
  @demo_user_id,
  'tnt_demo',
  @org_id,
  'fileflow-demo-bucket',
  'tenants/tnt_demo/orgs/1/uploads/demo-uuid-001/sample.jpg',
  'image/jpeg',
  1048576,  -- 1MB
  'abc123def456...',  -- Mock SHA256
  'PRIVATE',
  NOW() + INTERVAL 90 DAY,
  NOW() - INTERVAL 1 HOUR,
  NULL  -- Not deleted
)
ON DUPLICATE KEY UPDATE visibility=VALUES(visibility);

-- 5) Sample file variants -----------------------------------------------------
-- 원본 파일의 썸네일 변형
INSERT INTO file_variants (
  id, parent_file_asset_id, tenant_id, organization_id,
  variant_type, spec_width, spec_height, spec_format, spec_quality,
  s3_bucket, s3_key, file_size_bytes, created_at
) VALUES (
  'variant_demo_001',
  'file_demo_001',
  'tnt_demo',
  @org_id,
  'THUMBNAIL',
  200,
  200,
  'jpeg',
  85,
  'fileflow-demo-bucket',
  'tenants/tnt_demo/orgs/1/uploads/demo-uuid-001/sample_thumb_200x200.jpg',
  51200,  -- 50KB (썸네일)
  NOW() - INTERVAL 50 MINUTE
)
ON DUPLICATE KEY UPDATE variant_type=VALUES(variant_type);

-- 최적화된 WebP 변형
INSERT INTO file_variants (
  id, parent_file_asset_id, tenant_id, organization_id,
  variant_type, spec_width, spec_height, spec_format, spec_quality,
  s3_bucket, s3_key, file_size_bytes, created_at
) VALUES (
  'variant_demo_002',
  'file_demo_001',
  'tnt_demo',
  @org_id,
  'OPTIMIZED',
  NULL,
  NULL,
  'webp',
  90,
  'fileflow-demo-bucket',
  'tenants/tnt_demo/orgs/1/uploads/demo-uuid-001/sample_optimized.webp',
  768000,  -- 750KB (WebP 최적화)
  NOW() - INTERVAL 45 MINUTE
)
ON DUPLICATE KEY UPDATE variant_type=VALUES(variant_type);

-- 6) Sample file relationships ------------------------------------------------
-- 버전 관계 예제 (이전 버전 → 새 버전)
INSERT INTO file_assets (
  id, upload_session_id, owner_user_context_id,
  tenant_id, organization_id,
  s3_bucket, s3_key, mime_type, file_size_bytes, checksum_sha256,
  visibility, expires_at, created_at, deleted_at
) VALUES (
  'file_demo_002',
  'sess_demo_002',
  @demo_user_id,
  'tnt_demo',
  @org_id,
  'fileflow-demo-bucket',
  'tenants/tnt_demo/orgs/1/uploads/demo-uuid-002/sample_v2.jpg',
  'image/jpeg',
  1152000,  -- 1.1MB (수정된 버전)
  'def789ghi012...',
  'PRIVATE',
  NOW() + INTERVAL 90 DAY,
  NOW() - INTERVAL 30 MINUTE,
  NULL
)
ON DUPLICATE KEY UPDATE visibility=VALUES(visibility);

-- 버전 관계: file_demo_001 (v1) → file_demo_002 (v2)
INSERT INTO file_relationships (
  source_file_asset_id, target_file_asset_id, relationship_type,
  tenant_id, organization_id, created_at
) VALUES (
  'file_demo_001',
  'file_demo_002',
  'VERSION',
  'tnt_demo',
  @org_id,
  NOW() - INTERVAL 25 MINUTE
)
ON DUPLICATE KEY UPDATE relationship_type=VALUES(relationship_type);

-- 참조 관계 예제 (문서 → 첨부 파일)
INSERT INTO file_assets (
  id, upload_session_id, owner_user_context_id,
  tenant_id, organization_id,
  s3_bucket, s3_key, mime_type, file_size_bytes, checksum_sha256,
  visibility, expires_at, created_at, deleted_at
) VALUES (
  'file_demo_003',
  'sess_demo_003',
  @demo_user_id,
  'tnt_demo',
  @org_id,
  'fileflow-demo-bucket',
  'tenants/tnt_demo/orgs/1/uploads/demo-uuid-003/document.pdf',
  'application/pdf',
  2097152,  -- 2MB
  'jkl345mno678...',
  'INTERNAL',
  NOW() + INTERVAL 90 DAY,
  NOW() - INTERVAL 20 MINUTE,
  NULL
)
ON DUPLICATE KEY UPDATE visibility=VALUES(visibility);

-- 참조 관계: file_demo_003 (문서) → file_demo_001 (첨부 이미지)
INSERT INTO file_relationships (
  source_file_asset_id, target_file_asset_id, relationship_type,
  tenant_id, organization_id, created_at
) VALUES (
  'file_demo_003',
  'file_demo_001',
  'REFERENCE',
  'tnt_demo',
  @org_id,
  NOW() - INTERVAL 15 MINUTE
)
ON DUPLICATE KEY UPDATE relationship_type=VALUES(relationship_type);

-- 7) Sample file access log ---------------------------------------------------
INSERT INTO file_access_log (
  file_asset_id, accessor_user_id, access_type,
  tenant_id, organization_id,
  accessed_at, request_ip, user_agent
) VALUES
  -- 파일 조회 로그
  ('file_demo_001', @demo_user_id, 'READ',
   'tnt_demo', @org_id,
   NOW() - INTERVAL 30 MINUTE, '192.168.1.100', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)'),

  -- 파일 다운로드 로그
  ('file_demo_001', @demo_user_id, 'DOWNLOAD',
   'tnt_demo', @org_id,
   NOW() - INTERVAL 25 MINUTE, '192.168.1.100', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)'),

  -- 다른 사용자의 문서 조회 (org.manager 역할)
  ('file_demo_003', @demo_user_id, 'READ',
   'tnt_demo', @org_id,
   NOW() - INTERVAL 10 MINUTE, '192.168.1.101', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)');

-- 8) Sample expired file asset ------------------------------------------------
-- 만료된 파일 예제 (Batch Job 테스트용)
INSERT INTO file_assets (
  id, upload_session_id, owner_user_context_id,
  tenant_id, organization_id,
  s3_bucket, s3_key, mime_type, file_size_bytes, checksum_sha256,
  visibility, expires_at, created_at, deleted_at
) VALUES (
  'file_demo_expired_001',
  'sess_demo_expired_001',
  @demo_user_id,
  'tnt_demo',
  @org_id,
  'fileflow-demo-bucket',
  'tenants/tnt_demo/orgs/1/uploads/demo-uuid-expired-001/expired.jpg',
  'image/jpeg',
  512000,  -- 500KB
  'xyz789abc012...',
  'PRIVATE',
  NOW() - INTERVAL 1 DAY,  -- 이미 만료됨
  NOW() - INTERVAL 10 DAY,
  NULL  -- 아직 삭제되지 않음 (Batch Job이 처리해야 함)
)
ON DUPLICATE KEY UPDATE expires_at=VALUES(expires_at);

COMMIT;
