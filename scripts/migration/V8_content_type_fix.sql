-- ============================================================
-- V8: Content-Type / Extension / S3 Key 복합 마이그레이션
-- ============================================================
-- 목적:
--   CDN 이미지 리사이징 URL에서 확장자 추출 실패로 인한 3가지 오류 수정
--   1) asset.content_type = 'application/octet-stream' → 올바른 MIME 타입
--   2) asset.extension = '' → 올바른 확장자
--   3) s3_key에 확장자 누락 → 확장자 추가
--   4) marketplace.product_group_images.uploaded_url도 동기화
--
-- 원인:
--   SourceUrl.extractExtension()이 URL 마지막 path segment에서 확장자를 추출하지만
--   CDN URL: .../abc.jpeg/_dims_/resize/300x300/extent/300x400
--   마지막 segment = "300x400" → 확장자 없음 → s3_key에도 확장자 미포함
--
-- 실행 순서:
--   1. s3_content_type_copy.sh --dry-run  (S3 복사 대상 확인)
--   2. s3_content_type_copy.sh            (S3 오브젝트 복사)
--   3. 이 SQL 스크립트 실행                (DB 업데이트)
--   4. 검증 쿼리 실행
--   5. s3_content_type_copy.sh --cleanup  (구 S3 오브젝트 삭제)
--
-- 주의사항:
--   - 반드시 S3 복사가 먼저 완료된 후 실행
--   - marketplace 스키마명 확인 필요 (기본값: setof_commerce)
-- ============================================================

-- ============================================================
-- 변수 설정 (환경에 맞게 수정)
-- ============================================================
SET @marketplace_schema = 'setof_commerce';
SET @cdn_base_url = 'https://cdn.set-of.com/';


-- ============================================================
-- 0. DRY RUN - 대상 건수 및 패턴 분석
-- ============================================================

-- 0-1. 전체 대상 건수
SELECT COUNT(*) AS total_octet_stream_assets
FROM asset
WHERE content_type = 'application/octet-stream'
  AND deleted_at IS NULL;

-- 0-2. source_url 내 확장자별 분포
SELECT
    CASE
        WHEN dt.source_url REGEXP '\\.(jpe?g)([/?#_/]|$)' THEN 'jpeg'
        WHEN dt.source_url REGEXP '\\.(png)([/?#_/]|$)' THEN 'png'
        WHEN dt.source_url REGEXP '\\.(gif)([/?#_/]|$)' THEN 'gif'
        WHEN dt.source_url REGEXP '\\.(webp)([/?#_/]|$)' THEN 'webp'
        WHEN dt.source_url REGEXP '\\.(svg)([/?#_/]|$)' THEN 'svg'
        WHEN dt.source_url REGEXP '\\.(bmp)([/?#_/]|$)' THEN 'bmp'
        WHEN dt.source_url REGEXP '\\.(tiff?)([/?#_/]|$)' THEN 'tiff'
        WHEN dt.source_url REGEXP '\\.(avif)([/?#_/]|$)' THEN 'avif'
        WHEN dt.source_url REGEXP '\\.(heic)([/?#_/]|$)' THEN 'heic'
        WHEN dt.source_url REGEXP '\\.(pdf)([/?#_/]|$)' THEN 'pdf'
        ELSE 'unknown'
    END AS detected_ext,
    COUNT(*) AS cnt
FROM asset a
INNER JOIN download_task dt ON a.origin_id = dt.id
WHERE a.content_type = 'application/octet-stream'
  AND a.origin = 'EXTERNAL_DOWNLOAD'
  AND a.deleted_at IS NULL
GROUP BY detected_ext
ORDER BY cnt DESC;

-- 0-3. unknown 패턴 샘플 (수동 검토용)
SELECT a.id, a.s3_key, dt.source_url
FROM asset a
INNER JOIN download_task dt ON a.origin_id = dt.id
WHERE a.content_type = 'application/octet-stream'
  AND a.origin = 'EXTERNAL_DOWNLOAD'
  AND a.deleted_at IS NULL
  AND dt.source_url NOT REGEXP '\\.(jpe?g|png|gif|webp|svg|bmp|tiff?|avif|heic|pdf)([/?#_/]|$)'
LIMIT 30;

-- 0-4. s3_key 변경 미리보기 (10건 샘플)
SELECT
    a.id,
    a.s3_key AS current_s3_key,
    CASE
        WHEN dt.source_url REGEXP '\\.(jpe?g)([/?#_/]|$)' THEN CONCAT(a.s3_key, '.jpeg')
        WHEN dt.source_url REGEXP '\\.(png)([/?#_/]|$)' THEN CONCAT(a.s3_key, '.png')
        WHEN dt.source_url REGEXP '\\.(gif)([/?#_/]|$)' THEN CONCAT(a.s3_key, '.gif')
        WHEN dt.source_url REGEXP '\\.(webp)([/?#_/]|$)' THEN CONCAT(a.s3_key, '.webp')
        ELSE a.s3_key
    END AS new_s3_key,
    a.content_type AS current_type,
    dt.source_url
