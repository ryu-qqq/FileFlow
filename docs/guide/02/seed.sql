-- FileFlow Phase 2 seed.sql (Upload Management Initial Data - Extended)
-- Assumes Phase 1 seed.sql has been applied (tenants, organizations, permissions, roles)
-- Assumes schema.sql has been applied (11 tables including multipart, external download, policies)

START TRANSACTION;

-- 1) Upload-specific permissions ------------------------------------------------
INSERT INTO permissions (code, description) VALUES
  ('file.read', '파일 조회'),
  ('file.delete', '파일 삭제'),
  ('file.download', '파일 다운로드')
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- file.upload은 Phase 1에서 이미 정의됨

-- 2) Upload-specific roles ------------------------------------------------------
-- 기존 org.uploader, org.manager 역할이 이미 있으므로 추가 권한만 매핑

-- org.uploader: file.read, file.download 추가
INSERT INTO role_permissions (role_id, permission_id, scope)
SELECT r.id, p.id, 'ORGANIZATION'
FROM roles r, permissions p
WHERE r.code='org.uploader' AND p.code IN ('file.read', 'file.download')
ON DUPLICATE KEY UPDATE scope=VALUES(scope);

-- org.manager: file.read, file.download, file.delete (ORG scope)
INSERT INTO role_permissions (role_id, permission_id, scope)
SELECT r.id, p.id, 'ORGANIZATION'
FROM roles r, permissions p
WHERE r.code='org.manager' AND p.code IN ('file.read', 'file.download', 'file.delete')
ON DUPLICATE KEY UPDATE scope=VALUES(scope);

-- 3) Upload policy settings (Extended) ------------------------------------------
-- schema.sql에서 이미 INSERT되었으므로 tenant/org 레벨 설정만 추가

-- Demo tenant specific settings (Extended)
INSERT INTO tenant_settings (tenant_id, key_name, value_raw) VALUES
  ('tnt_demo', 'upload.max_file_size_bytes', '104857600'),            -- 100MB
  ('tnt_demo', 'upload.allowed_mimes', 'image/jpeg,image/png,application/pdf'),
  ('tnt_demo', 'upload.enable_virus_scan', 'false'),
  ('tnt_demo', 'upload.multipart_threshold_mb', '10'),                -- 10MB 이상 멀티파트
  ('tnt_demo', 'upload.session_ttl_minutes', '15'),                   -- 15분 TTL
  ('tnt_demo', 'upload.enable_external_download', 'true'),
  ('tnt_demo', 'upload.external_download_timeout_seconds', '300')     -- 5분 타임아웃
ON DUPLICATE KEY UPDATE value_raw=VALUES(value_raw);

-- Demo organization (Brand A) specific settings (더 엄격한 정책)
SET @org_id := (SELECT id FROM organizations WHERE tenant_id='tnt_demo' AND org_code='brand-a');

INSERT INTO organization_settings (tenant_id, organization_id, key_name, value_raw) VALUES
  ('tnt_demo', @org_id, 'upload.max_file_size_bytes', '20971520'),   -- 20MB (더 엄격)
  ('tnt_demo', @org_id, 'upload.allowed_mimes', 'image/jpeg,image/png'),  -- PDF 제외
  ('tnt_demo', @org_id, 'upload.multipart_threshold_mb', '5')         -- 5MB 이상 멀티파트
ON DUPLICATE KEY UPDATE value_raw=VALUES(value_raw);

-- 4) Upload Policies (템플릿) ----------------------------------------------------
-- 테넌트 레벨 기본 정책
INSERT INTO upload_policies (
  tenant_id, organization_id, policy_name,
  allowed_mime_json, max_size_mb,
  time_window_start, time_window_end,
  rate_limit_per_min, daily_quota_mb,
  is_active, created_at
) VALUES (
  'tnt_demo', NULL, 'default-tenant-policy',
  '["image/jpeg","image/png","image/gif","application/pdf"]',
  100,  -- 100MB
  NULL, NULL,  -- 24시간 허용
  10,          -- 분당 10회
  1000,        -- 일일 1GB
  TRUE,
  NOW()
)
ON DUPLICATE KEY UPDATE policy_name=VALUES(policy_name);

