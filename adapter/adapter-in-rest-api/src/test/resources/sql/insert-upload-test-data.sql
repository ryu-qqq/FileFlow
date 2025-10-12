-- ========================================
-- Insert test data for upload integration tests
-- ========================================
-- Test data for various upload scenarios
-- ========================================

-- ========================================
-- 1. Test Upload Sessions
-- ========================================

-- Expired session (for expiry test)
INSERT INTO upload_session (
    session_id,
    tenant_id,
    policy_key,
    file_name,
    content_type,
    file_size,
    status,
    presigned_url,
    s3_key,
    expires_at,
    created_at,
    updated_at
) VALUES (
    '11111111-1111-1111-1111-111111111111',
    'b2c',
    'b2c:CONSUMER:REVIEW',
    'expired-test.jpg',
    'image/jpeg',
    1024000,
    'EXPIRED',
    'https://expired-presigned-url.com',
    'test/expired-test.jpg',
    DATE_SUB(NOW(), INTERVAL 1 HOUR),
    DATE_SUB(NOW(), INTERVAL 2 HOUR),
    NOW()
);

-- Completed session (for successful upload test)
INSERT INTO upload_session (
    session_id,
    tenant_id,
    policy_key,
    file_name,
    content_type,
    file_size,
    status,
    presigned_url,
    s3_key,
    upload_started_at,
    upload_completed_at,
    expires_at,
    created_at,
    updated_at
) VALUES (
    '22222222-2222-2222-2222-222222222222',
    'b2c',
    'b2c:CONSUMER:REVIEW',
    'completed-test.jpg',
    'image/jpeg',
    2048000,
    'COMPLETED',
    'https://completed-presigned-url.com',
    'test/completed-test.jpg',
    DATE_SUB(NOW(), INTERVAL 10 MINUTE),
    DATE_SUB(NOW(), INTERVAL 5 MINUTE),
    DATE_ADD(NOW(), INTERVAL 20 MINUTE),
    DATE_SUB(NOW(), INTERVAL 15 MINUTE),
    NOW()
);

-- In-progress session
INSERT INTO upload_session (
    session_id,
    tenant_id,
    policy_key,
    file_name,
    content_type,
    file_size,
    status,
    presigned_url,
    s3_key,
    upload_started_at,
    expires_at,
    created_at,
    updated_at
) VALUES (
    '33333333-3333-3333-3333-333333333333',
    'b2c',
    'b2c:SELLER:PRODUCT',
    'inprogress-test.png',
    'image/png',
    5120000,
    'IN_PROGRESS',
    'https://inprogress-presigned-url.com',
    'test/inprogress-test.png',
    DATE_SUB(NOW(), INTERVAL 5 MINUTE),
    DATE_ADD(NOW(), INTERVAL 25 MINUTE),
    DATE_SUB(NOW(), INTERVAL 10 MINUTE),
    NOW()
);

-- ========================================
-- 2. Test File Assets (for completed uploads)
-- ========================================

INSERT INTO file_asset (
    file_id,
    session_id,
    tenant_id,
    original_file_name,
    stored_file_name,
    content_type,
    file_size,
    s3_bucket,
    s3_key,
    s3_region,
    checksum,
    is_public,
    created_at,
    updated_at
) VALUES (
    '44444444-4444-4444-4444-444444444444',
    '22222222-2222-2222-2222-222222222222',
    'b2c',
    'completed-test.jpg',
    'stored-completed-test.jpg',
    'image/jpeg',
    2048000,
    'test-bucket',
    'test/completed-test.jpg',
    'us-east-1',
    'abc123def456',
    FALSE,
    DATE_SUB(NOW(), INTERVAL 15 MINUTE),
    NOW()
);

-- ========================================
-- 3. Test File Metadata (for completed uploads)
-- ========================================

-- Width metadata
INSERT INTO file_metadata (
    file_id,
    metadata_key,
    metadata_value,
    value_type,
    created_at,
    updated_at
) VALUES (
    '44444444-4444-4444-4444-444444444444',
    'width',
    '1920',
    'NUMBER',
    DATE_SUB(NOW(), INTERVAL 5 MINUTE),
    NOW()
);

-- Height metadata
INSERT INTO file_metadata (
    file_id,
    metadata_key,
    metadata_value,
    value_type,
    created_at,
    updated_at
) VALUES (
    '44444444-4444-4444-4444-444444444444',
    'height',
    '1080',
    'NUMBER',
    DATE_SUB(NOW(), INTERVAL 5 MINUTE),
    NOW()
);

-- Format metadata
INSERT INTO file_metadata (
    file_id,
    metadata_key,
    metadata_value,
    value_type,
    created_at,
    updated_at
) VALUES (
    '44444444-4444-4444-4444-444444444444',
    'format',
    'JPEG',
    'STRING',
    DATE_SUB(NOW(), INTERVAL 5 MINUTE),
    NOW()
);