FROM asset a
INNER JOIN download_task dt ON a.origin_id = dt.id
WHERE a.content_type = 'application/octet-stream'
  AND a.origin = 'EXTERNAL_DOWNLOAD'
  AND a.deleted_at IS NULL
  AND dt.source_url REGEXP '\\.(jpe?g|png|gif|webp|svg|bmp|tiff?|avif|heic|pdf)([/?#_/]|$)'
LIMIT 10;


-- ============================================================
-- 1. FileFlow DB 업데이트 (asset + download_task)
-- ============================================================
-- 확장자를 탐지할 수 있는 레코드만 업데이트
-- 매칭 실패 시 기존값 유지 (안전)

START TRANSACTION;

-- 1-1. 확장자/Content-Type 매핑용 임시 테이블
DROP TEMPORARY TABLE IF EXISTS _content_type_fix;
CREATE TEMPORARY TABLE _content_type_fix (
    asset_id VARCHAR(36) NOT NULL,
    download_task_id VARCHAR(36) NOT NULL,
    old_s3_key VARCHAR(512) NOT NULL,
    new_extension VARCHAR(20) NOT NULL,
    new_content_type VARCHAR(100) NOT NULL,
    new_s3_key VARCHAR(512) NOT NULL,
    PRIMARY KEY (asset_id)
);

INSERT INTO _content_type_fix (asset_id, download_task_id, old_s3_key, new_extension, new_content_type, new_s3_key)
SELECT
    a.id,
    dt.id,
    a.s3_key,
    CASE
        WHEN dt.source_url REGEXP '\\.(jpe?g)([/?#_/]|$)' THEN 'jpeg'
        WHEN dt.source_url REGEXP '\\.(png)([/?#_/]|$)' THEN 'png'
        WHEN dt.source_url REGEXP '\\.(gif)([/?#_/]|$)' THEN 'gif'
        WHEN dt.source_url REGEXP '\\.(webp)([/?#_/]|$)' THEN 'webp'
        WHEN dt.source_url REGEXP '\\.(svg)([/?#_/]|$)' THEN 'svg'
        WHEN dt.source_url REGEXP '\\.(bmp)([/?#_/]|$)' THEN 'bmp'
        WHEN dt.source_url REGEXP '\\.(tiff?)([/?#_/]|$)' THEN 'tiff'
        WHEN dt.source_url REGEXP '\\.(avif)([/?#_/]|$)' THEN 'avif'
        WHEN dt.source_url REGEXP '\\.(heic)([/?#_/]|$)' THEN 'heic'
        WHEN dt.source_url REGEXP '\\.(pdf)([/?#_/]|$)' THEN 'pdf'
        ELSE ''
    END,
    CASE
        WHEN dt.source_url REGEXP '\\.(jpe?g)([/?#_/]|$)' THEN 'image/jpeg'
        WHEN dt.source_url REGEXP '\\.(png)([/?#_/]|$)' THEN 'image/png'
        WHEN dt.source_url REGEXP '\\.(gif)([/?#_/]|$)' THEN 'image/gif'
        WHEN dt.source_url REGEXP '\\.(webp)([/?#_/]|$)' THEN 'image/webp'
        WHEN dt.source_url REGEXP '\\.(svg)([/?#_/]|$)' THEN 'image/svg+xml'
        WHEN dt.source_url REGEXP '\\.(bmp)([/?#_/]|$)' THEN 'image/bmp'
        WHEN dt.source_url REGEXP '\\.(tiff?)([/?#_/]|$)' THEN 'image/tiff'
        WHEN dt.source_url REGEXP '\\.(avif)([/?#_/]|$)' THEN 'image/avif'
        WHEN dt.source_url REGEXP '\\.(heic)([/?#_/]|$)' THEN 'image/heic'
        WHEN dt.source_url REGEXP '\\.(pdf)([/?#_/]|$)' THEN 'application/pdf'
        ELSE 'application/octet-stream'
    END,
    CASE
        WHEN dt.source_url REGEXP '\\.(jpe?g)([/?#_/]|$)' THEN CONCAT(a.s3_key, '.jpeg')
        WHEN dt.source_url REGEXP '\\.(png)([/?#_/]|$)' THEN CONCAT(a.s3_key, '.png')
        WHEN dt.source_url REGEXP '\\.(gif)([/?#_/]|$)' THEN CONCAT(a.s3_key, '.gif')
        WHEN dt.source_url REGEXP '\\.(webp)([/?#_/]|$)' THEN CONCAT(a.s3_key, '.webp')
        WHEN dt.source_url REGEXP '\\.(svg)([/?#_/]|$)' THEN CONCAT(a.s3_key, '.svg')
        WHEN dt.source_url REGEXP '\\.(bmp)([/?#_/]|$)' THEN CONCAT(a.s3_key, '.bmp')
        WHEN dt.source_url REGEXP '\\.(tiff?)([/?#_/]|$)' THEN CONCAT(a.s3_key, '.tiff')
        WHEN dt.source_url REGEXP '\\.(avif)([/?#_/]|$)' THEN CONCAT(a.s3_key, '.avif')
        WHEN dt.source_url REGEXP '\\.(heic)([/?#_/]|$)' THEN CONCAT(a.s3_key, '.heic')
        WHEN dt.source_url REGEXP '\\.(pdf)([/?#_/]|$)' THEN CONCAT(a.s3_key, '.pdf')
        ELSE a.s3_key
    END
