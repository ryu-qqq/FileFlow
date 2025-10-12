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
        "imagePolicy": {
            "maxFileSizeMB": 10,
            "maxFileCount": 5,
            "allowedFormats": ["jpg", "png", "webp"],
            "maxDimension": {
                "width": 2048,
                "height": 2048
            }
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
        "imagePolicy": {
            "maxFileSizeMB": 20,
            "maxFileCount": 10,
            "allowedFormats": ["jpg", "png", "webp"],
            "maxDimension": {
                "width": 4096,
                "height": 4096
            }
        },
        "videoPolicy": {
            "maxFileSizeMB": 500,
            "maxFileCount": 5,
            "allowedFormats": ["mp4", "avi", "mov"],
            "maxDurationSeconds": 600
        },
        "pdfPolicy": {
            "maxFileSizeMB": 50,
            "maxFileCount": 3,
            "maxPageCount": 50
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
        "htmlPolicy": {
            "maxFileSizeMB": 1,
            "maxImageCount": 100,
            "downloadExternalImages": false
        },
        "imagePolicy": {
            "maxFileSizeMB": 5,
            "maxFileCount": 50,
            "allowedFormats": ["jpg", "png", "webp"],
            "maxDimension": {
                "width": 2048,
                "height": 2048
            }
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
        "excelPolicy": {
            "maxFileSizeMB": 10,
            "maxSheetCount": 5
        },
        "pdfPolicy": {
            "maxFileSizeMB": 20,
            "maxPageCount": 100
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