-- 조직 레벨 정책 (Brand A - 더 엄격)
INSERT INTO upload_policies (
  tenant_id, organization_id, policy_name,
  allowed_mime_json, max_size_mb,
  time_window_start, time_window_end,
  rate_limit_per_min, daily_quota_mb,
  is_active, created_at
) VALUES (
  'tnt_demo', @org_id, 'brand-a-strict-policy',
  '["image/jpeg","image/png"]',  -- PDF 제외
  20,   -- 20MB
  9, 18,  -- 09:00 ~ 18:00만 허용
  5,    -- 분당 5회
  500,  -- 일일 500MB
  TRUE,
  NOW()
)
ON DUPLICATE KEY UPDATE policy_name=VALUES(policy_name);

-- 5) Sample upload session (싱글파트 완료) --------------------------------------
SET @demo_user_id := (SELECT id FROM user_contexts WHERE external_user_id='ext_demo_user');

-- 싱글파트 업로드 세션 (COMPLETED)
INSERT INTO upload_sessions (
  session_id, tenant_id, organization_id, uploader_user_context_id,
  file_id_hint, original_filename, expected_mime, expected_size_bytes,
  storage_provider, storage_bucket, storage_key,
  visibility, policy_snapshot_json, status,
  presigned_url, presigned_expires_at, expires_at,
  created_at, updated_at, completed_at, deleted_at
) VALUES (
  'usn_demo_single_001',
  'tnt_demo',
  @org_id,
  @demo_user_id,
  'f47ac10b-58cc-4372-a567-0e02b2c3d479',  -- UUID hint
  'sample.jpg',
  'image/jpeg',
  1048576,  -- 1MB
  'S3',
  'fileflow-demo-bucket',
  'tenants/tnt_demo/orgs/1/uploads/demo-uuid-001/sample.jpg',
  'PRIVATE',
  '{"maxSizeMb":20,"allowedMimes":["image/jpeg","image/png"],"timeWindow":{"start":9,"end":18}}',
  'COMPLETED',
  'https://s3.amazonaws.com/fileflow-demo-bucket/...?X-Amz-Signature=mock',  -- Mock Presigned URL
  NOW() + INTERVAL 14 HOUR,
  NOW() + INTERVAL 15 MINUTE,
  NOW() - INTERVAL 1 HOUR,
  NOW(),
  NOW(),
  NULL  -- Not deleted
)
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- 6) Sample multipart upload session (COMPLETED) --------------------------------
-- 멀티파트 업로드 세션 (대용량 파일)
INSERT INTO upload_sessions (
  session_id, tenant_id, organization_id, uploader_user_context_id,
  file_id_hint, original_filename, expected_mime, expected_size_bytes,
  storage_provider, storage_bucket, storage_key,
  visibility, policy_snapshot_json, status,
  presigned_url, presigned_expires_at, expires_at,
  created_at, updated_at, completed_at, deleted_at
) VALUES (
  'usn_demo_multi_001',
  'tnt_demo',
  @org_id,
  @demo_user_id,
  '550e8400-e29b-41d4-a716-446655440000',  -- UUID hint
  'large-video.mp4',
  'video/mp4',
  52428800,  -- 50MB
  'S3',
  'fileflow-demo-bucket',
  'tenants/tnt_demo/orgs/1/uploads/demo-uuid-002/large-video.mp4',
  'PRIVATE',
  '{"maxSizeMb":100,"allowedMimes":["video/mp4"],"timeWindow":null}',
  'COMPLETED',
  NULL,  -- 멀티파트는 presigned_url이 NULL
  NULL,
  NOW() + INTERVAL 15 MINUTE,
  NOW() - INTERVAL 2 HOUR,
  NOW() - INTERVAL 30 MINUTE,
  NOW() - INTERVAL 30 MINUTE,
  NULL
)
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- Multipart upload metadata
INSERT INTO upload_multipart (
  session_id, provider_upload_id, total_parts, uploaded_parts,
  status, created_at, updated_at, completed_at
) VALUES (
  'usn_demo_multi_001',
  'mock-s3-upload-id-12345',  -- S3 UploadId
  5,   -- 5개 파트
  5,   -- 5개 완료
  'COMPLETED',
  NOW() - INTERVAL 2 HOUR,
  NOW() - INTERVAL 30 MINUTE,
  NOW() - INTERVAL 30 MINUTE
)
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- Multipart parts (5개)
INSERT INTO upload_parts (session_id, part_no, etag, size_bytes, presigned_url, presigned_expires_at, status, created_at, updated_at) VALUES
  ('usn_demo_multi_001', 1, 'etag-part-1-mock', 10485760, NULL, NULL, 'COMPLETED', NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 2 HOUR + INTERVAL 5 MINUTE),
  ('usn_demo_multi_001', 2, 'etag-part-2-mock', 10485760, NULL, NULL, 'COMPLETED', NOW() - INTERVAL 2 HOUR + INTERVAL 5 MINUTE, NOW() - INTERVAL 2 HOUR + INTERVAL 10 MINUTE),
  ('usn_demo_multi_001', 3, 'etag-part-3-mock', 10485760, NULL, NULL, 'COMPLETED', NOW() - INTERVAL 2 HOUR + INTERVAL 10 MINUTE, NOW() - INTERVAL 2 HOUR + INTERVAL 15 MINUTE),
  ('usn_demo_multi_001', 4, 'etag-part-4-mock', 10485760, NULL, NULL, 'COMPLETED', NOW() - INTERVAL 2 HOUR + INTERVAL 15 MINUTE, NOW() - INTERVAL 2 HOUR + INTERVAL 20 MINUTE),
  ('usn_demo_multi_001', 5, 'etag-part-5-mock', 10485760, NULL, NULL, 'COMPLETED', NOW() - INTERVAL 2 HOUR + INTERVAL 20 MINUTE, NOW() - INTERVAL 30 MINUTE)
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- 7) Sample external download (COMPLETED) ----------------------------------------
INSERT INTO upload_sessions (
  session_id, tenant_id, organization_id, uploader_user_context_id,
  file_id_hint, original_filename, expected_mime, expected_size_bytes,
  storage_provider, storage_bucket, storage_key,
  visibility, policy_snapshot_json, status,
  presigned_url, presigned_expires_at, expires_at,
  created_at, updated_at, completed_at, deleted_at
) VALUES (
  'usn_demo_external_001',
  'tnt_demo',
  @org_id,
  @demo_user_id,
  'e3f5a6b8-c2d4-4f5e-9a8b-7c6d5e4f3a2b',  -- UUID hint
  'external-document.pdf',
  'application/pdf',
  2097152,  -- 2MB
  'S3',
  'fileflow-demo-bucket',
  'tenants/tnt_demo/orgs/1/uploads/demo-uuid-003/external-document.pdf',
  'INTERNAL',
  '{"maxSizeMb":20,"allowedMimes":["application/pdf"],"externalDownload":true}',
  'COMPLETED',
  NULL,
  NULL,
  NOW() + INTERVAL 15 MINUTE,
  NOW() - INTERVAL 3 HOUR,
  NOW() - INTERVAL 2 HOUR,
  NOW() - INTERVAL 2 HOUR,
  NULL
)
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- External download metadata
INSERT INTO external_downloads (
  session_id, source_url, byte_transferred, total_bytes,
  status, error_code, error_message, retry_count, max_retry,
  created_at, updated_at, completed_at
) VALUES (
  'usn_demo_external_001',
  'https://example.com/documents/sample.pdf',
  2097152,  -- 2MB 완료
  2097152,  -- 총 2MB
  'COMPLETED',
  NULL,
  NULL,
  0,  -- 재시도 없음
  3,  -- 최대 3회
  NOW() - INTERVAL 3 HOUR,
  NOW() - INTERVAL 2 HOUR,
  NOW() - INTERVAL 2 HOUR
)
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- 8) Sample file metadata -------------------------------------------------------
-- 싱글파트 완료 후 file_metadata 생성
INSERT INTO file_metadata (
  id, upload_session_id, owner_user_context_id,
  tenant_id, organization_id,
  original_filename, mime_type, file_size_bytes,
  s3_bucket, s3_key, s3_version_id, checksum_sha256,
  created_at, deleted_at
) VALUES (
  'file_demo_001',
  'usn_demo_single_001',
  @demo_user_id,
  'tnt_demo',
  @org_id,
  'sample.jpg',
  'image/jpeg',
  1048576,
  'fileflow-demo-bucket',
  'tenants/tnt_demo/orgs/1/uploads/demo-uuid-001/sample.jpg',
  NULL,
  'abc123def456...',  -- Mock SHA256
  NOW() - INTERVAL 1 HOUR,
  NULL  -- Not deleted
)
ON DUPLICATE KEY UPDATE original_filename=VALUES(original_filename);