FROM asset a
INNER JOIN download_task dt ON a.origin_id = dt.id
WHERE a.content_type = 'application/octet-stream'
  AND a.origin = 'EXTERNAL_DOWNLOAD'
  AND a.deleted_at IS NULL
  AND dt.source_url REGEXP '\\.(jpe?g|png|gif|webp|svg|bmp|tiff?|avif|heic|pdf)([/?#_/]|$)';

-- 임시 테이블 건수 확인
SELECT COUNT(*) AS fix_target_count FROM _content_type_fix;

-- 1-2. asset 업데이트 (content_type + extension + s3_key)
UPDATE asset a
INNER JOIN _content_type_fix f ON a.id = f.asset_id
SET a.content_type = f.new_content_type,
    a.extension = f.new_extension,
    a.s3_key = f.new_s3_key;

-- 1-3. download_task 업데이트 (s3_key)
UPDATE download_task dt
INNER JOIN _content_type_fix f ON dt.id = f.download_task_id
SET dt.s3_key = f.new_s3_key;

-- 결과 확인
SELECT '=== asset 업데이트 결과 ===' AS section;
SELECT content_type, COUNT(*) AS cnt
FROM asset
WHERE origin = 'EXTERNAL_DOWNLOAD' AND deleted_at IS NULL
GROUP BY content_type
ORDER BY cnt DESC;

SELECT '=== 남은 octet-stream ===' AS section;
SELECT COUNT(*) AS remaining
FROM asset
WHERE content_type = 'application/octet-stream' AND deleted_at IS NULL;


-- ============================================================
-- 2. Marketplace DB 업데이트 (product_group_images.uploaded_url)
-- ============================================================
-- 같은 RDS 인스턴스, 다른 스키마
-- uploaded_url = 'https://cdn.set-of.com/' + s3_key
-- old: https://cdn.set-of.com/public/2026/01/download-001
-- new: https://cdn.set-of.com/public/2026/01/download-001.jpeg

-- 2-1. 대상 건수 확인
SELECT '=== marketplace 업데이트 대상 ===' AS section;

-- NOTE: 아래 쿼리의 스키마명을 실제 환경에 맞게 수정하세요
-- 기본값: setof_commerce

SELECT COUNT(*) AS marketplace_target_count
FROM setof_commerce.product_group_images pgi
INNER JOIN _content_type_fix f
    ON pgi.uploaded_url = CONCAT('https://cdn.set-of.com/', f.old_s3_key);

-- 2-2. 미리보기 (10건)
SELECT
    pgi.id,
    pgi.uploaded_url AS current_url,
    CONCAT('https://cdn.set-of.com/', f.new_s3_key) AS new_url
FROM setof_commerce.product_group_images pgi
INNER JOIN _content_type_fix f
    ON pgi.uploaded_url = CONCAT('https://cdn.set-of.com/', f.old_s3_key)
LIMIT 10;

-- 2-3. marketplace 업데이트
UPDATE setof_commerce.product_group_images pgi
INNER JOIN _content_type_fix f
    ON pgi.uploaded_url = CONCAT('https://cdn.set-of.com/', f.old_s3_key)
SET pgi.uploaded_url = CONCAT('https://cdn.set-of.com/', f.new_s3_key);


-- ============================================================
-- 3. 최종 검증
-- ============================================================
SELECT '=== 최종 검증 ===' AS section;

-- 3-1. FileFlow asset 검증
SELECT
    'asset' AS tbl,
    SUM(CASE WHEN content_type = 'application/octet-stream' THEN 1 ELSE 0 END) AS remaining_octet,
    SUM(CASE WHEN extension = '' THEN 1 ELSE 0 END) AS remaining_no_ext,
    COUNT(*) AS total
FROM asset
WHERE origin = 'EXTERNAL_DOWNLOAD' AND deleted_at IS NULL;

-- 3-2. s3_key 일관성 검증 (download_task ↔ asset)
SELECT COUNT(*) AS mismatched_s3_keys
FROM asset a
INNER JOIN download_task dt ON a.origin_id = dt.id
WHERE a.origin = 'EXTERNAL_DOWNLOAD'
  AND a.s3_key != dt.s3_key
  AND a.deleted_at IS NULL;

-- 3-3. marketplace URL 검증 (샘플)
SELECT a.id, a.s3_key, pgi.uploaded_url
FROM asset a
INNER JOIN _content_type_fix f ON a.id = f.asset_id
LEFT JOIN setof_commerce.product_group_images pgi
    ON pgi.uploaded_url = CONCAT('https://cdn.set-of.com/', a.s3_key)
LIMIT 10;

-- 임시 테이블 정리
DROP TEMPORARY TABLE IF EXISTS _content_type_fix;

-- 문제없으면 COMMIT, 문제 있으면 ROLLBACK
-- COMMIT;
-- ROLLBACK;
