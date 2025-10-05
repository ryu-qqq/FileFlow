-- ========================================
-- V5: Insert initial data
-- ========================================
-- 초기 테넌트 및 정책 데이터 삽입
-- - b2c, b2b 테넌트
-- - b2c:CONSUMER:REVIEW 정책
-- - b2c:SELLER:PRODUCT 정책
-- - b2c:CRAWLER:PRODUCT 정책
-- - b2b:BUYER:ORDER_SHEET 정책
-- ========================================

-- ========================================
-- 1. Tenant 초기 데이터
-- ========================================
INSERT INTO tenant (tenant_id, name, created_at, updated_at) VALUES
('b2c', 'B2C Platform', NOW(), NOW()),
('b2b', 'B2B Platform', NOW(), NOW());

-- ========================================
-- 2. Upload Policy 초기 데이터
-- ========================================

-- b2c:CONSUMER:REVIEW 정책
INSERT INTO upload_policy (
    policy_key,
    file_type_policies,
    rate_limiting,
    version,
    is_active,
    effective_from,
    effective_until,
    created_at,
    updated_at
) VALUES (
    'b2c:CONSUMER:REVIEW',
    '{
        "IMAGE": {
            "maxSizeBytes": 10485760,
            "maxFileCount": 5,
            "allowedFormats": ["JPG", "PNG", "WEBP"],
            "maxWidth": 2048,
            "maxHeight": 2048
        }
    }',
    '{
        "requestsPerHour": 100,
        "uploadsPerDay": 50
    }',
    1,
    TRUE,
    '2025-01-01 00:00:00',
    '2099-12-31 23:59:59',
    NOW(),
    NOW()
);

-- b2c:SELLER:PRODUCT 정책
INSERT INTO upload_policy (
    policy_key,
    file_type_policies,
    rate_limiting,
    version,
    is_active,
    effective_from,
    effective_until,
    created_at,
    updated_at
) VALUES (
    'b2c:SELLER:PRODUCT',
    '{
        "IMAGE": {
            "maxSizeBytes": 20971520,
            "maxFileCount": 10,
            "allowedFormats": ["JPG", "PNG", "WEBP"],
            "maxWidth": 4096,
            "maxHeight": 4096
        },
        "PDF": {
            "maxSizeBytes": 52428800,
            "maxFileCount": 3,
            "maxPages": 50
        }
    }',
    '{
        "requestsPerHour": 500,
        "uploadsPerDay": 200
    }',
    1,
    TRUE,
    '2025-01-01 00:00:00',
    '2099-12-31 23:59:59',
    NOW(),
    NOW()
);

-- b2c:CRAWLER:PRODUCT 정책
INSERT INTO upload_policy (
    policy_key,
    file_type_policies,
    rate_limiting,
    version,
    is_active,
    effective_from,
    effective_until,
    created_at,
    updated_at
) VALUES (
    'b2c:CRAWLER:PRODUCT',
    '{
        "HTML": {
            "maxSizeBytes": 1048576,
            "maxFileCount": 100
        },
        "IMAGE": {
            "maxSizeBytes": 5242880,
            "maxFileCount": 50,
            "allowedFormats": ["JPG", "PNG", "WEBP"],
            "maxWidth": 2048,
            "maxHeight": 2048
        }
    }',
    '{
        "requestsPerHour": 10000,
        "uploadsPerDay": 50000
    }',
    1,
    TRUE,
    '2025-01-01 00:00:00',
    '2099-12-31 23:59:59',
    NOW(),
    NOW()
);

-- b2b:BUYER:ORDER_SHEET 정책
INSERT INTO upload_policy (
    policy_key,
    file_type_policies,
    rate_limiting,
    version,
    is_active,
    effective_from,
    effective_until,
    created_at,
    updated_at
) VALUES (
    'b2b:BUYER:ORDER_SHEET',
    '{
        "EXCEL": {
            "maxSizeBytes": 10485760,
            "maxFileCount": 5,
            "maxRows": 10000,
            "maxColumns": 100
        },
        "PDF": {
            "maxSizeBytes": 20971520,
            "maxFileCount": 3,
            "maxPages": 100
        }
    }',
    '{
        "requestsPerHour": 200,
        "uploadsPerDay": 100
    }',
    1,
    TRUE,
    '2025-01-01 00:00:00',
    '2099-12-31 23:59:59',
    NOW(),
    NOW()
);