-- 멀티파트 완료 후 file_metadata 생성
INSERT INTO file_metadata (
  id, upload_session_id, owner_user_context_id,
  tenant_id, organization_id,
  original_filename, mime_type, file_size_bytes,
  s3_bucket, s3_key, s3_version_id, checksum_sha256,
  created_at, deleted_at
) VALUES (
  'file_demo_002',
  'usn_demo_multi_001',
  @demo_user_id,
  'tnt_demo',
  @org_id,
  'large-video.mp4',
  'video/mp4',
  52428800,
  'fileflow-demo-bucket',
  'tenants/tnt_demo/orgs/1/uploads/demo-uuid-002/large-video.mp4',
  NULL,
  'def456ghi789...',  -- Mock SHA256
  NOW() - INTERVAL 30 MINUTE,
  NULL
)
ON DUPLICATE KEY UPDATE original_filename=VALUES(original_filename);

-- 외부 다운로드 완료 후 file_metadata 생성
INSERT INTO file_metadata (
  id, upload_session_id, owner_user_context_id,
  tenant_id, organization_id,
  original_filename, mime_type, file_size_bytes,
  s3_bucket, s3_key, s3_version_id, checksum_sha256,
  created_at, deleted_at
) VALUES (
  'file_demo_003',
  'usn_demo_external_001',
  @demo_user_id,
  'tnt_demo',
  @org_id,
  'external-document.pdf',
  'application/pdf',
  2097152,
  'fileflow-demo-bucket',
  'tenants/tnt_demo/orgs/1/uploads/demo-uuid-003/external-document.pdf',
  NULL,
  'ghi789jkl012...',  -- Mock SHA256
  NOW() - INTERVAL 2 HOUR,
  NULL
)
ON DUPLICATE KEY UPDATE original_filename=VALUES(original_filename);

