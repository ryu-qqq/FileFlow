-- ============================================================
-- V7: S3 Key 패턴 마이그레이션 (Stage 환경)
-- ============================================================
-- 목적: 기존 s3_key를 CloudFront Behavior 패턴으로 변환
--   기존: product-images/..., transformed/...
--   변경: {access_type}/{yyyy}/{MM}/{id}.{ext}
--
-- 실행 전 주의사항:
--   1. 반드시 S3 오브젝트 복사가 먼저 완료되어야 합니다 (s3_copy.sh)
--   2. 트랜잭션 내에서 실행하여 롤백 가능하도록 합니다
--   3. 실행 전 SELECT 쿼리로 대상 건수를 먼저 확인하세요
-- ============================================================

-- ============================================================
-- 0. 마이그레이션 대상 확인 (DRY RUN)
-- ============================================================

-- download_task: 기존 패턴 확인
SELECT id, s3_key, access_type, bucket,
       CREATED_AT,
       SUBSTRING_INDEX(source_url, '.', -1) AS url_ext
FROM download_task
WHERE s3_key NOT LIKE 'public/%'
  AND s3_key NOT LIKE 'internal/%';

-- asset: 기존 패턴 확인 (download로 생성된 것)
SELECT id, s3_key, access_type, origin, origin_id
FROM asset
WHERE origin = 'EXTERNAL_DOWNLOAD'
  AND s3_key NOT LIKE 'public/%'
  AND s3_key NOT LIKE 'internal/%'
  AND deleted_at IS NULL;

-- asset: transform으로 생성된 것
SELECT id, s3_key, access_type, origin, origin_id
FROM asset
WHERE s3_key LIKE 'transformed/%'
  AND deleted_at IS NULL;


-- ============================================================
-- 1. download_task s3_key 업데이트
-- ============================================================
-- 패턴: {access_type}/{year(created_at)}/{month(created_at)}/{id}.{ext}
-- ext는 source_url에서 추출, 없으면 id만 사용

START TRANSACTION;

UPDATE download_task
SET s3_key = CONCAT(
    LOWER(access_type), '/',
    YEAR(created_at), '/',
    LPAD(MONTH(created_at), 2, '0'), '/',
    id,
    CASE
        WHEN source_url REGEXP '\\.[a-zA-Z0-9]{1,10}(\\?|#|$)'
        THEN CONCAT('.', LOWER(
            SUBSTRING_INDEX(
                SUBSTRING_INDEX(
                    SUBSTRING_INDEX(source_url, '?', 1),
                    '#', 1
                ),
                '.', -1
            )
        ))
        ELSE ''
    END
),
    bucket = (SELECT variable_value FROM _migration_config WHERE variable_name = 'bucket'
              UNION ALL SELECT 'fileflow-stage' LIMIT 1)
WHERE s3_key NOT LIKE 'public/%'
  AND s3_key NOT LIKE 'internal/%';

-- 결과 확인
SELECT id, s3_key, access_type FROM download_task LIMIT 20;

-- 문제없으면 COMMIT, 문제 있으면 ROLLBACK
-- COMMIT;
-- ROLLBACK;


-- ============================================================
-- 2. asset s3_key 업데이트 (EXTERNAL_DOWNLOAD origin)
-- ============================================================
-- download_task의 s3_key를 따라감 (download 완료 시 asset이 동일 s3_key로 생성되므로)

UPDATE asset a
INNER JOIN download_task dt ON a.origin_id = dt.id
SET a.s3_key = dt.s3_key,
    a.bucket = dt.bucket
WHERE a.origin = 'EXTERNAL_DOWNLOAD'
  AND a.s3_key NOT LIKE 'public/%'
  AND a.s3_key NOT LIKE 'internal/%'
  AND a.deleted_at IS NULL;

-- 결과 확인
SELECT a.id, a.s3_key, a.origin, a.origin_id, dt.s3_key AS dt_s3_key
FROM asset a
INNER JOIN download_task dt ON a.origin_id = dt.id
WHERE a.origin = 'EXTERNAL_DOWNLOAD'
LIMIT 20;


-- ============================================================
-- 3. asset s3_key 업데이트 (Transform 결과)
-- ============================================================
-- transformed/{type}/{uuid}.{ext} → {access_type}/{year}/{month}/{id}.{ext}

UPDATE asset a
SET a.s3_key = CONCAT(
    LOWER(a.access_type), '/',
    YEAR(a.created_at), '/',
    LPAD(MONTH(a.created_at), 2, '0'), '/',
    a.id,
    CASE
        WHEN a.extension IS NOT NULL AND a.extension != ''
        THEN CONCAT('.', LOWER(a.extension))
        ELSE ''
    END
)
WHERE a.s3_key LIKE 'transformed/%'
  AND a.deleted_at IS NULL;

-- 결과 확인
SELECT id, s3_key, access_type, extension FROM asset
WHERE origin_id IN (SELECT id FROM transform_request)
LIMIT 20;


-- COMMIT;