-- 9) Sample file access log -----------------------------------------------------
INSERT INTO file_access_log (
  file_id, accessor_user_id, access_type,
  tenant_id, organization_id,
  accessed_at, request_ip, user_agent
) VALUES
  -- 싱글파트 파일 조회
  ('file_demo_001', @demo_user_id, 'READ',
   'tnt_demo', @org_id,
   NOW() - INTERVAL 30 MINUTE, '192.168.1.100', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)'),

  -- 싱글파트 파일 다운로드
  ('file_demo_001', @demo_user_id, 'DOWNLOAD',
   'tnt_demo', @org_id,
   NOW() - INTERVAL 25 MINUTE, '192.168.1.100', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)'),

  -- 멀티파트 비디오 조회
  ('file_demo_002', @demo_user_id, 'READ',
   'tnt_demo', @org_id,
   NOW() - INTERVAL 20 MINUTE, '192.168.1.100', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)'),

  -- 외부 다운로드 문서 조회
  ('file_demo_003', @demo_user_id, 'READ',
   'tnt_demo', @org_id,
   NOW() - INTERVAL 10 MINUTE, '192.168.1.101', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)');

-- 10) Sample upload session events ----------------------------------------------
-- 싱글파트 세션 이벤트
INSERT INTO upload_session_events (
  session_id, event_type, previous_status, new_status, reason, occurred_at
) VALUES
  ('usn_demo_single_001', 'CREATED', NULL, 'INIT', NULL, NOW() - INTERVAL 1 HOUR),
  ('usn_demo_single_001', 'UPLOADING', 'INIT', 'IN_PROGRESS', NULL, NOW() - INTERVAL 1 HOUR + INTERVAL 5 SECOND),
  ('usn_demo_single_001', 'COMPLETED', 'IN_PROGRESS', 'COMPLETED', NULL, NOW() - INTERVAL 1 HOUR + INTERVAL 2 MINUTE);

-- 멀티파트 세션 이벤트
INSERT INTO upload_session_events (
  session_id, event_type, previous_status, new_status, reason, occurred_at
) VALUES
  ('usn_demo_multi_001', 'CREATED', NULL, 'INIT', NULL, NOW() - INTERVAL 2 HOUR),
  ('usn_demo_multi_001', 'UPLOADING', 'INIT', 'IN_PROGRESS', 'Multipart init started', NOW() - INTERVAL 2 HOUR + INTERVAL 1 MINUTE),
  ('usn_demo_multi_001', 'COMPLETED', 'IN_PROGRESS', 'COMPLETED', 'All 5 parts uploaded successfully', NOW() - INTERVAL 30 MINUTE);

-- 외부 다운로드 세션 이벤트
INSERT INTO upload_session_events (
  session_id, event_type, previous_status, new_status, reason, occurred_at
) VALUES
  ('usn_demo_external_001', 'CREATED', NULL, 'INIT', NULL, NOW() - INTERVAL 3 HOUR),
  ('usn_demo_external_001', 'UPLOADING', 'INIT', 'IN_PROGRESS', 'External download started', NOW() - INTERVAL 3 HOUR + INTERVAL 1 MINUTE),
  ('usn_demo_external_001', 'COMPLETED', 'IN_PROGRESS', 'COMPLETED', 'External download completed (2MB)', NOW() - INTERVAL 2 HOUR);

-- 11) Sample upload errors ------------------------------------------------------
-- 실패한 세션 예제 (테스트용)
INSERT INTO upload_sessions (
  session_id, tenant_id, organization_id, uploader_user_context_id,
  file_id_hint, original_filename, expected_mime, expected_size_bytes,
  storage_provider, storage_bucket, storage_key,
  visibility, policy_snapshot_json, status,
  presigned_url, presigned_expires_at, expires_at,
  created_at, updated_at, completed_at, deleted_at
) VALUES (
  'usn_demo_failed_001',
  'tnt_demo',
  @org_id,
  @demo_user_id,
  'a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d',
  'failed-upload.mp4',
  'video/mp4',
  104857600,  -- 100MB
  'S3',
  'fileflow-demo-bucket',
  'tenants/tnt_demo/orgs/1/uploads/demo-uuid-failed-001/failed-upload.mp4',
  'PRIVATE',
  '{"maxSizeMb":100,"allowedMimes":["video/mp4"]}',
  'FAILED',
  NULL,
  NULL,
  NOW() + INTERVAL 15 MINUTE,
  NOW() - INTERVAL 4 HOUR,
  NOW() - INTERVAL 3 HOUR,
  NULL,
  NULL
)
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- Upload error log
INSERT INTO upload_errors (
  session_id, error_code, error_message, error_detail, created_at
) VALUES (
  'usn_demo_failed_001',
  'UP-500-IO',
  'S3 connection timeout during multipart upload',
  'com.amazonaws.SdkClientException: Unable to execute HTTP request: Connection timeout\n  at com.amazonaws...',
  NOW() - INTERVAL 3 HOUR
);

COMMIT;
